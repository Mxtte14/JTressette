package Model.Impostazioni;

import java.io.*;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Gestore centralizzato delle impostazioni utente del gioco.
 * Implementa il pattern Singleton per garantire un unico punto di accesso
 * alle configurazioni dell'applicazione.
 * 
 * <p>Impostazioni gestite:</p>
 * <ul>
 *   <li><b>Volume:</b> livello audio generale (0-100)</li>
 *   <li><b>Effects:</b> abilita/disabilita effetti sonori</li>
 *   <li><b>ShowScore:</b> mostra/nasconde punteggi durante la partita</li>
 *   <li><b>ShowMessages:</b> mostra/nasconde messaggi di sistema</li>
 *   <li><b>Fullscreen:</b> modalità schermo intero (futura implementazione)</li>
 * </ul>
 * 
 * <p>Le impostazioni vengono salvate automaticamente su file nella directory home
 * dell'utente (~/.jtressette/settings.properties) e caricate all'avvio.</p>
 * 
 * <p>Supporta il pattern Observer attraverso l'interfaccia SettingsListener
 * per notificare i componenti dei cambiamenti in tempo reale.</p>
 */
public class MenuImpostazioni {
    /** Nome della directory di salvataggio delle impostazioni */
    private static final String DIR_NAME = ".jtressette";
    
    /** Nome del file delle impostazioni */
    private static final String FILE_NAME = "settings.properties";
    
    /** Istanza singleton */
    private static MenuImpostazioni instance;

    /** Volume generale (0-100) */
    private int volume = 50;
    
    /** Flag per abilitare/disabilitare effetti sonori */
    private boolean effects = true;
    
    /** Flag per mostrare/nascondere punteggi in-game */
    private boolean showScore = true;
    
    /** Flag per mostrare/nascondere messaggi di sistema */
    private boolean showMessages = true;
    
    /** Flag per modalità schermo intero */
    private boolean fullscreen = false;

    /** Percorso alla directory delle impostazioni */
    private final Path settingsDir;
    
    /** Percorso al file delle impostazioni */
    private final Path settingsFile;

    /** Lista dei listener registrati per ricevere notifiche */
    private final List<SettingsListener> listeners = new ArrayList<>();

    /**
     * Costruttore privato per pattern Singleton.
     * Inizializza i percorsi e carica le impostazioni salvate.
     */
    private MenuImpostazioni() {
        String home = System.getProperty("user.home");
        settingsDir = Paths.get(home, DIR_NAME);
        settingsFile = settingsDir.resolve(FILE_NAME);
        load();
    }

    /**
     * Restituisce l'istanza singleton delle impostazioni.
     * Se l'istanza non esiste, viene creata automaticamente.
     * Metodo thread-safe.
     * 
     * @return l'istanza unica di MenuImpostazioni
     */
    public static synchronized MenuImpostazioni getInstance() {
        if (instance == null) {
            instance = new MenuImpostazioni();
        }
        return instance;
    }

    /**
     * Restituisce il livello del volume corrente.
     * 
     * @return volume (0-100)
     */
    public int getVolume() { return volume; }
    
    /**
     * Imposta il livello del volume.
     * Il valore viene automaticamente limitato nell'intervallo 0-100.
     * Notifica i listener del cambiamento.
     * 
     * @param v nuovo livello di volume
     */
    public void setVolume(int v) {
        volume = Math.max(0, Math.min(v, 100));
        notifyListeners();
    }
    
    /**
     * Verifica se gli effetti sonori sono abilitati.
     * 
     * @return true se gli effetti sono abilitati
     */
    public boolean isEffects() { return effects; }
    
    /**
     * Abilita o disabilita gli effetti sonori.
     * Notifica i listener del cambiamento.
     * 
     * @param eff true per abilitare, false per disabilitare
     */
    public void setEffects(boolean eff) {
        effects = eff;
        notifyListeners();
    }
    
    /**
     * Verifica se la visualizzazione dei punteggi è abilitata.
     * 
     * @return true se i punteggi sono visibili
     */
    public boolean isShowScore() { return showScore; }
    
    /**
     * Abilita o disabilita la visualizzazione dei punteggi in-game.
     * Notifica i listener del cambiamento.
     * 
     * @param sel true per mostrare, false per nascondere
     */
    public void setShowScore(boolean sel) {
        showScore = sel;
        notifyListeners();
    }
    
    /**
     * Verifica se la visualizzazione dei messaggi è abilitata.
     * 
     * @return true se i messaggi sono visibili
     */
    public boolean isShowMessages() { return showMessages; }
    
    /**
     * Abilita o disabilita la visualizzazione dei messaggi di sistema.
     * Notifica i listener del cambiamento.
     * 
     * @param sel true per mostrare, false per nascondere
     */
    public void setShowMessages(boolean sel) {
        showMessages = sel;
        notifyListeners();
    }
    
    /**
     * Verifica se la modalità schermo intero è abilitata.
     * 
     * @return true se in modalità fullscreen
     */
    public boolean isFullscreen() { return fullscreen; }
    
    /**
     * Abilita o disabilita la modalità schermo intero.
     * Notifica i listener del cambiamento.
     * 
     * @param fs true per abilitare fullscreen
     */
    public void setFullscreen(boolean fs) {
        fullscreen = fs;
        notifyListeners();
    }

    /**
     * Registra un listener per ricevere notifiche sui cambiamenti delle impostazioni.
     * Previene l'inserimento di duplicati.
     * 
     * @param listener il listener da registrare (null viene ignorato)
     */
    public void addListener(SettingsListener listener) {
        if (listener != null && !listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    /**
     * Rimuove un listener dalla lista delle notifiche.
     * 
     * @param listener il listener da rimuovere
     */
    public void removeListener(SettingsListener listener) {
        listeners.remove(listener);
    }

    /**
     * Notifica tutti i listener registrati del cambiamento delle impostazioni.
     * Metodo interno chiamato automaticamente dai setter.
     */
    private void notifyListeners() {
        for (SettingsListener listener : listeners) {
            listener.onSettingsChanged(this);
        }
    }

    /**
     * Interfaccia per i listener dei cambiamenti delle impostazioni.
     * I componenti che vogliono reagire ai cambiamenti delle impostazioni
     * devono implementare questa interfaccia e registrarsi tramite addListener().
     */
    public interface SettingsListener {
        /**
         * Chiamato quando una o più impostazioni vengono modificate.
         * 
         * @param settings l'oggetto MenuImpostazioni con i nuovi valori
         */
        void onSettingsChanged(MenuImpostazioni settings);
    }

    /**
     * Carica le impostazioni dal file su disco.
     * Se il file non esiste, mantiene i valori di default.
     * In caso di errori di parsing, usa i valori di default per i campi problematici.
     * Metodo chiamato automaticamente dal costruttore.
     */
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

    /**
     * Salva le impostazioni correnti su file.
     * Crea la directory se non esiste.
     * In caso di errore, il salvataggio viene silenziosamente ignorato
     * per non interrompere il flusso dell'applicazione.
     * 
     * <p>Questo metodo dovrebbe essere chiamato esplicitamente quando
     * l'utente conferma le modifiche alle impostazioni.</p>
     */
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