package controller;

import profile.GamesRecord;
import profile.UserProfile;

import java.io.File;

/**
 * Controller che espone operazioni per modificare il profilo.
 */
public interface ProfileController {
    UserProfile getProfile();
    void setName(String newName);
    void setAvatar(File imageFile);
    void addListener(ProfileListener l);
    void removeListener(ProfileListener l);
    void recordMatch(profile.GamesRecord game);
}