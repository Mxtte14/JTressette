package profile;

import java.util.List;

public class UserProfile {
    private String name;
    private String avatarPath; // percorso file dell'avatar
    private final List<GamesRecord> history;

    public UserProfile(String name, String avatarPath, List<GamesRecord> history) {
        this.name = name;
        this.avatarPath = avatarPath;
        this.history = history;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getAvatarPath() { return avatarPath; }
    public void setAvatarPath(String avatarPath) { this.avatarPath = avatarPath; }

    public List<GamesRecord> getHistory() { return history; }

    // add helper to append partita
    public void addMatch(GamesRecord m) { history.add(m); }
}