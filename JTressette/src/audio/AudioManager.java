package audio;

/**
 * Singleton semplice per avere istanza condivisa di AudioImpl.
 * Usalo in tutta l'app: AudioManager.getInstance().getAudio()
 */
public class AudioManager {

    private static AudioManager instance;
    private final AudioImpl audio;

    private AudioManager() {
        audio = new AudioImpl();
        // carica volumi/setting predefiniti se necessario
    }

    public static synchronized AudioManager getInstance() {
        if (instance == null) instance = new AudioManager();
        return instance;
    }

    public AudioImpl getAudio() {
        return audio;
    }
}