package menu;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;

public class Cursor {

    private static final Logger LOGGER = Logger.getLogger(Cursor.class.getName());

    public int x, y;
    private int selectedIndex = 0;
    private final HomeMenu mp;
    private BufferedImage image;

    public Cursor(HomeMenu mp) {
        this.mp = mp;
        loadImage();
        setPosition();
    }

    private void loadImage() {
        try {
            image = ImageIO.read(getClass().getResourceAsStream("/main/resource/Sfondo.jpg"));
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Errore nel caricamento del cursore", e);
        }
    }

    public void setSelectedIndex(int index) {
        if (index < 0 || index >= mp.options.length) return;
        selectedIndex = index;
        y = mp.options[index].y - 24; // centratura verticale
        x = 50; // distanza dal bordo sinistro
    }

    public int getSelectedIndex() {
        return selectedIndex;
    }

    private void setPosition() {
        x = 50;
        y = mp.options[selectedIndex].y - 24;
    }

    public void draw(Graphics g) {
        if (image != null) g.drawImage(image, x, y, 32, 32, null);
        else {
            g.setColor(Color.RED);
            g.fillOval(x, y, 20, 20);
        }
    }
}
