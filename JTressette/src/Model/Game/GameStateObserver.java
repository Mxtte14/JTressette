package Model.Game;

/**
 * Interfaccia Observer per monitorare i cambiamenti di stato del gioco.
 * Implementa il pattern Observer per notificare le viste e i controller
 * degli eventi significativi che avvengono durante una partita.
 * 
 * <p>Le classi che implementano questa interfaccia possono registrarsi
 * presso un GameState per ricevere notifiche in tempo reale degli eventi di gioco.</p>
 */
public interface GameStateObserver {
    /**
     * Chiamato quando un giocatore gioca una carta.
     * Permette di aggiornare l'interfaccia utente mostrando la carta giocata.
     * 
     * @param player il giocatore che ha giocato la carta
     * @param card la carta che è stata giocata
     */
    void onCardPlayed(Giocatore player, Cards card);

    /**
     * Chiamato quando una presa (trick) viene completata.
     * Avviene quando tutti i giocatori hanno giocato una carta nel turno corrente.
     * 
     * @param winner il giocatore che ha vinto la presa
     * @param cardsWon il numero di carte vinte nella presa
     */
    void onTrickCompleted(Giocatore winner, int cardsWon);

    /**
     * Chiamato quando le carte vengono distribuite ai giocatori.
     * Segnala l'inizio di una nuova mano o partita.
     */
    void onCardsDealt();

    /**
     * Chiamato quando cambia il turno di gioco.
     * Indica quale giocatore deve giocare la prossima carta.
     * 
     * @param currentPlayer il giocatore a cui tocca giocare
     */
    void onTurnChanged(Giocatore currentPlayer);

    /**
     * Chiamato quando la partita termina.
     * Segnala che tutte le carte sono state giocate e la partita è conclusa.
     */
    void onGameFinished();
}