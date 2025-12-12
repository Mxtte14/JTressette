package Model.Game;

import java.util.*;
import java.util.stream.Collectors;
import Model.Audio.AudioManager;

/*
    Classe che mantiene lo stato della partita in corso:
    - giocatori
    - mani dei giocatori
    - punteggi
    - mazzo
    - stato della presa corrente (trick)
    - storico delle carte giocate
    Fornisce metodi per eseguire mosse, distribuire carte, calcolare punteggi, determinare vincitori delle prese, ecc.
    Implementa il pattern Observer per notificare i cambiamenti di stato.
 */

public class GameState {
    private final List<Giocatore> players;
    private final Map<Giocatore, List<Cards>> hands = new LinkedHashMap<>();
    private final Map<Giocatore, Integer> scores = new LinkedHashMap<>();
    private final Map<Giocatore, Integer> wonCardsCount = new LinkedHashMap<>();
    private final Mazzo deck;
    private int currentPlayerIndex = 0;
    private final AudioManager audioManager;
    // trick buffer: mantiene l'ordine dei giocatori in questa presa
    private final List<Giocatore> trickPlayers = new ArrayList<>();
    private final List<Cards> trickCards = new ArrayList<>();

    // storico delle carte già acquisite (carte delle prese completate)
    private final List<Cards> playedCards = new ArrayList<>();

    // Dati sull'ultima presa completata
    private Giocatore lastTrickWinner = null;
    private int lastTrickCardsWon = 0;
    
    // Observer pattern: lista di observers
    private final List<GameStateObserver> observers = new ArrayList<>();


    // punti per carta (mappa semplificata): ASSO, TRE, DUE = 3; RE, CAVALLO, ALFIERE = 1
    private static final Map<Cards.Rank, Integer> CARD_POINTS;
    static {
        Map<Cards.Rank, Integer> m = new EnumMap<>(Cards.Rank.class);
        m.put(Cards.Rank.ASSO, 3);
        m.put(Cards.Rank.TRE, 3);
        m.put(Cards.Rank.DUE, 3);
        m.put(Cards.Rank.RE, 1);
        m.put(Cards.Rank.CAVALLO, 1);
        m.put(Cards.Rank.ALFIERE, 1);
        // le altre rimangono a 0
        CARD_POINTS = Collections.unmodifiableMap(m);

        // rende in scala i punti delle carte ovvero 3 = 1, 1 = 1/3, 2= 2/3 e cosi via anche per i multipli

    }

    // Costruttore: inizializza mani vuote e punteggi a 0
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
     * Observer pattern methods
     */
    public void addObserver(GameStateObserver observer) {
        if (observer != null && !observers.contains(observer)) {
            observers.add(observer);
        }
    }
    
    public void removeObserver(GameStateObserver observer) {
        observers.remove(observer);
    }
    
    private void notifyCardPlayed(Giocatore player, Cards card) {
        observers.forEach(observer -> observer.onCardPlayed(player, card));
    }
    
    private void notifyTrickCompleted(Giocatore winner, int cardsWon) {
        observers.forEach(observer -> observer.onTrickCompleted(winner, cardsWon));
    }
    
    private void notifyCardsDealt() {
        observers.forEach(observer -> observer.onCardsDealt());
    }
    
    private void notifyTurnChanged(Giocatore currentPlayer) {
        observers.forEach(observer -> observer.onTurnChanged(currentPlayer));
    }
    
    private void notifyGameFinished() {
        observers.forEach(observer -> observer.onGameFinished());
    }

    /**
     * FUNZIONE PRINCIPALE: GIOCA UNA CARTA
     * Esegue la mossa: rimuove la carta dalla mano del player e la aggiunge alla presa corrente.
     * Se la presa viene completata (tutti i players hanno giocato), determina il vincitore,
     * assegna i punti della presa al vincitore e svuota il buffer della presa.
     * Ritorna la card giocata (o null se mossa invalida).
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
     Funzioni utilizzate per gestire lo stato generale della partita
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

    // Controlla se la partita è finita (tutte le mani vuote) con true o false come risultato
    public boolean isFinished() { 
        boolean finished = hands.values().stream().allMatch(List::isEmpty);
        if (finished) {
            notifyGameFinished();
        }
        return finished;
    }

    // Prende la lista dei giocatori
    public List<Giocatore> getPlayers() { return Collections.unmodifiableList(players); }

    // prende la mano di un giocatore (lista di carte)
    public List<Cards> getHand(Giocatore p) {
        List<Cards> hand = hands.get(p);
        return (hand == null) ? List.of() : Collections.unmodifiableList(hand);
    }

    // prende il mazzo di carte restanti
    public Mazzo getDeck() { return deck; }

    // Prende la mano di un giocatore (per modifiche interne)
    public List<Cards> getHandMutable(Giocatore p) {
        return hands.get(p);
    }

    // Prende il punteggio di un giocatore
    public int getScore(Giocatore p) { return scores.getOrDefault(p, 0); }

    // Prende la mappa completa dei punteggi
    public Map<Giocatore, Integer> getScores() { return Collections.unmodifiableMap(scores); }

    // Prende il giocatore corrente
    public Giocatore getCurrentPlayer() { return players.get(currentPlayerIndex); }

    // Avanza al prossimo giocatore nel turno
    public void advanceTurn() {
        if (players == null || players.isEmpty()) return;
        currentPlayerIndex = (currentPlayerIndex + 1) % players.size();
        notifyTurnChanged(getCurrentPlayer());
    }


    /**
     * Funzioni utilizzate per gestire i punteggi scalati in 1 e 1/3 e cosi via
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
        Funzioni utilizzate per raccogliere dati sul turno e il vincitore della mano.
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

    // Prende il numero di carte vinte da un giocatore
    public int getWonCardsCount(Giocatore p) {
        return wonCardsCount.getOrDefault(p, 0);
    }

    // Prende il giocatore che ha vinto l'ultima presa
    public Giocatore getLastTrickWinner() {
        return lastTrickWinner;
    }

    // Prende il numero di carte vinte nell'ultima presa
    public int getLastTrickCardsWon() {
        return lastTrickCardsWon;
    }

    /**
        Funzioni utilizzate per gestire i turni e le azioni possibili dei giocatori.
     */
    // Restituisce gli indici delle carte legali che il giocatore può giocare
    public int[] getLegalMoves(Giocatore p) {
        List<Cards> hand = hands.get(p);
        if (hand == null || hand.isEmpty()) return new int[0];

        // se nessuna carta ancora giocata nella presa corrente => tutte legali
        if (trickCards.isEmpty()) {
            return java.util.stream.IntStream.range(0, hand.size()).toArray();
        }

        // altrimenti segue il seme leader se possibile
        Cards lead = trickCards.get(0);
        Cards.Segno leadSuit = lead.getSegno();
        
        // Usa Streams per trovare carte dello stesso seme
        int[] sameSuit = java.util.stream.IntStream.range(0, hand.size())
            .filter(i -> hand.get(i).getSegno() == leadSuit)
            .toArray();
            
        if (sameSuit.length > 0) {
            return sameSuit;
        } else {
            return java.util.stream.IntStream.range(0, hand.size()).toArray();
        }
    }

    // Prende le carte giocate nella presa corrente
    public List<Cards> getTrickCards() {
        return Collections.unmodifiableList(trickCards);
    }

    // Prende i giocatori nell'ordine di gioco della presa corrente
    public List<Giocatore> getTrickPlayers() {
        return Collections.unmodifiableList(trickPlayers);
    }

    // Restituisce la carta che permette la vittoria della mano con minimo valore necessario
    public Cards getLeadCard() {
        if (trickCards.isEmpty()) return null;
        int winnerIdx = determineTrickWinner();
        return trickCards.get(winnerIdx);
    }



    /**
        Funzioni utilizzate per ottenere informazioni riguardo le carte
     */

    // Prende la lista delle carte già giocate (prese completate)
    public List<Cards> getPlayedCards() {
        return Collections.unmodifiableList(playedCards);
    }

    // Prende i punti associati a una carta
    public static int getCardPoints(Cards c) {
        if (c == null) return 0;
        return CARD_POINTS.getOrDefault(c.getRank(), 0);
    }

    // Pulisce le liste
    public void clearTrick() {
        trickPlayers.clear();
        trickCards.clear();
    }
}