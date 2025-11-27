package ui;

import game.*;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.Objects;

/**
 * Dialog minimale che:
 * - mostra lo stato di gioco testuale (lista carte in mano del player umano)
 * - polla lo stato per verificare se è il turno dell'umano e abilita i controlli
 *
 * Funziona con GameEngine che viene eseguito in background: HumanPlayer.chooseCard()
 * viene sbloccato chiamando submitCardChoice.
 */
public class GameDialog extends JDialog {
    private final Engine engine;
    private final GiocatoreUmano human;
    private final GameState state;
    private final DefaultListModel<String> handModel = new DefaultListModel<>();
    private final JList<String> handList = new JList<>(handModel);
    private final JTextArea logArea = new JTextArea(10,40);
    private final JButton playBtn = new JButton("Gioca carta");

    private final Timer pollTimer;

    // stato usato per evitare aggiornamenti inutili della UI
    private int lastHandSize = -1;
    private String lastCurrentName = null;

    public GameDialog(Window owner, Engine engine, GiocatoreUmano giocatore) {
        super(owner, "Partita in corso", ModalityType.MODELESS);
        this.engine = engine;
        this.human = giocatore;
        this.state = engine.getState();

        initUI();

        // poll ogni 1000ms per verificare se è il turno dell'umano (ridotto da 300ms)
        pollTimer = new Timer(1000, e -> refresh());
        pollTimer.start();

        pack();
        setLocationRelativeTo(owner);
    }

    private void initUI() {
        setLayout(new BorderLayout(8,8));

        // hand panel
        JPanel left = new JPanel(new BorderLayout(4,4));
        left.add(new JLabel("La tua mano:"), BorderLayout.NORTH);
        handList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        left.add(new JScrollPane(handList), BorderLayout.CENTER);
        playBtn.setEnabled(false);
        playBtn.addActionListener(e -> playSelected());
        left.add(playBtn, BorderLayout.SOUTH);

        // log panel
        logArea.setEditable(false);
        logArea.setLineWrap(true);
        add(left, BorderLayout.CENTER);
        add(new JScrollPane(logArea), BorderLayout.EAST);

        JButton close = new JButton("Chiudi");
        close.addActionListener(e -> {
            pollTimer.stop();
            dispose();
        });
        add(close, BorderLayout.SOUTH);

        setPreferredSize(new Dimension(900, 420));
    }

    private void refresh() {
        // aggiorna mano umana
        List<game.Cards> hand = state.getHand(human);
        Giocatore current = state.getCurrentPlayer();

        String currentName = (current != null) ? current.getName() : null;
        int handSize = (hand != null) ? hand.size() : 0;

        // Se lo stato (player corrente o dimensione mano) non è cambiato, evitiamo di ricostruire la lista
        boolean stateChanged = (handSize != lastHandSize) || !Objects.equals(currentName, lastCurrentName);

        boolean myTurn = current != null && current instanceof GiocatoreUmano && current.getName().equals(human.getName());
        playBtn.setEnabled(myTurn && handSize > 0);

        if (stateChanged) {
            // ricostruisci la lista mano solo quando serve (evita di perdere la selezione ad ogni tick)
            handModel.clear();
            for (int i = 0; i < handSize; i++) {
                handModel.addElement("[" + i + "] " + hand.get(i).toString());
            }

            if (myTurn) {
                log("È il tuo turno — scegli una carta.");
            }

            // aggiorna stato di confronto
            lastHandSize = handSize;
            lastCurrentName = currentName;
        }
    }

    private void playSelected() {
        int sel = handList.getSelectedIndex();
        if (sel < 0) {
            JOptionPane.showMessageDialog(this, "Seleziona una carta dalla lista", "Attenzione", JOptionPane.WARNING_MESSAGE);
            return;
        }
        human.submitCardChoice(sel);
        log("Hai giocato carta " + sel);
        // disabilitiamo finché non sarà di nuovo il turno
        playBtn.setEnabled(false);
    }

    public void log(String s) {
        logArea.append(s + "\n");
        logArea.setCaretPosition(logArea.getDocument().getLength());
    }
}