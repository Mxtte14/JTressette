package Model.Game;

/**
 * Interfaccia alternativa per rappresentare un giocatore generico.
 * 
 * <p><b>Nota:</b> Questa interfaccia è deprecata e mantenuta per retrocompatibilità.
 * Si consiglia di utilizzare l'interfaccia {@link Giocatore} che offre le stesse
 * funzionalità con gestione delle eccezioni migliorata.</p>
 * 
 * <p>Definisce il contratto base per tutti i tipi di giocatori nel gioco,
 * sia umani che controllati dal computer (bot).</p>
 */
public interface Player {
    /**
     * Restituisce il nome del giocatore.
     * 
     * @return il nome identificativo del giocatore
     */
    String getName();
    
    /**
     * Verifica se il giocatore è un bot controllato dal computer.
     * 
     * @return true se è un bot, false se è un giocatore umano
     */
    boolean isBot();

    /**
     * Sceglie quale carta giocare dato lo stato corrente del gioco.
     * L'implementazione dovrebbe analizzare il GameState per determinare
     * le mosse legali e selezionare la carta da giocare.
     * 
     * @param state lo stato corrente della partita contenente tutte le informazioni necessarie
     * @return l'indice della carta nella mano del giocatore (0-based); 
     *         -1 per pass (se supportato dalla regola del gioco)
     */
    int chooseCard(GameState state) ;

}
