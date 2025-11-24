package ui;

import game.Engine;
import game.GameState;
import game.GiocatoreUmano;
import game.Player;

import javax.swing.*;
import java.awt.*;
import java.util.List;

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

    public GameDialog(Window owner, Engine engine, GiocatoreUmano giocatore) {
        super(owner, "Partita in corso", ModalityType.MODELESS);
        this.engine = engine;
        this.human = giocatore;
        this.state = engine.getState();

        initUI();

        // poll ogni 300ms per verificare se è il turno dell'humano
        pollTimer = new Timer(300, e -> refresh());
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
        handModel.clear();
        for (int i = 0; i < hand.size(); i++) {
            handModel.addElement("[" + i + "] " + hand.get(i).toString());
        }

        // se è il turno dell'humano abilitiamo il controllo
        Player current = state.getCurrentPlayer();
        boolean myTurn = current != null && current instanceof GiocatoreUmano && current.getName().equals(human.getName());
        playBtn.setEnabled(myTurn && !hand.isEmpty());
        if (myTurn) {
            log("È il tuo turno — scegli una carta.");
        }
    }

    private void playSelected() {
        int sel = handList.getSelectedIndex();
        if (sel < 0) {
            JOptionPane.showMessageDialog(this, "Seleziona una carta dalla lista", "Attenzione", JOptionPane.WARNING_MESSAGE);
            return;
        }
        human.submitCardChoice(sel);
        log("Hai giocato carta indice " + sel);
        // disabilitiamo finché non sarà di nuovo il turno
        playBtn.setEnabled(false);
    }

    public void log(String s) {
        logArea.append(s + "\n");
        logArea.setCaretPosition(logArea.getDocument().getLength());
    }
}
