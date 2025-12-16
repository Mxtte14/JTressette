package View.Menu;

import Controller.Profile.ProfileController;
import Model.Impostazioni.MenuImpostazioni;
import View.Profile.ProfileMenu;
import View.Rules.RulesPage;

import Model.Audio.AudioManager;
import javax.swing.*;
import java.awt.*;

/**
 * Frame principale del menu dell'applicazione JTressette.
 * Gestisce la navigazione tra le diverse schermate utilizzando un CardLayout
 * e coordina la riproduzione dell'audio di sottofondo.
 *
 * <p>Schermate gestite:</p>
 * <ul>
 *   <li><b>MENU:</b> menu principale (HomeMenu)</li>
 *   <li><b>PROFILE:</b> profilo utente (ProfileMenu)</li>
 *   <li><b>RULES:</b> regole del gioco (RulesPage)</li>
 *   <li><b>IMPOSTAZIONI:</b> configurazioni (ViewImpostazioni)</li>
 * </ul>
 *
 * <p>Implementa SettingsListener per reagire ai cambiamenti di configurazione
 * come volume, fullscreen e altre opzioni di gioco.</p>
 *
 * @author JTressette Team
 * @version 1.0
 */
public class MenuFrame extends JFrame implements MenuImpostazioni.SettingsListener {

    /** Pannello principale del menu home (public per compatibilità con codice esistente) */
    public final HomeMenu panel;
    
    /** Container con CardLayout per la navigazione tra schermate */
    private final JPanel cards;
    
    /** Pannello del profilo utente */
    private ProfileMenu profilePanel;
    
    /** Pannello delle regole del gioco */
    private RulesPage rulesPanel;
    
    /** Gestore centralizzato dell'audio */
    private final AudioManager audioManager = new AudioManager();

    /**
     * Costruttore del frame principale.
     * Inizializza tutte le schermate, la musica di sottofondo e registra i listener.
     * Configura le dimensioni della finestra e la rende visibile.
     *
     * @param controller il controller del profilo utente per gestire i dati dell'utente
     */
    public MenuFrame(ProfileController controller) {
        super("JTressette");

        // Register as settings listener
        MenuImpostazioni settings = MenuImpostazioni.getInstance();
        settings.addListener(this);

        // Start menu background music with loop and apply volume
        audioManager.setFile(AudioManager.BACKGROUND_MENU);
        updateAudioVolume(settings);
        audioManager.loop();

        // pannello principale (home menu) che riceve il controller
        panel = new HomeMenu(controller);

        // contenitore a carte
        cards = new JPanel(new CardLayout());

        // pannello profilo (nuova card) - passa il container cards e il controller
        profilePanel = new ProfileMenu(cards, controller);

        // pannello regole (nuova card)
        rulesPanel = new RulesPage(cards);

        cards.add(panel, "MENU");
        cards.add(profilePanel, "PROFILE");
        cards.add(rulesPanel, "RULES"); // aggiunta della card Regole

        // collega il click sull'area avatar/nome dell'home per aprire il profilo
        panel.setOnProfileClick(this::showProfile);

        add(cards);

        setTitle("JTressette");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(true);
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    /**
     * Restituisce il gestore audio per controllo esterno.
     * Permette ad altri componenti di interagire con il sistema audio.
     *
     * @return istanza di AudioManager
     */
    public AudioManager getAudioManager() {
        return audioManager;
    }

    /**
     * Riproduce il suono di click per confermare le selezioni del menu.
     * Il suono viene riprodotto solo se gli effetti sonori sono abilitati nelle impostazioni.
     */
    public void playMenuClick() {
        // Only play if sound effects are enabled
        if (MenuImpostazioni.getInstance().isEffects()) {
            audioManager.playMenuClick();
        }
    }

    /**
     * Ferma la musica di sottofondo del menu.
     * Utilizzato quando si passa a una schermata di gioco con musica propria.
     */
    public void stopMenuMusic() {
        audioManager.stop();
    }

    /**
     * Riprende la riproduzione della musica di sottofondo del menu.
     * Utilizzato quando si torna al menu dalla partita o da altre schermate.
     */
    public void resumeMenuMusic() {
        audioManager.setFile(AudioManager.BACKGROUND_MENU);
        audioManager.loop();
    }

    /**
     * Gestisce la transizione da musica del menu a musica di gioco con effetto fade.
     * La musica del menu viene gradualmente ridotta (fade-out) e poi viene eseguito
     * il callback fornito per avviare la musica di gioco.
     *
     * @param onTransitionComplete callback da eseguire al completamento del fade-out (può essere null)
     */
    public void transitionToGameMusic(Runnable onTransitionComplete) {
        audioManager.fadeOut(800, () -> {
            if (onTransitionComplete != null) {
                onTransitionComplete.run();
            }
        });
    }

    /**
     * Restituisce il pannello principale con CardLayout.
     * Permette l'accesso al container delle card per aggiungere nuove schermate.
     *
     * @return il pannello con CardLayout che contiene tutte le schermate
     */
    public JPanel getCardsPanel() {
        return cards;
    }

    /**
     * Mostra la schermata delle impostazioni.
     * Ferma la musica durante la navigazione alla schermata impostazioni.
     */
    public void showSettings() {
        // Implementa la logica per mostrare le impostazioni se necessario
        audioManager.stop();
    }

    /**
     * Mostra la schermata profilo (card "PROFILE").
     */
    public void showProfile() {
        // aggiorna i dati della view dal controller (il ProfileMenu è registrato come listener al controller,
        // ma forziamo comunque un refresh immediato)
        playMenuClick();
        profilePanel.refreshFromModel();
        CardLayout cl = (CardLayout) cards.getLayout();
        cl.show(cards, "PROFILE");
    }

    /**
     * Mostra la schermata regole (card "RULES").
     */
    public void showRules() {
        playMenuClick();
        CardLayout cl = (CardLayout) cards.getLayout();
        cl.show(cards, "RULES");
    }

    /**
     * Torna alla schermata menu (card "MENU").
     */
    public void showMenu() {
        CardLayout cl = (CardLayout) cards.getLayout();
        cl.show(cards, "MENU");
        // Resume menu music if not playing
        if (!audioManager.isPlaying()) {
            resumeMenuMusic();
        }
    }

    /**
     * Callback invocato quando le impostazioni vengono modificate.
     * Applica i cambiamenti di volume all'audio e aggiorna la modalità fullscreen.
     *
     * @param settings l'oggetto MenuImpostazioni con i nuovi valori
     */
    @Override
    public void onSettingsChanged(MenuImpostazioni settings) {
        // Apply volume changes
        updateAudioVolume(settings);

        // Apply fullscreen changes
        updateFullscreen(settings);
    }

    /**
     * Applica il volume corrente delle impostazioni all'audio manager.
     * Converte il volume da scala 0-100 a scala 0.0-MAX_VOLUME_SCALE.
     *
     * @param settings oggetto impostazioni contenente il livello del volume
     */
    private void updateAudioVolume(MenuImpostazioni settings) {
        // Convert 0-100 volume to 0.0-MAX_VOLUME_SCALE range (AudioManager's max safe volume)
        float volume = settings.getVolume() / 100.0f * AudioManager.MAX_VOLUME_SCALE;
        audioManager.setVolume(volume);
    }

    /**
     * Applica o rimuove la modalità fullscreen in base alle impostazioni.
     * Gestisce la transizione tra modalità finestra e schermo intero,
     * ricreando la finestra per applicare le modifiche.
     *
     * @param settings oggetto impostazioni contenente lo stato fullscreen
     */
    private void updateFullscreen(MenuImpostazioni settings) {
        SwingUtilities.invokeLater(() -> {
            GraphicsDevice device = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
            if (settings.isFullscreen()) {
                if (device.isFullScreenSupported() && !isUndecorated()) {
                    dispose();
                    setUndecorated(true);
                    device.setFullScreenWindow(this);
                    setVisible(true);
                }
            } else {
                if (device.getFullScreenWindow() == this) {
                    device.setFullScreenWindow(null);
                    dispose();
                    setUndecorated(false);
                    pack();
                    setLocationRelativeTo(null);
                    setVisible(true);
                }
            }
        });
    }
}