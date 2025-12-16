package View.Menu;

import java.awt.*;

/**
 * Rappresenta una singola opzione selezionabile nel menu principale.
 * Gestisce la renderizzazione visuale con effetti moderni come:
 * <ul>
 *   <li>Gradienti dorati per le opzioni selezionate</li>
 *   <li>Effetti glow e ombreggiature</li>
 *   <li>Indicatori di selezione animati (frecce)</li>
 *   <li>Scaling dinamico per adattarsi a diverse risoluzioni</li>
 * </ul>
 *
 * <p>Lo stile visivo è ispirato ai menu dei giochi moderni con
 * palette di colori dorata e crema su sfondo scuro.</p>
 *
 * <p>Supporta lo scaling dell'UI tramite il metodo setScale() per
 * adattarsi a schermi di dimensioni diverse mantenendo proporzioni coerenti.</p>
 */
public class MenuOption {

    /** Testo visualizzato per questa opzione */
    public String text;

    /** Posizione verticale (coordinata y) dell'opzione sullo schermo */
    public int y;

    /** Posizione orizzontale (coordinata x) del testo */
    public int x = 100;

    /** Colore oro primario per testo selezionato */
    private static final Color GOLD_PRIMARY = new Color(255, 215, 0);

    /** Colore oro chiaro per gradienti */
    private static final Color GOLD_LIGHT = new Color(255, 235, 100);

    /** Colore crema per testo non selezionato */
    private static final Color TEXT_CREAM = new Color(255, 248, 220);

    /** Colore ombra per testo selezionato */
    private static final Color SHADOW_COLOR = new Color(0, 0, 0, 150);

    /** Colore effetto glow */
    private static final Color GLOW_COLOR = new Color(255, 215, 0, 80);

    /** Colore ombra per testo non selezionato */
    private static final Color UNSELECTED_SHADOW = new Color(0, 0, 0, 100);

    /** Nome font utilizzato per il menu */
    private static final String MENU_FONT_FAMILY = "Georgia";

    /** Dimensione base del font (prima dello scaling) */
    private static final int MENU_FONT_BASE_SIZE = 32;

    /** Dimensione base del font per opzioni selezionate */
    private static final int MENU_FONT_SELECTED_BASE_SIZE = 36;

    /** Stile del font (grassetto) */
    private static final int MENU_FONT_STYLE = Font.BOLD;

    /** Fattore di scala dell'UI (1.0 = dimensione base) */
    private double uiScale = 1.0;

    /**
     * Costruttore di MenuOption.
     *
     * @param text il testo da visualizzare per questa opzione
     * @param y la posizione verticale dell'opzione
     */
    public MenuOption(String text, int y) {
        this.text = text;
        this.y = y;
    }

    /**
     * Imposta il fattore di scala per l'UI di questa opzione.
     * Valori tipici vanno da 0.6 a 2.0.
     * Valori non validi (<= 0) vengono normalizzati a 1.0.
     *
     * @param scale fattore di scala da applicare
     */
    public void setScale(double scale) {
        if (scale <= 0) scale = 1.0;
        this.uiScale = scale;
    }

    /**
     * Restituisce il fattore di scala corrente in modo sicuro.
     * Se il valore interno non è valido, restituisce 1.0 come fallback.
     *
     * @return fattore di scala valido (sempre > 0)
     */
    public double uiScaleSafe() {
        return uiScale > 0 ? uiScale : 1.0;
    }

    /**
     * Calcola la larghezza preferita in pixel per questa opzione.
     * Utilizza il font scalato per determinare lo spazio necessario.
     * Utile per il layout e il dimensionamento dei container.
     *
     * @param g2d contesto grafico per misurare il testo
     * @return larghezza in pixel necessaria per il testo
     */
    public int getPreferredWidth(Graphics2D g2d) {
        Font f = new Font(MENU_FONT_FAMILY, MENU_FONT_STYLE, Math.max(8, (int) Math.round(MENU_FONT_BASE_SIZE * uiScale)));
        FontMetrics fm = g2d.getFontMetrics(f);
        return fm.stringWidth(text);
    }

    /**
     * Calcola l'altezza della linea di testo prevista per questa opzione.
     * Utilizza il font scalato per determinare l'altezza.
     *
     * @param g2d contesto grafico per misurare il testo
     * @return altezza in pixel della linea di testo
     */
    public int getLineHeight(Graphics2D g2d) {
        Font f = new Font(MENU_FONT_FAMILY, MENU_FONT_STYLE, Math.max(8, (int) Math.round(MENU_FONT_BASE_SIZE * uiScale)));
        FontMetrics fm = g2d.getFontMetrics(f);
        return fm.getHeight();
    }

    /**
     * Disegna l'opzione del menu con tutti gli effetti visuali.
     * Applica stili diversi in base allo stato di selezione:
     * <ul>
     *   <li><b>Selezionata:</b> gradiente oro, effetto glow, ombra profonda, indicatore freccia</li>
     *   <li><b>Non selezionata:</b> testo crema con ombra leggera</li>
     * </ul>
     *
     * @param g contesto grafico per il disegno
     * @param selected true se l'opzione è attualmente selezionata
     */
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
     * Disegna l'indicatore di selezione (freccia stilizzata) accanto all'opzione.
     * L'indicatore viene scalato in base a uiScale e include:
     * <ul>
     *   <li>Forma triangolare (freccia verso destra)</li>
     *   <li>Gradiente dorato</li>
     *   <li>Ombra</li>
     *   <li>Bordo sottile bianco</li>
     * </ul>
     *
     * @param g2d contesto grafico 2D per il disegno avanzato
     * @param ix coordinata x di partenza dell'indicatore
     * @param iy coordinata y centrale dell'indicatore
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