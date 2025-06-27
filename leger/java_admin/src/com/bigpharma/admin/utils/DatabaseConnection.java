package com.bigpharma.admin.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Classe utilitaire pour gérer la connexion à la base de données
 * Implémente le pattern Singleton pour garantir une seule instance de connexion
 */
public class DatabaseConnection {
    private static final Logger LOGGER = Logger.getLogger(DatabaseConnection.class.getName());
    private static DatabaseConnection instance;
    private Connection connection;
    
    /**
     * Constructeur privé pour empêcher l'instanciation directe
     */
    private DatabaseConnection() {
        try {
            // Charger le driver JDBC
            Class.forName(DatabaseConfig.DB_DRIVER);
            
            // Établir la connexion
            connection = DriverManager.getConnection(
                DatabaseConfig.FULL_DB_URL,
                DatabaseConfig.DB_USER,
                DatabaseConfig.DB_PASSWORD
            );
            
            LOGGER.info("Connexion à la base de données établie avec succès");
        } catch (ClassNotFoundException e) {
            LOGGER.log(Level.SEVERE, "Driver JDBC non trouvé", e);
            throw new RuntimeException("Driver JDBC non trouvé", e);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de la connexion à la base de données", e);
            throw new RuntimeException("Erreur lors de la connexion à la base de données", e);
        }
    }
    
    /**
     * Obtenir l'instance unique de la connexion (pattern Singleton)
     * @return L'instance de DatabaseConnection
     */
    public static synchronized DatabaseConnection getInstance() {
        if (instance == null || instance.getConnection() == null) {
            instance = new DatabaseConnection();
        }
        return instance;
    }
    
    /**
     * Obtenir la connexion à la base de données
     * @return La connexion JDBC
     */
    public Connection getConnection() {
        try {
            // Vérifier si la connexion est fermée ou invalide
            if (connection == null || connection.isClosed()) {
                // Recréer la connexion
                connection = DriverManager.getConnection(
                    DatabaseConfig.FULL_DB_URL,
                    DatabaseConfig.DB_USER,
                    DatabaseConfig.DB_PASSWORD
                );
                LOGGER.info("Connexion à la base de données rétablie");
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de la vérification/rétablissement de la connexion", e);
            throw new RuntimeException("Erreur lors de la vérification/rétablissement de la connexion", e);
        }
        return connection;
    }
    
    /**
     * Fermer la connexion à la base de données
     */
    public void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
                LOGGER.info("Connexion à la base de données fermée");
            } catch (SQLException e) {
                LOGGER.log(Level.WARNING, "Erreur lors de la fermeture de la connexion", e);
            }
        }
    }
}
