package main;

import menu.MenuFrame;
import java.util.logging.Logger;
import javax.swing.Timer;

public class JTressette {

    private static final Logger LOG = Logger.getLogger(JTressette.class.getName());
    private final MenuFrame frame;

    public JTressette() {
        frame = new MenuFrame();  // crea la finestra e il menu
        setupMouseListener();
        setupRepaintTimer();
    }

    // Gestione click del mouse sul menu
    private void setupMouseListener() {
        frame.panel.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                int selected = frame.panel.getSelectedOption();
                switch (selected) {
                    case 1 -> LOG.info("Avvio partita...");
                    case 2 -> LOG.info("Mostra regole...");
                    case 3 -> LOG.info("Accesso al profilo...");
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
        new JTressette(); // crea e avvia il menu
    }
}

