package game;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Human player helper: GameEngine chiama chooseCard() che aspetta la scelta
 * fornita dalla UI tramite submitCardChoice(index). La attesa usa una BlockingQueue
 * con timeout per evitare deadlock infiniti.
 *
 * UI: quando l'utente clicca una carta, chiama human.submitCardChoice(index).
 */
public class GiocatoreUmano implements Giocatore {
    private final String name;
    private final BlockingQueue<Integer> queue = new ArrayBlockingQueue<>(1);

    public GiocatoreUmano(String name) { this.name = name; }

    @Override public String getName() { return name; }
    @Override public boolean isBot() { return false; }

    /**
     * Il GameEngine chiamerà questo metodo; rimane in attesa fino a che
     * la UI non invia la scelta con submitCardChoice.
     */
    @Override
    public int chooseCard(GameState state) throws InterruptedException {
        // Attendi per un tempo ragionevole (es. 5 minuti) per evitare blocco infinito
        Integer idx = queue.poll(5, TimeUnit.MINUTES);
        return (idx == null) ? -1 : idx;
    }

    /**
     * Metodo invocato dalla UI per fornire la scelta dell'utente.
     * Deve essere chiamato da EDT (o da listener UI).
     */
    public void submitCardChoice(int handIndex) {
        // se queue piena, scarica prima (evitiamo che un vecchio valore rimanga)
        queue.clear();
        queue.offer(handIndex);
    }
}
