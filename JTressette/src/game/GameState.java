package game;

import java.util.*;

/**
 * Stato completo con:
 * - mani (Map<Player, List<Card>>)
 * - punteggi (Map<Player,Integer>) accumulati dalle prese
 * - buffer della presa corrente (lista di coppie Player->Card nell'ordine di gioco)
 * - regole legali: follow-suit obbligatorio se presente
 */
public class GameState {
    private final List<Player> players;
    private final Map<Player, List<Cards>> hands = new LinkedHashMap<>();
    private final Map<Player, Integer> scores = new LinkedHashMap<>();
    private final Mazzo deck;
    private int currentPlayerIndex = 0;

    // trick buffer: mantiene l'ordine dei giocatori in questa presa
    private final List<Player> trickPlayers = new ArrayList<>();
    private final List<Cards> trickCards = new ArrayList<>();

    // punti per carta (mappa semplificata): A,3,2,K,Q,J = 1 punto ciascuna (configurabile)
    private static final Map<Cards.Rank, Integer> CARD_POINTS;
    static {
        Map<Cards.Rank, Integer> m = new EnumMap<>(Cards.Rank.class);
        m.put(Cards.Rank.ASSO, 1);
        m.put(Cards.Rank.TRE, 1);
        m.put(Cards.Rank.DUE, 1);
        m.put(Cards.Rank.RE, 1);
        m.put(Cards.Rank.CAVALLO, 1);
        m.put(Cards.Rank.ALFIERE, 1);
        // le altre rimangono a 0
        CARD_POINTS = Collections.unmodifiableMap(m);
    }

    public GameState(List<Player> players) {
        this.players = new ArrayList<>(players);
        this.deck = new Mazzo();
        for (Player p : players) {
            hands.put(p, new ArrayList<>());
            scores.put(p, 0);
        }
    }

    public void deal() {
        deck.shuffle();
        while (!deck.isEmpty()) {
            for (Player p : players) {
                Cards c = deck.draw();
                if (c == null) break;
                hands.get(p).add(c);
            }
        }
        // definisci il primo giocatore (puoi scegliere random o fissare il primo della lista)
        currentPlayerIndex = 0;
    }

    public boolean isFinished() {
        for (List<Cards> h : hands.values()) {
            if (!h.isEmpty()) return false;
        }
        return true;
    }

    public List<Player> getPlayers() { return Collections.unmodifiableList(players); }

    public List<Cards> getHand(Player p) {
        List<Cards> hand = hands.get(p);
        return (hand == null) ? List.of() : Collections.unmodifiableList(hand);
    }

    public int getScore(Player p) { return scores.getOrDefault(p, 0); }
    public Map<Player, Integer> getScores() { return Collections.unmodifiableMap(scores); }

    public Player getCurrentPlayer() { return players.get(currentPlayerIndex); }

    public void advanceTurn() {
        currentPlayerIndex = (currentPlayerIndex + 1) % players.size();
    }

    /**
     * Restituisce gli indici legali nella mano del player rispettando l'obbligo
     * di seguire il seme di mano se possibile.
     */
    public int[] getLegalMoves(Player p) {
        List<Cards> hand = hands.get(p);
        if (hand == null || hand.isEmpty()) return new int[0];

        // se nessuna carta ancora giocata nella presa corrente => tutte legali
        if (trickCards.isEmpty()) {
            int[] all = new int[hand.size()];
            for (int i = 0; i < hand.size(); i++) all[i] = i;
            return all;
        }

        // altrimenti segue il seme leader se possibile
        Cards lead = trickCards.get(0);
        Cards.Segno leadSuit = lead.getSegno();
        List<Integer> sameSuit = new ArrayList<>();
        for (int i = 0; i < hand.size(); i++) {
            if (hand.get(i).getSegno() == leadSuit) sameSuit.add(i);
        }
        if (!sameSuit.isEmpty()) {
            return sameSuit.stream().mapToInt(Integer::intValue).toArray();
        } else {
            int[] all = new int[hand.size()];
            for (int i = 0; i < hand.size(); i++) all[i] = i;
            return all;
        }
    }

    /**
     * Esegue la mossa: rimuove la carta dalla mano del player e la aggiunge alla presa corrente.
     * Se la presa viene completata (tutti i players hanno giocato), determina il vincitore,
     * assegna i punti della presa al vincitore e svuota il buffer della presa.
     *
     * Ritorna la card giocata (o null se mossa invalida).
     */
    public Cards playCard(Player p, int handIndex) {
        List<Cards> hand = hands.get(p);
        if (hand == null || handIndex < 0 || handIndex >= hand.size()) return null;

        Cards c = hand.remove(handIndex);

        trickPlayers.add(p);
        trickCards.add(c);

        // se presa completata
        if (trickCards.size() == players.size()) {
            // determina vincitore della presa
            int winnerPos = determineTrickWinner();
            Player winner = trickPlayers.get(winnerPos);

            // calcola punti della presa
            int trickPoints = 0;
            for (Cards card : trickCards) {
                trickPoints += CARD_POINTS.getOrDefault(card.getRank(), 0);
            }

            // assegna punti
            int prev = scores.getOrDefault(winner, 0);
            scores.put(winner, prev + trickPoints);

            // prepara per la prossima presa: il prossimo currentPlayerIndex = index del winner nella lista players
            int winnerPlayerIndex = players.indexOf(winner);
            currentPlayerIndex = winnerPlayerIndex;

            // svuota buffer
            trickPlayers.clear();
            trickCards.clear();
        } else {
            // la turno continua: il next player sarà avanzato esternamente chiamando advanceTurn()
        }

        return c;
    }

    /**
     * Determina il vincitore della presa (indice nella trickPlayers/trickCards).
     * Regola: il vincitore è la carta del seme di mano (lead suit) con valore più alto (getStrength()).
     */
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
}