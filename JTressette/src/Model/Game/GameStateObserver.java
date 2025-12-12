package Model.Game;

/**
 * Observer interface for GameState changes.
 * Views and controllers can implement this to be notified of game state changes.
 */
public interface GameStateObserver {
    /**
     * Called when a card is played.
     */
    void onCardPlayed(Giocatore player, Cards card);

    /**
     * Called when a trick is completed.
     */
    void onTrickCompleted(Giocatore winner, int cardsWon);

    /**
     * Called when cards are dealt.
     */
    void onCardsDealt();

    /**
     * Called when a player's turn changes.
     */
    void onTurnChanged(Giocatore currentPlayer);

    /**
     * Called when the game finishes.
     */
    void onGameFinished();
}