package menu;

import controller.ProfileController;
import controller.ProfileListener;
import profile.UserProfile;


import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.*;

/**
 * HomeMenu è ora View e implementa ProfileListener per aggiornarsi quando il modello cambia.
 * Riceve ProfileController nel costruttore (per registrarsi come listener).
 *
 * Modifiche principali:
 * - scaling "cover" per lo sfondo con caching (scaledBackground)
 * - ridimensionamento in background via SwingWorker
 * - overlay semitrasparente per migliorare leggibilità del testo
 * - caricamento risorsa con diversi fallback di percorso
 */
public class HomeMenu extends JPanel implements ProfileListener {

    private static final Logger LOGGER = Logger.getLogger(HomeMenu.class.getName());

    final int originalTileSize = 16, scale = 3;
    public final int tileSize = originalTileSize * scale;
    public final int maxScreenCol = 17, maxScreenRow = 13;
    public final int screenWidth = tileSize * maxScreenCol, screenHeight = tileSize * maxScreenRow;

    // immagine originale caricata dalle risorse
    BufferedImage background;

    // immagine scalata in cache (per la dimensione corrente)
    private volatile Image scaledBackground;
    private volatile Dimension lastSize;
    private SwingWorker<Image, Void> scaleWorker;

    // overlay per migliorare leggibilità (alpha 0..255)
    private Color overlay = new Color(0, 0, 0, 80);

    public MenuOption[] options;
    public controller.Cursor cursor;

    private int selectedOption = 0; // 0 = nessuna selezione, 1..4 = opzioni

    // --- campi per avatar/nome in alto a destra
    private final JLabel avatarSmallLabel;
    private final JLabel nameSmallLabel;
    private final ProfileController controller;
    private Runnable onProfileClick; // callback per aprire profilo (MenuFrame imposta)

    public HomeMenu(ProfileController controller) {
        this.controller = controller;

        loadBackground(); // carica "background" originale (BufferedImage)
        // avvia il primo ridimensionamento (se possibile)
        // il listener componentResized si occuperà di futuri cambi di dimensione

        options = new MenuOption[]{
                new MenuOption("Gioca", 250),
                new MenuOption("Regole", 320),
                new MenuOption("Profilo", 390),
                new MenuOption("Impostazioni", 460),
                new MenuOption("Esci", 520)
        };

        cursor = new controller.Cursor(this);

        // usa BorderLayout così possiamo aggiungere un pannello in alto a destra
        setLayout(new BorderLayout());

        // pannello top trasparente per contenere avatar e nome a destra
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 6));
        topPanel.setOpaque(false); // lascia vedere lo sfondo disegnato in paintComponent

        avatarSmallLabel = new JLabel();
        avatarSmallLabel.setPreferredSize(new Dimension(48, 48));
        avatarSmallLabel.setHorizontalAlignment(SwingConstants.CENTER);

        nameSmallLabel = new JLabel();
        nameSmallLabel.setForeground(Color.WHITE);
        nameSmallLabel.setFont(new Font("Serif", Font.BOLD, 16));
        nameSmallLabel.setVerticalAlignment(SwingConstants.CENTER);

        // area cliccabile per aprire il profilo
        JPanel clickable = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        clickable.setOpaque(false);
        clickable.add(nameSmallLabel);
        clickable.add(avatarSmallLabel);
        clickable.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                if (onProfileClick != null) onProfileClick.run();
            }
        });

        topPanel.add(clickable);
        add(topPanel, BorderLayout.NORTH);

        setPreferredSize(new Dimension(screenWidth, screenHeight));
        setBackground(Color.BLACK);
        setFocusable(true);
        setDoubleBuffered(true);

        // Listener per ridimensionamento: riscaliamo l'immagine in background
        addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent e) {
                requestRescaleBackground();
            }
        });

        // Mouse movement
        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                for (int i = 0; i < options.length; i++) {
                    int top = options[i].y - 30;
                    int bottom = options[i].y;
                    if (e.getY() >= top && e.getY() <= bottom) {
                        cursor.setSelectedIndex(i);
                    }
                }
            }
        });

        // Mouse click
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                selectedOption = cursor.getSelectedIndex() + 1;
            }
        });

        // Swing Timer per aggiornare il pannello (~60 FPS)
        Timer timer = new Timer(16, e -> repaint());
        timer.start();

        // registrazione al controller come listener (observer)
        if (this.controller != null) {
            this.controller.addListener(this);
            // inizializza con i dati correnti
            UserProfile p = this.controller.getProfile();
            if (p != null) onProfileUpdated(p);
        }

        // se abbiamo già una dimensione valida proviamo a ridimensionare subito
        SwingUtilities.invokeLater(this::requestRescaleBackground);
    }

    public int getSelectedOption() {
        return selectedOption;
    }

    /**
     * Imposta la callback eseguita quando si clicca l'area avatar/nome.
     */
    public void setOnProfileClick(Runnable r) {
        this.onProfileClick = r;
    }

    @Override
    public void onProfileUpdated(UserProfile profile) {
        // aggiornamento UI da EDT
        SwingUtilities.invokeLater(() -> {
            String n = profile.getName() == null || profile.getName().isBlank() ? "Giocatore" : profile.getName();
            nameSmallLabel.setText(n);

            // avatar: prima prova il path dell'utente, altrimenti carica risorsa di default
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
                Image scaled = icon.getScaledInstance(48, 48, Image.SCALE_SMOOTH);
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
        // opzionale: mostrare dialog o loggare
        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(this, "Salvataggio profilo fallito: " + ex.getMessage(), "Errore", JOptionPane.ERROR_MESSAGE);
        });
    }

    /**
     * Carica l'immagine di sfondo (prova più percorsi per maggiore tolleranza)
     */
    private void loadBackground() {
        String[] resourceCandidates = {
                "/res/default_images/sfondo_1.jpg",
        };

        // prova caricamento da classpath
        for (String p : resourceCandidates) {
            try (InputStream is = getClass().getResourceAsStream(p)) {
                if (is != null) {
                    background = ImageIO.read(is);
                    LOGGER.info("Background caricato da resource: " + p);
                    scaledBackground = null;
                    lastSize = null;
                    return;
                }
            } catch (IOException e) {
                LOGGER.log(Level.WARNING, "Errore caricamento resource " + p, e);
            }
        }

        // fallback: prova a leggere dal file system relativo al progetto (utile in dev)
        File fs = new File("res/default_images/sfondo_1.jpg");
        if (fs.exists()) {
            try {
                background = ImageIO.read(fs);
                LOGGER.info("Background caricato da file system: res/images/sfondo_1.jpg");
                scaledBackground = null;
                lastSize = null;
                return;
            } catch (IOException e) {
                LOGGER.log(Level.WARNING, "Errore caricamento sfondo da file system", e);
            }
        }

        // se non trovato
        LOGGER.severe("Immagine di sfondo non trovata. Percorsi provati: res/default_images/sfondo_1.jpg");
        background = null;
    }

    /**
     * Richiesta di riscalare l'immagine di background (esegue il lavoro in background)
     */
    private void requestRescaleBackground() {
        if (background == null) return;

        final Dimension size = getSize();
        if (size.width <= 0 || size.height <= 0) return;

        // se la cache è già valida non fare nulla
        if (lastSize != null && lastSize.equals(size) && scaledBackground != null) return;

        // cancella eventuale worker precedente
        if (scaleWorker != null && !scaleWorker.isDone()) {
            scaleWorker.cancel(true);
        }

        final BufferedImage orig = background;
        scaleWorker = new SwingWorker<Image, Void>() {
            @Override
            protected Image doInBackground() {
                int w = orig.getWidth();
                int h = orig.getHeight();

                double sx = (double) size.width / w;
                double sy = (double) size.height / h;
                // COVER: riempi tutta l'area e ritaglia eccessi
                double scale = Math.max(sx, sy);

                int newW = Math.max(1, (int) Math.round(w * scale));
                int newH = Math.max(1, (int) Math.round(h * scale));

                BufferedImage out = new BufferedImage(newW, newH, BufferedImage.TYPE_INT_ARGB);
                Graphics2D g2 = out.createGraphics();
                try {
                    g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
                    g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.drawImage(orig, 0, 0, newW, newH, null);
                } finally {
                    g2.dispose();
                }
                return out;
            }

            @Override
            protected void done() {
                try {
                    if (!isCancelled()) {
                        Image img = get();
                        scaledBackground = img;
                        lastSize = new Dimension(size);
                        repaint();
                    }
                } catch (Exception ex) {
                    LOGGER.log(Level.WARNING, "Errore nel ridimensionamento dello sfondo", ex);
                }
            }
        };
        scaleWorker.execute();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // disegna lo sfondo scalato centrato (modalità cover)
        if (scaledBackground != null) {
            int sw = scaledBackground.getWidth(null);
            int sh = scaledBackground.getHeight(null);
            int x = (getWidth() - sw) / 2;
            int y = (getHeight() - sh) / 2;
            Graphics2D g2 = (Graphics2D) g.create();
            try {
                g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                g2.drawImage(scaledBackground, x, y, this);
                // overlay semitrasparente per migliorare leggibilità del testo
                if (overlay != null && overlay.getAlpha() > 0) {
                    g2.setColor(overlay);
                    g2.fillRect(0, 0, getWidth(), getHeight());
                }
            } finally {
                g2.dispose();
            }
        } else if (background != null) {
            // fallback: disegna l'originale ridimensionato "sul momento" (meglio evitare per performance)
            int w = background.getWidth();
            int h = background.getHeight();
            double sx = (double) getWidth() / w;
            double sy = (double) getHeight() / h;
            double scale = Math.max(sx, sy);
            int nw = (int) Math.round(w * scale);
            int nh = (int) Math.round(h * scale);
            int x = (getWidth() - nw) / 2;
            int y = (getHeight() - nh) / 2;
            Graphics2D g2 = (Graphics2D) g.create();
            try {
                g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                g2.drawImage(background, x, y, nw, nh, this);
                if (overlay != null && overlay.getAlpha() > 0) {
                    g2.setColor(overlay);
                    g2.fillRect(0, 0, getWidth(), getHeight());
                }
            } finally {
                g2.dispose();
            }
        } else {
            // nessuna immagine: sfondo normale
            // (super.paintComponent ha già riempito con setBackground color)
        }

        // Disegna le opzioni e il cursore sopra lo sfondo
        for (int i = 0; i < options.length; i++) {
            options[i].draw(g, cursor.getSelectedIndex() == i);
        }

        cursor.draw(g);
    }

    /**
     * Chiamare al termine dell'app per cancellare eventuali worker
     */
    public void dispose() {
        if (scaleWorker != null && !scaleWorker.isDone()) scaleWorker.cancel(true);
    }
}