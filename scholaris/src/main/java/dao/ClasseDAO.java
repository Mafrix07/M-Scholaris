package dao;

import model.Classe;
import util.DbConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO pour la table classe.
 */
public class ClasseDAO implements IDao<Classe> {

    private Connection conn;

    public ClasseDAO() throws SQLException {
        this.conn = DbConnection.getInstance();
    }

    @Override
    public void ajouter(Classe c) throws SQLException {
        String sql = "INSERT INTO classe (nom, niveau, annee_scolaire) VALUES (?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, c.getNom());
            ps.setString(2, c.getNiveau());
            ps.setInt(3, c.getAnneeScolaire());
            ps.executeUpdate();
            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) c.setId(rs.getInt(1));
        }
    }

    @Override
    public void modifier(Classe c) throws SQLException {
        String sql = "UPDATE classe SET nom = ?, niveau = ?, annee_scolaire = ? WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, c.getNom());
            ps.setString(2, c.getNiveau());
            ps.setInt(3, c.getAnneeScolaire());
            ps.setInt(4, c.getId());
            ps.executeUpdate();
        }
    }

    @Override
    public void supprimer(int id) throws SQLException {
        String sql = "DELETE FROM classe WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    @Override
    public Classe trouverParId(int id) throws SQLException {
        String sql = "SELECT * FROM classe WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapRow(rs);
        }
        return null;
    }

    @Override
    public List<Classe> trouverTous() throws SQLException {
        List<Classe> liste = new ArrayList<>();
        String sql = "SELECT * FROM classe ORDER BY annee_scolaire DESC, nom";
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) liste.add(mapRow(rs));
        }
        return liste;
    }

    /** Retourne les classes d'une année scolaire donnée. */
    public List<Classe> trouverParAnnee(int annee) throws SQLException {
        List<Classe> liste = new ArrayList<>();
        String sql = "SELECT * FROM classe WHERE annee_scolaire = ? ORDER BY nom";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, annee);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) liste.add(mapRow(rs));
        }
        return liste;
    }

    private Classe mapRow(ResultSet rs) throws SQLException {
        Classe c = new Classe();
        c.setId(rs.getInt("id"));
        c.setNom(rs.getString("nom"));
        c.setNiveau(rs.getString("niveau"));
        c.setAnneeScolaire(rs.getInt("annee_scolaire"));
        return c;
    }
}
