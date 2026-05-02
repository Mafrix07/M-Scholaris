package service;

import dao.NoteDAO;
import dao.TypeEvaluationDAO;
import dao.MatiereDAO;
import model.Note;
import model.Matiere;
import model.TypeEvaluation;

import java.sql.SQLException;
import java.util.*;

/**
 * Service pour le calcul des moyennes.
 * Gère la pondération par type d'évaluation et par coefficient de matière.
 */
public class MoyenneService {

    private NoteDAO           noteDAO;
    private TypeEvaluationDAO typeEvalDAO;
    private MatiereDAO        matiereDAO;

    public MoyenneService() throws SQLException {
        this.noteDAO     = new NoteDAO();
        this.typeEvalDAO = new TypeEvaluationDAO();
        this.matiereDAO  = new MatiereDAO();
    }

    /**
     * Calcule la moyenne d'un étudiant dans une matière spécifique.
     * Pondération par le poids du type d'évaluation (ex: Interro vs Examen).
     */
    public double calculerMoyenneMatiere(int etudiantId, int matiereId, String periode, int annee) throws SQLException {
        List<Note> notes = noteDAO.trouverParEtudiant(etudiantId, periode, annee);
        
        double sommeNotesPonderees = 0;
        double sommePoids          = 0;
        boolean aDesNotes          = false;

        for (Note n : notes) {
            if (n.getMatiereId() == matiereId) {
                TypeEvaluation te = typeEvalDAO.trouverParId(n.getTypeEvalId());
                double poids = (te != null) ? te.getPoids() : 1.0;
                
                sommeNotesPonderees += n.getValeur() * poids;
                sommePoids          += poids;
                aDesNotes           = true;
            }
        }

        if (!aDesNotes || sommePoids == 0) return 0.0;
        
        return arrondir(sommeNotesPonderees / sommePoids);
    }

    /**
     * Calcule la moyenne générale d'un étudiant.
     * Pondération par les coefficients des matières.
     */
    public double calculerMoyenneGenerale(int etudiantId, String periode, int annee) throws SQLException {
        Map<Integer, Double> moyennesParMatiere = calculerMoyennesToutesMatieres(etudiantId, periode, annee);
        
        double sommeMoyennesCoef = 0;
        double sommeCoef         = 0;

        for (Map.Entry<Integer, Double> entry : moyennesParMatiere.entrySet()) {
            Matiere m = matiereDAO.trouverParId(entry.getKey());
            double coef = (m != null) ? m.getCoefficient() : 1.0;
            
            sommeMoyennesCoef += entry.getValue() * coef;
            sommeCoef         += coef;
        }

        if (sommeCoef == 0) return 0.0;

        return arrondir(sommeMoyennesCoef / sommeCoef);
    }

    /**
     * Récupère toutes les moyennes par matière d'un étudiant.
     */
    public Map<Integer, Double> calculerMoyennesToutesMatieres(int etudiantId, String periode, int annee) throws SQLException {
        List<Note> notes = noteDAO.trouverParEtudiant(etudiantId, periode, annee);
        Map<Integer, List<Note>> notesParMatiere = new HashMap<>();
        
        for (Note n : notes) {
            notesParMatiere.computeIfAbsent(n.getMatiereId(), k -> new ArrayList<>()).add(n);
        }

        Map<Integer, Double> result = new HashMap<>();
        for (Integer mId : notesParMatiere.keySet()) {
            result.put(mId, calculerMoyenneMatiereDepuisListe(notesParMatiere.get(mId)));
        }
        return result;
    }

    private double calculerMoyenneMatiereDepuisListe(List<Note> notes) throws SQLException {
        double sommeNotesPonderees = 0;
        double sommePoids          = 0;

        for (Note n : notes) {
            TypeEvaluation te = typeEvalDAO.trouverParId(n.getTypeEvalId());
            double poids = (te != null) ? te.getPoids() : 1.0;
            sommeNotesPonderees += n.getValeur() * poids;
            sommePoids          += poids;
        }

        return sommePoids == 0 ? 0.0 : arrondir(sommeNotesPonderees / sommePoids);
    }

    private double arrondir(double v) {
        return Math.round(v * 100.0) / 100.0;
    }
}
