package com.scholaris.service;

import com.scholaris.model.Utilisateur;

public class SessionManager {
    private static SessionManager instance;
    private Utilisateur currentUser;

    private SessionManager() {}

    public static SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }

    public void setCurrentUser(Utilisateur user) {
        this.currentUser = user;
    }

    public Utilisateur getCurrentUser() {
        return currentUser;
    }

    public void logout() {
        currentUser = null;
    }
}
