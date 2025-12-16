package Model.Game;

/**
 * Interfaccia che rappresenta un giocatore generico nel gioco di Tressette.
 * Implementata sia dai giocatori umani che dai bot controllati dal computer.
 * Definisce i comportamenti base comuni a tutti i tipi di giocatori.
 */
public interface Giocatore {
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
     * Sceglie la carta da giocare nel turno corrente.
     * L'implementazione può usare GameState.getHand(this) per ottenere le carte in mano
     * e GameState.getLegalMoves(this) per determinare le mosse valide secondo le regole.
     *
     * @param state lo stato corrente della partita contenente tutte le informazioni necessarie
     * @return l'indice della carta nella mano da giocare (0-based), oppure -1 se nessuna mossa possibile
     * @throws InterruptedException se il thread viene interrotto durante la scelta (per giocatori umani)
     */
    int chooseCard(GameState state) throws InterruptedException;

}