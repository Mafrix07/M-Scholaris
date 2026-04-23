package Services;

import models.Utilisateur;
import models.Utilisateur.Role;
import java.util.HashMap;
import java.util.Map;

public class Auth {
    private Map<String, Utilisateur> utilisateurs;
    private Utilisateur utilisateurConnecte;

    public Auth() {
        utilisateurs = new HashMap<>();
        chargerUtilisateursDemo();
    }

    public boolean connecter(String identifiant, String motDePasse) {
        Utilisateur ut = utilisateurs.get(identifiant);
        if (ut != null && ut.verifierMotDePasse(motDePasse)) {
            utilisateurConnecte = ut;
            return true;
        }
        return false;
    }

    public void deconnecter() {
        utilisateurConnecte = null;
    }

    public Utilisateur getUtilisateurConnecte() {
        return utilisateurConnecte;
    }

    public boolean estConnecte() {
        return utilisateurConnecte != null;
    }

    public boolean estAdmin() {
        return estConnecte() && utilisateurConnecte.getRole() == Role.ADMIN;
    }

    public boolean estEnseignant() {
        return estConnecte() && utilisateurConnecte.getRole() == Role.ENSEIGNANT;
    }

    public boolean estEtudiant() {
        return estConnecte() && utilisateurConnecte.getRole() == Role.ETUDIANT;
    }

    public boolean peutGererEtudiants() {
        return estAdmin();
    }

    public boolean peutSaisirNotes() {
        return estAdmin() || estEnseignant();
    }

    public boolean peutConsulterTout() {
        return estAdmin() || estEnseignant();
    }

    public boolean ajouterUtilisateur(String identifiant, String motDePasse, Role role, String nomComplet) {
        if (utilisateurs.containsKey(identifiant)) return false;
        utilisateurs.put(identifiant, new Utilisateur(identifiant, motDePasse, role, nomComplet));
        return true;
    }

    public boolean supprimerUtilisateur(String identifiant) {
        return utilisateurs.remove(identifiant) != null;
    }

    public Map<String, Utilisateur> getTousUtilisateurs() {
        return utilisateurs;
    }

    private void chargerUtilisateursDemo() {
        ajouterUtilisateur("admin", "admin123", Role.ADMIN, "Administrateur Principal");
        ajouterUtilisateur("prof.math", "math2024", Role.ENSEIGNANT, "Prof. Koffi Mathématiques");
        ajouterUtilisateur("prof.fr", "fr2024", Role.ENSEIGNANT, "Prof. Amavi Français");

        Utilisateur e1 = new Utilisateur("agbedigni.kofi", "etud001", Role.ETUDIANT, "AGBEDIGNI Kofi");
        e1.setMatriculeEtudiant("2024001");
        utilisateurs.put("agbedigni.kofi", e1);

        Utilisateur e2 = new Utilisateur("almeida.akosua", "etud002", Role.ETUDIANT, "D'ALMEIDA Akosua");
        e2.setMatriculeEtudiant("2024002");
        utilisateurs.put("almeida.akosua", e2);
    }
}