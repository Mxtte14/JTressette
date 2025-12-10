package profile;

import controller.ProfileController;
import controller.ProfileListener;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
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
        setBackground(new Color(245, 245, 250));

        // Top panel: back button
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.setOpaque(false);
        JButton back = modernButton("Indietro");
        back.addActionListener(e -> {
            CardLayout cl = (CardLayout) cards.getLayout();
            cl.show(cards, "MENU");
        });
        top.add(back);

        add(top, BorderLayout.NORTH);

        // -------------------- PROFILE PANEL ---------------------

        JPanel profilePanel = new JPanel();
        profilePanel.setLayout(new GridBagLayout());
        profilePanel.setOpaque(false);
        profilePanel.setBorder(new EmptyBorder(10, 40, 10, 40));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10,10,10,10);
        gbc.fill = GridBagConstraints.BOTH;

        // Avatar
        avatarLabel = new JLabel();
        avatarLabel.setHorizontalAlignment(SwingConstants.CENTER);
        avatarLabel.setPreferredSize(new Dimension(110,110));
        avatarLabel.setBorder(new LineBorder(new Color(176,196,222), 2, true));
        avatarLabel.setOpaque(true);
        avatarLabel.setBackground(new Color(230,236,245));

        gbc.gridx=0; gbc.gridy=0; gbc.gridheight=2; gbc.gridwidth=1;
        profilePanel.add(avatarLabel, gbc);

        JButton changeAvatar = modernButton("Cambia Avatar");
        changeAvatar.addActionListener(e -> onChangeAvatar());
        gbc.gridx=0; gbc.gridy=2; gbc.gridheight=1; gbc.gridwidth=1;
        profilePanel.add(changeAvatar, gbc);

        // Dati utente a destra avatar
        JPanel userDataPanel = new JPanel();
        userDataPanel.setLayout(new BoxLayout(userDataPanel, BoxLayout.Y_AXIS));
        userDataPanel.setOpaque(false);
        userDataPanel.setBorder(new EmptyBorder(10,10,10,10));

        JPanel namePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        namePanel.setOpaque(false);
        JLabel nameTitle = new JLabel("Nome:");
        nameTitle.setFont(new Font("Segoe UI", Font.BOLD, 15));
        nameField = new JTextField(20);
        nameField.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        nameField.setMaximumSize(new Dimension(200, 28));
        namePanel.add(nameTitle);
        namePanel.add(nameField);

        JButton saveName = modernButton("Salva Nome");
        saveName.addActionListener(e -> onSaveName());
        namePanel.add(saveName);

        userDataPanel.add(namePanel);

        // Statistiche vittorie-sconfitte
        statsLabel = new JLabel();
        statsLabel.setFont(new Font("Segoe UI", Font.BOLD, 17));
        statsLabel.setForeground(new Color(51, 102, 153));
        statsLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        userDataPanel.add(Box.createVerticalStrut(8));
        userDataPanel.add(statsLabel);

        gbc.gridx=1; gbc.gridy=0; gbc.gridheight=2; gbc.gridwidth=1;
        profilePanel.add(userDataPanel, gbc);

        add(profilePanel, BorderLayout.CENTER);

        // -------------------- HISTORY (INFERIOR SCROLL PANEL) ---------------------

        JPanel historyPanel = new JPanel(new BorderLayout());
        historyPanel.setBorder(new EmptyBorder(16,32,16,32));
        historyPanel.setOpaque(false);

        JLabel historyLabel = new JLabel("Storico partite");
        historyLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        historyLabel.setForeground(new Color(51, 51, 120));
        historyLabel.setBorder(new EmptyBorder(0,4,12,4));
        historyPanel.add(historyLabel, BorderLayout.NORTH);

        String[] cols = {"Data", "Avversario", "Vincitore", "Punteggio"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };

        historyTable = new JTable(tableModel);
        historyTable.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        historyTable.setRowHeight(26);
        historyTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        historyTable.setFillsViewportHeight(true);

        JScrollPane sp = new JScrollPane(historyTable);
        sp.setPreferredSize(new Dimension(480, 260));
        sp.setBorder(new LineBorder(new Color(176,196,222), 1, true));
        historyPanel.add(sp, BorderLayout.CENTER);

        add(historyPanel, BorderLayout.SOUTH);
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
            }

            // Statistiche vittorie-sconfitte (es: 10-7)
            int wins = profile.getWinsNumber();
            int losses = profile.getTotalGames() - wins;
            statsLabel.setText("Vittorie - Sconfitte: "
                    + wins + " - " + losses);

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
                Image scaled = img.getScaledInstance(110, 110, Image.SCALE_SMOOTH);
                avatarLabel.setIcon(new ImageIcon(scaled));
                avatarLabel.setText("");
            } else {
                avatarLabel.setIcon(null);
                avatarLabel.setText("Avatar non valido");
            }
        } catch (IOException ex) {
            avatarLabel.setIcon(null);
            avatarLabel.setText("Impossibile aprire immagine");
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
        btn.setBackground(new Color(51,102,153));
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setBorder(new EmptyBorder(7,18,7,18));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }
}