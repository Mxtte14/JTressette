package game;

import java.util.*;

/**
 * Stato minimale del gioco:
 * - lista players (ordine di gioco)
 * - mani memorizzate internamente (Map<Player, List<Card>>)
 * - deck, punteggi basati su strength delle carte giocate
 * - indice del current player
 *
 * Questa implementazione è volutamente semplice: non ha il concetto
 * di "presa" completa; quando una carta viene giocata ne somma la strength
 * al punteggio del player (è un modo semplice per avere un risultato finale).
 * Estendi successivamente per regole complete di prese.
 */
public class GameState {
    private final List<Player> players;
    private final Map<Player, List<Cards>> hands = new LinkedHashMap<>();
    private final Map<Player, Integer> scores = new LinkedHashMap<>();
    private final Mazzo deck;
    private int currentPlayerIndex = 0;

    public GameState(List<Player> players) {
        this.players = new ArrayList<>(players);
        this.deck = new Mazzo();
        for (Player p : players) {
            hands.put(p, new ArrayList<>());
            scores.put(p, 0);
        }
    }

    /** Distribuisce tutte le carte del mazzo round-robin ai players (fino a esaurimento) */
    public void deal() {
        deck.shuffle();
        while (!deck.isEmpty()) {
            for (Player p : players) {
                Cards c = deck.draw();
                if (c == null) break;
                hands.get(p).add(c);
                // notifichiamo il Player per implementazioni che tengono mano localmente (es. SimpleBotPlayer)
                p.receiveCard(c);
            }
        }
    }

    public boolean isFinished() {
        // partita finisce quando tutte le mani sono vuote
        for (List<Cards> h : hands.values()) {
            if (!h.isEmpty()) return false;
        }
        return true;
    }

    public Player getCurrentPlayer() {
        return players.get(currentPlayerIndex);
    }

    public void advanceTurn() {
        currentPlayerIndex = (currentPlayerIndex + 1) % players.size();
    }

    /** Restituisce array di indici legali (0..handSize-1) per il player */
    public int[] getLegalMoves(Player p) {
        List<Cards> hand = hands.get(p);
        if (hand == null || hand.isEmpty()) return new int[0];
        int[] res = new int[hand.size()];
        for (int i = 0; i < hand.size(); i++) res[i] = i;
        return res;
    }

    /**
     * Esegue la mossa: rimuove la carta dalla mano del player, somma la strength
     * al punteggio (semplificazione). Restituisce la carta giocata o null.
     */
    public Cards playCard(Player p, int cardIndex) {
        List<Cards> hand = hands.get(p);
        if (hand == null || cardIndex < 0 || cardIndex >= hand.size()) return null;

        Cards c = hand.remove(cardIndex);

        // se il Player ha implementazioni locali della mano, è loro responsabilità sincronizzare;
        // SimpleBotPlayer rimuove la carta dalla propria mano solo se lo implementi li; per semplicità
        // manteniamo una mappa centralizzata come sorgente di verità.

        int prev = scores.getOrDefault(p, 0);
        scores.put(p, prev + c.getPriority());

        return c;
    }

    public int getScore(Player p) {
        return scores.getOrDefault(p, 0);
    }

    public Map<Player,Integer> getScores() {
        return Collections.unmodifiableMap(scores);
    }

    public List<Player> getPlayers() { return Collections.unmodifiableList(players); }
    public List<Cards> getHand(Player p) { return Collections.unmodifiableList(hands.getOrDefault(p, List.of())); }
}
