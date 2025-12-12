package View.Rules;

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
 *
 * Aggiornamento: dimensioni testo e titoli modificate come richiesto:
 * - testo principale, liste e note: 16pt
 * - titoli (h2, h3): 24pt
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
        // titolo impostato a 24 come richiesto
        title.setFont(new Font("Serif", Font.BOLD, 24));
        title.setHorizontalAlignment(SwingConstants.CENTER);

        JPanel center = new JPanel(new BorderLayout());
        center.setOpaque(false);
        center.add(title, BorderLayout.CENTER);

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

        String styledHtml = getString();

        // setText usa il renderer HTML; ci assicuriamo che la pane sia non-opaque così si vede l'overlay disegnato
        pane.setText(styledHtml);

        // prova a impostare base URL alle risorse del jar, se necessario per <img src="">
        URL base = getClass().getResource("/main/resource/");
        if (base != null) {
            try {
                pane.setPage(new URL("data:text/html," + java.net.URLEncoder.encode(styledHtml, "UTF-8")));
            } catch (IOException ignored) {
                // fallback: setText già fatto
            }
        }

        pane.setCaretPosition(0);
        return pane;
    }

    private String getString() {
        String html = buildHtmlContent();

        // Imposta stile CSS inline per testo e sfondo trasparente con miglioramenti
        // Modifiche: body/p/ul/ol/li -> 16pt; h2/h3 -> 24pt
        return "<html><head><style type='text/css'>"
                + "body { font-family: 'Serif', Georgia, 'Times New Roman'; color: rgb("
                + TEXT_CREME.getRed() + "," + TEXT_CREME.getGreen() + "," + TEXT_CREME.getBlue()
                + "); background: transparent; padding: 12px; line-height: 1.7; }"
                + "h2 { color: rgb(" + TITLE_GOLD.getRed() + "," + TITLE_GOLD.getGreen() + "," + TITLE_GOLD.getBlue()
                + "); margin-top: 20px; margin-bottom: 12px; font-size: 24pt; border-bottom: 2px solid rgba(255,215,0,0.3); padding-bottom: 8px; }"
                + "h3 { color: rgb(" + (TEXT_CREME.getRed()-20) + "," + Math.max(0, TEXT_CREME.getGreen()-20) + "," + Math.max(0, TEXT_CREME.getBlue()-20)
                + "); margin-top: 16px; margin-bottom: 10px; font-size: 24pt; }"
                + "p { font-size: 16pt; line-height: 1.7; margin: 10px 0; }"
                + "ul, ol { margin-left: 1.5em; margin-top: 8px; margin-bottom: 8px; }"
                + "li { margin: 6px 0; font-size: 16pt; }"
                + "ul ul { margin-left: 1.2em; }"
                + ".note { color: #d6cebf; font-size: 16pt; background: rgba(255,255,255,0.03); padding: 10px; "
                + "border-left: 3px solid rgba(255,215,0,0.5); margin: 15px 0; border-radius: 4px; }"
                + "code, b { background: rgba(255,255,255,0.08); padding: 2px 6px; border-radius: 3px; font-weight: bold; }"
                + "ol { counter-reset: item; list-style-type: none; }"
                + "ol > li { counter-increment: item; position: relative; padding-left: 36px; }"
                + "ol > li:before { content: counter(item); position: absolute; left: 0; top: 0; "
                + "background: rgba(255,215,0,0.2); color: rgb(" + TITLE_GOLD.getRed() + "," + TITLE_GOLD.getGreen() + "," + TITLE_GOLD.getBlue()
                + "); width: 26px; height: 26px; border-radius: 50%; text-align: center; line-height: 26px; font-weight: bold; font-size: 14pt; }"
                + "</style></head><body>"
                + html
                + "</body></html>";
    }

    private String buildHtmlContent() {
        String rulesHtml =
                "<h2>📜 Regole base del Tressette</h2>" +
                        "<p>Il <b>Tressette</b> è un classico gioco di carte italiano che si gioca con un mazzo da 40 carte. " +
                        "Scopri le regole fondamentali per iniziare a giocare!</p>" +

                        "<h3>🎯 Obiettivo del Gioco</h3>" +
                        "<p>L'obiettivo è raccogliere il maggior numero di punti possibile attraverso le carte vinte durante le mani. " +
                        "Il giocatore o la squadra con il punteggio più alto vince la partita.</p>" +

                        "<h3>🃏 Mazzo e Valori delle Carte</h3>" +
                        "<p>Si utilizza un mazzo da 40 carte italiano. Le carte hanno un ordine di priorità e valore specifico:</p>" +
                        "<ul>" +
                        "<li><b>Ordine di forza:</b> 3 (più forte), 2, Asso, Re, Cavallo, Fante, 7, 6, 5, 4 (più debole)</li>" +
                        "<li><b>Valori in punti:</b>" +
                        "  <ul>" +
                        "    <li>Asso = 1 punto</li>" +
                        "    <li>2 = 1 punto</li>" +
                        "    <li>3 = 1 punto</li>" +
                        "    <li>Re, Cavallo, Fante = 1/3 di punto ciascuno</li>" +
                        "    <li>Altre carte = 0 punti</li>" +
                        "  </ul>" +
                        "</li>" +
                        "</ul>" +

                        "<h3>🎮 Svolgimento della Partita</h3>" +
                        "<ol>" +
                        "<li><b>Distribuzione:</b> Vengono distribuite 10 carte a ciascun giocatore all'inizio della mano.</li>" +
                        "<li><b>Primo turno:</b> Il primo giocatore (o vincitore del turno precedente) gioca una carta. Il suo seme diventa il seme dominante del turno.</li>" +
                        "<li><b>Rispondere:</b> Gli altri giocatori devono rispondere con lo stesso seme, se possibile. Altrimenti, possono giocare qualsiasi altra carta.</li>" +
                        "<li><b>Vincere il turno:</b> Vince il turno la carta più alta del seme dominante secondo l'ordine di forza.</li>" +
                        "<li><b>Pesca:</b> Dopo ogni turno, i giocatori pescano una carta dal mazzo per riportare la mano a 10 carte.</li>" +
                        "</ol>" +

                        "<h3>📊 Calcolo del Punteggio</h3>" +
                        "<p>Al termine della mano, si contano i punti di tutte le carte raccolte da ogni giocatore o squadra:</p>" +
                        "<ul>" +
                        "<li>Si sommano i valori delle carte vinte</li>" +
                        "<li>Il punteggio viene arrotondato secondo le convenzioni del gioco</li>" +
                        "<li>Vince chi raggiunge per primo il punteggio stabilito (solitamente 21 punti)</li>" +
                        "</ul>" +

                        "<h3>💡 Consigli Strategici</h3>" +
                        "<ul>" +
                        "<li><b>Memoria:</b> Ricorda le carte giocate per anticipare le mosse degli avversari</li>" +
                        "<li><b>Gestione:</b> Conserva le carte forti per i momenti cruciali</li>" +
                        "<li><b>Attenzione:</b> Presta attenzione al seme dominante e pianifica le tue giocate</li>" +
                        "<li><b>Pratica:</b> L'esperienza è fondamentale per migliorare la propria strategia</li>" +
                        "</ul>" +

                        "<div class='note'><i>💭 Nota: Queste sono le regole base. Esistono diverse varianti regionali del Tressette con regole leggermente diverse.</i></div>";
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
    }
}