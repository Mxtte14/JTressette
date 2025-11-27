package ui;

import game.Cards;
import game.Giocatore;
import game.GiocatoreUmano;
import game.GameState;

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
 */
public class GameView extends JFrame {

    // Colors inspired by poker table felt
    private static final Color FELT_GREEN = new Color(26, 117, 65);
    private static final Color FELT_DARK = new Color(18, 85, 47);
    private static final Color FELT_BORDER = new Color(100, 70, 40);
    private static final Color CARD_BACK = new Color(30, 60, 120);
    private static final Color TEXT_GOLD = new Color(255, 215, 0);
    private static final Color TEXT_WHITE = Color.WHITE;

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

    private int selectedCardIndex = -1;
    private List<CardPanel> cardPanels = new ArrayList<>();

    public GameView(GameState gameState, GiocatoreUmano humanPlayer, GameController controller) {
        super("JTressette - Partita in Corso");
        this.gameState = gameState;
        this.humanPlayer = humanPlayer;
        this.controller = controller;
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

        // Face-down cards panel
        JPanel cardsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, -20, 0));
        cardsPanel.setOpaque(false);

        List<Cards> hand = gameState.getHand(player);
        for (int i = 0; i < hand.size(); i++) {
            JPanel cardBack = createCardBack();
            cardsPanel.add(cardBack);
        }

        box.add(nameLabel);
        box.add(Box.createVerticalStrut(5));
        box.add(cardsPanel);
        return box;
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

        playButton = new JButton("Gioca Carta");
        playButton.setFont(new Font("Arial", Font.BOLD, 14));
        playButton.setBackground(new Color(200, 160, 0));
        playButton.setForeground(Color.WHITE);
        playButton.setFocusPainted(false);
        playButton.setEnabled(false);
        playButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        playButton.addActionListener(e -> onPlayCard());

        area.add(playerLabel);
        area.add(Box.createVerticalStrut(10));
        area.add(playerHandPanel);
        area.add(Box.createVerticalStrut(10));
        area.add(playButton);

        return area;
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
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Shadow
                g2d.setColor(new Color(0, 0, 0, 80));
                g2d.fill(new RoundRectangle2D.Double(3, 3, 50, 75, 8, 8));

                // Card back
                g2d.setColor(CARD_BACK);
                g2d.fill(new RoundRectangle2D.Double(0, 0, 50, 75, 8, 8));

                // Border
                g2d.setColor(Color.WHITE);
                g2d.setStroke(new BasicStroke(1));
                g2d.draw(new RoundRectangle2D.Double(0, 0, 49, 74, 8, 8));

                // Inner pattern
                g2d.setColor(new Color(200, 180, 100));
                g2d.draw(new RoundRectangle2D.Double(5, 5, 40, 65, 4, 4));
            }
        };
        card.setOpaque(false);
        card.setPreferredSize(new Dimension(55, 80));
        return card;
    }

    // Custom card panel for face-up cards
    private class CardPanel extends JPanel {
        private final Cards card;
        private final int index;
        private final boolean isPlayable;
        private boolean isHovered = false;
        private boolean isSelected = false;

        public CardPanel(Cards card, int index, boolean isPlayable) {
            this.card = card;
            this.index = index;
            this.isPlayable = isPlayable;
            setOpaque(false);
            setPreferredSize(new Dimension(75, 105));

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

            int offsetY = (isHovered || isSelected) ? -10 : 0;

            // Shadow
            g2d.setColor(new Color(0, 0, 0, 80));
            g2d.fill(new RoundRectangle2D.Double(5, 5 + offsetY, 70, 100, 10, 10));

            // Card background
            g2d.setColor(Color.WHITE);
            g2d.fill(new RoundRectangle2D.Double(0, offsetY, 70, 100, 10, 10));

            // Border
            if (isSelected) {
                g2d.setColor(TEXT_GOLD);
                g2d.setStroke(new BasicStroke(3));
            } else if (isHovered) {
                g2d.setColor(TEXT_GOLD);
                g2d.setStroke(new BasicStroke(2));
            } else {
                g2d.setColor(Color.GRAY);
                g2d.setStroke(new BasicStroke(1));
            }
            g2d.draw(new RoundRectangle2D.Double(0, offsetY, 69, 99, 10, 10));

            // Card content
            Color suitColor = getSuitColor(card.getSegno());
            g2d.setColor(suitColor);

            // Rank
            g2d.setFont(new Font("Serif", Font.BOLD, 18));
            String rank = getRankSymbol(card.getRank());
            FontMetrics fm = g2d.getFontMetrics();
            int rankWidth = fm.stringWidth(rank);
            g2d.drawString(rank, (70 - rankWidth) / 2, 30 + offsetY);

            // Suit
            g2d.setFont(new Font("Serif", Font.BOLD, 28));
            String suit = getSuitSymbol(card.getSegno());
            fm = g2d.getFontMetrics();
            int suitWidth = fm.stringWidth(suit);
            g2d.drawString(suit, (70 - suitWidth) / 2, 70 + offsetY);
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