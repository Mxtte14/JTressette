package Model.Audio;

import java.io.IOException;
import java.net.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sound.sampled.*;
import javax.swing.Timer;

/**
 * Gestore centralizzato dell'audio del gioco.
 * Gestisce la riproduzione di musica di sottofondo ed effetti sonori con supporto per:
 * <ul>
 *   <li>Transizioni audio (fade-in e fade-out)</li>
 *   <li>Controllo del volume dinamico</li>
 *   <li>Riproduzione in loop per musica di sottofondo</li>
 *   <li>Effetti sonori one-shot senza interrompere l'audio principale</li>
 * </ul>
 *
 * <p><b>Indici audio disponibili:</b></p>
 * <ul>
 *   <li>0 - BACKGROUND_MENU: musica di sottofondo del menu principale</li>
 *   <li>1 - MENU_SELECTION_CLICK: suono click per selezioni menu</li>
 *   <li>2 - CARD_PLAYED: suono quando si gioca una carta</li>
 *   <li>3 - BACKGROUND_GAME: musica di sottofondo durante la partita</li>
 *   <li>4 - CARD_DRAW: suono quando si pesca una carta</li>
 *   <li>5 - CARD_DEALING: suono durante la distribuzione delle carte</li>
 *   <li>6 - VICTORY: suono di vittoria</li>
 *   <li>7 - DEFEAT: suono di sconfitta</li>
 * </ul>
 */
public class AudioManager {

    /** Logger per la registrazione di eventi ed errori audio */
    private static final Logger LOGGER = Logger.getLogger(AudioManager.class.getName());

    /** Indice audio: musica di sottofondo del menu */
    public static final int BACKGROUND_MENU = 0;

    /** Indice audio: click di selezione menu */
    public static final int MENU_SELECTION_CLICK = 1;

    /** Indice audio: suono carta giocata */
    public static final int CARD_PLAYED = 2;

    /** Indice audio: musica di sottofondo del gioco */
    public static final int BACKGROUND_GAME = 3;

    /** Indice audio: suono pesca carta */
    public static final int CARD_DRAW = 4;

    /** Indice audio: suono distribuzione carte */
    public static final int CARD_DEALING = 5;

    /** Indice audio: suono vittoria */
    public static final int VICTORY = 6;

    /** Indice audio: suono sconfitta */
    public static final int DEFEAT = 7;

    /** Costante di scalatura del volume massimo sicuro (0.4 = 40% del massimo) */
    public static final float MAX_VOLUME_SCALE = 0.4f;

    /** Clip audio principale attualmente in riproduzione */
    private Clip clip;

    /** Array degli URL dei file audio */
    private final URL[] soundURL = new URL[30];

    /** Volume corrente (da 0.0 a 1.0) */
    private float currentVolume = 1.0f;

    /** Timer per gestire le transizioni fade */
    private Timer fadeTimer;

    /** Flag che indica se è in corso una transizione fade */
    private boolean isFading = false;

    /**
     * Costruttore dell'AudioManager.
     * Inizializza gli URL dei file audio caricandoli dalle risorse del progetto.
     * I file devono trovarsi nella directory /res/audio/.
     */
    public AudioManager() {
        soundURL[BACKGROUND_MENU] = getClass().getResource("/res/audio/backgroundMenu.wav");
        soundURL[MENU_SELECTION_CLICK] = getClass().getResource("/res/audio/SelectionClick.wav");
        soundURL[CARD_PLAYED] = getClass().getResource("/res/audio/cardPlayed.wav");
        soundURL[BACKGROUND_GAME] = getClass().getResource("/res/audio/backGame.wav");
        soundURL[CARD_DRAW] = getClass().getResource("/res/audio/cardDraw.wav");
        soundURL[CARD_DEALING] = getClass().getResource("/res/audio/dealing.wav");
        soundURL[VICTORY] = getClass().getResource("/res/audio/victory.wav");
        soundURL[DEFEAT] = getClass().getResource("/res/audio/defeat.wav");
    }

    /**
     * Imposta il file audio da riprodurre.
     * Chiude l'eventuale clip precedente e apre quello nuovo.
     * Il volume viene applicato automaticamente al nuovo clip.
     *
     * @param i indice del file audio da caricare (usare le costanti pubbliche)
     */
    public void setFile(int i) {
        try {
            if (clip != null && clip.isOpen()) {
                clip.close();
            }
            AudioInputStream ais = AudioSystem.getAudioInputStream(soundURL[i]);
            clip = AudioSystem.getClip();
            clip.open(ais);
            setVolume(currentVolume);
        } catch (UnsupportedAudioFileException e) {
            LOGGER.log(Level.WARNING, "Unsupported audio file format for index: " + i, e);
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "IO error loading audio file for index: " + i, e);
        } catch (LineUnavailableException e) {
            LOGGER.log(Level.WARNING, "Audio line unavailable for index: " + i, e);
        }
    }

    /**
     * Avvia la riproduzione dell'audio dal inizio.
     * Riporta il clip alla posizione iniziale e inizia la riproduzione.
     */
    public void start() {
        if (clip != null) {
            clip.setFramePosition(0);
            clip.start();
        }
    }

    /**
     * Ferma la riproduzione dell'audio corrente.
     */
    public void stop() {
        if (clip != null) {
            clip.stop();
        }
    }

    /**
     * Attiva la riproduzione in loop continuo dell'audio.
     * Ideale per musica di sottofondo.
     */
    public void loop() {
        if (clip != null) {
            clip.loop(Clip.LOOP_CONTINUOUSLY);
        }
    }

    /**
     * Imposta il volume del clip audio corrente.
     * Il volume viene limitato automaticamente tra 0.0 e MAX_VOLUME_SCALE (0.4)
     * per evitare distorsioni o livelli pericolosi.
     *
     * @param volume livello del volume da 0.0 (silenzio) a 1.0 (volume massimo)
     */
    public void setVolume(float volume) {
        currentVolume = Math.max(0.0f, Math.min(0.4f, volume));
        if (clip != null && clip.isOpen()) {
            try {
                FloatControl gainControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
                // Convert linear volume to decibels
                float dB = (float) (Math.log(Math.max(currentVolume, 0.0001)) / Math.log(10.0) * 20.0);
                dB = Math.max(dB, gainControl.getMinimum());
                dB = Math.min(dB, gainControl.getMaximum());
                gainControl.setValue(dB);
            } catch (Exception e) {
                // Volume control not supported
            }
        }
    }

    /**
     * Restituisce il livello di volume corrente.
     *
     * @return volume corrente da 0.0 a 1.0
     */
    public float getVolume() {
        return currentVolume;
    }

    /**
     * Esegue un effetto fade-out (dissolvenza in uscita) sull'audio corrente.
     * Il volume viene ridotto gradualmente fino a zero in un tempo specificato,
     * dopo di che l'audio viene fermato e viene invocato il callback opzionale.
     *
     * @param durationMs durata del fade-out in millisecondi
     * @param onComplete callback da eseguire al completamento del fade (può essere null)
     */
    public void fadeOut(int durationMs, Runnable onComplete) {
        if (clip == null || !clip.isRunning()) {
            if (onComplete != null) {
                onComplete.run();
            }
            return;
        }

        stopFadeTimer();
        isFading = true;

        final float startVolume = currentVolume;
        final int steps = 20;
        final int delay = durationMs / steps;
        final float volumeStep = startVolume / steps;

        fadeTimer = new Timer(delay, null);
        final int[] currentStep = {0};

        fadeTimer.addActionListener(e -> {
            currentStep[0]++;
            float newVolume = startVolume - (volumeStep * currentStep[0]);
            setVolume(Math.max(0, newVolume));

            if (currentStep[0] >= steps) {
                fadeTimer.stop();
                stop();
                isFading = false;
                if (onComplete != null) {
                    onComplete.run();
                }
            }
        });
        fadeTimer.start();
    }

    /**
     * Esegue un effetto fade-in (dissolvenza in entrata) sull'audio.
     * L'audio parte da volume zero e viene aumentato gradualmente fino al volume target.
     * La riproduzione inizia automaticamente all'avvio del fade-in.
     *
     * @param durationMs durata del fade-in in millisecondi
     * @param targetVolume volume target da raggiungere (da 0.0 a 1.0)
     */
    public void fadeIn(int durationMs, float targetVolume) {
        if (clip == null) {
            return;
        }

        stopFadeTimer();
        isFading = true;

        setVolume(0);
        start();

        final int steps = 20;
        final int delay = durationMs / steps;
        final float volumeStep = targetVolume / steps;

        fadeTimer = new Timer(delay, null);
        final int[] currentStep = {0};

        fadeTimer.addActionListener(e -> {
            currentStep[0]++;
            float newVolume = volumeStep * currentStep[0];
            setVolume(Math.min(targetVolume, newVolume));

            if (currentStep[0] >= steps) {
                fadeTimer.stop();
                setVolume(targetVolume);
                isFading = false;
            }
        });
        fadeTimer.start();
    }


    /**
     * Riproduce un effetto sonoro one-shot senza interrompere l'audio principale.
     * Crea un nuovo clip temporaneo per l'effetto che viene chiuso automaticamente
     * al termine della riproduzione.
     *
     * @param soundIndex indice del suono da riprodurre (usare le costanti pubbliche)
     */
    public void playSoundEffect(int soundIndex) {
        if (soundURL[soundIndex] == null) {
            return;
        }
        try {
            AudioInputStream ais = AudioSystem.getAudioInputStream(soundURL[soundIndex]);
            Clip effectClip = AudioSystem.getClip();
            effectClip.open(ais);
            effectClip.addLineListener(event -> {
                if (event.getType() == LineEvent.Type.STOP) {
                    effectClip.close();
                }
            });
            effectClip.start();
        } catch (UnsupportedAudioFileException e) {
            LOGGER.log(Level.FINE, "Unsupported audio format for sound effect: " + soundIndex, e);
        } catch (IOException e) {
            LOGGER.log(Level.FINE, "IO error playing sound effect: " + soundIndex, e);
        } catch (LineUnavailableException e) {
            LOGGER.log(Level.FINE, "Audio line unavailable for sound effect: " + soundIndex, e);
        }
    }

    /**
     * Riproduce il suono di click del menu.
     * Effetto sonoro one-shot per confermare selezioni nel menu.
     */
    public void playMenuClick() {
        playSoundEffect(MENU_SELECTION_CLICK);
    }



    /**
     * Riproduce il suono di pesca carta.
     * Effetto sonoro one-shot quando si pesca una nuova carta dal mazzo.
     */
    public void playDrawSound() {
        playSoundEffect(CARD_DRAW);
    }

    /**
     * Riproduce il suono di carta giocata.
     * Effetto sonoro one-shot quando un giocatore gioca una carta.
     */
    public void playCardSound() {
        playSoundEffect(CARD_PLAYED);
    }

    /**
     * Riproduce il suono di vittoria.
     * Effetto sonoro one-shot al termine di una partita vinta.
     */
    public void playVictorySound() {
        playSoundEffect(VICTORY);
    }

    /**
     * Riproduce il suono di sconfitta.
     * Effetto sonoro one-shot al termine di una partita persa.
     */
    public void playDefeatSound() {
        playSoundEffect(DEFEAT);
    }

    /**
     * Ferma il timer di fade se è attivo.
     * Metodo interno per gestire le transizioni.
     */
    private void stopFadeTimer() {
        if (fadeTimer != null && fadeTimer.isRunning()) {
            fadeTimer.stop();
        }
        isFading = false;
    }

    /**
     * Verifica se l'audio è attualmente in riproduzione.
     *
     * @return true se sta riproducendo, false altrimenti
     */
    public boolean isPlaying() {
        return clip != null && clip.isRunning();
    }

    /**
     * Chiude e rilascia tutte le risorse audio.
     * Ferma eventuali timer di fade, l'audio e chiude il file audio.
     * Questo metodo dovrebbe essere chiamato quando l'AudioManager non è più necessario.
     */
    public void close() {
        stopFadeTimer();
        if (clip != null) {
            clip.stop();
            clip.close();
        }
    }
}