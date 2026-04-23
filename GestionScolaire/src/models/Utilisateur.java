package models;

public class Utilisateur {
    public enum Role {
        ADMIN, 
        ENSEIGNANT,
        ETUDIANT
    }

    private String identifiant;
    private String motDePasse;
    private Role role;
    private String nomComplet;
    private String matriculeEtudiant;

    public Utilisateur(String identifiant, String motDePasse, Role role, String nomComplet) {
        this(identifiant, motDePasse, role, nomComplet, null);
    }

    public Utilisateur(String identifiant, String motDePasse, Role role, String nomComplet, String matriculeEtudiant) {
        this.identifiant = identifiant;
        this.motDePasse = motDePasse;
        this.role = role;
        this.nomComplet = nomComplet;
        this.matriculeEtudiant = matriculeEtudiant;
    }

    public boolean verifierMotDePasse(String mdp) {
        return this.motDePasse.equals(mdp);
    }

    public String getIdentifiant() {
         return identifiant; 
        
        }
    public Role getRole() {
         return role; 
        }
    public String getNomComplet() { 
        return nomComplet; 
    }
    public String getMatriculeEtudiant() {
         return matriculeEtudiant;
         }
    public void setMatriculeEtudiant(String matricule) { this.matriculeEtudiant = matricule; }

    @Override
    public String toString() {
        return String.format("Utilisateur{id='', role=, nom=''}", identifiant, role, nomComplet);
    }
}