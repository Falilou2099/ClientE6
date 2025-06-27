package com.gestionpharma.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Optional;
import java.util.logging.Logger;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;

public class DatabaseConfig {
    private static final Logger LOGGER = Logger.getLogger(DatabaseConfig.class.getName());
    private static HikariDataSource dataSource;
    private static boolean databaseChecked = false;
    private static boolean offlineMode = false;

    static {
        initializeDatabase();
    }
    
    /**
     * Tente d'initialiser la connexion à la base de données
     */
    private static void initializeDatabase() {
        try {
            HikariConfig config = new HikariConfig();
            config.setJdbcUrl("jdbc:mysql://localhost:3306/bigpharma");
            config.setUsername("root");
            config.setPassword("");
            config.setMaximumPoolSize(10);
            config.setAutoCommit(true);
            config.addDataSourceProperty("cachePrepStmts", "true");
            config.addDataSourceProperty("prepStmtCacheSize", "250");
            config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
            
            // Paramètres de reconnexion automatique
            config.setConnectionTimeout(5000); // 5 secondes
            config.setIdleTimeout(600000); // 10 minutes
            config.setMaxLifetime(1800000); // 30 minutes
            config.addDataSourceProperty("useSSL", "false");
            config.addDataSourceProperty("allowPublicKeyRetrieval", "true");
            
            // Configuration pour traiter les erreurs de connexion
            config.addDataSourceProperty("connectTimeout", "5000"); // 5 secondes
            config.addDataSourceProperty("socketTimeout", "10000"); // 10 secondes

            dataSource = new HikariDataSource(config);
            LOGGER.info("Connexion à la base de données initialisée avec succès");
            
            // Tester la connexion
            try (Connection conn = dataSource.getConnection()) {
                createDatabaseIfNotExists();
                offlineMode = false; // Si on arrive ici, on est en ligne
            } catch (SQLException e) {
                LOGGER.severe("Erreur lors de l'initialisation de la base de données: " + e.getMessage());
                offlineMode = true;
                showDatabaseConnectionError();
            }
        } catch (Exception e) {
            LOGGER.severe("Erreur fatale lors de l'initialisation du pool de connexions: " + e.getMessage());
            offlineMode = true;
            showDatabaseConnectionError();
        }
    }
    
    /**
     * Affiche une fenêtre d'erreur concernant la connexion à la base de données
     */
    private static void showDatabaseConnectionError() {
        // Exécuter sur le thread JavaFX
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur de connexion à la base de données");
            alert.setHeaderText("Impossible de se connecter au serveur MySQL");
            alert.setContentText("Veuillez vérifier que :\n\n" +
                                 "1. Le serveur MySQL est démarré (XAMPP, WampServer, etc.)\n" +
                                 "2. Le serveur est accessible sur localhost:3306\n" +
                                 "3. L'utilisateur 'root' sans mot de passe a accès\n\n" +
                                 "L'application va fonctionner en mode hors ligne avec des fonctionnalités limitées.");
            
            // Ajouter un bouton pour réessayer la connexion
            ButtonType retryButton = new ButtonType("Réessayer", ButtonBar.ButtonData.OK_DONE);
            alert.getButtonTypes().setAll(retryButton);
            
            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == retryButton) {
                // Réessayer la connexion
                initializeDatabase();
            }
        });
    }

    /**
     * Obtient une connexion à la base de données et vérifie/répare la structure si nécessaire
     * @return Connexion à la base de données
     * @throws SQLException Si une erreur SQL se produit
     */
    public static Connection getConnection() throws SQLException {
        if (offlineMode) {
            // Si en mode hors ligne, tenter de se reconnecter
            try {
                initializeDatabase();
                if (offlineMode) { // Si toujours en mode hors ligne après tentative
                    throw new SQLException("Application en mode hors ligne, base de données inaccessible");
                }
            } catch (Exception e) {
                throw new SQLException("Impossible de se connecter à la base de données", e);
            }
        }
        
        if (dataSource == null) {
            throw new SQLException("La source de données n'est pas initialisée");
        }
        
        Connection connection = dataSource.getConnection();
        
        // Vérifier et réparer la structure de la base de données (seulement une fois par session)
        if (!databaseChecked) {
            checkAndRepairDatabaseStructure(connection);
            databaseChecked = true;
        }
        
        return connection;
    }
    
    /**
     * Vérifie si l'application est en mode hors ligne
     * @return true si l'application est en mode hors ligne
     */
    public static boolean isOfflineMode() {
        return offlineMode;
    }
    
    /**
     * Vérifie si l'application est connectée à la base de données
     * @return true si la connexion à la base de données est disponible
     */
    public static boolean isConnected() {
        if (offlineMode) {
            return false;
        }
        
        // Test la connexion en essayant d'obtenir une connexion
        try (Connection conn = dataSource.getConnection()) {
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            LOGGER.warning("Impossible de vérifier la connexion à la base de données: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Tente de remettre l'application en ligne en se reconnectant à la base de données
     * @return true si la reconnexion a réussi
     */
    public static boolean tryReconnect() {
        initializeDatabase();
        return !offlineMode;
    }
    
    /**
     * Crée la base de données si elle n'existe pas
     */
    private static void createDatabaseIfNotExists() {
        try (Connection conn = dataSource.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.execute("CREATE DATABASE IF NOT EXISTS bigpharma");
                stmt.execute("USE bigpharma");
                LOGGER.info("Base de données bigpharma vérifiée/créée avec succès");
            }
        } catch (SQLException e) {
            LOGGER.severe("Erreur lors de la création de la base de données: " + e.getMessage());
        }
    }
    
    /**
     * Vérifie et répare la structure de la base de données
     * @param conn Connexion à la base de données
     */
    private static void checkAndRepairDatabaseStructure(Connection conn) {
        try {
            DatabaseMetaData metaData = conn.getMetaData();
            
            // Vérifier les tables requises
            checkAndCreateTable(conn, metaData, "pharmacies", 
                "CREATE TABLE pharmacies (\n" +
                "    id INT AUTO_INCREMENT PRIMARY KEY,\n" +
                "    nom VARCHAR(255) NOT NULL,\n" +
                "    adresse TEXT NOT NULL,\n" +
                "    telephone VARCHAR(20) NOT NULL,\n" +
                "    email VARCHAR(255) NOT NULL UNIQUE,\n" +
                "    date_creation TIMESTAMP DEFAULT CURRENT_TIMESTAMP,\n" +
                "    statut ENUM('actif', 'inactif') DEFAULT 'actif'\n" +
                ")");
                
            checkAndCreateTable(conn, metaData, "administrateurs", 
                "CREATE TABLE administrateurs (\n" +
                "    id INT AUTO_INCREMENT PRIMARY KEY,\n" +
                "    username VARCHAR(50) NOT NULL UNIQUE,\n" +
                "    password VARCHAR(255) NOT NULL,\n" +
                "    nom VARCHAR(100),\n" +
                "    prenom VARCHAR(100),\n" +
                "    email VARCHAR(255) UNIQUE,\n" +
                "    pharmacie_id INT NOT NULL,\n" +
                "    date_creation TIMESTAMP DEFAULT CURRENT_TIMESTAMP,\n" +
                "    dernier_login TIMESTAMP NULL,\n" +
                "    statut ENUM('actif', 'inactif') DEFAULT 'actif',\n" +
                "    FOREIGN KEY (pharmacie_id) REFERENCES pharmacies(id)\n" +
                ")");
                
            checkAndCreateTable(conn, metaData, "produits", 
                "CREATE TABLE produits (\n" +
                "    id INT AUTO_INCREMENT PRIMARY KEY,\n" +
                "    nom VARCHAR(255) NOT NULL,\n" +
                "    description TEXT,\n" +
                "    prix_vente DECIMAL(10, 2) NOT NULL,\n" +
                "    prix_achat DECIMAL(10, 2) NOT NULL,\n" +
                "    quantite_stock INT NOT NULL DEFAULT 0,\n" +
                "    seuil_alerte INT NOT NULL DEFAULT 10,\n" +
                "    categorie VARCHAR(100),\n" +
                "    date_expiration DATE NULL,\n" +
                "    pharmacie_id INT NOT NULL,\n" +
                "    date_ajout TIMESTAMP DEFAULT CURRENT_TIMESTAMP,\n" +
                "    FOREIGN KEY (pharmacie_id) REFERENCES pharmacies(id)\n" +
                ")");
                
            checkAndCreateTable(conn, metaData, "fournisseurs", 
                "CREATE TABLE fournisseurs (\n" +
                "    id INT AUTO_INCREMENT PRIMARY KEY,\n" +
                "    nom VARCHAR(255) NOT NULL,\n" +
                "    adresse TEXT,\n" +
                "    telephone VARCHAR(20),\n" +
                "    email VARCHAR(255),\n" +
                "    siret VARCHAR(14),\n" +
                "    pharmacie_id INT NOT NULL,\n" +
                "    FOREIGN KEY (pharmacie_id) REFERENCES pharmacies(id)\n" +
                ")");
                
            checkAndCreateTable(conn, metaData, "stocks", 
                "CREATE TABLE stocks (\n" +
                "    id INT AUTO_INCREMENT PRIMARY KEY,\n" +
                "    produit_id INT NOT NULL,\n" +
                "    pharmacie_id INT NOT NULL,\n" +
                "    quantite INT NOT NULL DEFAULT 0,\n" +
                "    seuil_minimum INT NOT NULL DEFAULT 10,\n" +
                "    date_expiration DATE NULL,\n" +
                "    dernier_mouvement TIMESTAMP DEFAULT CURRENT_TIMESTAMP,\n" +
                "    FOREIGN KEY (produit_id) REFERENCES produits(id),\n" +
                "    FOREIGN KEY (pharmacie_id) REFERENCES pharmacies(id)\n" +
                ")");
                
            checkAndCreateTable(conn, metaData, "activites", 
                "CREATE TABLE activites (\n" +
                "    id INT AUTO_INCREMENT PRIMARY KEY,\n" +
                "    date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,\n" +
                "    type VARCHAR(50) NOT NULL,\n" +
                "    description TEXT NOT NULL,\n" +
                "    utilisateur VARCHAR(100),\n" +
                "    pharmacie_id INT NOT NULL,\n" +
                "    FOREIGN KEY (pharmacie_id) REFERENCES pharmacies(id)\n" +
                ")");
                
            // Ajouter la table des catégories
            checkAndCreateTable(conn, metaData, "categories", 
                "CREATE TABLE categories (\n" +
                "    id INT AUTO_INCREMENT PRIMARY KEY,\n" +
                "    name VARCHAR(100) NOT NULL UNIQUE\n" +
                ")");
            
            // Vérifier si la table pharmacies est vide, et ajouter des données de test si nécessaire
            Statement stmt = conn.createStatement();
            
            // Ajouter les catégories par défaut si la table est vide
            ResultSet rsCategories = stmt.executeQuery("SELECT COUNT(*) FROM categories");
            if (rsCategories.next() && rsCategories.getInt(1) == 0) {
                System.out.println("Ajout des catégories par défaut dans la base de données...");
                
                // Liste des catégories pharmaceutiques standard
                String[] defaultCategories = {
                    "Analgésiques", "Anti-inflammatoires", "Antibiotiques", "Antihistaminiques",
                    "Gastro-entérologie", "Dermatologie", "Cardiologie", "Vitamines",
                    "Compléments alimentaires", "Homéopathie", "Hygiène", "Premiers soins",
                    "Ophtalmologie", "ORL", "Contraception", "Nutrition", "Autres"
                };
                
                for (String category : defaultCategories) {
                    try {
                        stmt.execute("INSERT INTO categories (name) VALUES ('" + category + "')");
                    } catch (SQLException e) {
                        // Ignorer les erreurs de duplication (catégorie déjà existante)
                        if (!e.getMessage().contains("Duplicate entry")) {
                            LOGGER.warning("Erreur lors de l'ajout de la catégorie '" + category + "': " + e.getMessage());
                        }
                    }
                }
                
                LOGGER.info("Catégories par défaut ajoutées avec succès");
            }
            
            // Vérifier si la table pharmacies est vide
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM pharmacies");
            if (rs.next() && rs.getInt(1) == 0) {
                // Insérer une pharmacie de test
                stmt.execute("INSERT INTO pharmacies (nom, adresse, telephone, email) VALUES " +
                            "('Pharmacie Centrale', '15 rue de la Paix, 75001 Paris', '0123456789', 'contact@pharmaciecentrale.fr')");
                
                // Insérer un administrateur de test
                stmt.execute("INSERT INTO administrateurs (username, password, nom, prenom, email, pharmacie_id) VALUES " +
                            "('admin', 'Admin123!', 'Dupont', 'Jean', 'admin@pharmaciecentrale.fr', " +
                            "(SELECT id FROM pharmacies WHERE nom = 'Pharmacie Centrale'))");
                
                // Insérer des catégories par défaut
                stmt.execute("INSERT INTO categories (name) VALUES " +
                            "('Analgésiques'), " +
                            "('Anti-inflammatoires'), " +
                            "('Antibiotiques'), " +
                            "('Antihistaminiques'), " +
                            "('Gastro-entérologie'), " +
                            "('Dermatologie'), " +
                            "('Cardiologie'), " +
                            "('Vitamines'), " +
                            "('Compléments alimentaires'), " +
                            "('Autres')");
                
                LOGGER.info("Données de test insérées avec succès");
            }
            
            LOGGER.info("Structure de la base de données vérifiée et réparée avec succès");
            
        } catch (SQLException e) {
            LOGGER.severe("Erreur lors de la vérification/réparation de la base de données: " + e.getMessage());
        }
    }
    
    /**
     * Vérifie si une table existe et la crée si nécessaire
     * @param conn Connexion à la base de données
     * @param metaData Métadonnées de la base de données
     * @param tableName Nom de la table à vérifier
     * @param createTableSQL SQL pour créer la table
     */
    private static void checkAndCreateTable(Connection conn, DatabaseMetaData metaData, 
                                         String tableName, String createTableSQL) throws SQLException {
        ResultSet tables = metaData.getTables(null, null, tableName, null);
        if (!tables.next()) {
            LOGGER.info("La table '" + tableName + "' n'existe pas. Création en cours...");
            try (Statement stmt = conn.createStatement()) {
                stmt.execute(createTableSQL);
                LOGGER.info("Table '" + tableName + "' créée avec succès");
            }
        }
    }
}
