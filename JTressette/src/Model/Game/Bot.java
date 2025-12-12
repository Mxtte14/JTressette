package Model.Game;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Random;

/**
 * Bot semplice/strategico che cambia comportamento secondo la Difficulty:
 * - EASY: scelta casuale tra mosse legali
 * - MEDIUM: gioca la carta con maggiore forza tra le mosse legali
 * - HARD: se può vincere la presa gioca la minima carta che vince, altrimenti scarta la minima carta
 */
public class Bot implements Giocatore {
    private final String name;
    private final Difficoltà difficulty;
    private final Random rnd = new Random();

    public Bot(String name, Difficoltà difficulty) {
        this.name = name;
        this.difficulty = difficulty;
    }

    @Override public String getName() { return name; }
    @Override public boolean isBot() { return true; }

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
                    // Usa Streams per trovare la minima carta che vince
                    int bestWinIdx = Arrays.stream(legal)
                        .boxed()
                        .filter(idx -> {
                            Cards c = state.getHand(this).get(idx);
                            return c.getSegno() == lead.getSegno() && c.getPriority() > lead.getPriority();
                        })
                        .min(Comparator.comparingInt(idx -> state.getHand(this).get(idx).getPriority()))
                        .orElse(-1);
                    
                    if (bestWinIdx >= 0) return bestWinIdx;
                }
                // altrimenti gioca la carta legale con min strength
                return Arrays.stream(legal)
                        .boxed()
                        .min(Comparator.comparingInt(i -> state.getHand(this).get(i).getPriority()))
                        .orElse(legal[0]);

            case HARD:
                return chooseCardHard(state, legal);
        }
        return 0;
    }

    private int chooseCardHard(GameState state, int[] legal) {
            // Informazioni pubbliche
            var trickCards = state.getTrickCards();
            var played = state.getPlayedCards();
            Cards.Segno leadSuit = trickCards.isEmpty() ? null : trickCards.get(0).getSegno();
            
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
                Cards candidate = state.getHand(this).get(idx);

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

                // quante carte punto rimangono nel seme del lead (approssimazione) usando Streams
                Cards.Segno suitToCheck = (leadSuit != null) ? leadSuit : candidate.getSegno();
                int remainingPointCardsInLead = (int) Arrays.stream(Cards.Rank.values())
                    .filter(r -> {
                        Cards hypothetical = new Cards(suitToCheck, r);
                        // la carta è rimasta se non è nelle played e non è in mano del bot e non è nella trick corrente
                        boolean seen = played.stream().anyMatch(pc -> pc.getSegno() == suitToCheck && pc.getRank() == r);
                        if (seen) return false;
                        
                        boolean inTrick = trickCards.stream().anyMatch(tc -> tc.getSegno() == suitToCheck && tc.getRank() == r);
                        if (inTrick) return false;
                        
                        boolean inMyHand = state.getHand(this).stream().anyMatch(my -> my.getSegno() == suitToCheck && my.getRank() == r);
                        if (inMyHand) return false;
                        
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