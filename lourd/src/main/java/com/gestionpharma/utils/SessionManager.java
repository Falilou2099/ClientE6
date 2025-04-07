package com.gestionpharma.utils;

import com.gestionpharma.models.Admin;

/**
 * Gestionnaire de session pour stocker les informations
 * de l'utilisateur connecté pendant la session active
 */
public class SessionManager {
    private static Admin currentAdmin;
    private static int pharmacieId;
    
    /**
     * Définit l'administrateur actuellement connecté
     * @param admin L'administrateur
     */
    public static void setCurrentAdmin(Admin admin) {
        currentAdmin = admin;
        if (admin != null) {
            pharmacieId = admin.getPharmacieId();
        }
    }
    
    /**
     * Récupère l'administrateur actuellement connecté
     * @return L'administrateur
     */
    public static Admin getCurrentAdmin() {
        return currentAdmin;
    }
    
    /**
     * Définit l'ID de la pharmacie courante
     * @param id ID de la pharmacie
     */
    public static void setPharmacieId(int id) {
        pharmacieId = id;
    }
    
    /**
     * Récupère l'ID de la pharmacie courante
     * @return ID de la pharmacie
     */
    public static int getPharmacieId() {
        return pharmacieId;
    }
    
    /**
     * Réinitialise les données de session (déconnexion)
     */
    public static void clear() {
        currentAdmin = null;
        pharmacieId = 0;
    }
}
