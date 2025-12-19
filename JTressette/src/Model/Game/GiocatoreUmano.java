package Model.Game;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Implementazione di un giocatore umano controllato dall'interfaccia utente.
 * Utilizza una BlockingQueue per coordinare la comunicazione tra il thread di gioco
 * e il thread dell'interfaccia grafica (EDT - Event Dispatch Thread).
 *
 * <p>Il meccanismo funziona come segue:</p>
 * <ul>
 *   <li>Il GameController chiama chooseCard() che si mette in attesa</li>
 *   <li>L'interfaccia utente riceve il click del giocatore e chiama submitCardChoice()</li>
 *   <li>La scelta viene inserita nella coda e chooseCard() si sblocca restituendo l'indice</li>
 * </ul>
 *
 * <p>Viene utilizzato un timeout di 5 minuti per evitare deadlock in caso di problemi.</p>
 */
public class GiocatoreUmano implements Giocatore {
    /** Nome del giocatore umano */
    private final String name;

    /** Coda bloccante per sincronizzare la scelta della carta tra thread */
    private final BlockingQueue<Integer> queue = new ArrayBlockingQueue<>(1);

    /**
     * Costruttore del giocatore umano.
     *
     * @param name il nome del giocatore
     */
    public GiocatoreUmano(String name) { this.name = name; }

    /**
     * Restituisce il nome del giocatore.
     *
     * @return il nome del giocatore
     */
    @Override public String getName() { return name; }

    /**
     * Verifica se questo giocatore è un bot.
     *
     * @return sempre false per i giocatori umani
     */
    @Override public boolean isBot() { return false; }

    /**
     * Attende che il giocatore scelga una carta tramite l'interfaccia utente.
     * Questo metodo viene chiamato dal GameController e rimane in attesa
     * fino a quando l'UI non fornisce la scelta tramite submitCardChoice().
     * Utilizza un timeout di 5 minuti per evitare blocchi infiniti.
     *
     * @param state lo stato corrente della partita
     * @return l'indice della carta scelta nella mano (0-based), o -1 se timeout scaduto
     * @throws InterruptedException se il thread viene interrotto durante l'attesa
     */
    @Override
    public int chooseCard(GameState state) throws InterruptedException {
        // Attendi per un tempo ragionevole (es. 5 minuti) per evitare blocco infinito
        Integer idx = queue.poll(5, TimeUnit.MINUTES);
        return (idx == null) ? -1 : idx;
    }

    /**
     * Metodo invocato dall'interfaccia utente per fornire la scelta del giocatore.
     * Deve essere chiamato dal thread dell'interfaccia grafica (EDT) quando
     * l'utente clicca su una carta.
     * Se la coda contiene già un valore, viene svuotata prima di inserire il nuovo indice.
     *
     * @param handIndex l'indice della carta scelta nella mano del giocatore (0-based)
     */
    public void submitCardChoice(int handIndex) {
        // se queue piena, scarica prima (evitiamo che un vecchio valore rimanga)
        queue.clear();
        queue.offer(handIndex);
    }
}