package profile;

import javax.json.*;
import javax.json.stream.JsonParsingException;
import java.io.*;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementazione semplice di ProfileStorage usando JSON‑P (javax.json).
 * Non richiede Gson/Jackson, ma richiede la libreria javax.json se non presente.
 */
public class ProfileStorageJsonP implements ProfileStorage {
    private static final String DIR_NAME = ".jtressette";
    private static final String FILE_NAME = "profile.json";

    private final Path profileDir;
    private final Path profileFile;

    public ProfileStorageJsonP() {
        String userHome = System.getProperty("user.home");
        this.profileDir = Paths.get(userHome, DIR_NAME);
        this.profileFile = profileDir.resolve(FILE_NAME);
    }

    @Override
    public UserProfile loadOrCreateDefault() {
        try {
            if (Files.notExists(profileFile)) {
                if (Files.notExists(profileDir)) Files.createDirectories(profileDir);
                UserProfile defaultProfile = new UserProfile("Giocatore", null, new ArrayList<>());
                save(defaultProfile);
                return defaultProfile;
            } else {
                try (InputStream is = Files.newInputStream(profileFile);
                    JsonReader reader = Json.createReader(is)) {
                    JsonObject root = reader.readObject();
                    String name = root.containsKey("name") && !root.isNull("name") ? root.getString("name") : "Giocatore";
                    String avatarPath = root.containsKey("avatarPath") && !root.isNull("avatarPath") ? root.getString("avatarPath") : null;

                    List<GamesRecord> history = new ArrayList<>();
                    if (root.containsKey("history") && root.get("history").getValueType() == JsonValue.ValueType.ARRAY) {
                        JsonArray arr = root.getJsonArray("history");
                        for (JsonValue v : arr) {
                            if (v.getValueType() != JsonValue.ValueType.OBJECT) continue;
                            JsonObject o = (JsonObject) v;
                            String date = o.containsKey("date") && !o.isNull("date") ? o.getString("date") : "";
                            String opponent = o.containsKey("opponent") && !o.isNull("opponent") ? o.getString("opponent") : "";
                            String result = o.containsKey("result") && !o.isNull("result") ? o.getString("result") : "";
                            history.add(new GamesRecord(date, opponent, result));
                        }
                    }
                    return new UserProfile(name, avatarPath, history);
                }
            }
        } catch (IOException | JsonParsingException e) {
            e.printStackTrace();
            return new UserProfile("Giocatore", null, new ArrayList<>());
        }
    }

    @Override
    public synchronized void save(UserProfile profile) throws IOException {
        if (Files.notExists(profileDir)) Files.createDirectories(profileDir);
        Path tmp = profileDir.resolve(FILE_NAME + ".tmp");

        JsonArrayBuilder arrBuilder = Json.createArrayBuilder();
        for (GamesRecord r : profile.getHistory()) {
            arrBuilder.add(Json.createObjectBuilder()
                    .add("date", r.getDate() == null ? "" : r.getDate())
                    .add("opponent", r.getOpponent() == null ? "" : r.getOpponent())
                    .add("result", r.getResult() == null ? "" : r.getResult())
            );
        }

        JsonObjectBuilder rootBuilder = Json.createObjectBuilder()
                .add("name", profile.getName() == null ? "" : profile.getName());

        if (profile.getAvatarPath() == null) rootBuilder.addNull("avatarPath");
        else rootBuilder.add("avatarPath", profile.getAvatarPath());

        rootBuilder.add("history", arrBuilder);

        JsonObject root = rootBuilder.build();

        try (OutputStream os = Files.newOutputStream(tmp, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
             JsonWriter writer = Json.createWriterFactory(null).createWriter(os)) {
            writer.writeObject(root);
        }

        try {
            Files.move(tmp, profileFile, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (AtomicMoveNotSupportedException ex) {
            Files.move(tmp, profileFile, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    public Path getProfileFilePath() { return profileFile; }
}