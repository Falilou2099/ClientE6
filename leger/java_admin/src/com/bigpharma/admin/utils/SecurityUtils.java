package com.bigpharma.admin.utils;

import org.mindrot.jbcrypt.BCrypt;

import java.security.SecureRandom;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Classe utilitaire pour les opérations de sécurité
 * Gère le hachage et la vérification des mots de passe
 */
public class SecurityUtils {
    private static final Logger LOGGER = Logger.getLogger(SecurityUtils.class.getName());
    private static final int BCRYPT_ROUNDS = 12;
    
    /**
     * Hache un mot de passe en utilisant BCrypt
     * @param password Le mot de passe en clair
     * @return Le mot de passe haché
     */
    public static String hashPassword(String password) {
        try {
            return BCrypt.hashpw(password, BCrypt.gensalt(BCRYPT_ROUNDS));
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erreur lors du hachage du mot de passe", e);
            throw new RuntimeException("Erreur lors du hachage du mot de passe", e);
        }
    }
    
    /**
     * Vérifie si un mot de passe en clair correspond à un mot de passe haché
     * @param plainPassword Le mot de passe en clair
     * @param hashedPassword Le mot de passe haché
     * @return true si le mot de passe correspond, false sinon
     */
    public static boolean verifyPassword(String plainPassword, String hashedPassword) {
        try {
            // Vérifier si le mot de passe haché commence par $2a$ ou $2y$ (format BCrypt)
            if (hashedPassword != null && 
                (hashedPassword.startsWith("$2a$") || hashedPassword.startsWith("$2y$"))) {
                return BCrypt.checkpw(plainPassword, hashedPassword);
            }
            return false;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de la vérification du mot de passe", e);
            return false;
        }
    }
    
    /**
     * Génère un token aléatoire pour les opérations de sécurité
     * @param length La longueur du token
     * @return Le token généré
     */
    public static String generateToken(int length) {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[length];
        random.nextBytes(bytes);
        
        StringBuilder token = new StringBuilder();
        for (byte b : bytes) {
            token.append(String.format("%02x", b));
        }
        
        return token.toString();
    }
    
    /**
     * Vérifie si une chaîne est un email valide
     * @param email L'email à vérifier
     * @return true si l'email est valide, false sinon
     */
    public static boolean isValidEmail(String email) {
        if (email == null || email.isEmpty()) {
            return false;
        }
        
        // Expression régulière simple pour valider un email
        String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
        return email.matches(emailRegex);
    }
    
    /**
     * Nettoie une chaîne pour prévenir les injections SQL
     * @param input La chaîne à nettoyer
     * @return La chaîne nettoyée
     */
    public static String sanitizeInput(String input) {
        if (input == null) {
            return null;
        }
        
        // Remplacer les caractères potentiellement dangereux
        return input.replaceAll("['\"\\\\;]", "");
    }
}
