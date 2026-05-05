package com.scholaris.dao;

import com.scholaris.model.Note;
import com.scholaris.util.DbConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO pour la table note.
 * Adapté à Note.java qui utilise des int (etudiantId, matiereId...)
 * et non des objets complets pour éviter les chargements inutiles.
 */
public class NoteDAO implements IDao<Note> {

    private Connection conn;

    public NoteDAO() throws SQLException {
        this.conn = DbConnection.getInstance();
    }

    // ── INSERT ────────────────────────────────────────────
    @Override
    public void ajouter(Note n) throws SQLException {
        String sql = """
            INSERT INTO note
              (etudiant_id, matiere_id, classe_id, type_eval_id,
               evenement_id, valeur, periode, annee_scolaire, modifie_par)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;
        try (PreparedStatement ps = conn.prepareStatement(
                sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, n.getEtudiantId());
            ps.setInt(2, n.getMatiereId());
            ps.setInt(3, n.getClasseId());

            // type_eval_id : 0 = non renseigné → NULL en base
            if (n.getTypeEvalId() != 0)
                ps.setInt(4, n.getTypeEvalId());
            else
                ps.setNull(4, Types.INTEGER);

            // evenement_id : 0 = non renseigné → NULL en base
            if (n.getEvenementId() != 0)
                ps.setInt(5, n.getEvenementId());
            else
                ps.setNull(5, Types.INTEGER);

            ps.setDouble(6, n.getValeur());
            ps.setString(7, n.getPeriode());
            ps.setInt(8, n.getAnneeScolaire());

            // modifie_par : 0 = non renseigné → NULL en base
            if (n.getModifiePar() != 0)
                ps.setInt(9, n.getModifiePar());
            else
                ps.setNull(9, Types.INTEGER);

            ps.executeUpdate();

            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) n.setId(rs.getInt(1));
        }
    }

    // ── UPDATE ────────────────────────────────────────────
    @Override
    public void modifier(Note n) throws SQLException {
        String sql = """
            UPDATE note
            SET valeur = ?, type_eval_id = ?,
                modifie_par = ?, modifie_le = CURRENT_TIMESTAMP
            WHERE id = ?
            """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDouble(1, n.getValeur());

            if (n.getTypeEvalId() != 0)
                ps.setInt(2, n.getTypeEvalId());
            else
                ps.setNull(2, Types.INTEGER);

            if (n.getModifiePar() != 0)
                ps.setInt(3, n.getModifiePar());
            else
                ps.setNull(3, Types.INTEGER);

            ps.setInt(4, n.getId());
            ps.executeUpdate();
        }
    }

    // ── DELETE ────────────────────────────────────────────
    @Override
    public void supprimer(int id) throws SQLException {
        String sql = "DELETE FROM note WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    // ── SELECT BY ID ──────────────────────────────────────
    @Override
    public Note trouverParId(int id) throws SQLException {
        String sql = "SELECT * FROM note WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapRow(rs);
        }
        return null;
    }

    // ── SELECT ALL ────────────────────────────────────────
    @Override
    public List<Note> trouverTous() throws SQLException {
        List<Note> liste = new ArrayList<>();
        String sql = "SELECT * FROM note ORDER BY modifie_le DESC";
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) liste.add(mapRow(rs));
        }
        return liste;
    }

    // ── REQUÊTES SPÉCIFIQUES ──────────────────────────────

    /** Notes d'un étudiant pour une période et année donnée. */
    public List<Note> trouverParEtudiant(int etudiantId,
                                          String periode,
                                          int annee) throws SQLException {
        List<Note> liste = new ArrayList<>();
        String sql = """
            SELECT * FROM note
            WHERE etudiant_id = ? AND periode = ? AND annee_scolaire = ?
            ORDER BY matiere_id
            """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, etudiantId);
            ps.setString(2, periode);
            ps.setInt(3, annee);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) liste.add(mapRow(rs));
        }
        return liste;
    }

    /** Notes d'une classe pour une matière et période données. */
    public List<Note> trouverParClasseEtMatiere(int classeId,
                                                  int matiereId,
                                                  String periode,
                                                  int annee) throws SQLException {
        List<Note> liste = new ArrayList<>();
        String sql = """
            SELECT * FROM note
            WHERE classe_id = ? AND matiere_id = ?
              AND periode = ? AND annee_scolaire = ?
            """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, classeId);
            ps.setInt(2, matiereId);
            ps.setString(3, periode);
            ps.setInt(4, annee);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) liste.add(mapRow(rs));
        }
        return liste;
    }

    /**
     * Retourne [{etudiant_id, moyenne_generale}] triés par moyenne DESC.
     * Utilisé par RangService pour calculer les rangs généraux.
     */
    public List<double[]> getMoyennesGeneralesClasse(int classeId,
                                                      String periode,
                                                      int annee) throws SQLException {
        List<double[]> result = new ArrayList<>();
        String sql = """
            SELECT n.etudiant_id,
                   SUM(n.valeur * m.coefficient) / NULLIF(SUM(m.coefficient), 0)
                       AS moyenne_generale
            FROM note n
            JOIN matiere m ON n.matiere_id = m.id
            WHERE n.classe_id = ? AND n.periode = ? AND n.annee_scolaire = ?
            GROUP BY n.etudiant_id
            ORDER BY moyenne_generale DESC
            """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, classeId);
            ps.setString(2, periode);
            ps.setInt(3, annee);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                result.add(new double[]{
                    rs.getInt("etudiant_id"),
                    rs.getDouble("moyenne_generale")
                });
            }
        }
        return result;
    }

    /**
     * Retourne [{etudiant_id, moyenne_matiere}] triés par moyenne DESC.
     * Utilisé par RangService pour calculer les rangs par matière.
     */
    public List<double[]> getMoyennesParMatiere(int classeId,
                                                 int matiereId,
                                                 String periode,
                                                 int annee) throws SQLException {
        List<double[]> result = new ArrayList<>();
        String sql = """
            SELECT etudiant_id, AVG(valeur) AS moyenne_matiere
            FROM note
            WHERE classe_id = ? AND matiere_id = ?
              AND periode = ? AND annee_scolaire = ?
            GROUP BY etudiant_id
            ORDER BY moyenne_matiere DESC
            """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, classeId);
            ps.setInt(2, matiereId);
            ps.setString(3, periode);
            ps.setInt(4, annee);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                result.add(new double[]{
                    rs.getInt("etudiant_id"),
                    rs.getDouble("moyenne_matiere")
                });
            }
        }
        return result;
    }

    // ── MAPPING ResultSet → Note ──────────────────────────
    private Note mapRow(ResultSet rs) throws SQLException {
        Note n = new Note();
        n.setId(rs.getInt("id"));
        n.setEtudiantId(rs.getInt("etudiant_id"));
        n.setMatiereId(rs.getInt("matiere_id"));
        n.setClasseId(rs.getInt("classe_id"));
        n.setValeur(rs.getDouble("valeur"));
        n.setPeriode(rs.getString("periode"));
        n.setAnneeScolaire(rs.getInt("annee_scolaire"));

        // Champs nullable : getInt retourne 0 si NULL → on garde 0
        n.setTypeEvalId(rs.getInt("type_eval_id"));
        n.setEvenementId(rs.getInt("evenement_id"));
        n.setModifiePar(rs.getInt("modifie_par"));

        Timestamp ts = rs.getTimestamp("modifie_le");
        if (ts != null) n.setModifieLe(ts.toLocalDateTime());

        return n;
    }
}
