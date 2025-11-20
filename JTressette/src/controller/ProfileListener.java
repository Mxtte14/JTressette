package controller;

import profile.UserProfile;

/**
 * Listener per ricevere notifiche sulla modifica del profilo.
 */
public interface ProfileListener {
    void onProfileUpdated(UserProfile profile);
    default void onProfileSaveFailed(Exception ex) {}
}