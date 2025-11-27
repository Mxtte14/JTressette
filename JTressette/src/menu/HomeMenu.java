package menu;

import controller.ProfileController;
import controller.ProfileListener;
import profile.UserProfile;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.*;

/**
 * HomeMenu - Menu principale modernizzato con stile elegante.
 * Include titolo con effetti, menu centrato, sfondo con overlay e profilo utente.
 */
public class HomeMenu extends JPanel implements ProfileListener {

    private static final Logger LOGGER = Logger.getLogger(HomeMenu.class.getName());

    // Dimensioni schermo
    final int originalTileSize = 16, scale = 3;
    public final int tileSize = originalTileSize * scale;
    public final int maxScreenCol = 17, maxScreenRow = 13;
    public final int screenWidth = tileSize * maxScreenCol, screenHeight = tileSize * maxScreenRow;

    // Risorse grafiche
    BufferedImage background;
    public MenuOption[] options;
    public controller.Cursor cursor;

    private int selectedOption = 0;

    // Colori moderni
    private static final Color OVERLAY_COLOR = new Color(0, 0, 0, 100);
    private static final Color TITLE_GOLD = new Color(255, 215, 0);
    private static final Color TITLE_GOLD_LIGHT = new Color(255, 235, 130);
    private static final Color TITLE_SHADOW = new Color(0, 0, 0, 180);
    private static final Color ACCENT_RED = new Color(180, 40, 40);
    private static final Color PANEL_BG = new Color(0, 50, 30, 180);
    private static final Color PANEL_BORDER = new Color(255, 215, 0, 100);

    // Font per il titolo
    private static final Font TITLE_FONT = new Font("Georgia", Font.BOLD, 56);
    private static final Font SUBTITLE_FONT = new Font("Georgia", Font.ITALIC, 18);

    // Campi per profilo utente
    private JLabel avatarSmallLabel;
    private JLabel nameSmallLabel;
    private final ProfileController controller;
    private Runnable onProfileClick;

    // Posizioni menu centrate
    private static final int MENU_START_Y = 280;
    private static final int MENU_SPACING = 55;
    private static final int MENU_X = 120;

    public HomeMenu(ProfileController controller) {
        this.controller = controller;

        loadBackground();

        // Opzioni del menu con posizioni migliorate
        options = new MenuOption[]{
                new MenuOption("Gioca", MENU_START_Y),
                new MenuOption("Regole", MENU_START_Y + MENU_SPACING),
                new MenuOption("Profilo", MENU_START_Y + MENU_SPACING * 2),
                new MenuOption("Impostazioni", MENU_START_Y + MENU_SPACING * 3),
                new MenuOption("Esci", MENU_START_Y + MENU_SPACING * 4)
        };

        // Imposta la posizione X per tutte le opzioni
        for (MenuOption opt : options) {
            opt.x = MENU_X;
        }

        cursor = new controller.Cursor(this);

        setLayout(new BorderLayout());

        // Pannello superiore per profilo utente (moderno)
        JPanel topPanel = createModernTopPanel();
        add(topPanel, BorderLayout.NORTH);

        setPreferredSize(new Dimension(screenWidth, screenHeight));
        setBackground(Color.BLACK);
        setFocusable(true);
        setDoubleBuffered(true);

        setupMouseListeners();

        // Timer per repaint
        Timer timer = new Timer(16, e -> repaint());
        timer.start();

        // Registrazione observer
        if (this.controller != null) {
            this.controller.addListener(this);
            UserProfile p = this.controller.getProfile();
            if (p != null) onProfileUpdated(p);
        }
    }

    private JPanel createModernTopPanel() {
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 10));
        topPanel.setOpaque(false);

        // Avatar con bordo arrotondato
        avatarSmallLabel = new JLabel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Bordo dorato
                g2d.setColor(TITLE_GOLD);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);

                // Sfondo interno
                g2d.setColor(new Color(30, 30, 30));
                g2d.fillRoundRect(2, 2, getWidth() - 4, getHeight() - 4, 10, 10);

                g2d.dispose();
                super.paintComponent(g);
            }
        };
        avatarSmallLabel.setPreferredSize(new Dimension(52, 52));
        avatarSmallLabel.setHorizontalAlignment(SwingConstants.CENTER);

        // Nome utente con stile
        nameSmallLabel = new JLabel();
        nameSmallLabel.setForeground(new Color(255, 248, 220));
        nameSmallLabel.setFont(new Font("Georgia", Font.BOLD, 16));
        nameSmallLabel.setVerticalAlignment(SwingConstants.CENTER);

        // Area cliccabile
        JPanel clickable = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        clickable.setOpaque(false);
        clickable.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        clickable.add(nameSmallLabel);
        clickable.add(avatarSmallLabel);
        clickable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (onProfileClick != null) onProfileClick.run();
            }
        });

        topPanel.add(clickable);
        return topPanel;
    }

    private void setupMouseListeners() {
        // Movimento mouse per hover
        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                for (int i = 0; i < options.length; i++) {
                    int top = options[i].y - 35;
                    int bottom = options[i].y + 5;
                    if (e.getY() >= top && e.getY() <= bottom && e.getX() >= options[i].x - 50 && e.getX() <= options[i].x + 250) {
                        cursor.setSelectedIndex(i);
                        setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
                        return;
                    }
                }
                setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
            }
        });

        // Click mouse
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                for (int i = 0; i < options.length; i++) {
                    int top = options[i].y - 35;
                    int bottom = options[i].y + 5;
                    if (e.getY() >= top && e.getY() <= bottom && e.getX() >= options[i].x - 50 && e.getX() <= options[i].x + 250) {
                        selectedOption = i + 1;
                        return;
                    }
                }
            }
        });
    }

    public int getSelectedOption() {
        return selectedOption;
    }

    public void setOnProfileClick(Runnable r) {
        this.onProfileClick = r;
    }

    @Override
    public void onProfileUpdated(UserProfile profile) {
        SwingUtilities.invokeLater(() -> {
            String n = profile.getName() == null || profile.getName().isBlank() ? "Giocatore" : profile.getName();
            nameSmallLabel.setText(n);

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
                Image scaled = icon.getScaledInstance(46, 46, Image.SCALE_SMOOTH);
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
        Graphics2D g2d = (Graphics2D) g;

        // Anti-aliasing
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        // Sfondo
        if (background != null) {
            g2d.drawImage(background, 0, 0, screenWidth, screenHeight, null);
        }

        // Overlay scuro leggero per migliorare leggibilita
        g2d.setColor(OVERLAY_COLOR);
        g2d.fillRect(0, 0, screenWidth, screenHeight);

        // Vignettatura (scurisce i bordi)
        drawVignette(g2d);

        // Pannello semi-trasparente per il menu
        drawMenuPanel(g2d);

        // Titolo del gioco
        drawTitle(g2d);

        // Simboli carte decorativi
        drawCardSymbols(g2d);

        // Opzioni del menu
        for (int i = 0; i < options.length; i++) {
            options[i].draw(g, cursor.getSelectedIndex() == i);
        }

        // Footer decorativo
        drawFooter(g2d);
    }

    private void drawVignette(Graphics2D g2d) {
        int centerX = screenWidth / 2;
        int centerY = screenHeight / 2;
        float radius = Math.max(screenWidth, screenHeight) * 0.8f;

        RadialGradientPaint vignette = new RadialGradientPaint(
                centerX, centerY, radius,
                new float[]{0.0f, 0.7f, 1.0f},
                new Color[]{
                        new Color(0, 0, 0, 0),
                        new Color(0, 0, 0, 50),
                        new Color(0, 0, 0, 150)
                }
        );
        g2d.setPaint(vignette);
        g2d.fillRect(0, 0, screenWidth, screenHeight);
    }

    private void drawMenuPanel(Graphics2D g2d) {
        int panelX = 50;
        int panelY = 220;
        int panelWidth = 350;
        int panelHeight = 320;
        int arc = 25;

        // Ombra del pannello
        g2d.setColor(new Color(0, 0, 0, 80));
        g2d.fillRoundRect(panelX + 5, panelY + 5, panelWidth, panelHeight, arc, arc);

        // Pannello principale con gradiente
        GradientPaint panelGradient = new GradientPaint(
                panelX, panelY, new Color(10, 60, 35, 200),
                panelX, panelY + panelHeight, new Color(5, 40, 25, 220)
        );
        g2d.setPaint(panelGradient);
        g2d.fillRoundRect(panelX, panelY, panelWidth, panelHeight, arc, arc);

        // Bordo dorato
        g2d.setColor(PANEL_BORDER);
        g2d.setStroke(new BasicStroke(2f));
        g2d.drawRoundRect(panelX, panelY, panelWidth, panelHeight, arc, arc);

        // Linea decorativa interna
        g2d.setColor(new Color(255, 215, 0, 30));
        g2d.setStroke(new BasicStroke(1f));
        g2d.drawRoundRect(panelX + 8, panelY + 8, panelWidth - 16, panelHeight - 16, arc - 5, arc - 5);
    }

    private void drawTitle(Graphics2D g2d) {
        String title = "JTressette";
        g2d.setFont(TITLE_FONT);
        FontMetrics fm = g2d.getFontMetrics();

        int titleX = (screenWidth - fm.stringWidth(title)) / 2;
        int titleY = 120;

        // Ombra multipla per effetto 3D
        for (int i = 4; i >= 1; i--) {
            g2d.setColor(new Color(0, 0, 0, 50 + i * 20));
            g2d.drawString(title, titleX + i, titleY + i);
        }

        // Effetto glow
        g2d.setColor(new Color(255, 215, 0, 40));
        for (int i = 4; i >= 1; i--) {
            g2d.drawString(title, titleX - i, titleY);
            g2d.drawString(title, titleX + i, titleY);
            g2d.drawString(title, titleX, titleY - i);
            g2d.drawString(title, titleX, titleY + i);
        }

        // Gradiente per il titolo
        GradientPaint titleGradient = new GradientPaint(
                titleX, titleY - fm.getAscent(), TITLE_GOLD_LIGHT,
                titleX, titleY, TITLE_GOLD
        );
        g2d.setPaint(titleGradient);
        g2d.drawString(title, titleX, titleY);

        // Sottotitolo
        g2d.setFont(SUBTITLE_FONT);
        String subtitle = "Il classico gioco di carte italiano";
        FontMetrics fmSub = g2d.getFontMetrics();
        int subX = (screenWidth - fmSub.stringWidth(subtitle)) / 2;
        int subY = titleY + 35;

        g2d.setColor(new Color(0, 0, 0, 100));
        g2d.drawString(subtitle, subX + 1, subY + 1);

        g2d.setColor(new Color(220, 210, 190));
        g2d.drawString(subtitle, subX, subY);
    }

    private void drawCardSymbols(Graphics2D g2d) {
        // Simboli delle carte decorativi ai lati del titolo
        Font symbolFont = new Font("Serif", Font.PLAIN, 36);
        g2d.setFont(symbolFont);

        String spade = "\u2660"; // Picche
        String heart = "\u2665"; // Cuori
        String diamond = "\u2666"; // Quadri
        String club = "\u2663"; // Fiori

        int leftX = 80;
        int rightX = screenWidth - 120;
        int y = 100;

        // Simboli a sinistra
        g2d.setColor(new Color(0, 0, 0, 60));
        g2d.drawString(spade, leftX + 1, y + 1);
        g2d.drawString(club, leftX + 1, y + 41);

        g2d.setColor(new Color(40, 40, 40));
        g2d.drawString(spade, leftX, y);
        g2d.drawString(club, leftX, y + 40);

        // Simboli a destra
        g2d.setColor(new Color(0, 0, 0, 60));
        g2d.drawString(heart, rightX + 1, y + 1);
        g2d.drawString(diamond, rightX + 1, y + 41);

        g2d.setColor(ACCENT_RED);
        g2d.drawString(heart, rightX, y);
        g2d.drawString(diamond, rightX, y + 40);
    }

    private void drawFooter(Graphics2D g2d) {
        // Linea decorativa in basso
        int footerY = screenHeight - 40;

        // Gradiente per la linea
        GradientPaint lineGradient = new GradientPaint(
                100, footerY, new Color(255, 215, 0, 0),
                screenWidth / 2, footerY, new Color(255, 215, 0, 100)
        );

        g2d.setPaint(lineGradient);
        g2d.setStroke(new BasicStroke(1.5f));
        g2d.drawLine(100, footerY, screenWidth / 2, footerY);

        // Specchiato a destra
        GradientPaint lineGradient2 = new GradientPaint(
                screenWidth / 2, footerY, new Color(255, 215, 0, 100),
                screenWidth - 100, footerY, new Color(255, 215, 0, 0)
        );
        g2d.setPaint(lineGradient2);
        g2d.drawLine(screenWidth / 2, footerY, screenWidth - 100, footerY);

        // Piccolo diamante al centro
        int diamondSize = 8;
        int[] xPoints = {screenWidth / 2, screenWidth / 2 + diamondSize, screenWidth / 2, screenWidth / 2 - diamondSize};
        int[] yPoints = {footerY - diamondSize, footerY, footerY + diamondSize, footerY};

        g2d.setColor(new Color(255, 215, 0, 150));
        g2d.fillPolygon(xPoints, yPoints, 4);
    }
}
