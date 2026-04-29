package dao;

import model.Matiere;
import util.DbConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO pour la table matiere.
 */
public class MatiereDAO implements IDao<Matiere> {

    private Connection conn;

    public MatiereDAO() throws SQLException {
        this.conn = DbConnection.getInstance();
    }

    @Override
    public void ajouter(Matiere m) throws SQLException {
        String sql = "INSERT INTO matiere (nom, code, coefficient) VALUES (?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, m.getNom());
            ps.setString(2, m.getCode());
            ps.setDouble(3, m.getCoefficient());
            ps.executeUpdate();
            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) m.setId(rs.getInt(1));
        }
    }

    @Override
    public void modifier(Matiere m) throws SQLException {
        String sql = "UPDATE matiere SET nom = ?, code = ?, coefficient = ? WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, m.getNom());
            ps.setString(2, m.getCode());
            ps.setDouble(3, m.getCoefficient());
            ps.setInt(4, m.getId());
            ps.executeUpdate();
        }
    }

    @Override
    public void supprimer(int id) throws SQLException {
        String sql = "DELETE FROM matiere WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    @Override
    public Matiere trouverParId(int id) throws SQLException {
        String sql = "SELECT * FROM matiere WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapRow(rs);
        }
        return null;
    }

    @Override
    public List<Matiere> trouverTous() throws SQLException {
        List<Matiere> liste = new ArrayList<>();
        String sql = "SELECT * FROM matiere ORDER BY nom";
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) liste.add(mapRow(rs));
        }
        return liste;
    }

    /** Retourne les matières enseignées dans une classe donnée. */
    public List<Matiere> trouverParClasse(int classeId) throws SQLException {
        List<Matiere> liste = new ArrayList<>();
        String sql = """
            SELECT DISTINCT m.*
            FROM matiere m
            JOIN enseignant_matiere em ON em.matiere_id = m.id
            WHERE em.classe_id = ?
            ORDER BY m.nom
            """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, classeId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) liste.add(mapRow(rs));
        }
        return liste;
    }

    private Matiere mapRow(ResultSet rs) throws SQLException {
        Matiere m = new Matiere();
        m.setId(rs.getInt("id"));
        m.setNom(rs.getString("nom"));
        m.setCode(rs.getString("code"));
        m.setCoefficient(rs.getDouble("coefficient"));
        return m;
    }
}
