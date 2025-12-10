package Model.Profile;

import java.io.Serializable;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Profilo utente minimale. Tiene solo lo storico partite e dati essenziali.
 */
public class UserProfile implements Serializable {
    private static final long serialVersionUID = 1L;

    private String username;
    private long createdAt;
    private int totalGames;
    private int totalWins;
    private List<GamesRecord> recentGames;
    private String avatarPath;

    // Level system fields
    private int level;
    private int experience;

    // Constants for level system
    private static final int XP_PER_LEVEL = 500;
    private static final int XP_PER_POINT = 7; // 5-10 per point
    private static final int XP_PER_CARD = 2;
    private static final int XP_WIN_BONUS = 50;
    private static final int XP_LOSS_BONUS = 20;

    public UserProfile() {
        this.username = "Giocatore";
        this.createdAt = Instant.now().toEpochMilli();
        this.totalGames = 0;
        this.totalWins = 0;
        this.recentGames = new ArrayList<>();
        this.avatarPath = null;
        this.level = 1;
        this.experience = 0;
    }

    public UserProfile(String username) {
        this.username = username;
        this.createdAt = Instant.now().toEpochMilli();
        this.totalGames = 0;
        this.totalWins = 0;
        this.recentGames = new ArrayList<>();
        this.avatarPath = null;
        this.level = 1;
        this.experience = 0;
    }

    /**
     * Aggiunge un record alle recentGames (in testa). Mantiene al massimo 50 record.
     * Aggiorna totalGames e totalWins.
     * Calcola e aggiunge esperienza basata sulle statistiche della partita.
     */
    public void addGameRecord(GamesRecord summary) {
        if (summary == null) return;
        if (this.recentGames == null) this.recentGames = new ArrayList<>();
        this.recentGames.add(0, summary); // ultima prima
        if (this.recentGames.size() > 50) this.recentGames.remove(this.recentGames.size() - 1);

        this.totalGames++;
        boolean won = summary.getWinner() != null && summary.getWinner().equals(username);
        if (won) {
            this.totalWins++;
        }

        // Add experience based on game performance
        addGameExperience(summary.getMyPoints(), summary.getMyCardsWon(), won);
    }

    public List<GamesRecord> getHistory() {
        if (this.recentGames == null) this.recentGames = new ArrayList<>();
        return recentGames;
    }

    // --- JavaBean getters/setters ---

    public String getUsername() { return username; }

    public void setUsername(String username) { this.username = username; }

    public long getCreatedAt() { return createdAt; }

    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

    public Instant getCreatedAtInstant() { return Instant.ofEpochMilli(createdAt); }

    public void setCreatedAtInstant(Instant instant) {
        this.createdAt = (instant != null) ? instant.toEpochMilli() : Instant.now().toEpochMilli();
    }

    public int getTotalGames() { return totalGames; }

    public void setTotalGames(int totalGames) { this.totalGames = totalGames; }

    /**
     * Restituisce il conteggio delle vittorie in base allo storico.
     */
    public int getWinsNumber() {
        int wins = 0;
        for (GamesRecord record : getRecentGames()) {
            if (record.getWinner() != null && record.getWinner().equals(username)) {
                wins++;
            }
        }
        return wins;
    }

    public void setTotalWins(int totalWins) { this.totalWins = totalWins; }

    public List<GamesRecord> getRecentGames() {
        if (this.recentGames == null) this.recentGames = new ArrayList<>();
        return recentGames;
    }

    public void setRecentGames(List<GamesRecord> recentGames) { this.recentGames = recentGames; }

    public String getAvatarPath() { return avatarPath; }

    public void setAvatarPath(String avatarPath) { this.avatarPath = avatarPath; }

    // Level system getters/setters
    public int getLevel() { return level; }

    public void setLevel(int level) { this.level = level; }

    public int getExperience() { return experience; }

    public void setExperience(int experience) { this.experience = experience; }

    /**
     * Adds experience points and automatically levels up if threshold is reached.
     * @param xp Experience points to add
     */
    public void addExperience(int xp) {
        if (xp < 0) return; // Ignore negative XP
        this.experience += xp;

        // Check for level up (with safety limit to prevent infinite loops)
        int maxLevelUps = 100; // Safety limit
        while (this.experience >= getExperienceToNextLevel() && maxLevelUps > 0) {
            int xpNeeded = getExperienceToNextLevel();
            if (xpNeeded <= 0) break; // Safety check
            this.experience -= xpNeeded;
            this.level++;
            maxLevelUps--;
        }
    }

    /**
     * Calculates experience needed for the next level.
     */
    public int getExperienceToNextLevel() {
        return XP_PER_LEVEL;
    }

    /**
     * Returns progress percentage towards next level (0-100).
     */
    public double getProgressPercentage() {
        return (experience * 100.0) / getExperienceToNextLevel();
    }

    /**
     * Adds experience based on game statistics.
     * @param pointsScored Points scored in the game
     * @param cardsWon Number of cards won
     * @param won Whether the game was won
     */
    public void addGameExperience(int pointsScored, int cardsWon, boolean won) {
        int xpGained = 0;

        // XP from points scored
        xpGained += pointsScored * XP_PER_POINT;

        // XP from cards won
        xpGained += cardsWon * XP_PER_CARD;

        // Win/loss bonus
        xpGained += won ? XP_WIN_BONUS : XP_LOSS_BONUS;

        addExperience(xpGained);
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
                ", level=" + level +
                ", experience=" + experience +
                '}';
    }
}