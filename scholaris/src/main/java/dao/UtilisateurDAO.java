package dao;

import model.*;
import util.DbConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO pour la table utilisateur.
 * Gère aussi l'authentification et la recherche par rôle.
 */
public class UtilisateurDAO implements IDao<Utilisateur> {

    private Connection conn;

    public UtilisateurDAO() throws SQLException {
        this.conn = DbConnection.getInstance();
    }

    // ── INSERT ────────────────────────────────────────────
    @Override
    public void ajouter(Utilisateur u) throws SQLException {
        String sql = """
            INSERT INTO utilisateur (nom, prenom, email, mot_de_passe, role, actif)
            VALUES (?, ?, ?, ?, ?, ?)
            """;
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, u.getNom());
            ps.setString(2, u.getPrenom());
            ps.setString(3, u.getEmail());
            ps.setString(4, u.getMotDePasse());   // déjà hashé en BCrypt
            ps.setString(5, u.getRole());
            ps.setBoolean(6, u.isActif());
            ps.executeUpdate();

            // Récupérer l'id généré
            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) u.setId(rs.getInt(1));
        }
    }

    // ── UPDATE ────────────────────────────────────────────
    @Override
    public void modifier(Utilisateur u) throws SQLException {
        String sql = """
            UPDATE utilisateur
            SET nom = ?, prenom = ?, email = ?, role = ?, actif = ?
            WHERE id = ?
            """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, u.getNom());
            ps.setString(2, u.getPrenom());
            ps.setString(3, u.getEmail());
            ps.setString(4, u.getRole());
            ps.setBoolean(5, u.isActif());
            ps.setInt(6, u.getId());
            ps.executeUpdate();
        }
    }

    // ── DELETE ────────────────────────────────────────────
    @Override
    public void supprimer(int id) throws SQLException {
        String sql = "DELETE FROM utilisateur WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    // ── SELECT BY ID ──────────────────────────────────────
    @Override
    public Utilisateur trouverParId(int id) throws SQLException {
        String sql = "SELECT * FROM utilisateur WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapRow(rs);
        }
        return null;
    }

    // ── SELECT ALL ────────────────────────────────────────
    @Override
    public List<Utilisateur> trouverTous() throws SQLException {
        List<Utilisateur> liste = new ArrayList<>();
        String sql = "SELECT * FROM utilisateur ORDER BY nom, prenom";
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) liste.add(mapRow(rs));
        }
        return liste;
    }

    // ── AUTHENTIFICATION ──────────────────────────────────

    /**
     * Trouve un utilisateur par email (pour la connexion).
     * La vérification du mot de passe BCrypt se fait dans AuthService.
     */
    public Utilisateur trouverParEmail(String email) throws SQLException {
        String sql = "SELECT * FROM utilisateur WHERE email = ? AND actif = TRUE";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapRow(rs);
        }
        return null;
    }

    /** Retourne tous les utilisateurs d'un rôle donné. */
    public List<Utilisateur> trouverParRole(String role) throws SQLException {
        List<Utilisateur> liste = new ArrayList<>();
        String sql = "SELECT * FROM utilisateur WHERE role = ? AND actif = TRUE ORDER BY nom";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, role);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) liste.add(mapRow(rs));
        }
        return liste;
    }

    /** Mise à jour du mot de passe uniquement. */
    public void modifierMotDePasse(int id, String nouveauHash) throws SQLException {
        String sql = "UPDATE utilisateur SET mot_de_passe = ? WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nouveauHash);
            ps.setInt(2, id);
            ps.executeUpdate();
        }
    }

    // ── MAPPING ResultSet → Objet ─────────────────────────
    /**
     * Convertit une ligne SQL en objet selon le rôle.
     * Retourne Admin, Professeur ou Etudiant selon le champ role.
     */
    private Utilisateur mapRow(ResultSet rs) throws SQLException {
        String role = rs.getString("role");
        Utilisateur u;

        switch (role) {
            case "admin"      -> u = new Admin();
            case "enseignant" -> u = new Professeur();
            default           -> u = new Etudiant();
        }

        u.setId(rs.getInt("id"));
        u.setNom(rs.getString("nom"));
        u.setPrenom(rs.getString("prenom"));
        u.setEmail(rs.getString("email"));
        u.setMotDePasse(rs.getString("mot_de_passe"));
        u.setRole(role);
        u.setActif(rs.getBoolean("actif"));

        Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) u.setCreatedAt(ts.toLocalDateTime());

        return u;
    }
}
