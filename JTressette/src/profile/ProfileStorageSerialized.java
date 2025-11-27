package profile;

import java.io.*;
import java.nio.file.*;

/**
 * Implementazione ProfileStorage che serializza il modello su disco usando
 * ObjectOutputStream / ObjectInputStream. Non richiede librerie esterne.
 */
public class ProfileStorageSerialized extends StorageUser {
    private static final String DIR_NAME = ".jtressette";
    private static final String FILE_NAME = "profile.ser";

    private final Path profileDir;
    private final Path profileFile;

    public ProfileStorageSerialized() {
        String userHome = System.getProperty("user.home");
        this.profileDir = Paths.get(userHome, DIR_NAME);
        this.profileFile = profileDir.resolve(FILE_NAME);
    }

    @Override
    public UserProfile loadOrCreateDefault() {
        try {
            if (Files.notExists(profileFile)) {
                if (Files.notExists(profileDir)) Files.createDirectories(profileDir);
                UserProfile defaultProfile = new UserProfile("Giocatore");
                save(defaultProfile);
                return defaultProfile;
            } else {
                try (InputStream is = Files.newInputStream(profileFile);
                     ObjectInputStream ois = new ObjectInputStream(is)) {
                    Object o = ois.readObject();
                    if (o instanceof UserProfile) {
                        return (UserProfile) o;
                    } else {
                        // file corrotto / formato non previsto: ricrea default
                        UserProfile defaultProfile = new UserProfile("Giocatore");
                        save(defaultProfile);
                        return defaultProfile;
                    }
                } catch (ClassNotFoundException | IOException ex) {
                    // in caso di errore, ritorna default (e prova a sovrascrivere)
                    UserProfile defaultProfile = new UserProfile("Giocatore");
                    try { save(defaultProfile); } catch (IOException ignored) {}
                    return defaultProfile;
                }
            }
        } catch (IOException e) {
            // se non riesce a creare la dir, ritorna profilo default in memoria
            return new UserProfile("Giocatore");
        }
    }

    @Override
    public synchronized void save(UserProfile profile) throws IOException {
        if (Files.notExists(profileDir)) Files.createDirectories(profileDir);
        Path tmp = profileDir.resolve(FILE_NAME + ".tmp");

        // scrivo su file temporaneo e poi move atomico
        try (OutputStream os = Files.newOutputStream(tmp, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
             ObjectOutputStream oos = new ObjectOutputStream(os)) {
            oos.writeObject(profile);
            oos.flush();
        }

        try {
            Files.move(tmp, profileFile, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (AtomicMoveNotSupportedException ex) {
            Files.move(tmp, profileFile, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    public Path getProfileFilePath() { return profileFile; }
}