package com.gestionpharma.services;

import com.gestionpharma.config.DatabaseConfig;
import com.gestionpharma.models.Activite;
import com.gestionpharma.utils.AlertUtils;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Service pour gérer les opérations liées aux activités
 */
public class ActiviteService {
    private static final Logger LOGGER = Logger.getLogger(ActiviteService.class.getName());
    
    /**
     * Récupère toutes les activités d'une pharmacie
     * @param pharmacieId ID de la pharmacie
     * @param limit Nombre maximum d'activités à récupérer
     * @return Liste des activités
     */
    public List<Activite> getActivitesRecentes(int pharmacieId, int limit) {
        List<Activite> activites = new ArrayList<>();
        
        try (Connection conn = DatabaseConfig.getConnection()) {
            // Vérifier si la table activites existe
            DatabaseMetaData metaData = conn.getMetaData();
            ResultSet tables = metaData.getTables(null, null, "activites", null);
            
            if (!tables.next()) {
                LOGGER.info("La table 'activites' n'existe pas. Tentative de création...");
                createActivitesTable(conn);
            }
            
            // Récupérer les activités récentes
            String query = "SELECT * FROM activites WHERE pharmacie_id = ? ORDER BY date DESC LIMIT ?";
            
            try (PreparedStatement pstmt = conn.prepareStatement(query)) {
                pstmt.setInt(1, pharmacieId);
                pstmt.setInt(2, limit);
                ResultSet rs = pstmt.executeQuery();
                
                while (rs.next()) {
                    Activite activite = mapResultSetToActivite(rs);
                    activites.add(activite);
                }
            } catch (SQLException e) {
                LOGGER.warning("Erreur lors de la récupération des activités: " + e.getMessage());
                System.out.println("Erreur lors de la récupération des activités: " + e.getMessage());
            }
            
        } catch (SQLException e) {
            LOGGER.severe("Erreur de connexion à la base de données: " + e.getMessage());
            System.out.println("Erreur de connexion à la base de données: " + e.getMessage());
        }
        
        return activites;
    }
    
    /**
     * Crée la table activites si elle n'existe pas
     * @param conn Connexion à la base de données
     */
    private void createActivitesTable(Connection conn) throws SQLException {
        String createTableSQL = "CREATE TABLE IF NOT EXISTS activites (" +
                               "id INT AUTO_INCREMENT PRIMARY KEY, " +
                               "date TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                               "type VARCHAR(50) NOT NULL, " +
                               "description TEXT NOT NULL, " +
                               "utilisateur VARCHAR(100), " +
                               "pharmacie_id INT NOT NULL, " +
                               "FOREIGN KEY (pharmacie_id) REFERENCES pharmacies(id)" +
                               ")";
        
        try (Statement stmt = conn.createStatement()) {
            stmt.execute(createTableSQL);
            LOGGER.info("Table 'activites' créée avec succès.");
            
            // Ajouter une activité initiale pour chaque pharmacie
            String insertInitialActivity = "INSERT INTO activites (type, description, utilisateur, pharmacie_id) " +
                                           "SELECT 'Système', 'Initialisation du système', 'Système', id FROM pharmacies ";
            
            stmt.execute(insertInitialActivity);
            LOGGER.info("Activités initiales ajoutées.");
        }
    }
    
    /**
     * Ajoute une nouvelle activité
     * @param type Type d'activité (ex: "Produit", "Stock", "Commande")
     * @param description Description de l'activité
     * @param utilisateur Nom de l'utilisateur qui a effectué l'action
     * @param pharmacieId ID de la pharmacie
     * @return true si l'ajout a réussi, false sinon
     */
    public boolean ajouterActivite(String type, String description, String utilisateur, int pharmacieId) {
        String query = "INSERT INTO activites (type, description, date, utilisateur, pharmacie_id) " +
                       "VALUES (?, ?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setString(1, type);
            pstmt.setString(2, description);
            pstmt.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now()));
            pstmt.setString(4, utilisateur);
            pstmt.setInt(5, pharmacieId);
            
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
            
        } catch (SQLException e) {
            AlertUtils.showErrorAlert("Erreur", "Erreur de base de données", 
                    "Impossible d'ajouter l'activité : " + e.getMessage());
        }
        
        return false;
    }
    
    /**
     * Récupère les activités d'un certain type
     * @param type Type d'activité
     * @param pharmacieId ID de la pharmacie
     * @return Liste des activités du type spécifié
     */
    public List<Activite> getActivitesByType(String type, int pharmacieId) {
        List<Activite> activites = new ArrayList<>();
        String query = "SELECT * FROM activites WHERE type = ? AND pharmacie_id = ? ORDER BY date DESC";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setString(1, type);
            pstmt.setInt(2, pharmacieId);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                Activite activite = mapResultSetToActivite(rs);
                activites.add(activite);
            }
            
        } catch (SQLException e) {
            AlertUtils.showErrorAlert("Erreur", "Erreur de base de données", 
                    "Impossible de récupérer les activités : " + e.getMessage());
        }
        
        return activites;
    }
    
    /**
     * Récupère les activités d'un utilisateur
     * @param utilisateur Nom de l'utilisateur
     * @param pharmacieId ID de la pharmacie
     * @return Liste des activités de l'utilisateur
     */
    public List<Activite> getActivitesByUtilisateur(String utilisateur, int pharmacieId) {
        List<Activite> activites = new ArrayList<>();
        String query = "SELECT * FROM activites WHERE utilisateur = ? AND pharmacie_id = ? ORDER BY date DESC";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setString(1, utilisateur);
            pstmt.setInt(2, pharmacieId);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                Activite activite = mapResultSetToActivite(rs);
                activites.add(activite);
            }
            
        } catch (SQLException e) {
            AlertUtils.showErrorAlert("Erreur", "Erreur de base de données", 
                    "Impossible de récupérer les activités : " + e.getMessage());
        }
        
        return activites;
    }
    
    /**
     * Récupère les activités d'une période spécifique
     * @param debut Date de début
     * @param fin Date de fin
     * @param pharmacieId ID de la pharmacie
     * @return Liste des activités de la période
     */
    public List<Activite> getActivitesByPeriode(LocalDateTime debut, LocalDateTime fin, int pharmacieId) {
        List<Activite> activites = new ArrayList<>();
        String query = "SELECT * FROM activites WHERE date BETWEEN ? AND ? AND pharmacie_id = ? ORDER BY date DESC";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setTimestamp(1, Timestamp.valueOf(debut));
            pstmt.setTimestamp(2, Timestamp.valueOf(fin));
            pstmt.setInt(3, pharmacieId);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                Activite activite = mapResultSetToActivite(rs);
                activites.add(activite);
            }
            
        } catch (SQLException e) {
            AlertUtils.showErrorAlert("Erreur", "Erreur de base de données", 
                    "Impossible de récupérer les activités : " + e.getMessage());
        }
        
        return activites;
    }
    
    /**
     * Convertit un ResultSet en objet Activite
     * @param rs ResultSet contenant les données de l'activité
     * @return Objet Activite
     * @throws SQLException En cas d'erreur d'accès aux données
     */
    private Activite mapResultSetToActivite(ResultSet rs) throws SQLException {
        Activite activite = new Activite();
        activite.setId(rs.getInt("id"));
        activite.setType(rs.getString("type"));
        activite.setDescription(rs.getString("description"));
        
        Timestamp date = rs.getTimestamp("date");
        if (date != null) {
            activite.setDate(date.toLocalDateTime());
        }
        
        activite.setUtilisateur(rs.getString("utilisateur"));
        activite.setPharmacieId(rs.getInt("pharmacie_id"));
        
        return activite;
    }
}
