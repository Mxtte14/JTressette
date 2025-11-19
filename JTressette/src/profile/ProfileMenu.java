package profile;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;

public class ProfileMenu extends JPanel {
    private final JPanel cards;
    private final UserProfile profile;

    private JTextField nameField;
    private JLabel avatarLabel;
    private JTable historyTable;
    private DefaultTableModel tableModel;

    public ProfileMenu(JPanel cards, UserProfile profile) {
        this.cards = cards;
        this.profile = profile;
        init();
        refreshFromModel();
    }

    private void init() {
        setLayout(new BorderLayout(10,10));

        // Top: header + back button
        JPanel top = new JPanel(new BorderLayout());
        JButton back = new JButton("Indietro");
        back.addActionListener(e -> {
            CardLayout cl = (CardLayout) cards.getLayout();
            cl.show(cards, "MENU");
        });
        top.add(back, BorderLayout.WEST);
        add(top, BorderLayout.NORTH);

        // Center: profile form and history
        JPanel center = new JPanel(new GridLayout(1,2,10,10));

        // Left: avatar + change
        JPanel left = new JPanel(new BorderLayout(8,8));
        avatarLabel = new JLabel();
        avatarLabel.setHorizontalAlignment(SwingConstants.CENTER);
        avatarLabel.setPreferredSize(new Dimension(200,200));
        left.add(avatarLabel, BorderLayout.CENTER);

        JButton changeAvatar = new JButton("Cambia Avatar");
        changeAvatar.addActionListener(e -> onChangeAvatar());
        left.add(changeAvatar, BorderLayout.SOUTH);

        // Right: name + history
        JPanel right = new JPanel(new BorderLayout(8,8));
        JPanel namePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        namePanel.add(new JLabel("Nome:"));
        nameField = new JTextField(20);
        namePanel.add(nameField);
        JButton saveName = new JButton("Salva Nome");
        saveName.addActionListener(e -> onSaveName());
        namePanel.add(saveName);

        right.add(namePanel, BorderLayout.NORTH);

        // History table
        String[] cols = {"Data", "Avversario", "Risultato"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };
        historyTable = new JTable(tableModel);
        JScrollPane sp = new JScrollPane(historyTable);
        right.add(sp, BorderLayout.CENTER);

        center.add(left);
        center.add(right);

        add(center, BorderLayout.CENTER);
    }

    public void refreshFromModel() {
        // update name
        nameField.setText(profile.getName());

        // update avatar
        if (profile.getAvatarPath() != null) {
            setAvatarFromPath(profile.getAvatarPath());
        } else {
            avatarLabel.setIcon(null);
            avatarLabel.setText("Nessun avatar");
        }

        // update history table
        tableModel.setRowCount(0);
        List<GamesRecord> history = profile.getHistory();
        for (GamesRecord m : history) {
            tableModel.addRow(new Object[]{m.getDate(), m.getOpponent(), m.getResult()});
        }
    }

    private void onSaveName() {
        String newName = nameField.getText().trim();
        if (!newName.isEmpty()) {
            profile.setName(newName);
            JOptionPane.showMessageDialog(this, "Nome salvato.");
            // persist profile here if needed
        } else {
            JOptionPane.showMessageDialog(this, "Il nome non può essere vuoto.", "Errore", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onChangeAvatar() {
        JFileChooser chooser = new JFileChooser();
        int res = chooser.showOpenDialog(this);
        if (res == JFileChooser.APPROVE_OPTION) {
            File f = chooser.getSelectedFile();
            profile.setAvatarPath(f.getAbsolutePath());
            setAvatarFromPath(f.getAbsolutePath());
            // persist profile here if needed
        }
    }

    private void setAvatarFromPath(String path) {
        try {
            BufferedImage img = ImageIO.read(new File(path));
            if (img != null) {
                Image scaled = img.getScaledInstance(200, 200, Image.SCALE_SMOOTH);
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
}