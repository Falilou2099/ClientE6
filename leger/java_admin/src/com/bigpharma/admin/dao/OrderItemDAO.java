package com.bigpharma.admin.dao;

import com.bigpharma.admin.models.OrderItem;
import com.bigpharma.admin.utils.DatabaseConnection;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Classe DAO pour les opérations sur les articles de commande
 */
public class OrderItemDAO implements DAO<OrderItem, Integer> {
    private static final Logger LOGGER = Logger.getLogger(OrderItemDAO.class.getName());
    private final Connection connection;
    
    public OrderItemDAO() {
        this.connection = DatabaseConnection.getInstance().getConnection();
    }
    
    @Override
    public OrderItem findById(Integer id) {
        String query = "SELECT li.*, p.nom as nom_produit FROM lignes_commande li " +
                      "LEFT JOIN produits p ON li.produit_id = p.id " +
                      "WHERE li.id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return mapResultSetToOrderItem(rs);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de la recherche de l'article de commande par ID", e);
        }
        return null;
    }
    
    @Override
    public List<OrderItem> findAll() {
        List<OrderItem> items = new ArrayList<>();
        String query = "SELECT li.*, p.nom as nom_produit FROM lignes_commande li " +
                      "LEFT JOIN produits p ON li.produit_id = p.id";
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                items.add(mapResultSetToOrderItem(rs));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de la récupération de tous les articles de commande", e);
        }
        
        return items;
    }
    
    /**
     * Récupère tous les articles d'une commande
     * @param orderId L'ID de la commande
     * @return Liste des articles de la commande
     */
    public List<OrderItem> findByOrderId(Integer orderId) {
        List<OrderItem> items = new ArrayList<>();
        String query = "SELECT li.*, p.nom as nom_produit FROM lignes_commande li " +
                      "LEFT JOIN produits p ON li.produit_id = p.id " +
                      "WHERE li.commande_id = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, orderId);
            
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                items.add(mapResultSetToOrderItem(rs));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de la récupération des articles de la commande", e);
        }
        
        return items;
    }
    
    @Override
    public OrderItem save(OrderItem item) {
        String query = "INSERT INTO lignes_commande (commande_id, produit_id, quantite, prix_unitaire) " +
                      "VALUES (?, ?, ?, ?)";
        
        try (PreparedStatement stmt = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, item.getCommandeId());
            stmt.setInt(2, item.getProduitId());
            stmt.setInt(3, item.getQuantite());
            stmt.setBigDecimal(4, item.getPrixUnitaire());
            
            int affectedRows = stmt.executeUpdate();
            
            if (affectedRows == 0) {
                throw new SQLException("La création de l'article de commande a échoué, aucune ligne affectée.");
            }
            
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    item.setId(generatedKeys.getInt(1));
                } else {
                    throw new SQLException("La création de l'article de commande a échoué, aucun ID obtenu.");
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de la sauvegarde de l'article de commande", e);
            return null;
        }
        
        return item;
    }
    
    @Override
    public OrderItem update(OrderItem item) {
        String query = "UPDATE lignes_commande SET commande_id = ?, produit_id = ?, " +
                      "quantite = ?, prix_unitaire = ? WHERE id = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, item.getCommandeId());
            stmt.setInt(2, item.getProduitId());
            stmt.setInt(3, item.getQuantite());
            stmt.setBigDecimal(4, item.getPrixUnitaire());
            stmt.setInt(5, item.getId());
            
            int affectedRows = stmt.executeUpdate();
            
            if (affectedRows == 0) {
                throw new SQLException("La mise à jour de l'article de commande a échoué, aucune ligne affectée.");
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de la mise à jour de l'article de commande", e);
            return null;
        }
        
        return item;
    }
    
    @Override
    public boolean delete(Integer id) {
        String query = "DELETE FROM lignes_commande WHERE id = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, id);
            
            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de la suppression de l'article de commande", e);
            return false;
        }
    }
    
    /**
     * Supprime tous les articles d'une commande
     * @param orderId L'ID de la commande
     * @return true si la suppression a réussi, false sinon
     */
    public boolean deleteByOrderId(Integer orderId) {
        String query = "DELETE FROM lignes_commande WHERE commande_id = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, orderId);
            
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de la suppression des articles de la commande", e);
            return false;
        }
    }
    
    /**
     * Convertit un ResultSet en objet OrderItem
     * @param rs Le ResultSet contenant les données de l'article de commande
     * @return L'objet OrderItem créé
     * @throws SQLException En cas d'erreur SQL
     */
    private OrderItem mapResultSetToOrderItem(ResultSet rs) throws SQLException {
        OrderItem item = new OrderItem();
        item.setId(rs.getInt("id"));
        item.setCommandeId(rs.getInt("commande_id"));
        item.setProduitId(rs.getInt("produit_id"));
        item.setQuantite(rs.getInt("quantite"));
        item.setPrixUnitaire(rs.getBigDecimal("prix_unitaire"));
        
        // Récupérer le nom du produit s'il est disponible
        try {
            item.setNomProduit(rs.getString("nom_produit"));
        } catch (SQLException e) {
            // Ignorer si la colonne n'existe pas
        }
        
        return item;
    }
}
