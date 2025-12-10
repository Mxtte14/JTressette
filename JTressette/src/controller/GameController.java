package controller;

import audio.AudioManager;
import game.*;
import impostazioni.MenuImpostazioni;
import menu.HomeMenu;
import profile.GamesRecord;
import ui.GameView;

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
 * Gestisce tutta la logica della partita (distribuzione, trick, pescata post trick, vincitore, UI update...).
 * NON dipende da Engine.
 */
public class GameController implements MenuImpostazioni.SettingsListener {

    private final GameState gameState;
    private final GiocatoreUmano humanPlayer;
    private final GameView view;
    private final Runnable onGameEnd;
    private final AudioManager audioManager;

    private final ExecutorService gameExecutor;
    private volatile boolean gameRunning = false;

    public GameController(List<Giocatore> players, Runnable onGameEnd) {
        this.onGameEnd = onGameEnd;
        this.gameState = new GameState(players); // Direttamente!
        this.gameExecutor = Executors.newSingleThreadExecutor();
        this.audioManager = new AudioManager();

        // Register as settings listener
        MenuImpostazioni.getInstance().addListener(this);

        // Trova il player umano
        GiocatoreUmano human = null;
        for (Giocatore p : players) {
            if (!p.isBot() && p instanceof GiocatoreUmano) {
                human = (GiocatoreUmano) p;
                break;
            }
        }
        this.humanPlayer = human;

        // Crea view
        this.view = new GameView(gameState, humanPlayer, this);

        // Handle chiusura finestra
        view.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                onExitGame();
            }
        });
    }

    /**
     * Avvia la partita.
     */
    public void startGame() {
        gameRunning = true;

        // Musica background - apply volume from settings
        MenuImpostazioni settings = MenuImpostazioni.getInstance();
        float volume = settings.getVolume() / 100.0f * AudioManager.MAX_VOLUME_SCALE;
        audioManager.setFile(AudioManager.BACKGROUND_GAME);
        audioManager.fadeIn(800, volume);
        audioManager.loop();

        // Animazione distribuzione, poi si distribuiscono 10 carte a giocatore
        view.setVisible(true);
        view.showDealingAnimation(gameState.getPlayers(), () -> {
            gameState.deal(10); // Distribuisci 10 carte a ciascuno!
            view.refresh();
            view.log("Partita iniziata!");

            // Game loop sincrono in background thread
            gameExecutor.submit(this::runGameLoop);
        });
    }

    /**
     * Loop principale della partita (gestisce trick, pescata, avanzamento, UI).
     */
    private void runGameLoop() {
        try {
            while (!gameState.isFinished() && gameRunning) {
                Giocatore current = gameState.getCurrentPlayer();
                SwingUtilities.invokeLater(view::refresh);
                System.out.println(current);
                int idx;
                System.out.println(humanPlayer); // DEBUG
                System.out.println(current == humanPlayer); // DEBUG
                if (humanPlayer != null && current == humanPlayer) {
                    view.log("È il tuo turno - scegli una carta");
                    idx = humanPlayer.chooseCard(gameState);
                } else {
                    Thread.sleep(400);
                    System.out.println("gioca il bot"); // DEBUG
                    idx = current.chooseCard(gameState);
                }
                // Se non valido, gioca la prima mossa legale
                if (idx < 0) {
                    System.out.println("Indice invalido, scelgo mossa legale");
                    int[] legal = gameState.getLegalMoves(current);
                    idx = (legal.length > 0) ? legal[0] : -1;
                }

                if (idx >= 0) {
                    System.out.println("sto giocando la carta");
                    Cards played = gameState.playCard(current, idx);
                    if (played != null) {
                        // Play card sound only if effects are enabled
                        if (MenuImpostazioni.getInstance().isEffects()) {
                            audioManager.playCardSound();
                        }

                        final Cards finalPlayed = played;
                        final Giocatore finalCurrent = current;
                        SwingUtilities.invokeLater(() -> {
                            view.showCardPlayed();
                            view.log(finalCurrent.getName() + " ha giocato " + finalPlayed);
                            view.refresh();
                        });


                        // Se la presa/trick è stata completata
                        System.out.println("Trick cards: " + gameState.getTrickCards().size() + " / Players: " + gameState.getPlayers().size());
                        System.out.println("players: " + gameState.getPlayers());
                        if (gameState.getTrickCards().size() == gameState.getPlayers().size()) {
                            System.out.println("stato: presa completata!");
                            Thread.sleep(1500);

                            final Giocatore trickWinner = gameState.getLastTrickWinner();
                            final int cardsWon = gameState.getLastTrickCardsWon();

                            SwingUtilities.invokeLater(() -> {
                                view.showTrickWon(trickWinner);
                                view.log(trickWinner.getName() + " vince la presa! (+" + cardsWon + " carte)");
                            });

                            Thread.sleep(1000);

                            // -------------------------
                            // LOGICA PESCATA POST-PRESA
                            // -------------------------
                            int winnerIndex = gameState.getPlayers().indexOf(trickWinner);
                            List<Giocatore> players = gameState.getPlayers();
                            for (int i = 0; i < players.size(); i++) {
                                Giocatore p = players.get((winnerIndex + i) % players.size());
                                Cards nuovaCarta = gameState.getDeck().draw();
                                if (nuovaCarta != null) {
                                    gameState.getHandMutable(p).add(nuovaCarta);
                                }
                            }
                            // -------------------------

                            gameState.clearTrick();

                            SwingUtilities.invokeLater(view::refresh);
                        } else {
                            // Avanza turno nella presa corrente (non ancora completata)
                            gameState.advanceTurn();
                        }
                    }
                }
            }

            // Partita terminata!
            String result = calculateResult();
            SwingUtilities.invokeLater(() -> view.showGameOver(result));

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
            return "Vincitore: " + winner.getName() + " (punti: " + gameState.getScaledScoreString(winner) + ")";
        }
        return "Pareggio";
    }

    /**
     * Quando il giocatore umano gioca una carta (col click sulla UI).
     */
    public void onCardPlayed(int cardIndex) {
        if (humanPlayer != null) {
            humanPlayer.submitCardChoice(cardIndex);
        }
    }

    /**
     * Permette di tornare al menu terminata la partita
     */
    public void onReturnToMenu() {
        // pulizie
        gameRunning = false;
        gameExecutor.shutdownNow();
        MenuImpostazioni.getInstance().removeListener(this);
        audioManager.fadeOut(300, audioManager::close);

        SwingUtilities.invokeLater(() -> {
            if (view != null) {
                view.dispose();
            }
            if (onGameEnd != null) {
                onGameEnd.run(); // DELEGA chi ha creato il GameController
            }
        });
    }

    /**
     * Chiusura partita.
     */
    public void onExitGame() {
        gameRunning = false;
        gameExecutor.shutdownNow();

        // Unregister settings listener
        MenuImpostazioni.getInstance().removeListener(this);

        // Fade out musica
        audioManager.fadeOut(500, audioManager::close);

        SwingUtilities.invokeLater(() -> {
            view.dispose();
            if (onGameEnd != null) {
                onGameEnd.run();
            }
        });
    }

    /**
     * Ottieni il game record per lo storico.
     */
    public GamesRecord getGameRecord() {
        String date = Instant.now().toString();

        // Opponenti: tutti tranne l'umano!
        StringJoiner opponentsJoiner = new StringJoiner(",");
        for (Giocatore p : gameState.getPlayers()) {
            if (p != humanPlayer) {
                opponentsJoiner.add(p.getName());
            }
        }
        String opponentNames = opponentsJoiner.toString();

        // Vincitore e punteggi
        var scores = gameState.getScores();
        var winner = scores.entrySet().stream()
                .max(Comparator.comparingInt(java.util.Map.Entry::getValue))
                .map(java.util.Map.Entry::getKey)
                .orElse(null);
        String winnerName = (winner != null) ? winner.getName() : "";
        String winnerScore = (winner != null) ? gameState.getScaledScoreString(winner) : "";
        String myScore = gameState.getScaledScoreString(humanPlayer);

        return new GamesRecord(date, opponentNames, winnerName, winnerScore, myScore);
    }

    public GameView getView() {
        return view;
    }

    public AudioManager getAudioManager() {
        return audioManager;
    }

    @Override
    public void onSettingsChanged(MenuImpostazioni settings) {
        // Apply volume changes to audio
        float volume = settings.getVolume() / 100.0f * AudioManager.MAX_VOLUME_SCALE;
        audioManager.setVolume(volume);

        // Refresh view to apply UI changes (score, messages visibility)
        if (view != null) {
            SwingUtilities.invokeLater(() -> view.refresh());
        }
    }
}