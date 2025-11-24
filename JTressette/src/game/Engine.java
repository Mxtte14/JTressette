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

    public Engine(List<Player> players) {
        this.state = new GameState(players);
    }

    public GameState getState() { return state; }

    public GamesRecord playMatch() {
        state.deal();

        // loop principale: fino a fine partita
        while (!state.isFinished()) {
            Player current = state.getCurrentPlayer();
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
            }

            // Gestione avanzamento turno:
            Player newCurrent = state.getCurrentPlayer();
            if (newCurrent == current) {
                // la presa è stata completata e current è stato impostato sul vincitore:
                // non avanziamo in questo caso
            } else {
                state.advanceTurn();
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
        for (Player p : state.getPlayers()) {
            if (p.isBot()) opponents.add(p.getName());
        }

        String date = Instant.now().toString();
        return new GamesRecord(date, opponents.toString(), resultSummary);
    }
}