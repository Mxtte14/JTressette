package Model.Profile;

import java.io.Serializable;

/**
 * Rappresenta il record di una singola partita giocata.
 * Ogni record contiene tutte le informazioni necessarie per visualizzare
 * la partita nello storico dal punto di vista dell'utente loggato.
 * 
 * <p>Informazioni incluse:</p>
 * <ul>
 *   <li>Data e ora della partita</li>
 *   <li>Nome dell'avversario (o avversari concatenati)</li>
 *   <li>Vincitore della partita</li>
 *   <li>Punteggio del vincitore (formato scalato es. "2 1/3")</li>
 *   <li>Punteggio dell'utente (formato scalato)</li>
 *   <li>Esperienza guadagnata in questa partita</li>
 *   <li>Statistiche raw (punti e carte vinte)</li>
 * </ul>
 * 
 * <p>La classe è Serializable per permettere il salvataggio persistente.</p>
 */
public class GamesRecord implements Serializable {
    /** Versione di serializzazione per compatibilità */
    private static final long serialVersionUID = 1L;

    /** Data e ora della partita in formato ISO 8601 */
    private String date;
    
    /** Nome dell'avversario o lista degli avversari */
    private String opponent;
    
    /** Nome del vincitore della partita */
    private String winner;
    
    /** Punteggio del vincitore in formato scalato (es: "2 1/3") */
    private String winnerScore;
    
    /** Punteggio dell'utente in formato scalato (es: "1 2/3") */
    private String myScore;
    
    /** Punti esperienza guadagnati in questa partita */
    private int experience;
    
    /** Punti raw segnati dall'utente (non scalati) */
    private int myPoints;
    
    /** Numero di carte vinte dall'utente */
    private int myCardsWon;

    /**
     * Costruttore di default.
     * Inizializza un record vuoto.
     */
    public GamesRecord() {}

    /**
     * Costruttore per retrocompatibilità con vecchie versioni serializzate.
     * Utilizzato quando l'esperienza era l'ultimo parametro ma non c'erano myPoints e myCardsWon.
     * 
     * @param date data della partita in formato ISO
     * @param opponent nome dell'avversario
     * @param winner nome del vincitore
     * @param winnerScore punteggio del vincitore
     * @param myScore punteggio dell'utente
     * @param experience esperienza guadagnata
     */
    public GamesRecord(String date, String opponent, String winner, String winnerScore, String myScore, int experience) {
        this.date = date;
        this.opponent = opponent;
        this.winner = winner;
        this.winnerScore = winnerScore;
        this.myScore = myScore;
        this.experience = experience;
        this.myPoints = 0;
        this.myCardsWon = 0;
    }

    /**
     * Costruttore completo utilizzato dal GameController.
     * Include statistiche dettagliate della partita.
     * L'esperienza verrà calcolata e impostata successivamente.
     * 
     * @param date data della partita in formato ISO
     * @param opponent nome dell'avversario o avversari
     * @param winner nome del vincitore
     * @param winnerScore punteggio scalato del vincitore
     * @param myScore punteggio scalato dell'utente
     * @param myPoints punti raw segnati dall'utente
     * @param myCardsWon numero di carte vinte dall'utente
     */
    public GamesRecord(String date, String opponent, String winner, String winnerScore, String myScore, int myPoints, int myCardsWon) {
        this.date = date;
        this.opponent = opponent;
        this.winner = winner;
        this.winnerScore = winnerScore;
        this.myScore = myScore;
        this.myPoints = myPoints;
        this.myCardsWon = myCardsWon;
        this.experience = 0;
    }

    /**
     * Restituisce la data della partita in formato ISO.
     * 
     * @return stringa data in formato ISO 8601
     */
    public String getDate() { return date; }

    /**
     * Restituisce la data formattata in modo leggibile.
     * Converte la data ISO in formato italiano "dd/MM/yyyy".
     * In caso di errore di parsing, restituisce la data originale.
     * 
     * @return data formattata (es. "15/12/2025")
     */
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

    /**
     * Restituisce il nome dell'avversario o degli avversari.
     * 
     * @return nome avversario/i
     */
    public String getOpponent() { return opponent; }

    /**
     * Restituisce il nome del vincitore della partita.
     * 
     * @return nome del vincitore
     */
    public String getWinner() { return winner; }

    /**
     * Restituisce il punteggio del vincitore in formato scalato.
     * 
     * @return punteggio formattato (es. "3 2/3")
     */
    public String getWinnerScore() { return winnerScore; }

    /**
     * Restituisce il punteggio dell'utente loggato in formato scalato.
     * 
     * @return punteggio dell'utente (es. "2 1/3")
     */
    public String getMyScore() { return myScore; }

    /**
     * Restituisce i punti esperienza guadagnati in questa partita.
     * 
     * @return XP guadagnati
     */
    public int getExperience() { return experience; }

    /**
     * Imposta i punti esperienza per questa partita.
     * 
     * @param experience XP da assegnare
     */
    public void setExperience(int experience) { this.experience = experience; }

    /**
     * Restituisce i punti raw (non scalati) segnati dall'utente.
     * 
     * @return punti raw
     */
    public int getMyPoints() { return myPoints; }

    /**
     * Imposta i punti raw dell'utente.
     * 
     * @param myPoints punti segnati
     */
    public void setMyPoints(int myPoints) { this.myPoints = myPoints; }

    /**
     * Restituisce il numero di carte vinte dall'utente.
     * 
     * @return numero di carte vinte
     */
    public int getMyCardsWon() { return myCardsWon; }

    /**
     * Imposta il numero di carte vinte.
     * 
     * @param myCardsWon carte vinte
     */
    public void setMyCardsWon(int myCardsWon) { this.myCardsWon = myCardsWon; }

    /**
     * Restituisce una rappresentazione testuale del record.
     * Include tutti i campi per debug e logging.
     * 
     * @return stringa descrittiva del record
     */
    @Override
    public String toString() {
        return "GamesRecord{date='" + date +
                "', opponent='" + opponent +
                "', winner='" + winner +
                "', winnerScore='" + winnerScore +
                "', myScore='" + myScore +
                "', experience='" + experience +
                "', myPoints=" + myPoints +
                ", myCardsWon=" + myCardsWon + "}";
    }
}