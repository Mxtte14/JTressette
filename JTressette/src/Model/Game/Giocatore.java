package Model.Game;

/**
 * Interfaccia per i giocatori della partita.
 */
public interface Giocatore {
    String getName();
    boolean isBot();
    /**
     * Sceglie la carta da giocare. L'implementazione può usare GameState.getHand(this)
     * e GameState.getLegalMoves(this) per decidere.
     * Restituisce l'indice della carta nella mano (0-based), oppure -1 se nessuna mossa possibile.
     */
    int chooseCard(GameState state) throws InterruptedException;

}
