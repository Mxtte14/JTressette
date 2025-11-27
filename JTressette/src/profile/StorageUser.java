package profile;

import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Salvataggio/caricamento profilo utente usando XMLEncoder/XMLDecoder (built-in JDK).
 * Percorso di base: {user.home}/.jtressette/users/{username}/profile.xml
 *
 * Nota: UserProfile e GamesRecord sono JavaBean compatibili (costruttore no-arg + getter/setter)
 * per essere completamente compatibile con XMLEncoder/XMLDecoder.
 */
public abstract class StorageUser {

    private static final Logger LOG = Logger.getLogger(StorageUser.class.getName());
    private static final String BASE_DIR_NAME = ".jtressette";

    public static Path getAppBaseDir() {
        String home = System.getProperty("user.home");
        return Path.of(home, BASE_DIR_NAME);
    }

    public static Path getUserDir(String username) {
        return getAppBaseDir().resolve("users").resolve(username);
    }

    public static Path getProfilePath(String username) {
        return getUserDir(username).resolve("profile.xml");
    }

    public static UserProfile loadProfile(String username) {
        Path p = getProfilePath(username);
        if (Files.notExists(p)) return null;
        try (InputStream fis = new BufferedInputStream(new FileInputStream(p.toFile()));
             XMLDecoder decoder = new XMLDecoder(fis)) {
            Object o = decoder.readObject();
            if (o instanceof UserProfile) return (UserProfile) o;
            LOG.log(Level.WARNING, "profile.xml non contiene un UserProfile valido");
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Errore durante il caricamento del profilo XML", e);
        }
        return null;
    }

    public static UserProfile createAndSaveNewProfile(String username) {
        UserProfile profile = new UserProfile(username);
        saveProfile(profile);
        return profile;
    }

    public static void saveProfile(UserProfile profile) {
        try {
            Path dir = getUserDir(profile.getUsername());
            Files.createDirectories(dir);
            Path p = getProfilePath(profile.getUsername());
            try (OutputStream fos = new BufferedOutputStream(new FileOutputStream(p.toFile()));
                 XMLEncoder encoder = new XMLEncoder(fos)) {
                encoder.writeObject(profile);
                encoder.flush();
            }
        } catch (IOException e) {
            LOG.log(Level.SEVERE, "Errore nel salvataggio del profilo XML", e);
        }
    }

    public abstract UserProfile loadOrCreateDefault();

    public abstract void save(UserProfile profile) throws IOException;
}