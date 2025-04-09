package com.gestionpharma.services;

import com.gestionpharma.config.DatabaseConfig;
import com.gestionpharma.models.Produit;
import com.gestionpharma.utils.AlertUtils;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Service pour gérer les opérations liées aux produits
 */
public class ProduitService {
    
    /**
     * Récupère tous les produits d'une pharmacie
     * @param pharmacieId ID de la pharmacie
     * @return Liste des produits
     */
    public List<Produit> getAllProduits(int pharmacieId) {
        List<Produit> produits = new ArrayList<>();
        String query = "SELECT * FROM produits WHERE pharmacie_id = ?";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setInt(1, pharmacieId);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                Produit produit = mapResultSetToProduit(rs);
                produits.add(produit);
            }
            
        } catch (SQLException e) {
            AlertUtils.showErrorAlert("Erreur", "Erreur de base de données", 
                    "Impossible de récupérer les produits : " + e.getMessage());
        }
        
        return produits;
    }
    
    /**
     * Ajoute un nouveau produit
     * @param produit Produit à ajouter
     * @param pharmacieId ID de la pharmacie
     * @return true si l'ajout a réussi, false sinon
     */
    public boolean ajouterProduit(Produit produit, int pharmacieId) {
        try (Connection conn = DatabaseConfig.getConnection()) {
            // Vérifier la structure de la table produits
            verifierColonneProduits(conn);
            
            // Ajouter le produit
            String query = "INSERT INTO produits (nom, description, prix_achat, prix_vente, categorie, " +
                           "quantite_stock, seuil_alerte, date_expiration, pharmacie_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
            
            try (PreparedStatement pstmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
                pstmt.setString(1, produit.getNom());
                pstmt.setString(2, produit.getDescription());
                pstmt.setDouble(3, produit.getPrixAchat());
                pstmt.setDouble(4, produit.getPrixVente());
                pstmt.setString(5, produit.getCategorie());
                pstmt.setInt(6, produit.getQuantiteStock());
                pstmt.setInt(7, 10); // Seuil d'alerte par défaut
                pstmt.setDate(8, produit.getDateExpiration() != null ? 
                        Date.valueOf(produit.getDateExpiration()) : null);
                pstmt.setInt(9, pharmacieId);
                
                int affectedRows = pstmt.executeUpdate();
                
                if (affectedRows > 0) {
                    ResultSet generatedKeys = pstmt.getGeneratedKeys();
                    if (generatedKeys.next()) {
                        produit.setId(generatedKeys.getInt(1));
                        
                        // Synchroniser avec la table des stocks
                        synchroniserStock(conn, produit, pharmacieId);
                        
                        // Ajouter une activité
                        try {
                            String activityQuery = "INSERT INTO activites (type, description, utilisateur, pharmacie_id) " +
                                                  "VALUES (?, ?, ?, ?)";
                            try (PreparedStatement activityStmt = conn.prepareStatement(activityQuery)) {
                                activityStmt.setString(1, "Produit");
                                activityStmt.setString(2, "Ajout du produit: " + produit.getNom());
                                activityStmt.setString(3, "Admin"); // À remplacer par le nom de l'administrateur connecté
                                activityStmt.setInt(4, pharmacieId);
                                activityStmt.executeUpdate();
                            }
                        } catch (SQLException e) {
                            // Ignorer les erreurs d'activité, ce n'est pas critique
                            System.out.println("Erreur lors de l'ajout de l'activité: " + e.getMessage());
                        }
                        
                        return true;
                    }
                }
            } catch (SQLException e) {
                AlertUtils.showErrorAlert("Erreur", "Erreur de base de données", 
                        "Impossible d'ajouter le produit : " + e.getMessage());
                return false;
            }
        } catch (SQLException e) {
            System.out.println("Erreur de connexion à la base de données: " + e.getMessage());
            AlertUtils.showErrorAlert("Erreur", "Erreur de connexion", 
                    "Impossible de se connecter à la base de données : " + e.getMessage());
        }
        
        return false;
    }
    
    /**
     * Vérifie et ajoute la colonne date_expiration si elle n'existe pas
     * @param conn Connexion à la base de données
     * @throws SQLException En cas d'erreur SQL
     */
    private void verifierColonneProduits(Connection conn) throws SQLException {
        DatabaseMetaData metaData = conn.getMetaData();
        ResultSet columns = metaData.getColumns(null, null, "produits", "date_expiration");
        
        if (!columns.next()) {
            // La colonne n'existe pas, on l'ajoute
            try (Statement stmt = conn.createStatement()) {
                stmt.execute("ALTER TABLE produits ADD COLUMN date_expiration DATE");
                System.out.println("Colonne date_expiration ajoutée à la table produits");
            } catch (SQLException e) {
                // Si l'erreur est due à une colonne déjà existante, on ignore
                if (!e.getMessage().contains("Duplicate column")) {
                    throw e;
                }
            }
        }
        
        columns.close();
    }
    
    /**
     * Synchronise le produit avec la table des stocks
     * @param conn Connexion à la base de données
     * @param produit Produit à synchroniser
     * @param pharmacieId ID de la pharmacie
     */
    private void synchroniserStock(Connection conn, Produit produit, int pharmacieId) {
        try {
            // Vérifier si une entrée existe déjà dans la table stocks
            String checkQuery = "SELECT id FROM stocks WHERE produit_id = ? AND pharmacie_id = ?";
            try (PreparedStatement checkStmt = conn.prepareStatement(checkQuery)) {
                checkStmt.setInt(1, produit.getId());
                checkStmt.setInt(2, pharmacieId);
                ResultSet rs = checkStmt.executeQuery();
                
                if (rs.next()) {
                    // Mettre à jour l'entrée existante
                    String updateQuery = "UPDATE stocks SET quantite = ? WHERE produit_id = ? AND pharmacie_id = ?";
                    try (PreparedStatement updateStmt = conn.prepareStatement(updateQuery)) {
                        updateStmt.setInt(1, produit.getQuantiteStock());
                        updateStmt.setInt(2, produit.getId());
                        updateStmt.setInt(3, pharmacieId);
                        updateStmt.executeUpdate();
                    }
                } else {
                    // Créer une nouvelle entrée
                    String insertQuery = "INSERT INTO stocks (produit_id, pharmacie_id, quantite) VALUES (?, ?, ?)";
                    try (PreparedStatement insertStmt = conn.prepareStatement(insertQuery)) {
                        insertStmt.setInt(1, produit.getId());
                        insertStmt.setInt(2, pharmacieId);
                        insertStmt.setInt(3, produit.getQuantiteStock());
                        insertStmt.executeUpdate();
                    }
                }
            }
        } catch (SQLException e) {
            System.out.println("Erreur lors de la synchronisation du stock: " + e.getMessage());
            // On continue malgré l'erreur, car ce n'est pas critique
        }
    }
    
    /**
     * Met à jour un produit existant
     * @param produit Produit à mettre à jour
     * @return true si la mise à jour a réussi, false sinon
     */
    public boolean modifierProduit(Produit produit) {
        String query = "UPDATE produits SET nom = ?, description = ?, prix_achat = ?, prix_vente = ?, " +
                       "categorie = ?, quantite_stock = ?, date_expiration = ? WHERE id = ?";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setString(1, produit.getNom());
            pstmt.setString(2, produit.getDescription());
            pstmt.setDouble(3, produit.getPrixAchat());
            pstmt.setDouble(4, produit.getPrixVente());
            pstmt.setString(5, produit.getCategorie());
            pstmt.setInt(6, produit.getQuantiteStock());
            pstmt.setDate(7, produit.getDateExpiration() != null ? 
                    Date.valueOf(produit.getDateExpiration()) : null);
            pstmt.setInt(8, produit.getId());
            
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
            
        } catch (SQLException e) {
            AlertUtils.showErrorAlert("Erreur", "Erreur de base de données", 
                    "Impossible de modifier le produit : " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Supprime un produit
     * @param produitId ID du produit à supprimer
     * @return true si la suppression a réussi, false sinon
     */
    public boolean supprimerProduit(int produitId) {
        try (Connection conn = DatabaseConfig.getConnection()) {
            String query = "DELETE FROM produits WHERE id = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(query)) {
                pstmt.setInt(1, produitId);
                int affectedRows = pstmt.executeUpdate();
                return affectedRows > 0;
            }
        } catch (SQLException e) {
            AlertUtils.showErrorAlert("Erreur", "Erreur de base de données",
                    "Impossible de supprimer le produit : " + e.getMessage());
            return false;
        }
    }

    /**
     * Recherche des produits par nom ou catégorie
     * @param searchTerm Terme de recherche
     * @param pharmacieId ID de la pharmacie
     * @return Liste des produits correspondant à la recherche
     */
    public List<Produit> rechercherProduits(String searchTerm, int pharmacieId) {
        List<Produit> produits = new ArrayList<>();
        String query = "SELECT * FROM produits WHERE (nom LIKE ? OR description LIKE ? OR categorie LIKE ?) AND pharmacie_id = ?";
        String searchTermWithWildcards = "%" + searchTerm + "%";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, searchTermWithWildcards);
            pstmt.setString(2, searchTermWithWildcards);
            pstmt.setString(3, searchTermWithWildcards);
            pstmt.setInt(4, pharmacieId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Produit produit = mapResultSetToProduit(rs);
                produits.add(produit);
            }

        } catch (SQLException e) {
            AlertUtils.showErrorAlert("Erreur", "Erreur de base de données",
                    "Erreur lors de la recherche des produits : " + e.getMessage());
        }

        return produits;
    }

    /**
     * Récupère un produit par son ID
     * @param produitId ID du produit
     * @return Produit trouvé ou null
     */
    public Produit getProduitById(int produitId) {
        String query = "SELECT * FROM produits WHERE id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, produitId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return mapResultSetToProduit(rs);
            } else {
                return null;
            }

        } catch (SQLException e) {
            AlertUtils.showErrorAlert("Erreur", "Erreur de base de données",
                    "Erreur lors de la récupération du produit : " + e.getMessage());
            return null;
        }
    }

    /**
     * Récupère les produits dont le stock est inférieur au seuil minimum
     * @param pharmacieId ID de la pharmacie
     * @return Liste des produits en alerte de stock
     */
    public List<Produit> getProduitsEnAlerte(int pharmacieId) {
        List<Produit> produits = new ArrayList<>();
        String query = "SELECT * FROM produits WHERE quantite_stock <= seuil_alerte AND pharmacie_id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, pharmacieId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Produit produit = mapResultSetToProduit(rs);
                produits.add(produit);
            }

        } catch (SQLException e) {
            AlertUtils.showErrorAlert("Erreur", "Erreur de base de données",
                    "Erreur lors de la récupération des produits en alerte : " + e.getMessage());
        }

        return produits;
    }

    /**
     * Récupère les produits qui vont bientôt expirer
     * @param pharmacieId ID de la pharmacie
     * @param joursAvantExpiration Nombre de jours avant expiration
     * @return Liste des produits qui vont bientôt expirer
     */
    public List<Produit> getProduitsEnExpiration(int pharmacieId, int joursAvantExpiration) {
        List<Produit> produits = new ArrayList<>();
        String query = "SELECT * FROM produits WHERE date_expiration <= DATE_ADD(CURDATE(), INTERVAL ? DAY) AND pharmacie_id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, pharmacieId);
            pstmt.setInt(2, joursAvantExpiration);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Produit produit = mapResultSetToProduit(rs);
                produits.add(produit);
            }

        } catch (SQLException e) {
            AlertUtils.showErrorAlert("Erreur", "Erreur de base de données",
                    "Impossible de récupérer les produits en expiration : " + e.getMessage());
        }

        return produits;
    }
    
    /**
     * Convertit un ResultSet en objet Produit
     * @param rs ResultSet contenant les données du produit
     * @return Objet Produit
     * @throws SQLException En cas d'erreur d'accès aux données
     */
    private Produit mapResultSetToProduit(ResultSet rs) throws SQLException {
        Produit produit = new Produit();
        produit.setId(rs.getInt("id"));
        produit.setNom(rs.getString("nom"));
        produit.setDescription(rs.getString("description"));
        produit.setPrixAchat(rs.getDouble("prix_achat"));
        produit.setPrixVente(rs.getDouble("prix_vente"));
        produit.setCategorie(rs.getString("categorie"));
        produit.setQuantiteStock(rs.getInt("quantite_stock"));
        
        Date dateExpiration = rs.getDate("date_expiration");
        if (dateExpiration != null) {
            produit.setDateExpiration(dateExpiration.toLocalDate());
        }
        
        return produit;
    }
    
    /**
     * Récupère tous les produits d'une pharmacie (alias pour getAllProduits)
     * @param pharmacieId ID de la pharmacie
     * @return Liste des produits
     */
    public List<Produit> getProduitsByPharmacie(int pharmacieId) {
        return getAllProduits(pharmacieId);
    }
    
    /**
     * Ajoute un nouveau produit (alias pour ajouterProduit)
     * @param produit Produit à ajouter
     * @return true si l'ajout a réussi, false sinon
     */
    public boolean addProduit(Produit produit) {
        return ajouterProduit(produit, produit.getPharmacieId());
    }
    
    /**
     * Met à jour un produit existant (alias pour modifierProduit)
     * @param produit Produit à mettre à jour
     * @return true si la mise à jour a réussi, false sinon
     */
    public boolean updateProduit(Produit produit) {
        return modifierProduit(produit);
    }
    
    /**
     * Supprime un produit (alias pour supprimerProduit)
     * @param produitId ID du produit à supprimer
     * @return true si la suppression a réussi, false sinon
     */
    public boolean deleteProduit(int produitId) {
        return supprimerProduit(produitId);
    }
    
    /**
     * Récupère toutes les catégories de produits disponibles pour une pharmacie
     * @param pharmacieId ID de la pharmacie
     * @return Liste des catégories de produits
     */
    public List<String> getAllCategories(int pharmacieId) {
        List<String> categories = new ArrayList<>();
        String query = "SELECT DISTINCT categorie FROM produits WHERE pharmacie_id = ? ORDER BY categorie";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setInt(1, pharmacieId);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                String categorie = rs.getString("categorie");
                if (categorie != null && !categorie.isEmpty()) {
                    categories.add(categorie);
                }
            }
            
        } catch (SQLException e) {
            AlertUtils.showErrorAlert("Erreur", "Erreur de base de données", 
                    "Impossible de récupérer les catégories : " + e.getMessage());
        }
        
        return categories;
    }
}
