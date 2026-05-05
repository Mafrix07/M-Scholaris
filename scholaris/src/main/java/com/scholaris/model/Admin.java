package com.scholaris.model;
import java.util.List;

public class Admin extends Utilisateur {
    private int utilisateurId;
    private int niveauAcces;



    public Admin(){
        super();
        setRole("ADMIN");
        this.niveauAcces =1;
    }
    public Admin(int id, String nom, String prenom, String email, String motDePasse, int niveauAcces) {
        super(id, nom, prenom, email, motDePasse, "ADMIN");
        this.niveauAcces = niveauAcces;
    }

    // Getters et Setters
    public int getUtilisateurId() {
        return utilisateurId;
    }
    public int getNiveauAcces() {
        return niveauAcces;
    }
    public void setUtilisateurId(int u) {
        this.utilisateurId = u;
    }
    public void setNiveauAcces(int n) {
        this.niveauAcces = n;
    }


    // Comportement de la classe Admin

    // Fonctions communes aux utilisateurs mais redefinies
    @Override
    public void afficherDashboard() {
        System.out.println("=== Dashboard Administrateur : " + getNomComplet() + " ===");
        System.out.println("Niveau d'accès : " + niveauAcces);
        System.out.println("Accès complet au système");
    }

    @Override
    public String[] getPermissions() {
        return new String[]{
                "GERER_UTILISATEURS","GERER_ETUDIANTS","GERER_CLASSES",
                "GERER_MATIERES","SAISIR_NOTE","MODIFIER_NOTE",
                "GENERER_BULLETIN","VOIR_STATISTIQUES","GERER_EVENEMENTS"
        };
    }
    @Override public String toString() { return "Admin{nom='" + getNomComplet() + "', niveau=" + niveauAcces + "}"; }


    // fonctions spécifique aux admins
    public void gererUtilisateurs() { System.out.println("[Admin] Gestion des utilisateurs"); }
    public void genererBulletins()  { System.out.println("[Admin] Génération des bulletins"); }
    public void gererClasses()      { System.out.println("[Admin] Gestion des classes"); }


}