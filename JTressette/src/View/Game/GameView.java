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
 */
public class GameView extends JFrame {

    // Colors inspired by poker table felt
    private static final Color FELT_GREEN = new Color(26, 117, 65);
    private static final Color FELT_DARK = new Color(18, 85, 47);
    private static final Color FELT_BORDER = new Color(100, 70, 40);
    private static final Color TEXT_GOLD = new Color(255, 215, 0);
    private static final Color TEXT_WHITE = Color.WHITE;

    // Card dimensions (base)
    private static final int CARD_WIDTH = 70;
    private static final int CARD_HEIGHT = 100;
    private static final int SMALL_CARD_WIDTH = 50;
    private static final int SMALL_CARD_HEIGHT = 75;

    // Hand dynamic sizing
    private static final int HAND_GAP = 5; // gap between hand cards
    private static final int HAND_CARD_MIN_WIDTH = 45;
    private static final int HAND_CARD_MAX_WIDTH = 85;
    private int handCardWidth = 60;  // default
    private int handCardHeight = (int) Math.round(handCardWidth * ((double) CARD_HEIGHT / CARD_WIDTH));

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
    private JLabel statusLabel;
    private JLabel scoreLabel;
    private JPanel logArea;
    private JButton playButton;
    private JLabel wonCardsLabel;
    private JPanel tableOval; // actual table panel

    private final List<CardPanel> cardPanels = new ArrayList<>();
    private List<Giocatore> players;

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

        // Start with opacity 0 for fade-in effect
        setOpacity(0.0f);

        // Main panel with gradient background
        JPanel mainPanel = new GradientPanel();
        mainPanel.setLayout(new BorderLayout(10, 10));

        // Top: Opponent area (face-down cards)
        opponentArea = createOpponentArea();
        mainPanel.add(opponentArea, BorderLayout.NORTH);

        // Center: Table with played cards
        JPanel tableCenter = createTableCenter();
        mainPanel.add(tableCenter, BorderLayout.CENTER);

        // Bottom: Player's hand
        JPanel bottomArea = createPlayerArea();
        mainPanel.add(bottomArea, BorderLayout.SOUTH);

        // Right: Log/info panel\
        // Crea il pannello delle informazioni solo se abilitato nelle impostazioni

        JPanel rightPanel = createInfoPanel();
        mainPanel.add(rightPanel, BorderLayout.EAST);

        setContentPane(mainPanel);

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
        // Compute available content area size (exclude right info panel roughly)
        int contentW = getContentPane().getWidth();
        int contentH = getContentPane().getHeight();

        // Check if info panel is visible based on settings
        MenuImpostazioni settings = MenuImpostazioni.getInstance();
        boolean infoPanelVisible = settings.isShowScore() || settings.isShowMessages();
        int rightPanelW = infoPanelVisible ? 220 : 20; // Smaller margin if panel not visible

        int availableW = Math.max(400, contentW - rightPanelW - 60); // margin
        int availableH = Math.max(300, contentH - 220); // top + hand area approx

        // Table size attempts to occupy ~55% of available height, clamped
        int tableW = Math.max(TABLE_MIN_W, Math.min(TABLE_MAX_W, (int) (availableW * 0.78)));
        int tableH = Math.max(TABLE_MIN_H, Math.min(TABLE_MAX_H, (int) (availableH * 0.58)));

        if (tableOval != null) {
            tableOval.setPreferredSize(new Dimension(tableW, tableH));
            tableOval.revalidate();
        }

        // Recompute hand card size so it fits into remaining area
        recomputeHandCardSize();
    }

    private void recomputeHandCardSize() {
        // figure available width inside center area (exclude info panel)
        int contentW = getContentPane().getWidth();

        // Check if info panel is visible
        MenuImpostazioni settings = MenuImpostazioni.getInstance();
        boolean infoPanelVisible = settings.isShowScore() || settings.isShowMessages();
        int rightPanelW = infoPanelVisible ? 220 : 20;

        int effectiveWidth = Math.max(320, contentW - rightPanelW - 60);

        int handCount = Math.max(1, gameState.getHand(humanPlayer).size());
        // Calculate card width to fit all cards without scrolling
        int totalGaps = (handCount + 1) * HAND_GAP;
        int availableForCards = effectiveWidth - totalGaps - 40; // extra margin
        int maxPerCard = availableForCards / handCount;

        handCardWidth = Math.max(HAND_CARD_MIN_WIDTH, Math.min(HAND_CARD_MAX_WIDTH, maxPerCard));
        double ratio = (double) CARD_HEIGHT / (double) CARD_WIDTH;
        handCardHeight = (int) Math.round(handCardWidth * ratio);

        if (playerHandPanel != null && handScrollPane != null) {
            // Disable scrolling - all cards should fit
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

    private JPanel createOpponentArea() {
        JPanel area = new JPanel();
        area.setOpaque(false);
        area.setLayout(null); // Use null layout for precise positioning
        area.setBorder(new EmptyBorder(20, 20, 10, 20));
        area.setPreferredSize(new Dimension(0, 150));
        return area;
    }

    private JPanel createOpponentBox(Giocatore player, boolean isVertical) {
        JPanel box = new JPanel();
        box.setOpaque(false);
        box.setLayout(new BoxLayout(box, isVertical ? BoxLayout.X_AXIS : BoxLayout.Y_AXIS));

        // Player name with improved styling
        JLabel nameLabel = new JLabel(player.getName());
        nameLabel.setForeground(new Color(255, 215, 0)); // Gold color
        nameLabel.setFont(new Font("Georgia", Font.BOLD, 14));
        nameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        nameLabel.setAlignmentY(Component.CENTER_ALIGNMENT);

        // Face-down cards panel using card back images
        // Dynamically size cards to ensure all are visible
        List<Cards> hand = gameState.getHand(player);
        int handSize = hand.size();

        // Calculate card size to fit all cards
        // For horizontal: max width depends on available space and overlap
        // For vertical: max height depends on available space and overlap
        int baseCardWidth = SMALL_CARD_WIDTH;
        int baseCardHeight = SMALL_CARD_HEIGHT;
        int overlap = isVertical ? -20 : -20; // Negative for overlapping cards

        // Adjust size if too many cards
        if (handSize > 5) {
            // Reduce size for larger hands
            float scaleFactor = Math.min(1.0f, 5.0f / handSize);
            baseCardWidth = (int)(baseCardWidth * scaleFactor);
            baseCardHeight = (int)(baseCardHeight * scaleFactor);
        }

        JPanel cardsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, isVertical ? 0 : overlap, isVertical ? overlap : 0));
        cardsPanel.setOpaque(false);

        int cardWidth = isVertical ? baseCardHeight : baseCardWidth;
        int cardHeight = isVertical ? baseCardWidth : baseCardHeight;
        Image cardBackImg = CardImageLoader.getScaledCardBackImage(cardWidth, cardHeight);
        for (int i = 0; i < handSize; i++) {
            JLabel cardBack = new JLabel(new ImageIcon(cardBackImg));
            cardBack.setPreferredSize(new Dimension(cardWidth + 5, cardHeight + 5));
            cardsPanel.add(cardBack);
        }

        // Score and won cards info panel
        JPanel infoPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));
        infoPanel.setOpaque(false);

        int wonCards = gameState.getWonCardsCount(player);
        JPanel opponentDeckIcon = createOpponentWonCardsDeckIcon(player);
        infoPanel.add(opponentDeckIcon);

        JLabel wonLabel = new JLabel("Carte: " + wonCards);
        wonLabel.setForeground(TEXT_GOLD);
        wonLabel.setFont(new Font("Arial", Font.PLAIN, 10));
        infoPanel.add(wonLabel);

        // Add score if enabled in settings
        MenuImpostazioni settings = MenuImpostazioni.getInstance();
        if (settings.isShowScore()) {
            JLabel scoreLabel = new JLabel(" | Punti: " + gameState.getScaledScoreString(player));
            scoreLabel.setForeground(new Color(255, 215, 0)); // Gold
            scoreLabel.setFont(new Font("Georgia", Font.BOLD, 11));
            infoPanel.add(scoreLabel);
        }

        if (isVertical) {
            box.add(Box.createHorizontalStrut(5));
            box.add(nameLabel);
            box.add(Box.createHorizontalStrut(5));
            box.add(cardsPanel);
            box.add(Box.createHorizontalStrut(5));
            box.add(infoPanel);
        } else {
            box.add(nameLabel);
            box.add(Box.createVerticalStrut(5));
            box.add(cardsPanel);
            box.add(Box.createVerticalStrut(3));
            box.add(infoPanel);
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
        // default size; recomputeLayoutSizes() may adjust
        tableOval.setPreferredSize(new Dimension(640, 320));
        tableOval.setLayout(null);

        // Note: Don't permanently add deckLabel here; updateTableCards will add the deck on each refresh.
        center.add(tableOval);
        return center;
    }


    // Funzione attraverso la quale si crea un mazzo al centro con il numero di carte rimanenti in esso
    private JLabel createDeckImage() {
        Image cardBackImg = CardImageLoader.getScaledCardBackImage(40, 70);
        JLabel deckLabel = new JLabel(new ImageIcon(cardBackImg));
        int cardsLeft = gameState.getDeck().remaining();
        deckLabel.setToolTipText("Carte nel mazzo: " + cardsLeft);
        JLabel numberLabel = new JLabel("" + cardsLeft, SwingConstants.CENTER);
        numberLabel.setForeground(TEXT_GOLD);
        numberLabel.setFont(new Font("Arial", Font.BOLD, 10));
        deckLabel.setLayout(new BorderLayout());
        deckLabel.add(numberLabel, BorderLayout.SOUTH);
        return deckLabel;
    }

    // Custom table panel with oval shape
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

        // Put handPanel in a wrapper - no scroll needed as cards auto-size
        handScrollPane = new JScrollPane(playerHandPanel, JScrollPane.VERTICAL_SCROLLBAR_NEVER, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        handScrollPane.setOpaque(false);
        handScrollPane.getViewport().setOpaque(false);
        handScrollPane.setBorder(null);
        handScrollPane.setAlignmentX(Component.CENTER_ALIGNMENT);
        handScrollPane.setPreferredSize(new Dimension(800, handCardHeight + 30)); // will be updated by recomputeHandCardSize()

        // Won cards indicator panel
        JPanel wonCardsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        wonCardsPanel.setOpaque(false);
        JPanel deckIcon = createWonCardsDeckIcon();
        wonCardsPanel.add(deckIcon);

        wonCardsLabel = new JLabel("Carte prese: 0");
        wonCardsLabel.setForeground(TEXT_GOLD);
        wonCardsLabel.setFont(new Font("Arial", Font.BOLD, 12));
        wonCardsPanel.add(wonCardsLabel);

        // Play button no longer needed - cards play on click
        playButton = new JButton("Gioca Carta");
        playButton.setVisible(false); // Hide the button
        playButton.setEnabled(false);

        area.add(playerLabel);
        area.add(Box.createVerticalStrut(6));
        area.add(wonCardsPanel);
        area.add(Box.createVerticalStrut(8));
        area.add(handScrollPane);
        area.add(Box.createVerticalStrut(8));

        // set a reasonable preferred height to avoid overlap
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
        // Check if info panel should be visible
        MenuImpostazioni settings = MenuImpostazioni.getInstance();
        if (!settings.isShowScore() && !settings.isShowMessages()) {
            // If both score and chat are disabled, return an empty panel
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

        // Mostra punteggio solo se abilitato nelle impostazioni

        scoreLabel = new JLabel("Punteggio: 0");
        scoreLabel.setForeground(new Color(255, 215, 0)); // Gold color
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

        JButton backButton = new JButton("Esci dalla Partita");
        backButton.setBackground(new Color(139, 0, 0));
        backButton.setForeground(Color.WHITE);
        backButton.setFocusPainted(false);
        backButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        backButton.addActionListener(e -> controller.onExitGame());

        panel.add(titleLabel);
        panel.add(Box.createVerticalStrut(15));
        panel.add(statusLabel);
        panel.add(Box.createVerticalStrut(10));

        // Add score only if enabled
        if (settings.isShowScore()) {
            panel.add(scoreLabel);
            panel.add(Box.createVerticalStrut(15));
        }

        // Add chat/log only if enabled
        if (settings.isShowMessages()) {
            panel.add(logTitle);
            panel.add(Box.createVerticalStrut(5));
            panel.add(logScroll);
        }

        panel.add(Box.createVerticalGlue());
        panel.add(backButton);

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
        private final Image cardImage;
        private final int drawWidth;
        private final int drawHeight;

        /**
         * Constructor with explicit draw size - useful for table cards (fixed size).
         */
        public CardPanel(Cards card, int index, boolean isPlayable, int drawWidth, int drawHeight) {
            this.card = card;
            this.drawWidth = drawWidth;
            this.drawHeight = drawHeight;
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
                        // Play card directly on click
                        int[] legalMoves = gameState.getLegalMoves(humanPlayer);
                        boolean isLegal = false;
                        for (int legal : legalMoves) {
                            if (legal == index) {
                                isLegal = true;
                                break;
                            }
                        }

                        if (isLegal) {
                            controller.onCardPlayed(index);
                        } else {
                            log("Mossa non valida! Devi seguire il seme se possibile.");
                        }
                    }
                });
                setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            }
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

            int offsetY = isHovered ? -8 : 0; // Lift card on hover

            // Shadow
            g2d.setColor(new Color(0, 0, 0, 80));
            g2d.fill(new RoundRectangle2D.Double(3, 3 + offsetY, drawWidth, drawHeight, 10, 10));

            // Draw card image
            if (cardImage != null) {
                g2d.drawImage(cardImage, 0, offsetY, drawWidth, drawHeight, this);
            }

            // Highlight border on hover
            if (isHovered) {
                g2d.setColor(TEXT_GOLD);
                g2d.setStroke(new BasicStroke(3));
                g2d.drawRoundRect(0, offsetY, drawWidth - 1, drawHeight - 1, 10, 10);
            }
        }

    }

    private String getRankSymbol(Cards.Rank rank) {
        return getString(rank);
    }

    public static String getString(Cards.Rank rank) {
        return switch (rank) {
            case ASSO -> "A";
            case DUE -> "2";
            case TRE -> "3";
            case QUATTRO -> "4";
            case CINQUE -> "5";
            case SEI -> "6";
            case SETTE -> "7";
            case ALFIERE -> "J";
            case CAVALLO -> "Q";
            case RE -> "K";
        };
    }

    private String getSuitSymbol(Cards.Segno segno) {
        return switch (segno) {
            case DENARA -> "♦";
            case SPADE -> "♠";
            case BASTONI -> "♣";
            case COPPE -> "♥";
        };
    }

    private Color getSuitColor(Cards.Segno segno) {
        return switch (segno) {
            case DENARA, COPPE -> Color.RED;
            case SPADE, BASTONI -> Color.BLACK;
        };
    }



    public void refresh() {
        SwingUtilities.invokeLater(() -> {
            updatePlayerHand();
            updateTableCards();
            updateOpponentArea();
            updateScores();
            updateStatus();
            updateWonCardsDisplay();
        });
    }

    private void updatePlayerHand() {
        recomputeHandCardSize(); // ensure sizes up-to-date

        playerHandPanel.removeAll();
        cardPanels.clear();

        List<Cards> hand = gameState.getHand(humanPlayer);
        int[] legalMoves = gameState.getLegalMoves(humanPlayer);
        Giocatore current = gameState.getCurrentPlayer();
        boolean isMyTurn = current == humanPlayer;

        for (int i = 0; i < hand.size(); i++) {
            Cards card = hand.get(i);
            boolean isLegal = false;
            for (int legal : legalMoves) {
                if (legal == i) {
                    isLegal = true;
                    break;
                }
            }
            CardPanel cardPanel = new CardPanel(card, i, isMyTurn && isLegal, handCardWidth, handCardHeight);
            cardPanels.add(cardPanel);
            playerHandPanel.add(cardPanel);
        }

        playerHandPanel.revalidate();
        playerHandPanel.repaint();
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
            // Human at bottom, opponent at top
            for (int i = 0; i < numPlayers; i++) {
                if (i == humanIndex) {
                    positions[i] = new int[]{centerX, h - CARD_HEIGHT - 12}; // bottom
                } else {
                    positions[i] = new int[]{centerX, 12}; // top
                }
            }
        } else if (numPlayers == 3) {
            // Fixed positions: human bottom, left, top (based on index)
            for (int i = 0; i < numPlayers; i++) {
                int relativePos = (i - humanIndex + 3) % 3;
                if (relativePos == 0) {
                    positions[i] = new int[]{centerX, h - CARD_HEIGHT - 12}; // bottom (human)
                } else if (relativePos == 1) {
                    positions[i] = new int[]{12, centerY - CARD_HEIGHT / 2}; // left
                } else {
                    positions[i] = new int[]{centerX, 12}; // top
                }
            }
        } else if (numPlayers == 4) {
            // Fixed positions: human bottom, left, top, right
            for (int i = 0; i < numPlayers; i++) {
                int relativePos = (i - humanIndex + 4) % 4;
                if (relativePos == 0) {
                    positions[i] = new int[]{centerX, h - CARD_HEIGHT - 12}; // bottom (human)
                } else if (relativePos == 1) {
                    positions[i] = new int[]{12, centerY - CARD_HEIGHT / 2}; // left
                } else if (relativePos == 2) {
                    positions[i] = new int[]{centerX, 12}; // top
                } else {
                    positions[i] = new int[]{w - CARD_WIDTH - 12, centerY - CARD_HEIGHT / 2}; // right
                }
            }
        }

        return positions;
    }

    private void updateTableCards() {
        tableOval.removeAll();

        // Deck image al centro del tavolo (come prima)
        JLabel deck = createDeckImage();
        int dw = deck.getPreferredSize().width;
        int dh = deck.getPreferredSize().height;
        int deckX = Math.max(0, (int)(tableOval.getWidth() * 0.13));
        int deckY = (tableOval.getHeight() - dh) / 2;
        deck.setBounds(deckX, deckY, dw, dh);
        tableOval.add(deck);

        int nPlayers = this.players.size();
        int[][] positions = computeSlotPositions();

        // Slot highlights (bordo giallo SOLO sul turno, posizioni fisse)
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

        // Carte giocate: sempre nella posizione fissa del proprio giocatore
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

        // Etichette con nomi
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

    private void updateOpponentArea() {
        opponentArea.removeAll();

        int numPlayers = players.size();
        int areaWidth = opponentArea.getWidth() > 0 ? opponentArea.getWidth() : 1000;
        int areaHeight = opponentArea.getHeight() > 0 ? opponentArea.getHeight() : 150;

        // Find human player index
        int humanIndex = players.indexOf(humanPlayer);

        // Position opponents based on fixed positions
        for (int i = 0; i < numPlayers; i++) {
            if (i == humanIndex) continue; // Skip human player

            Giocatore player = players.get(i);
            boolean isVertical = false;
            int x = 0, y = 0;

            if (numPlayers == 2) {
                // Top center
                isVertical = false;
                x = areaWidth / 2 - 80;
                y = 10;
            } else if (numPlayers == 3) {
                // Arrange as: left, top, bottom (excluding human at bottom)
                if (i == (humanIndex + 1) % 3) {
                    // Left player - vertical cards
                    isVertical = true;
                    x = 20;
                    y = areaHeight / 2 - 60;
                } else {
                    // Top player - horizontal cards
                    isVertical = false;
                    x = areaWidth / 2 - 80;
                    y = 10;
                }
            } else if (numPlayers == 4) {
                // Arrange as: left, top, right (excluding human at bottom)
                int relativePos = (i - humanIndex + 4) % 4;
                if (relativePos == 1) {
                    // Left player - vertical cards
                    isVertical = true;
                    x = 20;
                    y = areaHeight / 2 - 60;
                } else if (relativePos == 2) {
                    // Top player - horizontal cards
                    isVertical = false;
                    x = areaWidth / 2 - 80;
                    y = 10;
                } else if (relativePos == 3) {
                    // Right player - vertical cards
                    isVertical = true;
                    x = areaWidth - 180;
                    y = areaHeight / 2 - 60;
                }
            }

            JPanel opponentBox = createOpponentBox(player, isVertical);
            opponentBox.setBounds(x, y, isVertical ? 180 : 200, isVertical ? 120 : 150);
            opponentArea.add(opponentBox);
        }

        opponentArea.revalidate();
        opponentArea.repaint();
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
            // Get the card position in hand or opponent area
            Point startPos;
            if (player == humanPlayer && cardIndex >= 0 && cardIndex < cardPanels.size()) {
                // Get the card panel position for animation
                CardPanel cardPanel = cardPanels.get(cardIndex);
                startPos = cardPanel.getLocationOnScreen();
            } else {
                // For opponent, start from their area
                Point tablePos = tableOval.getLocationOnScreen();
                int[][] positions = computeSlotPositions();
                int playerIdx = players.indexOf(player);
                if (playerIdx >= 0) {
                    startPos = new Point(
                            tablePos.x + positions[playerIdx][0],
                            tablePos.y + positions[playerIdx][1]
                    );
                } else {
                    // Fallback to center
                    startPos = new Point(
                            tablePos.x + tableOval.getWidth() / 2,
                            tablePos.y + tableOval.getHeight() / 2
                    );
                }
            }

            // For now, just update the table (animation can be enhanced later)
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
        int x = tableOval.getWidth() - labelW - 28; // 28px dal bordo destro
        int y = 18;
        winnerLabel.setBounds(x, y, labelW, labelH);

        tableOval.add(winnerLabel);
        tableOval.revalidate();
        tableOval.repaint();

        Timer removeTimer = new Timer(800, evt -> {
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

        fadeTimer.addActionListener(evt -> {
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

    // Animazione di distribuzione delle carte
    public void showDealingAnimation(List<Giocatore> players, Runnable onComplete) {
        SwingUtilities.invokeLater(() -> {
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

            JPanel glassPane = (JPanel) getGlassPane();
            glassPane.setLayout(null);
            glassPane.add(animationOverlay);
            glassPane.setVisible(true);

            int centerX = getWidth() / 2 - CARD_WIDTH / 2;
            int centerY = getHeight() / 2 - CARD_HEIGHT / 2;

            int numPlayers = players.size();
            int[][] posBase = computeSlotPositions();
            int[][] targetPositions = new int[numPlayers][2];

            // convert table-relative positions to frame coordinates:
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

    public void showDrawAnimationToPlayerHand(Giocatore player, Runnable onComplete) {
        SwingUtilities.invokeLater(() -> {
            // 1. Trova il mazzo sul tavolo
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

            // 2. Calcola posizione partenza (mazzo)
            Point tableOnScreen = tableOval.getLocationOnScreen();
            Point deckOnScreen = deckLabel.getLocationOnScreen();
            int deckX = deckOnScreen.x - tableOnScreen.x + deckLabel.getWidth() / 2 - CARD_WIDTH / 2;
            int deckY = deckOnScreen.y - tableOnScreen.y + deckLabel.getHeight() / 2 - CARD_HEIGHT / 2;

            // 3. Calcola destinazione: se umano, la sua mano, altrimenti posizione tavolo
            int destX, destY;

            // Se è l'umano, vola verso la mano
            if (player == humanPlayer) {
                Point handOnScreen = playerHandPanel.getLocationOnScreen();
                destX = handOnScreen.x - tableOnScreen.x + playerHandPanel.getWidth() / 2 - CARD_WIDTH / 2;
                destY = handOnScreen.y - tableOnScreen.y + playerHandPanel.getHeight() / 2 - CARD_HEIGHT / 2;
            } else {
                // Per gli altri giocatori, usa le posizioni slot sul tavolo
                int idx = players.indexOf(player);
                int[][] positions = computeSlotPositions();
                destX = positions[idx][0];
                destY = positions[idx][1];
            }

            // 4. Overlay e carta volante
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

            // 5. Animazione
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

    // Animazione del volo della carta verso il giocatore vincitore
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
            // Crea overlay trasparente sopra la finestra
            JPanel overlay = new JPanel() {
                float alpha = 0f; // Per fade-in

                {
                    // Effetto fade-in
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

            // Contenuto centrale
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

            // Gestione click
            backButton.addActionListener(e -> {
                // Fade-out veloce
                Timer fadeOut = new Timer(16, null);
                fadeOut.addActionListener(evt -> {
                    overlay.setVisible(false);
                    getGlassPane().setVisible(false);
                    // Richiama evento controller
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

            // Disabilita altre interazioni
            playButton.setEnabled(false);
            statusLabel.setText("");
        });
    }

    /**
     * Fade in the window when it becomes visible
     */
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