package Model.Profile;

import java.io.*;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class StorageProfile {
    private static final Logger LOG = Logger.getLogger(StorageProfile.class.getName());
    private static final String DIR_NAME = ".jtressette";
    private static final String FILE_NAME = "profile.properties";

    // Chiavi per le proprietà
    private static final String KEY_USERNAME = "username";
    private static final String KEY_EXPERIENCE = "experience";
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
    }

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

        // Carica i game records (MINIMALISTA)
        List<GamesRecord> games = loadGamesFromProperties(props);
        profile.setRecentGames(games);

        // Leggi esperienza totale se presente; altrimenti ricava dalla somma delle partite
        String expProp = props.getProperty(KEY_EXPERIENCE);
        if (expProp != null && !expProp.isEmpty()) {
            try {
                profile.setExperience(Integer.parseInt(expProp));
            } catch (NumberFormatException e) {
                LOG.log(Level.WARNING, "Valore non valido per experience: " + expProp + ", uso somma dei record");
                profile.setExperience(sumExperienceFromGames(games));
            }
        } else {
            // fallback: se il file non conteneva il campo experience, calcoliamo la somma delle partite
            profile.setExperience(sumExperienceFromGames(games));
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

        return profile;
    }

    private int sumExperienceFromGames(List<GamesRecord> games) {
        if (games == null) return 0;
        return games.stream()
            .mapToInt(GamesRecord::getExperience)
            .sum();
    }

    /**
     * Carica i game records dalle proprietà (includiamo ora experience, myPoints e myCardsWon se presenti).
     */
    private List<GamesRecord> loadGamesFromProperties(Properties props) {
        String countStr = props.getProperty(KEY_GAMES_COUNT, "0");
        int count;
        try {
            count = Integer.parseInt(countStr);
        } catch (NumberFormatException e) {
            LOG.log(Level.WARNING, "Valore non valido per gamesCount: " + countStr + ", uso 0");
            count = 0;
        }
        
        // Usa Streams per creare i game records
        return IntStream.range(0, count)
            .mapToObj(i -> {
                String prefix = KEY_GAME_PREFIX + i + ".";
                String date = props.getProperty(prefix + "date", "");
                String opponent = props.getProperty(prefix + "opponent", "");
                String winner = props.getProperty(prefix + "winner", "");
                String winnerScore = props.getProperty(prefix + "winnerScore", "");
                String myScore = props.getProperty(prefix + "myScore", "");
                
                int experience = parseIntOrDefault(props.getProperty(prefix + "experience", "0"), 0);
                int myPoints = parseIntOrDefault(props.getProperty(prefix + "myPoints", "0"), 0);
                int myCardsWon = parseIntOrDefault(props.getProperty(prefix + "myCardsWon", "0"), 0);
                
                GamesRecord record = new GamesRecord(date, opponent, winner, winnerScore, myScore, myPoints, myCardsWon);
                record.setExperience(experience);
                return record;
            })
            .collect(Collectors.toList());
    }
    
    private int parseIntOrDefault(String value, int defaultValue) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    /**
     * Salva il profilo utente su file.
     */
    public synchronized void save(UserProfile profile) throws IOException {
        if (Files.notExists(profileDir)) {
            Files.createDirectories(profileDir);
        }

        Properties props = new Properties();
        props.setProperty(KEY_USERNAME, profile.getUsername() != null ? profile.getUsername() : "Giocatore");

        props.setProperty(KEY_EXPERIENCE, String.valueOf(profile.getExperience()));
        if (profile.getAvatarPath() != null) {
            props.setProperty(KEY_AVATAR_PATH, profile.getAvatarPath());
        }

        props.setProperty(KEY_CREATED_AT, String.valueOf(profile.getCreatedAt()));
        props.setProperty(KEY_TOTAL_GAMES, String.valueOf(profile.getTotalGames()));
        props.setProperty(KEY_TOTAL_WINS, String.valueOf(profile.getWinsNumber()));

        // Salva i game records (includiamo experience, myPoints e myCardsWon)
        List<GamesRecord> games = profile.getRecentGames();
        props.setProperty(KEY_GAMES_COUNT, String.valueOf(games.size()));
        for (int i = 0; i < games.size(); i++) {
            GamesRecord g = games.get(i);
            String prefix = KEY_GAME_PREFIX + i + ".";
            props.setProperty(prefix + "date", g.getDate() != null ? g.getDate() : "");
            props.setProperty(prefix + "opponent", g.getOpponent() != null ? g.getOpponent() : "");
            props.setProperty(prefix + "winner", g.getWinner() != null ? g.getWinner() : "");
            props.setProperty(prefix + "winnerScore", g.getWinnerScore() != null ? g.getWinnerScore() : "");
            props.setProperty(prefix + "myScore", g.getMyScore() != null ? g.getMyScore() : "");
            props.setProperty(prefix + "experience", String.valueOf(g.getExperience()));
            props.setProperty(prefix + "myPoints", String.valueOf(g.getMyPoints()));
            props.setProperty(prefix + "myCardsWon", String.valueOf(g.getMyCardsWon()));
        }

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
}