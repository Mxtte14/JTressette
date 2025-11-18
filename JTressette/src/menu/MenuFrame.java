package menu;

import javax.swing.*;

public class MenuFrame extends JFrame {

    public final HomeMenu panel;

    public MenuFrame() {
        panel = new HomeMenu();
        add(panel);
        setTitle("JTressette");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }
}
