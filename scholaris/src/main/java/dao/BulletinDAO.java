package dao;

import model.*;
import util.DbConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO pour la table bulletin.
 */
public class BulletinDAO implements IDao<Bulletin> {

    private Connection  conn;
    private EtudiantDAO etudiantDAO;
    private ClasseDAO   classeDAO;

    public BulletinDAO() throws SQLException {
        this.conn        = DbConnection.getInstance();
        this.etudiantDAO = new EtudiantDAO();
        this.classeDAO   = new ClasseDAO();
    }

    @Override
    public void ajouter(Bulletin b) throws SQLException {
        String sql = """
            INSERT INTO bulletin
              (etudiant_id, classe_id, periode, annee_scolaire,
               moyenne_generale, rang, appreciation, fichier_pdf)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            """;
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, b.getEtudiant().getId());
            ps.setInt(2, b.getClasse().getId());
            ps.setString(3, b.getPeriode());
            ps.setInt(4, b.getAnneeScolaire());
            ps.setDouble(5, b.getMoyenneGenerale());
            ps.setInt(6, b.getRang());
            ps.setString(7, b.getAppreciation());
            ps.setString(8, b.getFichierPdf());
            ps.executeUpdate();
            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) b.setId(rs.getInt(1));
        }
    }

    @Override
    public void modifier(Bulletin b) throws SQLException {
        String sql = """
            UPDATE bulletin
            SET moyenne_generale = ?, rang = ?, appreciation = ?, fichier_pdf = ?
            WHERE id = ?
            """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDouble(1, b.getMoyenneGenerale());
            ps.setInt(2, b.getRang());
            ps.setString(3, b.getAppreciation());
            ps.setString(4, b.getFichierPdf());
            ps.setInt(5, b.getId());
            ps.executeUpdate();
        }
    }

    @Override
    public void supprimer(int id) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "DELETE FROM bulletin WHERE id = ?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    @Override
    public Bulletin trouverParId(int id) throws SQLException {
        String sql = "SELECT * FROM bulletin WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapRow(rs);
        }
        return null;
    }

    @Override
    public List<Bulletin> trouverTous() throws SQLException {
        List<Bulletin> liste = new ArrayList<>();
        String sql = "SELECT * FROM bulletin ORDER BY genere_le DESC";
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) liste.add(mapRow(rs));
        }
        return liste;
    }

    /** Retourne le bulletin d'un étudiant pour une période donnée. */
    public Bulletin trouverParEtudiantEtPeriode(int etudiantId,
                                                  String periode,
                                                  int annee) throws SQLException {
        String sql = """
            SELECT * FROM bulletin
            WHERE etudiant_id = ? AND periode = ? AND annee_scolaire = ?
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

    /** Retourne tous les bulletins d'une classe pour une période. */
    public List<Bulletin> trouverParClasse(int classeId,
                                            String periode,
                                            int annee) throws SQLException {
        List<Bulletin> liste = new ArrayList<>();
        String sql = """
            SELECT * FROM bulletin
            WHERE classe_id = ? AND periode = ? AND annee_scolaire = ?
            ORDER BY rang ASC
            """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, classeId);
            ps.setString(2, periode);
            ps.setInt(3, annee);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) liste.add(mapRow(rs));
        }
        return liste;
    }

    private Bulletin mapRow(ResultSet rs) throws SQLException {
        Bulletin b = new Bulletin();
        b.setId(rs.getInt("id"));
        b.setPeriode(rs.getString("periode"));
        b.setAnneeScolaire(rs.getInt("annee_scolaire"));
        b.setMoyenneGenerale(rs.getDouble("moyenne_generale"));
        b.setRang(rs.getInt("rang"));
        b.setAppreciation(rs.getString("appreciation"));
        b.setFichierPdf(rs.getString("fichier_pdf"));

        Timestamp ts = rs.getTimestamp("genere_le");
        if (ts != null) b.setGenereLe(ts.toLocalDateTime());

        b.setEtudiant(etudiantDAO.trouverParId(rs.getInt("etudiant_id")));
        b.setClasse(classeDAO.trouverParId(rs.getInt("classe_id")));
        return b;
    }
}
