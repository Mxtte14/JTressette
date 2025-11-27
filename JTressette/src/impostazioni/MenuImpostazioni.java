package impostazioni;


/* * Classe SettingsMenu per gestire le impostazioni del gioco.
 In questa classe vengono implementati i metodi per visualizzare e modificare le impostazioni del gioco,
    come il volume, la risoluzione dello schermo, le preferenze di gioco, ecc.
 */

public class MenuImpostazioni {
    // Attributi per le impostazioni del gioco
    private int volume;
    private String screenResolution;
    private boolean enableSoundEffects;

    // Costruttore
    public MenuImpostazioni() {
        // Impostazioni di default
        this.volume = 50; // Volume di default al 50%
        this.screenResolution = "1920x1080"; // Risoluzione di default
        this.enableSoundEffects = true; // Effetti sonori abilitati di default
    }

    // Metodi per visualizzare e modificare le impostazioni

    public int getVolume() {
        return volume;
    }

    public void setVolume(int volume) {
        this.volume = volume;
    }

    public String getScreenResolution() {
        return screenResolution;
    }

    public void setScreenResolution(String screenResolution) {
        this.screenResolution = screenResolution;
    }

    public boolean isEnableSoundEffects() {
        return enableSoundEffects;
    }

    public void setEnableSoundEffects(boolean enableSoundEffects) {
        this.enableSoundEffects = enableSoundEffects;
    }
}
