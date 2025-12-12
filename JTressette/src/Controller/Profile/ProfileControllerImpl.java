package Controller.Profile;

import Model.Profile.GamesRecord;
import Model.Profile.StorageProfile;
import Model.Profile.UserProfile;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Implementazione del controller: aggiorna il modello e salva su file in background.
 * Notifica i ProfileListener su EDT.
 */
public class ProfileControllerImpl implements ProfileController {
    private final StorageProfile storage;
    private final UserProfile profile;
    private final List<ProfileListener> listeners = new CopyOnWriteArrayList<>();

    public ProfileControllerImpl(StorageProfile storage, UserProfile profile) {
        this.storage = storage;
        this.profile = profile;
    }

    @Override
    public UserProfile getProfile() {
        return profile;
    }

    @Override
    public void setName(String newName) {
        if (newName == null || newName.isBlank()) return;
        profile.setUsername(newName);
        saveAsync();
    }

    @Override
    public void setAvatar(File imageFile) {
        if (imageFile == null || !imageFile.exists()) return;
        // Opzione: qui puoi copiare il file nella cartella dell'app; per semplicità salvo l'assoluto
        profile.setAvatarPath(imageFile.getAbsolutePath());
        saveAsync();
    }

    @Override
    public void recordMatch(GamesRecord game) {
        profile.addGameRecord(game);
        saveAsync();
    }

    @Override
    public void addListener(ProfileListener l) {
        listeners.add(l);
    }


    private void saveAsync() {
        SwingWorker<Void, Void> w = new SwingWorker<>() {
            Exception err = null;
            @Override
            protected Void doInBackground() {
                try {
                    storage.save(profile);
                } catch (IOException e) {
                    err = e;
                }
                return null;
            }
            @Override
            protected void done() {
                if (err != null) {
                    listeners.forEach(l -> l.onProfileSaveFailed(err));
                } else {
                    listeners.forEach(l -> l.onProfileUpdated(profile));
                }
            }
        };
        w.execute();
    }
}

