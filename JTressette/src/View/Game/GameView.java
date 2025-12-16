package View.Game;

import Controller.Game.GameController;
import Model.Game.*;
import Model.Impostazioni.MenuImpostazioni;
import Model.Util.CardImageLoader;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Vista grafica principale del gioco di Tressette implementata con Swing.
 * Segue il pattern architetturale MVC come componente View.
 *
 * <p>Caratteristiche principali:</p>
 * <ul>
 *   <li>Tavolo da gioco stilizzato come feltro da poker verde</li>
 *   <li>Visualizzazione carte del giocatore umano (fronte) in basso</li>
 *   <li>Visualizzazione carte avversari (retro) sui lati e in alto</li>
 *   <li>Area centrale per carte giocate nella presa corrente</li>
 *   <li>Pannello laterale con log, punteggi e statistiche</li>
 *   <li>Animazioni per giocate, prese e pescate di carte</li>
 *   <li>Effetti audio coordinati con le azioni di gioco</li>
 *   <li>Effetto fade-in/fade-out per transizioni</li>
 * </ul>
 *
 * <p>Layout responsive con adattamento dinamico:</p>
 * <ul>
 *   <li>Dimensione carte proporzionale alla finestra</li>
 *   <li>Disposizione verticale per avversari laterali</li>
 *   <li>Scrolling orizzontale per mano del giocatore se necessario</li>
 *   <li>Tavolo ovale con dimensioni adattive</li>
 * </ul>
 *
 * <p>Le immagini delle carte vengono caricate da risorse tramite CardImageLoader.
 * La vista si aggiorna automaticamente quando lo stato del gioco cambia.</p>
 *
 * @author JTressette Team
 * @version 1.0
 */
public class GameView extends JFrame {

    // Palette colori ispirata al feltro da poker
    /** Colore verde feltro principale */
    private static final Color FELT_GREEN = new Color(26, 117, 65);
    
    /** Colore verde feltro scuro per ombre e gradienti */
    private static final Color FELT_DARK = new Color(18, 85, 47);
    
    /** Colore bordo marrone legno */
    private static final Color FELT_BORDER = new Color(100, 70, 40);
    
    /** Colore oro per testo importante */
    private static final Color TEXT_GOLD = new Color(255, 215, 0);
    
    /** Colore bianco per testo normale */
    private static final Color TEXT_WHITE = Color.WHITE;

    // Dimensioni carte con adattamento dinamico
    /** Larghezza base carta */
    private static final int CARD_WIDTH = 60;
    
    /** Altezza base carta */
    private static final int CARD_HEIGHT = 86;
    
    /** Spazio tra carte nella mano */
    private static final int HAND_GAP = 5;
    
    /** Larghezza minima carte mano giocatore */
    private static final int HAND_CARD_MIN_WIDTH = 45;
    
    /** Larghezza massima carte mano giocatore */
    private static final int HAND_CARD_MAX_WIDTH = 85;
    
    /** Larghezza corrente carte mano giocatore (adattiva) */
    private int handCardWidth = 60;
    
    /** Altezza corrente carte mano giocatore (calcolata proporzionalmente) */
    private int handCardHeight = (int) Math.round(handCardWidth * ((double) CARD_HEIGHT / CARD_WIDTH));

    /** Larghezza carte laterali bot (proporzionale a handCardWidth) */
    private int sideCardWidth = (int) (handCardWidth * 0.78);
    
    /** Altezza carte laterali bot (calcolata proporzionalmente) */
    private int sideCardHeight = (int) Math.round(sideCardWidth * ((double) CARD_HEIGHT / CARD_WIDTH));

    // Vincoli dimensionali tavolo ovale
    /** Larghezza minima tavolo */
    private static final int TABLE_MIN_W = 540;
    
    /** Altezza minima tavolo */
    private static final int TABLE_MIN_H = 240;
    
    /** Larghezza massima tavolo */
    private static final int TABLE_MAX_W = 650;
    
    /** Altezza massima tavolo */
    private static final int TABLE_MAX_H = 280;


    /** Stato corrente della partita */
    private final GameState gameState;
    
    /** Riferimento al giocatore umano */
    private final GiocatoreUmano humanPlayer;
    
    /** Controller di gioco per gestire le azioni */
    private final GameController controller;

    // Componenti UI che necessitano di aggiornamenti frequenti
    /** Pannello mano giocatore */
    private JPanel playerHandPanel;
    
    /** Scroll pane per mano giocatore */
    private JScrollPane handScrollPane;
    
    /** Area avversari in alto */
    private JPanel opponentArea;
    
    /** Pannello bot lato sinistro */
    private JPanel leftBotPanel;
    
    /** Pannello bot lato destro */
    private JPanel rightBotPanel;
    
    /** Label stato partita */
    private JLabel statusLabel;
    
    /** Label punteggio */
    private JLabel scoreLabel;
    
    /** Area log messaggi */
    private JPanel logArea;
    
    /** Pulsante gioca (non utilizzato attualmente) */
    private JButton playButton;
    
    /** Label contatore carte vinte */
    private JLabel wonCardsLabel;
    
    /** Pannello tavolo ovale centrale */
    private JPanel tableOval;

    /** Layered pane per gestire sovrapposizioni carte */
    private JLayeredPane layeredPaneRef;
    
    /** Gestore audio per suoni ed effetti */
    private final Model.Audio.AudioManager audioManager;

    /** Lista pannelli carte nella mano del giocatore */
    private final List<CardPanel> cardPanels = new ArrayList<>();
    private final List<Giocatore> players;

    // gap used for hand cards (calculated)
    private int handCardGapUsed = HAND_GAP;
    private final int dealDelayMs = 800;
    
    // Flag per impedire l'aggiornamento del tavolo durante le animazioni delle carte
    private final AtomicBoolean cardAnimationInProgress = new AtomicBoolean(false);

    public GameView(GameState gameState, GiocatoreUmano humanPlayer, GameController controller, Model.Audio.AudioManager audioManager) {
        super("JTressette - Partita in Corso");
        
        // Validate inputs
        if (gameState == null) {
            throw new IllegalArgumentException("GameState cannot be null");
        }
        if (humanPlayer == null) {
            throw new IllegalArgumentException("HumanPlayer cannot be null");
        }
        if (controller == null) {
            throw new IllegalArgumentException("Controller cannot be null");
        }
        if (audioManager == null) {
            throw new IllegalArgumentException("AudioManager cannot be null");
        }
        
        this.gameState = gameState;
        this.humanPlayer = humanPlayer;
        this.controller = controller;
        this.players = gameState.getPlayers();
        this.audioManager = audioManager;
        
        // Validate players list
        if (this.players == null || this.players.isEmpty()) {
            throw new IllegalStateException("Players list cannot be null or empty");
        }
        
        // Preload card images
        CardImageLoader.preloadImages();
        initUI();
    }

    private void initUI() {
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setSize(1100, 750);
        setResizable(true); // allow resize for testing; layout adapts
        setLocationRelativeTo(null);

        // Make frame undecorated to allow opacity changes
        setUndecorated(true);

        // Start with opacity 0 for fade-in effect
        setOpacity(0.0f);

        // Main panel with gradient background
        JPanel mainPanel = new GradientPanel();
        mainPanel.setLayout(new BorderLayout(10, 10));

        // Top: Opponent area (face-down cards) with exit button overlay
        JPanel topContainer = new JPanel(new BorderLayout());
        topContainer.setOpaque(false);
        opponentArea = createOpponentArea();
        topContainer.add(opponentArea, BorderLayout.CENTER);

        // Add exit button in top-right corner
        JPanel exitButtonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 10));
        exitButtonPanel.setOpaque(false);
        JButton exitButton = createExitButton();
        exitButtonPanel.add(exitButton);
        topContainer.add(exitButtonPanel, BorderLayout.NORTH);

        mainPanel.add(topContainer, BorderLayout.NORTH);

        // Center: Container for table and side bot hands
        JPanel centerContainer = new JPanel(new BorderLayout(5, 5));
        centerContainer.setOpaque(false);

        // Create left and right panels for side bot hands
        leftBotPanel = createSideBotPanel();
        rightBotPanel = createSideBotPanel();

        // Center: Table with played cards
        JPanel tableCenter = createTableCenter();

        centerContainer.add(leftBotPanel, BorderLayout.WEST);
        centerContainer.add(tableCenter, BorderLayout.CENTER);
        centerContainer.add(rightBotPanel, BorderLayout.EAST);

        mainPanel.add(centerContainer, BorderLayout.CENTER);

        // Bottom: Player's hand
        JPanel bottomArea = createPlayerArea();
        mainPanel.add(bottomArea, BorderLayout.SOUTH);

        // Right: Log/info panel
        JPanel rightPanel = createInfoPanel();
        mainPanel.add(rightPanel, BorderLayout.EAST);

        setContentPane(mainPanel);

        // store layered pane reference to add player's cards directly on it
        layeredPaneRef = getLayeredPane();

        // Add mouse listeners to make undecorated window draggable
        final Point[] initialClick = {null};
        mainPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                initialClick[0] = e.getPoint();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                initialClick[0] = null;
            }
        });
        mainPanel.addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (initialClick[0] != null) {
                    // Get location of window
                    int thisX = getLocation().x;
                    int thisY = getLocation().y;

                    // Determine how much the mouse moved since initial click
                    int xMoved = e.getX() - initialClick[0].x;
                    int yMoved = e.getY() - initialClick[0].y;

                    // Move window to new position
                    int X = thisX + xMoved;
                    int Y = thisY + yMoved;
                    setLocation(X, Y);
                }
            }
        });

        // Resize listener to recompute sizes
        this.addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent e) {
                recomputeLayoutSizes();
                refresh();
            }
        });

        // initial sizing
        recomputeLayoutSizes();

        // Initial refresh
        refresh();
    }

    private void recomputeLayoutSizes() {
        int contentW = getContentPane().getWidth();
        int contentH = getContentPane().getHeight();

        MenuImpostazioni settings = MenuImpostazioni.getInstance();
        boolean infoPanelVisible = settings.isShowScore() || settings.isShowMessages();
        int rightPanelW = infoPanelVisible ? 220 : 20;

        int availableW = Math.max(400, contentW - rightPanelW - 60);
        int availableH = Math.max(300, contentH - 220);

        int tableW = Math.max(TABLE_MIN_W, Math.min(TABLE_MAX_W, (int) (availableW * 0.85)));
        int tableH = Math.max(TABLE_MIN_H, Math.min(TABLE_MAX_H, (int) (availableH * 0.68)));

        if (tableOval != null) {
            tableOval.setPreferredSize(new Dimension(tableW, tableH));
            tableOval.revalidate();
        }

        recomputeHandCardSize();
    }

    private void recomputeHandCardSize() {
        int contentW = getContentPane().getWidth();

        MenuImpostazioni settings = MenuImpostazioni.getInstance();
        boolean infoPanelVisible = settings.isShowScore() || settings.isShowMessages();
        int rightPanelW = infoPanelVisible ? 220 : 20;

        int effectiveWidth = Math.max(320, contentW - rightPanelW - 60);

        int handCount = Math.max(1, gameState.getHand(humanPlayer).size());

        // Bounds for card width
        final int maxW = HAND_CARD_MAX_WIDTH;
        final int minW = HAND_CARD_MIN_WIDTH;

        // Prefer this gap when there's plenty of space
        final int preferredGap = HAND_GAP;

        // Allow overlap up to this fraction of card width (e.g. 0.6 => up to 60% overlap)
        final double maxOverlapFraction = 0.6;
        final int maxNegativeGap = -(int) Math.round(maxW * maxOverlapFraction);

        // First try to use the max width and preferred gap
        int requiredForMax = handCount * maxW + Math.max(0, (handCount - 1) * preferredGap);
        if (requiredForMax <= effectiveWidth) {
            handCardWidth = maxW;
            handCardGapUsed = preferredGap;
        } else {
            // Compute a candidate width using preferredGap (no overlap beyond preferredGap)
            int candidate = (effectiveWidth - Math.max(0, (handCount - 1) * preferredGap)) / handCount;
            if (candidate >= minW) {
                handCardWidth = Math.min(maxW, candidate);
                handCardGapUsed = preferredGap;
            } else {
                // Not enough space even with min width: allow overlap (negative gap)
                handCardWidth = minW;
                // compute gap (may be negative); clamp to maxNegativeGap
                int gap = (handCount > 1) ? (effectiveWidth - handCount * handCardWidth) / (handCount - 1) : preferredGap;
                if (gap < maxNegativeGap) gap = maxNegativeGap;
                handCardGapUsed = gap;
            }
        }

        // Recompute height preserving aspect ratio
        double ratio = (double) CARD_HEIGHT / (double) CARD_WIDTH;
        handCardHeight = (int) Math.round(handCardWidth * ratio);

        // Side (bot) cards slightly smaller than player's hand cards
        sideCardWidth = Math.max(36, (int) Math.round(handCardWidth * 0.78));
        sideCardHeight = (int) Math.round(sideCardWidth * ratio);

        // Ensure scroll pane sized accordingly (if still used visually)
        if (playerHandPanel != null && handScrollPane != null) {
            handScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
            handScrollPane.setPreferredSize(new Dimension(effectiveWidth, handCardHeight + 30));
            playerHandPanel.revalidate();
        }
    }

    // Custom panel with gradient background
    private static class GradientPanel extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            GradientPaint gp = new GradientPaint(0, 0, FELT_DARK, 0, (float) getHeight() / 2, FELT_GREEN);
            g2d.setPaint(gp);
            g2d.fillRect(0, 0, getWidth(), getHeight() / 2);
            GradientPaint gp2 = new GradientPaint(0, (float) getHeight() / 2, FELT_GREEN, 0, getHeight(), FELT_DARK);
            g2d.setPaint(gp2);
            g2d.fillRect(0, getHeight() / 2, getWidth(), getHeight() / 2);
        }
    }

    private JButton createExitButton() {
        JButton exitButton = new JButton("✕ Abbandona Partita");
        exitButton.setBackground(new Color(180, 40, 40));
        exitButton.setForeground(Color.WHITE);
        exitButton.setFont(new Font("Arial", Font.BOLD, 13));
        exitButton.setFocusPainted(false);
        exitButton.setBorderPainted(false);
        exitButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        exitButton.setPreferredSize(new Dimension(180, 35));

        // Add hover effect
        exitButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                exitButton.setBackground(new Color(200, 50, 50));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                exitButton.setBackground(new Color(180, 40, 40));
            }
        });

        exitButton.addActionListener(e -> controller.onExitGame());
        return exitButton;
    }

    // Keep createSideBotPanel simple - we'll insert specialized vertical stacks inside createOpponentBox when needed
    private JPanel createSideBotPanel() {
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        // width will be adjusted dynamically in updateOpponentArea based on sideCardWidth
        panel.setPreferredSize(new Dimension(sideCardWidth + 24, 0));
        return panel;
    }

    private JPanel createOpponentArea() {
        JPanel area = new JPanel();
        area.setOpaque(false);
        area.setLayout(null); // Use null layout for precise positioning
        area.setBorder(new EmptyBorder(20, 20, 10, 20));
        area.setPreferredSize(new Dimension(0, 180)); // Aumentata da 150 a 180
        return area;
    }

    // Sostituisci il metodo createOpponentBox con questa versione aggiornata:

    /**
     * Create opponent box.
     * - isVertical==true: vertical stack (per bot laterali)
     * - isVertical==false: horizontal layout (per bot in alto)
     */
    private JPanel createOpponentBox(Giocatore player, boolean isVertical) {
        JPanel box = new JPanel();
        box.setOpaque(false);
        box.setLayout(new BoxLayout(box, isVertical ? BoxLayout.Y_AXIS : BoxLayout.X_AXIS));

        JLabel nameLabel = new JLabel(player.getName());
        nameLabel.setForeground(new Color(255, 215, 0));
        nameLabel.setFont(new Font("Georgia", Font.BOLD, 14));
        nameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        nameLabel.setAlignmentY(Component.CENTER_ALIGNMENT);

        List<Cards> hand = gameState.getHand(player);
        int handSize = hand.size();

        if (isVertical) {
            // BOT LATERALI - verticale
            int overlap = Math.max(8, sideCardHeight / 3);
            VerticalCardStackPanel vstack = new VerticalCardStackPanel(sideCardWidth, sideCardHeight, overlap);
            vstack.setOpaque(false);
            vstack.setAlignmentX(Component.CENTER_ALIGNMENT);

            Image cardBackImg = CardImageLoader.getScaledCardBackImage(sideCardWidth, sideCardHeight);
            for (int i = 0; i < handSize; i++) {
                JLabel cardBack = new JLabel(new ImageIcon(cardBackImg));
                cardBack.setOpaque(false);
                vstack.add(cardBack);
            }

            if (handSize == 0) {
                JPanel placeholder = new JPanel();
                placeholder.setOpaque(false);
                placeholder.setPreferredSize(new Dimension(sideCardWidth, sideCardHeight));
                vstack.add(placeholder);
            }

            // Info panel verticale per bot laterali
            JPanel infoPanel = new JPanel();
            infoPanel.setOpaque(false);
            infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));

            JPanel cardsRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 3, 0));
            cardsRow.setOpaque(false);
            JPanel opponentDeckIcon = createOpponentWonCardsDeckIcon(player);
            cardsRow.add(opponentDeckIcon);

            JLabel wonLabel = new JLabel("Carte: " + gameState.getWonCardsCount(player));
            wonLabel.setForeground(TEXT_GOLD);
            wonLabel.setFont(new Font("Arial", Font.PLAIN, 10));
            cardsRow.add(wonLabel);

            cardsRow.setAlignmentX(Component.CENTER_ALIGNMENT);
            infoPanel.add(cardsRow);

            MenuImpostazioni settings = MenuImpostazioni.getInstance();
            if (settings.isShowScore()) {
                infoPanel.add(Box.createVerticalStrut(3));
                JLabel scoreLabel = new JLabel("Punti: " + gameState.getScaledScoreString(player));
                scoreLabel.setForeground(new Color(255, 215, 0));
                scoreLabel.setFont(new Font("Georgia", Font.BOLD, 10));
                scoreLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
                infoPanel.add(scoreLabel);
            }

            box.add(Box.createVerticalStrut(5));
            box.add(nameLabel);
            box.add(Box.createVerticalStrut(8));
            box.add(vstack);
            box.add(Box.createVerticalStrut(5));
            box.add(infoPanel);
            box.add(Box.createVerticalStrut(5));

            Dimension vPref = vstack.getPreferredSize();
            int prefW = Math.max(sideCardWidth + 12, vPref.width);
            int prefH;
            if (vPref.height > 0) {
                prefH = vPref.height;
            } else {
                int step = Math.max(4, sideCardHeight - overlap);
                int totalH = (handSize == 0) ? sideCardHeight : sideCardHeight + Math.max(0, (handSize - 1) * step);
                prefH = totalH + 8;
            }

            // DIMENSIONE MINIMA per evitare che il box sparisca
            int minHeight = sideCardHeight + nameLabel.getPreferredSize().height +
                    infoPanel.getPreferredSize().height + 40;
            prefH = Math.max(minHeight, prefH);

            int extraH = nameLabel.getPreferredSize().height + infoPanel.getPreferredSize().height + 26;
            box.setPreferredSize(new Dimension(prefW + 12, prefH + extraH));

            vstack.setMaximumSize(new Dimension(prefW, Integer.MAX_VALUE));
            nameLabel.setMaximumSize(new Dimension(prefW + 10, nameLabel.getPreferredSize().height));
            infoPanel.setMaximumSize(new Dimension(prefW + 10, infoPanel.getPreferredSize().height));

        } else {
            // BOT IN ALTO - orizzontale
            int cardWidth = sideCardWidth;
            int cardHeight = sideCardHeight;
            int overlap = -12;

            JPanel cardsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, overlap, 3));
            cardsPanel.setOpaque(false);

            Image cardBackImg = CardImageLoader.getScaledCardBackImage(cardWidth, cardHeight);
            for (int i = 0; i < handSize; i++) {
                JLabel cardBack = new JLabel(new ImageIcon(cardBackImg));
                cardBack.setPreferredSize(new Dimension(cardWidth, cardHeight));
                cardsPanel.add(cardBack);
            }

            JPanel infoPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));
            infoPanel.setOpaque(false);
            JPanel opponentDeckIcon = createOpponentWonCardsDeckIcon(player);
            infoPanel.add(opponentDeckIcon);

            JLabel wonLabel = new JLabel("Carte: " + gameState.getWonCardsCount(player));
            wonLabel.setForeground(TEXT_GOLD);
            wonLabel.setFont(new Font("Arial", Font.PLAIN, 11));
            infoPanel.add(wonLabel);

            MenuImpostazioni settings = MenuImpostazioni.getInstance();
            if (settings.isShowScore()) {
                JLabel sc = new JLabel(" | Punti: " + gameState.getScaledScoreString(player));
                sc.setForeground(new Color(255, 215, 0));
                sc.setFont(new Font("Georgia", Font.BOLD, 11));
                infoPanel.add(sc);
            }

            JPanel container = new JPanel();
            container.setOpaque(false);
            container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));

            nameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            cardsPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
            infoPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

            container.add(nameLabel);
            container.add(Box.createVerticalStrut(5));
            container.add(cardsPanel);
            container.add(Box.createVerticalStrut(3));
            container.add(infoPanel);

            box.add(container);

            // DIMENSIONE MINIMA anche per bot in alto
            int minWidth = Math.max(200, cardWidth * 2);
            int totalWidth = Math.max(minWidth, handSize * cardWidth + Math.max(0, (handSize - 1) * overlap) + 30);
            int boxHeight = cardHeight + nameLabel.getPreferredSize().height +
                    infoPanel.getPreferredSize().height + 25;
            box.setPreferredSize(new Dimension(totalWidth, boxHeight));
        }
        return box;
    }

    private JPanel createOpponentWonCardsDeckIcon(Giocatore player) {
        JPanel deckIcon = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int wonCards = gameState.getWonCardsCount(player);

                int w = 18;
                int h = 25;
                int stackOffset = 1;
                int stackSize = Math.min(wonCards / 2, 4);

                for (int i = 0; i <= stackSize; i++) {
                    int x = i * stackOffset;
                    int y = (stackSize - i) * stackOffset;
                    g2d.setColor(new Color(0, 0, 0, 60));
                    g2d.fillRoundRect(x + 1, y + 1, w, h, 3, 3);

                    g2d.setColor(new Color(30, 60, 120));
                    g2d.fillRoundRect(x, y, w, h, 3, 3);

                    g2d.setColor(new Color(20, 40, 80));
                    g2d.drawRoundRect(x, y, w, h, 3, 3);
                }
            }

            @Override
            public Dimension getPreferredSize() {
                return new Dimension(28, 35);
            }
        };
        deckIcon.setOpaque(false);
        return deckIcon;
    }

    private JPanel createTableCenter() {
        JPanel center = new JPanel(new GridBagLayout());
        center.setOpaque(false);

        tableOval = new TablePanel();
        tableOval.setPreferredSize(new Dimension(640, 320));
        tableOval.setLayout(null);

        center.add(tableOval);
        return center;
    }

    private JLabel createDeckImage() {
        Image cardBackImg = CardImageLoader.getScaledCardBackImage(CARD_WIDTH, CARD_HEIGHT);
        JLabel deckLabel = new JLabel(new ImageIcon(cardBackImg));
        int cardsLeft = gameState.getDeck().remaining();
        deckLabel.setToolTipText("Carte nel mazzo: " + cardsLeft);
        JLabel numberLabel = new JLabel("" + cardsLeft, SwingConstants.CENTER);
        numberLabel.setForeground(TEXT_GOLD);
        numberLabel.setFont(new Font("Arial", Font.BOLD, 12));
        deckLabel.setLayout(new BorderLayout());
        deckLabel.add(numberLabel, BorderLayout.SOUTH);
        return deckLabel;
    }

    private static class TablePanel extends JPanel {
        public TablePanel() {
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // Shadow
            g2d.setColor(new Color(0, 0, 0, 80));
            g2d.fill(new RoundRectangle2D.Double(6, 6, getWidth() - 12, getHeight() - 12, 90, 90));

            // Table fill
            g2d.setColor(new Color(35, 130, 75));
            g2d.fill(new RoundRectangle2D.Double(4, 4, getWidth() - 8, getHeight() - 8, 90, 90));

            // Border (thinner)
            g2d.setColor(FELT_BORDER);
            g2d.setStroke(new BasicStroke(6));
            g2d.draw(new RoundRectangle2D.Double(4, 4, getWidth() - 8, getHeight() - 8, 90, 90));
        }
    }

    // Vertical stack panel: arranges child components bottom-to-top with overlap and adapts preferred height
    private static class VerticalCardStackPanel extends JPanel {
        private final int cardWidth;
        private final int cardHeight;
        private final int overlap;

        public VerticalCardStackPanel(int cardWidth, int cardHeight, int overlap) {
            this.cardWidth = cardWidth;
            this.cardHeight = cardHeight;
            this.overlap = overlap;
            setOpaque(false);
            setLayout(null); // we set component bounds ourselves
        }

        /**
         * layoutStack positions children bottom-to-top (component 0 at bottom).
         */
        private void layoutStack() {
            int count = getComponentCount();
            int w = getWidth();
            int h = getHeight();

            if (count == 0) return;

            int step = cardHeight - overlap;
            int totalH = cardHeight + (count - 1) * step;
            if (totalH > h && count > 1) {
                step = Math.max(4, (h - cardHeight) / (count - 1));
                totalH = cardHeight + (count - 1) * step;
            }

            int startY = h - totalH;
            for (int i = 0; i < count; i++) {
                Component c = getComponent(i);
                int x = Math.max(0, (w - cardWidth) / 2);
                int y = startY + i * step;
                c.setBounds(x, y, cardWidth, cardHeight);
            }
            revalidate();
            repaint();
        }

        @Override
        public void doLayout() {
            super.doLayout();
            layoutStack();
        }

        @Override
        public Dimension getPreferredSize() {
            int count = getComponentCount();
            int step = Math.max(4, cardHeight - overlap);
            int totalH = (count <= 0) ? cardHeight : cardHeight + Math.max(0, (count - 1) * step);
            return new Dimension(cardWidth + 12, totalH + 8);
        }
    }

    private JPanel createPlayerArea() {
        JPanel area = new JPanel();
        area.setOpaque(false);
        area.setLayout(new BoxLayout(area, BoxLayout.Y_AXIS));
        area.setBorder(new EmptyBorder(6, 20, 12, 20));

        JLabel playerLabel = new JLabel("La tua mano - " + humanPlayer.getName());
        playerLabel.setForeground(TEXT_WHITE);
        playerLabel.setFont(new Font("Arial", Font.BOLD, 15));
        playerLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        playerHandPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, HAND_GAP, 6));
        playerHandPanel.setOpaque(false);

        handScrollPane = new JScrollPane(playerHandPanel, JScrollPane.VERTICAL_SCROLLBAR_NEVER, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        handScrollPane.setOpaque(false);
        handScrollPane.getViewport().setOpaque(false);
        handScrollPane.setBorder(null);
        handScrollPane.setAlignmentX(Component.CENTER_ALIGNMENT);
        handScrollPane.setPreferredSize(new Dimension(800, handCardHeight + 30)); // will be updated

        JPanel wonCardsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        wonCardsPanel.setOpaque(false);
        JPanel deckIcon = createWonCardsDeckIcon();
        wonCardsPanel.add(deckIcon);

        wonCardsLabel = new JLabel("Carte prese: 0");
        wonCardsLabel.setForeground(TEXT_GOLD);
        wonCardsLabel.setFont(new Font("Arial", Font.BOLD, 12));
        wonCardsPanel.add(wonCardsLabel);

        playButton = new JButton("Gioca Carta");
        playButton.setVisible(false);
        playButton.setEnabled(false);

        area.add(playerLabel);
        area.add(Box.createVerticalStrut(6));
        area.add(wonCardsPanel);
        area.add(Box.createVerticalStrut(8));
        area.add(handScrollPane);
        area.add(Box.createVerticalStrut(8));

        area.setPreferredSize(new Dimension(0, handCardHeight + 110));
        return area;
    }

    private JPanel createWonCardsDeckIcon() {
        JPanel deckIcon = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int wonCards = gameState.getWonCardsCount(humanPlayer);
                int w = 25;
                int h = 35;
                int stackOffset = 2;
                int stackSize = Math.min(wonCards / 2, 5);

                for (int i = 0; i <= stackSize; i++) {
                    int x = i * stackOffset;
                    int y = (stackSize - i) * stackOffset;
                    g2d.setColor(new Color(0, 0, 0, 60));
                    g2d.fillRoundRect(x + 2, y + 2, w, h, 5, 5);

                    g2d.setColor(new Color(30, 60, 120));
                    g2d.fillRoundRect(x, y, w, h, 5, 5);

                    g2d.setColor(new Color(20, 40, 80));
                    g2d.drawRoundRect(x, y, w, h, 5, 5);
                }
            }

            @Override
            public Dimension getPreferredSize() {
                return new Dimension(40, 50);
            }
        };
        deckIcon.setOpaque(false);
        return deckIcon;
    }

    private JPanel createInfoPanel() {
        MenuImpostazioni settings = MenuImpostazioni.getInstance();
        if (!settings.isShowScore() && !settings.isShowMessages()) {
            JPanel emptyPanel = new JPanel();
            emptyPanel.setOpaque(false);
            emptyPanel.setPreferredSize(new Dimension(0, 0));
            return emptyPanel;
        }

        JPanel panel = getJPanel();

        JLabel titleLabel = new JLabel("Info Partita");
        titleLabel.setForeground(TEXT_GOLD);
        titleLabel.setFont(new Font("Serif", Font.BOLD, 18));
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        statusLabel = new JLabel("In attesa...");
        statusLabel.setForeground(TEXT_WHITE);
        statusLabel.setFont(new Font("Arial", Font.PLAIN, 13));
        statusLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        scoreLabel = new JLabel("Punteggio: 0");
        scoreLabel.setForeground(new Color(255, 215, 0));
        scoreLabel.setFont(new Font("Georgia", Font.BOLD, 16));
        scoreLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel logTitle = new JLabel("Log:");
        logTitle.setForeground(TEXT_GOLD);
        logTitle.setFont(new Font("Serif", Font.BOLD, 14));
        logTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        logArea = new JPanel();
        logArea.setOpaque(false);
        logArea.setLayout(new BoxLayout(logArea, BoxLayout.Y_AXIS));
        logArea.setAlignmentX(Component.LEFT_ALIGNMENT);

        JScrollPane logScroll = new JScrollPane(logArea);
        logScroll.setOpaque(false);
        logScroll.getViewport().setOpaque(false);
        logScroll.setBorder(null);
        logScroll.setPreferredSize(new Dimension(190, 200));
        logScroll.setAlignmentX(Component.LEFT_ALIGNMENT);

        panel.add(titleLabel);
        panel.add(Box.createVerticalStrut(15));
        panel.add(statusLabel);
        panel.add(Box.createVerticalStrut(10));

        if (settings.isShowScore()) {
            panel.add(scoreLabel);
            panel.add(Box.createVerticalStrut(15));
        }
        if (settings.isShowMessages()) {
            panel.add(logTitle);
            panel.add(Box.createVerticalStrut(5));
            panel.add(logScroll);
        }

        panel.add(Box.createVerticalGlue());

        return panel;
    }

    private static JPanel getJPanel() {
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(new Color(0, 0, 0, 128));
                g2d.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 20, 20));
            }
        };
        panel.setOpaque(false);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));
        panel.setPreferredSize(new Dimension(220, 0));
        return panel;
    }

    // CardPanel class supports arbitrary draw size
    private class CardPanel extends JPanel {
        private boolean isHovered = false;
        private boolean isAnimating = false;
        private float animationProgress = 0f;
        private final Image cardImage;
        private final int drawWidth;
        private final int drawHeight;
        private final int handIndex; // index in player's hand

        public CardPanel(Cards card, int index, boolean isPlayable, int drawWidth, int drawHeight) {
            this.drawWidth = drawWidth;
            this.drawHeight = drawHeight;
            this.handIndex = index;
            this.cardImage = CardImageLoader.getScaledCardImage(card, drawWidth, drawHeight);
            setOpaque(false);
            setPreferredSize(new Dimension(drawWidth + 5, drawHeight + 10));

            if (isPlayable) {
                addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseEntered(MouseEvent e) {
                        isHovered = true;
                        repaint();
                    }

                    @Override
                    public void mouseExited(MouseEvent e) {
                        isHovered = false;
                        repaint();
                    }

                    @Override
                    public void mouseClicked(MouseEvent e) {
                        int[] legalMoves = gameState.getLegalMoves(humanPlayer);
                        boolean isLegal = false;
                        for (int legal : legalMoves) {
                            if (legal == handIndex) {
                                isLegal = true;
                                break;
                            }
                        }

                        if (isLegal) {
                            animateCardSelection(() -> controller.onCardPlayed(handIndex));
                        } else {
                            log("Mossa non valida! Devi seguire il seme se possibile.");
                        }
                    }
                });
                setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            }
        }

        private void animateCardSelection(Runnable onComplete) {
            if (isAnimating) return;

            isAnimating = true;
            animationProgress = 0f;

            Timer animTimer = new Timer(16, null);
            animTimer.addActionListener(e -> {
                animationProgress += 0.15f;
                if (animationProgress >= 1.0f) {
                    animationProgress = 1.0f;
                    animTimer.stop();
                    isAnimating = false;
                    if (onComplete != null) {
                        onComplete.run();
                    }
                }
                repaint();
            });
            animTimer.start();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

            int offsetY = isHovered ? -8 : 0;

            if (isAnimating) {
                offsetY -= (int)(20 * animationProgress);
                float scale = 1.0f + (0.1f * animationProgress);
                int scaledWidth = (int)(drawWidth * scale);
                int scaledHeight = (int)(drawHeight * scale);
                int offsetX = (drawWidth - scaledWidth) / 2;

                g2d.setColor(new Color(0, 0, 0, 80));
                g2d.fill(new RoundRectangle2D.Double(3 + offsetX, 3 + offsetY, scaledWidth, scaledHeight, 10, 10));

                if (cardImage != null) {
                    g2d.drawImage(cardImage, offsetX, offsetY, scaledWidth, scaledHeight, this);
                }

                int alpha = (int)(255 * animationProgress);
                g2d.setColor(new Color(255, 215, 0, alpha));
                g2d.setStroke(new BasicStroke(3));
                g2d.drawRoundRect(offsetX, offsetY, scaledWidth - 1, scaledHeight - 1, 10, 10);
            } else {
                g2d.setColor(new Color(0, 0, 0, 80));
                g2d.fill(new RoundRectangle2D.Double(3, 3 + offsetY, drawWidth, drawHeight, 10, 10));

                if (cardImage != null) {
                    g2d.drawImage(cardImage, 0, offsetY, drawWidth, drawHeight, this);
                }

                if (isHovered) {
                    g2d.setColor(TEXT_GOLD);
                    g2d.setStroke(new BasicStroke(3));
                    g2d.drawRoundRect(0, offsetY, drawWidth - 1, drawHeight - 1, 10, 10);
                }
            }
        }
    }

    public void refresh() {
        MenuImpostazioni settings = MenuImpostazioni.getInstance();
        SwingUtilities.invokeLater(() -> {
            updatePlayerHand();
            // Non aggiornare le carte sul tavolo se c'è un'animazione in corso
            if (!cardAnimationInProgress.get()) {
                updateTableCards();
            }
            updateOpponentArea();
            if (settings.isShowScore()) { updateScores(); }
            if (settings.isShowMessages()) { updateStatus(); }
            updateWonCardsDisplay();
        });
    }

    /**
     * Remove player's CardPanel instances from the layered pane.
     */
    private void removePlayerCardsFromLayer() {
        if (cardPanels.isEmpty() || layeredPaneRef == null) return;
        for (CardPanel cp : new ArrayList<>(cardPanels)) {
            Container parent = cp.getParent();
            if (parent != null) {
                parent.remove(cp);
            }
        }
        cardPanels.clear();
        layeredPaneRef.revalidate();
        layeredPaneRef.repaint();
    }

    /**
     * Update player hand: create CardPanel instances and place them on the layered pane
     * positioned over the same visual area of playerHandPanel.
     */
    private void updatePlayerHand() {
        recomputeHandCardSize();

        // remove previous ones
        removePlayerCardsFromLayer();

        List<Cards> hand = gameState.getHand(humanPlayer);
        int cardCount = hand.size();

        // determine panel width/height (area where cards should appear)
        int panelWidth = playerHandPanel.getWidth();
        int panelHeight = playerHandPanel.getHeight();
        if (panelWidth <= 0 || panelHeight <= 0) {
            // fallback values if layout not computed yet
            panelWidth = Math.max(320, getContentPane().getWidth() - 60);
            panelHeight = handCardHeight + 30;
        }

        // origin of playerHandPanel in layered pane coordinates
        Point panelOrigin = SwingUtilities.convertPoint(playerHandPanel, 0, 0, layeredPaneRef);

        int totalWidth = cardCount * handCardWidth + Math.max(0, (cardCount - 1) * handCardGapUsed);

        int gap = handCardGapUsed;

        if (cardCount > 0 && totalWidth > panelWidth) {
            // recalc start to center with possible overlap already considered in recomputeHandCardSize
            totalWidth = cardCount * handCardWidth + Math.max(0, (cardCount - 1) * gap);
        }

        int startX = panelOrigin.x + Math.max(0, (panelWidth - totalWidth) / 2);
        int y = panelOrigin.y + Math.max(0, (panelHeight - handCardHeight) / 2);

        int[] legalMoves = gameState.getLegalMoves(humanPlayer);
        Giocatore current = gameState.getCurrentPlayer();
        boolean isMyTurn = current == humanPlayer;

        for (int i = 0; i < cardCount; i++) {
            Cards card = hand.get(i);
            boolean isLegal = false;
            for (int legal : legalMoves) {
                if (legal == i) { isLegal = true; break; }
            }

            CardPanel cp = new CardPanel(card, i, isMyTurn && isLegal, handCardWidth, handCardHeight);
            int x = startX + i * (handCardWidth + gap);
            cp.setBounds(x, y, handCardWidth, handCardHeight);
            layeredPaneRef.add(cp, JLayeredPane.PALETTE_LAYER);
            cardPanels.add(cp);
        }

        layeredPaneRef.revalidate();
        layeredPaneRef.repaint();

        // keep visual labels updated
        updateWonCardsDisplay();
    }

    /**
     * Compute slot positions relative to current tableOval size.
     * Returns positions for all players in their FIXED order.
     */
    private int[][] computeSlotPositions() {
        int w = tableOval.getWidth() > 0 ? tableOval.getWidth() : tableOval.getPreferredSize().width;
        int h = tableOval.getHeight() > 0 ? tableOval.getHeight() : tableOval.getPreferredSize().height;

        int centerX = (w - CARD_WIDTH) / 2;
        int centerY = (h - CARD_HEIGHT) / 2;

        int numPlayers = this.players.size();
        int humanIndex = players.indexOf(humanPlayer);

        int[][] positions = new int[numPlayers][2];

        // Margini aumentati per i nomi
        int topMargin = 40;      // Spazio per nome in alto
        int bottomMargin = 40;   // Spazio per nome in basso
        int sideMargin = 60;     // Spazio per nomi laterali

        if (numPlayers == 2) {
            for (int i = 0; i < numPlayers; i++) {
                if (i == humanIndex) {
                    positions[i] = new int[]{centerX, h - CARD_HEIGHT - bottomMargin};
                } else {
                    positions[i] = new int[]{centerX, topMargin};
                }
            }
        } else if (numPlayers == 3) {
            for (int i = 0; i < numPlayers; i++) {
                int relativePos = (i - humanIndex + 3) % 3;
                if (relativePos == 0) {
                    positions[i] = new int[]{centerX, h - CARD_HEIGHT - bottomMargin};
                } else if (relativePos == 1) {
                    positions[i] = new int[]{sideMargin, centerY - CARD_HEIGHT / 2};
                } else {
                    positions[i] = new int[]{centerX, topMargin};
                }
            }
        } else if (numPlayers == 4) {
            for (int i = 0; i < numPlayers; i++) {
                int relativePos = (i - humanIndex + 4) % 4;
                if (relativePos == 0) {
                    positions[i] = new int[]{centerX, h - CARD_HEIGHT - bottomMargin};
                } else if (relativePos == 1) {
                    positions[i] = new int[]{sideMargin, centerY - CARD_HEIGHT / 2};
                } else if (relativePos == 2) {
                    positions[i] = new int[]{centerX, topMargin};
                } else {
                    positions[i] = new int[]{w - CARD_WIDTH - sideMargin, centerY - CARD_HEIGHT / 2};
                }
            }
        }

        return positions;
    }


    private void updateTableCards() {
        tableOval.removeAll();

        // Posiziona il mazzo PIÙ AL CENTRO (tra bot sinistro e centro tavolo)
        JLabel deck = createDeckImage();
        int dw = deck.getPreferredSize().width;
        int dh = deck.getPreferredSize().height;
        int deckX = (int)(tableOval.getWidth() * 0.60); // Era 15 fisso, ora 15% della larghezza
        int deckY = (tableOval.getHeight() - dh) / 2;
        deck.setBounds(deckX, deckY, dw, dh);
        tableOval.add(deck);

        int nPlayers = this.players.size();
        int[][] positions = computeSlotPositions();

        // Slot per evidenziare turno corrente
        for (int i = 0; i < nPlayers; i++) {
            int x = positions[i][0];
            int y = positions[i][1];
            int finalI = i;
            JPanel slot = new JPanel() {
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    Graphics2D g2d = (Graphics2D) g;
                    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    if (GameView.this.players.get(finalI) == gameState.getCurrentPlayer()) {
                        g2d.setColor(new Color(255, 215, 0, 160));
                        g2d.setStroke(new BasicStroke(3));
                        g2d.drawRoundRect(4, 4, CARD_WIDTH, CARD_HEIGHT, 12, 12);
                    }
                }
            };
            slot.setOpaque(false);
            slot.setBounds(Math.max(0, x - 6), Math.max(0, y - 6), CARD_WIDTH + 12, CARD_HEIGHT + 12);
            tableOval.add(slot);
        }

        // Carte giocate
        List<Giocatore> trickPlayers = gameState.getTrickPlayers();
        List<Cards> trickCards = gameState.getTrickCards();

        for (int i = 0; i < nPlayers; i++) {
            Giocatore player = this.players.get(i);
            int cardIndex = trickPlayers.indexOf(player);
            if (cardIndex >= 0 && cardIndex < trickCards.size()) {
                int x = positions[i][0];
                int y = positions[i][1];
                CardPanel cardPanel = new CardPanel(trickCards.get(cardIndex), -1, false, CARD_WIDTH, CARD_HEIGHT);
                cardPanel.setBounds(x, y, CARD_WIDTH, CARD_HEIGHT);
                tableOval.add(cardPanel);
            }
        }

        // Nomi giocatori - BOT LATERALI SOTTO LE CARTE
        int tableW = tableOval.getWidth();
        int tableH = tableOval.getHeight();

        for (int i = 0; i < nPlayers; i++) {
            Giocatore player = this.players.get(i);
            JLabel name = new JLabel(player.getName());
            name.setForeground(TEXT_GOLD);
            name.setFont(new Font("Arial", Font.BOLD, 13));

            int cardX = positions[i][0];
            int cardY = positions[i][1];

            FontMetrics fm = name.getFontMetrics(name.getFont());
            int labelWidth = fm.stringWidth(player.getName()) + 15;
            int labelHeight = 18;

            int nx, ny;
            int humanIdx = players.indexOf(humanPlayer);

            if (i == humanIdx) {
                // Giocatore umano - sotto la carta
                nx = cardX + (CARD_WIDTH - labelWidth) / 2;
                ny = cardY + CARD_HEIGHT + 8;
            } else if (nPlayers == 2) {
                // Bot in alto - sopra la carta
                nx = cardX + (CARD_WIDTH - labelWidth) / 2;
                ny = cardY - labelHeight - 8;
            } else {
                int relativePos = (i - humanIdx + nPlayers) % nPlayers;

                if (relativePos == 1) {
                    // Bot a sinistra - SOTTO la carta invece che a destra
                    nx = cardX + (CARD_WIDTH - labelWidth) / 2;
                    ny = cardY + CARD_HEIGHT + 8;
                } else if (relativePos == nPlayers - 1) {
                    // Bot a destra - SOTTO la carta invece che a sinistra
                    nx = cardX + (CARD_WIDTH - labelWidth) / 2;
                    ny = cardY + CARD_HEIGHT + 8;
                } else {
                    // Bot in alto - sopra la carta
                    nx = cardX + (CARD_WIDTH - labelWidth) / 2;
                    ny = cardY - labelHeight - 8;
                }
            }

            // Assicura che il nome stia dentro i limiti
            nx = Math.max(10, Math.min(nx, tableW - labelWidth - 10));
            ny = Math.max(10, Math.min(ny, tableH - labelHeight - 10));

            name.setBounds(nx, ny, labelWidth, labelHeight);
            tableOval.add(name);
        }

        tableOval.revalidate();
        tableOval.repaint();
    }


    /**
     * Update opponent area.
     * - top bot when vertical uses same vertical stack as side bots,
     *   and its box preferred size depends on number of cards.
     * - left/right adapt width based on sideCardWidth.
     */
    private void updateOpponentArea() {
        opponentArea.removeAll();
        leftBotPanel.removeAll();
        rightBotPanel.removeAll();

        int sidePanelW = sideCardWidth + 24;
        leftBotPanel.setPreferredSize(new Dimension(sidePanelW, leftBotPanel.getHeight()));
        rightBotPanel.setPreferredSize(new Dimension(sidePanelW, rightBotPanel.getHeight()));

        int numPlayers = players.size();
        int areaWidth = opponentArea.getWidth() > 0 ? opponentArea.getWidth() : 1000;

        int humanIndex = players.indexOf(humanPlayer);

        for (int i = 0; i < numPlayers; i++) {
            if (i == humanIndex) continue;

            Giocatore player = players.get(i);
            boolean isVertical = false;

            JPanel targetPanel = opponentArea;

            if (numPlayers == 2) {
                // Bot in alto - orizzontale
                isVertical = false;
                targetPanel = opponentArea;
            } else if (numPlayers == 3) {
                int relativePos = (i - humanIndex + 3) % 3;
                if (relativePos == 1) {
                    // left player - verticale
                    isVertical = true;
                    targetPanel = leftBotPanel;
                } else { // relativePos == 2 (top)
                    // top player - orizzontale
                    isVertical = false;
                    targetPanel = opponentArea;
                }
            } else if (numPlayers == 4) {
                int relativePos = (i - humanIndex + 4) % 4;
                if (relativePos == 1) {
                    // left - verticale
                    isVertical = true;
                    targetPanel = leftBotPanel;
                } else if (relativePos == 2) {
                    // top player - orizzontale
                    isVertical = false;
                    targetPanel = opponentArea;
                } else if (relativePos == 3) {
                    // right - verticale
                    isVertical = true;
                    targetPanel = rightBotPanel;
                }
            }

            JPanel opponentBox = createOpponentBox(player, isVertical);

            if (targetPanel == leftBotPanel || targetPanel == rightBotPanel) {
                // Bot laterali - centra verticalmente
                opponentBox.setAlignmentX(Component.CENTER_ALIGNMENT);
                targetPanel.add(Box.createVerticalGlue());
                targetPanel.add(opponentBox);
                targetPanel.add(Box.createVerticalGlue());
            } else {
                // Bot in alto - centra orizzontalmente nell'opponentArea
                Dimension pref = opponentBox.getPreferredSize();
                int prefW = pref.width > 0 ? pref.width : 300;
                int prefH = pref.height > 0 ? pref.height : 120;

                // Centra sopra il tavolo
                int posX;
                try {
                    Rectangle tableRect = SwingUtilities.convertRectangle(
                            tableOval.getParent(),
                            tableOval.getBounds(),
                            opponentArea
                    );
                    int tableCenterX = tableRect.x + tableRect.width / 2;
                    posX = tableCenterX - prefW / 2;
                } catch (Exception ex) {
                    posX = Math.max(8, (areaWidth - prefW) / 2);
                }

                int posY = 10;
                posX = Math.max(8, Math.min(posX, Math.max(8, areaWidth - prefW - 8)));

                opponentBox.setBounds(posX, posY, prefW, prefH);
                opponentArea.add(opponentBox);
            }
        }

        opponentArea.revalidate();
        opponentArea.repaint();
        leftBotPanel.revalidate();
        leftBotPanel.repaint();
        rightBotPanel.revalidate();
        rightBotPanel.repaint();
    }

    private void updateScores() {
        String score = gameState.getScaledScoreString(humanPlayer);
        scoreLabel.setText("Punteggio: " + score);
    }

    private void updateStatus() {
        Giocatore current = gameState.getCurrentPlayer();
        if (current == humanPlayer) {
            statusLabel.setText("<html>È il tuo turno!<br>Scegli una carta da giocare.</html>");
            statusLabel.setForeground(TEXT_GOLD);
        } else {
            statusLabel.setText("Turno di: " + current.getName());
            statusLabel.setForeground(TEXT_WHITE);
        }
    }

    public void log(String message) {
        MenuImpostazioni settings = MenuImpostazioni.getInstance();
        if (!settings.isShowMessages()) {
            return;
        }
        SwingUtilities.invokeLater(() -> {
            JLabel logEntry = new JLabel("• " + message);
            logEntry.setForeground(TEXT_WHITE);
            logEntry.setFont(new Font("Arial", Font.PLAIN, 11));
            logEntry.setAlignmentX(Component.LEFT_ALIGNMENT);

            logArea.add(logEntry, 0);

            while (logArea.getComponentCount() > 50) {
                logArea.remove(logArea.getComponentCount() - 1);
            }

            logArea.revalidate();
            logArea.repaint();
        });
    }
    /**
     * Mostra l'animazione di una carta giocata dalla mano del giocatore al tavolo.
     * L'animazione parte dalla posizione della mano (o area del giocatore) e arriva
     * alla posizione finale sul tavolo con un effetto fluido.
     * 
     * @param player Il giocatore che ha giocato la carta
     * @param card La carta giocata
     * @param onComplete Callback chiamato quando l'animazione è completata
     */
    public void showCardPlayed(Giocatore player, Cards card, Runnable onComplete) {
        SwingUtilities.invokeLater(() -> {
            cardAnimationInProgress.set(true);
            
            int playerIdx = players.indexOf(player);
            if (playerIdx < 0) {
                cardAnimationInProgress.set(false);
                if (onComplete != null) onComplete.run();
                return;
            }

            int[][] positions = computeSlotPositions();

            // Trova la posizione di partenza basata sul tipo di giocatore
            Point startPos = getPlayerCardStartPosition(player, playerIdx);

            // Posizione finale sul tavolo
            Point tableOrigin = tableOval.getLocationOnScreen();
            Point frameOrigin = getLocationOnScreen();

            int endX = positions[playerIdx][0];
            int endY = positions[playerIdx][1];

            // Crea overlay per l'animazione
            JPanel overlay = new JPanel(null);
            overlay.setOpaque(false);
            overlay.setBounds(0, 0, getWidth(), getHeight());

            // Converti coordinate relative al frame
            int startX = startPos.x - frameOrigin.x;
            int startY = startPos.y - frameOrigin.y;
            int finalX = tableOrigin.x - frameOrigin.x + endX;
            int finalY = tableOrigin.y - frameOrigin.y + endY;

            // Crea la carta volante
            Image cardImg = CardImageLoader.getScaledCardImage(card, CARD_WIDTH, CARD_HEIGHT);
            JLabel flyingCard = new JLabel(new ImageIcon(cardImg));
            flyingCard.setBounds(startX, startY, CARD_WIDTH, CARD_HEIGHT);
            overlay.add(flyingCard);

            // Aggiungi overlay al glass pane per coprire tutto
            JPanel glassPane = (JPanel) getGlassPane();
            glassPane.setLayout(null);
            glassPane.add(overlay);
            glassPane.setVisible(true);

            // Anima la carta con effetto curvo
            animateCardFlightCurved(flyingCard, startX, startY, finalX, finalY, () -> {
                // Cleanup: rimuovi overlay e aggiorna tavolo
                glassPane.remove(overlay);
                glassPane.setVisible(false);
                glassPane.repaint();
                updateTableCards();
                
                cardAnimationInProgress.set(false);
                
                // Notifica il completamento dell'animazione
                if (onComplete != null) {
                    onComplete.run();
                }
            });
        });
    }

    /**
     * Ottiene la posizione di partenza della carta per un giocatore specifico.
     */
    private Point getPlayerCardStartPosition(Giocatore player, int playerIdx) {
        if (player == humanPlayer) {
            // Per il giocatore umano: centro della mano
            Point handPos = playerHandPanel.getLocationOnScreen();
            return new Point(
                    handPos.x + playerHandPanel.getWidth() / 2 - CARD_WIDTH / 2,
                    handPos.y + playerHandPanel.getHeight() / 2 - CARD_HEIGHT / 2
            );
        } else {
            // Per i bot: dall'area delle loro carte
            if (players.size() == 2) {
                // Bot in alto
                Point opponentPos = opponentArea.getLocationOnScreen();
                return new Point(
                        opponentPos.x + opponentArea.getWidth() / 2 - CARD_WIDTH / 2,
                        opponentPos.y + opponentArea.getHeight() - 30
                );
            } else {
                // Bot laterali o in alto (3-4 giocatori)
                int humanIdx = players.indexOf(humanPlayer);
                int relativePos = (playerIdx - humanIdx + players.size()) % players.size();

                if (relativePos == 1) {
                    // Bot sinistro
                    Point leftPos = leftBotPanel.getLocationOnScreen();
                    return new Point(
                            leftPos.x + leftBotPanel.getWidth() / 2 - CARD_WIDTH / 2,
                            leftPos.y + leftBotPanel.getHeight() / 2
                    );
                } else if (relativePos == players.size() - 1) {
                    // Bot destro
                    Point rightPos = rightBotPanel.getLocationOnScreen();
                    return new Point(
                            rightPos.x + rightBotPanel.getWidth() / 2 - CARD_WIDTH / 2,
                            rightPos.y + rightBotPanel.getHeight() / 2
                    );
                } else {
                    // Bot in alto
                    Point topPos = opponentArea.getLocationOnScreen();
                    return new Point(
                            topPos.x + opponentArea.getWidth() / 2 - CARD_WIDTH / 2,
                            topPos.y + opponentArea.getHeight() - 30
                    );
                }
            }
        }
    }

    /**
     * Anima il volo di una carta con traiettoria curva più realistica.
     */
    private void animateCardFlightCurved(JLabel card, int startX, int startY, int endX, int endY, Runnable onComplete) {
        final int steps = 20;
        final int delay = 50;

        Timer flyTimer = new Timer(delay, null);
        final int[] step = {0};

        // Calcola il punto di controllo per la curva (più alto rispetto alla linea retta)
        int midX = (startX + endX) / 2;
        int midY = Math.min(startY, endY) - 50; // Arco verso l'alto

        flyTimer.addActionListener(e -> {
            step[0]++;
            double t = (double) step[0] / steps;

            // Easing con accelerazione all'inizio e decelerazione alla fine
            double easedT = t < 0.5
                    ? 2 * t * t
                    : 1 - Math.pow(-2 * t + 2, 2) / 2;

            // Curva di Bezier quadratica per traiettoria realistica
            double currentX = Math.pow(1 - easedT, 2) * startX +
                    2 * (1 - easedT) * easedT * midX +
                    Math.pow(easedT, 2) * endX;

            double currentY = Math.pow(1 - easedT, 2) * startY +
                    2 * (1 - easedT) * easedT * midY +
                    Math.pow(easedT, 2) * endY;

            // Scala la carta leggermente durante il volo
            double scale = 1.0 - (Math.sin(t * Math.PI) * 0.1); // Max 10% scaling
            int scaledWidth = (int) (CARD_WIDTH * scale);
            int scaledHeight = (int) (CARD_HEIGHT * scale);

            // Rotazione leggera della carta
            // (Per semplicità usiamo solo scaling, ma potresti aggiungere rotazione con Graphics2D)

            card.setBounds(
                    (int) currentX - (scaledWidth - CARD_WIDTH) / 2,
                    (int) currentY - (scaledHeight - CARD_HEIGHT) / 2,
                    scaledWidth,
                    scaledHeight
            );
            card.getParent().repaint();

            if (step[0] >= steps) {
                flyTimer.stop();
                if (onComplete != null) {
                    Timer delayTimer = new Timer(50, e2 -> onComplete.run());
                    delayTimer.setRepeats(false);
                    delayTimer.start();
                }
            }
        });

        flyTimer.start();
    }


    public void clearTable() {
        SwingUtilities.invokeLater(() -> {
            tableOval.removeAll();
            tableOval.revalidate();
            tableOval.repaint();
            // remove player's card components from layered pane as well
            removePlayerCardsFromLayer();
        });
    }

    public void showTrickWon(Giocatore winner) {
        SwingUtilities.invokeLater(() -> {
            if (winner == humanPlayer) {
                wonCardsLabel.setText("Carte prese: " + gameState.getWonCardsCount(humanPlayer));
            }
            showWinnerIndicator(winner);
            animateCardsToWinner();
        });
    }

    private void showWinnerIndicator(Giocatore winner) {
        JLabel winnerLabel = new JLabel("🏆 " + winner.getName() + " prende!");
        winnerLabel.setForeground(TEXT_GOLD);
        winnerLabel.setFont(new Font("Serif", Font.BOLD, 16));
        winnerLabel.setHorizontalAlignment(SwingConstants.CENTER);

        int labelW = Math.min(280, tableOval.getWidth() - 20);
        int labelH = 30;
        // Posizionato in BASSO A DESTRA per evitare sovrapposizioni
        int x = tableOval.getWidth() - labelW - 15;
        int y = tableOval.getHeight() - labelH - 15;
        winnerLabel.setBounds(x, y, labelW, labelH);

        tableOval.add(winnerLabel);
        tableOval.revalidate();
        tableOval.repaint();

        Timer removeTimer = new Timer(1200, e -> {
            tableOval.remove(winnerLabel);
            tableOval.revalidate();
            tableOval.repaint();
        });
        removeTimer.setRepeats(false);
        removeTimer.start();
    }

    private void animateCardsToWinner() {
        Timer fadeTimer = new Timer(50, null);
        final float[] alpha = {1.0f};

        fadeTimer.addActionListener(e -> {
            alpha[0] -= 0.1f;
            if (alpha[0] <= 0) {
                fadeTimer.stop();
                clearTable();
                refresh();
            } else {
                tableOval.repaint();
            }
        });
        fadeTimer.start();
    }

    private void updateWonCardsDisplay() {
        int humanWonCards = gameState.getWonCardsCount(humanPlayer);
        wonCardsLabel.setText("Carte prese: " + humanWonCards);
    }

    public void showDealingAnimation(List<Giocatore> players, Runnable onComplete) {
        SwingUtilities.invokeLater(() -> {
            // Crea overlay con sfondo semi-trasparente
            JPanel animationOverlay = createAnimationOverlay();

            JPanel glassPane = (JPanel) getGlassPane();
            glassPane.setLayout(null);
            glassPane.removeAll();
            glassPane.add(animationOverlay);
            glassPane.setVisible(true);

            int centerX = getWidth() / 2 - CARD_WIDTH / 2;
            int centerY = getHeight() / 2 - CARD_HEIGHT / 2;

            // Label "Distribuzione carte..."
            JLabel dealingLabel = createDealingLabel(centerY);
            animationOverlay.add(dealingLabel);

            // Mazzo centrale animato
            List<JLabel> deckCards = createAnimatedDeck(centerX, centerY);
            for (JLabel deckCard : deckCards) {
                animationOverlay.add(deckCard);
            }

            // Calcola posizioni target per tutti i giocatori
            int numPlayers = players.size();
            Point[] targetPositions = calculatePlayerTargetPositions(numPlayers);

            animationOverlay.repaint();

            final int cardsPerPlayer = 10;
            final int totalCards = numPlayers * cardsPerPlayer;
            final int[] currentCard = {0};
            final int dealDelay = this.dealDelayMs; // usa il campo configurabile

            Timer dealTimer = new Timer(dealDelay, null);

            dealTimer.addActionListener(e -> {
                if (currentCard[0] >= totalCards) {
                    dealTimer.stop();
                    finalizeDealingAnimation(dealingLabel, deckCards, glassPane, animationOverlay, onComplete);
                    return;
                }

                int playerIndex = currentCard[0] % numPlayers;
                int cardNumber = currentCard[0] / numPlayers;

                Point targetPos = targetPositions[playerIndex];

                int offsetX = cardNumber * 2;
                int offsetY = cardNumber * 2;
                int finalX = targetPos.x + offsetX;
                int finalY = targetPos.y + offsetY;

                // Crea la carta volante
                Image cardBackImg = CardImageLoader.getScaledCardBackImage(CARD_WIDTH, CARD_HEIGHT);
                JLabel flyingCard = new JLabel(new ImageIcon(cardBackImg));
                flyingCard.setBounds(centerX, centerY, CARD_WIDTH, CARD_HEIGHT);
                animationOverlay.add(flyingCard);
                animationOverlay.setComponentZOrder(flyingCard, 0);

                // Effetto: rimuovi una carta dal mazzo visivo (come prima)
                if (!deckCards.isEmpty() && currentCard[0] % 3 == 0) {
                    JLabel topCard = deckCards.remove(deckCards.size() - 1);
                    animationOverlay.remove(topCard);
                }
                

                // Anima la carta verso il giocatore con curva realistica
                // Se vuoi il volo più lento, aumenta steps/delay in animateDealingCardFlight
                animateDealingCardFlight(flyingCard, centerX, centerY, finalX, finalY, playerIndex);
                // riproduci il suono "card played" per ogni carta distribuita
                if (audioManager != null) {
                    // chiamata non bloccante: AudioManager crea e avvia un Clip per ogni effetto
                    audioManager.playCardSound();
                }

                currentCard[0]++;
                animationOverlay.repaint();
            });

            dealTimer.start();
        });
    }

    private void finalizeDealingAnimation(JLabel dealingLabel, List<JLabel> deckCards,
                                          JPanel glassPane, JPanel overlay, Runnable onComplete) {
        // Cambia testo
        dealingLabel.setText("Pronto!");

        // Rimuovi le carte del mazzo rimaste
        for (JLabel deckCard : deckCards) {
            overlay.remove(deckCard);
        }
        overlay.repaint();

        // Fade out dell'overlay
        Timer fadeOutTimer = new Timer(20, null);
        final float[] alpha = {1f};

        fadeOutTimer.addActionListener(e -> {
            alpha[0] -= 0.05f;

            if (alpha[0] <= 0f) {
                fadeOutTimer.stop();
                glassPane.remove(overlay);
                glassPane.setVisible(false);
                glassPane.repaint();

                // Callback finale
                if (onComplete != null) {
                    onComplete.run();
                }
            } else {
                overlay.repaint();
            }
        });

        // Aspetta un momento prima del fade out
        Timer delayTimer = new Timer(800, e -> fadeOutTimer.start());
        delayTimer.setRepeats(false);
        delayTimer.start();
    }

    private JLabel createDealingLabel(int centerY) {
        JLabel label = new JLabel("Distribuzione carte...") {
            private float glowAlpha = 0f;
            private boolean increasing = true;

            {
                Timer glowTimer = new Timer(30, e -> {
                    if (increasing) {
                        glowAlpha += 0.05f;
                        if (glowAlpha >= 1f) {
                            glowAlpha = 1f;
                            increasing = false;
                        }
                    } else {
                        glowAlpha -= 0.05f;
                        if (glowAlpha <= 0.3f) {
                            glowAlpha = 0.3f;
                            increasing = true;
                        }
                    }
                    repaint();
                });
                glowTimer.start();
            }

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

                // Ombra
                g2d.setColor(new Color(0, 0, 0, 150));
                g2d.drawString(getText(), 3, getHeight() - 5);

                // Glow effect
                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, glowAlpha * 0.5f));
                g2d.setColor(TEXT_GOLD);
                for (int i = 1; i <= 3; i++) {
                    g2d.drawString(getText(), -i, getHeight() - 8 - i);
                    g2d.drawString(getText(), i, getHeight() - 8 - i);
                }

                // Testo principale
                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
                g2d.setColor(TEXT_GOLD);
                g2d.drawString(getText(), 0, getHeight() - 8);

                g2d.dispose();
            }
        };

        label.setFont(new Font("Georgia", Font.BOLD, 32));
        label.setForeground(TEXT_GOLD);
        label.setHorizontalAlignment(SwingConstants.CENTER);

        FontMetrics fm = label.getFontMetrics(label.getFont());
        int labelWidth = fm.stringWidth("Distribuzione carte...") + 40;
        int labelHeight = fm.getHeight() + 20;

        label.setBounds(
                (getWidth() - labelWidth) / 2,
                centerY - 100,
                labelWidth,
                labelHeight
        );

        return label;
    }

    private List<JLabel> createAnimatedDeck(int centerX, int centerY) {
        List<JLabel> deckCards = new ArrayList<>();
        Image cardBackImg = CardImageLoader.getScaledCardBackImage(CARD_WIDTH, CARD_HEIGHT);

        // Crea un mazzo visivo di 8 carte impilate
        for (int i = 0; i < 8; i++) {
            JLabel deckCard = new JLabel(new ImageIcon(cardBackImg));
            int offsetX = i * 2;
            int offsetY = -i * 2;
            deckCard.setBounds(centerX + offsetX, centerY + offsetY, CARD_WIDTH, CARD_HEIGHT);
            deckCards.add(deckCard);
        }

        return deckCards;
    }

    private Point[] calculatePlayerTargetPositions(int numPlayers) {
        Point frameOrigin = getLocationOnScreen();
        Point[] positions = new Point[numPlayers];

        int humanIdx = players.indexOf(humanPlayer);

        for (int i = 0; i < numPlayers; i++) {
            Point targetPos = null;

            if (i == humanIdx) {
                // GIOCATORE UMANO: verso la sua mano in basso
                try {
                    Point handPos = playerHandPanel.getLocationOnScreen();
                    targetPos = new Point(
                            handPos.x - frameOrigin.x + playerHandPanel.getWidth() / 2 - CARD_WIDTH / 2,
                            handPos.y - frameOrigin.y - 20 // Leggermente sopra la mano
                    );
                } catch (Exception e) {
                    // Fallback
                    targetPos = new Point(
                            getWidth() / 2 - CARD_WIDTH / 2,
                            getHeight() - 200
                    );
                }
            } else {
                // BOT: verso le loro aree
                int relativePos = (i - humanIdx + numPlayers) % numPlayers;

                if (numPlayers == 2) {
                    // 1 vs 1: bot in alto
                    try {
                        Point opponentPos = opponentArea.getLocationOnScreen();
                        targetPos = new Point(
                                opponentPos.x - frameOrigin.x + opponentArea.getWidth() / 2 - CARD_WIDTH / 2,
                                opponentPos.y - frameOrigin.y + opponentArea.getHeight() - 40
                        );
                    } catch (Exception e) {
                        targetPos = new Point(
                                getWidth() / 2 - CARD_WIDTH / 2,
                                120
                        );
                    }
                } else if (numPlayers == 3) {
                    // 3 giocatori
                    if (relativePos == 1) {
                        // Bot a SINISTRA
                        try {
                            Point leftPos = leftBotPanel.getLocationOnScreen();
                            targetPos = new Point(
                                    leftPos.x - frameOrigin.x + leftBotPanel.getWidth() / 2 - CARD_WIDTH / 2,
                                    leftPos.y - frameOrigin.y + leftBotPanel.getHeight() / 2 - CARD_HEIGHT / 2
                            );
                        } catch (Exception e) {
                            targetPos = new Point(80, getHeight() / 2);
                        }
                    } else {
                        // Bot in ALTO
                        try {
                            Point topPos = opponentArea.getLocationOnScreen();
                            targetPos = new Point(
                                    topPos.x - frameOrigin.x + opponentArea.getWidth() / 2 - CARD_WIDTH / 2,
                                    topPos.y - frameOrigin.y + opponentArea.getHeight() - 40
                            );
                        } catch (Exception e) {
                            targetPos = new Point(getWidth() / 2, 120);
                        }
                    }
                } else if (numPlayers == 4) {
                    // 4 giocatori
                    if (relativePos == 1) {
                        // Bot a SINISTRA
                        try {
                            Point leftPos = leftBotPanel.getLocationOnScreen();
                            targetPos = new Point(
                                    leftPos.x - frameOrigin.x + leftBotPanel.getWidth() / 2 - CARD_WIDTH / 2,
                                    leftPos.y - frameOrigin.y + leftBotPanel.getHeight() / 2 - CARD_HEIGHT / 2
                            );
                        } catch (Exception e) {
                            targetPos = new Point(80, getHeight() / 2);
                        }
                    } else if (relativePos == 2) {
                        // Bot in ALTO
                        try {
                            Point topPos = opponentArea.getLocationOnScreen();
                            targetPos = new Point(
                                    topPos.x - frameOrigin.x + opponentArea.getWidth() / 2 - CARD_WIDTH / 2,
                                    topPos.y - frameOrigin.y + opponentArea.getHeight() - 40
                            );
                        } catch (Exception e) {
                            targetPos = new Point(getWidth() / 2, 120);
                        }
                    } else {
                        // Bot a DESTRA
                        try {
                            Point rightPos = rightBotPanel.getLocationOnScreen();
                            targetPos = new Point(
                                    rightPos.x - frameOrigin.x + rightBotPanel.getWidth() / 2 - CARD_WIDTH / 2,
                                    rightPos.y - frameOrigin.y + rightBotPanel.getHeight() / 2 - CARD_HEIGHT / 2
                            );
                        } catch (Exception e) {
                            targetPos = new Point(getWidth() - 150, getHeight() / 2);
                        }
                    }
                }
            }

            // Assicura che ci sia sempre una posizione valida
            if (targetPos == null) {
                targetPos = new Point(getWidth() / 2, getHeight() / 2);
            }

            positions[i] = targetPos;
        }

        return positions;
    }

    private void animateDealingCardFlight(JLabel card, int startX, int startY,
                                          int endX, int endY, int playerIndex) {
        final int steps = 24;
        final int delay = 45;  // 40ms per step = 480ms totale

        Timer flyTimer = new Timer(delay, null);
        final int[] step = {0};

        // Calcola punto di controllo per curva
        int midX = (startX + endX) / 2 + (playerIndex % 2 == 0 ? -30 : 30); // Varia la curva
        int midY = (startY + endY) / 2 - 40;

        flyTimer.addActionListener(e -> {
            step[0]++;
            double t = (double) step[0] / steps;

            // Easing veloce (ease-out)
            double easedT = 1 - Math.pow(1 - t, 3);

            // Curva di Bezier
            double currentX = Math.pow(1 - easedT, 2) * startX +
                    2 * (1 - easedT) * easedT * midX +
                    Math.pow(easedT, 2) * endX;

            double currentY = Math.pow(1 - easedT, 2) * startY +
                    2 * (1 - easedT) * easedT * midY +
                    Math.pow(easedT, 2) * endY;

            // Scaling durante il volo
            double scale = 1.0 + (Math.sin(t * Math.PI) * 0.15); // Ingrandisce e rimpicciolisce
            int scaledWidth = (int) (CARD_WIDTH * scale);
            int scaledHeight = (int) (CARD_HEIGHT * scale);

            card.setBounds(
                    (int) currentX - (scaledWidth - CARD_WIDTH) / 2,
                    (int) currentY - (scaledHeight - CARD_HEIGHT) / 2,
                    scaledWidth,
                    scaledHeight
            );

            // Fade out graduale alla fine
            if (t > 0.8) {
                float alpha = 1f - ((float)(t - 0.8) / 0.2f) * 0.7f;
                card.setEnabled(alpha > 0.3f);
            }

            if (step[0] >= steps) {
                flyTimer.stop();
                // Rimuovi la carta volante dopo l'animazione
                Container parent = card.getParent();
                if (parent != null) {
                    parent.remove(card);
                    parent.repaint();
                }
            }
        });

        flyTimer.start();
    }

    private JPanel createAnimationOverlay() {
        JPanel overlay = new JPanel() {
            private float alpha = 0f;
            private Timer fadeTimer;

            {
                fadeTimer = new Timer(20, e -> {
                    alpha += 0.05f;
                    if (alpha >= 1f) {
                        alpha = 1f;
                        fadeTimer.stop();
                    }
                    repaint();
                });
                fadeTimer.start();
            }

            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, Math.min(1f, alpha)));
                g2d.setColor(new Color(0, 0, 0, 180));
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        overlay.setLayout(null);
        overlay.setOpaque(false);
        overlay.setBounds(0, 0, getWidth(), getHeight());
        return overlay;
    }


    /**
     * Mostra l'animazione realistica di pescata carta dal mazzo alla mano del giocatore.
     */
    public void showDrawAnimationToPlayerHand(Giocatore player, Runnable onComplete) {
        SwingUtilities.invokeLater(() -> {
            // Trova il mazzo sul tavolo
            JLabel deckLabel = findDeckLabel();
            if (deckLabel == null) {
                if (onComplete != null) onComplete.run();
                return;
            }

            // Posizione di partenza: mazzo
            Point deckOnScreen = deckLabel.getLocationOnScreen();
            Point frameOrigin = getLocationOnScreen();

            int startX = deckOnScreen.x - frameOrigin.x;
            int startY = deckOnScreen.y - frameOrigin.y;

            // Posizione di destinazione: mano del giocatore
            Point destPoint = getPlayerHandDestination(player);
            if (destPoint == null) {
                if (onComplete != null) onComplete.run();
                return;
            }

            int destX = destPoint.x - frameOrigin.x;
            int destY = destPoint.y - frameOrigin.y;

            // Crea overlay per l'animazione
            JPanel glassPane = (JPanel) getGlassPane();
            glassPane.setLayout(null);
            glassPane.setVisible(true);

            // Crea la carta che "vola" dal mazzo
            Image cardBackImg = CardImageLoader.getScaledCardBackImage(CARD_WIDTH, CARD_HEIGHT);
            JLabel flyingCard = new JLabel(new ImageIcon(cardBackImg));
            flyingCard.setBounds(startX, startY, CARD_WIDTH, CARD_HEIGHT);
            glassPane.add(flyingCard);
            glassPane.repaint();

            // Anima il volo con curva realistica
            animateCardFlightCurved(flyingCard, startX, startY, destX, destY, () -> {
                // Cleanup
                glassPane.remove(flyingCard);
                glassPane.setVisible(false);
                glassPane.repaint();

                if (onComplete != null) onComplete.run();
            });
        });
    }

    /**
     * Trova il label del mazzo sul tavolo.
     */
    private JLabel findDeckLabel() {
        for (Component comp : tableOval.getComponents()) {
            if (comp instanceof JLabel label) {
                if (label.getToolTipText() != null &&
                        label.getToolTipText().contains("Carte nel mazzo")) {
                    return label;
                }
            }
        }
        return null;
    }

    /**
     * Ottiene la posizione di destinazione della carta pescata per un giocatore.
     */
    private Point getPlayerHandDestination(Giocatore player) {
        if (player == humanPlayer) {
            // Destinazione: centro della mano del giocatore umano
            Point handPos = playerHandPanel.getLocationOnScreen();
            return new Point(
                    handPos.x + playerHandPanel.getWidth() / 2,
                    handPos.y + playerHandPanel.getHeight() / 2
            );
        } else {
            // Destinazione: area delle carte del bot
            int playerIdx = players.indexOf(player);
            if (playerIdx < 0) return null;

            int humanIdx = players.indexOf(humanPlayer);
            int relativePos = (playerIdx - humanIdx + players.size()) % players.size();

            if (players.size() == 2) {
                // Bot in alto
                Point opponentPos = opponentArea.getLocationOnScreen();
                return new Point(
                        opponentPos.x + opponentArea.getWidth() / 2,
                        opponentPos.y + opponentArea.getHeight() / 2
                );
            } else {
                // 3-4 giocatori
                if (relativePos == 1) {
                    // Bot sinistro
                    Point leftPos = leftBotPanel.getLocationOnScreen();
                    return new Point(
                            leftPos.x + leftBotPanel.getWidth() / 2,
                            leftPos.y + leftBotPanel.getHeight() / 2
                    );
                } else if (relativePos == players.size() - 1) {
                    // Bot destro
                    Point rightPos = rightBotPanel.getLocationOnScreen();
                    return new Point(
                            rightPos.x + rightBotPanel.getWidth() / 2,
                            rightPos.y + rightBotPanel.getHeight() / 2
                    );
                } else {
                    // Bot in alto
                    Point topPos = opponentArea.getLocationOnScreen();
                    return new Point(
                            (int) (topPos.x + topPos.getY() / 2),
                            topPos.y + opponentArea.getHeight() / 2
                    );
                }
            }
        }
    }



    public void showGameOver(String result) {
        SwingUtilities.invokeLater(() -> {
            // remove player's cards so overlay is clean
            removePlayerCardsFromLayer();

            JPanel overlay = new JPanel() {
                float alpha = 0f;

                {
                    Timer fadeIn = new Timer(18, null);
                    fadeIn.addActionListener(e -> {
                        alpha += 0.07f;
                        if (alpha >= 1f) {
                            alpha = 1f;
                            fadeIn.stop();
                        }
                        repaint();
                    });
                    fadeIn.start();
                }

                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, Math.min(1f, alpha) * 0.92f));
                    g2.setColor(new Color(0,0,0,210));
                    g2.fillRect(0, 0, getWidth(), getHeight());
                    g2.dispose();
                }
            };
            overlay.setLayout(new GridBagLayout());
            overlay.setOpaque(false);
            overlay.setBounds(0, 0, getWidth(), getHeight());

            JPanel content = new JPanel();
            content.setOpaque(false);
            content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
            content.setBorder(BorderFactory.createEmptyBorder(28, 44, 28, 44));

            JLabel title = new JLabel("🏁 Partita Terminata");
            title.setForeground(TEXT_GOLD);
            title.setFont(new Font("Serif", Font.BOLD, 33));
            title.setAlignmentX(Component.CENTER_ALIGNMENT);

            JLabel resLabel = new JLabel("<html><div style='text-align:center;'>" + result + "</div></html>");
            resLabel.setForeground(Color.WHITE);
            resLabel.setFont(new Font("Arial", Font.PLAIN, 16));
            resLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            resLabel.setBorder(BorderFactory.createEmptyBorder(15, 5, 25, 5));

            JButton backButton = new JButton("Torna al Menu");
            backButton.setFont(new Font("Arial", Font.BOLD, 18));
            backButton.setBackground(new Color(27, 155, 95));
            backButton.setForeground(Color.WHITE);
            backButton.setFocusPainted(false);
            backButton.setAlignmentX(Component.CENTER_ALIGNMENT);
            backButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            backButton.addActionListener(e -> {
                Timer fadeOut = new Timer(16, null);
                fadeOut.addActionListener(e2 -> {
                    overlay.setVisible(false);
                    getGlassPane().setVisible(false);
                    controller.onReturnToMenu();
                    fadeOut.stop();
                });
                fadeOut.start();
            });

            content.add(title);
            content.add(resLabel);
            content.add(Box.createVerticalStrut(24));
            content.add(backButton);

            overlay.add(content, new GridBagConstraints());
            JPanel glass = (JPanel) getGlassPane();
            glass.setLayout(null);
            glass.removeAll();
            glass.add(overlay);
            glass.setVisible(true);
            overlay.setVisible(true);
            overlay.repaint();

            playButton.setEnabled(false);
            statusLabel.setText("");
        });
    }

    public void fadeIn() {
        Timer fadeTimer = new Timer(16, null);
        final float[] alpha = {0.0f};
        fadeTimer.addActionListener(e -> {
            alpha[0] += 0.05f;
            if (alpha[0] >= 1.0f) {
                alpha[0] = 1.0f;
                fadeTimer.stop();
            }
            setOpacity(alpha[0]);
        });
        fadeTimer.start();
    }
}