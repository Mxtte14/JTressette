package menu;

import controller.ProfileController;
import profile.ProfileMenu;
import rules.RulesPage;

import audio.AudioManager;
import javax.swing.*;
import java.awt.*;

/**
 * MenuFrame: contiene le card e gestisce la navigazione.
 * Ora è "View" e riceve il ProfileController per passarlo alle sottoview.
 */
public class MenuFrame extends JFrame {

    public final HomeMenu panel; // mantiene nome e visibilità originale per compatibilità
    private final JPanel cards;
    private ProfileMenu profilePanel;
    private RulesPage rulesPanel;
    private final AudioManager audioManager = new AudioManager();

    public MenuFrame(ProfileController controller) {
        super("JTressette");

        // Start menu background music with loop
        audioManager.setFile(AudioManager.BACKGROUND_MENU);
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
        audioManager.playMenuClick();
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

    public JPanel getCardsPanel() {
        return cards;
    }

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
}