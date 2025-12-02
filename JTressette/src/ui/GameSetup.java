package ui;

import game.*;
import profile.UserProfile;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Dialog modulare e minimale che raccoglie:
 * - numero di bot (0..3)
 * - per ogni bot: nome e difficoltà
 *
 * Il nome del giocatore viene preso automaticamente dal profilo utente.
 * Dopo che l'utente preme "Avvia" la dialog si chiude e getPlayers() restituisce la lista.
 */
public class GameSetup extends JDialog {
    private final JLabel nameLabel;
    private final JComboBox<Integer> nbBox = new JComboBox<>(new Integer[]{0,1,2,3});
    private final JPanel botsPanel = new JPanel(new GridLayout(3, 1, 4, 4));
    private final List<JTextField> botNameFields = new ArrayList<>();
    private final List<JComboBox<Difficoltà>> botDiffBoxes = new ArrayList<>();

    private List<Giocatore> players = null;
    private final String playerName;

    public GameSetup(Window owner) {
        this(owner, null);
    }

    public GameSetup(Window owner, UserProfile profile) {
        super(owner, "Impostazioni partita", ModalityType.APPLICATION_MODAL);
        // Get player name from profile, or use default
        if (profile != null && profile.getUsername() != null && !profile.getUsername().isBlank()) {
            this.playerName = profile.getUsername();
        } else {
            this.playerName = System.getProperty("user.name", "Giocatore");
        }
        this.nameLabel = new JLabel(playerName);
        init();
    }

    private void init() {
        setLayout(new BorderLayout(8,8));
        JPanel main = new JPanel(new GridLayout(0,1,6,6));

        // Show player name (non-editable, taken from profile)
        JPanel pName = new JPanel(new FlowLayout(FlowLayout.LEFT));
        pName.add(new JLabel("Giocatore: "));
        nameLabel.setFont(nameLabel.getFont().deriveFont(Font.BOLD));
        nameLabel.setForeground(new Color(0, 100, 0));
        pName.add(nameLabel);
        main.add(pName);

        JPanel pNb = new JPanel(new FlowLayout(FlowLayout.LEFT));
        pNb.add(new JLabel("Numero di avversari (bot):"));
        pNb.add(nbBox);
        main.add(pNb);

        nbBox.addActionListener(e -> updateBotsPanel());

        // prepara i controlli per 3 bot (default nascosti/disabled)
        for (int i = 0; i < 3; i++) {
            JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT));
            JTextField botName = new JTextField(12);
            JComboBox<Difficoltà> diff = new JComboBox<>(Difficoltà.values());
            botNameFields.add(botName);
            botDiffBoxes.add(diff);
            row.add(new JLabel("Bot " + (i+1) + ":"));
            row.add(botName);
            row.add(new JLabel("Diff:"));
            row.add(diff);
            botsPanel.add(row);
        }
        updateBotsPanel();

        add(main, BorderLayout.NORTH);
        add(botsPanel, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton cancel = new JButton("Annulla");
        cancel.addActionListener(e -> { players = null; dispose(); });
        JButton start = new JButton("Avvia");
        start.addActionListener(e -> onStart());
        bottom.add(cancel);
        bottom.add(start);
        add(bottom, BorderLayout.SOUTH);

        pack();
        setLocationRelativeTo(getOwner());
    }

    private void updateBotsPanel() {
        int numberOfBots = (Integer) nbBox.getSelectedItem();
        Component[] comps = botsPanel.getComponents();
        for (int i = 0; i < comps.length; i++) {
            comps[i].setVisible(i < numberOfBots);
        }
        botsPanel.revalidate();
        botsPanel.repaint();
    }

    private void onStart() {
        players = new ArrayList<>();
        // human first - use name from profile
        players.add((Giocatore) new GiocatoreUmano(playerName));

        int nb = (Integer) nbBox.getSelectedItem();
        for (int i = 0; i < nb; i++) {
            String bn = botNameFields.get(i).getText().trim();
            if (bn.isEmpty()) bn = randomBotName();
            Difficoltà d = (Difficoltà) botDiffBoxes.get(i).getSelectedItem();
            players.add(new Bot(bn, d));
        }

        dispose();
    }

    private String randomBotName() {
        String[] names = {"Marco", "Marta", "Stefano", "Luca", "Anna", "Giulia", "Pippo", "Neri", "Mauro", "Sara", "Gino"};
        return "Bot-" + names[new Random().nextInt(names.length)];
    }

    /**
     * Mostra la dialog (modal). Restituisce la lista di players o null se annullato.
     */
    public List<Giocatore> showDialogAndGetPlayers() {
        setVisible(true);
        return players;
    }
}