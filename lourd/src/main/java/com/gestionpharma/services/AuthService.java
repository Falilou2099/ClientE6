package com.gestionpharma.services;

import com.gestionpharma.config.DatabaseConfig;
import com.gestionpharma.models.Admin;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.mindrot.jbcrypt.BCrypt;
import java.util.logging.Logger;
import java.util.logging.Level;

public class AuthService {
    
    private static final Logger LOGGER = Logger.getLogger(AuthService.class.getName());
    
    /**
     * Authentifie un utilisateur avec son email et son mot de passe
     * @param email Email de l'utilisateur (celui utilisé lors de l'inscription sur l'application PHP)
     * @param password Mot de passe de l'utilisateur
     * @return L'objet Admin si l'authentification réussit, null sinon
     */
    public static Admin login(String email, String password) {
        LOGGER.info("Tentative de connexion avec l'email: " + email);
        
        try (Connection conn = DatabaseConfig.getConnection()) {
            // 1. Vérifier d'abord si l'utilisateur existe dans la table administrateurs
            String adminSql = "SELECT * FROM administrateurs WHERE email = ?";
            LOGGER.info("Recherche dans la table administrateurs: " + adminSql);
            
            try (PreparedStatement pstmt = conn.prepareStatement(adminSql)) {
                pstmt.setString(1, email);
                
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        LOGGER.info("Utilisateur trouvé dans la table administrateurs");
                        String storedPassword = rs.getString("password");
                        
                        // Vérifier si le mot de passe correspond
                        boolean passwordMatches = verifyPassword(password, storedPassword);
                        LOGGER.info("Résultat de la vérification du mot de passe: " + (passwordMatches ? "Succès" : "Échec"));
                        
                        if (passwordMatches) {
                            LOGGER.info("Connexion réussie en tant qu'administrateur");
                            return new Admin(
                                rs.getInt("id"),
                                rs.getString("username"),
                                rs.getString("nom"),
                                rs.getString("prenom"),
                                rs.getString("email"),
                                rs.getInt("pharmacie_id")
                            );
                        }
                    } else {
                        LOGGER.info("Utilisateur non trouvé dans la table administrateurs");
                    }
                }
            }
            
            // 2. Si l'utilisateur n'est pas dans la table administrateurs, vérifier dans la table users
            LOGGER.info("Vérification dans la table users");
            
            // 2.1 Vérifier si l'utilisateur existe dans la table users
            String userSql = "SELECT * FROM users WHERE email = ?";
            LOGGER.info("Requête SQL pour vérifier l'utilisateur dans users: " + userSql);
            
            try (PreparedStatement pstmt = conn.prepareStatement(userSql)) {
                pstmt.setString(1, email);
                
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        LOGGER.info("Utilisateur trouvé dans la table users");
                        
                        // Récupérer les informations de l'utilisateur
                        int userId = rs.getInt("id");
                        String userEmail = rs.getString("email");
                        String storedPassword = rs.getString("password");
                        
                        // Vérifier si le mot de passe correspond
                        boolean passwordMatches = verifyPassword(password, storedPassword);
                        LOGGER.info("Résultat de la vérification du mot de passe: " + (passwordMatches ? "Succès" : "Échec"));
                        
                        if (passwordMatches) {
                            // 3. Créer un administrateur si l'authentification réussit
                            LOGGER.info("Authentification réussie dans la table users");
                            
                            // 3.1 Vérifier si un administrateur existe déjà avec cet email
                            String checkAdminSql = "SELECT * FROM administrateurs WHERE email = ?";
                            try (PreparedStatement checkStmt = conn.prepareStatement(checkAdminSql)) {
                                checkStmt.setString(1, userEmail);
                                try (ResultSet checkRs = checkStmt.executeQuery()) {
                                    if (checkRs.next()) {
                                        LOGGER.info("L'administrateur existe déjà, retour des informations existantes");
                                        return new Admin(
                                            checkRs.getInt("id"),
                                            checkRs.getString("username"),
                                            checkRs.getString("nom"),
                                            checkRs.getString("prenom"),
                                            checkRs.getString("email"),
                                            checkRs.getInt("pharmacie_id")
                                        );
                                    } else {
                                        // 3.2 Créer un nouvel administrateur
                                        LOGGER.info("Création d'un nouvel administrateur basé sur l'utilisateur PHP");
                                        
                                        // Générer un nom d'utilisateur basé sur l'email
                                        String username = userEmail.split("@")[0];
                                        
                                        // Déterminer l'ID de la pharmacie
                                        int pharmacyId = 1; // Valeur par défaut
                                        try {
                                            // Essayer de récupérer pharmacy_id si la colonne existe
                                            pharmacyId = rs.getInt("pharmacy_id");
                                            LOGGER.info("Pharmacy ID récupéré: " + pharmacyId);
                                        } catch (SQLException e) {
                                            LOGGER.warning("Impossible de récupérer pharmacy_id, utilisation de la valeur par défaut: " + pharmacyId);
                                        }
                                        
                                        // Vérifier si la pharmacie existe, sinon la créer
                                        try {
                                            String checkPharmacySql = "SELECT * FROM pharmacies WHERE id = ?";
                                            PreparedStatement checkPharmacyStmt = conn.prepareStatement(checkPharmacySql);
                                            checkPharmacyStmt.setInt(1, pharmacyId);
                                            ResultSet checkPharmacyRs = checkPharmacyStmt.executeQuery();
                                            
                                            if (!checkPharmacyRs.next()) {
                                                LOGGER.info("Pharmacie non trouvée, création d'une nouvelle pharmacie");
                                                String createPharmacySql = "INSERT INTO pharmacies (id, nom) VALUES (?, 'Pharmacie par défaut')";
                                                PreparedStatement createPharmacyStmt = conn.prepareStatement(createPharmacySql);
                                                createPharmacyStmt.setInt(1, pharmacyId);
                                                createPharmacyStmt.executeUpdate();
                                                createPharmacyStmt.close();
                                            }
                                            
                                            checkPharmacyRs.close();
                                            checkPharmacyStmt.close();
                                        } catch (SQLException e) {
                                            LOGGER.warning("Erreur lors de la vérification/création de la pharmacie: " + e.getMessage());
                                        }
                                        
                                        // Insérer le nouvel administrateur
                                        String insertSql = "INSERT INTO administrateurs (username, password, email, pharmacie_id) VALUES (?, ?, ?, ?)";
                                        try (PreparedStatement insertStmt = conn.prepareStatement(insertSql, PreparedStatement.RETURN_GENERATED_KEYS)) {
                                            insertStmt.setString(1, username);
                                            insertStmt.setString(2, storedPassword); // Utiliser le même mot de passe que dans la table users
                                            insertStmt.setString(3, userEmail);
                                            insertStmt.setInt(4, pharmacyId);
                                            
                                            int affectedRows = insertStmt.executeUpdate();
                                            if (affectedRows > 0) {
                                                try (ResultSet generatedKeys = insertStmt.getGeneratedKeys()) {
                                                    if (generatedKeys.next()) {
                                                        int adminId = generatedKeys.getInt(1);
                                                        LOGGER.info("Nouvel administrateur créé avec ID: " + adminId);
                                                        return new Admin(
                                                            adminId,
                                                            username,
                                                            "", // Nom non défini
                                                            "", // Prénom non défini
                                                            userEmail,
                                                            pharmacyId
                                                        );
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        } else {
                            LOGGER.warning("Mot de passe incorrect pour l'utilisateur: " + userEmail);
                        }
                    } else {
                        LOGGER.warning("Aucun utilisateur trouvé avec l'email: " + email);
                    }
                }
            }
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de l'authentification", e);
        }
        
        return null;
    }
    
    /**
     * Vérifie si un mot de passe correspond à un hash PHP password_hash()
     * @param password Mot de passe en texte brut
     * @param phpPasswordHash Hash généré par PHP password_hash()
     * @return true si le mot de passe correspond, false sinon
     */
    private static boolean verifyPassword(String password, String phpPasswordHash) {
        if (phpPasswordHash == null || password == null) {
            LOGGER.warning("Mot de passe ou hash null");
            return false;
        }
        
        LOGGER.info("Vérification du mot de passe. Hash format: " + phpPasswordHash.substring(0, Math.min(10, phpPasswordHash.length())) + "...");
        
        try {
            // Vérifier si c'est un hash BCrypt (commence par $2y$ ou $2a$)
            if (phpPasswordHash.startsWith("$2y$") || phpPasswordHash.startsWith("$2a$")) {
                LOGGER.info("Détecté comme hash BCrypt");
                // Convertir le format PHP $2y$ en format Java $2a$ si nécessaire
                String bcryptHash = phpPasswordHash;
                if (phpPasswordHash.startsWith("$2y$")) {
                    bcryptHash = "$2a$" + phpPasswordHash.substring(4);
                    LOGGER.info("Conversion du format $2y$ vers $2a$: " + bcryptHash.substring(0, Math.min(10, bcryptHash.length())) + "...");
                }
                
                // Utiliser BCrypt pour vérifier le mot de passe
                boolean result = false;
                try {
                    result = BCrypt.checkpw(password, bcryptHash);
                    LOGGER.info("Résultat de la vérification BCrypt: " + result);
                } catch (Exception e) {
                    LOGGER.log(Level.WARNING, "Erreur lors de la vérification BCrypt, essai avec égalité simple", e);
                    // Si la vérification BCrypt échoue, essayer une comparaison simple
                    result = password.equals(phpPasswordHash);
                    LOGGER.info("Résultat de la vérification par égalité simple: " + result);
                }
                return result;
            } 
            // Si c'est un mot de passe en texte brut (pour la compatibilité avec les anciens comptes)
            else {
                LOGGER.info("Hash non BCrypt, vérification par égalité simple");
                boolean result = password.equals(phpPasswordHash);
                LOGGER.info("Résultat de la vérification par égalité simple: " + result);
                return result;
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de la vérification du mot de passe", e);
            return false;
        }
    }
}
