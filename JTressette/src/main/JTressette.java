package main;

import controller.ProfileController;
import controller.ProfileControllerImpl;
import profile.ProfileStorage;
import profile.ProfileStorageSerialized;
import profile.UserProfile;
import menu.MenuFrame;
import ui.GameDialog;
import ui.GameSetupDialog;
import game.GameEngine;
import game.HumanPlayer;
import game.Player;
import profile.GamesRecord;

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
        ProfileStorage storage = new ProfileStorageSerialized();
        UserProfile userProfile = storage.loadOrCreateDefault();
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
        // mostra dialog di setup
        GameSetupDialog setup = new GameSetupDialog(frame);
        List<Player> players = setup.showDialogAndGetPlayers();
        if (players == null || players.isEmpty()) return;

        // trova il primo umano nella lista (assumiamo sia il primo)
        HumanPlayer human = null;
        for (Player p : players) {
            if (!p.isBot() && p instanceof HumanPlayer) {
                human = (HumanPlayer) p;
                break;
            }
        }

        // crea GameEngine e GamePlayDialog (UI per l'umano)
        GameEngine engine = new GameEngine(players);
        GamePlayDialog playDialog = null;
        if (human != null) {
            playDialog = new GamePlayDialog(frame, engine, human);
            playDialog.setVisible(true); // modeless: rimane davanti, il motore gira in background
        }

        GamePlayDialog finalPlayDialog = playDialog;
        // avvia partita in background
        SwingWorker<GamesRecord, Void> w = new SwingWorker<>() {
            @Override
            protected GamesRecord doInBackground() {
                return engine.playMatch();
            }
            @Override
            protected void done() {
                try {
                    GamesRecord rec = get();
                    // registra risultato nello storico tramite ProfileController
                    profileController.recordMatch(rec);

                    // mostra risultato
                    JOptionPane.showMessageDialog(frame, "Partita terminata:\n" + rec.getResult(),
                            "Risultato", JOptionPane.INFORMATION_MESSAGE);

                    if (finalPlayDialog != null) {
                        finalPlayDialog.log("Partita terminata: " + rec.getResult());
                        finalPlayDialog.dispose();
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        };
        w.execute();
    }

    // Timer per aggiornare il pannello (~60 FPS)
    private void setupRepaintTimer() {
        Timer timer = new Timer(16, event -> frame.panel.repaint());
        timer.start();
    }

    // -------------------------------
    // PUNTO D'INGRESSO DEL PROGRAMMA
    // -------------------------------
    static void main(String[] args) {
        // Avvia Swing sul EDT
        SwingUtilities.invokeLater(JTressette::new);
    }
}