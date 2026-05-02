package dao;

import model.Professeur;
import util.DbConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO pour la table enseignant (stratégie tables jointes).
 */
public class ProfesseurDAO implements IDao<Professeur> {

    private Connection conn;

    public ProfesseurDAO() throws SQLException {
        this.conn = DbConnection.getInstance();
    }

    @Override
    public void ajouter(Professeur p) throws SQLException {
        conn.setAutoCommit(false);
        try {
            // 1. Insérer dans utilisateur
            String sqlU = """
                INSERT INTO utilisateur (nom, prenom, email, mot_de_passe, role)
                VALUES (?, ?, ?, ?, 'enseignant')
                """;
            try (PreparedStatement ps = conn.prepareStatement(sqlU, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, p.getNom());
                ps.setString(2, p.getPrenom());
                ps.setString(3, p.getEmail());
                ps.setString(4, p.getMotDePasse());
                ps.executeUpdate();
                ResultSet rs = ps.getGeneratedKeys();
                if (rs.next()) p.setId(rs.getInt(1));
            }

            // 2. Insérer dans enseignant
            String sqlE = """
                INSERT INTO enseignant (utilisateur_id, specialite)
                VALUES (?, ?)
                """;
            try (PreparedStatement ps = conn.prepareStatement(sqlE)) {
                ps.setInt(1, p.getId());
                ps.setString(2, p.getSpecialite());
                ps.executeUpdate();
            }
            conn.commit();
        } catch (SQLException ex) {
            conn.rollback();
            throw ex;
        } finally {
            conn.setAutoCommit(true);
        }
    }

    @Override
    public void modifier(Professeur p) throws SQLException {
        conn.setAutoCommit(false);
        try {
            // 1. Update utilisateur
            String sqlU = "UPDATE utilisateur SET nom = ?, prenom = ?, email = ? WHERE id = ?";
            try (PreparedStatement ps = conn.prepareStatement(sqlU)) {
                ps.setString(1, p.getNom());
                ps.setString(2, p.getPrenom());
                ps.setString(3, p.getEmail());
                ps.setInt(4, p.getId());
                ps.executeUpdate();
            }

            // 2. Update enseignant
            String sqlE = "UPDATE enseignant SET specialite = ? WHERE utilisateur_id = ?";
            try (PreparedStatement ps = conn.prepareStatement(sqlE)) {
                ps.setString(1, p.getSpecialite());
                ps.setInt(2, p.getId());
                ps.executeUpdate();
            }
            conn.commit();
        } catch (SQLException ex) {
            conn.rollback();
            throw ex;
        } finally {
            conn.setAutoCommit(true);
        }
    }

    @Override
    public void supprimer(int id) throws SQLException {
        // ON DELETE CASCADE devrait gérer la suppression dans 'enseignant'
        String sql = "DELETE FROM utilisateur WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    @Override
    public Professeur trouverParId(int id) throws SQLException {
        String sql = """
            SELECT u.*, e.specialite
            FROM utilisateur u
            JOIN enseignant e ON u.id = e.utilisateur_id
            WHERE u.id = ?
            """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapRow(rs);
        }
        return null;
    }

    @Override
    public List<Professeur> trouverTous() throws SQLException {
        List<Professeur> liste = new ArrayList<>();
        String sql = """
            SELECT u.*, e.specialite
            FROM utilisateur u
            JOIN enseignant e ON u.id = e.utilisateur_id
            WHERE u.actif = TRUE
            ORDER BY u.nom, u.prenom
            """;
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) liste.add(mapRow(rs));
        }
        return liste;
    }

    private Professeur mapRow(ResultSet rs) throws SQLException {
        Professeur p = new Professeur();
        p.setId(rs.getInt("id"));
        p.setNom(rs.getString("nom"));
        p.setPrenom(rs.getString("prenom"));
        p.setEmail(rs.getString("email"));
        p.setMotDePasse(rs.getString("mot_de_passe"));
        p.setRole(rs.getString("role"));
        p.setActif(rs.getBoolean("actif"));
        p.setSpecialite(rs.getString("specialite"));
        return p;
    }
}
