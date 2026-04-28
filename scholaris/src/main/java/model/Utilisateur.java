package model;
import java.time.LocalDateTime;
/**
 * Classe abstraite représentant un utilisateur du système.
 * Classe parente de Etudiant, Professeur et Admin.
 */
public abstract class Utilisateur {

    // Attributs
    private int id;
    private String nom;
    private String prenom;
    private String email;
    private String motDePasse;
    private String role;
    private boolean actif;
    private LocalDateTime createdAt;

    // Constructeurs avec et sans paramètres



    public Utilisateur() {
        this.actif = true;
        this.createdAt = LocalDateTime.now();
    }
    public Utilisateur(int id, String nom, String prenom,
                       String email, String motDePasse, String role) {
        this.id = id; this.nom = nom; this.prenom = prenom;
        this.email = email; this.motDePasse = motDePasse; this.role = role;
        this.actif = true; this.createdAt = LocalDateTime.now();
    }





    // Getters et setters
    public int getId() {
        return id;
    }

    public String getNom() {
        return nom;
    }


    public String getPrenom() {
        return prenom;
    }

    public String getEmail() {
        return email;
    }

    public String getMotDePasse() {
        return motDePasse;
    }

    public String getRole() {
        return role;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt; }

    public boolean isActif() {
        return actif;
    }
    public void setId(int id) {
        this.id = id;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }


    public void setEmail(String email) {
        this.email = email;
    }


    public void setMotDePasse(String motDePasse) {
        this.motDePasse = motDePasse;
    }

    public void setRole(String role) {
        this.role = role; }

    public void setActif(boolean a) {
        this.actif = a;
    }

    // Comportement de la classe utilisateur avec ses fonctions de bases

    public boolean seConnecter(String motDePasse) {
        return this.motDePasse != null && this.motDePasse.equals(motDePasse);
    }

    public String getNomComplet() {
        return prenom + " " + nom.toUpperCase();
    }

    public void setCreatedAt(LocalDateTime c) {
        this.createdAt = c;
    }

    @Override
    public String toString() {
        return "[" + role + "] " + getNomComplet() + " <" + email + ">";
    }
    public abstract void   afficherDashboard();
    public abstract String[] getPermissions();

    public boolean hasRole(String r) { return role != null && role.equalsIgnoreCase(r); }
}
