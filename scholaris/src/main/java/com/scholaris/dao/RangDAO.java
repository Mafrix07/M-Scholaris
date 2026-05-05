package com.scholaris.dao;

import com.scholaris.model.*;
import com.scholaris.util.DbConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO pour la table rang.
 */
public class RangDAO implements IDao<Rang> {

    private Connection conn;
    private EtudiantDAO etudiantDAO;
    private ClasseDAO   classeDAO;
    private MatiereDAO  matiereDAO;

    public RangDAO() throws SQLException {
        this.conn = DbConnection.getInstance();
        this.etudiantDAO = new EtudiantDAO();
        this.classeDAO = new ClasseDAO();
        this.matiereDAO = new MatiereDAO();
    }

    @Override
    public void ajouter(Rang r) throws SQLException {
        String sql = """
            INSERT INTO rang (etudiant_id, classe_id, matiere_id, periode, annee_scolaire, rang, effectif)
            VALUES (?, ?, ?, ?, ?, ?, ?)
            """;
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, r.getEtudiant().getId());
            ps.setInt(2, r.getClasse().getId());
            if (r.getMatiere() != null) {
                ps.setInt(3, r.getMatiere().getId());
            } else {
                ps.setNull(3, Types.INTEGER);
            }
            ps.setString(4, r.getPeriode());
            ps.setInt(5, r.getAnneeScolaire());
            ps.setInt(6, r.getRang());
            ps.setInt(7, r.getEffectif());
            ps.executeUpdate();
            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) r.setId(rs.getInt(1));
        }
    }

    @Override
    public void modifier(Rang r) throws SQLException {
        String sql = """
            UPDATE rang SET rang = ?, effectif = ?
            WHERE id = ?
            """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, r.getRang());
            ps.setInt(2, r.getEffectif());
            ps.setInt(3, r.getId());
            ps.executeUpdate();
        }
    }

    @Override
    public void supprimer(int id) throws SQLException {
        String sql = "DELETE FROM rang WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    @Override
    public Rang trouverParId(int id) throws SQLException {
        String sql = "SELECT * FROM rang WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapRow(rs);
        }
        return null;
    }

    @Override
    public List<Rang> trouverTous() throws SQLException {
        List<Rang> liste = new ArrayList<>();
        String sql = "SELECT * FROM rang ORDER BY annee_scolaire DESC, periode, rang";
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) liste.add(mapRow(rs));
        }
        return liste;
    }

    /** Trouve le rang général d'un étudiant pour une période donnée. */
    public Rang trouverRangGeneral(int etudiantId, String periode, int annee) throws SQLException {
        String sql = """
            SELECT * FROM rang
            WHERE etudiant_id = ? AND periode = ? AND annee_scolaire = ? AND matiere_id IS NULL
            """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, etudiantId);
            ps.setString(2, periode);
            ps.setInt(3, annee);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapRow(rs);
        }
        return null;
    }

    /** Trouve le rang d'un étudiant dans une matière pour une période donnée. */
    public Rang trouverRangMatiere(int etudiantId, int matiereId, String periode, int annee) throws SQLException {
        String sql = """
            SELECT * FROM rang
            WHERE etudiant_id = ? AND matiere_id = ? AND periode = ? AND annee_scolaire = ?
            """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, etudiantId);
            ps.setInt(2, matiereId);
            ps.setString(3, periode);
            ps.setInt(4, annee);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapRow(rs);
        }
        return null;
    }

    /** Supprime tous les rangs d'une classe pour une période (utile avant recalcul). */
    public void supprimerParClasseEtPeriode(int classeId, String periode, int annee) throws SQLException {
        String sql = "DELETE FROM rang WHERE classe_id = ? AND periode = ? AND annee_scolaire = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, classeId);
            ps.setString(2, periode);
            ps.setInt(3, annee);
            ps.executeUpdate();
        }
    }

    private Rang mapRow(ResultSet rs) throws SQLException {
        Rang r = new Rang();
        r.setId(rs.getInt("id"));
        r.setPeriode(rs.getString("periode"));
        r.setAnneeScolaire(rs.getInt("annee_scolaire"));
        r.setRang(rs.getInt("rang"));
        r.setEffectif(rs.getInt("effectif"));

        r.setEtudiant(etudiantDAO.trouverParId(rs.getInt("etudiant_id")));
        r.setClasse(classeDAO.trouverParId(rs.getInt("classe_id")));

        int matiereId = rs.getInt("matiere_id");
        if (!rs.wasNull()) {
            r.setMatiere(matiereDAO.trouverParId(matiereId));
        }

        return r;
    }
}
