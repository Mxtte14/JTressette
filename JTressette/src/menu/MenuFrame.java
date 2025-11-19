package menu;

import profile.ProfileMenu;

import javax.swing.*;
import java.awt.*;


/**
 * MenuFrame aggiornato per usare CardLayout e navigare nella stessa finestra.
 * Mantiene la proprietà pubblica `panel` per compatibilità con il codice esistente.
 */
public class MenuFrame extends JFrame {

    public final HomeMenu panel; // mantiene nome e visibilità originale per compatibilità
    private final JPanel cards;
    private ProfileMenu ProfileMenu;

    public MenuFrame() {
        super("JTressette"); 

        // pannello principale (home menu)
        panel = new HomeMenu();

        // pannello profilo (nuova card)
        ProfileMenu profilePanel = new ProfileMenu(this);

        // contenitore a carte
        cards = new JPanel(new CardLayout());
        cards.add(panel, "MENU");
        cards.add(profilePanel, "PROFILE");

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
        CardLayout cl = (CardLayout) cards.getLayout();
        // opzionale: il profilePanel può aggiornarsi quando viene mostrato
        ProfileMenu.refreshFromModel();
        cl.show(cards, "PROFILE");
    }

    /**
     * Torna alla schermata menu (card "MENU").
     */
    public void showMenu() {
        CardLayout cl = (CardLayout) cards.getLayout();
        cl.show(cards, "MENU");
    }
}
