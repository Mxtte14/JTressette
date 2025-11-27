package profile;

import java.io.Serializable;

/**
 * Rappresenta un record di partita. Reso JavaBean e Serializable per
 * compatibilità sia con ObjectOutputStream sia con XMLEncoder/XMLDecoder.
 */
public class GamesRecord implements Serializable {
    private static final long serialVersionUID = 1L;

    private String date;
    private String opponent;
    private String result;

    // costruttore no-arg richiesto da XMLEncoder / JavaBeans
    public GamesRecord() {
    }

    public GamesRecord(String date, String opponent, String result) {
        this.date = date;
        this.opponent = opponent;
        this.result = result;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getOpponent() {
        return opponent;
    }

    public void setOpponent(String opponent) {
        this.opponent = opponent;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    @Override
    public String toString() {
        return "GamesRecord{" +
                "date='" + date + '\'' +
                ", opponent='" + opponent + '\'' +
                ", result='" + result + '\'' +
                '}';
    }
}