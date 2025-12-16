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

/**
 * Gestore della persistenza del profilo utente su file system.
 * Utilizza il formato Properties di Java per salvare e caricare i dati del profilo.
 *
 * <p>Il profilo viene salvato nella directory home dell'utente in una cartella nascosta
 * ".jtressette" con il nome "profile.properties".</p>
 *
 * <p>Funzionalità principali:</p>
 * <ul>
 *   <li>Salvataggio atomico per prevenire corruzione dei dati</li>
 *   <li>Caricamento con gestione retrocompatibilità</li>
 *   <li>Creazione automatica directory se non esiste</li>
 *   <li>Gestione errori con fallback su profilo di default</li>
 *   <li>Serializzazione di tutti i game records con statistiche complete</li>
 * </ul>
 */
public class StorageProfile {
    /** Logger per registrare eventi ed errori */
    private static final Logger LOG = Logger.getLogger(StorageProfile.class.getName());

    /** Nome della directory di salvataggio (nascosta) */
    private static final String DIR_NAME = ".jtressette";

    /** Nome del file del profilo */
    private static final String FILE_NAME = "profile.properties";

    // Chiavi per le proprietà nel file
    private static final String KEY_USERNAME = "username";
    private static final String KEY_EXPERIENCE = "experience";
    private static final String KEY_AVATAR_PATH = "avatarPath";
    private static final String KEY_CREATED_AT = "createdAt";
    private static final String KEY_TOTAL_GAMES = "totalGames";
    private static final String KEY_TOTAL_WINS = "totalWins";
    private static final String KEY_GAMES_COUNT = "gamesCount";
    private static final String KEY_GAME_PREFIX = "game.";

    /** Percorso alla directory del profilo */
    private final Path profileDir;

    /** Percorso al file del profilo */
    private final Path profileFile;

    /**
     * Costruttore dello StorageProfile.
     * Determina automaticamente la directory home dell'utente e
     * imposta i percorsi per la directory e il file del profilo.
     */
    public StorageProfile() {
        String userHome = System.getProperty("user.home");
        this.profileDir = Paths.get(userHome, DIR_NAME);
        this.profileFile = profileDir.resolve(FILE_NAME);
    }

    /**
     * Carica il profilo dal file o crea un profilo di default se non esiste.
     * Se il file non esiste, crea un nuovo profilo con username "Giocatore",
     * lo salva su disco e lo restituisce.
     * In caso di errore di caricamento, restituisce un profilo di default senza salvarlo.
     *
     * @return il profilo utente caricato o un profilo di default
     */
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
     * Gestisce la retrocompatibilità con vecchie versioni del formato.
     * Se alcuni campi mancano, usa valori di default.
     *
     * @return il profilo caricato dal file
     * @throws IOException se si verifica un errore di lettura
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

    /**
     * Calcola la somma dell'esperienza di tutte le partite.
     * Utilizzato come fallback quando il campo esperienza totale non è presente nel file.
     *
     * @param games lista dei record di partite
     * @return somma totale dell'esperienza
     */
    private int sumExperienceFromGames(List<GamesRecord> games) {
        if (games == null) return 0;
        return games.stream()
                .mapToInt(GamesRecord::getExperience)
                .sum();
    }

    /**
     * Carica i record delle partite dalle proprietà del file.
     * Legge tutti i campi inclusi esperienza, punti e carte vinte se presenti.
     * Utilizza Stream API per costruire efficientemente la lista.
     *
     * @param props oggetto Properties contenente i dati
     * @return lista dei record di partite caricati
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

    /**
     * Effettua il parsing sicuro di una stringa in intero.
     * In caso di errore restituisce il valore di default.
     *
     * @param value stringa da convertire
     * @param defaultValue valore da restituire in caso di errore
     * @return intero parsato o valore di default
     */
    private int parseIntOrDefault(String value, int defaultValue) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    /**
     * Salva il profilo utente su file in modo atomico e thread-safe.
     *
     * <p>Il salvataggio avviene in due fasi per garantire l'atomicità:</p>
     * <ol>
     *   <li>Scrive i dati in un file temporaneo (.tmp)</li>
     *   <li>Sposta atomicamente il file temporaneo sopra quello originale</li>
     * </ol>
     *
     * <p>Questo approccio previene la corruzione dei dati in caso di interruzione
     * durante il salvataggio (crash, spegnimento, ecc.).</p>
     *
     * <p>Il metodo è sincronizzato per garantire che solo un thread alla volta
     * possa salvare il profilo.</p>
     *
     * @param profile il profilo da salvare
     * @throws IOException se si verifica un errore di scrittura
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