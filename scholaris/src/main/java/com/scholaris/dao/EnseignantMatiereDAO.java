package com.scholaris.dao;

import com.scholaris.model.*;
import com.scholaris.util.DbConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO pour la table de liaison enseignant_matiere.
 */
public class EnseignantMatiereDAO implements IDao<EnseignantMatiere> {

    private Connection conn;
    private ProfesseurDAO professeurDAO;
    private MatiereDAO    matiereDAO;
    private ClasseDAO     classeDAO;

    public EnseignantMatiereDAO() throws SQLException {
        this.conn = DbConnection.getInstance();
        this.professeurDAO = new ProfesseurDAO();
        this.matiereDAO = new MatiereDAO();
        this.classeDAO = new ClasseDAO();
    }

    @Override
    public void ajouter(EnseignantMatiere em) throws SQLException {
        String sql = "INSERT INTO enseignant_matiere (utilisateur_id, matiere_id, classe_id) VALUES (?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, em.getProfesseur().getId());
            ps.setInt(2, em.getMatiere().getId());
            ps.setInt(3, em.getClasse().getId());
            ps.executeUpdate();
            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) em.setId(rs.getInt(1));
        }
    }

    @Override
    public void modifier(EnseignantMatiere em) throws SQLException {
        String sql = "UPDATE enseignant_matiere SET utilisateur_id = ?, matiere_id = ?, classe_id = ? WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, em.getProfesseur().getId());
            ps.setInt(2, em.getMatiere().getId());
            ps.setInt(3, em.getClasse().getId());
            ps.setInt(4, em.getId());
            ps.executeUpdate();
        }
    }

    @Override
    public void supprimer(int id) throws SQLException {
        String sql = "DELETE FROM enseignant_matiere WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    @Override
    public EnseignantMatiere trouverParId(int id) throws SQLException {
        String sql = "SELECT * FROM enseignant_matiere WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapRow(rs);
        }
        return null;
    }

    @Override
    public List<EnseignantMatiere> trouverTous() throws SQLException {
        List<EnseignantMatiere> liste = new ArrayList<>();
        String sql = "SELECT * FROM enseignant_matiere";
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) liste.add(mapRow(rs));
        }
        return liste;
    }

    /** Retourne toutes les affectations d'un professeur. */
    public List<EnseignantMatiere> trouverParProfesseur(int profId) throws SQLException {
        List<EnseignantMatiere> liste = new ArrayList<>();
        String sql = "SELECT * FROM enseignant_matiere WHERE utilisateur_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, profId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) liste.add(mapRow(rs));
        }
        return liste;
    }

    /** Retourne toutes les affectations pour une classe. */
    public List<EnseignantMatiere> trouverParClasse(int classeId) throws SQLException {
        List<EnseignantMatiere> liste = new ArrayList<>();
        String sql = "SELECT * FROM enseignant_matiere WHERE classe_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, classeId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) liste.add(mapRow(rs));
        }
        return liste;
    }

    private EnseignantMatiere mapRow(ResultSet rs) throws SQLException {
        EnseignantMatiere em = new EnseignantMatiere();
        em.setId(rs.getInt("id"));
        
        int profId = rs.getInt("utilisateur_id");
        em.setUtilisateurId(profId);
        em.setProfesseur(professeurDAO.trouverParId(profId));
        
        int matiereId = rs.getInt("matiere_id");
        em.setMatiereId(matiereId);
        em.setMatiere(matiereDAO.trouverParId(matiereId));
        
        int classeId = rs.getInt("classe_id");
        em.setClasseId(classeId);
        em.setClasse(classeDAO.trouverParId(classeId));
        
        return em;
    }
}
