package com.gestionpharma.services;

import com.gestionpharma.config.DatabaseConfig;
import com.gestionpharma.models.Stock;
import com.gestionpharma.utils.AlertUtils;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Service pour gérer les opérations liées aux stocks
 */
public class StockService {
    // Logger pour tracer les opérations
    private static final Logger logger = Logger.getLogger(StockService.class.getName());
    
    /**
     * Récupère tous les stocks d'une pharmacie
     * @param pharmacieId ID de la pharmacie
     * @return Liste des stocks
     */
    public List<Stock> getAllStocks(int pharmacieId) {
        List<Stock> stocks = new ArrayList<>();
        
        try (Connection conn = DatabaseConfig.getConnection()) {
            // Vérifier si la table stocks existe
            DatabaseMetaData metaData = conn.getMetaData();
            ResultSet tables = metaData.getTables(null, null, "stocks", null);
            
            if (!tables.next()) {
                System.out.println("La table 'stocks' n'existe pas. Tentative de création...");
                createStocksTable(conn, pharmacieId);
            }
            
            // Requête pour récupérer les stocks
            String query = "SELECT s.*, p.nom as produit_nom FROM stocks s " +
                           "JOIN produits p ON s.produit_id = p.id " +
                           "WHERE s.pharmacie_id = ?";
            
            try (PreparedStatement pstmt = conn.prepareStatement(query)) {
                pstmt.setInt(1, pharmacieId);
                ResultSet rs = pstmt.executeQuery();
                
                while (rs.next()) {
                    Stock stock = mapResultSetToStock(rs);
                    stocks.add(stock);
                }
            } catch (SQLException e) {
                System.out.println("Erreur lors de la récupération des stocks: " + e.getMessage());
            }
            
        } catch (SQLException e) {
            System.out.println("Erreur de connexion à la base de données: " + e.getMessage());
        }
        
        return stocks;
    }
    
    /**
     * Crée la table stocks si elle n'existe pas et y ajoute les données de base
     * @param conn Connexion à la base de données
     * @param pharmacieId ID de la pharmacie
     */
    private void createStocksTable(Connection conn, int pharmacieId) throws SQLException {
        // Création de la table stocks
        String createTableSQL = "CREATE TABLE IF NOT EXISTS stocks (" +
                               "id INT AUTO_INCREMENT PRIMARY KEY, " +
                               "produit_id INT NOT NULL, " +
                               "pharmacie_id INT NOT NULL, " +
                               "quantite INT NOT NULL DEFAULT 0, " +
                               "seuil_minimum INT NOT NULL DEFAULT 10, " +
                               "date_expiration DATE NULL, " +
                               "dernier_mouvement TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                               "FOREIGN KEY (produit_id) REFERENCES produits(id), " +
                               "FOREIGN KEY (pharmacie_id) REFERENCES pharmacies(id)" +
                               ")";
        
        try (Statement stmt = conn.createStatement()) {
            stmt.execute(createTableSQL);
            System.out.println("Table 'stocks' créée avec succès.");
            
            // Synchroniser les stocks avec les produits existants
            String syncStocksSQL = "INSERT INTO stocks (produit_id, pharmacie_id, quantite, seuil_minimum, date_expiration) " +
                                  "SELECT id, pharmacie_id, quantite_stock, seuil_alerte, date_expiration FROM produits " +
                                  "WHERE pharmacie_id = ? AND id NOT IN (SELECT produit_id FROM stocks WHERE pharmacie_id = ?)";
            
            try (PreparedStatement pstmt = conn.prepareStatement(syncStocksSQL)) {
                pstmt.setInt(1, pharmacieId);
                pstmt.setInt(2, pharmacieId);
                pstmt.executeUpdate();
                System.out.println("Stocks synchronisés avec les produits existants.");
            }
        }
    }
    
    /**
     * Ajoute un nouveau stock
     * @param stock Stock à ajouter
     * @return true si l'ajout a réussi, false sinon
     */
    public boolean ajouterStock(Stock stock) {
        String query = "INSERT INTO stocks (produit_id, pharmacie_id, quantite, seuil_minimum, " +
                       "statut, date_expiration, dernier_mouvement) VALUES (?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            
            pstmt.setInt(1, stock.getProduitId());
            pstmt.setInt(2, stock.getPharmacieId());
            pstmt.setInt(3, stock.getQuantite());
            pstmt.setInt(4, stock.getSeuilMinimum());
            pstmt.setString(5, stock.getStatut());
            pstmt.setDate(6, stock.getDateExpiration() != null ? 
                    Date.valueOf(stock.getDateExpiration()) : null);
            pstmt.setTimestamp(7, Timestamp.valueOf(LocalDateTime.now()));
            
            int affectedRows = pstmt.executeUpdate();
            
            if (affectedRows > 0) {
                ResultSet generatedKeys = pstmt.getGeneratedKeys();
                if (generatedKeys.next()) {
                    stock.setId(generatedKeys.getInt(1));
                    return true;
                }
            }
            
        } catch (SQLException e) {
            AlertUtils.showErrorAlert("Erreur", "Erreur de base de données", 
                    "Impossible d'ajouter le stock : " + e.getMessage());
        }
        
        return false;
    }
    
    /**
     * Met à jour la quantité d'un stock
     * @param stockId ID du stock
     * @param nouvelleQuantite Nouvelle quantité
     * @return true si la mise à jour a réussi, false sinon
     */
    public boolean updateQuantiteStock(int stockId, int nouvelleQuantite) {
        String query = "UPDATE stocks SET quantite = ?, dernier_mouvement = ?, " +
                       "statut = CASE WHEN ? <= seuil_minimum THEN 'Alerte' ELSE 'Normal' END " +
                       "WHERE id = ?";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setInt(1, nouvelleQuantite);
            pstmt.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));
            pstmt.setInt(3, nouvelleQuantite);
            pstmt.setInt(4, stockId);
            
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
            
        } catch (SQLException e) {
            AlertUtils.showErrorAlert("Erreur", "Erreur de base de données", 
                    "Impossible de mettre à jour la quantité du stock : " + e.getMessage());
        }
        
        return false;
    }
    
    /**
     * Met à jour le seuil minimum d'un stock
     * @param stockId ID du stock
     * @param nouveauSeuil Nouveau seuil minimum
     * @return true si la mise à jour a réussi, false sinon
     */
    public boolean updateSeuilMinimum(int stockId, int nouveauSeuil) {
        String query = "UPDATE stocks SET seuil_minimum = ?, " +
                       "statut = CASE WHEN quantite <= ? THEN 'Alerte' ELSE 'Normal' END " +
                       "WHERE id = ?";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setInt(1, nouveauSeuil);
            pstmt.setInt(2, nouveauSeuil);
            pstmt.setInt(3, stockId);
            
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
            
        } catch (SQLException e) {
            AlertUtils.showErrorAlert("Erreur", "Erreur de base de données", 
                    "Impossible de mettre à jour le seuil minimum : " + e.getMessage());
        }
        
        return false;
    }
    
    /**
     * Met à jour la date d'expiration d'un stock
     * @param stockId ID du stock
     * @param nouvelleDate Nouvelle date d'expiration
     * @return true si la mise à jour a réussi, false sinon
     */
    public boolean updateDateExpiration(int stockId, LocalDate nouvelleDate) {
        String query = "UPDATE stocks SET date_expiration = ? WHERE id = ?";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setDate(1, Date.valueOf(nouvelleDate));
            pstmt.setInt(2, stockId);
            
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
            
        } catch (SQLException e) {
            AlertUtils.showErrorAlert("Erreur", "Erreur de base de données", 
                    "Impossible de mettre à jour la date d'expiration : " + e.getMessage());
        }
        
        return false;
    }
    
    /**
     * Récupère un stock par son ID
     * @param stockId ID du stock
     * @return Stock trouvé ou null
     */
    public Stock getStockById(int stockId) {
        String query = "SELECT s.*, p.nom as produit_nom FROM stocks s " +
                       "JOIN produits p ON s.produit_id = p.id " +
                       "WHERE s.id = ?";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setInt(1, stockId);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return mapResultSetToStock(rs);
            }
            
        } catch (SQLException e) {
            AlertUtils.showErrorAlert("Erreur", "Erreur de base de données", 
                    "Impossible de récupérer le stock : " + e.getMessage());
        }
        
        return null;
    }
    
    /**
     * Récupère le stock d'un produit dans une pharmacie
     * @param produitId ID du produit
     * @param pharmacieId ID de la pharmacie
     * @return Stock trouvé ou null
     */
    public Stock getStockByProduitAndPharmacie(int produitId, int pharmacieId) {
        String query = "SELECT s.*, p.nom as produit_nom FROM stocks s " +
                       "JOIN produits p ON s.produit_id = p.id " +
                       "WHERE s.produit_id = ? AND s.pharmacie_id = ?";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setInt(1, produitId);
            pstmt.setInt(2, pharmacieId);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return mapResultSetToStock(rs);
            }
            
        } catch (SQLException e) {
            AlertUtils.showErrorAlert("Erreur", "Erreur de base de données", 
                    "Impossible de récupérer le stock : " + e.getMessage());
        }
        
        return null;
    }
    
    /**
     * Récupère les stocks en alerte (quantité <= seuil minimum)
     * @param pharmacieId ID de la pharmacie
     * @return Liste des stocks en alerte
     */
    public List<Stock> getStocksEnAlerte(int pharmacieId) {
        List<Stock> stocks = new ArrayList<>();
        String query = "SELECT s.*, p.nom as produit_nom FROM stocks s " +
                       "JOIN produits p ON s.produit_id = p.id " +
                       "WHERE s.pharmacie_id = ? AND s.quantite <= s.seuil_minimum";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setInt(1, pharmacieId);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                Stock stock = mapResultSetToStock(rs);
                stocks.add(stock);
            }
            
        } catch (SQLException e) {
            AlertUtils.showErrorAlert("Erreur", "Erreur de base de données", 
                    "Impossible de récupérer les stocks en alerte : " + e.getMessage());
        }
        
        return stocks;
    }
    
    /**
     * Récupère les stocks qui vont bientôt expirer
     * @param pharmacieId ID de la pharmacie
     * @param joursAvantExpiration Nombre de jours avant expiration
     * @return Liste des stocks qui vont bientôt expirer
     */
    public List<Stock> getStocksEnExpiration(int pharmacieId, int joursAvantExpiration) {
        List<Stock> stocks = new ArrayList<>();
        String query = "SELECT s.*, p.nom as produit_nom FROM stocks s " +
                       "JOIN produits p ON s.produit_id = p.id " +
                       "WHERE s.pharmacie_id = ? AND s.date_expiration IS NOT NULL " +
                       "AND s.date_expiration <= DATE_ADD(CURDATE(), INTERVAL ? DAY)";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setInt(1, pharmacieId);
            pstmt.setInt(2, joursAvantExpiration);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                Stock stock = mapResultSetToStock(rs);
                stocks.add(stock);
            }
            
        } catch (SQLException e) {
            AlertUtils.showErrorAlert("Erreur", "Erreur de base de données", 
                    "Impossible de récupérer les stocks en expiration : " + e.getMessage());
        }
        
        return stocks;
    }
    
    /**
     * Ajoute une quantité à un stock existant
     * @param produitId ID du produit
     * @param quantite Quantité à ajouter
     * @param pharmacieId ID de la pharmacie
     * @return true si l'ajout a réussi, false sinon
     */
    public boolean ajouterStock(int produitId, int quantite, int pharmacieId) {
        // D'abord, récupérer le stock existant
        Stock stock = getStockByProduitAndPharmacie(produitId, pharmacieId);
        
        if (stock != null) {
            // Mettre à jour la quantité
            int nouvelleQuantite = stock.getQuantite() + quantite;
            return updateQuantiteStock(stock.getId(), nouvelleQuantite);
        } else {
            // Créer un nouveau stock si aucun n'existe
            Stock nouveauStock = new Stock();
            nouveauStock.setProduitId(produitId);
            nouveauStock.setPharmacieId(pharmacieId);
            nouveauStock.setQuantite(quantite);
            nouveauStock.setSeuilMinimum(5); // Valeur par défaut
            nouveauStock.setStatut("Normal");
            nouveauStock.setDernierMouvement(LocalDateTime.now());
            
            return ajouterStock(nouveauStock);
        }
    }
    
    /**
     * Ajuste le stock à une valeur spécifique
     * @param produitId ID du produit
     * @param nouvelleQuantite Nouvelle quantité totale
     * @param raison Raison de l'ajustement
     * @param pharmacieId ID de la pharmacie
     * @return true si l'ajustement a réussi, false sinon
     */
    public boolean ajusterStock(int produitId, int nouvelleQuantite, String raison, int pharmacieId) {
        if (nouvelleQuantite < 0) {
            logger.warning("La quantité ne peut pas être négative");
            return false;
        }

        try (Connection conn = DatabaseConfig.getConnection()) {
            conn.setAutoCommit(false); // Démarrer une transaction

            try {
                // Vérifier si le stock existe 
                String checkQuery = "SELECT id, quantite FROM stocks WHERE produit_id = ? AND pharmacie_id = ?";
                int stockId = -1;
                int quantiteActuelle = 0;
                boolean stockExiste = false;

                try (PreparedStatement checkStmt = conn.prepareStatement(checkQuery)) {
                    checkStmt.setInt(1, produitId);
                    checkStmt.setInt(2, pharmacieId);

                    try (ResultSet rs = checkStmt.executeQuery()) {
                        if (rs.next()) {
                            stockId = rs.getInt("id");
                            quantiteActuelle = rs.getInt("quantite");
                            stockExiste = true;
                        }
                    }
                }

                // Calculer la différence pour le mouvement de stock
                int difference = nouvelleQuantite - quantiteActuelle;
                String typeMouvement = "ajustement";

                if (stockExiste) {
                    // Mise à jour du stock existant
                    String updateQuery = "UPDATE stocks SET quantite = ?, dernier_mouvement = NOW() WHERE id = ?";
                    try (PreparedStatement updateStmt = conn.prepareStatement(updateQuery)) {
                        updateStmt.setInt(1, nouvelleQuantite);
                        updateStmt.setInt(2, stockId);
                        updateStmt.executeUpdate();
                    }
                } else {
                    // Création d'un nouveau stock si n'existe pas
                    String insertQuery = "INSERT INTO stocks (produit_id, pharmacie_id, quantite, seuil_minimum, dernier_mouvement) " +
                            "SELECT id, ?, ?, stock_minimum, NOW() FROM produits WHERE id = ?";
                    try (PreparedStatement insertStmt = conn.prepareStatement(insertQuery)) {
                        insertStmt.setInt(1, pharmacieId);
                        insertStmt.setInt(2, nouvelleQuantite);
                        insertStmt.setInt(3, produitId);
                        insertStmt.executeUpdate();
                    }
                }

                // Mise à jour du produit
                String updateProduitQuery = "UPDATE produits SET quantite_stock = ? WHERE id = ?";
                try (PreparedStatement updateProduitStmt = conn.prepareStatement(updateProduitQuery)) {
                    updateProduitStmt.setInt(1, nouvelleQuantite);
                    updateProduitStmt.setInt(2, produitId);
                    updateProduitStmt.executeUpdate();
                }

                // Enregistrement du mouvement
                String mouvementQuery = "INSERT INTO mouvements_stock (produit_id, type_mouvement, quantite, commentaire) VALUES (?, ?, ABS(?), ?)";
                try (PreparedStatement mouvementStmt = conn.prepareStatement(mouvementQuery)) {
                    mouvementStmt.setInt(1, produitId);
                    mouvementStmt.setString(2, typeMouvement);
                    mouvementStmt.setInt(3, difference); // On utilise la valeur absolue pour la quantité
                    mouvementStmt.setString(4, raison + " (Ajustement de " + quantiteActuelle + " à " + nouvelleQuantite + ")");
                    mouvementStmt.executeUpdate();
                }
                
                conn.commit(); // Valider toutes les opérations
                logger.info("Stock ajusté avec succès: produit " + produitId + " - nouvelle quantité: " + nouvelleQuantite);
                return true;
                
            } catch (SQLException e) {
                conn.rollback(); // Annuler toutes les opérations en cas d'erreur
                logger.severe("Erreur lors de l'ajustement de stock: " + e.getMessage());
                e.printStackTrace();
                return false;
            } finally {
                conn.setAutoCommit(true); // Rétablir l'auto-commit
            }
            
        } catch (SQLException e) {
            logger.severe("Erreur de connexion lors de l'ajustement de stock: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Retire une quantité d'un stock existant
     * @param produitId ID du produit
     * @param quantite Quantité à retirer
     * @param raison Raison du retrait
     * @param pharmacieId ID de la pharmacie
     * @return true si le retrait a réussi, false sinon
     */
    public boolean retirerStock(int produitId, int quantite, String raison, int pharmacieId) {
        // Récupérer le stock existant
        Stock stock = getStockByProduitAndPharmacie(produitId, pharmacieId);
        
        if (stock != null) {
            // Vérifier que la quantité est suffisante
            if (stock.getQuantite() < quantite) {
                AlertUtils.showWarningAlert("Attention", "Stock insuffisant", 
                        "La quantité en stock est insuffisante pour effectuer cette sortie.");
                return false;
            }
            
            // Mettre à jour la quantité
            int nouvelleQuantite = stock.getQuantite() - quantite;
            return updateQuantiteStock(stock.getId(), nouvelleQuantite);
        } else {
            AlertUtils.showWarningAlert("Attention", "Stock inexistant", 
                    "Aucun stock n'existe pour ce produit dans cette pharmacie.");
            return false;
        }
    }
    
    /**
     * Convertit un ResultSet en objet Stock
     * @param rs ResultSet contenant les données du stock
     * @return Objet Stock
     * @throws SQLException En cas d'erreur d'accès aux données
     */
    private Stock mapResultSetToStock(ResultSet rs) throws SQLException {
        Stock stock = new Stock();
        stock.setId(rs.getInt("id"));
        stock.setProduitId(rs.getInt("produit_id"));
        stock.setProduitNom(rs.getString("produit_nom"));
        stock.setPharmacieId(rs.getInt("pharmacie_id"));
        stock.setQuantite(rs.getInt("quantite"));
        stock.setSeuilMinimum(rs.getInt("seuil_minimum"));
        // Définir un statut par défaut puisque la colonne n'existe pas dans la base de données
        stock.setStatut(stock.getQuantite() <= stock.getSeuilMinimum() ? "Alerte" : "Normal");
        
        Date dateExpiration = rs.getDate("date_expiration");
        if (dateExpiration != null) {
            stock.setDateExpiration(dateExpiration.toLocalDate());
        }
        
        Timestamp dernierMouvement = rs.getTimestamp("dernier_mouvement");
        if (dernierMouvement != null) {
            stock.setDernierMouvement(dernierMouvement.toLocalDateTime());
        }
        
        return stock;
    }
}
