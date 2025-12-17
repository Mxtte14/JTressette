package View.Menu;

import Controller.Profile.ProfileController;
import Controller.Profile.ProfileListener;
import Model.Profile.UserProfile;

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
 * HomeMenu - Menu principale con box dimensionato in base al testo delle opzioni.
 *
 * Modifiche:
 * - Calcolo di uno scale sicuro (clamp a [0.6, 1.6]).
 * - Impostazione esplicita di MenuOption.setScale(scale) prima di misurare e disegnare.
 * - DrawMenuPanel misura i testi in base alla scala delle opzioni e dimensiona il box di conseguenza.
 */
public class HomeMenu extends JPanel implements ProfileListener {

    private static final Logger LOGGER = Logger.getLogger(HomeMenu.class.getName());

    private static final int DESIGN_WIDTH = 1100;
    private static final int DESIGN_HEIGHT = 720;

    /** Immagine di sfondo del menu */
    BufferedImage background;
    
    /** Array di opzioni del menu */
    public MenuOption[] options;
    
    /** Cursore per la navigazione del menu */
    public Controller.Game.Cursor cursor;
    
    /** Indice dell'opzione attualmente selezionata */
    private int selectedOption = 0;

    private static final Color OVERLAY_COLOR = new Color(0, 0, 0, 100);
    private static final Color TITLE_GOLD = new Color(255, 215, 0);
    private static final Color TITLE_GOLD_LIGHT = new Color(255, 235, 130);
    private static final Color PANEL_BORDER = new Color(255, 215, 0, 100);
    private static final Color PANEL_SHADOW = new Color(0, 0, 0, 80);
    private static final Color PANEL_GRADIENT_TOP = new Color(10, 60, 35, 200);
    private static final Color PANEL_GRADIENT_BOTTOM = new Color(5, 40, 25, 220);
    private static final Color PANEL_INNER_BORDER = new Color(255, 215, 0, 30);
    private static final Color TITLE_GLOW = new Color(255, 215, 0, 40);
    private static final Color SUBTITLE_SHADOW = new Color(0, 0, 0, 100);
    private static final Color SUBTITLE_COLOR = new Color(220, 210, 190);
    private static final Color[] SHADOW_COLORS = {
            new Color(0, 0, 0, 130), new Color(0, 0, 0, 110),
            new Color(0, 0, 0, 90), new Color(0, 0, 0, 70)
    };
    private static final float VIGNETTE_RADIUS_FACTOR = 0.8f;

    private static final Font TITLE_FONT = new Font("Georgia", Font.BOLD, 56);
    private static final Font SUBTITLE_FONT = new Font("Georgia", Font.ITALIC, 18);

    /** Etichetta per l'avatar piccolo nell'angolo */
    private JLabel avatarSmallLabel;
    
    /** Etichetta per il nome utente nell'angolo */
    private JLabel nameSmallLabel;
    
    /** Etichetta per il badge del livello */
    private JLabel levelBadgeLabel;
    
    /** Callback da eseguire quando si clicca sul profilo */
    private Runnable onProfileClick;
    
    /** Dimensione dell'avatar in pixel */
    private int avatarSize = 46;

    /**
     * Costruttore del menu principale.
     *
     * @param controller il controller del profilo per gestire i dati utente
     */
    public HomeMenu(ProfileController controller) {
        loadBackground();

        options = new MenuOption[]{
                new MenuOption("Gioca", 0),
                new MenuOption("Regole", 0),
                new MenuOption("Profilo", 0),
                new MenuOption("Impostazioni", 0),
                new MenuOption("Esci", 0)
        };

        for (MenuOption opt : options) opt.x = 120;

        cursor = new Controller.Game.Cursor(this);
        setLayout(new BorderLayout());

        JPanel topPanel = createModernTopPanel();
        add(topPanel, BorderLayout.NORTH);

        setBackground(Color.BLACK);
        setFocusable(true);
        setDoubleBuffered(true);
        setupMouseListeners();

        Timer timer = new Timer(16, e -> repaint());
        timer.start();

        if (controller != null) {
            controller.addListener(this);
            UserProfile p = controller.getProfile();
            if (p != null) onProfileUpdated(p);
        }

        addComponentListener(new ComponentAdapter() {
            @Override public void componentResized(ComponentEvent e) { adjustResponsiveLayout(); }
            @Override public void componentShown(ComponentEvent e) { adjustResponsiveLayout(); }
        });

        SwingUtilities.invokeLater(this::adjustResponsiveLayout);
    }

    private JPanel createModernTopPanel() {
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 10));
        topPanel.setOpaque(false);

        JPanel avatarContainer = new JPanel(null);
        avatarContainer.setOpaque(false);

        avatarSmallLabel = new JLabel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(TITLE_GOLD);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2d.setColor(new Color(30, 30, 30));
                g2d.fillRoundRect(2, 2, getWidth() - 4, getHeight() - 4, 10, 10);
                g2d.dispose();
                super.paintComponent(g);
            }
        };

        avatarSmallLabel.setHorizontalAlignment(SwingConstants.CENTER);
        avatarSmallLabel.setOpaque(false);

        levelBadgeLabel = new JLabel("1", SwingConstants.CENTER) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(new Color(20,20,20));
                g2d.fillOval(0,0,getWidth(),getHeight());
                g2d.setColor(TITLE_GOLD);
                g2d.setStroke(new BasicStroke(Math.max(1f, getWidth()*0.06f)));
                g2d.drawOval(1,1,getWidth()-2,getHeight()-2);
                g2d.dispose();
                super.paintComponent(g);
            }
        };
        levelBadgeLabel.setOpaque(false);
        levelBadgeLabel.setForeground(TITLE_GOLD);

        avatarContainer.add(avatarSmallLabel);
        avatarContainer.add(levelBadgeLabel);

        nameSmallLabel = new JLabel();
        nameSmallLabel.setForeground(new Color(255,248,220));
        nameSmallLabel.setVerticalAlignment(SwingConstants.CENTER);

        JPanel clickable = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        clickable.setOpaque(false);
        clickable.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        clickable.add(nameSmallLabel);
        clickable.add(avatarContainer);
        clickable.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) { if (onProfileClick != null) onProfileClick.run(); }
        });

        avatarContainer.setPreferredSize(new Dimension(52,52));
        avatarSmallLabel.setBounds(0,0,52,52);
        levelBadgeLabel.setBounds(36,36,20,20);

        topPanel.add(clickable);
        return topPanel;
    }

    private void adjustResponsiveLayout() {
        int w = Math.max(1, getWidth());
        double scale = getClampedScale();

        avatarSize = Math.max(36, (int)Math.round(46 * scale * 1.1));
        Container avatarContainer = avatarSmallLabel.getParent();
        if (avatarContainer != null) {
            int cont = avatarSize + 6;
            avatarContainer.setPreferredSize(new Dimension(cont, cont));
            avatarSmallLabel.setBounds(0,0,cont,cont);
            int badge = Math.max(10, avatarSize/3);
            levelBadgeLabel.setBounds(cont - badge - 2, cont - badge - 2, badge, badge);
        }

        float nameFontSize = Math.max(12f, (float)(14 * scale));
        nameSmallLabel.setFont(new Font("Georgia", Font.BOLD, (int) nameFontSize));
        revalidate();
        repaint();
    }

    private void setupMouseListeners() {
        addMouseMotionListener(new MouseMotionAdapter() {
            @Override public void mouseMoved(MouseEvent e) {
                double scale = getClampedScale();
                int verticalMargin = Math.max(8, (int)Math.round(18*scale));
                int horizontalMargin = Math.max(10, (int)Math.round(40*scale));
                for (int i=0;i<options.length;i++) {
                    int top = options[i].y - verticalMargin;
                    int bottom = options[i].y + (int)Math.round(8*scale);
                    if (e.getY() >= top && e.getY() <= bottom && e.getX() >= options[i].x - horizontalMargin && e.getX() <= options[i].x + (int)Math.round(220*scale)) {
                        cursor.setSelectedIndex(i);
                        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                        return;
                    }
                }
                setCursor(Cursor.getDefaultCursor());
            }
        });

        addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                double scale = getClampedScale();
                int verticalMargin = Math.max(8, (int)Math.round(18*scale));
                int horizontalMargin = Math.max(10, (int)Math.round(40*scale));
                for (int i=0;i<options.length;i++) {
                    int top = options[i].y - verticalMargin;
                    int bottom = options[i].y + (int)Math.round(8*scale);
                    if (e.getY() >= top && e.getY() <= bottom && e.getX() >= options[i].x - horizontalMargin && e.getX() <= options[i].x + (int)Math.round(220*scale)) {
                        selectedOption = i + 1;
                        return;
                    }
                }
            }
        });
    }

    /**
     * Compute raw scale and clamp it to a safe range [0.6, 1.6].
     */
    private double getClampedScale() {
        int w = Math.max(1, getWidth());
        int h = Math.max(1, getHeight());
        double sx = (double) w / DESIGN_WIDTH;
        double sy = (double) h / DESIGN_HEIGHT;
        double raw = Math.min(sx, sy);
        return Math.max(0.6, Math.min(1.6, raw));
    }

    /**
     * Restituisce l'indice dell'opzione attualmente selezionata.
     *
     * @return l'indice dell'opzione selezionata (0-based)
     */
    public int getSelectedOption() { return selectedOption; }
    
    /**
     * Imposta il callback da eseguire quando si clicca sul profilo.
     *
     * @param r il callback da eseguire
     */
    public void setOnProfileClick(Runnable r) { this.onProfileClick = r; }

    @Override
    public void onProfileUpdated(UserProfile profile) {
        SwingUtilities.invokeLater(() -> {
            String n = profile.getUsername() == null || profile.getUsername().isBlank() ? "Giocatore" : profile.getUsername();
            nameSmallLabel.setText(n);
            if (levelBadgeLabel != null) levelBadgeLabel.setText(String.valueOf(profile.getLevel()));

            Image icon = null;
            if (profile.getAvatarPath() != null) {
                try { icon = ImageIO.read(new File(profile.getAvatarPath())); }
                catch (IOException ex) { LOGGER.log(Level.WARNING, "Impossibile leggere avatar: " + profile.getAvatarPath(), ex); }
            }
            if (icon == null) {
                try { BufferedImage def = ImageIO.read(getClass().getResourceAsStream("/res/default_images/default_icon.jpg")); if (def != null) icon = def; }
                catch (Exception ex) { LOGGER.log(Level.WARNING, "Impossibile caricare avatar di default", ex); }
            }

            if (icon != null) {
                Image scaled = icon.getScaledInstance(avatarSize, avatarSize, Image.SCALE_SMOOTH);
                avatarSmallLabel.setIcon(new ImageIcon(scaled));
                avatarSmallLabel.setText("");
            } else {
                avatarSmallLabel.setIcon(null);
                avatarSmallLabel.setText("?");
                avatarSmallLabel.setForeground(Color.WHITE);
            }
        });
    }

    private void loadBackground() {
        try (java.io.InputStream is = getClass().getResourceAsStream("/res/default_images/sfondo_1.jpg")) {
            if (is == null) { LOGGER.severe("Immagine di sfondo non trovata"); return; }
            background = ImageIO.read(is);
        } catch (IOException e) { LOGGER.log(Level.SEVERE, "Errore nel caricamento dello sfondo", e); }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        int w = getWidth();
        int h = getHeight();
        double clamped = getClampedScale();

        // Applica scale aumentato alle opzioni (1.3x invece di 1.0x)
        double optionScale = Math.max(0.6, Math.min(1.6, clamped * 1.3));
        for (MenuOption mo : options) mo.setScale(optionScale);

        // Background cover
        if (background != null && w > 0 && h > 0) {
            double s = Math.max((double) w / background.getWidth(), (double) h / background.getHeight());
            int drawWidth = (int) Math.round(background.getWidth() * s);
            int drawHeight = (int) Math.round(background.getHeight() * s);
            int drawX = (w - drawWidth) / 2;
            int drawY = (h - drawHeight) / 2;
            g2d.drawImage(background, drawX, drawY, drawWidth, drawHeight, null);
        } else {
            g2d.setColor(Color.DARK_GRAY);
            g2d.fillRect(0, 0, w, h);
        }

        g2d.setColor(OVERLAY_COLOR);
        g2d.fillRect(0, 0, w, h);

        drawVignette(g2d, w, h);

        // Disegna menu box e titolo con nuove dimensioni
        drawMenuPanel(g2d, w, h, clamped);
        drawTitleUsingOptionSize(g2d, w, h, optionScale);

        // Disegna opzioni
        for (int i = 0; i < options.length; i++) {
            options[i].draw(g2d, cursor.getSelectedIndex() == i);
        }

        // Footer
        drawFooter(g2d, w, h, clamped);
    }

    private void drawVignette(Graphics2D g2d, int w, int h) {
        int centerX = w/2;
        int centerY = h/2;
        float radius = Math.max(w,h) * VIGNETTE_RADIUS_FACTOR;
        RadialGradientPaint vignette = new RadialGradientPaint(centerX, centerY, radius, new float[]{0f,0.7f,1f}, new Color[]{new Color(0,0,0,0), new Color(0,0,0,50), new Color(0,0,0,150)});
        Paint prev = g2d.getPaint();
        g2d.setPaint(vignette);
        g2d.fillRect(0,0,w,h);
        g2d.setPaint(prev);
    }

    private void drawMenuPanel(Graphics2D g2d, int w, int h, double scale) {
        // Font più grande per le opzioni
        int optionBase = 32;
        // Aumentato da 1.0 a 1.3 per opzioni più grandi
        int optFontSize = Math.max(12, (int)Math.round(optionBase * options[0].uiScaleSafe() * 1.3));
        Font tmpFont = new Font("Georgia", Font.BOLD, optFontSize);
        FontMetrics fm = g2d.getFontMetrics(tmpFont);

        int maxTextWidth = 0;
        int textLineHeight = fm.getHeight();
        for (MenuOption mo : options) {
            int tw = fm.stringWidth(mo.text);
            maxTextWidth = Math.max(maxTextWidth, tw);
        }

        // Padding aumentato per box più grande
        int paddingH = (int)Math.round(45 * scale); // era 28
        int paddingV = (int)Math.round(35 * scale); // era 22

        // Box più largo e più alto
        int desiredBoxW = Math.max(w / 3, maxTextWidth + paddingH * 2); // era w/4
        int boxW = (int)Math.round(desiredBoxW * Math.max(1.0, scale)); // era 0.95

        // Spaziatura aumentata tra le opzioni
        int optionSpacing = (int)Math.round(15 * scale); // era 8
        int boxH = (int)Math.round(
                options.length * textLineHeight +
                        paddingV * 2 +
                        (options.length - 1) * optionSpacing
        );

        // Posizione del box - leggermente più in basso
        int panelX = Math.max(20, (w - boxW) / 8); // era /10
        int panelY = Math.max((int)(h * 0.35), h / 5); // era 0.30
        int arc = Math.max(15, Math.round(boxW / 10f)); // era /12

        // Ombra con offset proporzionale
        int shadowOffset = Math.max(4, Math.round(8f * (float)scale));
        g2d.setColor(PANEL_SHADOW);
        g2d.fillRoundRect(
                panelX + shadowOffset,
                panelY + shadowOffset,
                boxW, boxH, arc, arc
        );

        // Gradiente del pannello
        GradientPaint grad = new GradientPaint(
                panelX, panelY,
                PANEL_GRADIENT_TOP,
                panelX, panelY + boxH,
                PANEL_GRADIENT_BOTTOM
        );
        Paint prev = g2d.getPaint();
        g2d.setPaint(grad);
        g2d.fillRoundRect(panelX, panelY, boxW, boxH, arc, arc);
        g2d.setPaint(prev);

        // Bordo esterno
        g2d.setColor(PANEL_BORDER);
        g2d.setStroke(new BasicStroke(Math.max(2f, 3f * (float)scale))); // era 2f
        g2d.drawRoundRect(panelX, panelY, boxW, boxH, arc, arc);

        // Bordo interno
        int innerOffset = Math.max(8, Math.round(12f * (float)scale));
        g2d.setColor(PANEL_INNER_BORDER);
        g2d.setStroke(new BasicStroke(Math.max(1f, 1.5f * (float)scale)));
        g2d.drawRoundRect(
                panelX + innerOffset,
                panelY + innerOffset,
                boxW - innerOffset * 2,
                boxH - innerOffset * 2,
                Math.max(8, arc - 6),
                Math.max(8, arc - 6)
        );

        // Posiziona le opzioni centrate con spaziatura aumentata
        int contentX = panelX + paddingH;
        int contentW = boxW - paddingH * 2;
        int baseY = panelY + paddingV;
        int spacing = textLineHeight + optionSpacing;

        for (int i = 0; i < options.length; i++) {
            int tw = fm.stringWidth(options[i].text);
            options[i].x = contentX + Math.max(0, (contentW - tw) / 2);
            options[i].y = baseY + i * spacing + fm.getAscent();
        }
    }

    private void drawTitleUsingOptionSize(Graphics2D g2d, int w, int h, double optionScale) {
        int optionBase = 32;
        // Aumentato il moltiplicatore da 1.0 a 1.8 per un titolo molto più grande
        float titleSize = Math.max(28f, (float)(optionBase * optionScale * 1.8));
        Font titleFont = TITLE_FONT.deriveFont(titleSize);
        g2d.setFont(titleFont);
        FontMetrics fm = g2d.getFontMetrics();

        int titleX = (w - fm.stringWidth("JTressette")) / 2;
        // Posizionato più in alto per dare più spazio al menu
        int titleY = Math.max(80, (int)(h * 0.15));

        // Ombre con offset proporzionale alla dimensione
        for (int i = 0; i < SHADOW_COLORS.length; i++) {
            g2d.setColor(SHADOW_COLORS[i]);
            int offset = Math.max(2, (int)(6 * optionScale) - i);
            g2d.drawString("JTressette", titleX + offset, titleY + offset);
        }

        // Effetto glow
        g2d.setColor(TITLE_GLOW);
        int glowOffset = Math.max(2, (int)Math.round(3 * optionScale));
        g2d.drawString("JTressette", titleX - glowOffset, titleY);
        g2d.drawString("JTressette", titleX + glowOffset, titleY);

        // Gradiente del titolo
        GradientPaint titleGrad = new GradientPaint(
                titleX, titleY - fm.getAscent(),
                TITLE_GOLD_LIGHT,
                titleX, titleY,
                TITLE_GOLD
        );
        Paint prev = g2d.getPaint();
        g2d.setPaint(titleGrad);
        g2d.drawString("JTressette", titleX, titleY);
        g2d.setPaint(prev);

        // Sottotitolo aumentato proporzionalmente (1.5x più grande)
        float subSize = Math.max(14f, (float)((optionBase * optionScale) * 0.65));
        Font subFont = SUBTITLE_FONT.deriveFont(subSize);
        g2d.setFont(subFont);
        String subtitle = "Gioco di carte italiano";
        FontMetrics fmSub = g2d.getFontMetrics();
        int subX = (w - fmSub.stringWidth(subtitle)) / 2;
        int subY = titleY + (int)(fm.getHeight() * 0.7);

        // Ombra sottotitolo
        g2d.setColor(SUBTITLE_SHADOW);
        int subShadow = Math.max(1, (int)Math.round(2 * optionScale));
        g2d.drawString(subtitle, subX + subShadow, subY + subShadow);
        g2d.setColor(SUBTITLE_COLOR);
        g2d.drawString(subtitle, subX, subY);
    }

    private void drawFooter(Graphics2D g2d, int w, int h, double scale) {
        int footerY = h - (int)Math.round(40*scale);
        GradientPaint lg = new GradientPaint(Math.max(80, w/12), footerY, new Color(255,215,0,0), w/2, footerY, new Color(255,215,0,100));
        Paint prev = g2d.getPaint();
        g2d.setPaint(lg);
        g2d.setStroke(new BasicStroke(Math.max(1f, (float)(1.5f*scale))));
        g2d.drawLine(Math.max(80, w/12), footerY, w/2, footerY);

        GradientPaint lg2 = new GradientPaint(w/2, footerY, new Color(255,215,0,100), w - Math.max(80, w/12), footerY, new Color(255,215,0,0));
        g2d.setPaint(lg2);
        g2d.drawLine(w/2, footerY, w - Math.max(80, w/12), footerY);
        g2d.setPaint(prev);
    }
}