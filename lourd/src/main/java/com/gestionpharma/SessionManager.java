package com.gestionpharma;

/**
 * Gestionnaire de session pour l'application BigPharma
 * Stocke les informations de l'utilisateur connecté
 */
public class SessionManager {
    private static int userId = -1;
    private static int pharmacieId = -1;
    private static String nomUtilisateur = "";
    private static String emailUtilisateur = "";
    private static String roleUtilisateur = "";
    private static boolean isConnected = false;
    
    /**
     * Initialise la session avec les données de l'utilisateur connecté
     */
    public static void initSession(int userId, int pharmacieId, String nomUtilisateur, String email, String role) {
        SessionManager.userId = userId;
        SessionManager.pharmacieId = pharmacieId;
        SessionManager.nomUtilisateur = nomUtilisateur;
        SessionManager.emailUtilisateur = email;
        SessionManager.roleUtilisateur = role;
        SessionManager.isConnected = true;
        
        System.out.println("Session initialisée pour: " + nomUtilisateur + " (ID: " + userId + ", Pharmacie: " + pharmacieId + ")");
    }
    
    /**
     * Ferme la session actuelle
     */
    public static void closeSession() {
        userId = -1;
        pharmacieId = -1;
        nomUtilisateur = "";
        emailUtilisateur = "";
        roleUtilisateur = "";
        isConnected = false;
        
        System.out.println("Session fermée");
    }
    
    /**
     * Vérifie si un utilisateur est connecté
     */
    public static boolean isUserConnected() {
        return isConnected && userId > 0;
    }
    
    /**
     * Vérifie si l'utilisateur connecté est un administrateur
     */
    public static boolean isAdmin() {
        return isConnected && "admin".equalsIgnoreCase(roleUtilisateur);
    }
    
    // Getters
    public static int getUserId() {
        return userId;
    }
    
    public static int getPharmacieId() {
        return pharmacieId > 0 ? pharmacieId : 1; // Fallback sur pharmacie ID 1
    }
    
    public static String getNomUtilisateur() {
        return nomUtilisateur;
    }
    
    public static String getEmailUtilisateur() {
        return emailUtilisateur;
    }
    
    public static String getRoleUtilisateur() {
        return roleUtilisateur;
    }
    
    /**
     * Retourne les informations de session sous forme de chaîne
     */
    public static String getSessionInfo() {
        if (!isConnected) {
            return "Aucune session active";
        }
        
        return String.format("Utilisateur: %s | Email: %s | Rôle: %s | Pharmacie ID: %d", 
                           nomUtilisateur, emailUtilisateur, roleUtilisateur, pharmacieId);
    }
}
