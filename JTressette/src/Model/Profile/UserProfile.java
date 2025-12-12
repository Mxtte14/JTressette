
package Model.Profile;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Profilo utente: mantiene experience come totale cumulativo.
 * Livello calcolato come: level = totalXP / XP_PER_LEVEL (0-based).
 * XP corrente nel livello = totalXP % XP_PER_LEVEL.
 */
public class UserProfile implements Serializable {
    private static final long serialVersionUID = 1L;

    private String username;
    private long createdAt;
    private int totalGames;
    private int totalWins;
    private List<GamesRecord> recentGames;
    private String avatarPath;

    // XP totali cumulativi
    private int experience = 0;

    // Costanti per sistema XP (configurabili qui)
    public static final int XP_PER_LEVEL = 500;
    public static final int XP_PER_POINT = 5;  // Aggiornato da 7 a 5 come da specifiche
    public static final int XP_PER_CARD = 2;
    public static final int XP_WIN_BONUS = 50;
    public static final int XP_LOSS_BONUS = 20;

    public UserProfile() {
        this("Giocatore");
    }

    public UserProfile(String username) {
        this.username = username != null ? username : "Giocatore";
        this.createdAt = System.currentTimeMillis();
        this.totalGames = 0;
        this.totalWins = 0;
        this.recentGames = new ArrayList<>();
        this.avatarPath = null;
        this.experience = 0;
    }

    // --- getters / setters base ---
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

    public int getTotalGames() { return totalGames; }
    public void setTotalGames(int totalGames) { this.totalGames = totalGames; }

    public int getTotalWins() { return totalWins; }
    public void setTotalWins(int totalWins) { this.totalWins = totalWins; }

    public List<GamesRecord> getRecentGames() {
        if (this.recentGames == null) this.recentGames = new ArrayList<>();
        return recentGames;
    }
    public void setRecentGames(List<GamesRecord> recentGames) { this.recentGames = recentGames; }

    public String getAvatarPath() { return avatarPath; }
    public void setAvatarPath(String avatarPath) { this.avatarPath = avatarPath; }

    // --- XP / level API (cumulativa) ---

    /**
     * Ritorna il totale XP cumulativo.
     */
    public int getExperience() {
        return experience;
    }

    /**
     * Imposta il totale XP cumulativo (usato dal loader).
     */
    public void setExperience(int experience) {
        this.experience = Math.max(0, experience);
    }

    /**
     * Aggiunge XP al totale cumulativo.
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
     * Aggiunge XP derivanti dalle statistiche di una partita
     */
    public void addGameExperience(int pointsScored, int cardsWon, boolean won) {
        int xpGained = 0;
        xpGained += pointsScored * XP_PER_POINT;
        xpGained += cardsWon * XP_PER_CARD;
        xpGained += won ? XP_WIN_BONUS : XP_LOSS_BONUS;
        addExperience(xpGained);
    }

    /**
     * Livello calcolato dal totale XP (0-based).
     * Se vuoi che i livelli partano da 1, restituisci (experience / XP_PER_LEVEL) + 1.
     */
    public int getLevel() {
        return experience / XP_PER_LEVEL;
    }

    /**
     * XP accumulati nel livello corrente (es. per 1320 => 320).
     */
    public int getExperienceInCurrentLevel() {
        return experience % XP_PER_LEVEL;
    }

    /**
     * XP necessari per completare il livello corrente (costante qui).
     */
    public int getExperienceToNextLevel() {
        return XP_PER_LEVEL;
    }

    /**
     * Percentuale di progresso verso il prossimo livello (0-100).
     */
    public double getProgressPercentage() {
        int denom = getExperienceToNextLevel();
        if (denom <= 0) return 0.0;
        return (getExperienceInCurrentLevel() * 100.0) / denom;
    }

    // --- utility per statistiche ---
    /**
     * Calcola vittorie guardando lo storico (compatibile con ProfileMenu)
     */
    public int getWinsNumber() {
        return (int) getRecentGames().stream()
                .filter(record -> Objects.equals(record.getWinner(), username))
                .count();
    }

    /**
     * Aggiunge un record di partita allo storico e aggiorna esperienza e statistiche
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
     * Ritorna lo storico delle partite (alias per compatibilità con ProfileMenu)
     */
    public List<GamesRecord> getHistory() {
        return getRecentGames();
    }

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