package profile;

public class GamesRecord {
    private final String date;
    private final String opponent;
    private final String result;

    public GamesRecord(String date, String opponent, String result) {
        this.date = date;
        this.opponent = opponent;
        this.result = result;
    }

    public String getDate() { return date; }
    public String getOpponent() { return opponent; }
    public String getResult() { return result; }
}
