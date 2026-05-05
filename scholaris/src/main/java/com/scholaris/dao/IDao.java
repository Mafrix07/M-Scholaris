package com.scholaris.dao;

import java.sql.SQLException;
import java.util.List;

/**
 * Interface générique DAO.
 * Tous les DAOs implémentent ces méthodes CRUD de base.
 * T = type de l'entité (ex : Etudiant, Note...)
 */
public interface IDao<T> {

    /** Insère un nouvel enregistrement en base. */
    void ajouter(T entity) throws SQLException;

    /** Met à jour un enregistrement existant. */
    void modifier(T entity) throws SQLException;

    /** Supprime un enregistrement par son id. */
    void supprimer(int id) throws SQLException;

    /** Récupère un enregistrement par son id. */
    T trouverParId(int id) throws SQLException;

    /** Récupère tous les enregistrements. */
    List<T> trouverTous() throws SQLException;
}
