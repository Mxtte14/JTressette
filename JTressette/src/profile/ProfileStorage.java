package profile;

import java.io.IOException;

/**
 * Interfaccia di persistenza per il profilo (Model / Infra).
 */
public interface ProfileStorage {
    /**
     * Carica il profilo da disco o crea e salva un profilo di default.
     */
    UserProfile loadOrCreateDefault();

    /**
     * Salva il profilo su disco (deve essere thread-safe).
     */
    void save(UserProfile profile) throws IOException;
}