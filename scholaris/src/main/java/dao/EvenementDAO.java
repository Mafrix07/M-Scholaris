package dao;

import model.*;
import util.DbConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO pour la table evenement (calendrier scolaire).
 */
public class EvenementDAO implements IDao<Evenement> {

    private Connection conn;
    private MatiereDAO matiereDAO;
    private ClasseDAO  classeDAO;

    public EvenementDAO() throws SQLException {
        this.conn       = DbConnection.getInstance();
        this.matiereDAO = new MatiereDAO();
        this.classeDAO  = new ClasseDAO();
    }

    @Override
    public void ajouter(Evenement ev) throws SQLException {
        String sql = """
            INSERT INTO evenement
              (titre, type_eval_id, matiere_id, classe_id,
               date_event, heure_debut, heure_fin, description, cree_par)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, ev.getTitre());

            if (ev.getTypeEvaluation() != null)
                ps.setInt(2, ev.getTypeEvaluation().getId());
            else ps.setNull(2, Types.INTEGER);

            if (ev.getMatiere() != null)
                ps.setInt(3, ev.getMatiere().getId());
            else ps.setNull(3, Types.INTEGER);

            if (ev.getClasse() != null)
                ps.setInt(4, ev.getClasse().getId());
            else ps.setNull(4, Types.INTEGER);

            ps.setDate(5, Date.valueOf(ev.getDateEvent()));

            if (ev.getHeureDebut() != null)
                ps.setTime(6, Time.valueOf(ev.getHeureDebut()));
            else ps.setNull(6, Types.TIME);

            if (ev.getHeureFin() != null)
                ps.setTime(7, Time.valueOf(ev.getHeureFin()));
            else ps.setNull(7, Types.TIME);

            ps.setString(8, ev.getDescription());

            if (ev.getCreePar() != 0)
                ps.setInt(9, ev.getCreePar());
            else ps.setNull(9, Types.INTEGER);

            ps.executeUpdate();
            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) ev.setId(rs.getInt(1));
        }
    }

    @Override
    public void modifier(Evenement ev) throws SQLException {
        String sql = """
            UPDATE evenement
            SET titre = ?, date_event = ?, heure_debut = ?,
                heure_fin = ?, description = ?
            WHERE id = ?
            """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, ev.getTitre());
            ps.setDate(2, Date.valueOf(ev.getDateEvent()));
            ps.setTime(3, ev.getHeureDebut() != null ? Time.valueOf(ev.getHeureDebut()) : null);
            ps.setTime(4, ev.getHeureFin()   != null ? Time.valueOf(ev.getHeureFin())   : null);
            ps.setString(5, ev.getDescription());
            ps.setInt(6, ev.getId());
            ps.executeUpdate();
        }
    }

    @Override
    public void supprimer(int id) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "DELETE FROM evenement WHERE id = ?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    @Override
    public Evenement trouverParId(int id) throws SQLException {
        String sql = "SELECT * FROM evenement WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapRow(rs);
        }
        return null;
    }

    @Override
    public List<Evenement> trouverTous() throws SQLException {
        List<Evenement> liste = new ArrayList<>();
        String sql = "SELECT * FROM evenement ORDER BY date_event, heure_debut";
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) liste.add(mapRow(rs));
        }
        return liste;
    }

    /** Retourne les prochains événements d'une classe (date >= aujourd'hui). */
    public List<Evenement> trouverProchains(int classeId) throws SQLException {
        List<Evenement> liste = new ArrayList<>();
        String sql = """
            SELECT * FROM evenement
            WHERE classe_id = ? AND date_event >= CURRENT_DATE
            ORDER BY date_event, heure_debut
            """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, classeId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) liste.add(mapRow(rs));
        }
        return liste;
    }

    private Evenement mapRow(ResultSet rs) throws SQLException {
        Evenement ev = new Evenement();
        ev.setId(rs.getInt("id"));
        ev.setTitre(rs.getString("titre"));
        ev.setDescription(rs.getString("description"));

        Date d = rs.getDate("date_event");
        if (d != null) ev.setDateEvent(d.toLocalDate());

        Time hd = rs.getTime("heure_debut");
        if (hd != null) ev.setHeureDebut(hd.toLocalTime());

        Time hf = rs.getTime("heure_fin");
        if (hf != null) ev.setHeureFin(hf.toLocalTime());

        int matiereId = rs.getInt("matiere_id");
        if (!rs.wasNull()) ev.setMatiere(matiereDAO.trouverParId(matiereId));

        int classeId = rs.getInt("classe_id");
        if (!rs.wasNull()) ev.setClasse(classeDAO.trouverParId(classeId));

        return ev;
    }
}
