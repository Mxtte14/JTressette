package profile;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Esempio minimale di profilo utente. Estendi con i campi che ti servono.
 */
public class UserProfile {
    public String username;
    public Instant createdAt;
    public int totalGames;
    public int totalWins;
    public List<String> recentGames; // potresti usare oggetti più ricchi

    public UserProfile() { // per Gson
    }

    public UserProfile(String username) {
        this.username = username;
        this.createdAt = Instant.now();
        this.totalGames = 0;
        this.totalWins = 0;
        this.recentGames = new ArrayList<>();
    }

    public void addGameRecord(String summary) {
        this.recentGames.add(0, summary); // ultima prima
        if (this.recentGames.size() > 50) this.recentGames.remove(this.recentGames.size() - 1);
        this.totalGames++;
    }

    public String getUsername() {
        return username;
    }

    public String getAvatarPath() {

    }
}