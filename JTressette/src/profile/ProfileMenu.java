package profile;

import controller.ProfileController;
import controller.ProfileListener;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * ProfileMenu: View che si registra come listener al ProfileController.
 * Non esegue I/O direttamente: chiama il controller per cambiare nome/avatar.
 */
public class ProfileMenu extends JPanel implements ProfileListener {
    private final JPanel cards;
    private final ProfileController controller;

    private JTextField nameField;
    private JLabel avatarLabel;
    private JTable historyTable;
    private DefaultTableModel tableModel;

    public ProfileMenu(JPanel cards, ProfileController controller) {
        this.cards = cards;
        this.controller = controller;
        init();

        // registrazione come observer
        if (this.controller != null) {
            this.controller.addListener(this);
            // inizializza la view dallo stato corrente
            UserProfile p = this.controller.getProfile();
            if (p != null) onProfileUpdated(p);
        }
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

    /**
     * Aggiorna la view dal modello; viene chiamato dal controller tramite observer.
     */
    @Override
    public void onProfileUpdated(UserProfile profile) {
        SwingUtilities.invokeLater(() -> {
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
            // delega al controller (controller si occupa di validare/salvare)
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
            // delega al controller (che potrà copiare il file nella cartella dell'app se desiderato)
            controller.setAvatar(f);
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

    /**
     * Metodo pubblico per forzare un refresh immediato dalla view esterna (MenuFrame).
     */
    public void refreshFromModel() {
        UserProfile p = controller.getProfile();
        if (p != null) onProfileUpdated(p);
    }
}