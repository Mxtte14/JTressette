package Controller.Profile;

import Model.Profile.GamesRecord;
import Model.Profile.UserProfile;

import java.io.File;

/**
 * Interfaccia del controller per la gestione del profilo utente.
 * Fornisce operazioni per modificare il profilo (nome, avatar),
 * registrare partite e notificare i listener dei cambiamenti.
 *
 * <p>Segue il pattern MVC separando la logica di controllo dalla vista e dal modello.
 * Le modifiche al profilo vengono automaticamente persistite su disco.</p>
 */
public interface ProfileController {
    /**
     * Restituisce il profilo utente corrente.
     *
     * @return il profilo utente gestito da questo controller
     */
    UserProfile getProfile();

    /**
     * Modifica il nome utente del profilo.
     * Il nuovo nome viene validato e salvato automaticamente.
     *
     * @param newName il nuovo nome utente (non deve essere nullo o vuoto)
     */
    void setName(String newName);

    /**
     * Imposta un nuovo avatar per il profilo.
     * L'immagine viene copiata e il percorso salvato nel profilo.
     *
     * @param imageFile file immagine da utilizzare come avatar (deve esistere)
     */
    void setAvatar(File imageFile);

    /**
     * Registra un listener per ricevere notifiche sugli aggiornamenti del profilo.
     *
     * @param l il listener da registrare
     */
    void addListener(ProfileListener l);

    /**
     * Registra una partita completata nello storico del profilo.
     * I dati della partita vengono aggiunti al profilo e salvati automaticamente.
     *
     * @param game record della partita da registrare
     */
    void recordMatch(GamesRecord game);
}