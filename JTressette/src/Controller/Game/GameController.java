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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

/**
 * Controller principale del gioco che gestisce la logica di una partita di Tressette.
 * Segue il pattern MVC separando la logica di gioco dalla presentazione.
 * 
 * <p>Responsabilità principali:</p>
 * <ul>
 *   <li>Coordinare il flusso di gioco tra turni dei giocatori</li>
 *   <li>Gestire la distribuzione e la pescata delle carte</li>
 *   <li>Determinare i vincitori delle prese e della partita</li>
 *   <li>Aggiornare la vista con animazioni e feedback visivi</li>
 *   <li>Gestire l'audio (musica di sottofondo ed effetti sonori)</li>
 *   <li>Generare statistiche e record della partita</li>
 *   <li>Applicare le impostazioni di gioco (volume, effetti)</li>
 * </ul>
 * 
 * <p>Il controller utilizza un ExecutorService per eseguire il loop di gioco
 * su un thread separato, evitando di bloccare l'interfaccia grafica.</p>
 */
public class GameController implements MenuImpostazioni.SettingsListener {

    /** Stato corrente della partita contenente carte, punteggi e giocatori */
    private final GameState gameState;
    
    /** Riferimento al giocatore umano per gestire l'input dell'utente */
    private final GiocatoreUmano humanPlayer;
    
    /** Vista principale del gioco (interfaccia grafica) */
    private final GameView view;
    
    /** Callback da invocare al termine della partita per tornare al menu */
    private final Runnable onGameEnd;
    
    /** Gestore audio per musica ed effetti sonori */
    private final AudioManager audioManager;

    /** Executor per eseguire il loop di gioco su un thread separato */
    private final ExecutorService gameExecutor;
    
    /** Flag volatile per controllare l'esecuzione del loop di gioco */
    private volatile boolean gameRunning = false;

    /**
     * Costruttore del controller di gioco.
     * Inizializza lo stato di gioco, la vista, l'audio e registra i listener necessari.
     * Valida che la lista dei giocatori contenga almeno un giocatore umano.
     * 
     * @param players lista dei giocatori partecipanti (deve contenere almeno un GiocatoreUmano)
     * @param onGameEnd callback da invocare quando la partita termina o viene abbandonata
     * @throws IllegalArgumentException se la lista dei giocatori è nulla o vuota
     * @throws IllegalStateException se non viene trovato nessun giocatore umano
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
     * Avvia la partita.
     * Esegue le seguenti operazioni:
     * <ul>
     *   <li>Imposta il flag gameRunning a true</li>
     *   <li>Avvia la musica di sottofondo con effetto fade-in</li>
     *   <li>Mostra la finestra di gioco con animazione</li>
     *   <li>Riproduce l'animazione di distribuzione delle carte</li>
     *   <li>Distribuisce 10 carte a ciascun giocatore</li>
     *   <li>Avvia il loop principale di gioco su un thread separato</li>
     * </ul>
     * 
     * <p>Il volume della musica viene applicato dalle impostazioni correnti.</p>
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
     * Loop principale della partita.
     * Gestisce l'alternanza dei turni tra giocatori, la giocata delle carte,
     * il completamento delle prese, la pescata di nuove carte e l'aggiornamento della vista.
     * 
     * <p>Funzionamento del loop:</p>
     * <ol>
     *   <li>Determina il giocatore corrente</li>
     *   <li>Richiede la scelta di una carta (attendendo input per giocatori umani)</li>
     *   <li>Valida e gioca la carta scelta</li>
     *   <li>Riproduce animazioni ed effetti sonori</li>
     *   <li>Se la presa è completata: determina il vincitore, assegna punti, pesca nuove carte</li>
     *   <li>Avanza al turno successivo</li>
     *   <li>Ripete fino al termine della partita</li>
     * </ol>
     * 
     * <p>Al termine mostra la schermata di game over con i risultati.</p>
     * 
     * <p>Gestisce InterruptedException per permettere l'interruzione controllata del thread.</p>
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
                        SwingUtilities.invokeLater(() -> {
                            view.showCardPlayed(current, played);
                            view.log(finalCurrent.getName() + " ha giocato " + finalPlayed);
                            view.refresh();
                        });


                        // Se la presa/trick è stata completata
                        if (gameState.getTrickCards().size() == gameState.getPlayers().size()) {
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
     * Calcola il risultato finale della partita.
     * Determina il vincitore in base ai punteggi e riproduce il suono appropriato
     * (vittoria o sconfitta) se gli effetti sonori sono abilitati.
     * 
     * @return stringa descrittiva del risultato (es. "Vincitore: Mario (punti: 8 1/3)")
     *         o "Pareggio" se non c'è un chiaro vincitore
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
     * Chiamato quando il giocatore umano clicca su una carta nell'interfaccia.
     * Trasmette la scelta al GiocatoreUmano che la sta attendendo nel suo turno.
     * 
     * @param cardIndex indice della carta cliccata nella mano del giocatore (0-based)
     */
    public void onCardPlayed(int cardIndex) {
        if (humanPlayer != null) {
            humanPlayer.submitCardChoice(cardIndex);
        }
    }

    /**
     * Gestisce il ritorno al menu principale dopo la conclusione della partita.
     * Esegue le operazioni di pulizia necessarie:
     * <ul>
     *   <li>Ferma il loop di gioco impostando gameRunning a false</li>
     *   <li>Chiude l'executor del gioco</li>
     *   <li>Rimuove il listener delle impostazioni</li>
     *   <li>Effettua fade-out dell'audio</li>
     *   <li>Chiude la finestra di gioco con animazione</li>
     *   <li>Invoca la callback onGameEnd per tornare al menu</li>
     * </ul>
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
     * Gestisce la chiusura forzata della partita (es. chiusura della finestra).
     * Simile a onReturnToMenu ma può essere chiamato in qualsiasi momento
     * per abbandonare la partita in corso.
     * 
     * <p>Esegue le stesse operazioni di pulizia di onReturnToMenu.</p>
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
     * Genera un record della partita completata per lo storico del profilo.
     * Il record include:
     * <ul>
     *   <li>Data e ora della partita</li>
     *   <li>Nomi degli avversari</li>
     *   <li>Nome e punteggio del vincitore</li>
     *   <li>Punteggio del giocatore umano</li>
     *   <li>Esperienza guadagnata calcolata in base a punti, carte vinte e vittoria</li>
     * </ul>
     * 
     * @return oggetto GamesRecord con tutte le statistiche della partita
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
     * Calcola i punti esperienza guadagnati dal giocatore umano nella partita.
     * Il calcolo si basa su:
     * <ul>
     *   <li>5 XP per ogni punto ottenuto</li>
     *   <li>2 XP per ogni carta vinta</li>
     *   <li>50 XP bonus se il giocatore ha vinto</li>
     *   <li>20 XP bonus partecipazione se il giocatore ha perso</li>
     * </ul>
     * 
     * @param humanPlayer il giocatore umano
     * @param winner il vincitore della partita
     * @param myPoints punti ottenuti dal giocatore umano
     * @param myCardsWon numero di carte vinte dal giocatore umano
     * @return punti esperienza totali guadagnati
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
     * Restituisce la vista del gioco.
     * 
     * @return la finestra di gioco (GameView)
     */
    public GameView getView() {
        return view;
    }

    /**
     * Callback invocato quando le impostazioni di gioco vengono modificate.
     * Applica i cambiamenti al volume dell'audio e aggiorna la vista.
     * Implementa l'interfaccia MenuImpostazioni.SettingsListener.
     * 
     * @param settings le nuove impostazioni da applicare
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