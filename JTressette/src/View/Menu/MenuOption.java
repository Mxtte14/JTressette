package View.Menu;

import java.awt.*;

/**
 * Rappresenta un'opzione del menu principale con stile moderno.
 * Include effetti di ombra, hover con gradiente e font eleganti.
 *
 * Ora supporta uno scale factor via setScale(double) così il testo e gli indicatori
 * possono crescere/rimpicciolire in base alla dimensione della pagina.
 */
public class MenuOption {

    public String text;
    public int y;       // posizione verticale dell'opzione
    public int x = 100; // posizione orizzontale del testo (modificabile)

    // Colori moderni per il menu
    private static final Color GOLD_PRIMARY = new Color(255, 215, 0);
    private static final Color GOLD_LIGHT = new Color(255, 235, 100);
    private static final Color TEXT_CREAM = new Color(255, 248, 220);
    private static final Color SHADOW_COLOR = new Color(0, 0, 0, 150);
    private static final Color GLOW_COLOR = new Color(255, 215, 0, 80);
    private static final Color UNSELECTED_SHADOW = new Color(0, 0, 0, 100);

    // Font base (dimensione base, poi scalata)
    private static final String MENU_FONT_FAMILY = "Georgia";
    private static final int MENU_FONT_BASE_SIZE = 32;
    private static final int MENU_FONT_SELECTED_BASE_SIZE = 36;
    private static final int MENU_FONT_STYLE = Font.BOLD;

    // scale factor (1.0 = base size). Viene impostato da HomeMenu prima di disegnare.
    private double uiScale = 1.0;

    public MenuOption(String text, int y) {
        this.text = text;
        this.y = y;
    }

    /**
     * Set UI scale for this option. Typical values >= 0.6 and <= 2.0.
     */
    public void setScale(double scale) {
        if (scale <= 0) scale = 1.0;
        this.uiScale = scale;
    }

    /**
     * Safe getter per uiScale: evita valori non validi e ritorna 1.0 come fallback.
     */
    public double uiScaleSafe() {
        return uiScale > 0 ? uiScale : 1.0;
    }

    /**
     * Restituisce la larghezza in pixel prevista per questa opzione (usando la scala applicata).
     * Utile per dimensionare il box che la conterrà.
     */
    public int getPreferredWidth(Graphics2D g2d) {
        Font f = new Font(MENU_FONT_FAMILY, MENU_FONT_STYLE, Math.max(8, (int) Math.round(MENU_FONT_BASE_SIZE * uiScale)));
        FontMetrics fm = g2d.getFontMetrics(f);
        return fm.stringWidth(text);
    }

    /**
     * Restituisce l'altezza di riga prevista per questa opzione usando la scala applicata.
     */
    public int getLineHeight(Graphics2D g2d) {
        Font f = new Font(MENU_FONT_FAMILY, MENU_FONT_STYLE, Math.max(8, (int) Math.round(MENU_FONT_BASE_SIZE * uiScale)));
        FontMetrics fm = g2d.getFontMetrics(f);
        return fm.getHeight();
    }

    public void draw(Graphics g, boolean selected) {
        Graphics2D g2d = (Graphics2D) g;

        // Abilita anti-aliasing per testo più fluido
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Deriva font scalati dalla dimensione base
        int fs = Math.max(8, (int) Math.round(MENU_FONT_BASE_SIZE * uiScale));
        int fsSel = Math.max(fs, (int) Math.round(MENU_FONT_SELECTED_BASE_SIZE * uiScale));
        Font font = new Font(MENU_FONT_FAMILY, MENU_FONT_STYLE, fs);
        Font fontSelected = new Font(MENU_FONT_FAMILY, MENU_FONT_STYLE, fsSel);

        Font useFont = selected ? fontSelected : font;
        g2d.setFont(useFont);

        FontMetrics fm = g2d.getFontMetrics();

        if (selected) {
            // Effetto glow/alone semplificato per l'opzione selezionata
            g2d.setColor(GLOW_COLOR);
            int glowOffset = Math.max(1, (int) Math.round(2 * uiScale));
            g2d.drawString(text, x - glowOffset, y);
            g2d.drawString(text, x + glowOffset, y);
            g2d.drawString(text, x, y - glowOffset);
            g2d.drawString(text, x, y + glowOffset);

            // Ombra del testo
            g2d.setColor(SHADOW_COLOR);
            int shadowOff = Math.max(2, (int) Math.round(3 * uiScale));
            g2d.drawString(text, x + shadowOff, y + shadowOff);

            // Gradiente dorato per il testo selezionato
            GradientPaint gradient = new GradientPaint(
                    x, y - fm.getAscent(), GOLD_LIGHT,
                    x, y, GOLD_PRIMARY
            );
            Paint prev = g2d.getPaint();
            g2d.setPaint(gradient);
            g2d.drawString(text, x, y);
            g2d.setPaint(prev);

            // Simbolo decorativo a sinistra (freccia/indicatore) scalato
            drawSelectionIndicator(g2d, x - (int) Math.round(40 * uiScale), y - (int) Math.round(12 * uiScale));

        } else {
            // Ombra leggera per opzioni non selezionate
            g2d.setColor(UNSELECTED_SHADOW);
            int off = Math.max(1, (int) Math.round(2 * uiScale));
            g2d.drawString(text, x + off, y + off);

            // Testo cremoso per opzioni normali
            g2d.setColor(TEXT_CREAM);
            g2d.drawString(text, x, y);
        }
    }

    /**
     * Disegna un indicatore di selezione (freccia stilizzata), scalato con uiScale.
     */
    private void drawSelectionIndicator(Graphics2D g2d, int ix, int iy) {
        int w = Math.max(8, (int) Math.round(18 * uiScale));
        int h = Math.max(8, (int) Math.round(16 * uiScale));

        int[] xPoints = {ix, ix + w, ix};
        int[] yPoints = {iy - h/2, iy, iy + h/2};

        // Ombra dell'indicatore (scaled)
        g2d.setColor(SHADOW_COLOR);
        int shadowOffset = Math.max(1, (int) Math.round(2 * uiScale));
        int[] xShadow = {ix + shadowOffset, ix + w + shadowOffset, ix + shadowOffset};
        int[] yShadow = {iy - h/2 + shadowOffset, iy + shadowOffset, iy + h/2 + shadowOffset};
        g2d.fillPolygon(xShadow, yShadow, 3);

        // Indicatore con gradiente
        GradientPaint indicatorGradient = new GradientPaint(
                ix, iy - h/2, GOLD_LIGHT,
                ix + w, iy + h/2, GOLD_PRIMARY
        );
        Paint prev = g2d.getPaint();
        g2d.setPaint(indicatorGradient);
        g2d.fillPolygon(xPoints, yPoints, 3);

        // Bordo sottile
        g2d.setColor(new Color(255, 255, 255, 150));
        g2d.setStroke(new BasicStroke(Math.max(1f, (float) (1.5f * uiScale))));
        g2d.drawPolygon(xPoints, yPoints, 3);
        g2d.setPaint(prev);
    }
}