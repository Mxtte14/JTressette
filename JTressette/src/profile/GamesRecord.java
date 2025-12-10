package profile;

import java.io.Serializable;

/**
 * Rappresenta un record di partita. Reso JavaBean e Serializable per
 * compatibilità con diverse modalità di storage.
 */
public class GamesRecord implements Serializable {
    private static final long serialVersionUID = 1L;

    private String date;
    private String opponent;
    private String result;
    private String scaledScore;

    // costruttore no-arg richiesto da JavaBeans
    public GamesRecord() {
    }

    public GamesRecord(String date, String opponent, String result, String scaledScore) {
        this.date = date;
        this.opponent = opponent;
        this.result = result;
        this.scaledScore = scaledScore;
    }

    public String getDate() {
        return date;
    }

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

    public String getOpponent() {
        return opponent;
    }

    public String getResult() {
        return result;
    }

    /**
     * Restituisce la stringa del punteggio già calcolata (es. "1 2/3").
     */
    public String getScaledScore() {
        return scaledScore;
    }


    @Override
    public String toString() {
        return "GamesRecord{" +
                "date ='" + date + '\'' +
                ", opponent ='" + opponent + '\'' +
                ", winner ='" + result + '\'' +
                ", your scaledScore ='" + scaledScore + '\'' +
                '}';
    }
}