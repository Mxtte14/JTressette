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
    // Sostituisci il metodo setupMouseListener() nel tuo JTressette.java con questa versione
    private void setupMouseListener() {
        frame.panel.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                // Prendi l'indice selezionato dal Cursor (0-based).
                // Se vuoi mantenere la vecchia logica 1-based, usa (sel + 1).
                int sel = -1;
                if (frame.panel.cursor != null) {
                    sel = frame.panel.cursor.getSelectedIndex();
                }
                if (sel < 0) {
                    // niente selezione valida
                    return;
                }

                // switch su 0-based index
                switch (sel) {
                    case 0 -> {
                        frame.playMenuClick();
                        onStartGame(); // AVVIO PARTITA (prima case 1)
                    }
                    case 1 -> {
                        LOG.info("Mostra regole...");
                        frame.showRules(); // (prima case 2)
                    }
                    case 2 -> {
                        LOG.info("Accesso al profilo...");
                        frame.showProfile(); // (prima case 3)
                    }
                    case 3 -> {
                        frame.playMenuClick();
                        LOG.info("Impostazioni...");
                        showSettings(); // (prima case 4)
                    }
                    case 4 -> {
                        frame.playMenuClick();
                        LOG.info("Uscita...");
                        System.exit(0); // (prima case 5)
                    }
                    default -> {
                        // eventualmente loggare index fuori range
                        LOG.fine("Menu click con indice non gestito: " + sel);
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