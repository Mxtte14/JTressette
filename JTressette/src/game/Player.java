package game;

/**
 * Interfaccia Player usata dal GameEngine.
 * chooseCard riceve lo stato corrente e deve tornare l'indice della carta
 * nella mano del player da giocare (0-based). Implementazioni possono essere bot
 * o "human" (UI).
 */
public interface Player {
    String getName();
    boolean isBot();

    /**
     * Sceglie quale carta giocare, dato lo stato di gioco (GameState).
     * Deve restituire l'indice della carta nella mano del player; -1 = pass (se supportato).
     */
    int chooseCard(GameState state) ;

    /** Riceve una carta in mano */
    void receiveCard(Cards c);
}
