package impostazioni;

import java.io.*;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Gestione persistente delle impostazioni utente.
 * Singleton pattern per accesso globale.
 */
public class MenuImpostazioni {
    private static final String DIR_NAME = ".jtressette";
    private static final String FILE_NAME = "settings.properties";
    private static MenuImpostazioni instance;

    // Impostazioni principali
    private int volume = 50;
    private boolean effects = true;
    private boolean showScore = true;
    private boolean showMessages = true;
    private boolean fullscreen = false;

    // Percorsi file
    private final Path settingsDir;
    private final Path settingsFile;
    
    // Listeners per notificare cambiamenti
    private final List<SettingsListener> listeners = new ArrayList<>();

    private MenuImpostazioni() {
        String home = System.getProperty("user.home");
        settingsDir = Paths.get(home, DIR_NAME);
        settingsFile = settingsDir.resolve(FILE_NAME);
        load();
    }
    
    /**
     * Ottieni l'istanza singleton delle impostazioni.
     */
    public static synchronized MenuImpostazioni getInstance() {
        if (instance == null) {
            instance = new MenuImpostazioni();
        }
        return instance;
    }

    // ----- Getters & Setters -----
    public int getVolume() { return volume; }
    public void setVolume(int v) { 
        volume = Math.max(0, Math.min(v, 100)); 
        notifyListeners();
    }
    public boolean isEffects() { return effects; }
    public void setEffects(boolean eff) { 
        effects = eff; 
        notifyListeners();
    }
    public boolean isShowScore() { return showScore; }
    public void setShowScore(boolean sel) { 
        showScore = sel; 
        notifyListeners();
    }
    public boolean isShowMessages() { return showMessages; }
    public void setShowMessages(boolean sel) { 
        showMessages = sel; 
        notifyListeners();
    }
    public boolean isFullscreen() { return fullscreen; }
    public void setFullscreen(boolean fs) { 
        fullscreen = fs; 
        notifyListeners();
    }
    
    // ----- Listener Management -----
    public void addListener(SettingsListener listener) {
        if (listener != null && !listeners.contains(listener)) {
            listeners.add(listener);
        }
    }
    
    public void removeListener(SettingsListener listener) {
        listeners.remove(listener);
    }
    
    private void notifyListeners() {
        for (SettingsListener listener : listeners) {
            listener.onSettingsChanged(this);
        }
    }
    
    /**
     * Interfaccia per listener dei cambiamenti delle impostazioni.
     */
    public interface SettingsListener {
        void onSettingsChanged(MenuImpostazioni settings);
    }

    // ----- Persistance -----
    public void load() {
        if (!Files.exists(settingsFile)) return;
        Properties props = new Properties();
        try (InputStream in = Files.newInputStream(settingsFile)) {
            props.load(in);
            volume      = Integer.parseInt(props.getProperty("volume", "50"));
            effects     = Boolean.parseBoolean(props.getProperty("effects", "true"));
            showScore   = Boolean.parseBoolean(props.getProperty("showScore", "true"));
            showMessages= Boolean.parseBoolean(props.getProperty("showMessages", "true"));
            fullscreen  = Boolean.parseBoolean(props.getProperty("fullscreen", "false"));
        } catch(Exception e) { /* Ignora, usa default */ }
    }

    public void save() {
        try {
            if (!Files.exists(settingsDir)) Files.createDirectories(settingsDir);
            Properties props = new Properties();
            props.setProperty("volume", String.valueOf(volume));
            props.setProperty("effects", String.valueOf(effects));
            props.setProperty("showScore", String.valueOf(showScore));
            props.setProperty("showMessages", String.valueOf(showMessages));
            props.setProperty("fullscreen", String.valueOf(fullscreen));
            try (OutputStream out = Files.newOutputStream(settingsFile, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
                props.store(out, "JTressette Settings");
            }
        } catch(Exception e){ /* log/ignora */ }
    }
}