package model;

public abstract class Utilisateur {
    protected int id;
    protected String nom, prenom, email;
    protected String motDePasse;
    protected String role;

    public abstract void afficherDashboard(); // chaque rôle a son dashboard
}
