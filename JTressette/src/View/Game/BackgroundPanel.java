package View.Game;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * JPanel con supporto a background image in modalità "cover" (riempi tutta l'area)
 * - carica l'immagine da classpath (getResourceAsStream("/images/..."))
 * - scala e cache l'immagine ridimensionata
 * - esegue il ridimensionamento in un thread dedicato per non bloccare la UI
 * - applica RenderingHints per qualità
 * - supporta overlay semitrasparente (per migliorare la leggibilità dei componenti sopra)
 */
public class BackgroundPanel extends JPanel {

    private BufferedImage original;
    private volatile Image scaledImage;        // immagine scalata cached
    private volatile Dimension lastSize;       // dimensione per cui scaledImage è valida
    private final ExecutorService scaler = Executors.newSingleThreadExecutor();
    private boolean coverMode = true;          // true = cover (crop), false = contain (letterbox)
    private Color overlay = new Color(0, 0, 0, 0); // overlay trasparente di default

    public BackgroundPanel() {
        super(new BorderLayout());
        // rileva resize per eventualmente scatenare il ridimensionamento asincrono
        addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent e) {
                requestRescale();
            }
        });
        setOpaque(true);
    }

    /**
     * Carica l'immagine dal classpath. Esempio path: "/images/sfondo_1.jpg"
     */
    public void setBackgroundImageFromResource(String resourcePath) {
        try (InputStream is = getClass().getResourceAsStream(resourcePath)) {
            if (is == null) {
                throw new IOException("Risorsa non trovata: " + resourcePath);
            }
            original = ImageIO.read(is);
            // invalida cache e richiedi rescale
            scaledImage = null;
            lastSize = null;
            requestRescale();
            repaint();
        } catch (IOException e) {
            e.printStackTrace();
            original = null;
            scaledImage = null;
        }
    }

    /**
     * Opzionale: permetti di passare un URL (se carichi altrove)
     */
    public void setBackgroundImage(URL url) throws IOException {
        original = ImageIO.read(url);
        scaledImage = null;
        lastSize = null;
        requestRescale();
        repaint();
    }

    public void setCoverMode(boolean cover) {
        this.coverMode = cover;
        // invalida e ridimensiona
        scaledImage = null;
        requestRescale();
        repaint();
    }

    public void setOverlay(Color overlay) {
        this.overlay = overlay;
        repaint();
    }

    private void requestRescale() {
        if (original == null) return;
        final Dimension size = getSize();
        // se dimensione zero, ignora
        if (size.width <= 0 || size.height <= 0) return;

        // se la dimensione è già quella della cache non serve rescalare
        if (lastSize != null && lastSize.equals(size) && scaledImage != null) return;

        // esegui rescale in background
        scaler.submit(() -> {
            Image img = getScaledInstanceToCover(original, size.width, size.height, coverMode);
            scaledImage = img;
            lastSize = new Dimension(size);
            // repaint su EDT
            SwingUtilities.invokeLater(this::repaint);
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // 1) disegna background scalato (se pronto) o un placeholder
        Graphics2D g2 = (Graphics2D) g.create();
        try {
            // alta qualità di rendering
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            if (scaledImage != null) {
                int x = (getWidth() - scaledImage.getWidth(null)) / 2;
                int y = (getHeight() - scaledImage.getHeight(null)) / 2;
                g2.drawImage(scaledImage, x, y, this);
            } else if (original != null) {
                // se scaledImage non è pronta disegna l'originale come fallback (potrebbe sembrare sgranata)
                // posizionato al centro e ridotto se troppo grande
                Image fallback = original;
                int fw = fallback.getWidth(null);
                int fh = fallback.getHeight(null);
                double sx = (double) getWidth() / fw;
                double sy = (double) getHeight() / fh;
                double s = coverMode ? Math.max(sx, sy) : Math.min(sx, sy);
                int nw = (int) Math.round(fw * s);
                int nh = (int) Math.round(fh * s);
                int x = (getWidth() - nw) / 2;
                int y = (getHeight() - nh) / 2;
                g2.drawImage(fallback, x, y, nw, nh, this);
            } else {
                // nessuna immagine: riempi con colore neutro
                g2.setColor(getBackground());
                g2.fillRect(0, 0, getWidth(), getHeight());
            }

            // 2) overlay semitrasparente per migliorare leggibilità della UI
            if (overlay != null && overlay.getAlpha() > 0) {
                g2.setColor(overlay);
                g2.fillRect(0, 0, getWidth(), getHeight());
            }
        } finally {
            g2.dispose();
        }
    }

    /**
     * Ridimensiona l'immagine originale alla dimensione target. Se cover=true usa cover mode
     * (riempi tutta la area e ritaglia eventuali parti), se false usa contain.
     * Usa un BufferedImage e Graphics2D con RenderingHints per qualità.
     */
    private static Image getScaledInstanceToCover(BufferedImage original, int targetW, int targetH, boolean cover) {
        if (original == null || targetW <= 0 || targetH <= 0) return null;

        int w = original.getWidth();
        int h = original.getHeight();
        double sx = (double) targetW / w;
        double sy = (double) targetH / h;
        double scale = cover ? Math.max(sx, sy) : Math.min(sx, sy);

        int newW = Math.max(1, (int) Math.round(w * scale));
        int newH = Math.max(1, (int) Math.round(h * scale));

        BufferedImage out = new BufferedImage(newW, newH, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = out.createGraphics();
        try {
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.drawImage(original, 0, 0, newW, newH, null);
        } finally {
            g2.dispose();
        }
        return out;
    }

    /**
     * Be sure to call when closing the application to stop background thread.
     */
    public void dispose() {
        scaler.shutdownNow();
    }
}