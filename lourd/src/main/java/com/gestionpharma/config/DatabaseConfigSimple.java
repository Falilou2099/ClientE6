package com.gestionpharma.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Logger;

/**
 * Configuration simplifiée de la base de données pour les tests
 * sans dépendances JavaFX
 */
public class DatabaseConfigSimple {
    private static final Logger LOGGER = Logger.getLogger(DatabaseConfigSimple.class.getName());
    
    private static final String URL = "jdbc:mysql://localhost:3306/bigpharma";
    private static final String USERNAME = "root";
    private static final String PASSWORD = "";
    
    /**
     * Obtient une connexion simple à la base de données
     * @return Connexion à la base de données
     * @throws SQLException Si une erreur SQL se produit
     */
    public static Connection getConnection() throws SQLException {
        try {
            // Charger le driver MySQL
            Class.forName("com.mysql.cj.jdbc.Driver");
            return DriverManager.getConnection(URL, USERNAME, PASSWORD);
        } catch (ClassNotFoundException e) {
            LOGGER.severe("Driver MySQL non trouvé: " + e.getMessage());
            throw new SQLException("Driver MySQL non disponible", e);
        } catch (SQLException e) {
            LOGGER.warning("Erreur de connexion à la base de données: " + e.getMessage());
            throw e;
        }
    }
    
    /**
     * Teste la connexion à la base de données
     * @return true si la connexion fonctionne
     */
    public static boolean testConnection() {
        try (Connection conn = getConnection()) {
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            LOGGER.warning("Test de connexion échoué: " + e.getMessage());
            return false;
        }
    }
}
