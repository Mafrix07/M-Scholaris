package model;
import java.util.ArrayList;
import java.util.List;

public class Professeur extends Utilisateur {
    private int utilisateurId;
    private String specialite;
    private List<EnseignantMatiere> matieres = new ArrayList<>();


    // Constructeurs
    public Professeur() {
        super();
        setRole("enseignant");
    }

    public Professeur(int id, String nom , String prenom, String email, String motDePasse, String specialite){
        super(id, nom, prenom, email, motDePasse, "enseignant");
        this.specialite = specialite;

    }
    // Getters et SSetters

    public int getUtilisateurId() {
        return utilisateurId;
    }

    public List<EnseignantMatiere> getMatieres() {
        return matieres;
    }

    public String getSpecialite() {
        return specialite;
    }

    public void setUtilisateurId(int u) {
        this.utilisateurId = u;
    }

    public void setSpecialite(String s) {
        this.specialite = s;
    }

    public void setMatieres(List<EnseignantMatiere> m) {
        this.matieres = m;
    }

    // Comportement de la classe


    public void ajouterMatiere(EnseignantMatiere em) {
        if (em != null) matieres.add(em);
    }

    public void creerEvenement(Evenement e) {
        if (e != null) { e.setCreePar(getId()); System.out.println("Événement créé : " + e.getTitre()); }
    }

    // Fonctions communes aux utilisateurs mais redefinies
    @Override
    public String[] getPermissions() {
        return new String[]{"SAISIR_NOTE","MODIFIER_NOTE","VOIR_ETUDIANTS","CREER_EVENEMENT","VOIR_CALENDRIER"};
    }

    @Override
    public void afficherDashboard() {
        System.out.println("=== Dashboard Professeur : " + getNomComplet() + " ===");
        System.out.println("Spécialité : " + specialite);
        System.out.println("Matières enseignées : " + matieres.size());
    }


}