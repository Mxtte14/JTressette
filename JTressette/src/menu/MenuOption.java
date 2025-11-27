package menu;

import java.awt.*;

/**
 * Rappresenta un'opzione del menu principale con stile moderno.
 * Include effetti di ombra, hover con gradiente e font eleganti.
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

    // Font moderno
    private static final Font MENU_FONT = new Font("Georgia", Font.BOLD, 32);
    private static final Font MENU_FONT_SELECTED = new Font("Georgia", Font.BOLD, 36);

    public MenuOption(String text, int y) {
        this.text = text;
        this.y = y;
    }

    public void draw(Graphics g, boolean selected) {
        Graphics2D g2d = (Graphics2D) g;

        // Abilita anti-aliasing per testo piu fluido
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        Font font = selected ? MENU_FONT_SELECTED : MENU_FONT;
        g2d.setFont(font);

        FontMetrics fm = g2d.getFontMetrics();

        if (selected) {
            // Effetto glow/alone semplificato per l'opzione selezionata (ridotto per performance)
            g2d.setColor(GLOW_COLOR);
            g2d.drawString(text, x - 2, y);
            g2d.drawString(text, x + 2, y);
            g2d.drawString(text, x, y - 2);
            g2d.drawString(text, x, y + 2);

            // Ombra del testo
            g2d.setColor(SHADOW_COLOR);
            g2d.drawString(text, x + 3, y + 3);

            // Gradiente dorato per il testo selezionato
            GradientPaint gradient = new GradientPaint(
                    x, y - fm.getAscent(), GOLD_LIGHT,
                    x, y, GOLD_PRIMARY
            );
            g2d.setPaint(gradient);
            g2d.drawString(text, x, y);

            // Simbolo decorativo a sinistra (freccia/indicatore)
            drawSelectionIndicator(g2d, x - 40, y - 12);

        } else {
            // Ombra leggera per opzioni non selezionate
            g2d.setColor(UNSELECTED_SHADOW);
            g2d.drawString(text, x + 2, y + 2);

            // Testo cremoso per opzioni normali
            g2d.setColor(TEXT_CREAM);
            g2d.drawString(text, x, y);
        }
    }

    /**
     * Disegna un indicatore di selezione (freccia stilizzata)
     */
    private void drawSelectionIndicator(Graphics2D g2d, int ix, int iy) {
        int[] xPoints = {ix, ix + 18, ix};
        int[] yPoints = {iy - 8, iy, iy + 8};

        // Ombra dell'indicatore
        g2d.setColor(SHADOW_COLOR);
        int[] xShadow = {ix + 2, ix + 20, ix + 2};
        int[] yShadow = {iy - 6, iy + 2, iy + 10};
        g2d.fillPolygon(xShadow, yShadow, 3);

        // Indicatore con gradiente
        GradientPaint indicatorGradient = new GradientPaint(
                ix, iy - 8, GOLD_LIGHT,
                ix + 18, iy + 8, GOLD_PRIMARY
        );
        g2d.setPaint(indicatorGradient);
        g2d.fillPolygon(xPoints, yPoints, 3);

        // Bordo sottile
        g2d.setColor(new Color(255, 255, 255, 150));
        g2d.setStroke(new BasicStroke(1.5f));
        g2d.drawPolygon(xPoints, yPoints, 3);
    }
}