package View.Game;

import Model.Game.*;
import Model.Profile.UserProfile;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Dialog per la configurazione di una nuova partita di Tressette.
 * Permette all'utente di impostare:
 * <ul>
 *   <li>Numero di avversari bot (1-3)</li>
 *   <li>Nome personalizzato per ciascun bot</li>
 *   <li>Livello di difficoltà per ciascun bot (EASY, MEDIUM, HARD)</li>
 * </ul>
 *
 * <p>Il nome del giocatore umano viene preso automaticamente dal profilo utente
 * e non può essere modificato in questa schermata.</p>
 *
 * <p>La dialog è modale e blocca l'interazione con la finestra padre
 * fino alla conferma o cancellazione dell'utente.</p>
 *
 * @author JTressette Team
 * @version 1.0
 */
public class GameSetup extends JDialog {
    /** Label che mostra il nome del giocatore umano */
    private final JLabel nameLabel;
    
    /** ComboBox per selezionare il numero di bot (1-3) */
    private final JComboBox<Integer> nbBox = new JComboBox<>(new Integer[]{1,2,3});
    
    /** Pannello che contiene i campi per configurare i bot */
    private final JPanel botsPanel = new JPanel(new GridLayout(3, 1, 4, 4));
    
    /** Lista dei campi di testo per i nomi dei bot */
    private final List<JTextField> botNameFields = new ArrayList<>();
    
    /** Lista delle combobox per le difficoltà dei bot */
    private final List<JComboBox<Difficoltà>> botDiffBoxes = new ArrayList<>();

    /** Lista dei giocatori configurati (null se l'utente annulla) */
    private List<Giocatore> players = null;
    
    /** Nome del giocatore umano preso dal profilo */
    private final String playerName;

    /**
     * Costruttore del dialog di setup della partita.
     * Legge il nome del giocatore dal profilo utente e inizializza l'interfaccia.
     *
     * @param owner finestra padre del dialog
     * @param profile profilo utente da cui leggere il nome del giocatore
     */
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

    /**
     * Inizializza l'interfaccia grafica del dialog.
     * Crea i pannelli per visualizzare il nome del giocatore, selezionare il numero di bot,
     * configurare ogni bot e i pulsanti di conferma/annullamento.
     */
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

    /**
     * Aggiorna la visibilità dei pannelli di configurazione bot in base al numero selezionato.
     * Mostra solo i campi necessari per il numero di bot scelto dall'utente.
     */
    private void updateBotsPanel() {
        int numberOfBots = (Integer) nbBox.getSelectedItem();
        Component[] comps = botsPanel.getComponents();
        for (int i = 1; i < comps.length; i++) {
            comps[i].setVisible(i < numberOfBots);
        }
        botsPanel.revalidate();
        botsPanel.repaint();
    }

    /**
     * Gestisce il click sul pulsante "Avvia".
     * Crea la lista dei giocatori includendo il giocatore umano e i bot configurati.
     * Se il nome di un bot è vuoto, viene generato un nome casuale.
     * Chiude il dialog dopo aver creato la lista.
     */
    private void onStart() {
        players = new ArrayList<>();
        // human first - use name from profile
        players.add(new GiocatoreUmano(playerName));

        int nb = (Integer) nbBox.getSelectedItem();
        for (int i = 0; i < nb; i++) {
            String bn = botNameFields.get(i).getText().trim();
            if (bn.isEmpty()) bn = randomBotName();
            Difficoltà d = (Difficoltà) botDiffBoxes.get(i).getSelectedItem();
            players.add(new Bot(bn, d));
        }

        dispose();
    }

    /**
     * Genera un nome casuale per un bot.
     * Sceglie casualmente da un array di nomi predefiniti e aggiunge il prefisso "Bot-".
     *
     * @return nome casuale per il bot (es. "Bot-Marco", "Bot-Sara")
     */
    private String randomBotName() {
        String[] names = {"Marco", "Marta", "Stefano", "Luca", "Anna", "Giulia", "Pippo", "Neri", "Mauro", "Sara", "Gino"};
        return "Bot-" + names[new Random().nextInt(names.length)];
    }

    /**
     * Mostra il dialog modale e attende la risposta dell'utente.
     * Il metodo blocca fino a quando l'utente non preme "Avvia" o "Annulla".
     *
     * @return lista dei giocatori configurati (umano + bot), o null se l'utente ha annullato
     */
    public List<Giocatore> showDialogAndGetPlayers() {
        setVisible(true);
        return players;
    }
}