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

    BufferedImage background;
    public MenuOption[] options;
    public Controller.Game.Cursor cursor;
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

    private JLabel avatarSmallLabel;
    private JLabel nameSmallLabel;
    private JLabel levelBadgeLabel;
    private Runnable onProfileClick;
    private int avatarSize = 46;

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

    public int getSelectedOption() { return selectedOption; }
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

        // apply clamped scale to options (so MenuOption draws with correct scaled font)
        double optionScale = Math.max(0.6, Math.min(1.6, clamped)); // same clamp applied
        for (MenuOption mo : options) mo.setScale(optionScale);

        // background cover
        if (background != null && w > 0 && h > 0) {
            double s = Math.max((double) w / background.getWidth(), (double) h / background.getHeight());
            int drawWidth = (int) Math.round(background.getWidth() * s);
            int drawHeight = (int) Math.round(background.getHeight() * s);
            int drawX = (w - drawWidth) / 2;
            int drawY = (h - drawHeight) / 2;
            g2d.drawImage(background, drawX, drawY, drawWidth, drawHeight, null);
        } else {
            g2d.setColor(Color.DARK_GRAY);
            g2d.fillRect(0,0,w,h);
        }

        g2d.setColor(OVERLAY_COLOR);
        g2d.fillRect(0,0,w,h);

        drawVignette(g2d, w, h);

        // draw menu box moved lower and sized by text width
        drawMenuPanel(g2d, w, h, clamped);

        // draw title using option size (smaller) and slightly larger subtitle
        drawTitleUsingOptionSize(g2d, w, h, optionScale);

        // draw options
        for (int i=0;i<options.length;i++) {
            options[i].draw(g2d, cursor.getSelectedIndex() == i);
        }

        // footer
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
        // Determine option font metrics using the option scale applied earlier
        // Use a temporary font matching MenuOption's family and scaled size to measure text.
        int optionBase = 32;
        int optFontSize = Math.max(8, (int)Math.round(optionBase * options[0].uiScaleSafe()));
        Font tmpFont = new Font("Georgia", Font.BOLD, optFontSize);
        FontMetrics fm = g2d.getFontMetrics(tmpFont);

        int maxTextWidth = 0;
        int textLineHeight = fm.getHeight();
        for (MenuOption mo : options) {
            int tw = fm.stringWidth(mo.text);
            maxTextWidth = Math.max(maxTextWidth, tw);
        }

        int paddingH = (int)Math.round(28 * scale);
        int paddingV = (int)Math.round(22 * scale);

        int desiredBoxW = Math.max(w/4, maxTextWidth + paddingH*2);
        int boxW = (int)Math.round(desiredBoxW * Math.max(0.95, scale));
        int boxH = (int)Math.round( options.length * textLineHeight + paddingV*2 + (options.length-1) * Math.round(8 * scale) );

        // Move box lower
        int panelX = Math.max(16, (w - boxW) / 10);
        int panelY = Math.max((int)(h * 0.30), h/6);
        int arc = Math.max(12, Math.round(boxW/12f));

        // shadow
        g2d.setColor(PANEL_SHADOW);
        g2d.fillRoundRect(panelX + Math.round(6f*(float)scale), panelY + Math.round(6f*(float)scale), boxW, boxH, arc, arc);

        // gradient
        GradientPaint grad = new GradientPaint(panelX, panelY, PANEL_GRADIENT_TOP, panelX, panelY + boxH, PANEL_GRADIENT_BOTTOM);
        Paint prev = g2d.getPaint();
        g2d.setPaint(grad);
        g2d.fillRoundRect(panelX, panelY, boxW, boxH, arc, arc);
        g2d.setPaint(prev);

        // border
        g2d.setColor(PANEL_BORDER);
        g2d.setStroke(new BasicStroke(Math.max(1f, 2f*(float)scale)));
        g2d.drawRoundRect(panelX, panelY, boxW, boxH, arc, arc);

        // inner line
        g2d.setColor(PANEL_INNER_BORDER);
        g2d.setStroke(new BasicStroke(Math.max(0.75f, (float)scale)));
        g2d.drawRoundRect(panelX + Math.round(8f*(float)scale), panelY + Math.round(8f*(float)scale), boxW - Math.round(16f*(float)scale), boxH - Math.round(16f*(float)scale), Math.max(6, arc-4), Math.max(6, arc-4));

        // compute option positions: center texts horizontally inside the content area
        int contentX = panelX + paddingH;
        int contentW = boxW - paddingH*2;
        int baseY = panelY + paddingV;
        int spacing = Math.max(textLineHeight + (int)Math.round(6*scale), boxH / (options.length + 1));
        for (int i=0;i<options.length;i++) {
            int tw = fm.stringWidth(options[i].text);
            options[i].x = contentX + Math.max(0, (contentW - tw)/2);
            options[i].y = baseY + i*spacing + fm.getAscent();
        }
    }

    private void drawTitleUsingOptionSize(Graphics2D g2d, int w, int h, double optionScale) {
        int optionBase = 32;
        float titleSize = Math.max(18f, (float)(optionBase * optionScale));
        Font titleFont = TITLE_FONT.deriveFont(titleSize);
        g2d.setFont(titleFont);
        FontMetrics fm = g2d.getFontMetrics();

        int titleX = (w - fm.stringWidth("JTressette")) / 2;
        int titleY = Math.max(60, h/8);

        for (int i=0;i<SHADOW_COLORS.length;i++) {
            g2d.setColor(SHADOW_COLORS[i]);
            int offset = Math.max(1, 4 - i);
            g2d.drawString("JTressette", titleX + offset, titleY + offset);
        }

        g2d.setColor(TITLE_GLOW);
        g2d.drawString("JTressette", titleX - (int)Math.round(2*optionScale), titleY);
        g2d.drawString("JTressette", titleX + (int)Math.round(2*optionScale), titleY);

        GradientPaint titleGrad = new GradientPaint(titleX, titleY - fm.getAscent(), TITLE_GOLD_LIGHT, titleX, titleY, TITLE_GOLD);
        Paint prev = g2d.getPaint();
        g2d.setPaint(titleGrad);
        g2d.drawString("JTressette", titleX, titleY);
        g2d.setPaint(prev);

        float subSize = Math.max(11f, (float)((optionBase * optionScale) * 0.45 * 1.2));
        Font subFont = SUBTITLE_FONT.deriveFont(subSize);
        g2d.setFont(subFont);
        String subtitle = "Gioco di carte italiano";
        FontMetrics fmSub = g2d.getFontMetrics();
        int subX = (w - fmSub.stringWidth(subtitle)) / 2;
        int subY = titleY + (int)(fm.getHeight() * 0.6);

        g2d.setColor(SUBTITLE_SHADOW);
        g2d.drawString(subtitle, subX + (int)Math.round(1*optionScale), subY + (int)Math.round(1*optionScale));
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