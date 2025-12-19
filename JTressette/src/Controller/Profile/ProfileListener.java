package Controller.Profile;

import Model.Profile.UserProfile;

/**
 * Interfaccia listener per ricevere notifiche sui cambiamenti del profilo utente.
 * Implementa il pattern Observer permettendo alla view di reagire agli aggiornamenti
 * del profilo in tempo reale.
 *
 * <p>Le implementazioni devono gestire le notifiche sul thread EDT (Event Dispatch Thread)
 * di Swing quando aggiornano componenti dell'interfaccia grafica.</p>
 */
public interface ProfileListener {
    /**
     * Chiamato quando il profilo utente viene aggiornato con successo.
     * Questo metodo viene invocato sul thread EDT dopo che le modifiche
     * sono state salvate correttamente su disco.
     *
     * @param profile il profilo aggiornato con i nuovi dati
     */
    void onProfileUpdated(UserProfile profile);

    /**
     * Chiamato quando il salvataggio del profilo fallisce.
     * Permette di gestire errori di I/O o altre eccezioni durante il salvataggio.
     *
     * @param ex l'eccezione che ha causato il fallimento del salvataggio
     */
    default void onProfileSaveFailed(Exception ex) {}
}