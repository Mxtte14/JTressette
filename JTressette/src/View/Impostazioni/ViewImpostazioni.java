package View.Impostazioni;

import Model.Impostazioni.MenuImpostazioni;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class ViewImpostazioni extends JPanel {
    private final MenuImpostazioni settings;
    private final JSlider volumeSlider;
    private final JCheckBox effectsBox, showScoreBox, showMessagesBox, fullscreenBox;

    public ViewImpostazioni(MenuImpostazioni settings, Runnable onBack) {
        this.settings = settings != null ? settings : MenuImpostazioni.getInstance();

        setLayout(new GridBagLayout());
        setBackground(Color.BLACK);

        // Modern glass card panel (stile HomeMenu)
        JPanel card = new GlassPanel();
        card.setLayout(new GridBagLayout());
        card.setBorder(new EmptyBorder(24,40,32,40));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(12,6,8,6);
        gbc.gridx=0; gbc.gridwidth=2;

        JLabel title = new JLabel("IMPOSTAZIONI");
        title.setFont(new Font("Georgia", Font.BOLD, 32));
        title.setForeground(new Color(255,215,0));
        gbc.gridy=0;
        card.add(title, gbc);

        // Volume
        JLabel vlabel = new JLabel("Volume:");
        vlabel.setFont(new Font("Georgia", Font.BOLD, 18));
        vlabel.setForeground(Color.WHITE);
        volumeSlider = new JSlider(0, 100, settings.getVolume());
        volumeSlider.setMajorTickSpacing(20);
        volumeSlider.setPaintTicks(true);
        volumeSlider.setPaintLabels(true);
        volumeSlider.setPreferredSize(new Dimension(210,35));
        volumeSlider.addChangeListener(e -> { settings.setVolume(volumeSlider.getValue()); settings.save(); });

        gbc.gridy=1; gbc.gridwidth=1; card.add(vlabel, gbc);
        gbc.gridx=1; card.add(volumeSlider, gbc);

        // Effetti sonori
        gbc.gridx=0; gbc.gridy++;
        effectsBox = new JCheckBox("Effetti sonori", settings.isEffects());
        effectsBox.setFont(new Font("Georgia", Font.PLAIN, 16)); effectsBox.setOpaque(false); effectsBox.setForeground(Color.WHITE);
        effectsBox.addActionListener(e-> {settings.setEffects(effectsBox.isSelected()); settings.save();});
        card.add(effectsBox, gbc);

        // Mostra punteggio
        gbc.gridy++;
        showScoreBox = new JCheckBox("Punteggio utente visibile in partita", settings.isShowScore());
        showScoreBox.setFont(new Font("Georgia", Font.PLAIN, 16)); showScoreBox.setOpaque(false); showScoreBox.setForeground(Color.WHITE);
        showScoreBox.addActionListener(e-> {settings.setShowScore(showScoreBox.isSelected()); settings.save();});
        card.add(showScoreBox, gbc);

        // Mostra messaggi
        gbc.gridy++;
        showMessagesBox = new JCheckBox("Messaggi a lato visibili", settings.isShowMessages());
        showMessagesBox.setFont(new Font("Georgia", Font.PLAIN,16)); showMessagesBox.setOpaque(false); showMessagesBox.setForeground(Color.WHITE);
        showMessagesBox.addActionListener(e-> {settings.setShowMessages(showMessagesBox.isSelected()); settings.save();});
        card.add(showMessagesBox, gbc);

        // Schermo intero
        gbc.gridy++;
        fullscreenBox = new JCheckBox("Schermo intero", settings.isFullscreen());
        fullscreenBox.setFont(new Font("Georgia", Font.PLAIN, 16)); fullscreenBox.setOpaque(false); fullscreenBox.setForeground(Color.WHITE);
        fullscreenBox.addActionListener(e-> {settings.setFullscreen(fullscreenBox.isSelected()); settings.save();});
        card.add(fullscreenBox, gbc);


        // Bottone Indietro
        gbc.gridx=0; gbc.gridy++; gbc.gridwidth=2;
        JButton backBtn = modernButton("Indietro");
        backBtn.addActionListener(e-> {if (onBack!=null) onBack.run();});
        card.add(backBtn, gbc);

        add(card);
    }

    private JButton modernButton(String text) {
        JButton btn = new JButton(text);
        btn.setFocusPainted(false); btn.setBackground(new Color(51,102,153));
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Georgia", Font.BOLD, 15));
        btn.setBorder(new EmptyBorder(8,24,8,24));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    // Card stile HomeMenu
    public static class GlassPanel extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int arc = 28;
            GradientPaint gp = new GradientPaint(
                    0, 0, new Color(10, 60, 35, 210),
                    0, getHeight(), new Color(5, 40, 25, 235)
            );
            g2d.setPaint(gp);
            g2d.fillRoundRect(0, 0, getWidth(), getHeight(), arc, arc);

            g2d.setColor(new Color(255, 215, 0, 158));
            g2d.setStroke(new BasicStroke(2f));
            g2d.drawRoundRect(1, 1, getWidth()-2, getHeight()-2, arc, arc);

            g2d.dispose();
        }
    }
}