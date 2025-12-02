package audio;

import java.net.*;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;

public class AudioManager {

    Clip clip;
    URL[] soundURL = new URL[30];

    public AudioManager() {

        soundURL[0] = getClass().getResource("/res/audio/backgroundMenu.wav");
        soundURL[1] = getClass().getResource("/res/audio/MenuSelectionClick.wav");
        soundURL[2] = getClass().getResource("/res/audio/cardPlayed.wav");
        soundURL[3] = getClass().getResource("/res/audio/backGame.wav");

    }

    public void setFile(int i) {

        try {

            AudioInputStream ais = AudioSystem.getAudioInputStream(soundURL[i]);
            clip = AudioSystem.getClip();
            clip.open(ais);

        }catch(Exception e) { }

    }

    public void start() {
        clip.start();
    }

    public void stop() {
        clip.stop();
    }

    public void loop() {
        clip.loop(Clip.LOOP_CONTINUOUSLY);
    }
}