package main;

import controller.ProfileController;
import controller.ProfileControllerImpl;
import game.Giocatore;
import profile.StorageProfile;
import profile.UserProfile;
import menu.MenuFrame;
import profile.GamesRecord;
import ui.GameSetup;
import controller.GameController;
import impostazioni.MenuImpostazioni;
import impostazioni.ViewImpostazioni;

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
            frame.setVisible(false);

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

                            // opzionale: mostra dialog con risultato (puoi personalizzare)
                            JOptionPane.showMessageDialog(frame,
                                    "Partita terminata:\n" + record.getResult(),
                                    "Risultato", JOptionPane.INFORMATION_MESSAGE);
                        } catch (Exception ex) {
                            // logga ma non bloccare la UI
                            LOG.warning("Impossibile ottenere o registrare game record: " + ex.getMessage());
                        }
                    }

                    // Riporta l'app alla schermata principale
                    frame.setVisible(true);
                    frame.resumeMenuMusic();
                });
            };

            // Crea il controller della partita passando la lambda onGameEnd
            controllerHolder[0] = new GameController(players, onGameEnd);
            controllerHolder[0].startGame();
        }));
    }

    private void setupRepaintTimer() {
        Timer timer = new Timer(16, e -> frame.panel.repaint());
        timer.start();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(JTressette::new);
    }
}