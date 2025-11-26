package game;

import java.util.Arrays;
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
                // scegli la carta con massima strength tra le legali
                return Arrays.stream(legal)
                        .boxed()
                        .max((i, j) -> Integer.compare(
                                state.getHand(this).get(i).getPriority(),
                                state.getHand(this).get(j).getPriority()))
                        .orElse(legal[0]);

            case HARD:
                // se esiste una carta legale che vince il lead (se presente) sceglie la minima che vince,
                // altrimenti scarta la minima (più debole)
                Cards lead = state.getLeadCard();
                if (lead != null) {
                    int bestWinIdx = -1;
                    int bestWinStrength = Integer.MAX_VALUE;
                    for (int idx : legal) {
                        Cards c = state.getHand(this).get(idx);
                        if (c.getSegno() == lead.getSegno() && c.getPriority() > lead.getPriority()) {
                            if (c.getPriority() < bestWinStrength) {
                                bestWinStrength = c.getPriority();
                                bestWinIdx = idx;
                            }
                        }
                    }
                    if (bestWinIdx >= 0) return bestWinIdx;
                }
                // altrimenti gioca la carta legale con min strength
                return Arrays.stream(legal)
                        .boxed()
                        .min((i, j) -> Integer.compare(
                                state.getHand(this).get(i).getPriority(),
                                state.getHand(this).get(j).getPriority()))
                        .orElse(legal[0]);

            default:
                return legal[0];
        }
    }
}