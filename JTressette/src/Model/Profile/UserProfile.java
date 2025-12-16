
package Model.Profile;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Rappresenta il profilo completo di un utente del gioco JTressette.
 * Mantiene tutte le informazioni relative all'utente, inclusi:
 * <ul>
 *   <li>Dati anagrafici (username, avatar, data creazione)</li>
 *   <li>Statistiche di gioco (partite totali, vittorie)</li>
 *   <li>Sistema di esperienza e livelli</li>
 *   <li>Storico delle partite recenti</li>
 * </ul>
 *
 * <p><b>Sistema di esperienza:</b> L'esperienza è cumulativa e il livello viene calcolato
 * dividendo il totale XP per XP_PER_LEVEL. Ogni 500 XP si sale di un livello.</p>
 *
 * <p><b>Guadagno XP:</b></p>
 * <ul>
 *   <li>5 XP per ogni punto segnato (XP_PER_POINT)</li>
 *   <li>2 XP per ogni carta vinta (XP_PER_CARD)</li>
 *   <li>50 XP bonus per vittoria (XP_WIN_BONUS)</li>
 *   <li>20 XP bonus partecipazione per sconfitta (XP_LOSS_BONUS)</li>
 * </ul>
 *
 * <p>La classe è Serializable per permettere il salvataggio su disco.</p>
 */
public class UserProfile implements Serializable {
    /** Versione di serializzazione per compatibilità */
    private static final long serialVersionUID = 1L;

    /** Nome utente del giocatore */
    private String username;

    /** Timestamp di creazione del profilo (milliseconds since epoch) */
    private long createdAt;

    /** Numero totale di partite giocate */
    private int totalGames;

    /** Numero totale di vittorie */
    private int totalWins;

    /** Lista delle partite recenti giocate */
    private List<GamesRecord> recentGames;

    /** Percorso al file immagine dell'avatar personalizzato */
    private String avatarPath;

    /** Esperienza totale cumulativa guadagnata */
    private int experience = 0;

    /** XP necessari per salire di un livello */
    public static final int XP_PER_LEVEL = 500;

    /** XP guadagnati per ogni punto segnato */
    public static final int XP_PER_POINT = 5;

    /** XP guadagnati per ogni carta vinta */
    public static final int XP_PER_CARD = 2;

    /** Bonus XP per vittoria della partita */
    public static final int XP_WIN_BONUS = 50;

    /** Bonus XP per partecipazione (partita persa) */
    public static final int XP_LOSS_BONUS = 20;

    /**
     * Costruttore di default.
     * Crea un profilo con username "Giocatore".
     */
    public UserProfile() {
        this("Giocatore");
    }

    /**
     * Costruttore con username personalizzato.
     * Inizializza il profilo con valori di default e timestamp corrente.
     *
     * @param username nome utente (se null, usa "Giocatore")
     */
    public UserProfile(String username) {
        this.username = username != null ? username : "Giocatore";
        this.createdAt = System.currentTimeMillis();
        this.totalGames = 0;
        this.totalWins = 0;
        this.recentGames = new ArrayList<>();
        this.avatarPath = null;
        this.experience = 0;
    }

    /**
     * Restituisce il nome utente.
     *
     * @return il nome utente
     */
    public String getUsername() { return username; }

    /**
     * Imposta il nome utente.
     *
     * @param username il nuovo nome utente
     */
    public void setUsername(String username) { this.username = username; }

    /**
     * Restituisce il timestamp di creazione del profilo.
     *
     * @return milliseconds since epoch della creazione
     */
    public long getCreatedAt() { return createdAt; }

    /**
     * Imposta il timestamp di creazione.
     *
     * @param createdAt timestamp in millisecondi
     */
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

    /**
     * Restituisce il numero totale di partite giocate.
     *
     * @return numero di partite totali
     */
    public int getTotalGames() { return totalGames; }

    /**
     * Imposta il numero totale di partite.
     *
     * @param totalGames numero di partite
     */
    public void setTotalGames(int totalGames) { this.totalGames = totalGames; }

    /**
     * Restituisce il numero totale di vittorie.
     *
     * @return numero di vittorie
     */
    public int getTotalWins() { return totalWins; }

    /**
     * Imposta il numero totale di vittorie.
     *
     * @param totalWins numero di vittorie
     */
    public void setTotalWins(int totalWins) { this.totalWins = totalWins; }

    /**
     * Restituisce la lista delle partite recenti.
     * Se la lista non esiste, viene inizializzata automaticamente.
     *
     * @return lista dei record delle partite
     */
    public List<GamesRecord> getRecentGames() {
        if (this.recentGames == null) this.recentGames = new ArrayList<>();
        return recentGames;
    }

    /**
     * Imposta la lista delle partite recenti.
     *
     * @param recentGames lista dei record di partite
     */
    public void setRecentGames(List<GamesRecord> recentGames) { this.recentGames = recentGames; }

    /**
     * Restituisce il percorso del file avatar personalizzato.
     *
     * @return percorso del file avatar, o null se non impostato
     */
    public String getAvatarPath() { return avatarPath; }

    /**
     * Imposta il percorso del file avatar.
     *
     * @param avatarPath percorso al file immagine dell'avatar
     */
    public void setAvatarPath(String avatarPath) { this.avatarPath = avatarPath; }

    /**
     * Restituisce l'esperienza totale cumulativa.
     *
     * @return punti esperienza totali
     */
    public int getExperience() {
        return experience;
    }

    /**
     * Imposta l'esperienza totale cumulativa.
     * Utilizzato dal sistema di caricamento del profilo.
     * Il valore viene limitato a valori non negativi.
     *
     * @param experience punti esperienza da impostare
     */
    public void setExperience(int experience) {
        this.experience = Math.max(0, experience);
    }

    /**
     * Aggiunge punti esperienza al totale cumulativo.
     * Gestisce l'overflow proteggendo dal superamento di Integer.MAX_VALUE.
     *
     * @param xp punti esperienza da aggiungere (valori negativi o zero vengono ignorati)
     */
    public void addExperience(int xp) {
        if (xp <= 0) return;
        long sum = (long) this.experience + xp;
        if (sum > Integer.MAX_VALUE) {
            this.experience = Integer.MAX_VALUE;
        } else {
            this.experience = (int) sum;
        }
    }

    /**
     * Aggiunge esperienza derivata dalle statistiche di una partita.
     * Calcola automaticamente i punti XP basandosi su:
     * - Punti segnati × XP_PER_POINT
     * - Carte vinte × XP_PER_CARD
     * - Bonus vittoria/partecipazione
     *
     * @param pointsScored punti segnati nella partita
     * @param cardsWon numero di carte vinte
     * @param won true se la partita è stata vinta, false altrimenti
     */
    public void addGameExperience(int pointsScored, int cardsWon, boolean won) {
        int xpGained = 0;
        xpGained += pointsScored * XP_PER_POINT;
        xpGained += cardsWon * XP_PER_CARD;
        xpGained += won ? XP_WIN_BONUS : XP_LOSS_BONUS;
        addExperience(xpGained);
    }

    /**
     * Calcola il livello corrente basato sull'esperienza totale.
     * Il livello è calcolato come: totalXP / XP_PER_LEVEL (0-based).
     * Per livelli 1-based, aggiungere 1 al valore restituito.
     *
     * @return livello corrente del giocatore
     */
    public int getLevel() {
        return experience / XP_PER_LEVEL;
    }

    /**
     * Restituisce i punti XP accumulati nel livello corrente.
     * Rappresenta il progresso verso il prossimo livello.
     * Es: con 1320 XP totali, restituisce 320 (1320 % 500).
     *
     * @return XP nel livello corrente
     */
    public int getExperienceInCurrentLevel() {
        return experience % XP_PER_LEVEL;
    }

    /**
     * Restituisce i punti XP necessari per completare il livello corrente.
     * Valore costante pari a XP_PER_LEVEL (500).
     *
     * @return XP richiesti per il prossimo livello
     */
    public int getExperienceToNextLevel() {
        return XP_PER_LEVEL;
    }

    /**
     * Calcola la percentuale di progresso verso il prossimo livello.
     *
     * @return percentuale di completamento (0.0 - 100.0)
     */
    public double getProgressPercentage() {
        int denom = getExperienceToNextLevel();
        if (denom <= 0) return 0.0;
        return (getExperienceInCurrentLevel() * 100.0) / denom;
    }

    /**
     * Calcola il numero di vittorie analizzando lo storico delle partite.
     * Conta quante partite hanno questo utente come vincitore.
     *
     * @return numero di vittorie trovate nello storico
     */
    public int getWinsNumber() {
        return (int) getRecentGames().stream()
                .filter(record -> Objects.equals(record.getWinner(), username))
                .count();
    }

    /**
     * Aggiunge un record di partita allo storico e aggiorna le statistiche.
     * Esegue le seguenti operazioni:
     * <ul>
     *   <li>Aggiunge il record alla lista delle partite</li>
     *   <li>Incrementa il contatore delle partite totali</li>
     *   <li>Incrementa le vittorie se l'utente ha vinto</li>
     *   <li>Calcola e aggiunge l'esperienza guadagnata</li>
     *   <li>Salva l'esperienza nel record</li>
     * </ul>
     *
     * @param record il record della partita da aggiungere (null viene ignorato)
     */
    public void addGameRecord(GamesRecord record) {
        if (record == null) return;

        // Aggiungi il record alla lista
        getRecentGames().add(record);

        // Aggiorna totale partite
        this.totalGames++;

        // Verifica se l'utente ha vinto
        boolean won = Objects.equals(record.getWinner(), username);

        // Aggiorna vittorie se l'utente ha vinto
        if (won) {
            this.totalWins++;
        }

        // Calcola e aggiungi esperienza basata sui dati della partita
        int xpGained = 0;
        xpGained += record.getMyPoints() * XP_PER_POINT;
        xpGained += record.getMyCardsWon() * XP_PER_CARD;
        xpGained += won ? XP_WIN_BONUS : XP_LOSS_BONUS;

        // Salva l'esperienza nel record
        record.setExperience(xpGained);

        // Aggiungi al totale
        addExperience(xpGained);
    }

    /**
     * Restituisce lo storico delle partite.
     * Metodo alias per compatibilità con componenti legacy che usano getHistory().
     *
     * @return lista dei record delle partite
     */
    public List<GamesRecord> getHistory() {
        return getRecentGames();
    }

    /**
     * Restituisce una rappresentazione testuale del profilo.
     * Include tutti i campi principali per debug e logging.
     *
     * @return stringa descrittiva del profilo
     */
    @Override
    public String toString() {
        return "UserProfile{" +
                "username='" + username + '\'' +
                ", createdAt=" + createdAt +
                ", totalGames=" + totalGames +
                ", totalWins=" + totalWins +
                ", recentGames=" + recentGames +
                ", avatarPath='" + avatarPath + '\'' +
                ", experience=" + experience +
                '}';
    }
}