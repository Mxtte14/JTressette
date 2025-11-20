package menu;

import controller.ProfileController;
import profile.ProfileMenu;
import rules.RulesPage;

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

    public MenuFrame(ProfileController controller) {
        super("JTressette");

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
        setResizable(false);
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    /**
     * Mostra la schermata profilo (card "PROFILE").
     */
    public void showProfile() {
        // aggiorna i dati della view dal controller (il ProfileMenu è registrato come listener al controller,
        // ma forziamo comunque un refresh immediato)
        profilePanel.refreshFromModel();
        CardLayout cl = (CardLayout) cards.getLayout();
        cl.show(cards, "PROFILE");
    }

    /**
     * Mostra la schermata regole (card "RULES").
     */
    public void showRules() {
        CardLayout cl = (CardLayout) cards.getLayout();
        cl.show(cards, "RULES");
    }

    /**
     * Torna alla schermata menu (card "MENU").
     */
    public void showMenu() {
        CardLayout cl = (CardLayout) cards.getLayout();
        cl.show(cards, "MENU");
    }
}