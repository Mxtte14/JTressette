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

    public UserProfile() {
        this.username = "Giocatore";
        this.createdAt = Instant.now().toEpochMilli();
        this.totalGames = 0;
        this.totalWins = 0;
        this.recentGames = new ArrayList<>();
        this.avatarPath = null;
    }

    public UserProfile(String username) {
        this.username = username;
        this.createdAt = Instant.now().toEpochMilli();
        this.totalGames = 0;
        this.totalWins = 0;
        this.recentGames = new ArrayList<>();
        this.avatarPath = null;
    }

    /**
     * Aggiunge un record alle recentGames (in testa). Mantiene al massimo 50 record.
     * Aggiorna totalGames e totalWins.
     */
    public void addGameRecord(GamesRecord summary) {
        if (summary == null) return;
        if (this.recentGames == null) this.recentGames = new ArrayList<>();
        this.recentGames.add(0, summary); // ultima prima
        if (this.recentGames.size() > 50) this.recentGames.remove(this.recentGames.size() - 1);

        this.totalGames++;
        if (summary.getWinner() != null && summary.getWinner().equals(username)) {
            this.totalWins++;
        }
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

    @Override
    public String toString() {
        return "UserProfile{" +
                "username='" + username + '\'' +
                ", createdAt=" + createdAt +
                ", totalGames=" + totalGames +
                ", totalWins=" + totalWins +
                ", recentGames=" + recentGames +
                ", avatarPath='" + avatarPath + '\'' +
                '}';
    }
}