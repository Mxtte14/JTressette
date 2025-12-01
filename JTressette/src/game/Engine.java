package game;

import profile.GamesRecord;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.StringJoiner;

/**
 * GameEngine che esegue la partita con regole di presa e scoring.
 * GameEngine è sincrono: chiamare playMatch() in background (SwingWorker).
 */
public class Engine {
    private final GameState state;

    public Engine(List<Giocatore> players) {
        this.state = new GameState(players);
    }

    public GameState getState() { return state; }

    public GamesRecord playMatch() throws InterruptedException {
        state.deal();

        // loop principale: fino a fine partita
        while (!state.isFinished()) {
            Giocatore current = state.getCurrentPlayer();
            int idx = -1;
            try {
                idx = current.chooseCard(state);
            } finally {

            }

            if (idx < 0) {
                int[] legal = state.getLegalMoves(current);
                idx = (legal.length > 0) ? legal[0] : -1;
            }

            if (idx >= 0) {
                Cards played = state.playCard(current, idx);
                // (opzionale) log su console
                System.out.println(current.getName() + " played " + played);
                
                // Check if trick was just completed
                if (state.isTrickJustCompleted()) {
                    Giocatore winner = state.getLastTrickWinner();
                    int cardsWon = state.getLastTrickCardsWon();
                    System.out.println(winner.getName() + " wins the trick! (+" + cardsWon + " cards)");
                    
                    // Clear trick buffer after logging
                    state.clearTrick();
                    
                    // currentPlayerIndex is already set to winner in playCard()
                    // so we don't advance turn here
                } else {
                    // Trick not complete, advance to next player
                    state.advanceTurn();
                }
            }
        }

        // determina vincitore per punti
        var scores = state.getScores();
        var winner = scores.entrySet().stream()
                .max(Comparator.comparingInt(java.util.Map.Entry::getValue))
                .map(java.util.Map.Entry::getKey)
                .orElse(null);

        String resultSummary;
        if (winner != null) {
            resultSummary = "Vincitore: " + winner.getName() + " (punti: " + state.getScore(winner) + ")";
        } else {
            resultSummary = "Pareggio";
        }

        // lista avversari bot (comma separated)
        StringJoiner opponents = new StringJoiner(",");
        for (Giocatore p : state.getPlayers()) {
            if (p.isBot()) opponents.add(p.getName());
        }

        String date = Instant.now().toString();
        return new GamesRecord(date, opponents.toString(), resultSummary);
    }
}