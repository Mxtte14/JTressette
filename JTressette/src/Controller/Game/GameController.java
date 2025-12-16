package Controller.Game;

import Model.Audio.AudioManager;
import Model.Game.*;
import Model.Impostazioni.MenuImpostazioni;
import Model.Profile.GamesRecord;
import View.Game.GameView;

import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

/**
 * Controller principale del gioco che segue il pattern architetturale MVC.
 * Gestisce tutta la logica della partita includendo:
 * <ul>
 *   <li>Distribuzione delle carte ai giocatori</li>
 *   <li>Gestione dei turni e delle prese (trick)</li>
 *   <li>Pescata carte dal mazzo dopo ogni presa</li>
 *   <li>Determinazione del vincitore e calcolo punteggi</li>
 *   <li>Aggiornamento dell'interfaccia utente</li>
 *   <li>Riproduzione audio (musica e effetti sonori)</li>
 *   <li>Salvataggio dello storico partite</li>
 * </ul>
 *
 * <p>Il controller gestisce l'esecuzione asincrona del loop di gioco su un thread separato
 * per non bloccare l'interfaccia grafica, coordinandosi con il thread EDT di Swing
 * per gli aggiornamenti visuali.</p>
 *
 * @author JTressette Team
 * @version 1.0
 */
public class GameController implements MenuImpostazioni.SettingsListener {

    /** Stato corrente della partita (carte, punteggi, mazzo) */
    private final GameState gameState;
    
    /** Riferimento al giocatore umano */
    private final GiocatoreUmano humanPlayer;
    
    /** Vista grafica della partita */
    private final GameView view;
    
    /** Callback da eseguire al termine della partita */
    private final Runnable onGameEnd;
    
    /** Gestore audio per musica ed effetti sonori */
    private final AudioManager audioManager;

    /** Executor per eseguire il loop di gioco in background */
    private final ExecutorService gameExecutor;
    
    /** Flag che indica se la partita è in esecuzione */
    private volatile boolean gameRunning = false;

    /**
     * Costruttore del controller di gioco.
     * Inizializza lo stato di gioco, l'interfaccia grafica e i sistemi audio.
     * Valida che la lista dei giocatori contenga almeno un giocatore umano.
     *
     * @param players lista dei giocatori che partecipano alla partita (deve contenere almeno un giocatore umano)
     * @param onGameEnd callback da eseguire quando la partita termina (può essere null)
     * @throws IllegalArgumentException se la lista dei giocatori è null o vuota
     * @throws IllegalStateException se non viene trovato un giocatore umano nella lista
     */
    public GameController(List<Giocatore> players, Runnable onGameEnd) {
        this.onGameEnd = onGameEnd;

        // Validate input
        if (players == null || players.isEmpty()) {
            throw new IllegalArgumentException("Players list cannot be null or empty");
        }

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

        // Validate that we have a human player
        if (this.humanPlayer == null) {
            throw new IllegalStateException("No human player found in the game");
        }

        // Crea view
        this.view = new GameView(gameState, humanPlayer, this, audioManager);

        // Handle chiusura finestra
        view.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                onExitGame();
            }
        });
    }

    /**
     * Avvia la partita inizializzando musica, interfaccia grafica e loop di gioco.
     * Esegue l'animazione di distribuzione delle carte, poi distribuisce 10 carte a giocatore
     * e avvia il loop principale della partita su un thread in background.
     * La musica di sottofondo viene avviata con un effetto di fade-in.
     */
    public void startGame() {
        gameRunning = true;

        // Musica background - apply volume from settings
        MenuImpostazioni settings = MenuImpostazioni.getInstance();
        float volume = settings.getVolume() / 100.0f * AudioManager.MAX_VOLUME_SCALE;
        audioManager.setFile(AudioManager.BACKGROUND_GAME);
        audioManager.fadeIn(800, volume);
        audioManager.loop();

        // Show window with fade-in effect
        view.setVisible(true);
        view.fadeIn();

        // Animazione distribuzione, poi si distribuiscono 10 carte a giocatore
        view.showDealingAnimation(gameState.getPlayers(), () -> {
            gameState.deal(10); // Distribuisci 10 carte a ciascuno!
            view.refresh();
            view.log("Partita iniziata!");

            // Game loop sincrono in background thread
            gameExecutor.submit(this::runGameLoop);
        });
    }

    /**
     * Loop principale della partita che gestisce il flusso di gioco.
     * Esegue le seguenti operazioni ciclicamente fino al termine della partita:
     * <ul>
     *   <li>Determina il giocatore corrente</li>
     *   <li>Ottiene la scelta della carta (da UI per umani, da AI per bot)</li>
     *   <li>Gioca la carta e mostra l'animazione</li>
     *   <li>Se la presa è completa, determina il vincitore</li>
     *   <li>Fa pescare nuove carte dal mazzo a tutti i giocatori</li>
     *   <li>Avanza al turno successivo</li>
     * </ul>
     * 
     * <p>Il metodo viene eseguito su un thread in background per non bloccare l'UI.
     * Utilizza SwingUtilities.invokeLater per aggiornare l'interfaccia sul thread EDT.</p>
     */
    private void runGameLoop() {
        try {
            while (!gameState.isFinished() && gameRunning) {
                Giocatore current = gameState.getCurrentPlayer();
                SwingUtilities.invokeLater(view::refresh);
                int idx;
                if (humanPlayer != null && current == humanPlayer) {
                    view.log("È il tuo turno - scegli una carta");
                    idx = humanPlayer.chooseCard(gameState);
                } else {
                    Thread.sleep(400);
                    idx = current.chooseCard(gameState);
                }
                // Se non valido, gioca la prima mossa legale
                if (idx < 0) {
                    int[] legal = gameState.getLegalMoves(current);
                    idx = (legal.length > 0) ? legal[0] : -1;
                }

                if (idx >= 0) {
                    Cards played = gameState.playCard(current, idx);
                    if (played != null) {
                        // Play card sound only if effects are enabled
                        if (MenuImpostazioni.getInstance().isEffects()) {
                            audioManager.playCardSound();
                        }

                        final Cards finalPlayed = played;
                        final Giocatore finalCurrent = current;

                        // Usa CountDownLatch per aspettare il completamento dell'animazione
                        final CountDownLatch animationLatch = new CountDownLatch(1);

                        SwingUtilities.invokeLater(() -> {
                            view.showCardPlayed(current, played, () -> {
                                // Notifica il completamento dell'animazione
                                animationLatch.countDown();
                            });
                            view.log(finalCurrent.getName() + " ha giocato " + finalPlayed);
                            // Non chiamare refresh qui - verrà fatto dopo l'animazione
                        });

                        // Aspetta che l'animazione sia completata
                        try {
                            animationLatch.await();
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            // L'eccezione è già gestita dal try-catch esterno
                            return;
                        }


                        // Se la presa/trick è stata completata
                        if (gameState.getTrickCards().size() == gameState.getPlayers().size()) {
                            // Aspetta un momento per vedere tutte le carte sul tavolo
                            Thread.sleep(800);

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
                                    view.showDrawAnimationToPlayerHand(p, () -> {
                                        view.refresh();
                                        if (p == humanPlayer) {
                                            audioManager.playDrawSound(); // suono
                                        }});
                                    gameState.getHandMutable(p).add(nuovaCarta);
                                }
                                Thread.sleep(200);
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

    /**
     * Calcola il risultato finale della partita e riproduce il suono appropriato.
     * Determina il giocatore con il punteggio più alto e riproduce il suono di vittoria
     * o sconfitta in base al risultato del giocatore umano.
     *
     * @return stringa descrittiva del risultato finale (es. "Vincitore: Mario (punti: 5 1/3)")
     */
    private String calculateResult() {
        var scores = gameState.getScores();
        var winner = scores.entrySet().stream()
                .max(Comparator.comparingInt(java.util.Map.Entry::getValue))
                .map(java.util.Map.Entry::getKey)
                .orElse(null);

        // Play victory or defeat sound if effects are enabled
        if (MenuImpostazioni.getInstance().isEffects()) {
            if (winner == humanPlayer) {
                audioManager.playVictorySound();
            } else {
                audioManager.playDefeatSound();
            }
        }

        if (winner != null) {
            return "Vincitore: " + winner.getName() + " (punti: " + gameState.getScaledScoreString(winner) + ")";
        }
        return "Pareggio";
    }

    /**
     * Callback invocato quando il giocatore umano gioca una carta tramite l'interfaccia utente.
     * Questo metodo viene chiamato dall'evento di click sull'interfaccia e inoltra
     * la scelta al giocatore umano che è in attesa nel metodo chooseCard().
     *
     * @param cardIndex indice della carta giocata nella mano del giocatore (0-based)
     */
    public void onCardPlayed(int cardIndex) {
        if (humanPlayer != null) {
            humanPlayer.submitCardChoice(cardIndex);
        }
    }

    /**
     * Gestisce il ritorno al menu principale al termine della partita.
     * Ferma il loop di gioco, chiude le risorse audio con effetto fade-out,
     * deregistra i listener delle impostazioni e applica un effetto di dissolvenza
     * alla finestra prima di chiuderla.
     * Infine invoca il callback onGameEnd per notificare il completamento.
     */
    public void onReturnToMenu() {
        // pulizie
        gameRunning = false;
        gameExecutor.shutdownNow();
        MenuImpostazioni.getInstance().removeListener(this);
        audioManager.fadeOut(300, audioManager::close);

        // Fade out the window before closing
        Timer fadeTimer = new Timer(16, null);
        final float[] alpha = {1.0f};
        fadeTimer.addActionListener(e -> {
            alpha[0] -= 0.1f;
            if (alpha[0] <= 0.0f) {
                alpha[0] = 0.0f;
                fadeTimer.stop();

                SwingUtilities.invokeLater(() -> {
                    if (view != null) {
                        view.dispose();
                    }
                    if (onGameEnd != null) {
                        onGameEnd.run(); // DELEGA chi ha creato il GameController
                    }
                });
            } else {
                view.setOpacity(alpha[0]);
            }
        });
        fadeTimer.start();
    }

    /**
     * Gestisce la chiusura della finestra di gioco durante una partita.
     * Ferma il loop di gioco, rilascia le risorse audio con fade-out,
     * deregistra i listener e applica un effetto di dissolvenza alla finestra.
     * Questo metodo viene chiamato quando l'utente chiude manualmente la finestra.
     */
    public void onExitGame() {
        gameRunning = false;
        gameExecutor.shutdownNow();

        // Unregister settings listener
        MenuImpostazioni.getInstance().removeListener(this);

        // Fade out musica
        audioManager.fadeOut(500, audioManager::close);

        // Fade out the window before closing
        Timer fadeTimer = new Timer(16, null);
        final float[] alpha = {1.0f};
        fadeTimer.addActionListener(e -> {
            alpha[0] -= 0.1f;
            if (alpha[0] <= 0.0f) {
                alpha[0] = 0.0f;
                fadeTimer.stop();

                SwingUtilities.invokeLater(() -> {
                    view.dispose();
                    if (onGameEnd != null) {
                        onGameEnd.run();
                    }
                });
            } else {
                view.setOpacity(alpha[0]);
            }
        });
        fadeTimer.start();
    }

    /**
     * Ottiene il record della partita per salvarlo nello storico del profilo utente.
     * Crea un oggetto GamesRecord contenente tutte le informazioni rilevanti:
     * data, avversari, vincitore, punteggi e esperienza guadagnata.
     *
     * @return oggetto GamesRecord con i dati della partita appena conclusa
     */
    public GamesRecord getGameRecord() {
        String date = Instant.now().toString();

        // Opponenti: tutti tranne l'umano usando Streams!
        String opponentNames = gameState.getPlayers().stream()
                .filter(p -> p != humanPlayer)
                .map(Giocatore::getName)
                .collect(Collectors.joining(","));

        // Vincitore e punteggi
        var scores = gameState.getScores();
        var winner = scores.entrySet().stream()
                .max(Comparator.comparingInt(java.util.Map.Entry::getValue))
                .map(java.util.Map.Entry::getKey)
                .orElse(null);
        String winnerName = (winner != null) ? winner.getName() : "";
        String winnerScore = (winner != null) ? gameState.getScaledScoreString(winner) : "";
        String myScore = gameState.getScaledScoreString(humanPlayer);

        // Get raw points and cards won for experience calculation
        int myPoints = gameState.getScore(humanPlayer);
        int myCardsWon = gameState.getWonCardsCount(humanPlayer);

        return new GamesRecord(date, opponentNames, winnerName, winnerScore, myScore, (Integer) experienceFromGame(humanPlayer, winner, myPoints, myCardsWon));
    }

    /**
     * Calcola l'esperienza guadagnata dal giocatore in base ai risultati della partita.
     * L'esperienza viene calcolata secondo le seguenti regole:
     * <ul>
     *   <li>5 XP per ogni punto segnato</li>
     *   <li>2 XP per ogni carta vinta</li>
     *   <li>50 XP bonus se il giocatore ha vinto</li>
     *   <li>20 XP bonus partecipazione se il giocatore ha perso</li>
     * </ul>
     *
     * @param humanPlayer il giocatore umano
     * @param winner il vincitore della partita
     * @param myPoints punti totali segnati dal giocatore umano
     * @param myCardsWon numero di carte vinte dal giocatore umano
     * @return esperienza totale guadagnata (come Object per compatibilità)
     */
    private Object experienceFromGame(GiocatoreUmano humanPlayer, Giocatore winner, int myPoints, int myCardsWon) {
        int experience = 0;
        experience += myPoints * 5; // XP per punto
        experience += myCardsWon * 2; // XP per carta vinta
        if (humanPlayer == winner) {
            experience += 50; // bonus vittoria
        } else {
            experience += 20; // bonus partecipazione
        }
        return experience;
    }

    /**
     * Restituisce la vista grafica della partita.
     *
     * @return istanza di GameView associata a questo controller
     */
    public GameView getView() {
        return view;
    }

    /**
     * Callback invocato quando le impostazioni del gioco vengono modificate.
     * Applica i cambiamenti di volume all'audio e aggiorna la vista per riflettere
     * le modifiche alla visibilità dei punteggi e dei messaggi.
     *
     * @param settings l'oggetto MenuImpostazioni con i nuovi valori
     */
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