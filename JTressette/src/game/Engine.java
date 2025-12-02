package game;

import profile.GamesRecord;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.time.format.DateTimeFormatter;

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
            int idx;
            try {
                idx = current.chooseCard(state);
            } finally {}

            if (idx <= 0) {
                int[] legal = state.getLegalMoves(current);
                idx = (legal.length > 0) ? legal[0] : -1;
            }

            if (idx > 0) {
                Cards played = state.playCard(current, idx);

                if (state.isTrickJustCompleted()) {
                    Giocatore winner = state.getLastTrickWinner();
                    int cardsWon = state.getLastTrickCardsWon();

                    state.clearTrick();

                    // il vincitore è lo stesso del current quindi non cambio current player
                } else {
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

        LocalDate localDate = LocalDate.now(ZoneId.systemDefault());
        String date = localDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        return new GamesRecord(date, opponents.toString(), resultSummary);
    }
}