package main;

import Controller.Profile.ProfileController;
import Controller.Profile.ProfileControllerImpl;
import Model.Game.Giocatore;
import Model.Profile.StorageProfile;
import Model.Profile.UserProfile;
import View.Menu.MenuFrame;
import Model.Profile.GamesRecord;
import View.Game.GameSetup;
import Controller.Game.GameController;
import Model.Impostazioni.MenuImpostazioni;
import View.Impostazioni.ViewImpostazioni;

import javax.swing.*;
import java.util.List;
import java.util.logging.Logger;
import java.awt.CardLayout;

public class JTressette {

    private static final Logger LOG = Logger.getLogger(JTressette.class.getName());
    private final MenuFrame frame;
    private final ProfileController profileController;
    private final MenuImpostazioni impostazioni;

    public JTressette() {
        // Inizializza storage e controller (Model + Controller)
        StorageProfile storage = new StorageProfile();
        UserProfile userProfile = storage.loadOrCreateDefault();
        profileController = new ProfileControllerImpl(storage, userProfile);

        // Inizializza Settings (carica da file se esiste, altrimenti default)
        impostazioni = MenuImpostazioni.getInstance();

        // Crea la UI (view) passando il controller
        frame = new MenuFrame(profileController);

        // Aggiungi la schermata impostazioni alle "cards" del menu
        addSettingsPanelCard();

        setupMouseListener();
        setupRepaintTimer();
    }

    // Gestione click mouse menu principale
    private void setupMouseListener() {
        frame.panel.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                int selected = frame.panel.getSelectedOption();
                switch (selected) {
                    case 1 -> {
                        frame.playMenuClick();
                        onStartGame(); // AVVIO PARTITA
                    }
                    case 2 -> {
                        LOG.info("Mostra regole...");
                        frame.showRules(); // NAVIGA schermata regole
                    }
                    case 3 -> {
                        LOG.info("Accesso al profilo...");
                        frame.showProfile(); // NAVIGA schermata profilo
                    }
                    case 4 -> {
                        frame.playMenuClick();
                        LOG.info("Impostazioni...");
                        showSettings(); // <-- Mostra schermata impostazioni!
                    }
                    case 5 -> {
                        frame.playMenuClick();
                        LOG.info("Uscita...");
                        System.exit(0);
                    }
                }
            }
        });
    }

    private void addSettingsPanelCard() {
        JPanel cards = frame.getCardsPanel(); // Usa il vero pannello card
        ViewImpostazioni panelImpostazioni = new ViewImpostazioni(impostazioni, frame::showMenu);
        cards.add(panelImpostazioni, "IMPOSTAZIONI");
    }


    private void showSettings() {
        JPanel cards = frame.getCardsPanel();
        CardLayout cl = (CardLayout) cards.getLayout();
        cl.show(cards, "IMPOSTAZIONI");
    }

    private void onStartGame() {
        UserProfile profile = profileController.getProfile();
        GameSetup setup = new GameSetup(frame, profile);
        List<Giocatore> players = setup.showDialogAndGetPlayers();
        if (players == null || players.isEmpty()) return;

        frame.transitionToGameMusic(() -> SwingUtilities.invokeLater(() -> {
            // Fade out the menu frame before hiding it
            Timer fadeTimer = new Timer(16, null);
            final float[] alpha = {1.0f};
            fadeTimer.addActionListener(e -> {
                alpha[0] -= 0.1f;
                if (alpha[0] <= 0.0f) {
                    alpha[0] = 0.0f;
                    fadeTimer.stop();
                    frame.setVisible(false);
                    frame.setOpacity(1.0f); // Reset for when it comes back
                    
                    // Start the game after fade out
                    startGameAfterTransition(players);
                } else {
                    frame.setOpacity(alpha[0]);
                }
            });
            fadeTimer.start();
        }));
    }

    private void startGameAfterTransition(List<Giocatore> players) {
        // user final holder to allow reference inside lambda
        final GameController[] controllerHolder = new GameController[1];

        // onGameEnd: verrà chiamato da GameController.onReturnToMenu()
        Runnable onGameEnd = () -> {
            // Esegui su EDT per essere sicuri di aggiornare UI
            SwingUtilities.invokeLater(() -> {
                // registra partita se possibile (se controller esiste)
                if (controllerHolder[0] != null) {
                    try {
                        GamesRecord record = controllerHolder[0].getGameRecord();
                        profileController.recordMatch(record);

                    } catch (Exception ex) {
                        // logga ma non bloccare la UI
                        LOG.warning("Impossibile ottenere o registrare game record: " + ex.getMessage());
                    }
                }

                // Riporta l'app alla schermata principale con fade-in
                frame.setOpacity(0.0f);
                frame.setVisible(true);
                frame.resumeMenuMusic();
                
                // Fade in the menu frame
                Timer fadeTimer = new Timer(16, null);
                final float[] alpha = {0.0f};
                fadeTimer.addActionListener(e -> {
                    alpha[0] += 0.1f;
                    if (alpha[0] >= 1.0f) {
                        alpha[0] = 1.0f;
                        fadeTimer.stop();
                    }
                    frame.setOpacity(alpha[0]);
                });
                fadeTimer.start();
            });
        };

        // Crea il controller della partita passando la lambda onGameEnd
        controllerHolder[0] = new GameController(players, onGameEnd);
        controllerHolder[0].startGame();
    }

    private void setupRepaintTimer() {
        Timer timer = new Timer(16, e -> frame.panel.repaint());
        timer.start();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(JTressette::new);
    }
}