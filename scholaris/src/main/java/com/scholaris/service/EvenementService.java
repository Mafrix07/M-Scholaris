package com.scholaris.service;

import com.scholaris.dao.EvenementDAO;
import com.scholaris.model.Evenement;

import java.sql.SQLException;
import java.util.List;

/**
 * Service pour la gestion du calendrier scolaire.
 */
public class EvenementService {

    private EvenementDAO evenementDAO;

    public EvenementService() throws SQLException {
        this.evenementDAO = new EvenementDAO();
    }

    /**
     * Récupère les prochains événements pour une classe donnée.
     */
    public List<Evenement> getProchainsEvenements(int classeId) throws SQLException {
        return evenementDAO.trouverProchains(classeId);
    }

    /**
     * Création d'un événement (ex: examen, réunion).
     */
    public void creerEvenement(Evenement ev) throws SQLException {
        if (ev.getTitre() == null || ev.getTitre().isEmpty()) {
            throw new IllegalArgumentException("Le titre de l'événement est obligatoire.");
        }
        if (ev.getDateEvent() == null) {
            throw new IllegalArgumentException("La date de l'événement est obligatoire.");
        }
        evenementDAO.ajouter(ev);
    }

    /**
     * Récupère tous les événements.
     */
    public List<Evenement> listerTous() throws SQLException {
        return evenementDAO.trouverTous();
    }

    /**
     * Supprime un événement.
     */
    public void supprimerEvenement(int id) throws SQLException {
        evenementDAO.supprimer(id);
    }
}
