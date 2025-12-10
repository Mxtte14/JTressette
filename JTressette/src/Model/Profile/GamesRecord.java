package Model.Profile;

import java.io.Serializable;

/**
 * Rappresenta un record di partita compatto.
 * Ogni record rappresenta la partita dal punto di vista dell'utente loggato.
 */
public class GamesRecord implements Serializable {
    private static final long serialVersionUID = 1L;

    private String date;         // Data ISO
    private String opponent;     // Avversario
    private String winner;       // Vincitore della partita
    private String winnerScore;  // Punteggio del vincitore (es: 2/3)
    private String myScore;      // Punteggio dell'utente (es: 1 2/3)
    private int myPoints;        // Punti raw dell'utente
    private int myCardsWon;      // Numero di carte vinte dall'utente

    public GamesRecord() {}

    public GamesRecord(String date, String opponent, String winner, String winnerScore, String myScore) {
        this.date = date;
        this.opponent = opponent;
        this.winner = winner;
        this.winnerScore = winnerScore;
        this.myScore = myScore;
        this.myPoints = 0;
        this.myCardsWon = 0;
    }

    public GamesRecord(String date, String opponent, String winner, String winnerScore, String myScore, int myPoints, int myCardsWon) {
        this.date = date;
        this.opponent = opponent;
        this.winner = winner;
        this.winnerScore = winnerScore;
        this.myScore = myScore;
        this.myPoints = myPoints;
        this.myCardsWon = myCardsWon;
    }

    public String getDate() { return date; }

    public String getFormattedDate() {
        if (date == null || date.isEmpty()) return "";
        try {
            java.time.ZonedDateTime zdt = java.time.ZonedDateTime.parse(date);
            java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy");
            return zdt.format(formatter);
        } catch (Exception e) {
            // fallback se formato non ISO
            return date;
        }
    }

    public String getOpponent() { return opponent; }

    public String getWinner() { return winner; }

    public String getWinnerScore() { return winnerScore; }

    /**
     * Punteggio dell'utente loggato in questa partita.
     */
    public String getMyScore() { return myScore; }

    public int getMyPoints() { return myPoints; }

    public int getMyCardsWon() { return myCardsWon; }

    @Override
    public String toString() {
        return "GamesRecord{date='" + date +
                "', opponent='" + opponent +
                "', winner='" + winner +
                "', winnerScore='" + winnerScore +
                "', myScore='" + myScore +
                "', myPoints=" + myPoints +
                ", myCardsWon=" + myCardsWon + "}";
    }
}