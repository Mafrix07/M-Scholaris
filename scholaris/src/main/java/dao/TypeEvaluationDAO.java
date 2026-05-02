package dao;

import model.TypeEvaluation;
import util.DbConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO pour la table type_evaluation.
 */
public class TypeEvaluationDAO implements IDao<TypeEvaluation> {

    private Connection conn;

    public TypeEvaluationDAO() throws SQLException {
        this.conn = DbConnection.getInstance();
    }

    @Override
    public void ajouter(TypeEvaluation te) throws SQLException {
        String sql = "INSERT INTO type_evaluation (nom, code, poids) VALUES (?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, te.getNom());
            ps.setString(2, te.getCode());
            ps.setDouble(3, te.getPoids());
            ps.executeUpdate();
            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) te.setId(rs.getInt(1));
        }
    }

    @Override
    public void modifier(TypeEvaluation te) throws SQLException {
        String sql = "UPDATE type_evaluation SET nom = ?, code = ?, poids = ? WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, te.getNom());
            ps.setString(2, te.getCode());
            ps.setDouble(3, te.getPoids());
            ps.setInt(4, te.getId());
            ps.executeUpdate();
        }
    }

    @Override
    public void supprimer(int id) throws SQLException {
        String sql = "DELETE FROM type_evaluation WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    @Override
    public TypeEvaluation trouverParId(int id) throws SQLException {
        String sql = "SELECT * FROM type_evaluation WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapRow(rs);
        }
        return null;
    }

    @Override
    public List<TypeEvaluation> trouverTous() throws SQLException {
        List<TypeEvaluation> liste = new ArrayList<>();
        String sql = "SELECT * FROM type_evaluation ORDER BY nom";
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) liste.add(mapRow(rs));
        }
        return liste;
    }

    /** Trouve un type d'évaluation par son code (ex: 'DEVOIR', 'EXAMEN'). */
    public TypeEvaluation trouverParCode(String code) throws SQLException {
        String sql = "SELECT * FROM type_evaluation WHERE code = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, code);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapRow(rs);
        }
        return null;
    }

    private TypeEvaluation mapRow(ResultSet rs) throws SQLException {
        TypeEvaluation te = new TypeEvaluation();
        te.setId(rs.getInt("id"));
        te.setNom(rs.getString("nom"));
        te.setCode(rs.getString("code"));
        te.setPoids(rs.getDouble("poids"));
        return te;
    }
}
