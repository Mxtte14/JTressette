package menu;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.*;

public class HomeMenu extends JPanel {

    private static final Logger LOGGER = Logger.getLogger(HomeMenu.class.getName());

    final int originalTileSize = 16, scale = 3;
    public final int tileSize = originalTileSize * scale;
    public final int maxScreenCol = 17, maxScreenRow = 13;
    public final int screenWidth = tileSize * maxScreenCol, screenHeight = tileSize * maxScreenRow;

    BufferedImage background;
    public MenuOption[] options;
    public Cursor cursor;

    private int selectedOption = 0; // 0 = nessuna selezione, 1..4 = opzioni

    public HomeMenu() {
        loadBackground();

        options = new MenuOption[]{
                new MenuOption("Gioca", 250),
                new MenuOption("Regole", 320),
                new MenuOption("Profilo", 390),
                new MenuOption("Impostazioni", 460),
                new MenuOption("Esci", 520)
        };

        cursor = new Cursor(this);

        setPreferredSize(new Dimension(screenWidth, screenHeight));
        setBackground(Color.BLACK);
        setFocusable(true);
        setDoubleBuffered(true);

        // Mouse movement
        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                for (int i = 0; i < options.length; i++) {
                    int top = options[i].y - 30;
                    int bottom = options[i].y;
                    if (e.getY() >= top && e.getY() <= bottom) {
                        cursor.setSelectedIndex(i);
                    }
                }
            }
        });

        // Mouse click
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                selectedOption = cursor.getSelectedIndex() + 1;
            }
        });

        // Swing Timer per aggiornare il pannello (~60 FPS)
        Timer timer = new Timer(16, e -> repaint());
        timer.start();
    }

    public int getSelectedOption() {
        return selectedOption;
    }

    private void loadBackground() {
        try (java.io.InputStream is = getClass().getResourceAsStream("/main/resource/Sfondo.jpg")) {
            if (is == null) {
                LOGGER.severe("Immagine di sfondo non trovata: /main/resource/Sfondo.jpg");
                return;
            }
            background = ImageIO.read(is);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Errore nel caricamento dello sfondo", e);
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (background != null) g.drawImage(background, 0, 0, screenWidth, screenHeight, null);

        for (int i = 0; i < options.length; i++) {
            options[i].draw(g, cursor.getSelectedIndex() == i);
        }

        cursor.draw(g);
    }
}
