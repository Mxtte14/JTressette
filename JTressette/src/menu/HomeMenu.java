
package menu;

import controller.ProfileController;
import controller.ProfileListener;
import profile.UserProfile;


import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.*;

/**
 * HomeMenu è ora View e implementa ProfileListener per aggiornarsi quando il modello cambia.
 * Riceve ProfileController nel costruttore (per registrarsi come listener).
 */
public class HomeMenu extends JPanel implements ProfileListener {

    private static final Logger LOGGER = Logger.getLogger(HomeMenu.class.getName());

    final int originalTileSize = 16, scale = 3;
    public final int tileSize = originalTileSize * scale;
    public final int maxScreenCol = 17, maxScreenRow = 13;
    public final int screenWidth = tileSize * maxScreenCol, screenHeight = tileSize * maxScreenRow;

    BufferedImage background;
    public MenuOption[] options;
    public controller.Cursor cursor;

    private int selectedOption = 0; // 0 = nessuna selezione, 1..4 = opzioni

    // --- campi per avatar/nome in alto a destra
    private final JLabel avatarSmallLabel;
    private final JLabel nameSmallLabel;
    private final ProfileController controller;
    private Runnable onProfileClick; // callback per aprire profilo (MenuFrame imposta)

    public HomeMenu(ProfileController controller) {
        this.controller = controller;

        loadBackground();

        options = new MenuOption[]{
                new MenuOption("Gioca", 250),
                new MenuOption("Regole", 320),
                new MenuOption("Profilo", 390),
                new MenuOption("Impostazioni", 460),
                new MenuOption("Esci", 520)
        };

        cursor = new controller.Cursor(this);

        // usa BorderLayout così possiamo aggiungere un pannello in alto a destra
        setLayout(new BorderLayout());

        // pannello top trasparente per contenere avatar e nome a destra
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 6));
        topPanel.setOpaque(false); // lascia vedere lo sfondo disegnato in paintComponent

        avatarSmallLabel = new JLabel();
        avatarSmallLabel.setPreferredSize(new Dimension(48, 48));
        avatarSmallLabel.setHorizontalAlignment(SwingConstants.CENTER);

        nameSmallLabel = new JLabel();
        nameSmallLabel.setForeground(Color.WHITE);
        nameSmallLabel.setFont(new Font("Serif", Font.BOLD, 16));
        nameSmallLabel.setVerticalAlignment(SwingConstants.CENTER);

        // area cliccabile per aprire il profilo
        JPanel clickable = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        clickable.setOpaque(false);
        clickable.add(nameSmallLabel);
        clickable.add(avatarSmallLabel);
        clickable.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                if (onProfileClick != null) onProfileClick.run();
            }
        });

        topPanel.add(clickable);
        add(topPanel, BorderLayout.NORTH);

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

        // registrazione al controller come listener (observer)
        if (this.controller != null) {
            this.controller.addListener(this);
            // inizializza con i dati correnti
            UserProfile p = this.controller.getProfile();
            if (p != null) onProfileUpdated(p);
        }
    }

    public int getSelectedOption() {
        return selectedOption;
    }

    /**
     * Imposta la callback eseguita quando si clicca l'area avatar/nome.
     */
    public void setOnProfileClick(Runnable r) {
        this.onProfileClick = r;
    }

    @Override
    public void onProfileUpdated(UserProfile profile) {
        // aggiornamento UI da EDT
        SwingUtilities.invokeLater(() -> {
            String n = profile.getName() == null || profile.getName().isBlank() ? "Giocatore" : profile.getName();
            nameSmallLabel.setText(n);

            // avatar: prima prova il path dell'utente, altrimenti carica risorsa di default
            Image icon = null;
            if (profile.getAvatarPath() != null) {
                try {
                    icon = ImageIO.read(new File(profile.getAvatarPath()));
                } catch (IOException ex) {
                    LOGGER.log(Level.WARNING, "Impossibile leggere avatar utente: " + profile.getAvatarPath(), ex);
                }
            }
            if (icon == null) {
                try {
                    BufferedImage def = ImageIO.read(getClass().getResourceAsStream("/main/resource/default_icon.jpg"));
                    if (def != null) icon = def;
                } catch (Exception ex) {
                    LOGGER.log(Level.WARNING, "Impossibile caricare avatar di default dalle risorse", ex);
                }
            }

            if (icon != null) {
                Image scaled = icon.getScaledInstance(48, 48, Image.SCALE_SMOOTH);
                avatarSmallLabel.setIcon(new ImageIcon(scaled));
                avatarSmallLabel.setText("");
            } else {
                avatarSmallLabel.setIcon(null);
                avatarSmallLabel.setText("?");
                avatarSmallLabel.setForeground(Color.WHITE);
            }
        });
    }

    @Override
    public void onProfileSaveFailed(Exception ex) {
        // opzionale: mostrare dialog o loggare
        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(this, "Salvataggio profilo fallito: " + ex.getMessage(), "Errore", JOptionPane.ERROR_MESSAGE);
        });
    }

    private void loadBackground() {
        try (java.io.InputStream is = getClass().getResourceAsStream("/main/resource/sfondo_1.jpg")) {
            if (is == null) {
                LOGGER.severe("Immagine di sfondo non trovata: /main/resource/sfondo_1.jpg");
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
