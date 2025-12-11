package View.Profile;

import Controller.Profile.ProfileController;
import Controller.Profile.ProfileListener;
import Model.Profile.UserProfile;
import Model.Profile.GamesRecord;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;

public class ProfileMenu extends JPanel implements ProfileListener {
    private final JPanel cards;
    private final ProfileController controller;
    private JTextField nameField;
    private JLabel avatarLabel;
    private JTable historyTable;
    private DefaultTableModel tableModel;
    private JLabel statsLabel;
    private JLabel levelLabel;
    private JProgressBar expBar;
    private JLabel expLabel;

    // Modern colors - Updated to match HomeMenu style (green, gold, black)
    private static final Color BACKGROUND_COLOR = new Color(10, 25, 15); // Dark green background
    private static final Color PANEL_BG_COLOR = new Color(20, 50, 30, 200); // Semi-transparent dark green
    private static final Color PRIMARY_COLOR = new Color(35, 130, 75); // Poker table green
    private static final Color ACCENT_COLOR = new Color(46, 160, 90); // Lighter green for hover
    private static final Color TEXT_COLOR = new Color(255, 248, 220); // Light gold text
    private static final Color SECONDARY_TEXT_COLOR = new Color(200, 200, 180); // Dimmer gold
    private static final Color SUCCESS_COLOR = new Color(46, 204, 113); // Bright green
    private static final Color BORDER_COLOR = new Color(255, 215, 0, 100); // Gold border
    private static final Color LEVEL_BADGE_BG = new Color(231, 76, 60); // Red badge
    private static final Color EXP_BAR_COLOR = new Color(255, 215, 0); // Gold progress bar

    public ProfileMenu(JPanel cards, ProfileController controller) {
        this.cards = cards;
        this.controller = controller;
        init();

        // registrazione come observer
        if (this.controller != null) {
            this.controller.addListener(this);
            UserProfile p = this.controller.getProfile();
            if (p != null) onProfileUpdated(p);
        }
    }

    private void init() {
        setLayout(new BorderLayout());
        setOpaque(true);
        setBackground(BACKGROUND_COLOR);

        // Add background panel with gradient
        JPanel backgroundPanel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                
                // Create a gradient background similar to GameView
                GradientPaint gp = new GradientPaint(0, 0, new Color(18, 85, 47), 
                                                      0, (float) getHeight() / 2, new Color(26, 117, 65));
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, getWidth(), getHeight() / 2);
                
                GradientPaint gp2 = new GradientPaint(0, (float) getHeight() / 2, new Color(26, 117, 65), 
                                                       0, getHeight(), new Color(18, 85, 47));
                g2d.setPaint(gp2);
                g2d.fillRect(0, getHeight() / 2, getWidth(), getHeight() / 2);
            }
        };
        backgroundPanel.setOpaque(true);

        // Top panel: back button
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 15));
        top.setOpaque(false);
        JButton back = modernButton("← Indietro");
        back.addActionListener(e -> {
            CardLayout cl = (CardLayout) cards.getLayout();
            cl.show(cards, "MENU");
        });
        top.add(back);

        backgroundPanel.add(top, BorderLayout.NORTH);

        // Main content panel
        JPanel mainContent = new JPanel(new BorderLayout(0, 20));
        mainContent.setOpaque(false);
        mainContent.setBorder(new EmptyBorder(10, 40, 20, 40));

        // -------------------- PROFILE CARD ---------------------
        JPanel profileCard = createProfileCard();
        mainContent.add(profileCard, BorderLayout.NORTH);

        // -------------------- HISTORY PANEL ---------------------
        JPanel historyPanel = createHistoryPanel();
        mainContent.add(historyPanel, BorderLayout.CENTER);

        backgroundPanel.add(mainContent, BorderLayout.CENTER);
        add(backgroundPanel, BorderLayout.CENTER);
    }

    private JPanel createProfileCard() {
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Semi-transparent background with gradient
                GradientPaint gradient = new GradientPaint(
                    0, 0, new Color(10, 60, 35, 200),
                    0, getHeight(), new Color(5, 40, 25, 220)
                );
                g2d.setPaint(gradient);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
            }
        };
        card.setLayout(new BorderLayout(20, 0));
        card.setOpaque(false);
        card.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDER_COLOR, 2, true),
                new EmptyBorder(25, 25, 25, 25)
        ));

        // Left side: Avatar with level badge
        JPanel avatarPanel = new JPanel(new BorderLayout());
        avatarPanel.setOpaque(false);

        // Avatar with custom painting for level badge
        JPanel avatarContainer = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.dispose();
            }
        };
        avatarContainer.setLayout(new BorderLayout());
        avatarContainer.setOpaque(false);
        avatarContainer.setPreferredSize(new Dimension(130, 150));

        avatarLabel = new JLabel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Rounded border with gold color
                g2d.setColor(new Color(255, 215, 0)); // Gold
                g2d.setStroke(new BasicStroke(3));
                g2d.drawRoundRect(2, 2, getWidth()-4, getHeight()-4, 15, 15);

                g2d.dispose();
                super.paintComponent(g);
            }
        };
        avatarLabel.setHorizontalAlignment(SwingConstants.CENTER);
        avatarLabel.setVerticalAlignment(SwingConstants.CENTER);
        avatarLabel.setPreferredSize(new Dimension(120, 120));
        avatarLabel.setOpaque(true);
        avatarLabel.setBackground(new Color(30, 30, 30));

        avatarContainer.add(avatarLabel, BorderLayout.NORTH);

        // Level badge below avatar
        levelLabel = new JLabel("Livello 1", SwingConstants.CENTER);
        levelLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        levelLabel.setForeground(Color.WHITE);
        levelLabel.setOpaque(true);
        levelLabel.setBackground(LEVEL_BADGE_BG);
        levelLabel.setBorder(new EmptyBorder(5, 10, 5, 10));
        levelLabel.setPreferredSize(new Dimension(120, 30));

        avatarContainer.add(levelLabel, BorderLayout.SOUTH);
        avatarPanel.add(avatarContainer, BorderLayout.NORTH);

        JButton changeAvatar = modernButton("Cambia Avatar");
        changeAvatar.addActionListener(e -> onChangeAvatar());
        changeAvatar.setPreferredSize(new Dimension(120, 35));
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.setOpaque(false);
        buttonPanel.add(changeAvatar);
        avatarPanel.add(buttonPanel, BorderLayout.SOUTH);

        // Right side: User info and stats
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setOpaque(false);

        // Name section
        JPanel namePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        namePanel.setOpaque(false);
        JLabel nameTitle = new JLabel("Nome:");
        nameTitle.setFont(new Font("Georgia", Font.BOLD, 16));
        nameTitle.setForeground(new Color(255, 215, 0)); // Gold
        nameField = new JTextField(20);
        nameField.setFont(new Font("Arial", Font.PLAIN, 15));
        nameField.setPreferredSize(new Dimension(200, 32));
        nameField.setBackground(new Color(30, 30, 30));
        nameField.setForeground(TEXT_COLOR);
        nameField.setCaretColor(TEXT_COLOR);
        nameField.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDER_COLOR, 2, true),
                new EmptyBorder(5, 10, 5, 10)
        ));
        namePanel.add(nameTitle);
        namePanel.add(nameField);

        JButton saveName = modernButton("Salva");
        saveName.addActionListener(e -> onSaveName());
        namePanel.add(saveName);

        infoPanel.add(namePanel);
        infoPanel.add(Box.createVerticalStrut(20));

        // Stats section
        statsLabel = new JLabel();
        statsLabel.setFont(new Font("Georgia", Font.BOLD, 16));
        statsLabel.setForeground(new Color(255, 215, 0)); // Gold
        statsLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        infoPanel.add(statsLabel);
        infoPanel.add(Box.createVerticalStrut(15));

        // Experience bar section
        JLabel expTitle = new JLabel("Esperienza:");
        expTitle.setFont(new Font("Georgia", Font.BOLD, 14));
        expTitle.setForeground(new Color(255, 215, 0)); // Gold
        expTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        infoPanel.add(expTitle);
        infoPanel.add(Box.createVerticalStrut(5));

        // Custom experience progress bar
        expBar = createExperienceBar();
        infoPanel.add(expBar);
        infoPanel.add(Box.createVerticalStrut(5));

        expLabel = new JLabel();
        expLabel.setFont(new Font("Arial", Font.PLAIN, 13));
        expLabel.setForeground(SECONDARY_TEXT_COLOR);
        expLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        infoPanel.add(expLabel);

        card.add(avatarPanel, BorderLayout.WEST);
        card.add(infoPanel, BorderLayout.CENTER);

        return card;
    }

    private JPanel createHistoryPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 15)) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Semi-transparent background
                g2d.setColor(new Color(0, 0, 0, 128));
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
            }
        };
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDER_COLOR, 2, true),
                new EmptyBorder(15, 15, 15, 15)
        ));

        JLabel historyLabel = new JLabel("Storico Partite");
        historyLabel.setFont(new Font("Georgia", Font.BOLD, 22));
        historyLabel.setForeground(new Color(255, 215, 0)); // Gold color
        panel.add(historyLabel, BorderLayout.NORTH);

        // Table setup
        String[] cols = {"Data", "Avversario", "Vincitore", "Punteggio"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };

        historyTable = new JTable(tableModel);
        historyTable.setFont(new Font("Arial", Font.PLAIN, 13));
        historyTable.setRowHeight(32);
        historyTable.setShowGrid(false);
        historyTable.setIntercellSpacing(new Dimension(0, 0));
        historyTable.setBackground(new Color(20, 50, 30, 150));
        historyTable.setForeground(TEXT_COLOR);
        historyTable.setSelectionBackground(new Color(255, 215, 0, 50));
        historyTable.setSelectionForeground(TEXT_COLOR);

        // Header styling
        historyTable.getTableHeader().setFont(new Font("Georgia", Font.BOLD, 14));
        historyTable.getTableHeader().setBackground(PRIMARY_COLOR);
        historyTable.getTableHeader().setForeground(new Color(255, 215, 0));
        historyTable.getTableHeader().setPreferredSize(new Dimension(0, 40));
        historyTable.getTableHeader().setBorder(BorderFactory.createEmptyBorder());

        // Cell renderer for alternating row colors
        DefaultTableCellRenderer cellRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (!isSelected) {
                    c.setBackground(row % 2 == 0 ? new Color(20, 50, 30, 150) : new Color(15, 40, 25, 150));
                }
                setForeground(TEXT_COLOR);
                setBorder(new EmptyBorder(5, 10, 5, 10));
                return c;
            }
        };
        for (int i = 0; i < historyTable.getColumnCount(); i++) {
            historyTable.getColumnModel().getColumn(i).setCellRenderer(cellRenderer);
        }

        JScrollPane sp = new JScrollPane(historyTable);
        sp.setPreferredSize(new Dimension(700, 280));
        sp.setBorder(new LineBorder(BORDER_COLOR, 2, true));
        sp.getViewport().setBackground(new Color(20, 50, 30, 150));
        sp.setOpaque(false);

        panel.add(sp, BorderLayout.CENTER);

        return panel;
    }

    private JProgressBar createExperienceBar() {
        JProgressBar bar = new JProgressBar(0, 100);
        bar.setStringPainted(false);
        bar.setPreferredSize(new Dimension(400, 25));
        bar.setMaximumSize(new Dimension(400, 25));
        bar.setAlignmentX(Component.LEFT_ALIGNMENT);
        bar.setForeground(EXP_BAR_COLOR); // Gold
        bar.setBackground(new Color(30, 30, 30, 180));
        bar.setBorder(new LineBorder(BORDER_COLOR, 2, true));
        bar.setUI(new javax.swing.plaf.basic.BasicProgressBarUI() {
            @Override
            protected void paintDeterminate(Graphics g, JComponent c) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int width = bar.getWidth();
                int height = bar.getHeight();
                int fillWidth = (int) (width * (bar.getPercentComplete()));

                // Background
                g2d.setColor(bar.getBackground());
                g2d.fillRoundRect(0, 0, width, height, 12, 12);

                // Fill with gradient (gold)
                if (fillWidth > 0) {
                    GradientPaint gradient = new GradientPaint(
                            0, 0, new Color(255, 215, 0),
                            fillWidth, 0, new Color(255, 185, 0)
                    );
                    g2d.setPaint(gradient);
                    g2d.fillRoundRect(1, 1, fillWidth - 1, height - 2, 11, 11);
                }

                g2d.dispose();
            }
        });
        return bar;
    }

    /**
     * Aggiorna la view dal modello; viene chiamato dal controller tramite observer.
     */
    @Override
    public void onProfileUpdated(UserProfile profile) {
        SwingUtilities.invokeLater(() -> {
            nameField.setText(profile.getUsername());
            if (profile.getAvatarPath() != null) {
                setAvatarFromPath(profile.getAvatarPath());
            } else {
                avatarLabel.setIcon(null);
                avatarLabel.setText("Nessun avatar");
                avatarLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                avatarLabel.setForeground(SECONDARY_TEXT_COLOR);
            }

            // Update level and experience
            levelLabel.setText("Livello " + profile.getLevel());
            int expPercent = (int) profile.getProgressPercentage();
            expBar.setValue(expPercent);
            expLabel.setText(String.format("%d / %d XP (%d%%)",
                    profile.getExperience(),
                    profile.getExperienceToNextLevel(),
                    expPercent));

            // Statistiche vittorie-sconfitte
            int wins = profile.getWinsNumber();
            int losses = profile.getTotalGames() - wins;
            statsLabel.setText(String.format("Vittorie: %d  |  Sconfitte: %d  |  Totale: %d",
                    wins, losses, profile.getTotalGames()));

            tableModel.setRowCount(0);
            List<GamesRecord> history = profile.getHistory();
            for (GamesRecord m : history) {
                // Vincitore: Nome + (punteggio vincitore)
                String winnerCell = m.getWinner();
                if (m.getWinnerScore() != null && !m.getWinnerScore().isEmpty())
                    winnerCell += " (" + m.getWinnerScore() + ")";

                tableModel.addRow(new Object[]{
                        m.getFormattedDate(),
                        m.getOpponent(),
                        winnerCell,
                        m.getMyScore()});
            }
        });
    }

    @Override
    public void onProfileSaveFailed(Exception ex) {
        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(this, "Salvataggio profilo fallito: " + ex.getMessage(), "Errore", JOptionPane.ERROR_MESSAGE);
        });
    }

    private void onSaveName() {
        String newName = nameField.getText().trim();
        if (!newName.isEmpty()) {
            controller.setName(newName);
        } else {
            JOptionPane.showMessageDialog(this, "Il nome non può essere vuoto.", "Errore", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onChangeAvatar() {
        JFileChooser chooser = new JFileChooser();
        int res = chooser.showOpenDialog(this);
        if (res == JFileChooser.APPROVE_OPTION) {
            File f = chooser.getSelectedFile();
            controller.setAvatar(f);
        }
    }

    private void setAvatarFromPath(String path) {
        try {
            BufferedImage img = ImageIO.read(new File(path));
            if (img != null) {
                Image scaled = img.getScaledInstance(120, 120, Image.SCALE_SMOOTH);
                avatarLabel.setIcon(new ImageIcon(scaled));
                avatarLabel.setText("");
            } else {
                avatarLabel.setIcon(null);
                avatarLabel.setText("Avatar non valido");
                avatarLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                avatarLabel.setForeground(SECONDARY_TEXT_COLOR);
            }
        } catch (IOException ex) {
            avatarLabel.setIcon(null);
            avatarLabel.setText("Errore caricamento");
            avatarLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            avatarLabel.setForeground(SECONDARY_TEXT_COLOR);
        }
    }

    public void refreshFromModel() {
        UserProfile p = controller.getProfile();
        if (p != null) onProfileUpdated(p);
    }

    // Helper per bottoni moderni
    private JButton modernButton(String text) {
        JButton btn = new JButton(text);
        btn.setFocusPainted(false);
        btn.setBackground(PRIMARY_COLOR);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setBorder(new EmptyBorder(8, 20, 8, 20));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setOpaque(true);

        // Hover effect
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btn.setBackground(ACCENT_COLOR);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btn.setBackground(PRIMARY_COLOR);
            }
        });

        return btn;
    }
}