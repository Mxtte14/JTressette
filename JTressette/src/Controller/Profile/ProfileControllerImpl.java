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
 * Implementazione concreta del ProfileController.
 * Gestisce le modifiche al profilo utente, il salvataggio asincrono su disco
 * e la notifica dei listener registrati.
 *
 * <p>Caratteristiche principali:</p>
 * <ul>
 *   <li>Salvataggio asincrono su thread in background per non bloccare l'UI</li>
 *   <li>Notifiche ai listener sul thread EDT di Swing</li>
 *   <li>Lista thread-safe di listener (CopyOnWriteArrayList)</li>
 *   <li>Validazione input per prevenire stati inconsistenti</li>
 * </ul>
 */
public class ProfileControllerImpl implements ProfileController {
    /** Gestore della persistenza del profilo su file */
    private final StorageProfile storage;

    /** Profilo utente gestito da questo controller */
    private final UserProfile profile;

    /** Lista thread-safe dei listener registrati */
    private final List<ProfileListener> listeners = new CopyOnWriteArrayList<>();

    /**
     * Costruttore del controller.
     *
     * @param storage gestore della persistenza per salvare/caricare il profilo
     * @param profile il profilo utente da gestire
     */
    public ProfileControllerImpl(StorageProfile storage, UserProfile profile) {
        this.storage = storage;
        this.profile = profile;
    }

    /**
     * Restituisce il profilo utente corrente.
     *
     * @return il profilo gestito
     */
    @Override
    public UserProfile getProfile() {
        return profile;
    }

    /**
     * Modifica il nome utente del profilo.
     * Il nome viene validato (non nullo e non vuoto) prima di essere applicato.
     * Le modifiche vengono salvate automaticamente in modo asincrono.
     *
     * @param newName il nuovo nome utente
     */
    @Override
    public void setName(String newName) {
        if (newName == null || newName.isBlank()) return;
        profile.setUsername(newName);
        saveAsync();
    }

    /**
     * Imposta un nuovo file immagine come avatar del profilo.
     * Il file viene validato (esistente) prima di essere applicato.
     * Le modifiche vengono salvate automaticamente in modo asincrono.
     *
     * @param imageFile file immagine da usare come avatar
     */
    @Override
    public void setAvatar(File imageFile) {
        if (imageFile == null || !imageFile.exists()) return;
        profile.setAvatarPath(imageFile.getAbsolutePath());
        saveAsync();
    }

    /**
     * Registra una partita completata nello storico del profilo.
     * Il record di gioco viene aggiunto al profilo e salvato automaticamente.
     *
     * @param game dati della partita da registrare
     */
    @Override
    public void recordMatch(GamesRecord game) {
        profile.addGameRecord(game);
        saveAsync();
    }

    /**
     * Registra un listener per ricevere notifiche sugli aggiornamenti del profilo.
     *
     * @param l il listener da aggiungere
     */
    @Override
    public void addListener(ProfileListener l) {
        listeners.add(l);
    }


    /**
     * Salva il profilo in modo asincrono utilizzando un SwingWorker.
     * Il salvataggio avviene su un thread in background per non bloccare l'interfaccia.
     * Al termine del salvataggio, i listener vengono notificati sul thread EDT.
     * In caso di errore, viene chiamato onProfileSaveFailed sui listener.
     */
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