package audio;

import javax.sound.sampled.*;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class AudioImpl {

    private final Map<Integer, String> soundPaths = new HashMap<>();
    private Clip musicClip;
    private float musicVolume = 1.0f;
    private float soundVolume = 1.0f;

    public AudioImpl() {
        soundPaths.put(0, "/audio/backgroundMenu.wav");
        soundPaths.put(1, "/audio/Game.wav");
        soundPaths.put(2, "/audio/Menu Selection Click.wav");
        soundPaths.put(3, "/audio/HandLoss.wav");
    }

    public void loadMusic(int id) {
        String path = soundPaths.get(id);
        if (path == null) return;
        URL url = getClass().getResource(path);
        if (url == null) {
            System.err.println("Audio resource non trovata: " + path);
            return;
        }
        try (AudioInputStream ais = AudioSystem.getAudioInputStream(url)) {
            if (musicClip != null && musicClip.isOpen()) {
                musicClip.close();
            }
            musicClip = AudioSystem.getClip();
            musicClip.open(ais);
            setVolumeOnClip(musicClip, musicVolume);
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            e.printStackTrace();
        }
    }

    public void playMusic(int id) {
        if (musicClip == null) loadMusic(id);
        if (musicClip == null) return;
        if (musicClip.isRunning()) musicClip.stop();
        musicClip.setFramePosition(0);
        musicClip.start();
    }

    public void loopMusic(int id) {
        if (musicClip == null) loadMusic(id);
        if (musicClip == null) return;
        if (musicClip.isRunning()) musicClip.stop();
        musicClip.setFramePosition(0);
        musicClip.loop(Clip.LOOP_CONTINUOUSLY);
    }

    public void stopMusic() {
        if (musicClip != null && musicClip.isRunning()) {
            musicClip.stop();
        }
    }

    public void playSound(int id) {
        String path = soundPaths.get(id);
        if (path == null) return;
        URL url = getClass().getResource(path);
        if (url == null) {
            System.err.println("Audio resource non trovata: " + path);
            return;
        }
        new Thread(() -> {
            try (AudioInputStream ais = AudioSystem.getAudioInputStream(url)) {
                Clip clip = AudioSystem.getClip();
                clip.open(ais);
                setVolumeOnClip(clip, soundVolume);
                clip.addLineListener(event -> {
                    if (event.getType() == LineEvent.Type.STOP) {
                        clip.close();
                    }
                });
                clip.start();
            } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
                e.printStackTrace();
            }
        }, "Audio-play-" + id).start();
    }

    public void stopAll() {
        stopMusic();
    }

    public void setMusicVolume(float percent) {
        musicVolume = clamp(percent, 0f, 1f);
        if (musicClip != null && musicClip.isOpen()) {
            setVolumeOnClip(musicClip, musicVolume);
        }
    }

    public void setSoundVolume(float percent) {
        soundVolume = clamp(percent, 0f, 1f);
    }

    public float getMusicVolume() {
        return musicVolume;
    }

    public float getSoundVolume() {
        return soundVolume;
    }

    private void setVolumeOnClip(Clip clip, float percent) {
        if (clip == null) return;
        try {
            if (clip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
                FloatControl gainControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
                float dB = linearToDecibels(percent);
                gainControl.setValue(dB);
            } else if (clip.isControlSupported(FloatControl.Type.VOLUME)) {
                FloatControl vol = (FloatControl) clip.getControl(FloatControl.Type.VOLUME);
                vol.setValue(percent);
            }
        } catch (Exception e) {
            // controllo non supportato o errore; ignoriamo
        }
    }

    private float linearToDecibels(float linear) {
        if (linear <= 0f) return -80f;
        return (float) (20.0 * Math.log10(linear));
    }

    private float clamp(float v, float min, float max) {
        if (v < min) return min;
        if (v > max) return max;
        return v;
    }

    public void close() {
        if (musicClip != null) {
            if (musicClip.isRunning()) musicClip.stop();
            musicClip.close();
            musicClip = null;
        }
    }
}