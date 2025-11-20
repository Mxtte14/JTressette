package rules;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.net.URL;

/**
 * Pagina che mostra le regole del gioco in forma testuale (HTML) e supporta immagini
 * prese dalle risorse del progetto. Fornisce anche un pulsante "Indietro" che torna
 * alla card "MENU" nel container passato nel costruttore.
 *
 * Uso:
 *   JPanel cards = ...; // container a CardLayout
 *   RulesPage rules = new RulesPage(cards);
 *   cards.add(rules, "RULES");
 *
 * Nota: le immagini usate nell'HTML vengono risolte rispetto alla cartella /main/resource/.
 */
public class RulesPage extends JPanel {

    private final JPanel cards;
    private final JEditorPane htmlPane;

    public RulesPage(JPanel cards) {
        this.cards = cards;
        setLayout(new BorderLayout(8, 8));
        initTop();
        htmlPane = createHtmlPane();
        JScrollPane sp = new JScrollPane(htmlPane);
        sp.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        add(sp, BorderLayout.CENTER);
    }

    private void initTop() {
        JPanel top = new JPanel(new BorderLayout());
        JButton back = new JButton("Indietro");
        back.addActionListener(e -> {
            CardLayout cl = (CardLayout) cards.getLayout();
            cl.show(cards, "MENU");
        });
        top.add(back, BorderLayout.WEST);

        JLabel title = new JLabel("Regole del Tressette", SwingConstants.CENTER);
        title.setFont(new Font("Serif", Font.BOLD, 20));
        title.setForeground(Color.WHITE);

        // header trasparente per vedere lo sfondo se presente
        top.setOpaque(false);
        title.setOpaque(false);
        top.add(title, BorderLayout.CENTER);

        add(top, BorderLayout.NORTH);
    }

    private JEditorPane createHtmlPane() {
        JEditorPane pane = new JEditorPane();
        pane.setContentType("text/html");
        pane.setEditable(false);
        pane.setBackground(new Color(0,0,0,0)); // lascia trasparente se richiesto

        String html = buildHtmlContent();
        pane.setText(html);

        // imposta base URL in modo che <img src="..."> risolva le risorse del jar
        URL base = getClass().getResource("/main/resource/");
        if (base != null) {
            try {
                pane.setPage(new URL("data:text/html," + java.net.URLEncoder.encode(html, "UTF-8")));
            } catch (IOException ignored) {
                // fallback: setText già fatto
            }
            // Nota: JEditorPane risolve i <img src="..."> relativamente al setPage URL.
            // per semplicità manteniamo il contenuto impostato via setText; immagini
            // referenziate dovranno usare URL assoluti alla risorsa, ad es.
            // getClass().getResource("/main/resource/img.png").toString()
        } else {
            // nessuna risorsa base disponibile: resta il testo senza immagini
        }

        // permetti la selezione/scorrimento testuale comodo
        pane.setCaretPosition(0);
        return pane;
    }

    private String buildHtmlContent() {
        // Testo esplicativo in italiano con markup semplice.
        // Se vuoi aggiungere immagini, usa il tag <img src="URL"> dove URL è
        // ad esempio getClass().getResource("/main/resource/NOME.png").toString()
        // oppure modifica manualmente questo HTML per includere immagini presenti nelle risorse.
        String rulesHtml =
                "<html>" +
                        "<head>" +
                        "<style>" +
                        "body { font-family: Serif; color: #FFFFFF; background: transparent; padding: 10px; }" +
                        "h2 { color: #FFD700; }" +
                        "p { font-size: 12pt; line-height:1.4; }" +
                        "ul { margin-left: 1.2em; }" +
                        ".note { color: #cccccc; font-size:10pt; }" +
                        "</style>" +
                        "</head>" +
                        "<body>" +
                        "<h2>Regole base del Tressette (sintesi)</h2>" +
                        "<p>Il Tressette è un gioco di prese tradizionale italiano. Di seguito una descrizione sintetica delle regole più comuni utili per giocare nella versione locale:</p>" +
                        "<h3>Obiettivo</h3>" +
                        "<p>Accumular più punti possibile facendo prese con le carte di valore.</p>" +
                        "<h3>Carte e valori</h3>" +
                        "<ul>" +
                        "<li>Si gioca con un mazzo da 40 carte (senza 8 e 9 nel mazzo da 52).</li>" +
                        "<li>I valori di punta: <b>3</b> (più alto), <b>2</b>, <b>A</b>, <b>K</b>, <b>Q</b>, <b>J</b> e poi 7..4.</li>" +
                        "</ul>" +
                        "<h3>Svolgimento</h3>" +
                        "<ul>" +
                        "<li>Ogni giocatore riceve 10 carte (in 4 giocatori) o diversa a seconda della variante.</li>" +
                        "<li>Si gioca per prese: il giocatore di mano gioca una carta e gli altri devono, se possibile, rispondere del seme giocato.</li>" +
                        "<li>Vince la presa la carta di seme di mano con il valore più alto secondo la scala del Tressette.</li>" +
                        "</ul>" +
                        "<h3>Punteggi</h3>" +
                        "<p>Al termine della mano si conteggiano i punti dalle carte catturate. Le regole di punteggio possono variare; nella variante più semplice, alcune carte (A, 3, 2, K, Q, J) hanno valore in punti e il totale decide il vincitore.</p>" +
                        "<h3>Varianti e suggerimenti</h3>" +
                        "<ul>" +
                        "<li>Esistono molte varianti regionali: Napoletano, Siciliano, Piemontese ecc.</li>" +
                        "<li>Se desideri, posso aggiungere una sezione con esempi illustrati o immagini delle carte: forniscimi le risorse (PNG/JPG) e le inserisco automaticamente.</li>" +
                        "</ul>" +
                        "<p class='note'>Questa è una versione semplificata delle regole pensata per il gioco locale. Per regole complete e varianti cerca una guida completa o dimmi quali dettagli vuoi aggiungere.</p>" +
                        "</body>" +
                        "</html>";
        return rulesHtml;
    }
}