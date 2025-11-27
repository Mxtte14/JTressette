package main;

import controller.ProfileController;
import controller.ProfileControllerImpl;
import game.Giocatore;
import profile.StorageUser;
import profile.ProfileStorageSerialized;
import profile.UserProfile;
import menu.MenuFrame;
import game.Engine;
import game.GiocatoreUmano;
import profile.GamesRecord;
import ui.GameSetup;
import ui.GameController;

import javax.swing.*;
import java.util.List;
import java.util.logging.Logger;

/**
 * Punto d'ingresso: all'avvio, quando si clicca "Gioca" nel menu viene mostrato il GameSetupDialog.
 * Dopo l'impostazione si avvia GameEngine in background; per la sessione umana viene mostrato GamePlayDialog.
 */
public class JTressette {

    private static final Logger LOG = Logger.getLogger(JTressette.class.getName());
    private final MenuFrame frame;
    private final ProfileController profileController;

    public JTressette() {
        // inizializza storage e controller (Model + Controller)
        StorageUser storage = new ProfileStorageSerialized();
        UserProfile userProfile = ((ProfileStorageSerialized) storage).loadOrCreateDefault();
        profileController = new ProfileControllerImpl(storage, userProfile);

        // crea la UI (View) passando il controller
        frame = new MenuFrame(profileController);

        setupMouseListener();
        setupRepaintTimer();
    }

    // Gestione click del mouse sul menu (mantengo la logica di navigazione qui)
    private void setupMouseListener() {
        frame.panel.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                int selected = frame.panel.getSelectedOption();
                switch (selected) {
                    case 1 -> onStartGame(); // AVVIO PARTITA
                    case 2 -> {
                        LOG.info("Mostra regole...");
                        frame.showRules(); // NAVIGA alla schermata regole
                    }
                    case 3 -> {
                        LOG.info("Accesso al profilo...");
                        frame.showProfile(); // NAVIGA alla schermata profilo
                    }
                    case 4 -> LOG.info("Impostazioni...");
                    case 5 -> {
                        LOG.info("Uscita...");
                        System.exit(0);
                    }
                }
            }
        });
    }

    private void onStartGame() {
        // mostra dialog di setup con il profilo utente per ottenere automaticamente il nome
        UserProfile profile = profileController.getProfile();
        GameSetup setup = new GameSetup(frame, profile);
        List<Giocatore> players = setup.showDialogAndGetPlayers();
        if (players == null || players.isEmpty()) return;

        // Nascondi il menu principale
        frame.setVisible(false);

        // Create controller with Swing-based game view
        final GameController[] controllerHolder = new GameController[1];

        controllerHolder[0] = new GameController(players, () -> {
            // Registra il risultato nel profilo
            GamesRecord record = controllerHolder[0].getGameRecord();
            profileController.recordMatch(record);

            // Mostra il risultato
            SwingUtilities.invokeLater(() -> {
                JOptionPane.showMessageDialog(frame, "Partita terminata:\n" + record.getResult(),
                        "Risultato", JOptionPane.INFORMATION_MESSAGE);
                frame.setVisible(true);
            });
        });

        controllerHolder[0].startGame();
    }

    // Timer per aggiornare il pannello (~60 FPS)
    private void setupRepaintTimer() {
        Timer timer = new Timer(16, event -> frame.panel.repaint());
        timer.start();
    }

    // -------------------------------
    // PUNTO D'INGRESSO DEL PROGRAMMA
    // -------------------------------
    public static void main(String[] args) {
        // Avvia Swing sul EDT
        SwingUtilities.invokeLater(JTressette::new);
    }
}