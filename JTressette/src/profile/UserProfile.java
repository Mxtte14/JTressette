package profile;

import java.io.Serializable;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Profilo utente adattato per:
 * - essere Serializable (per ProfileStorageSerialized)
 * - essere JavaBean compatibile (costruttore no-arg + getter/setter) per XMLEncoder/XMLDecoder
 * - usare List<GamesRecord> per lo storico partite (coerente con ProfileMenu)
 *
 * Nota: createdAt è memorizzato come epoch millis per compatibilità con XMLEncoder.
 */
public class UserProfile implements Serializable {
    private static final long serialVersionUID = 1L;

    private String username;
    private long createdAt; // epoch millis
    private int totalGames;
    private int totalWins;
    private List<GamesRecord> recentGames;
    private String avatarPath;

    // costruttore no-arg per XMLEncoder / deserializzazione
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
     * Incrementa totalGames. Se il campo result contiene "win" o "vitt" (case-insensitive)
     * incrementa totalWins.
     */
    public void addGameRecord(GamesRecord summary) {
        if (summary == null) return;
        if (this.recentGames == null) this.recentGames = new ArrayList<>();
        this.recentGames.add(0, summary); // ultima prima
        if (this.recentGames.size() > 50) this.recentGames.remove(this.recentGames.size() - 1);
        this.totalGames++;
        String r = summary.getResult();
        if (r != null) {
            String lr = r.toLowerCase();
            if (lr.contains("win") || lr.contains("vitt")) this.totalWins++;
        }
    }

    // Compatibilità con ProfileMenu (usa getHistory())
    public List<GamesRecord> getHistory() {
        if (this.recentGames == null) this.recentGames = new ArrayList<>();
        return recentGames;
    }

    // --- JavaBean getters/setters ---

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Restituisce createdAt come epoch millis.
     * XMLEncoder funziona meglio con tipi primari come long.
     */
    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    /**
     * Helper per usare Instant se necessario.
     */
    public Instant getCreatedAtInstant() {
        return Instant.ofEpochMilli(createdAt);
    }

    public void setCreatedAtInstant(Instant instant) {
        if (instant == null) this.createdAt = Instant.now().toEpochMilli();
        else this.createdAt = instant.toEpochMilli();
    }

    public int getTotalGames() {
        return totalGames;
    }

    public void setTotalGames(int totalGames) {
        this.totalGames = totalGames;
    }

    public int getTotalWins() {
        return totalWins;
    }

    public void setTotalWins(int totalWins) {
        this.totalWins = totalWins;
    }

    // Getter/Setter per XMLEncoder/XMLDecoder
    public List<GamesRecord> getRecentGames() {
        if (this.recentGames == null) this.recentGames = new ArrayList<>();
        return recentGames;
    }

    public void setRecentGames(List<GamesRecord> recentGames) {
        this.recentGames = recentGames;
    }

    public String getAvatarPath() {
        return avatarPath;
    }

    public void setAvatarPath(String avatarPath) {
        this.avatarPath = avatarPath;
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
                '}';
    }

}