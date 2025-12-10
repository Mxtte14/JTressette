package ui;

import controller.GameController;
import game.*;
import util.CardImageLoader;

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
    private static final int HAND_GAP = 8; // gap between hand cards
    private static final int HAND_CARD_MIN_WIDTH = 36;
    private static final int HAND_CARD_MAX_WIDTH = 90;
    private int handCardWidth = 56;  // default
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

    private int selectedCardIndex = -1;
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

        // Right: Log/info panel
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

        int rightPanelW = 220; // same as createInfoPanel preferred
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
        int rightPanelW = 220;
        int effectiveWidth = Math.max(320, contentW - rightPanelW - 80);

        int handCount = Math.max(1, gameState.getHand(humanPlayer).size());
        int maxPerCard = (effectiveWidth - (handCount + 1) * HAND_GAP) / handCount;

        handCardWidth = Math.max(HAND_CARD_MIN_WIDTH, Math.min(HAND_CARD_MAX_WIDTH, maxPerCard));
        double ratio = (double) CARD_HEIGHT / (double) CARD_WIDTH;
        handCardHeight = (int) Math.round(handCardWidth * ratio);

        if (playerHandPanel != null && handScrollPane != null) {
            handScrollPane.setPreferredSize(new Dimension(Math.min(effectiveWidth, 1000), handCardHeight + 36));
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
        area.setLayout(new FlowLayout(FlowLayout.CENTER, 40, 10));
        area.setBorder(new EmptyBorder(20, 20, 10, 20));

        for (Giocatore player : this.players) {
            if (player != humanPlayer) {
                JPanel opponentBox = createOpponentBox(player);
                area.add(opponentBox);
            }
        }

        return area;
    }

    private JPanel createOpponentBox(Giocatore player) {
        JPanel box = new JPanel();
        box.setOpaque(false);
        box.setLayout(new BoxLayout(box, BoxLayout.Y_AXIS));

        JLabel nameLabel = new JLabel(player.getName());
        nameLabel.setForeground(TEXT_WHITE);
        nameLabel.setFont(new Font("Arial", Font.BOLD, 14));
        nameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Face-down cards panel using card back images
        JPanel cardsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, -20, 0));
        cardsPanel.setOpaque(false);

        List<Cards> hand = gameState.getHand(player);
        Image cardBackImg = CardImageLoader.getScaledCardBackImage(SMALL_CARD_WIDTH, SMALL_CARD_HEIGHT);
        for (int i = 0; i < hand.size(); i++) {
            JLabel cardBack = new JLabel(new ImageIcon(cardBackImg));
            cardBack.setPreferredSize(new Dimension(SMALL_CARD_WIDTH + 5, SMALL_CARD_HEIGHT + 5));
            cardsPanel.add(cardBack);
        }

        int wonCards = gameState.getWonCardsCount(player);
        JPanel wonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));
        wonPanel.setOpaque(false);

        JPanel opponentDeckIcon = createOpponentWonCardsDeckIcon(player);
        wonPanel.add(opponentDeckIcon);

        JLabel wonLabel = new JLabel("Carte: " + wonCards);
        wonLabel.setForeground(TEXT_GOLD);
        wonLabel.setFont(new Font("Arial", Font.PLAIN, 10));
        wonPanel.add(wonLabel);

        box.add(nameLabel);
        box.add(Box.createVerticalStrut(5));
        box.add(cardsPanel);
        box.add(Box.createVerticalStrut(3));
        box.add(wonPanel);
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

        // Put handPanel in horizontal scroll pane to avoid clipping and keep proportions
        handScrollPane = new JScrollPane(playerHandPanel, JScrollPane.VERTICAL_SCROLLBAR_NEVER, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        handScrollPane.setOpaque(false);
        handScrollPane.getViewport().setOpaque(false);
        handScrollPane.setBorder(null);
        handScrollPane.getHorizontalScrollBar().setUnitIncrement(16);
        handScrollPane.setAlignmentX(Component.CENTER_ALIGNMENT);
        handScrollPane.setPreferredSize(new Dimension(800, handCardHeight + 36)); // will be updated by recomputeHandCardSize()

        // Won cards indicator panel
        JPanel wonCardsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        wonCardsPanel.setOpaque(false);
        JPanel deckIcon = createWonCardsDeckIcon();
        wonCardsPanel.add(deckIcon);

        wonCardsLabel = new JLabel("Carte prese: 0");
        wonCardsLabel.setForeground(TEXT_GOLD);
        wonCardsLabel.setFont(new Font("Arial", Font.BOLD, 12));
        wonCardsPanel.add(wonCardsLabel);

        playButton = new JButton("Gioca Carta");
        playButton.setFont(new Font("Arial", Font.BOLD, 14));
        playButton.setBackground(new Color(200, 160, 0));
        playButton.setForeground(Color.WHITE);
        playButton.setFocusPainted(false);
        playButton.setEnabled(false);
        playButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        playButton.addActionListener(_ -> onPlayCard());

        area.add(playerLabel);
        area.add(Box.createVerticalStrut(6));
        area.add(wonCardsPanel);
        area.add(Box.createVerticalStrut(8));
        area.add(handScrollPane);
        area.add(Box.createVerticalStrut(8));
        area.add(playButton);

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
        scoreLabel.setForeground(TEXT_WHITE);
        scoreLabel.setFont(new Font("Arial", Font.BOLD, 14));
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
        backButton.addActionListener(_ -> controller.onExitGame());

        panel.add(titleLabel);
        panel.add(Box.createVerticalStrut(15));
        panel.add(statusLabel);
        panel.add(Box.createVerticalStrut(10));
        panel.add(scoreLabel);
        panel.add(Box.createVerticalStrut(15));
        panel.add(logTitle);
        panel.add(Box.createVerticalStrut(5));
        panel.add(logScroll);
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
        private boolean isSelected = false;
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
            setPreferredSize(new Dimension(drawWidth + 5, drawHeight + 15));

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
                        selectCard(index);
                    }
                });
                setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            }
        }

        public void setSelected(boolean selected) {
            this.isSelected = selected;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

            int offsetY = (isHovered || isSelected) ? 0 : 6;

            // Shadow
            g2d.setColor(new Color(0, 0, 0, 80));
            g2d.fill(new RoundRectangle2D.Double(4, 4 + offsetY, drawWidth, drawHeight, 10, 10));

            // Draw card image
            if (cardImage != null) {
                g2d.drawImage(cardImage, 0, offsetY, drawWidth, drawHeight, this);
            } else {
                drawFallbackCard(g2d, offsetY, drawWidth, drawHeight);
            }

            // Selection border
            if (isSelected) {
                g2d.setColor(TEXT_GOLD);
                g2d.setStroke(new BasicStroke(3));
                g2d.drawRoundRect(0, offsetY, drawWidth - 1, drawHeight - 1, 10, 10);
            } else if (isHovered) {
                g2d.setColor(TEXT_GOLD);
                g2d.setStroke(new BasicStroke(2));
                g2d.drawRoundRect(0, offsetY, drawWidth - 1, drawHeight - 1, 10, 10);
            }
        }

        private void drawFallbackCard(Graphics2D g2d, int offsetY, int w, int h) {
            g2d.setColor(Color.WHITE);
            g2d.fill(new RoundRectangle2D.Double(0, offsetY, w, h, 10, 10));

            g2d.setColor(Color.GRAY);
            g2d.setStroke(new BasicStroke(1));
            g2d.draw(new RoundRectangle2D.Double(0, offsetY, w - 1, h - 1, 10, 10));

            Color suitColor = getSuitColor(card.getSegno());
            g2d.setColor(suitColor);

            g2d.setFont(new Font("Serif", Font.BOLD, Math.max(12, w / 6)));
            String rank = getRankSymbol(card.getRank());
            FontMetrics fm = g2d.getFontMetrics();
            int rankWidth = fm.stringWidth(rank);
            g2d.drawString(rank, (w - rankWidth) / 2, 30 + offsetY);

            g2d.setFont(new Font("Serif", Font.BOLD, Math.max(16, w / 3)));
            String suit = getSuitSymbol(card.getSegno());
            fm = g2d.getFontMetrics();
            int suitWidth = fm.stringWidth(suit);
            g2d.drawString(suit, (w - suitWidth) / 2, (int) (h * 0.7) + offsetY);
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

    private void selectCard(int index) {
        if (selectedCardIndex >= 0 && selectedCardIndex < cardPanels.size()) {
            cardPanels.get(selectedCardIndex).setSelected(false);
        }
        selectedCardIndex = index;
        if (index >= 0 && index < cardPanels.size()) {
            cardPanels.get(index).setSelected(true);
        }
        playButton.setEnabled(true);
    }

    private void onPlayCard() {
        if (selectedCardIndex >= 0) {
            int[] legalMoves = gameState.getLegalMoves(humanPlayer);
            boolean isLegal = false;
            for (int legal : legalMoves) {
                if (legal == selectedCardIndex) {
                    isLegal = true;
                    break;
                }
            }

            if (isLegal) {
                controller.onCardPlayed(selectedCardIndex);
                selectedCardIndex = -1;
                playButton.setEnabled(false);
            } else {
                log("Mossa non valida! Devi seguire il seme se possibile.");
            }
        }
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

        selectedCardIndex = -1;
        playButton.setEnabled(false);

        playerHandPanel.revalidate();
        playerHandPanel.repaint();
    }

    /**
     * Compute slot positions relative to current tableOval size.
     */
    private int[][] computeSlotPositions() {
        int w = tableOval.getWidth() > 0 ? tableOval.getWidth() : tableOval.getPreferredSize().width;
        int h = tableOval.getHeight() > 0 ? tableOval.getHeight() : tableOval.getPreferredSize().height;

        int centerX = (w - CARD_WIDTH) / 2;
        int centerY = (h - CARD_HEIGHT) / 2;

        int[][] pos2 = {
                {centerX, h - CARD_HEIGHT - 12}, // bottom
                {centerX, 12}                    // top
        };
        int[][] pos3 = {
                {centerX, h - CARD_HEIGHT - 12},     // bottom
                {12, centerY - CARD_HEIGHT / 2},     // left
                {centerX, 12}                        // top
        };
        int[][] pos4 = {
                {centerX, h - CARD_HEIGHT - 12},     // bottom
                {12, centerY - CARD_HEIGHT / 2},     // left
                {centerX, 12},                       // top
                {w - CARD_WIDTH - 12, centerY - CARD_HEIGHT / 2} // right
        };

        int numplayers = this.players.size();
        return numplayers == 2 ? pos2 : (numplayers == 3 ? pos3 : pos4);
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
        for (Giocatore player : gameState.getPlayers()) {
            if (player != humanPlayer) {
                JPanel opponentBox = createOpponentBox(player);
                opponentArea.add(opponentBox);
            }
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

            dealTimer.addActionListener(_ -> {
                if (currentCard[0] >= totalCards) {
                    dealTimer.stop();
                    dealingLabel.setText("Pronto!");
                    Timer fadeOutTimer = new Timer(800, _ -> {
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

    // Animazione del volo della carta verso il giocatore vincitore
    private void animateCardFlight(JLabel card, int startX, int startY, int endX, int endY) {
        final int steps = 15;
        final int delay = GameView.CARD_FLY_DURATION_MS / steps;

        Timer flyTimer = new Timer(delay, null);
        final int[] step = {0};

        flyTimer.addActionListener(_ -> {
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
                    fadeIn.addActionListener(_ -> {
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
            backButton.addActionListener(_ -> {
                // Fade-out veloce
                Timer fadeOut = new Timer(16, null);
                fadeOut.addActionListener(_ -> {
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
}