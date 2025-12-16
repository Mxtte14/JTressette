package View.Impostazioni;

import Model.Impostazioni.MenuImpostazioni;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Pannello delle impostazioni del gioco JTressette.
 * Fornisce un'interfaccia grafica per modificare le preferenze utente incluse:
 * <ul>
 *   <li>Volume generale dell'audio (0-100)</li>
 *   <li>Abilitazione/disabilitazione effetti sonori</li>
 *   <li>Visualizzazione del punteggio durante la partita</li>
 *   <li>Visualizzazione dei messaggi di sistema</li>
 *   <li>Modalità schermo intero</li>
 * </ul>
 *
 * <p>Le modifiche vengono salvate automaticamente sul file system
 * quando l'utente interagisce con i controlli.</p>
 *
 * <p>Il pannello è trasparente per permettere la visualizzazione dello
 * sfondo del menu sottostante, mantenendo continuità visiva con la home screen.</p>
 *
 * @author JTressette Team
 * @version 1.0
 */
public class ViewImpostazioni extends JPanel {
    /** Istanza delle impostazioni globali del gioco */
    private final MenuImpostazioni settings;
    
    /** Slider per il controllo del volume (0-100) */
    private final JSlider volumeSlider;
    
    /** Checkbox per abilitare/disabilitare effetti sonori, punteggi, messaggi e fullscreen */
    private final JCheckBox effectsBox, showScoreBox, showMessagesBox, fullscreenBox;

    /**
     * Costruttore della vista impostazioni.
     * Inizializza tutti i controlli con i valori correnti delle impostazioni
     * e registra i listener per salvare automaticamente le modifiche.
     *
     * @param settings istanza delle impostazioni del gioco (se null, usa getInstance())
     * @param onBack callback da eseguire quando si preme il pulsante "Indietro"
     */
    public ViewImpostazioni(MenuImpostazioni settings, Runnable onBack) {
        this.settings = settings != null ? settings : MenuImpostazioni.getInstance();

        setLayout(new GridBagLayout());
        // Make panel transparent to show the menu background image underneath,
        // providing visual continuity with the home screen
        setOpaque(false);

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