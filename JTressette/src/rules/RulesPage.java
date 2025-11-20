package rules;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.net.URL;

/**
 * Pagina che mostra le regole del gioco in forma testuale (HTML) e supporta immagini
 * prese dalle risorse del progetto. Ora presenta un overlay "professionale"
 * con uno sfondo a due colori ispirato al feltro da poker e testo in colore crema.
 *
 * La card viene aggiunta al container esterno (CardLayout) con la chiave "RULES".
 */
public class RulesPage extends JPanel {

    private final JPanel cards;
    private final JEditorPane htmlPane;

    // colori ispirati al feltro da poker e agli accenti delle carte
    private static final Color FELT_TOP = new Color(6, 94, 57);      // verde feltro (più scuro)
    private static final Color FELT_BOTTOM = new Color(18, 121, 75); // verde feltro (più chiaro)
    private static final Color PANEL_BG = new Color(0, 0, 0, 120);   // overlay scuro semitrasparente
    private static final Color TEXT_CREME = new Color(245, 235, 221); // crema per il testo
    private static final Color TITLE_GOLD = new Color(255, 215, 0);   // oro per il titolo
    private static final Color ACCENT_RED = new Color(200, 40, 40);   // rosso per semi (se necessario)

    public RulesPage(JPanel cards) {
        this.cards = cards;
        setOpaque(false); // lasciamo vedere lo sfondo sotto l'overlay
        setLayout(new GridBagLayout()); // centriamo il pannello a finestra

        // pannello centrale che conterrà header e contenuto (trasparente; disegno personalizzato nella paint)
        JPanel contentWrapper = new JPanel(new BorderLayout(12, 12)) {
            @Override
            public boolean isOpaque() {
                return false; // render trasparente per permettere il disegno del rounded box
            }
        };
        contentWrapper.setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));
        contentWrapper.setPreferredSize(new Dimension(820, 560));

        initTop(contentWrapper);

        htmlPane = createHtmlPane();
        JScrollPane sp = new JScrollPane(htmlPane);
        sp.setOpaque(false);
        sp.getViewport().setOpaque(false);
        sp.setBorder(BorderFactory.createEmptyBorder());
        sp.setViewportBorder(null);

        contentWrapper.add(sp, BorderLayout.CENTER);

        // Bottone sotto (opzionale) o si usa il back nella header
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottom.setOpaque(false);
        JButton backFooter = new JButton("Indietro");
        backFooter.addActionListener(this::onBack);
        styleButton(backFooter);
        bottom.add(backFooter);
        contentWrapper.add(bottom, BorderLayout.SOUTH);

        // aggiungiamo il wrapper al centro del pannello principale
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0; gbc.gridy = 0;
        add(contentWrapper, gbc);
    }

    private void initTop(JPanel contentWrapper) {
        JPanel top = new JPanel(new BorderLayout(8, 8)) {
            @Override public boolean isOpaque() { return false; }
        };

        JButton back = new JButton("\u2190 Indietro");
        back.addActionListener(this::onBack);
        styleButton(back);
        top.add(back, BorderLayout.WEST);

        // titolo con semi delle carte come decorazione
        JLabel title = new JLabel("<html><span style='font-weight:bold'>Regole del Tressette</span></html>", SwingConstants.CENTER);
        title.setForeground(TITLE_GOLD);
        title.setFont(new Font("Serif", Font.BOLD, 22));
        title.setHorizontalAlignment(SwingConstants.CENTER);

        // piccolo pannello con i semi (♠ ♥ ♦ ♣)
        JLabel suits = new JLabel("<html><span style='color:black'>&spades;</span> " +
                "<span style='color:red'>\u2665</span> " +
                "<span style='color:red'>\u2666</span> " +
                "<span style='color:black'>\u2663</span></html>", SwingConstants.CENTER);
        suits.setFont(new Font("Serif", Font.PLAIN, 18));
        suits.setForeground(TEXT_CREME);

        JPanel center = new JPanel(new BorderLayout());
        center.setOpaque(false);
        center.add(title, BorderLayout.CENTER);
        center.add(suits, BorderLayout.SOUTH);

        top.add(center, BorderLayout.CENTER);

        contentWrapper.add(top, BorderLayout.NORTH);
    }

    private void styleButton(AbstractButton b) {
        b.setFocusPainted(false);
        b.setBackground(new Color(255, 255, 255, 200));
        b.setForeground(Color.DARK_GRAY);
        b.setBorder(BorderFactory.createEmptyBorder(6, 10, 6, 10));
        b.setFont(new Font("SansSerif", Font.PLAIN, 12));
    }

    private void onBack(ActionEvent ev) {
        CardLayout cl = (CardLayout) cards.getLayout();
        cl.show(cards, "MENU");
    }

    private JEditorPane createHtmlPane() {
        JEditorPane pane = new JEditorPane();
        pane.setContentType("text/html");
        pane.setEditable(false);
        pane.setOpaque(false);
        pane.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE); // usa i font swing

        String html = buildHtmlContent();

        // Imposta stile CSS inline per testo e sfondo trasparente
        String styledHtml = "<html><head><style type='text/css'>"
                + "body { font-family: 'Serif', Georgia, 'Times New Roman'; color: rgb("
                + TEXT_CREME.getRed() + "," + TEXT_CREME.getGreen() + "," + TEXT_CREME.getBlue()
                + "); background: transparent; padding: 8px; }"
                + "h2 { color: rgb(" + TITLE_GOLD.getRed() + "," + TITLE_GOLD.getGreen() + "," + TITLE_GOLD.getBlue() + "); }"
                + "h3 { color: rgb(" + (TEXT_CREME.getRed()-20) + "," + Math.max(0, TEXT_CREME.getGreen()-20) + "," + Math.max(0, TEXT_CREME.getBlue()-20) + "); }"
                + "p { font-size: 12pt; line-height:1.45; }"
                + "ul { margin-left: 1.2em; }"
                + ".note { color: #d6cebf; font-size:10pt; }"
                + "code { background: rgba(255,255,255,0.06); padding:2px 4px; border-radius:3px; }"
                + "</style></head><body>"
                + html
                + "</body></html>";

        // setText usa il renderer HTML; ci assicuriamo che la pane sia non-opaque così si vede l'overlay disegnato
        pane.setText(styledHtml);

        // prova a impostare base URL alle risorse del jar, se necessario per <img src="">
        URL base = getClass().getResource("/main/resource/");
        if (base != null) {
            try {
                // setPage con data: è una soluzione per forzare il base; il browser HTML di JEditorPane è limitato,
                // quindi per garantire funzionamento delle immagini è preferibile usare URL assoluti nelle <img>.
                pane.setPage(new URL("data:text/html," + java.net.URLEncoder.encode(styledHtml, "UTF-8")));
            } catch (IOException ignored) {
                // fallback: setText già fatto
            }
        }

        pane.setCaretPosition(0);
        return pane;
    }

    private String buildHtmlContent() {
        String rulesHtml =
                "<h2>Regole base del Tressette (sintesi)</h2>" +
                        "<p>Il <b>Tressette</b> è un classico gioco di prese italiano. Qui trovi una versione sintetica delle regole per giocare velocemente nella nostra applicazione.</p>" +
                        "<h3>Obiettivo</h3>" +
                        "<p>Accumular più punti possibile prendendo carte di valore.</p>" +
                        "<h3>Carte e valori</h3>" +
                        "<ul>" +
                        "<li>Si gioca con un mazzo da 40 carte (itagliano): le figure e i valori seguono la scala tipica del Tressette.</li>" +
                        "<li>Scala di forza (dalla più forte): <b>3</b>, <b>2</b>, <b>A</b>, <b>K</b>, <b>Q</b>, <b>J</b>, 7..4.</li>" +
                        "</ul>" +
                        "<h3>Svolgimento</h3>" +
                        "<ul>" +
                        "<li>Il giocatore di mano gioca una carta, gli altri devono rispondere del seme se possibile.</li>" +
                        "<li>Vince la presa la carta di seme di mano con il valore più alto secondo la scala del Tressette.</li>" +
                        "</ul>" +
                        "<h3>Punteggi</h3>" +
                        "<p>Al termine della mano si conteggiano i punti dalle carte catturate. Le varianti possono differire sui valori esatti; questa implementazione usa una modalità semplificata.</p>" +
                        "<h3>Consigli</h3>" +
                        "<ul>" +
                        "<li>Ricorda di osservare le carte giocate e pianificare la presa.</li>" +
                        "<li>Usa le regole fornite come base e personalizza la strategia in base alla tua variante preferita.</li>" +
                        "</ul>" +
                        "<p class='note'>Se vuoi aggiungere immagini illustrative (carte, esempi di mano), posso integrarle nella pagina: carica i file PNG/JPG nella cartella <code>src/main/resource/</code> e inseriremo le immagini con URL assoluti.</p>";
        return rulesHtml;
    }

    /**
     * Disegno personalizzato del pannello per ottenere un overlay professionale:
     * - sfondo semitrasparente scuro
     * - riquadro centrale arrotondato con gradiente verde feltro
     * - ombra leggera
     */
    @Override
    protected void paintComponent(Graphics g) {
        // prerogativa: il pannello principale è trasparente; disegniamo un overlay scuro su tutto lo sfondo
        Graphics2D g2 = (Graphics2D) g.create();
        try {
            // anti-aliasing
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();

            // disegno overlay semitrasparente su tutto lo sfondo
            g2.setColor(PANEL_BG);
            g2.fillRect(0, 0, w, h);

            // calcoliamo la posizione e dimensione del box centrale (trovato tramite componenti figlie)
            Component[] comps = getComponents();
            if (comps.length > 0 && comps[0] instanceof JPanel) {
                JPanel wrapper = (JPanel) comps[0];
                Rectangle bounds = wrapper.getBounds();

                int arc = 20;
                // ombra
                g2.setColor(new Color(0, 0, 0, 120));
                g2.fillRoundRect(bounds.x + 6, bounds.y + 6, bounds.width, bounds.height, arc, arc);

                // gradient per il feltro
                GradientPaint gp = new GradientPaint(
                        bounds.x, bounds.y, FELT_TOP,
                        bounds.x, bounds.y + bounds.height, FELT_BOTTOM);

                g2.setPaint(gp);
                g2.fillRoundRect(bounds.x, bounds.y, bounds.width, bounds.height, arc, arc);

                // bordo sottile
                g2.setColor(new Color(255, 255, 255, 40));
                g2.setStroke(new BasicStroke(1.2f));
                g2.drawRoundRect(bounds.x, bounds.y, bounds.width - 1, bounds.height - 1, arc, arc);
            }
        } finally {
            g2.dispose();
        }

        // non chiamare super.paintComponent(g) perché vogliamo mantenere la trasparenza
        // e lasciare che i componenti figli (non-opaque) disegnino il contenuto sopra il box.
    }
}