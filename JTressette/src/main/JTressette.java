package main;

import controller.ProfileController;
import controller.ProfileControllerImpl;
import profile.ProfileStorage;
import profile.ProfileStorageJsonP;
import profile.UserProfile;
import menu.MenuFrame;

import javax.swing.*;
import java.util.logging.Logger;

public class JTressette {

    private static final Logger LOG = Logger.getLogger(JTressette.class.getName());
    private final MenuFrame frame;

    public JTressette() {
        // inizializza storage e controller (Model + Controller)
        ProfileStorage storage = new ProfileStorageJsonP();
        UserProfile userProfile = storage.loadOrCreateDefault();
        ProfileController profileController = new ProfileControllerImpl(storage, userProfile);

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
                    case 1 -> LOG.info("Avvio partita...");
                    case 2 -> LOG.info("Mostra regole...");
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