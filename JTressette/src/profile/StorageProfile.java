package profile;



import java.io.*;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
/**
 *
 * @author andre
 *
 */
public class StorageProfile {
    private static final Logger LOG = Logger.getLogger(StorageProfile.class.getName());
    private static final String DIR_NAME = ".jtressette";
    private static final String FILE_NAME = "profile.properties";

    // Chiavi per le proprietà
    private static final String KEY_USERNAME = "username";
    private static final String KEY_AVATAR_PATH = "avatarPath";
    private static final String KEY_CREATED_AT = "createdAt";
    private static final String KEY_TOTAL_GAMES = "totalGames";
    private static final String KEY_TOTAL_WINS = "totalWins";
    private static final String KEY_GAMES_COUNT = "gamesCount";
    private static final String KEY_GAME_PREFIX = "game.";

    private final Path profileDir;
    private final Path profileFile;

    public StorageProfile() {
        String userHome = System.getProperty("user.home");
        this.profileDir = Paths.get(userHome, DIR_NAME);
        this.profileFile = profileDir.resolve(FILE_NAME);
    }// Map of game data


    public UserProfile loadOrCreateDefault() {
        try {
            if (Files.notExists(profileFile)) {
                if (Files.notExists(profileDir)) {
                    Files.createDirectories(profileDir);
                }
                UserProfile defaultProfile = new UserProfile("Giocatore");
                save(defaultProfile);
                return defaultProfile;
            } else {
                return loadFromFile();
            }
        } catch (IOException e) {
            LOG.log(Level.SEVERE, "Errore durante il caricamento del profilo", e);
            return new UserProfile("Giocatore");
        }
    }

    /**
     * Carica il profilo dal file properties.
     */
    private UserProfile loadFromFile() throws IOException {
        Properties props = new Properties();
        try (InputStream is = Files.newInputStream(profileFile)) {
            props.load(is);
        }

        UserProfile profile = new UserProfile();
        profile.setUsername(props.getProperty(KEY_USERNAME, "Giocatore"));
        profile.setAvatarPath(props.getProperty(KEY_AVATAR_PATH, null));

        String createdAtStr = props.getProperty(KEY_CREATED_AT);
        if (createdAtStr != null && !createdAtStr.isEmpty()) {
            try {
                profile.setCreatedAt(Long.parseLong(createdAtStr));
            } catch (NumberFormatException e) {
                LOG.log(Level.WARNING, "Valore non valido per createdAt: " + createdAtStr + ", uso valore default");
            }
        }

        String totalGamesStr = props.getProperty(KEY_TOTAL_GAMES, "0");
        try {
            profile.setTotalGames(Integer.parseInt(totalGamesStr));
        } catch (NumberFormatException e) {
            LOG.log(Level.WARNING, "Valore non valido per totalGames: " + totalGamesStr + ", uso 0");
            profile.setTotalGames(0);
        }

        String totalWinsStr = props.getProperty(KEY_TOTAL_WINS, "0");
        try {
            profile.setTotalWins(Integer.parseInt(totalWinsStr));
        } catch (NumberFormatException e) {
            LOG.log(Level.WARNING, "Valore non valido per totalWins: " + totalWinsStr + ", uso 0");
            profile.setTotalWins(0);
        }

        // Carica i game records
        List<GamesRecord> games = loadGamesFromProperties(props);
        profile.setRecentGames(games);

        return profile;
    }

    /**
     * Carica i game records dalle proprietà.
     * Formato: game.0.date, game.0.opponent, game.0.result, ecc.
     */
    private List<GamesRecord> loadGamesFromProperties(Properties props) {
        List<GamesRecord> games = new ArrayList<>();
        String countStr = props.getProperty(KEY_GAMES_COUNT, "0");
        int count;
        try {
            count = Integer.parseInt(countStr);
        } catch (NumberFormatException e) {
            LOG.log(Level.WARNING, "Valore non valido per gamesCount: " + countStr + ", uso 0");
            count = 0;
        }
        for (int i = 0; i < count; i++) {
            String prefix = KEY_GAME_PREFIX + i + ".";
            String date = props.getProperty(prefix + "date", "");
            String opponent = props.getProperty(prefix + "opponent", "");
            String result = props.getProperty(prefix + "result", "");
            String scaledScore = props.getProperty(prefix + "scaledScore", "");
            games.add(new GamesRecord(date, opponent, result, scaledScore ));
        }
        return games;
    }

    /**
     * Salva il profilo nel file properties.
     */

    /**
     * Salva il profilo utente su file.
     * @param profile Il profilo da salvare
     * @throws IOException Se si verifica un errore durante il salvataggio
     */
    public synchronized void save(UserProfile profile) throws IOException {
        if (Files.notExists(profileDir)) {
            Files.createDirectories(profileDir);
        }

        Properties props = new Properties();
        props.setProperty(KEY_USERNAME, profile.getUsername() != null ? profile.getUsername() : "Giocatore");

        if (profile.getAvatarPath() != null) {
            props.setProperty(KEY_AVATAR_PATH, profile.getAvatarPath());
        }

        props.setProperty(KEY_CREATED_AT, String.valueOf(profile.getCreatedAt()));
        props.setProperty(KEY_TOTAL_GAMES, String.valueOf(profile.getTotalGames()));
        props.setProperty(KEY_TOTAL_WINS, String.valueOf(profile.getWinsNumber()));

        // Salva i game records
        List<GamesRecord> games = profile.getRecentGames();
        props.setProperty(KEY_GAMES_COUNT, String.valueOf(games.size()));
        for (int i = 0; i < games.size(); i++) {
            GamesRecord g = games.get(i);
            String prefix = KEY_GAME_PREFIX + i + ".";
            props.setProperty(prefix + "date", g.getFormattedDate() != null ? g.getDate() : "");
            props.setProperty(prefix + "opponent", g.getOpponent() != null ? g.getOpponent() : "");
            props.setProperty(prefix + "result", g.getResult() != null ? g.getResult() : "");
            props.setProperty(prefix + "scaledScore", g.getScaledScore() != null ? g.getScaledScore() : "");

        }

        // Scrivi su file temporaneo e poi rinomina per evitare corruzione
        Path tmp = profileDir.resolve(FILE_NAME + ".tmp");
        try (OutputStream os = Files.newOutputStream(tmp, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
            props.store(os, "JTressette User Profile");
        }

        try {
            Files.move(tmp, profileFile, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (AtomicMoveNotSupportedException ex) {
            LOG.log(Level.FINE, "Atomic move non supportato, uso move standard");
            Files.move(tmp, profileFile, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    /**
     * Restituisce il percorso del file di profilo.
     * @return Path del file di profilo
     */
    public Path getProfileFilePath() {
        return profileFile;
    }
}
