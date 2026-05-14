package com.scholaris.dao;

import com.scholaris.model.*;
import com.scholaris.util.*;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EtudiantDAO implements IDao<Etudiant> {

    private Connection conn;
    private ClasseDAO  classeDAO;

    public EtudiantDAO() throws SQLException {
        this.conn      = DbConnection.getInstance();
        this.classeDAO = new ClasseDAO();
    }

    // ── INSERT ────────────────────────────────────────────
    @Override
    public void ajouter(Etudiant e) throws SQLException {
        conn.setAutoCommit(false); // transaction
        try {
            // 1. Insérer dans utilisateur
            String sqlU = """
                INSERT INTO utilisateur (nom, prenom, email, mot_de_passe, role)
                VALUES (?, ?, ?, ?, 'etudiant')
                """;
            int utilisateurId;
            try (PreparedStatement ps = conn.prepareStatement(sqlU, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, e.getNom());
                ps.setString(2, e.getPrenom());
                ps.setString(3, e.getEmail());
                ps.setString(4, e.getMotDePasse());
                ps.executeUpdate();
                ResultSet rs = ps.getGeneratedKeys();
                rs.next();
                utilisateurId = rs.getInt(1);
                e.setId(utilisateurId);
            }

            // 2. Insérer dans etudiant
            String sqlE = """
                INSERT INTO etudiant (utilisateur_id, matricule, date_naissance, classe_id)
                VALUES (?, ?, ?, ?)
                """;
            try (PreparedStatement ps = conn.prepareStatement(sqlE)) {
                ps.setInt(1, utilisateurId);
                ps.setString(2, e.getMatricule());
                ps.setDate(3, e.getDateNaissance() != null
                        ? Date.valueOf(e.getDateNaissance()) : null);
                ps.setInt(4, e.getClasse().getId());
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

    // ── UPDATE ────────────────────────────────────────────
    @Override
    public void modifier(Etudiant e) throws SQLException {
        conn.setAutoCommit(false);
        try {
            // Mettre à jour utilisateur
            String sqlU = """
                UPDATE utilisateur SET nom = ?, prenom = ?, email = ? WHERE id = ?
                """;
            try (PreparedStatement ps = conn.prepareStatement(sqlU)) {
                ps.setString(1, e.getNom());
                ps.setString(2, e.getPrenom());
                ps.setString(3, e.getEmail());
                ps.setInt(4, e.getId());
                ps.executeUpdate();
            }

            // Mettre à jour etudiant
            String sqlE = """
                UPDATE etudiant SET matricule = ?, date_naissance = ?, classe_id = ?
                WHERE utilisateur_id = ?
                """;
            try (PreparedStatement ps = conn.prepareStatement(sqlE)) {
                ps.setString(1, e.getMatricule());
                ps.setDate(2, e.getDateNaissance() != null
                        ? Date.valueOf(e.getDateNaissance()) : null);
                ps.setInt(3, e.getClasse().getId());
                ps.setInt(4, e.getId());
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

    // ── DELETE ────────────────────────────────────────────
    @Override
    public void supprimer(int id) throws SQLException {
        // ON DELETE CASCADE supprime etudiant automatiquement
        String sql = "DELETE FROM utilisateur WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    // ── SELECT BY ID ──────────────────────────────────────
    @Override
    public Etudiant trouverParId(int id) throws SQLException {
        String sql = """
            SELECT u.*, e.matricule, e.date_naissance, e.classe_id
            FROM utilisateur u
            JOIN etudiant e ON e.utilisateur_id = u.id
            WHERE u.id = ?
            """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapRow(rs);
        }
        return null;
    }

    // ── SELECT ALL ────────────────────────────────────────
    @Override
    public List<Etudiant> trouverTous() throws SQLException {
        List<Etudiant> liste = new ArrayList<>();
        String sql = """
            SELECT u.*, e.matricule, e.date_naissance, e.classe_id
            FROM utilisateur u
            JOIN etudiant e ON e.utilisateur_id = u.id
            WHERE u.actif = TRUE
            ORDER BY u.nom, u.prenom
            """;
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) liste.add(mapRow(rs));
        }
        return liste;
    }

    // ── REQUÊTES SPÉCIFIQUES ──────────────────────────────

    /** Génère un matricule automatique au format SCH-ANNEE-XXXXX. */
    public String genererMatricule() throws SQLException {
        String sql = "SELECT COUNT(*) FROM etudiant";
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            rs.next();
            int count = rs.getInt(1) + 1;
            int annee = java.time.LocalDate.now().getYear();
            return String.format("SCH-%d-%05d", annee, count);
        }
    }

    /** Trouve une classe disponible selon le niveau et la série choisis. */
    public Classe trouverClasseDisponible(String niveau, String serie) throws SQLException {
        // Le nom de la classe contient souvent le niveau et la série (ex: 'Terminale D')
        String pattern = niveau + (serie != null && !serie.isEmpty() ? " " + serie : "");
        int anneeScolaire = java.time.LocalDate.now().getYear();

        String sql = """
            SELECT c.id, c.nom, c.niveau, c.annee_scolaire,
                   COUNT(e.id) AS effectif_actuel
            FROM classe c
            LEFT JOIN etudiant e ON e.classe_id = c.id
            WHERE c.nom LIKE ?
              AND c.annee_scolaire = ?
            GROUP BY c.id, c.nom, c.niveau, c.annee_scolaire
            HAVING COUNT(e.id) < 50
            ORDER BY RANDOM()
            LIMIT 1
            """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, "%" + pattern + "%");
            ps.setInt(2, anneeScolaire);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                Classe c = new Classe();
                c.setId(rs.getInt("id"));
                c.setNom(rs.getString("nom"));
                c.setNiveau(rs.getString("niveau"));
                c.setAnneeScolaire(rs.getInt("annee_scolaire"));
                return c;
            }
        }
        return null;
    }

    /** Retourne tous les étudiants d'une classe. */
    public List<Etudiant> trouverParClasse(int classeId) throws SQLException {
        List<Etudiant> liste = new ArrayList<>();
        String sql = """
            SELECT u.*, e.matricule, e.date_naissance, e.classe_id
            FROM utilisateur u
            JOIN etudiant e ON e.utilisateur_id = u.id
            WHERE e.classe_id = ? AND u.actif = TRUE
            ORDER BY u.nom, u.prenom
            """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, classeId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) liste.add(mapRow(rs));
        }
        return liste;
    }

    /** Recherche par nom, prénom ou matricule. */
    public List<Etudiant> rechercher(String terme) throws SQLException {
        List<Etudiant> liste = new ArrayList<>();
        String sql = """
            SELECT u.*, e.matricule, e.date_naissance, e.classe_id
            FROM utilisateur u
            JOIN etudiant e ON e.utilisateur_id = u.id
            WHERE u.actif = TRUE AND (
                LOWER(u.nom)      LIKE LOWER(?) OR
                LOWER(u.prenom)   LIKE LOWER(?) OR
                LOWER(e.matricule) LIKE LOWER(?)
            )
            ORDER BY u.nom, u.prenom
            """;
        String pattern = "%" + terme + "%";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, pattern);
            ps.setString(2, pattern);
            ps.setString(3, pattern);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) liste.add(mapRow(rs));
        }
        return liste;
    }

    /** Trouve un étudiant par son matricule. */
    public Etudiant trouverParMatricule(String matricule) throws SQLException {
        String sql = """
            SELECT u.*, e.matricule, e.date_naissance, e.classe_id
            FROM utilisateur u
            JOIN etudiant e ON e.utilisateur_id = u.id
            WHERE e.matricule = ?
            """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, matricule);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapRow(rs);
        }
        return null;
    }

    // ── MAPPING ───────────────────────────────────────────
    private Etudiant mapRow(ResultSet rs) throws SQLException {
        Etudiant e = new Etudiant();
        e.setId(rs.getInt("id"));
        e.setNom(rs.getString("nom"));
        e.setPrenom(rs.getString("prenom"));
        e.setEmail(rs.getString("email"));
        e.setMotDePasse(rs.getString("mot_de_passe"));
        e.setActif(rs.getBoolean("actif"));
        e.setMatricule(rs.getString("matricule"));

        Date ddn = rs.getDate("date_naissance");
        if (ddn != null) e.setDateNaissance(ddn.toLocalDate());

        // Charger la classe associée
        int classeId = rs.getInt("classe_id");
        Classe classe = this.classeDAO.trouverParId(classeId);
        e.setClasse(classe);

        return e;
    }
}
