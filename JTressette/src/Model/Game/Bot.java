package Model.Game;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

/**
 * Implementazione di un giocatore artificiale (bot) controllato dal computer.
 * Il comportamento strategico del bot varia in base al livello di difficoltà impostato:
 * <ul>
 *   <li><b>EASY</b>: scelta casuale tra le mosse legali disponibili</li>
 *   <li><b>MEDIUM</b>: gioca la carta con maggiore forza tra le mosse legali,
 *       cerca di vincere la presa con la carta minima necessaria</li>
 *   <li><b>HARD</b>: strategia avanzata che considera le carte già giocate,
 *       i punti nella presa corrente e le carte rimanenti in gioco</li>
 * </ul>
 */
public class Bot implements Giocatore {
    /** Nome identificativo del bot */
    private final String name;

    /** Livello di difficoltà che determina la strategia di gioco del bot */
    private final Difficoltà difficulty;

    /** Generatore di numeri casuali per le scelte del bot */
    private final Random rnd = new Random();

    /**
     * Costruttore del bot.
     *
     * @param name il nome identificativo del bot
     * @param difficulty il livello di difficoltà che determina l'intelligenza artificiale del bot
     */
    public Bot(String name, Difficoltà difficulty) {
        this.name = name;
        this.difficulty = difficulty;
    }

    /**
     * Restituisce il nome del bot.
     *
     * @return il nome identificativo del bot
     */
    @Override public String getName() { return name; }

    /**
     * Verifica se questo giocatore è un bot.
     *
     * @return sempre true per questa implementazione
     */
    @Override public boolean isBot() { return true; }

    /**
     * Sceglie la carta da giocare in base alla strategia determinata dal livello di difficoltà.
     * <ul>
     *   <li><b>EASY</b>: selezione casuale</li>
     *   <li><b>MEDIUM</b>: cerca di vincere con la carta minima o scarta la carta più debole</li>
     *   <li><b>HARD</b>: applica una strategia euristica avanzata considerando punti e carte rimanenti</li>
     * </ul>
     *
     * @param state lo stato corrente della partita
     * @return l'indice della carta scelta nella mano del bot, o -1 se non ci sono mosse legali
     */
    @Override
    public int chooseCard(GameState state) {
        int[] legal = state.getLegalMoves(this);
        if (legal == null || legal.length == 0) return -1;
        switch (difficulty) {
            case EASY:
                return legal[rnd.nextInt(legal.length)];

            case MEDIUM:
                // se esiste una carta legale che vince il lead (se presente) sceglie la minima che vince,
                // altrimenti scarta la minima (più debole)
                Cards lead = state.getLeadCard();
                if (lead != null) {
                    // Cache hand reference per evitare chiamate multiple
                    List<Cards> hand = state.getHand(this);
                    // Usa Streams per trovare la minima carta che vince
                    int bestWinIdx = Arrays.stream(legal)
                            .boxed()
                            .filter(idx -> {
                                Cards c = hand.get(idx);
                                return c.getSegno() == lead.getSegno() && c.getPriority() > lead.getPriority();
                            })
                            .min(Comparator.comparingInt(idx -> hand.get(idx).getPriority()))
                            .orElse(-1);

                    if (bestWinIdx >= 0) return bestWinIdx;
                }
                // altrimenti gioca la carta legale con min strength
                List<Cards> hand = state.getHand(this);
                return Arrays.stream(legal)
                        .boxed()
                        .min(Comparator.comparingInt(i -> hand.get(i).getPriority()))
                        .orElse(legal[0]);

            case HARD:
                return chooseCardHard(state, legal);
        }
        return 0;
    }

    /**
     * Implementazione della strategia di gioco avanzata per il livello HARD.
     * Utilizza un'euristica sofisticata che considera:
     * <ul>
     *   <li>Le carte già giocate e quelle nella presa corrente</li>
     *   <li>I punti accumulabili nella presa</li>
     *   <li>Le carte punto rimanenti nel seme di mano</li>
     *   <li>Se la mossa può vincere la presa o meno</li>
     * </ul>
     * La strategia tenta di minimizzare lo spreco di carte forti e massimizzare i punti vinti.
     *
     * @param state lo stato corrente della partita
     * @param legal array degli indici delle mosse legali disponibili
     * @return l'indice della carta migliore da giocare secondo l'euristica
     */
    private int chooseCardHard(GameState state, int[] legal) {
        // Informazioni pubbliche
        var trickCards = state.getTrickCards();
        var played = state.getPlayedCards();
        var myHand = state.getHand(this);
        Cards.Segno leadSuit = trickCards.isEmpty() ? null : trickCards.get(0).getSegno();

        // Crea Sets per lookup O(1) invece di stream O(n)
        Set<Cards> playedSet = new HashSet<>(played);
        Set<Cards> trickSet = new HashSet<>(trickCards);
        Set<Cards> myHandSet = new HashSet<>(myHand);

        // miglior carta attuale nella presa (se esiste) usando Streams
        Cards currentBest = null;
        if (!trickCards.isEmpty()) {
            Cards.Segno firstSuit = trickCards.get(0).getSegno();
            currentBest = trickCards.stream()
                    .filter(c -> c.getSegno() == firstSuit)
                    .max(Comparator.comparingInt(Cards::getPriority))
                    .orElse(trickCards.get(0));
        }

        int bestIdx = legal[0];
        double bestScore = Double.NEGATIVE_INFINITY;

        // per ogni mossa legale calcolo un punteggio euristico usando Streams
        for (int idx : legal) {
            Cards candidate = myHand.get(idx);

            boolean wouldWin = false;
            if (trickCards.isEmpty()) {
                // se si è leader non sappiamo se vincerà (dipende dagli altri), valutiamo come "lead" case
                wouldWin = false;
            } else {
                // può vincere se segue il seme e supera currentBest (se currentBest è del seme di mano)
                if (candidate.getSegno() == currentBest.getSegno() && candidate.getPriority() > currentBest.getPriority()) {
                    wouldWin = true;
                }
            }

            // punti nella presa considerando la carta candidata usando Streams
            int trickPoints = trickCards.stream()
                    .mapToInt(GameState::getCardPoints)
                    .sum() + GameState.getCardPoints(candidate);

            // quante carte punto rimangono nel seme del lead (approssimazione) usando Streams con Set lookup
            Cards.Segno suitToCheck = (leadSuit != null) ? leadSuit : candidate.getSegno();
            int remainingPointCardsInLead = (int) Arrays.stream(Cards.Rank.values())
                    .filter(r -> {
                        Cards hypothetical = new Cards(suitToCheck, r);
                        // la carta è rimasta se non è nelle played e non è in mano del bot e non è nella trick corrente
                        // Usa Set.contains per O(1) invece di stream per O(n)
                        if (playedSet.contains(hypothetical)) return false;
                        if (trickSet.contains(hypothetical)) return false;
                        if (myHandSet.contains(hypothetical)) return false;

                        // se questa rank ha punti, la consideriamo
                        return GameState.getCardPoints(hypothetical) > 0;
                    })
                    .count();

            // euristica:
            // - se la mossa vince e ci sono già punti nella presa => alta priorità (minimizzare waste scegliendo la minima che vince)
            // - se la mossa vince e la presa ha 0 punti ma nel seme ci sono punti rimasti => possibile "setup", piccolo vantaggio
            // - se la mossa non vince => preferisco scartare carte di bassa priorità
            double score;
            if (wouldWin) {
                score = 2000 - candidate.getPriority(); // preferisco vincere con carta più bassa possibile
                // se ci sono punti nella presa li valorizzo ulteriormente
                score += trickPoints * 100;
                // valorizzo anche il fatto che ci siano ancora punti in quel seme
                score += remainingPointCardsInLead * 50;
            } else {
                // penalizzo giocare carte forti senza vincere; preferisco scartare le più basse
                score = -candidate.getPriority();
                // se la presa non contiene punti e non ci sono punti rimasti in seme, scartare è ancora più favorevole
                if (trickPoints == 0 && remainingPointCardsInLead == 0) {
                    score += 20; // piccolo bonus per scartare carte inutili
                }
            }

            // tie-breaker randomico per varietà
            score += rnd.nextDouble() * 0.1;

            if (score > bestScore) {
                bestScore = score;
                bestIdx = idx;
            }
        }

        return bestIdx;
    }
}