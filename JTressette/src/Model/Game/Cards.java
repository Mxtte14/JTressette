package Model.Game;

/**
 * Rappresenta una carta da gioco italiana del gioco Tressette.
 * Ogni carta è definita da un seme (Segno) e un valore (Rank).
 * Le carte hanno priorità diverse che determinano quale carta vince in una presa.
 */
public class Cards {
    /**
     * Enumerazione dei semi delle carte italiane.
     * I quattro semi tradizionali sono: DENARA, SPADE, BASTONI e COPPE.
     */
    public enum Segno {
        /** Seme delle denara (monete) */
        DENARA,
        /** Seme delle spade */
        SPADE,
        /** Seme dei bastoni */
        BASTONI,
        /** Seme delle coppe */
        COPPE;
    }

    /**
     * Enumerazione dei valori (ranghi) delle carte del mazzo italiano.
     * Ogni valore ha una priorità associata che determina la forza della carta in gioco.
     * Le priorità vanno da 10 (TRE, carta più forte) a 1 (QUATTRO, carta più debole).
     */
    public enum Rank {
        /** Tre - carta con priorità massima (10) */
        TRE(10),
        /** Due - carta con priorità 9 */
        DUE(9),
        /** Asso - carta con priorità 8 */
        ASSO(8),
        /** Re - carta con priorità 7 */
        RE(7),
        /** Cavallo - carta con priorità 6 */
        CAVALLO(6),
        /** Alfiere (fante) - carta con priorità 5 */
        ALFIERE(5),
        /** Sette - carta con priorità 4 */
        SETTE(4),
        /** Sei - carta con priorità 3 */
        SEI(3),
        /** Cinque - carta con priorità 2 */
        CINQUE(2),
        /** Quattro - carta con priorità minima (1) */
        QUATTRO(1);

        /** Valore di priorità della carta */
        private final int value;

        /**
         * Costruttore del Rank.
         *
         * @param value valore di priorità della carta
         */
        Rank(int value) {this.value = value;}

        /**
         * Restituisce la priorità della carta.
         * La priorità determina quale carta vince quando si confrontano carte dello stesso seme.
         *
         * @return valore di priorità (da 1 a 10)
         */
        public int getPriority() {return value; }
    }

    /** Seme della carta */
    private final Segno segno;

    /** Valore/rango della carta */
    private final Rank rank;

    /**
     * Costruttore di una carta da gioco.
     *
     * @param segno il seme della carta (DENARA, SPADE, BASTONI, COPPE)
     * @param rank il valore/rango della carta
     */
    public Cards(Segno segno, Rank rank) {
        this.segno = segno;
        this.rank = rank;
    }

    /**
     * Restituisce il seme della carta.
     *
     * @return il seme della carta
     */
    public Segno getSegno() {return segno;}

    /**
     * Restituisce il valore/rango della carta.
     *
     * @return il rango della carta
     */
    public Rank getRank() {return rank;}

    /**
     * Restituisce la priorità della carta.
     * La priorità determina la forza della carta in una presa.
     *
     * @return valore di priorità (da 1 a 10)
     */
    public int getPriority() {return rank.getPriority();}

    /**
     * Restituisce una rappresentazione testuale della carta.
     * Il formato è "SEME VALORE" (es. "DENARA TRE").
     *
     * @return stringa che rappresenta la carta
     */
    @Override
    public String toString() {
        return segno.name() + " " + rank.name();
    }
}