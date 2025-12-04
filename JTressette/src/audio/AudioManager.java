package audio;

import java.io.IOException;
import java.net.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sound.sampled.*;
import javax.swing.Timer;

/**
 * AudioManager - Manages audio playback with support for transitions (fade in/out),
 * volume control, and one-shot sound effects.
 *
 * Sound indices:
 * 0 - backgroundMenu (menu background music)
 * 1 - MenuSelectionClick (click sound for menu selections)
 * 2 - cardPlayed (sound when a card is played)
 * 3 - backGame (game background music)
 */
public class AudioManager {

    private static final Logger LOGGER = Logger.getLogger(AudioManager.class.getName());

    // Audio constants
    public static final int BACKGROUND_MENU = 0;
    public static final int MENU_SELECTION_CLICK = 1;
    public static final int CARD_PLAYED = 2;
    public static final int BACKGROUND_GAME = 3;

    private Clip clip;
    private final URL[] soundURL = new URL[30];
    private float currentVolume = 1.0f;
    private Timer fadeTimer;
    private boolean isFading = false;

    public AudioManager() {
        soundURL[BACKGROUND_MENU] = getClass().getResource("/res/audio/backgroundMenu.wav");
        soundURL[MENU_SELECTION_CLICK] = getClass().getResource("/res/audio/MenuSelectionClick.wav");
        soundURL[CARD_PLAYED] = getClass().getResource("/res/audio/cardPlayed.wav");
        soundURL[BACKGROUND_GAME] = getClass().getResource("/res/audio/backGame.wav");
    }

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

    public void start() {
        if (clip != null) {
            clip.setFramePosition(0);
            clip.start();
        }
    }

    public void stop() {
        if (clip != null) {
            clip.stop();
        }
    }

    public void loop() {
        if (clip != null) {
            clip.loop(Clip.LOOP_CONTINUOUSLY);
        }
    }

    /**
     * Set the volume of the current clip.
     * @param volume Volume level from 0.0 (silent) to 1.0 (full volume)
     */
    public void setVolume(float volume) {
        currentVolume = Math.max(0.0f, Math.min(1.0f, volume));
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
     * Get the current volume level.
     * @return Volume level from 0.0 to 1.0
     */
    public float getVolume() {
        return currentVolume;
    }

    /**
     * Fade out the current audio over a specified duration.
     * @param durationMs Duration of fade in milliseconds
     * @param onComplete Callback to run when fade completes (can be null)
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
     * Fade in the audio over a specified duration.
     * @param durationMs Duration of fade in milliseconds
     * @param targetVolume Target volume level (0.0 to 1.0)
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
     * Transition from current audio to a new audio file with crossfade effect.
     * @param newFileIndex Index of the new audio file
     * @param fadeDurationMs Duration of the fade in milliseconds
     * @param loopNew Whether to loop the new audio
     */
    public void transitionTo(int newFileIndex, int fadeDurationMs, boolean loopNew) {
        fadeOut(fadeDurationMs, () -> {
            setFile(newFileIndex);
            if (loopNew) {
                setVolume(0);
                loop();
                fadeIn(fadeDurationMs, 1.0f);
            } else {
                setVolume(1.0f);
                start();
            }
        });
    }

    /**
     * Play a one-shot sound effect without interrupting the main audio.
     * Creates a new clip for the sound effect.
     * @param soundIndex Index of the sound to play
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
     * Play the menu selection click sound effect.
     */
    public void playMenuClick() {
        playSoundEffect(MENU_SELECTION_CLICK);
    }

    /**
     * Play the card played sound effect.
     */
    public void playCardSound() {
        playSoundEffect(CARD_PLAYED);
    }

    private void stopFadeTimer() {
        if (fadeTimer != null && fadeTimer.isRunning()) {
            fadeTimer.stop();
        }
        isFading = false;
    }

    /**
     * Check if audio is currently fading.
     * @return true if fading, false otherwise
     */
    public boolean isFading() {
        return isFading;
    }

    /**
     * Check if audio is currently playing.
     * @return true if playing, false otherwise
     */
    public boolean isPlaying() {
        return clip != null && clip.isRunning();
    }

    /**
     * Close and release audio resources.
     */
    public void close() {
        stopFadeTimer();
        if (clip != null) {
            clip.stop();
            clip.close();
        }
    }
}