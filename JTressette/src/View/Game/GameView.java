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

/**
 * GameViewSwing: Swing-based view for the card game following MVC pattern.
 * Displays the table, player's hand, opponent hands (face down), and cards played.
 * Styled like an online poker server with a green felt table.
 * Uses actual card images from src/res/Cards/.
 *
 * Modifiche principali in questa versione:
 * - VerticalCardStackPanel per disporre carte verticalmente dal basso verso l'alto.
 * - Top opponent (quando visualizzato verticalmente) usa la stessa dimensione e comportamento
 *   dei bot laterali; lo spazio riservato dipende dal numero di carte (preferred size calcolata).
 * - left/right panels vengono adattati per tener conto della larghezza delle carte laterali.
 */
public class GameView extends JFrame {

    // Colors inspired by poker table felt
    private static final Color FELT_GREEN = new Color(26, 117, 65);
    private static final Color FELT_DARK = new Color(18, 85, 47);
    private static final Color FELT_BORDER = new Color(100, 70, 40);
    private static final Color TEXT_GOLD = new Color(255, 215, 0);
    private static final Color TEXT_WHITE = Color.WHITE;

    // Dimensioni carte anche dinamico
    private static final int CARD_WIDTH = 70;
    private static final int CARD_HEIGHT = 100;
    private static final int HAND_GAP = 5; // gap between hand cards
    private static final int HAND_CARD_MIN_WIDTH = 45;
    private static final int HAND_CARD_MAX_WIDTH = 85;
    private int handCardWidth = 60;  // default
    private int handCardHeight = (int) Math.round(handCardWidth * ((double) CARD_HEIGHT / CARD_WIDTH));

    // Side (bot) card size (will be computed relative to handCardWidth)
    private int sideCardWidth = Math.max(36, (int) (handCardWidth * 0.78));
    private int sideCardHeight = (int) Math.round(sideCardWidth * ((double) CARD_HEIGHT / CARD_WIDTH));

    // Table sizing constraints
    private static final int TABLE_MIN_W = 520;
    private static final int TABLE_MIN_H = 260;
    private static final int TABLE_MAX_W = 760;
    private static final int TABLE_MAX_H = 420;

    // Animation constants
    private static final int ANIMATION_DELAY_MS = 80;
    private static final int CARD_FLY_DURATION_MS = 200;
    private static final double CARD_ARC_HEIGHT = 30.0;
    private static final double CARD_SCALE_FACTOR = 0.15;

    private final GameState gameState;
    private final GiocatoreUmano humanPlayer;
    private final GameController controller;

    // UI components that need updating
    private JPanel playerHandPanel;
    private JScrollPane handScrollPane;
    private JPanel opponentArea;
    private JPanel leftBotPanel;
    private JPanel rightBotPanel;
    private JLabel statusLabel;
    private JLabel scoreLabel;
    private JPanel logArea;
    private JButton playButton;
    private JLabel wonCardsLabel;
    private JPanel tableOval; // actual table panel

    // layered pane reference for placing player's cards directly on screen (not inside playerHandPanel)
    private JLayeredPane layeredPaneRef;

    // CardPanels representing the player's hand (they live on layeredPaneRef)
    private final List<CardPanel> cardPanels = new ArrayList<>();
    private List<Giocatore> players;

    // gap used for hand cards (calculated)
    private int handCardGapUsed = HAND_GAP;

    public GameView(GameState gameState, GiocatoreUmano humanPlayer, GameController controller) {
        super("JTressette - Partita in Corso");
        this.gameState = gameState;
        this.humanPlayer = humanPlayer;
        this.controller = controller;
        this.players = gameState.getPlayers();
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

        int tableW = Math.max(TABLE_MIN_W, Math.min(TABLE_MAX_W, (int) (availableW * 0.78)));
        int tableH = Math.max(TABLE_MIN_H, Math.min(TABLE_MAX_H, (int) (availableH * 0.58)));

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
        area.setPreferredSize(new Dimension(0, 150));
        return area;
    }

    /**
     * Create opponent box. If isVertical==true it will contain a VerticalCardStackPanel
     * which arranges cards from bottom-to-top and adapts its preferred size to the
     * number of cards using sideCardWidth/sideCardHeight as fixed card size.
     */
    private JPanel createOpponentBox(Giocatore player, boolean isVertical) {
        JPanel box = new JPanel();
        box.setOpaque(false);
        box.setLayout(new BoxLayout(box, isVertical ? BoxLayout.Y_AXIS : BoxLayout.X_AXIS));

        JLabel nameLabel = new JLabel(player.getName());
        nameLabel.setForeground(new Color(255, 215, 0)); // Gold color
        nameLabel.setFont(new Font("Georgia", Font.BOLD, 14));
        nameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        nameLabel.setAlignmentY(Component.CENTER_ALIGNMENT);

        List<Cards> hand = gameState.getHand(player);
        int handSize = hand.size();

        if (isVertical) {
            // Vertical stack: cards bottom-to-top, with fixed sideCardWidth/sideCardHeight
            int overlap = Math.max(8, sideCardHeight / 3); // overlap amount (how much cards "rise" above previous)
            VerticalCardStackPanel vstack = new VerticalCardStackPanel(sideCardWidth, sideCardHeight, overlap);

            // Add card backs (they will be laid out by vstack.doLayout)
            Image cardBackImg = CardImageLoader.getScaledCardBackImage(sideCardWidth, sideCardHeight);
            for (int i = 0; i < handSize; i++) {
                JLabel cardBack = new JLabel(new ImageIcon(cardBackImg));
                cardBack.setOpaque(false);
                vstack.add(cardBack);
            }

            // If no cards, add an invisible placeholder to keep box consistent
            if (handSize == 0) {
                JPanel placeholder = new JPanel();
                placeholder.setOpaque(false);
                placeholder.setPreferredSize(new Dimension(sideCardWidth, sideCardHeight));
                vstack.add(placeholder);
            }

            // assemble vertical box
            box.add(Box.createVerticalStrut(5));
            box.add(nameLabel);
            box.add(Box.createVerticalStrut(8));
            box.add(vstack);
            box.add(Box.createVerticalStrut(5));

            // info panel (won cards etc.)
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
                JLabel scoreLabel = new JLabel(" | Punti: " + gameState.getScaledScoreString(player));
                scoreLabel.setForeground(new Color(255, 215, 0)); // Gold
                scoreLabel.setFont(new Font("Georgia", Font.BOLD, 11));
                infoPanel.add(scoreLabel);
            }

            box.add(infoPanel);
            box.add(Box.createVerticalStrut(5));
        } else {
            // Horizontal cards (existing behavior)
            int baseCardWidth = 55;
            int baseCardHeight = 80;
            int overlap = -25; // negative in FlowLayout gap to cause visual overlap

            if (handSize > 6) {
                float scaleFactor = Math.min(1.0f, 6.0f / handSize);
                baseCardWidth = (int) (baseCardWidth * scaleFactor);
                baseCardHeight = (int) (baseCardHeight * scaleFactor);
            }

            JPanel cardsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, overlap, 3));
            cardsPanel.setOpaque(false);

            int cardWidth = baseCardWidth;
            int cardHeight = baseCardHeight;

            Image cardBackImg = CardImageLoader.getScaledCardBackImage(cardWidth, cardHeight);
            for (int i = 0; i < handSize; i++) {
                JLabel cardBack = new JLabel(new ImageIcon(cardBackImg));
                cardBack.setPreferredSize(new Dimension(cardWidth + 5, cardHeight + 5));
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
                sc.setForeground(new Color(255, 215, 0)); // Gold
                sc.setFont(new Font("Georgia", Font.BOLD, 11));
                infoPanel.add(sc);
            }

            // Horizontal layout - wrap in a container for better centering
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
        private final Cards card;
        private boolean isHovered = false;
        private boolean isAnimating = false;
        private float animationProgress = 0f;
        private final Image cardImage;
        private final int drawWidth;
        private final int drawHeight;
        private final int handIndex; // index in player's hand

        public CardPanel(Cards card, int index, boolean isPlayable, int drawWidth, int drawHeight) {
            this.card = card;
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
            updateTableCards();
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
            layeredPaneRef.add(cp, Integer.valueOf(JLayeredPane.PALETTE_LAYER));
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

        if (numPlayers == 2) {
            for (int i = 0; i < numPlayers; i++) {
                if (i == humanIndex) {
                    positions[i] = new int[]{centerX, h - CARD_HEIGHT - 12};
                } else {
                    positions[i] = new int[]{centerX, 12};
                }
            }
        } else if (numPlayers == 3) {
            for (int i = 0; i < numPlayers; i++) {
                int relativePos = (i - humanIndex + 3) % 3;
                if (relativePos == 0) {
                    positions[i] = new int[]{centerX, h - CARD_HEIGHT - 12};
                } else if (relativePos == 1) {
                    positions[i] = new int[]{12, centerY - CARD_HEIGHT / 2};
                } else {
                    positions[i] = new int[]{centerX, 12};
                }
            }
        } else if (numPlayers == 4) {
            for (int i = 0; i < numPlayers; i++) {
                int relativePos = (i - humanIndex + 4) % 4;
                if (relativePos == 0) {
                    positions[i] = new int[]{centerX, h - CARD_HEIGHT - 12};
                } else if (relativePos == 1) {
                    positions[i] = new int[]{12, centerY - CARD_HEIGHT / 2};
                } else if (relativePos == 2) {
                    positions[i] = new int[]{centerX, 12};
                } else {
                    positions[i] = new int[]{w - CARD_WIDTH - 12, centerY - CARD_HEIGHT / 2};
                }
            }
        }

        return positions;
    }

    private void updateTableCards() {
        tableOval.removeAll();

        JLabel deck = createDeckImage();
        int dw = deck.getPreferredSize().width;
        int dh = deck.getPreferredSize().height;
        int deckX = Math.max(0, (int)(tableOval.getWidth() * 0.20));
        int deckY = (tableOval.getHeight() - dh) / 2;
        deck.setBounds(deckX, deckY, dw, dh);
        tableOval.add(deck);

        int nPlayers = this.players.size();
        int[][] positions = computeSlotPositions();

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

        for (int i = 0; i < nPlayers; i++) {
            Giocatore player = this.players.get(i);
            JLabel name = new JLabel(player.getName());
            name.setForeground(TEXT_WHITE);
            name.setFont(new Font("Arial", Font.BOLD, 13));
            int nx = positions[i][0] - 10;
            int ny = positions[i][1] + CARD_HEIGHT + 8;
            name.setBounds(Math.max(2, nx), ny, 140, 18);
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

        // adjust left/right panel preferred width according to side card width (+padding)
        int sidePanelW = sideCardWidth + 24;
        leftBotPanel.setPreferredSize(new Dimension(sidePanelW, leftBotPanel.getHeight()));
        rightBotPanel.setPreferredSize(new Dimension(sidePanelW, rightBotPanel.getHeight()));

        int numPlayers = players.size();
        int areaWidth = opponentArea.getWidth() > 0 ? opponentArea.getWidth() : 1000;
        int areaHeight = opponentArea.getHeight() > 0 ? opponentArea.getHeight() : 150;

        int humanIndex = players.indexOf(humanPlayer);

        for (int i = 0; i < numPlayers; i++) {
            if (i == humanIndex) continue;

            Giocatore player = players.get(i);
            boolean isVertical = false;

            JPanel targetPanel = opponentArea;
            int x = 0, y = 0;

            if (numPlayers == 2) {
                // top center - keep horizontal
                isVertical = false;
                x = areaWidth / 2 - 100;
                y = 10;
            } else if (numPlayers == 3) {
                int relativePos = (i - humanIndex + 3) % 3;
                if (relativePos == 1) {
                    // left player - vertical cards on side panel
                    isVertical = true;
                    targetPanel = leftBotPanel;
                } else { // relativePos == 2 (top)
                    // top player - make vertical as well and place in opponentArea
                    isVertical = true;
                    targetPanel = opponentArea;
                }
            } else if (numPlayers == 4) {
                int relativePos = (i - humanIndex + 4) % 4;
                if (relativePos == 1) {
                    isVertical = true;
                    targetPanel = leftBotPanel;
                } else if (relativePos == 2) {
                    // top player - vertical stack in top area
                    isVertical = true;
                    targetPanel = opponentArea;
                } else if (relativePos == 3) {
                    isVertical = true;
                    targetPanel = rightBotPanel;
                }
            }

            JPanel opponentBox = createOpponentBox(player, isVertical);

            if (targetPanel == leftBotPanel || targetPanel == rightBotPanel) {
                opponentBox.setAlignmentX(Component.CENTER_ALIGNMENT);
                targetPanel.add(Box.createVerticalGlue());
                targetPanel.add(opponentBox);
                targetPanel.add(Box.createVerticalGlue());
            } else {
                // targetPanel == opponentArea (absolute)
                if (isVertical) {
                    // place vertical stack centered at top; its preferred size depends on card count
                    Dimension pref = opponentBox.getPreferredSize();
                    int prefW = pref.width > 0 ? pref.width : (sideCardWidth + 12);
                    int prefH = pref.height > 0 ? pref.height : 160;
                    int posX = Math.max(8, (areaWidth - prefW) / 2);
                    int posY = 10; // top margin
                    opponentBox.setBounds(posX, posY, prefW, prefH);
                    opponentArea.add(opponentBox);
                } else {
                    // legacy horizontal top
                    opponentBox.setBounds(x, y, 220, 150);
                    opponentArea.add(opponentBox);
                }
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

    public void showCardPlayed() {
        SwingUtilities.invokeLater(this::updateTableCards);
    }

    /**
     * Animate a card being played from hand to table position
     */
    public void animateCardPlay(Giocatore player, int cardIndex, Runnable onComplete) {
        SwingUtilities.invokeLater(() -> {
            Point startPos;
            if (player == humanPlayer && cardIndex >= 0 && cardIndex < cardPanels.size()) {
                CardPanel cardPanel = cardPanels.get(cardIndex);
                startPos = cardPanel.getLocationOnScreen();
            } else {
                Point tablePos = tableOval.getLocationOnScreen();
                int[][] positions = computeSlotPositions();
                int playerIdx = players.indexOf(player);
                if (playerIdx >= 0) {
                    startPos = new Point(
                            tablePos.x + positions[playerIdx][0],
                            tablePos.y + positions[playerIdx][1]
                    );
                } else {
                    startPos = new Point(
                            tablePos.x + tableOval.getWidth() / 2,
                            tablePos.y + tableOval.getHeight() / 2
                    );
                }
            }

            updateTableCards();

            if (onComplete != null) {
                Timer delay = new Timer(100, e -> onComplete.run());
                delay.setRepeats(false);
                delay.start();
            }
        });
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

        int labelW = Math.min(320, tableOval.getWidth() - 10);
        int labelH = 26;
        int x = tableOval.getWidth() - labelW - 28;
        int y = 18;
        winnerLabel.setBounds(x, y, labelW, labelH);

        tableOval.add(winnerLabel);
        tableOval.revalidate();
        tableOval.repaint();

        Timer removeTimer = new Timer(800, _ -> {
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

        fadeTimer.addActionListener(_ -> {
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
            JPanel animationOverlay = getPanel();

            JPanel glassPane = (JPanel) getGlassPane();
            glassPane.setLayout(null);
            glassPane.add(animationOverlay);
            glassPane.setVisible(true);

            int centerX = getWidth() / 2 - CARD_WIDTH / 2;
            int centerY = getHeight() / 2 - CARD_HEIGHT / 2;

            int numPlayers = players.size();
            int[][] posBase = computeSlotPositions();
            int[][] targetPositions = new int[numPlayers][2];

            Point tableOnScreen = tableOval.getLocationOnScreen();
            Point frameOnScreen = this.getLocationOnScreen();
            int offsetX = tableOnScreen.x - frameOnScreen.x;
            int offsetY = tableOnScreen.y - frameOnScreen.y;

            for (int i = 0; i < numPlayers; i++) {
                targetPositions[i][0] = posBase[i][0] + offsetX;
                targetPositions[i][1] = posBase[i][1] + offsetY;
            }

            JLabel dealingLabel = new JLabel("Distribuzione carte...");
            dealingLabel.setFont(new Font("Georgia", Font.BOLD, 28));
            dealingLabel.setForeground(TEXT_GOLD);
            dealingLabel.setHorizontalAlignment(SwingConstants.CENTER);
            int labelWidth = 400;
            int labelHeight = 40;
            dealingLabel.setBounds((getWidth() - labelWidth) / 2, centerY - 80, labelWidth, labelHeight);
            animationOverlay.add(dealingLabel);

            Image cardBackImg = CardImageLoader.getScaledCardBackImage(CARD_WIDTH, CARD_HEIGHT);
            for (int i = 0; i < 5; i++) {
                JLabel deckCard = new JLabel(new ImageIcon(cardBackImg));
                deckCard.setBounds(centerX + i * 2, centerY - i * 2, CARD_WIDTH, CARD_HEIGHT);
                animationOverlay.add(deckCard);
            }

            animationOverlay.repaint();

            int cardsPerPlayer = 10;
            Timer dealTimer = new Timer(ANIMATION_DELAY_MS, null);
            final int[] currentCard = {0};
            final int totalCards = numPlayers * cardsPerPlayer;

            dealTimer.addActionListener(evt -> {
                if (currentCard[0] >= totalCards) {
                    dealTimer.stop();
                    dealingLabel.setText("Pronto!");
                    Timer fadeOutTimer = new Timer(800, evt2 -> {
                        glassPane.remove(animationOverlay);
                        glassPane.setVisible(false);
                        glassPane.repaint();
                        if (onComplete != null) onComplete.run();
                    });
                    fadeOutTimer.setRepeats(false);
                    fadeOutTimer.start();
                    return;
                }

                int playerIndex = currentCard[0] % numPlayers;
                int targetX = targetPositions[playerIndex][0];
                int targetY = targetPositions[playerIndex][1];

                int cardNum = currentCard[0] / numPlayers;
                int offset = cardNum * 12;
                targetX += offset;

                JLabel flyingCard = new JLabel(new ImageIcon(cardBackImg));
                flyingCard.setBounds(centerX, centerY, CARD_WIDTH, CARD_HEIGHT);
                animationOverlay.add(flyingCard);
                animationOverlay.setComponentZOrder(flyingCard, 0);
                animationOverlay.repaint();

                animateCardFlight(flyingCard, centerX, centerY, targetX, targetY);
                currentCard[0]++;
            });

            dealTimer.start();
        });
    }

    private JPanel getPanel() {
        JPanel animationOverlay = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(new Color(0, 0, 0, 150));
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        animationOverlay.setLayout(null);
        animationOverlay.setOpaque(false);
        animationOverlay.setBounds(0, 0, getWidth(), getHeight());
        return animationOverlay;
    }

    public void showDrawAnimationToPlayerHand(Giocatore player, Runnable onComplete) {
        SwingUtilities.invokeLater(() -> {
            JLabel deckLabel = null;
            for (Component comp : tableOval.getComponents()) {
                if (comp instanceof JLabel && ((JLabel) comp).getToolTipText() != null
                        && ((JLabel) comp).getToolTipText().contains("Carte nel mazzo")) {
                    deckLabel = (JLabel) comp;
                    break;
                }
            }
            if (deckLabel == null) {
                if (onComplete != null) onComplete.run();
                return;
            }

            Point tableOnScreen = tableOval.getLocationOnScreen();
            Point deckOnScreen = deckLabel.getLocationOnScreen();
            int deckX = deckOnScreen.x - tableOnScreen.x + deckLabel.getWidth() / 2 - CARD_WIDTH / 2;
            int deckY = deckOnScreen.y - tableOnScreen.y + deckLabel.getHeight() / 2 - CARD_HEIGHT / 2;

            int destX, destY;

            if (player == humanPlayer) {
                // Use the center of the visible playerHandPanel as destination so visual position is unchanged
                Point handOnScreen = playerHandPanel.getLocationOnScreen();
                destX = handOnScreen.x - tableOnScreen.x + playerHandPanel.getWidth() / 2 - CARD_WIDTH / 2;
                destY = handOnScreen.y - tableOnScreen.y + playerHandPanel.getHeight() / 2 - CARD_HEIGHT / 2;
            } else {
                int idx = players.indexOf(player);
                int[][] positions = computeSlotPositions();
                destX = positions[idx][0];
                destY = positions[idx][1];
            }

            JPanel overlay = new JPanel(null);
            overlay.setOpaque(false);
            overlay.setBounds(0, 0, tableOval.getWidth(), tableOval.getHeight());

            Image cardBackImg = CardImageLoader.getScaledCardBackImage(CARD_WIDTH, CARD_HEIGHT);
            JLabel flyingCard = new JLabel(new ImageIcon(cardBackImg));
            flyingCard.setBounds(deckX, deckY, CARD_WIDTH, CARD_HEIGHT);
            overlay.add(flyingCard);

            tableOval.add(overlay, 0);
            tableOval.setComponentZOrder(overlay, 0);
            tableOval.repaint();

            animateCardFlight(flyingCard, deckX, deckY, destX, destY);

            Timer cleanup = new Timer(CARD_FLY_DURATION_MS + 40, e -> {
                tableOval.remove(overlay);
                tableOval.repaint();
                if (onComplete != null) onComplete.run();
            });
            cleanup.setRepeats(false);
            cleanup.start();
        });
    }

    private void animateCardFlight(JLabel card, int startX, int startY, int endX, int endY) {
        final int steps = 15;
        final int delay = GameView.CARD_FLY_DURATION_MS / steps;

        Timer flyTimer = new Timer(delay, null);
        final int[] step = {0};

        flyTimer.addActionListener(evt -> {
            step[0]++;
            double t = (double) step[0] / steps;
            double easedT = 1 - Math.pow(1 - t, 3);
            int currentX = (int) (startX + (endX - startX) * easedT);
            int currentY = (int) (startY + (endY - startY) * easedT);
            double arc = Math.sin(t * Math.PI) * CARD_ARC_HEIGHT;
            currentY -= (int) arc;
            double scale = 1.0 - (t * CARD_SCALE_FACTOR);
            int scaledWidth = (int) (CARD_WIDTH * scale);
            int scaledHeight = (int) (CARD_HEIGHT * scale);
            card.setBounds(currentX, currentY, scaledWidth, scaledHeight);
            card.getParent().repaint();

            if (step[0] >= steps) {
                flyTimer.stop();
            }
        });

        flyTimer.start();
    }

    public void showGameOver(String result) {
        SwingUtilities.invokeLater(() -> {
            // remove player's cards so overlay is clean
            removePlayerCardsFromLayer();

            JPanel overlay = new JPanel() {
                float alpha = 0f;

                {
                    Timer fadeIn = new Timer(18, null);
                    fadeIn.addActionListener(evt -> {
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
                fadeOut.addActionListener(evt -> {
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