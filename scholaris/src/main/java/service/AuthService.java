package service;

import dao.UtilisateurDAO;
import model.Utilisateur;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.SQLException;

/**
 * Service d'authentification gérant les rôles et la sécurité BCrypt.
 */
public class AuthService {

    private UtilisateurDAO utilisateurDAO;
    private static Utilisateur utilisateurConnecte = null;

    public AuthService() throws SQLException {
        this.utilisateurDAO = new UtilisateurDAO();
    }

    /**
     * Authentifie l'utilisateur.
     * UtilisateurDAO.trouverParEmail retourne déjà le bon sous-type (Admin, Professeur, Etudiant).
     */
    public Utilisateur login(String email, String motDePasse) throws SQLException {
        if (email == null || email.isBlank() || motDePasse == null || motDePasse.isBlank() ){
            return  null ;
        }
        Utilisateur u = utilisateurDAO.trouverParEmail(email.trim().toLowerCase());
        if (u == null ) return null ;

        if (!u.isActif()) {
            throw new IllegalStateException("Compte désactivé. Contactez l'administrateur.");
        }

        if (u != null && BCrypt.checkpw(motDePasse, u.getMotDePasse())) {
            utilisateurConnecte = u;
            return u;
        }
        return null;
    }
public boolean changerMotDePasse(int userId, String ancienMdp, String nouveauMdp) throws SQLException {
    Utilisateur u = utilisateurDAO.trouverParId(userId);
    if (u == null) return false;

    if (!BCrypt.checkpw(ancienMdp, u.getMotDePasse()))
        throw new IllegalArgumentException("Ancien mot de passe incorrect.");

    if (nouveauMdp.length() < 8)
        throw new IllegalArgumentException("Mot de passe trop court (8 caractères minimum).");

    String nouveauHash = BCrypt.hashpw(nouveauMdp, BCrypt.gensalt());
    utilisateurDAO.modifierMotDePasse(userId, nouveauHash); // ✅ méthode déjà dans ton DAO
    return true;
}

    public String hacherMotDePasse(String clair) {
        return BCrypt.hashpw(clair, BCrypt.gensalt());
    }

    public void deconnecter() { utilisateurConnecte = null; }
    public static Utilisateur getUtilisateurConnecte() { return utilisateurConnecte; }
    public static boolean estConnecte() { return utilisateurConnecte != null; }
}