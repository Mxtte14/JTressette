package util;

import java.util.prefs.Preferences;

/**
 * Piccolo wrapper su java.util.prefs per impostazioni globali (ultimo user, volumi).
 */
public class PreferencesManager {

    private static final Preferences prefs = Preferences.userNodeForPackage(PreferencesManager.class);
    private static final String KEY_LAST_USER = "last_user";
    private static final String KEY_MUSIC_VOLUME = "music_volume";
    private static final String KEY_SOUND_VOLUME = "sound_volume";

    public static void saveLastUser(String username) {
        if (username == null) prefs.remove(KEY_LAST_USER);
        else prefs.put(KEY_LAST_USER, username);
    }

    public static String loadLastUser() {
        return prefs.get(KEY_LAST_USER, null);
    }

    public static void saveMusicVolume(float value) {
        prefs.putDouble(KEY_MUSIC_VOLUME, value);
    }

    public static float loadMusicVolume(float defaultValue) {
        return (float) prefs.getDouble(KEY_MUSIC_VOLUME, defaultValue);
    }

    public static void saveSoundVolume(float value) {
        prefs.putDouble(KEY_SOUND_VOLUME, value);
    }

    public static float loadSoundVolume(float defaultValue) {
        return (float) prefs.getDouble(KEY_SOUND_VOLUME, defaultValue);
    }
}
