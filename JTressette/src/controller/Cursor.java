package controller;

import menu.HomeMenu;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
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
        try (java.io.InputStream is = getClass().getResourceAsStream("/res/default_images/white_ping.jpg")) {
            if (is == null) {
                LOGGER.warning("Immagine cursore non trovata: /main/resource/sfondo_1.jpg");
                return;
            }
            image = ImageIO.read(is);
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
}