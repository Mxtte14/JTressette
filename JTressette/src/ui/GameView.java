package ui;

import game.Cards;
import game.Giocatore;
import game.GiocatoreUmano;
import game.GameState;
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
    private static final Color CARD_BACK = new Color(30, 60, 120);
    private static final Color TEXT_GOLD = new Color(255, 215, 0);
    private static final Color TEXT_WHITE = Color.WHITE;

    // Card dimensions
    private static final int CARD_WIDTH = 70;
    private static final int CARD_HEIGHT = 100;
    private static final int SMALL_CARD_WIDTH = 50;
    private static final int SMALL_CARD_HEIGHT = 75;

    // Animation constants
    private static final int ANIMATION_DELAY_MS = 80;
    private static final int CARD_FLY_DURATION_MS = 200;
    private static final int PLAYER_POSITION_SPACING = 200;
    private static final double CARD_ARC_HEIGHT = 30.0;
    private static final double CARD_SCALE_FACTOR = 0.15;

    private final GameState gameState;
    private final GiocatoreUmano humanPlayer;
    private final GameController controller;

    // UI components that need updating
    private JPanel playerHandPanel;
    private JPanel tableCardsPanel;
    private JPanel opponentArea;
    private JLabel statusLabel;
    private JLabel scoreLabel;
    private JPanel logArea;
    private JButton playButton;
    private JPanel wonCardsPanel;
    private JLabel wonCardsLabel;

    private int selectedCardIndex = -1;
    private List<CardPanel> cardPanels = new ArrayList<>();

    public GameView(GameState gameState, GiocatoreUmano humanPlayer, GameController controller) {
        super("JTressette - Partita in Corso");
        this.gameState = gameState;
        this.humanPlayer = humanPlayer;
        this.controller = controller;
        // Preload card images
        CardImageLoader.preloadImages();
        initUI();
    }

    private void initUI() {
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setSize(1100, 750);
        setResizable(false);
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

        // Initial refresh
        refresh();
    }

    // Custom panel with gradient background
    private class GradientPanel extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            GradientPaint gp = new GradientPaint(0, 0, FELT_DARK, 0, getHeight() / 2, FELT_GREEN);
            g2d.setPaint(gp);
            g2d.fillRect(0, 0, getWidth(), getHeight() / 2);
            GradientPaint gp2 = new GradientPaint(0, getHeight() / 2, FELT_GREEN, 0, getHeight(), FELT_DARK);
            g2d.setPaint(gp2);
            g2d.fillRect(0, getHeight() / 2, getWidth(), getHeight() / 2);
        }
    }

    private JPanel createOpponentArea() {
        JPanel area = new JPanel();
        area.setOpaque(false);
        area.setLayout(new FlowLayout(FlowLayout.CENTER, 40, 10));
        area.setBorder(new EmptyBorder(20, 20, 10, 20));

        for (Giocatore player : gameState.getPlayers()) {
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

        // Show won cards count with small deck icon - use GameState tracking
        int wonCards = gameState.getWonCardsCount(player);
        JPanel wonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));
        wonPanel.setOpaque(false);

        // Small deck icon for opponent
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

    /**
     * Creates a small deck icon for opponent's won cards pile.
     */
    private JPanel createOpponentWonCardsDeckIcon(Giocatore player) {
        JPanel deckIcon = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int wonCards = gameState.getWonCardsCount(player);

                // Draw stacked cards to represent won pile (smaller for opponents)
                int cardWidth = 18;
                int cardHeight = 25;
                int stackOffset = 1;
                int stackSize = Math.min(wonCards / 2, 4); // Show up to 4 layers

                for (int i = 0; i <= stackSize; i++) {
                    int x = i * stackOffset;
                    int y = (stackSize - i) * stackOffset;

                    // Card shadow
                    g2d.setColor(new Color(0, 0, 0, 60));
                    g2d.fillRoundRect(x + 1, y + 1, cardWidth, cardHeight, 3, 3);

                    // Card back
                    g2d.setColor(new Color(30, 60, 120));
                    g2d.fillRoundRect(x, y, cardWidth, cardHeight, 3, 3);

                    // Card border
                    g2d.setColor(new Color(20, 40, 80));
                    g2d.drawRoundRect(x, y, cardWidth, cardHeight, 3, 3);
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

        // Table oval panel
        JPanel tableOval = new TablePanel();
        tableOval.setPreferredSize(new Dimension(500, 250));
        tableOval.setLayout(new BoxLayout(tableOval, BoxLayout.Y_AXIS));

        JLabel tableLabel = new JLabel("Tavolo");
        tableLabel.setForeground(TEXT_GOLD);
        tableLabel.setFont(new Font("Serif", Font.BOLD, 18));
        tableLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        tableCardsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 5));
        tableCardsPanel.setOpaque(false);

        tableOval.add(Box.createVerticalStrut(30));
        tableOval.add(tableLabel);
        tableOval.add(Box.createVerticalStrut(10));
        tableOval.add(tableCardsPanel);

        center.add(tableOval);
        return center;
    }

    // Custom table panel with oval shape
    private class TablePanel extends JPanel {
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
            g2d.fill(new RoundRectangle2D.Double(8, 8, getWidth() - 16, getHeight() - 16, 100, 100));

            // Table fill
            g2d.setColor(new Color(35, 130, 75));
            g2d.fill(new RoundRectangle2D.Double(4, 4, getWidth() - 8, getHeight() - 8, 100, 100));

            // Border
            g2d.setColor(FELT_BORDER);
            g2d.setStroke(new BasicStroke(8));
            g2d.draw(new RoundRectangle2D.Double(4, 4, getWidth() - 8, getHeight() - 8, 100, 100));
        }
    }

    private JPanel createPlayerArea() {
        JPanel area = new JPanel();
        area.setOpaque(false);
        area.setLayout(new BoxLayout(area, BoxLayout.Y_AXIS));
        area.setBorder(new EmptyBorder(10, 20, 20, 20));

        JLabel playerLabel = new JLabel("La tua mano - " + humanPlayer.getName());
        playerLabel.setForeground(TEXT_WHITE);
        playerLabel.setFont(new Font("Arial", Font.BOLD, 16));
        playerLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        playerHandPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        playerHandPanel.setOpaque(false);

        // Won cards indicator panel with deck icon
        wonCardsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        wonCardsPanel.setOpaque(false);

        // Create a small deck icon panel
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
        playButton.addActionListener(e -> onPlayCard());

        area.add(playerLabel);
        area.add(Box.createVerticalStrut(5));
        area.add(wonCardsPanel);
        area.add(Box.createVerticalStrut(10));
        area.add(playerHandPanel);
        area.add(Box.createVerticalStrut(10));
        area.add(playButton);

        return area;
    }

    /**
     * Creates a small deck icon representing won cards pile.
     */
    private JPanel createWonCardsDeckIcon() {
        JPanel deckIcon = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int wonCards = gameState.getWonCardsCount(humanPlayer);

                // Draw stacked cards to represent won pile
                int cardWidth = 25;
                int cardHeight = 35;
                int stackOffset = 2;
                int stackSize = Math.min(wonCards / 2, 5); // Show up to 5 layers

                for (int i = 0; i <= stackSize; i++) {
                    int x = i * stackOffset;
                    int y = (stackSize - i) * stackOffset;

                    // Card shadow
                    g2d.setColor(new Color(0, 0, 0, 60));
                    g2d.fillRoundRect(x + 2, y + 2, cardWidth, cardHeight, 5, 5);

                    // Card back
                    g2d.setColor(new Color(30, 60, 120));
                    g2d.fillRoundRect(x, y, cardWidth, cardHeight, 5, 5);

                    // Card border
                    g2d.setColor(new Color(20, 40, 80));
                    g2d.drawRoundRect(x, y, cardWidth, cardHeight, 5, 5);
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
        backButton.addActionListener(e -> controller.onExitGame());

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

    private JPanel createCardBack() {
        Image cardBackImg = CardImageLoader.getScaledCardBackImage(SMALL_CARD_WIDTH, SMALL_CARD_HEIGHT);
        JLabel card = new JLabel(new ImageIcon(cardBackImg));
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Shadow
                g2d.setColor(new Color(0, 0, 0, 80));
                g2d.fill(new RoundRectangle2D.Double(3, 3, SMALL_CARD_WIDTH, SMALL_CARD_HEIGHT, 8, 8));
            }
        };
        panel.setOpaque(false);
        panel.setLayout(new BorderLayout());
        panel.add(card, BorderLayout.CENTER);
        panel.setPreferredSize(new Dimension(SMALL_CARD_WIDTH + 5, SMALL_CARD_HEIGHT + 5));
        return panel;
    }

    // Custom card panel for face-up cards using images
    private class CardPanel extends JPanel {
        private final Cards card;
        private final int index;
        private final boolean isPlayable;
        private boolean isHovered = false;
        private boolean isSelected = false;
        private Image cardImage;

        public CardPanel(Cards card, int index, boolean isPlayable) {
            this.card = card;
            this.index = index;
            this.isPlayable = isPlayable;
            this.cardImage = CardImageLoader.getScaledCardImage(card, CARD_WIDTH, CARD_HEIGHT);
            setOpaque(false);
            setPreferredSize(new Dimension(CARD_WIDTH + 5, CARD_HEIGHT + 15));

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

            int offsetY = (isHovered || isSelected) ? 0 : 10;

            // Shadow
            g2d.setColor(new Color(0, 0, 0, 80));
            g2d.fill(new RoundRectangle2D.Double(5, 5 + offsetY, CARD_WIDTH, CARD_HEIGHT, 10, 10));

            // Draw card image
            if (cardImage != null) {
                g2d.drawImage(cardImage, 0, offsetY, CARD_WIDTH, CARD_HEIGHT, this);
            } else {
                // Fallback to drawn card if image not available
                drawFallbackCard(g2d, offsetY);
            }

            // Selection border
            if (isSelected) {
                g2d.setColor(TEXT_GOLD);
                g2d.setStroke(new BasicStroke(3));
                g2d.drawRoundRect(0, offsetY, CARD_WIDTH - 1, CARD_HEIGHT - 1, 10, 10);
            } else if (isHovered) {
                g2d.setColor(TEXT_GOLD);
                g2d.setStroke(new BasicStroke(2));
                g2d.drawRoundRect(0, offsetY, CARD_WIDTH - 1, CARD_HEIGHT - 1, 10, 10);
            }
        }

        private void drawFallbackCard(Graphics2D g2d, int offsetY) {
            // Card background
            g2d.setColor(Color.WHITE);
            g2d.fill(new RoundRectangle2D.Double(0, offsetY, CARD_WIDTH, CARD_HEIGHT, 10, 10));

            // Border
            g2d.setColor(Color.GRAY);
            g2d.setStroke(new BasicStroke(1));
            g2d.draw(new RoundRectangle2D.Double(0, offsetY, CARD_WIDTH - 1, CARD_HEIGHT - 1, 10, 10));

            // Card content
            Color suitColor = getSuitColor(card.getSegno());
            g2d.setColor(suitColor);

            // Rank
            g2d.setFont(new Font("Serif", Font.BOLD, 18));
            String rank = getRankSymbol(card.getRank());
            FontMetrics fm = g2d.getFontMetrics();
            int rankWidth = fm.stringWidth(rank);
            g2d.drawString(rank, (CARD_WIDTH - rankWidth) / 2, 30 + offsetY);

            // Suit
            g2d.setFont(new Font("Serif", Font.BOLD, 28));
            String suit = getSuitSymbol(card.getSegno());
            fm = g2d.getFontMetrics();
            int suitWidth = fm.stringWidth(suit);
            g2d.drawString(suit, (CARD_WIDTH - suitWidth) / 2, 70 + offsetY);
        }
    }

    private String getRankSymbol(Cards.Rank rank) {
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
        // Deselect previous
        if (selectedCardIndex >= 0 && selectedCardIndex < cardPanels.size()) {
            cardPanels.get(selectedCardIndex).setSelected(false);
        }

        // Select new
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

    /**
     * Refresh the view to reflect current game state.
     */
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
            CardPanel cardPanel = new CardPanel(card, i, isMyTurn && isLegal);
            cardPanels.add(cardPanel);
            playerHandPanel.add(cardPanel);
        }

        selectedCardIndex = -1;
        playButton.setEnabled(false);

        playerHandPanel.revalidate();
        playerHandPanel.repaint();
    }

    private void updateTableCards() {
        tableCardsPanel.removeAll();

        List<Cards> trickCards = gameState.getTrickCards();
        List<Giocatore> trickPlayers = gameState.getTrickPlayers();

        for (int i = 0; i < trickCards.size(); i++) {
            Cards card = trickCards.get(i);
            Giocatore player = trickPlayers.get(i);

            JPanel playedCard = new JPanel();
            playedCard.setOpaque(false);
            playedCard.setLayout(new BoxLayout(playedCard, BoxLayout.Y_AXIS));

            JLabel nameLabel = new JLabel(player.getName());
            nameLabel.setForeground(TEXT_WHITE);
            nameLabel.setFont(new Font("Arial", Font.PLAIN, 10));
            nameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

            CardPanel cardPanel = new CardPanel(card, -1, false);
            cardPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

            playedCard.add(nameLabel);
            playedCard.add(Box.createVerticalStrut(3));
            playedCard.add(cardPanel);

            tableCardsPanel.add(playedCard);
        }

        if (tableCardsPanel.getComponentCount() == 0) {
            JLabel noCards = new JLabel("Nessuna carta giocata");
            noCards.setForeground(new Color(200, 200, 200, 180));
            noCards.setFont(new Font("Arial", Font.PLAIN, 14));
            tableCardsPanel.add(noCards);
        }

        tableCardsPanel.revalidate();
        tableCardsPanel.repaint();
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
        int score = gameState.getScore(humanPlayer);
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

    /**
     * Add a log message to the info panel.
     */
    public void log(String message) {
        SwingUtilities.invokeLater(() -> {
            JLabel logEntry = new JLabel("• " + message);
            logEntry.setForeground(TEXT_WHITE);
            logEntry.setFont(new Font("Arial", Font.PLAIN, 11));
            logEntry.setAlignmentX(Component.LEFT_ALIGNMENT);

            logArea.add(logEntry, 0);

            // Keep only last 15 messages
            while (logArea.getComponentCount() > 15) {
                logArea.remove(logArea.getComponentCount() - 1);
            }

            logArea.revalidate();
            logArea.repaint();
        });
    }

    /**
     * Show a card being played on the table.
     */
    public void showCardPlayed(Giocatore player, Cards card) {
        SwingUtilities.invokeLater(() -> {
            // Remove "no cards" label if present
            if (tableCardsPanel.getComponentCount() == 1 &&
                    tableCardsPanel.getComponent(0) instanceof JLabel) {
                tableCardsPanel.removeAll();
            }

            JPanel playedCard = new JPanel();
            playedCard.setOpaque(false);
            playedCard.setLayout(new BoxLayout(playedCard, BoxLayout.Y_AXIS));

            JLabel nameLabel = new JLabel(player.getName());
            nameLabel.setForeground(TEXT_WHITE);
            nameLabel.setFont(new Font("Arial", Font.PLAIN, 10));
            nameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

            CardPanel cardPanel = new CardPanel(card, -1, false);
            cardPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

            playedCard.add(nameLabel);
            playedCard.add(Box.createVerticalStrut(3));
            playedCard.add(cardPanel);

            tableCardsPanel.add(playedCard);
            tableCardsPanel.revalidate();
            tableCardsPanel.repaint();
        });
    }

    /**
     * Clear the table when a trick is completed.
     */
    public void clearTable() {
        SwingUtilities.invokeLater(() -> {
            tableCardsPanel.removeAll();
            tableCardsPanel.revalidate();
            tableCardsPanel.repaint();
        });
    }

    /**
     * Called when a trick is won to update the won cards count and show animation.
     * @param winner The player who won the trick
     * @param cardsWon Number of cards won in this trick
     */
    public void showTrickWon(Giocatore winner, int cardsWon) {
        SwingUtilities.invokeLater(() -> {
            // Update human player's won cards label from GameState
            if (winner == humanPlayer) {
                wonCardsLabel.setText("Carte prese: " + gameState.getWonCardsCount(humanPlayer));
            }

            // Show winner indicator on the table
            showWinnerIndicator(winner);

            // Animate cards moving to winner's pile
            animateCardsToWinner(winner);
        });
    }

    /**
     * Show a visual indicator of who won the trick.
     */
    private void showWinnerIndicator(Giocatore winner) {
        // Add a "Winner" label to the table panel
        JLabel winnerLabel = new JLabel("🏆 " + winner.getName() + " prende!");
        winnerLabel.setForeground(TEXT_GOLD);
        winnerLabel.setFont(new Font("Serif", Font.BOLD, 16));
        winnerLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Add to parent of tableCardsPanel
        JPanel tableOval = (JPanel) tableCardsPanel.getParent();
        if (tableOval != null) {
            tableOval.add(winnerLabel);
            tableOval.revalidate();
            tableOval.repaint();

            // Remove after animation
            Timer removeTimer = new Timer(800, e -> {
                tableOval.remove(winnerLabel);
                tableOval.revalidate();
                tableOval.repaint();
            });
            removeTimer.setRepeats(false);
            removeTimer.start();
        }
    }

    /**
     * Animate cards moving from table to winner's pile.
     */
    private void animateCardsToWinner(Giocatore winner) {
        // Simple fade-out animation for the table cards
        Timer fadeTimer = new Timer(50, null);
        final float[] alpha = {1.0f};

        fadeTimer.addActionListener(e -> {
            alpha[0] -= 0.1f;
            if (alpha[0] <= 0) {
                fadeTimer.stop();
                clearTable();
                refresh();
            } else {
                // Repaint with reduced alpha
                tableCardsPanel.repaint();
            }
        });
        fadeTimer.start();
    }

    /**
     * Animate drawing a card (visual effect when dealing).
     */
    public void animateCardDraw() {
        SwingUtilities.invokeLater(() -> {
            // Flash effect on player hand
            Timer flashTimer = new Timer(100, null);
            final int[] flashCount = {0};
            final Color originalBg = playerHandPanel.getBackground();

            flashTimer.addActionListener(e -> {
                flashCount[0]++;
                if (flashCount[0] > 4) {
                    flashTimer.stop();
                    playerHandPanel.setOpaque(false);
                    playerHandPanel.repaint();
                } else {
                    playerHandPanel.setOpaque(flashCount[0] % 2 == 1);
                    if (flashCount[0] % 2 == 1) {
                        playerHandPanel.setBackground(new Color(255, 215, 0, 50));
                    }
                    playerHandPanel.repaint();
                }
            });
            flashTimer.start();
        });
    }

    /**
     * Update the won cards count display.
     */
    private void updateWonCardsDisplay() {
        int humanWonCards = gameState.getWonCardsCount(humanPlayer);
        wonCardsLabel.setText("Carte prese: " + humanWonCards);
    }

    /**
     * Show a modern card dealing animation at the start of the game.
     * Cards fly from a central deck position to each player's position.
     * @param players List of players in the game
     * @param onComplete Callback to run when animation completes
     */
    public void showDealingAnimation(List<Giocatore> players, Runnable onComplete) {
        SwingUtilities.invokeLater(() -> {
            // Create overlay panel for the animation
            JPanel animationOverlay = new JPanel() {
                @Override
                protected void paintComponent(Graphics g) {
                    // Semi-transparent background
                    Graphics2D g2d = (Graphics2D) g;
                    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2d.setColor(new Color(0, 0, 0, 150));
                    g2d.fillRect(0, 0, getWidth(), getHeight());
                }
            };
            animationOverlay.setLayout(null);
            animationOverlay.setOpaque(false);
            animationOverlay.setBounds(0, 0, getWidth(), getHeight());

            // Get glass pane and add overlay
            JPanel glassPane = (JPanel) getGlassPane();
            glassPane.setLayout(null);
            glassPane.add(animationOverlay);
            glassPane.setVisible(true);

            // Center position (deck position)
            int centerX = getWidth() / 2 - CARD_WIDTH / 2;
            int centerY = getHeight() / 2 - CARD_HEIGHT / 2;

            // Calculate target positions for each player
            int numPlayers = players.size();
            int[][] targetPositions = new int[numPlayers][2];
            
            for (int i = 0; i < numPlayers; i++) {
                Giocatore player = players.get(i);
                if (player == humanPlayer) {
                    // Human player at bottom
                    targetPositions[i][0] = getWidth() / 2 - CARD_WIDTH / 2;
                    targetPositions[i][1] = getHeight() - 180;
                } else {
                    // Opponents at top (spread horizontally)
                    int opponentIndex = i;
                    int startX = (getWidth() - (numPlayers - 1) * PLAYER_POSITION_SPACING) / 2;
                    targetPositions[i][0] = startX + opponentIndex * PLAYER_POSITION_SPACING - CARD_WIDTH / 2;
                    targetPositions[i][1] = 80;
                }
            }

            // Create "Distribuzione carte..." label
            JLabel dealingLabel = new JLabel("Distribuzione carte...");
            dealingLabel.setFont(new Font("Georgia", Font.BOLD, 28));
            dealingLabel.setForeground(TEXT_GOLD);
            dealingLabel.setHorizontalAlignment(SwingConstants.CENTER);
            int labelWidth = 400;
            int labelHeight = 40;
            dealingLabel.setBounds((getWidth() - labelWidth) / 2, centerY - 80, labelWidth, labelHeight);
            animationOverlay.add(dealingLabel);

            // Create deck visual in center
            Image cardBackImg = CardImageLoader.getScaledCardBackImage(CARD_WIDTH, CARD_HEIGHT);
            
            // Draw deck (stack of cards)
            for (int i = 0; i < 5; i++) {
                JLabel deckCard = new JLabel(new ImageIcon(cardBackImg));
                deckCard.setBounds(centerX + i * 2, centerY - i * 2, CARD_WIDTH, CARD_HEIGHT);
                animationOverlay.add(deckCard);
            }

            animationOverlay.repaint();

            // Animation parameters
            int cardsPerPlayer = 10; // Standard Tressette hand size
            
            Timer dealTimer = new Timer(ANIMATION_DELAY_MS, null);
            final int[] currentCard = {0};
            final int totalCards = numPlayers * cardsPerPlayer;

            dealTimer.addActionListener(e -> {
                if (currentCard[0] >= totalCards) {
                    dealTimer.stop();
                    
                    // Show "Pronto!" message
                    dealingLabel.setText("Pronto!");
                    
                    // Fade out and remove overlay after a short delay
                    Timer fadeOutTimer = new Timer(800, evt -> {
                        glassPane.remove(animationOverlay);
                        glassPane.setVisible(false);
                        glassPane.repaint();
                        
                        if (onComplete != null) {
                            onComplete.run();
                        }
                    });
                    fadeOutTimer.setRepeats(false);
                    fadeOutTimer.start();
                    return;
                }

                // Determine which player gets this card
                int playerIndex = currentCard[0] % numPlayers;
                int targetX = targetPositions[playerIndex][0];
                int targetY = targetPositions[playerIndex][1];

                // Calculate offset for multiple cards (fan effect)
                int cardNum = currentCard[0] / numPlayers;
                int offsetX = cardNum * 15; // Horizontal offset for card fanning
                targetX += offsetX;

                // Create flying card
                JLabel flyingCard = new JLabel(new ImageIcon(cardBackImg));
                flyingCard.setBounds(centerX, centerY, CARD_WIDTH, CARD_HEIGHT);
                animationOverlay.add(flyingCard);
                animationOverlay.setComponentZOrder(flyingCard, 0);
                animationOverlay.repaint();

                // Animate card flying to target position
                animateCardFlight(flyingCard, centerX, centerY, targetX, targetY, CARD_FLY_DURATION_MS);

                currentCard[0]++;
            });

            dealTimer.start();
        });
    }

    /**
     * Animate a card flying from one position to another with easing.
     */
    private void animateCardFlight(JLabel card, int startX, int startY, int endX, int endY, int durationMs) {
        final int steps = 15;
        final int delay = durationMs / steps;
        
        Timer flyTimer = new Timer(delay, null);
        final int[] step = {0};
        
        flyTimer.addActionListener(e -> {
            step[0]++;
            
            // Use ease-out cubic for smooth deceleration
            double t = (double) step[0] / steps;
            double easedT = 1 - Math.pow(1 - t, 3);
            
            int currentX = (int) (startX + (endX - startX) * easedT);
            int currentY = (int) (startY + (endY - startY) * easedT);
            
            // Add slight arc to the motion
            double arc = Math.sin(t * Math.PI) * CARD_ARC_HEIGHT;
            currentY -= (int) arc;
            
            // Scale down slightly as card flies
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

    /**
     * Show game over message.
     */
    public void showGameOver(String result) {
        SwingUtilities.invokeLater(() -> {
            statusLabel.setText("<html>PARTITA TERMINATA<br>" + result + "</html>");
            statusLabel.setForeground(TEXT_GOLD);
            playButton.setEnabled(false);
            log("Partita terminata: " + result);
        });
    }
}