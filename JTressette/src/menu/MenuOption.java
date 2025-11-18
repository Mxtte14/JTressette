package menu;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;

public class MenuOption {

    public String text;
    public int y;       // posizione verticale dell’opzione
    public int x = 100; // posizione orizzontale del testo (modificabile)

    public MenuOption(String text, int y) {
        this.text = text;
        this.y = y;
    }

    public void draw(Graphics g, boolean selected) {
        if (selected) g.setColor(Color.YELLOW);
        else g.setColor(Color.WHITE);

        g.setFont(new Font("Serif", Font.BOLD, 36));
        g.drawString(text, x, y);
    }
}
