package Model.Game;

public class Cards {
    public enum Segno {DENARA, SPADE, BASTONI, COPPE;}
    public enum Rank {
        TRE(10), DUE(9), ASSO(8), RE(7),
        CAVALLO(6), ALFIERE(5), SETTE(4), SEI(3), CINQUE(2),
        QUATTRO(1);

        private final int value;
        Rank(int value) {this.value = value;}
        public int getPriority() {return value; }
    }

    private final Segno segno;
    private final Rank rank;

    public Cards(Segno segno, Rank rank) {
        this.segno = segno;
        this.rank = rank;
    }

    public Segno getSegno() {return segno;}
    public Rank getRank() {return rank;}
    public int getPriority() {return rank.getPriority();}

    @Override
    public String toString() {
        return segno.name() + " " + rank.name();
    }
}
