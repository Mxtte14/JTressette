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

/**
 * Classe principale dell'applicazione JTressette.
 * Gestisce l'inizializzazione del gioco, la navigazione tra i menu e l'avvio delle partite.
 * Coordina le interazioni tra il modello (Model), la vista (View) e il controller (Controller)
 * seguendo il pattern architetturale MVC.
 */
public class JTressette {

    /** Logger per la registrazione degli eventi dell'applicazione */
    private static final Logger LOG = Logger.getLogger(JTressette.class.getName());

    /** Frame principale del menu dell'applicazione */
    private final MenuFrame frame;

    /** Controller per la gestione del profilo utente */
    private final ProfileController profileController;

    /** Gestore delle impostazioni del gioco */
    private final MenuImpostazioni impostazioni;

    /**
     * Costruttore della classe JTressette.
     * Inizializza tutti i componenti principali dell'applicazione:
     * storage dei dati, profilo utente, impostazioni, interfaccia grafica,
     * listener del mouse e timer per il repaint.
     */
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

    /**
     * Configura il listener del mouse per gestire i click sulle opzioni del menu principale.
     * Gestisce le seguenti opzioni (basate su indice 0-based):
     * - 0: Avvia nuova partita
     * - 1: Mostra regole del gioco
     * - 2: Accesso al profilo utente
     * - 3: Impostazioni del gioco
     * - 4: Uscita dall'applicazione
     */
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

    /**
     * Aggiunge il pannello delle impostazioni al CardLayout del frame principale.
     * Permette la navigazione verso la schermata di configurazione del gioco.
     */
    private void addSettingsPanelCard() {
        JPanel cards = frame.getCardsPanel(); // Usa il vero pannello card
        ViewImpostazioni panelImpostazioni = new ViewImpostazioni(impostazioni, frame::showMenu);
        cards.add(panelImpostazioni, "IMPOSTAZIONI");
    }


    /**
     * Mostra la schermata delle impostazioni utilizzando il CardLayout.
     * Effettua il passaggio alla card "IMPOSTAZIONI".
     */
    private void showSettings() {
        JPanel cards = frame.getCardsPanel();
        CardLayout cl = (CardLayout) cards.getLayout();
        cl.show(cards, "IMPOSTAZIONI");
    }

    /**
     * Gestisce l'avvio di una nuova partita.
     * Mostra il dialogo di setup per la selezione dei giocatori,
     * gestisce la transizione della musica, inizializza il GameController
     * e registra la partita al termine del gioco.
     */
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

    /**
     * Configura il timer per il repaint periodico del pannello principale.
     * Il timer si attiva ogni 16 millisecondi (~60 FPS) per garantire
     * un'animazione fluida dell'interfaccia utente.
     */
    private void setupRepaintTimer() {
        Timer timer = new Timer(16, e -> frame.panel.repaint());
        timer.start();
    }

    /**
     * Metodo principale che avvia l'applicazione JTressette.
     * Crea l'istanza dell'applicazione sul thread di dispatching degli eventi Swing (EDT)
     * per garantire la thread-safety delle operazioni sulla GUI.
     *
     * @param args argomenti della linea di comando (non utilizzati)
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(JTressette::new);
    }
}