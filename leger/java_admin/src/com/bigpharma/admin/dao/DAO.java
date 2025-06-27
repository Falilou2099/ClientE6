package com.bigpharma.admin.dao;

import java.util.List;

/**
 * Interface générique pour les opérations CRUD
 * @param <T> Le type d'entité
 * @param <K> Le type de clé primaire
 */
public interface DAO<T, K> {
    
    /**
     * Récupère une entité par sa clé primaire
     * @param id La clé primaire
     * @return L'entité ou null si elle n'existe pas
     */
    T findById(K id);
    
    /**
     * Récupère toutes les entités
     * @return Liste de toutes les entités
     */
    List<T> findAll();
    
    /**
     * Enregistre une nouvelle entité
     * @param entity L'entité à enregistrer
     * @return L'entité enregistrée avec sa clé primaire générée
     */
    T save(T entity);
    
    /**
     * Met à jour une entité existante
     * @param entity L'entité à mettre à jour
     * @return L'entité mise à jour
     */
    T update(T entity);
    
    /**
     * Supprime une entité par sa clé primaire
     * @param id La clé primaire
     * @return true si la suppression a réussi, false sinon
     */
    boolean delete(K id);
}
