package ui;

import game.*;
import profile.GamesRecord;

import javafx.application.Platform;
import javafx.stage.Stage;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.StringJoiner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * GameController: Controller for the game following MVC pattern.
 * Manages game flow, player actions, and updates the view.
 */
public class GameController {

    private final Engine engine;
    private final GameState gameState;
    private final GiocatoreUmano humanPlayer;
    private final GameView view;
    private final Stage stage;
    private final Runnable onGameEnd;

    private final ExecutorService gameExecutor;
    private volatile boolean gameRunning = false;

    public GameController(List<Giocatore> players, Stage stage, Runnable onGameEnd) {
        this.stage = stage;
        this.onGameEnd = onGameEnd;
        this.engine = new Engine(players);
        this.gameState = engine.getState();
        this.gameExecutor = Executors.newSingleThreadExecutor();

        // Find human player
        GiocatoreUmano human = null;
        for (Giocatore p : players) {
            if (!p.isBot() && p instanceof GiocatoreUmano) {
                human = (GiocatoreUmano) p;
                break;
            }
        }
        this.humanPlayer = human;

        // Create view
        this.view = new GameView(stage, gameState, humanPlayer, this);
    }

    /**
     * Start the game.
     */
    public void startGame() {
        gameRunning = true;
        gameState.deal();
        view.refresh();
        view.log("Partita iniziata!");

        gameExecutor.submit(this::runGameLoop);
    }

    private void runGameLoop() {
        try {
            while (!gameState.isFinished() && gameRunning) {
                Giocatore current = gameState.getCurrentPlayer();

                Platform.runLater(() -> view.refresh());

                int idx;
                if (current == humanPlayer) {
                    // Wait for human input
                    view.log("È il tuo turno - scegli una carta");
                    idx = humanPlayer.chooseCard(gameState);
                } else {
                    // Bot plays
                    Thread.sleep(800); // Add delay for visual effect
                    idx = current.chooseCard(gameState);
                }

                if (idx < 0) {
                    int[] legal = gameState.getLegalMoves(current);
                    idx = (legal.length > 0) ? legal[0] : -1;
                }

                if (idx >= 0) {
                    Cards played = gameState.playCard(current, idx);
                    if (played != null) {
                        final Cards finalPlayed = played;
                        final Giocatore finalCurrent = current;
                        Platform.runLater(() -> {
                            view.showCardPlayed(finalCurrent, finalPlayed);
                            view.log(finalCurrent.getName() + " ha giocato " + finalPlayed);
                            view.refresh();
                        });

                        // Check if trick was completed (currentPlayer changed to winner)
                        Giocatore newCurrent = gameState.getCurrentPlayer();
                        if (newCurrent != current) {
                            // Trick completed - pause to show cards, then clear table
                            Thread.sleep(1500);
                            Platform.runLater(() -> {
                                view.clearTable();
                                view.log(newCurrent.getName() + " vince la presa!");
                            });
                        }
                    }
                }

                gameState.advanceTurn();
            }

            // Game finished
            String result = calculateResult();
            Platform.runLater(() -> {
                view.showGameOver(result);
            });

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            view.log("Partita interrotta.");
        } catch (Exception e) {
            e.printStackTrace();
            view.log("Errore: " + e.getMessage());
        }
    }

    private String calculateResult() {
        var scores = gameState.getScores();
        var winner = scores.entrySet().stream()
                .max(Comparator.comparingInt(java.util.Map.Entry::getValue))
                .map(java.util.Map.Entry::getKey)
                .orElse(null);

        if (winner != null) {
            return "Vincitore: " + winner.getName() + " (punti: " + gameState.getScore(winner) + ")";
        }
        return "Pareggio";
    }

    /**
     * Called when human player plays a card.
     */
    public void onCardPlayed(int cardIndex) {
        if (humanPlayer != null) {
            humanPlayer.submitCardChoice(cardIndex);
        }
    }

    /**
     * Called when player wants to exit the game.
     */
    public void onExitGame() {
        gameRunning = false;
        gameExecutor.shutdownNow();

        Platform.runLater(() -> {
            stage.close();
            if (onGameEnd != null) {
                onGameEnd.run();
            }
        });
    }

    /**
     * Get the game record for profile storage.
     */
    public GamesRecord getGameRecord() {
        StringJoiner opponents = new StringJoiner(",");
        for (Giocatore p : gameState.getPlayers()) {
            if (p.isBot()) opponents.add(p.getName());
        }

        String date = Instant.now().toString();
        String result = calculateResult();
        return new GamesRecord(date, opponents.toString(), result);
    }

    public GameView getView() {
        return view;
    }

    public Stage getStage() {
        return stage;
    }
}
