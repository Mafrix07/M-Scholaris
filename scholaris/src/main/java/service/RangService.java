package service;

import dao.EtudiantDAO;
import dao.MatiereDAO;
import dao.RangDAO;
import model.Etudiant;
import model.Matiere;
import model.Rang;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Service pour le calcul et l'enregistrement des rangs.
 * Gère les ex-aequo selon la règle standard (1, 1, 3, 4...).
 */
public class RangService {

    private RangDAO        rangDAO;
    private EtudiantDAO    etudiantDAO;
    private MoyenneService moyenneService;
    private MatiereDAO     matiereDAO;

    public RangService() throws SQLException {
        this.rangDAO        = new RangDAO();
        this.etudiantDAO    = new EtudiantDAO();
        this.moyenneService = new MoyenneService();
        this.matiereDAO     = new MatiereDAO();
    }

    /**
     * Calcule les rangs généraux d'une classe.
     */
    public void calculerRangsGeneraux(int classeId, String periode, int annee) throws SQLException {
        List<Etudiant> etudiants = etudiantDAO.trouverParClasse(classeId);
        List<Score> scores = new ArrayList<>();

        for (Etudiant e : etudiants) {
            double moy = moyenneService.calculerMoyenneGenerale(e.getId(), periode, annee);
            scores.add(new Score(e, moy));
        }

        attribuerEtSauvegarderRangs(scores, null, periode, annee, classeId);
    }

    /**
     * Calcule les rangs par matière pour une classe.
     */
    public void calculerRangsParMatiere(int classeId, int matiereId, String periode, int annee) throws SQLException {
        List<Etudiant> etudiants = etudiantDAO.trouverParClasse(classeId);
        List<Score> scores = new ArrayList<>();
        Matiere matiere = matiereDAO.trouverParId(matiereId);

        for (Etudiant e : etudiants) {
            double moy = moyenneService.calculerMoyenneMatiere(e.getId(), matiereId, periode, annee);
            scores.add(new Score(e, moy));
        }

        attribuerEtSauvegarderRangs(scores, matiere, periode, annee, classeId);
    }

    /**
     * Logique commune pour attribuer les rangs (avec gestion ex-aequo) et sauvegarder.
     */
    private void attribuerEtSauvegarderRangs(List<Score> scores, Matiere matiere, String periode, int annee, int classeId) throws SQLException {
        // Trier par score décroissant
        scores.sort((s1, s2) -> Double.compare(s2.valeur, s1.valeur));

        int effectif = scores.size();
        int rangActuel = 1;

        // Nettoyer les anciens rangs si nécessaire (optionnel selon la stratégie)
        if (matiere == null) {
            rangDAO.supprimerParClasseEtPeriode(classeId, periode, annee);
        }

        for (int i = 0; i < scores.size(); i++) {
            Score s = scores.get(i);
            
            // Gestion ex-aequo
            if (i > 0 && s.valeur < scores.get(i - 1).valeur) {
                rangActuel = i + 1;
            }

            Rang r = new Rang(s.etudiant, s.etudiant.getClasse(), matiere, periode, annee, rangActuel, effectif);
            
            // On peut chercher si le rang existe déjà pour mettre à jour ou ajouter
            Rang existant;
            if (matiere == null) {
                existant = rangDAO.trouverRangGeneral(s.etudiant.getId(), periode, annee);
            } else {
                existant = rangDAO.trouverRangMatiere(s.etudiant.getId(), matiere.getId(), periode, annee);
            }

            if (existant != null) {
                existant.setRang(rangActuel);
                existant.setEffectif(effectif);
                rangDAO.modifier(existant);
            } else {
                rangDAO.ajouter(r);
            }
        }
    }

    private static class Score {
        Etudiant etudiant;
        double   valeur;

        Score(Etudiant e, double v) {
            this.etudiant = e;
            this.valeur = v;
        }
    }
}
