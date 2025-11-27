package ui;

import game.*;
import profile.GamesRecord;

import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.StringJoiner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * GameControllerSwing: Controller for the game following MVC pattern.
 * Manages game flow, player actions, and updates the view.
 * Uses Swing instead of JavaFX.
 */
public class GameController {

    private final Engine engine;
    private final GameState gameState;
    private final GiocatoreUmano humanPlayer;
    private final GameView view;
    private final Runnable onGameEnd;

    private final ExecutorService gameExecutor;
    private volatile boolean gameRunning = false;

    public GameController(List<Giocatore> players, Runnable onGameEnd) {
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
        this.view = new GameView(gameState, humanPlayer, this);

        // Handle window close
        view.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                onExitGame();
            }
        });
    }

    /**
     * Start the game.
     */
    public void startGame() {
        gameRunning = true;
        gameState.deal();
        view.setVisible(true);
        view.refresh();
        view.log("Partita iniziata!");

        gameExecutor.submit(this::runGameLoop);
    }

    private void runGameLoop() {
        try {
            while (!gameState.isFinished() && gameRunning) {
                Giocatore current = gameState.getCurrentPlayer();

                SwingUtilities.invokeLater(() -> view.refresh());

                int idx;
                if (current.getName().equals(humanPlayer.getName())) {
                    // Wait for human input
                    view.log("È il tuo turno - scegli una carta");
                    idx = humanPlayer.chooseCard(gameState);
                } else {
                    // Bot plays - delay for visual effect (in background thread, so blocking is acceptable)
                    Thread.sleep(800);
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
                        SwingUtilities.invokeLater(() -> {
                            view.showCardPlayed(finalCurrent, finalPlayed);
                            view.log(finalCurrent.getName() + " ha giocato " + finalPlayed);
                            view.refresh();
                        });

                        // Check if trick was completed (currentPlayer changed to winner)
                        Giocatore newCurrent = gameState.getCurrentPlayer();
                        if (!newCurrent.getName().equals(current.getName())) {
                            // Trick completed - pause to show cards, then clear table
                            Thread.sleep(1500);
                            SwingUtilities.invokeLater(() -> {
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
            SwingUtilities.invokeLater(() -> {
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

        SwingUtilities.invokeLater(() -> {
            view.dispose();
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
}