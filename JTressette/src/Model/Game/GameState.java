package Model.Game;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import Model.Audio.AudioManager;

/**
 * Classe che mantiene e gestisce lo stato completo di una partita di Tressette in corso.
 *
 * <p>Responsabilità principali:</p>
 * <ul>
 *   <li>Gestione dei giocatori e delle loro mani di carte</li>
 *   <li>Tracciamento dei punteggi e delle carte vinte</li>
 *   <li>Gestione del mazzo e distribuzione delle carte</li>
 *   <li>Controllo dello stato della presa corrente (trick)</li>
 *   <li>Storico delle carte già giocate</li>
 *   <li>Determinazione dei vincitori delle prese</li>
 *   <li>Calcolo dei punteggi secondo le regole del Tressette</li>
 * </ul>
 *
 * <p>Implementa il pattern Observer per notificare i cambiamenti di stato
 * a view e controller registrati. Le operazioni critiche sono thread-safe
 * per supportare l'interazione tra thread di gioco e interfaccia grafica.</p>
 *
 * <p><b>Sistema di punteggio:</b> Le carte hanno valori in punti:
 * ASSO, TRE, DUE = 3 punti; RE, CAVALLO, ALFIERE = 1 punto; altre carte = 0 punti.
 * I punti vengono scalati con base 3 (es. 9 punti = 3, 10 punti = 3 1/3).</p>
 */
public class GameState {
    /** Lista dei giocatori partecipanti alla partita */
    private final List<Giocatore> players;

    /** Mappa che associa ogni giocatore alla sua mano di carte */
    private final Map<Giocatore, List<Cards>> hands = new LinkedHashMap<>();

    /** Mappa che associa ogni giocatore al suo punteggio corrente */
    private final Map<Giocatore, Integer> scores = new LinkedHashMap<>();

    /** Mappa che traccia il numero di carte vinte da ogni giocatore */
    private final Map<Giocatore, Integer> wonCardsCount = new LinkedHashMap<>();

    /** Mazzo di carte utilizzato nella partita */
    private final Mazzo deck;

    /** Indice del giocatore corrente nella lista players */
    private int currentPlayerIndex = 0;

    /** Gestore audio per riprodurre suoni durante la partita */
    private final AudioManager audioManager;

    /** Lista ordinata dei giocatori che hanno giocato nella presa corrente */
    private final List<Giocatore> trickPlayers = new ArrayList<>();

    /** Lista ordinata delle carte giocate nella presa corrente */
    private final List<Cards> trickCards = new ArrayList<>();

    /** Storico di tutte le carte acquisite nelle prese completate */
    private final List<Cards> playedCards = new ArrayList<>();

    /** Giocatore che ha vinto l'ultima presa completata */
    private Giocatore lastTrickWinner = null;

    /** Numero di carte vinte nell'ultima presa completata */
    private int lastTrickCardsWon = 0;

    /** Lista thread-safe di observers registrati per ricevere notifiche degli eventi */
    private final List<GameStateObserver> observers = new CopyOnWriteArrayList<>();

    /** Flag thread-safe per garantire che la notifica di fine gioco avvenga una sola volta */
    private final AtomicBoolean gameFinished = new AtomicBoolean(false);


    /**
     * Mappa immutabile dei punti assegnati a ogni valore di carta.
     * ASSO vale 3 punti; RE, CAVALLO, ALFIERE, TRE e DUE valgono 1 punto;
     * le altre carte (SETTE, SEI, CINQUE, QUATTRO) valgono 0 punti.
     */
    private static final Map<Cards.Rank, Integer> CARD_POINTS;
    static {
        Map<Cards.Rank, Integer> m = new EnumMap<>(Cards.Rank.class);
        m.put(Cards.Rank.ASSO, 3);
        m.put(Cards.Rank.TRE, 1);
        m.put(Cards.Rank.DUE, 1);
        m.put(Cards.Rank.RE, 1);
        m.put(Cards.Rank.CAVALLO, 1);
        m.put(Cards.Rank.ALFIERE, 1);
        // le altre rimangono a 0
        CARD_POINTS = Collections.unmodifiableMap(m);

        // rende in scala i punti delle carte ovvero 3 = 1, 1 = 1/3, 2= 2/3 e cosi via anche per i multipli

    }

    /**
     * Costruttore dello stato di gioco.
     * Inizializza tutte le strutture dati necessarie per la partita:
     * mani vuote, punteggi a zero e un mazzo completo di carte.
     *
     * @param players lista dei giocatori che partecipano alla partita (deve contenere almeno un giocatore)
     */
    public GameState(List<Giocatore> players) {
        this.players = new ArrayList<>(players);
        this.deck = new Mazzo();
        for (Giocatore p : players) {
            hands.put(p, new ArrayList<>());
            scores.put(p, 0);
            wonCardsCount.put(p, 0);
        }
        this.audioManager = new AudioManager();
    }

    /**
     * Aggiunge un observer per ricevere notifiche dei cambiamenti di stato.
     * Utilizza una lista thread-safe e previene l'inserimento di duplicati.
     *
     * @param observer l'observer da registrare (se null, viene ignorato)
     */
    public void addObserver(GameStateObserver observer) {
        if (observer != null) {
            // CopyOnWriteArrayList handles concurrent add operations safely
            // Use addIfAbsent to prevent duplicates in a thread-safe way
            ((CopyOnWriteArrayList<GameStateObserver>) observers).addIfAbsent(observer);
        }
    }

    /**
     * Rimuove un observer dalla lista delle notifiche.
     *
     * @param observer l'observer da rimuovere
     */
    public void removeObserver(GameStateObserver observer) {
        observers.remove(observer);
    }

    /**
     * Notifica tutti gli observers che una carta è stata giocata.
     *
     * @param player il giocatore che ha giocato la carta
     * @param card la carta giocata
     */
    private void notifyCardPlayed(Giocatore player, Cards card) {
        observers.forEach(observer -> observer.onCardPlayed(player, card));
    }

    /**
     * Notifica tutti gli observers che una presa è stata completata.
     *
     * @param winner il giocatore vincitore della presa
     * @param cardsWon il numero di carte vinte nella presa
     */
    private void notifyTrickCompleted(Giocatore winner, int cardsWon) {
        observers.forEach(observer -> observer.onTrickCompleted(winner, cardsWon));
    }

    /**
     * Notifica tutti gli observers che le carte sono state distribuite.
     */
    private void notifyCardsDealt() {
        observers.forEach(observer -> observer.onCardsDealt());
    }

    /**
     * Notifica tutti gli observers del cambio di turno.
     *
     * @param currentPlayer il giocatore a cui tocca giocare
     */
    private void notifyTurnChanged(Giocatore currentPlayer) {
        observers.forEach(observer -> observer.onTurnChanged(currentPlayer));
    }

    /**
     * Notifica tutti gli observers che la partita è terminata.
     */
    private void notifyGameFinished() {
        observers.forEach(observer -> observer.onGameFinished());
    }

    /**
     * FUNZIONE PRINCIPALE: esegue il gioco di una carta.
     *
     * <p>Rimuove la carta dalla mano del giocatore e la aggiunge alla presa corrente.
     * Se la presa viene completata (tutti i giocatori hanno giocato), determina il vincitore,
     * assegna i punti al vincitore e prepara la prossima presa.</p>
     *
     * <p>La funzione è thread-safe e notifica gli observers degli eventi rilevanti.</p>
     *
     * @param p il giocatore che sta giocando la carta (deve essere il giocatore corrente)
     * @param handIndex l'indice della carta nella mano del giocatore (0-based)
     * @return la carta giocata, o null se la mossa non è valida
     */
    public Cards playCard(Giocatore p, int handIndex) {
        // solo current player può giocare
        if (players.isEmpty() || players.get(currentPlayerIndex) != p) {
            return null;
        }

        List<Cards> hand = hands.get(p);
        if (hand == null || handIndex < 0 || handIndex >= hand.size()) return null;

        Cards c = hand.remove(handIndex);
        trickCards.add(c);
        trickPlayers.add(p);

        // Notify observers that a card was played
        notifyCardPlayed(p, c);

        // se presa completata
        if (trickCards.size() == players.size()) {
            // determina vincitore della presa
            int winnerPos = determineTrickWinner();
            Giocatore winner = trickPlayers.get(winnerPos);

            // calcola punti della presa usando Streams
            int trickPoints = trickCards.stream()
                    .mapToInt(card -> CARD_POINTS.getOrDefault(card.getRank(), 0))
                    .sum();

            // assegna punti
            int prev = scores.getOrDefault(winner, 0);
            scores.put(winner, prev + trickPoints);

            // Track won cards count
            int cardsWon = trickCards.size();
            int prevWonCards = wonCardsCount.getOrDefault(winner, 0);
            wonCardsCount.put(winner, prevWonCards + cardsWon);

            // Store last trick winner for UI
            lastTrickWinner = winner;
            lastTrickCardsWon = cardsWon;

            // Notify observers that trick was completed
            notifyTrickCompleted(winner, cardsWon);

            // prepara per la prossima presa: il prossimo currentPlayerIndex = index del winner nella lista players
            currentPlayerIndex = players.indexOf(winner);
            notifyTurnChanged(getCurrentPlayer());
        }
        return c;
    }


    /**
     * Distribuisce le carte ai giocatori e inizializza una nuova mano.
     * Il mazzo viene mescolato prima della distribuzione.
     * Il primo giocatore viene impostato come giocatore corrente.
     *
     * @param cardsPerPlayer numero di carte da distribuire a ciascun giocatore
     */
    // Distribuisce le carte ai giocatori e sceglie casualmente il primo giocatore
    public void deal(int cardsPerPlayer) {
        deck.shuffle();
        for (Giocatore p : players) {
            hands.get(p).clear(); // per partite nuove
        }

        for (int i = 0; i < cardsPerPlayer; i++) {
            for (Giocatore p : players) {
                Cards c = deck.draw();
                if (c != null) {
                    hands.get(p).add(c);
                }
            }
        }
        audioManager.playDrawSound();
        // Random rand = new Random();
        // currentPlayerIndex = rand.nextInt(players.size());
        currentPlayerIndex = 0; // sempre il primo giocatore nella lista

        // Notify observers that cards were dealt
        notifyCardsDealt();
        notifyTurnChanged(getCurrentPlayer());
    }

    /**
     * Verifica se la partita è terminata.
     * La partita termina quando tutte le mani dei giocatori sono vuote.
     * Notifica gli observers solo la prima volta che viene rilevata la fine della partita.
     *
     * @return true se tutte le mani sono vuote, false altrimenti
     */
    // Controlla se la partita è finita (tutte le mani vuote) con true o false come risultato
    public boolean isFinished() {
        boolean finished = hands.values().stream().allMatch(List::isEmpty);
        if (finished && gameFinished.compareAndSet(false, true)) {
            // Solo il primo thread che chiama isFinished quando finished=true notificherà
            notifyGameFinished();
        }
        return finished;
    }

    /**
     * Restituisce la lista immutabile di tutti i giocatori partecipanti.
     *
     * @return lista non modificabile dei giocatori
     */
    // Prende la lista dei giocatori
    public List<Giocatore> getPlayers() { return Collections.unmodifiableList(players); }

    /**
     * Restituisce una copia immutabile della mano di un giocatore.
     * La lista restituita non può essere modificata dall'esterno.
     *
     * @param p il giocatore di cui si vuole ottenere la mano
     * @return lista immutabile delle carte nella mano del giocatore, o lista vuota se il giocatore non esiste
     */
    // prende la mano di un giocatore (lista di carte)
    public List<Cards> getHand(Giocatore p) {
        List<Cards> hand = hands.get(p);
        return (hand == null) ? List.of() : Collections.unmodifiableList(hand);
    }

    /**
     * Restituisce il mazzo di carte utilizzato nella partita.
     * Consente di verificare quante carte rimangono o pescare carte aggiuntive.
     *
     * @return il mazzo di carte
     */
    // prende il mazzo di carte restanti
    public Mazzo getDeck() { return deck; }

    /**
     * Restituisce la mano modificabile di un giocatore.
     * <b>ATTENZIONE:</b> Questo metodo consente modifiche dirette alla mano.
     * Deve essere usato solo internamente per operazioni che richiedono modifiche (come pescare carte).
     *
     * @param p il giocatore di cui si vuole ottenere la mano
     * @return lista modificabile delle carte nella mano del giocatore
     */
    // Prende la mano di un giocatore (per modifiche interne)
    public List<Cards> getHandMutable(Giocatore p) {
        return hands.get(p);
    }

    /**
     * Restituisce il punteggio corrente di un giocatore.
     * Il punteggio è calcolato in base ai punti delle carte vinte nelle prese.
     *
     * @param p il giocatore di cui si vuole conoscere il punteggio
     * @return punteggio grezzo del giocatore (non scalato)
     */
    // Prende il punteggio di un giocatore
    public int getScore(Giocatore p) { return scores.getOrDefault(p, 0); }

    /**
     * Restituisce la mappa completa dei punteggi di tutti i giocatori.
     * La mappa restituita è immutabile.
     *
     * @return mappa non modificabile che associa ogni giocatore al suo punteggio
     */
    // Prende la mappa completa dei punteggi
    public Map<Giocatore, Integer> getScores() { return Collections.unmodifiableMap(scores); }

    /**
     * Restituisce il giocatore che deve giocare nel turno corrente.
     *
     * @return il giocatore corrente
     */
    // Prende il giocatore corrente
    public Giocatore getCurrentPlayer() { return players.get(currentPlayerIndex); }

    /**
     * Avanza al turno del prossimo giocatore in sequenza circolare.
     * Dopo l'ultimo giocatore si riparte dal primo.
     * Notifica gli observers del cambio di turno.
     */
    // Avanza al prossimo giocatore nel turno
    public void advanceTurn() {
        if (players == null || players.isEmpty()) return;
        currentPlayerIndex = (currentPlayerIndex + 1) % players.size();
        notifyTurnChanged(getCurrentPlayer());
    }


    /**
     * Restituisce il punteggio scalato di un giocatore in formato stringa.
     * I punti vengono convertiti in base 3: ogni 3 punti grezzi = 1 punto scalato.
     * Il formato è "punti frazione/3" (es. "2 1/3" per 7 punti grezzi).
     *
     * @param p il giocatore di cui calcolare il punteggio scalato
     * @return stringa formattata con il punteggio scalato
     */
    // Punteggio come stringa "1 2/3"
    public String getScaledScoreString(Giocatore p) {
        int rawScore = getScore(p);
        int punti = rawScore / 3;
        int frazione = rawScore % 3;
        if (frazione == 0) return String.valueOf(punti);
        return punti + " " + frazione + "/3";
    }

    /**
     * Determina l'indice del vincitore della presa corrente.
     * Il vincitore è il giocatore che ha giocato la carta più forte dello stesso seme
     * della carta di apertura (lead card). Le carte di semi diversi non possono vincere.
     *
     * @return l'indice (0-based) del vincitore nella lista trickPlayers
     */
    // Determina l'indice del vincitore della presa corrente
    private int determineTrickWinner() {
        Cards lead = trickCards.get(0);
        Cards.Segno leadSuit = lead.getSegno();

        int bestIdx = 0;
        Cards bestCard = trickCards.get(0);
        for (int i = 1; i < trickCards.size(); i++) {
            Cards c = trickCards.get(i);
            if (c.getSegno() == leadSuit && c.getPriority() > bestCard.getPriority()) {
                bestCard = c;
                bestIdx = i;
            }
        }
        return bestIdx;
    }

    /**
     * Restituisce il numero totale di carte vinte da un giocatore durante la partita.
     *
     * @param p il giocatore
     * @return numero di carte vinte
     */
    // Prende il numero di carte vinte da un giocatore
    public int getWonCardsCount(Giocatore p) {
        return wonCardsCount.getOrDefault(p, 0);
    }

    /**
     * Restituisce il giocatore che ha vinto l'ultima presa completata.
     *
     * @return il vincitore dell'ultima presa, o null se nessuna presa è stata ancora completata
     */
    // Prende il giocatore che ha vinto l'ultima presa
    public Giocatore getLastTrickWinner() {
        return lastTrickWinner;
    }

    /**
     * Restituisce il numero di carte vinte nell'ultima presa completata.
     *
     * @return numero di carte nell'ultima presa
     */
    // Prende il numero di carte vinte nell'ultima presa
    public int getLastTrickCardsWon() {
        return lastTrickCardsWon;
    }

    /**
     * Restituisce gli indici delle carte che il giocatore può legalmente giocare.
     * Secondo le regole del Tressette:
     * - Se la presa è vuota, tutte le carte sono giocabili
     * - Altrimenti, si deve rispondere al seme della prima carta se possibile
     * - Se non si hanno carte del seme richiesto, si può giocare qualsiasi carta
     *
     * @param p il giocatore
     * @return array di indici delle carte giocabili nella mano (0-based)
     */
    // Restituisce gli indici delle carte legali che il giocatore può giocare
    public int[] getLegalMoves(Giocatore p) {
        List<Cards> hand = hands.get(p);
        if (hand == null || hand.isEmpty()) return new int[0];

        // se nessuna carta ancora giocata nella presa corrente => tutte legali
        if (trickCards.isEmpty()) {
            return IntStream.range(0, hand.size()).toArray();
        }

        // altrimenti segue il seme leader se possibile
        Cards lead = trickCards.get(0);
        Cards.Segno leadSuit = lead.getSegno();

        // Usa Streams per trovare carte dello stesso seme
        int[] sameSuit = IntStream.range(0, hand.size())
                .filter(i -> hand.get(i).getSegno() == leadSuit)
                .toArray();

        if (sameSuit.length > 0) {
            return sameSuit;
        } else {
            return IntStream.range(0, hand.size()).toArray();
        }
    }

    /**
     * Restituisce le carte giocate nella presa corrente.
     * La lista è immutabile e preserva l'ordine di gioco.
     *
     * @return lista immutabile delle carte nella presa corrente
     */
    // Prende le carte giocate nella presa corrente
    public List<Cards> getTrickCards() {
        return Collections.unmodifiableList(trickCards);
    }

    /**
     * Restituisce i giocatori nell'ordine in cui hanno giocato nella presa corrente.
     * La lista è immutabile e parallela a getTrickCards().
     *
     * @return lista immutabile dei giocatori che hanno giocato nella presa
     */
    // Prende i giocatori nell'ordine di gioco della presa corrente
    public List<Giocatore> getTrickPlayers() {
        return Collections.unmodifiableList(trickPlayers);
    }

    /**
     * Restituisce la carta che attualmente vince la presa.
     * Si tratta della carta con priorità più alta dello stesso seme della prima carta giocata.
     *
     * @return la carta vincente della presa corrente, o null se la presa è vuota
     */
    // Restituisce la carta che permette la vittoria della mano con minimo valore necessario
    public Cards getLeadCard() {
        if (trickCards.isEmpty()) return null;
        int winnerIdx = determineTrickWinner();
        return trickCards.get(winnerIdx);
    }



    /**
     * Restituisce la lista immutabile di tutte le carte già giocate e acquisite.
     * Include solo le carte delle prese completate, non quelle della presa corrente.
     *
     * @return lista immutabile delle carte giocate
     */
    // Prende la lista delle carte già giocate (prese completate)
    public List<Cards> getPlayedCards() {
        return Collections.unmodifiableList(playedCards);
    }

    /**
     * Restituisce i punti associati a una carta secondo le regole del Tressette.
     * - ASSO, TRE, DUE: 3 punti
     * - RE, CAVALLO, ALFIERE: 1 punto
     * - Altre carte: 0 punti
     *
     * @param c la carta di cui calcolare i punti
     * @return punti della carta (0, 1 o 3)
     */
    // Prende i punti associati a una carta
    public static int getCardPoints(Cards c) {
        if (c == null) return 0;
        return CARD_POINTS.getOrDefault(c.getRank(), 0);
    }

    /**
     * Svuota i buffer della presa corrente.
     * Rimuove tutte le carte e i giocatori dalla presa in corso,
     * preparando lo stato per la prossima presa.
     */
    // Pulisce le liste
    public void clearTrick() {
        trickPlayers.clear();
        trickCards.clear();
    }
}