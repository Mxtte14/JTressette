package View.Menu;

import Controller.Profile.ProfileController;
import Model.Impostazioni.MenuImpostazioni;
import View.Profile.ProfileMenu;
import View.Rules.RulesPage;

import Model.Audio.AudioManager;
import javax.swing.*;
import java.awt.*;

/**
 * MenuFrame: contiene le card e gestisce la navigazione.
 * Ora è "View" e riceve il ProfileController per passarlo alle sottoview.
 */
public class MenuFrame extends JFrame implements MenuImpostazioni.SettingsListener {

    /** Pannello del menu principale */
    public final HomeMenu panel;
    
    /** Pannello contenitore con CardLayout */
    private final JPanel cards;
    
    /** Pannello del profilo */
    private ProfileMenu profilePanel;
    
    /** Pannello delle regole */
    private RulesPage rulesPanel;
    
    /** Gestore audio per suoni ed effetti */
    private final AudioManager audioManager = new AudioManager();

    /**
     * Costruttore della finestra del menu.
     *
     * @param controller il controller del profilo per gestire i dati utente
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
     * Get the audio manager for audio control.
     * @return The AudioManager instance
     */
    public AudioManager getAudioManager() {
        return audioManager;
    }

    /**
     * Play menu selection click sound.
     */
    public void playMenuClick() {
        // Only play if sound effects are enabled
        if (MenuImpostazioni.getInstance().isEffects()) {
            audioManager.playMenuClick();
        }
    }

    /**
     * Stop the menu background music (for transitioning to game).
     */
    public void stopMenuMusic() {
        audioManager.stop();
    }

    /**
     * Resume menu background music.
     */
    public void resumeMenuMusic() {
        audioManager.setFile(AudioManager.BACKGROUND_MENU);
        audioManager.loop();
    }

    /**
     * Transition from menu music to game music with fade effect.
     * @param onTransitionComplete Callback when transition is complete
     */
    public void transitionToGameMusic(Runnable onTransitionComplete) {
        audioManager.fadeOut(800, () -> {
            if (onTransitionComplete != null) {
                onTransitionComplete.run();
            }
        });
    }

    /**
     * Restituisce il pannello contenitore con CardLayout.
     *
     * @return il pannello con CardLayout per la navigazione
     */
    public JPanel getCardsPanel() {
        return cards;
    }

    /**
     * Mostra la schermata delle impostazioni.
     * Ferma la riproduzione audio del menu.
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

    @Override
    public void onSettingsChanged(MenuImpostazioni settings) {
        // Apply volume changes
        updateAudioVolume(settings);

        // Apply fullscreen changes
        updateFullscreen(settings);
    }

    private void updateAudioVolume(MenuImpostazioni settings) {
        // Convert 0-100 volume to 0.0-MAX_VOLUME_SCALE range (AudioManager's max safe volume)
        float volume = settings.getVolume() / 100.0f * AudioManager.MAX_VOLUME_SCALE;
        audioManager.setVolume(volume);
    }

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