package com.bigpharma.admin.dao;

import com.bigpharma.admin.models.Order;
import com.bigpharma.admin.models.OrderItem;
import com.bigpharma.admin.utils.DatabaseConnection;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Classe DAO pour les opérations sur les commandes
 */
public class OrderDAO implements DAO<Order, Integer> {
    private static final Logger LOGGER = Logger.getLogger(OrderDAO.class.getName());
    private final Connection connection;
    private final OrderItemDAO orderItemDAO;
    
    public OrderDAO() {
        this.connection = DatabaseConnection.getInstance().getConnection();
        this.orderItemDAO = new OrderItemDAO();
    }
    
    @Override
    public Order findById(Integer id) {
        String query = "SELECT * FROM commandes WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                Order order = mapResultSetToOrder(rs);
                // Charger les articles de la commande
                order.setItems(orderItemDAO.findByOrderId(order.getId()));
                return order;
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de la recherche de la commande par ID", e);
        }
        return null;
    }
    
    @Override
    public List<Order> findAll() {
        return findAllByPharmacyId(null);
    }
    
    /**
     * Récupère toutes les commandes d'une pharmacie
     * @param pharmacyId L'ID de la pharmacie ou null pour toutes les commandes
     * @return Liste des commandes
     */
    public List<Order> findAllByPharmacyId(Integer pharmacyId) {
        List<Order> orders = new ArrayList<>();
        String query = pharmacyId != null 
            ? "SELECT * FROM commandes WHERE pharmacy_id = ? ORDER BY date_commande DESC"
            : "SELECT * FROM commandes ORDER BY date_commande DESC";
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            if (pharmacyId != null) {
                stmt.setInt(1, pharmacyId);
            }
            
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Order order = mapResultSetToOrder(rs);
                // Charger les articles de la commande
                order.setItems(orderItemDAO.findByOrderId(order.getId()));
                orders.add(order);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de la récupération des commandes", e);
        }
        
        return orders;
    }
    
    @Override
    public Order save(Order order) {
        // Utiliser une transaction pour garantir l'intégrité des données
        try {
            connection.setAutoCommit(false);
            
            // Insérer la commande
            String query = "INSERT INTO commandes (client_id, pharmacy_id, date_commande, statut, " +
                          "montant_total, reference) VALUES (?, ?, ?, ?, ?, ?)";
            
            try (PreparedStatement stmt = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
                if (order.getClientId() != null) {
                    stmt.setInt(1, order.getClientId());
                } else {
                    stmt.setNull(1, Types.INTEGER);
                }
                
                if (order.getPharmacyId() != null) {
                    stmt.setInt(2, order.getPharmacyId());
                } else {
                    stmt.setNull(2, Types.INTEGER);
                }
                
                // Date de commande (utiliser la date actuelle si null)
                Timestamp dateCommande = order.getDateCommande() != null 
                    ? new Timestamp(order.getDateCommande().getTime()) 
                    : new Timestamp(System.currentTimeMillis());
                stmt.setTimestamp(3, dateCommande);
                
                stmt.setString(4, order.getStatut());
                stmt.setBigDecimal(5, order.getMontantTotal());
                stmt.setString(6, order.getReference());
                
                int affectedRows = stmt.executeUpdate();
                
                if (affectedRows == 0) {
                    throw new SQLException("La création de la commande a échoué, aucune ligne affectée.");
                }
                
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        order.setId(generatedKeys.getInt(1));
                    } else {
                        throw new SQLException("La création de la commande a échoué, aucun ID obtenu.");
                    }
                }
            }
            
            // Insérer les articles de la commande
            for (OrderItem item : order.getItems()) {
                item.setCommandeId(order.getId());
                orderItemDAO.save(item);
            }
            
            // Valider la transaction
            connection.commit();
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de la sauvegarde de la commande", e);
            try {
                connection.rollback();
            } catch (SQLException ex) {
                LOGGER.log(Level.SEVERE, "Erreur lors du rollback", ex);
            }
            return null;
        } finally {
            try {
                connection.setAutoCommit(true);
            } catch (SQLException e) {
                LOGGER.log(Level.SEVERE, "Erreur lors du rétablissement de l'autocommit", e);
            }
        }
        
        return order;
    }
    
    @Override
    public Order update(Order order) {
        // Utiliser une transaction pour garantir l'intégrité des données
        try {
            connection.setAutoCommit(false);
            
            // Mettre à jour la commande
            String query = "UPDATE commandes SET client_id = ?, pharmacy_id = ?, statut = ?, " +
                          "montant_total = ? WHERE id = ?";
            
            try (PreparedStatement stmt = connection.prepareStatement(query)) {
                if (order.getClientId() != null) {
                    stmt.setInt(1, order.getClientId());
                } else {
                    stmt.setNull(1, Types.INTEGER);
                }
                
                if (order.getPharmacyId() != null) {
                    stmt.setInt(2, order.getPharmacyId());
                } else {
                    stmt.setNull(2, Types.INTEGER);
                }
                
                stmt.setString(3, order.getStatut());
                stmt.setBigDecimal(4, order.getMontantTotal());
                stmt.setInt(5, order.getId());
                
                int affectedRows = stmt.executeUpdate();
                
                if (affectedRows == 0) {
                    throw new SQLException("La mise à jour de la commande a échoué, aucune ligne affectée.");
                }
            }
            
            // Supprimer les anciens articles de la commande
            orderItemDAO.deleteByOrderId(order.getId());
            
            // Insérer les nouveaux articles de la commande
            for (OrderItem item : order.getItems()) {
                item.setCommandeId(order.getId());
                orderItemDAO.save(item);
            }
            
            // Valider la transaction
            connection.commit();
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de la mise à jour de la commande", e);
            try {
                connection.rollback();
            } catch (SQLException ex) {
                LOGGER.log(Level.SEVERE, "Erreur lors du rollback", ex);
            }
            return null;
        } finally {
            try {
                connection.setAutoCommit(true);
            } catch (SQLException e) {
                LOGGER.log(Level.SEVERE, "Erreur lors du rétablissement de l'autocommit", e);
            }
        }
        
        return order;
    }
    
    @Override
    public boolean delete(Integer id) {
        // Utiliser une transaction pour garantir l'intégrité des données
        try {
            connection.setAutoCommit(false);
            
            // Supprimer les articles de la commande
            orderItemDAO.deleteByOrderId(id);
            
            // Supprimer la commande
            String query = "DELETE FROM commandes WHERE id = ?";
            
            try (PreparedStatement stmt = connection.prepareStatement(query)) {
                stmt.setInt(1, id);
                
                int affectedRows = stmt.executeUpdate();
                if (affectedRows == 0) {
                    throw new SQLException("La suppression de la commande a échoué, aucune ligne affectée.");
                }
            }
            
            // Valider la transaction
            connection.commit();
            return true;
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de la suppression de la commande", e);
            try {
                connection.rollback();
            } catch (SQLException ex) {
                LOGGER.log(Level.SEVERE, "Erreur lors du rollback", ex);
            }
            return false;
        } finally {
            try {
                connection.setAutoCommit(true);
            } catch (SQLException e) {
                LOGGER.log(Level.SEVERE, "Erreur lors du rétablissement de l'autocommit", e);
            }
        }
    }
    
    /**
     * Met à jour le statut d'une commande
     * @param id L'ID de la commande
     * @param status Le nouveau statut
     * @return true si la mise à jour a réussi, false sinon
     */
    public boolean updateStatus(int id, String status) {
        String query = "UPDATE commandes SET statut = ? WHERE id = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, status);
            stmt.setInt(2, id);
            
            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de la mise à jour du statut de la commande", e);
            return false;
        }
    }
    
    /**
     * Traite une livraison en mettant à jour le stock des produits
     * @param orderId L'ID de la commande
     * @return true si la livraison a été traitée avec succès, false sinon
     */
    public boolean processDelivery(int orderId) {
        // Récupérer la commande avec ses articles
        Order order = findById(orderId);
        if (order == null) {
            return false;
        }
        
        // Vérifier que la commande n'a pas déjà été livrée
        if ("delivered".equals(order.getStatut())) {
            return false;
        }
        
        // Utiliser une transaction pour garantir l'intégrité des données
        try {
            connection.setAutoCommit(false);
            
            // Mettre à jour le statut de la commande
            updateStatus(orderId, "delivered");
            
            // Mettre à jour le stock des produits
            ProductDAO productDAO = new ProductDAO();
            for (OrderItem item : order.getItems()) {
                productDAO.updateStock(item.getProduitId(), item.getQuantite());
            }
            
            // Valider la transaction
            connection.commit();
            return true;
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erreur lors du traitement de la livraison", e);
            try {
                connection.rollback();
            } catch (SQLException ex) {
                LOGGER.log(Level.SEVERE, "Erreur lors du rollback", ex);
            }
            return false;
        } finally {
            try {
                connection.setAutoCommit(true);
            } catch (SQLException e) {
                LOGGER.log(Level.SEVERE, "Erreur lors du rétablissement de l'autocommit", e);
            }
        }
    }
    
    /**
     * Génère une référence unique pour une commande
     * @return La référence générée
     */
    public String generateReference() {
        String prefix = "CMD";
        String timestamp = String.valueOf(System.currentTimeMillis()).substring(5);
        return prefix + timestamp;
    }
    
    /**
     * Convertit un ResultSet en objet Order
     * @param rs Le ResultSet contenant les données de la commande
     * @return L'objet Order créé
     * @throws SQLException En cas d'erreur SQL
     */
    private Order mapResultSetToOrder(ResultSet rs) throws SQLException {
        Order order = new Order();
        order.setId(rs.getInt("id"));
        
        // Gérer les valeurs NULL pour client_id
        int clientId = rs.getInt("client_id");
        if (!rs.wasNull()) {
            order.setClientId(clientId);
        }
        
        // Gérer les valeurs NULL pour pharmacy_id
        int pharmacyId = rs.getInt("pharmacy_id");
        if (!rs.wasNull()) {
            order.setPharmacyId(pharmacyId);
        }
        
        Timestamp dateCommande = rs.getTimestamp("date_commande");
        if (dateCommande != null) {
            order.setDateCommande(new java.util.Date(dateCommande.getTime()));
        }
        
        order.setStatut(rs.getString("statut"));
        order.setMontantTotal(rs.getBigDecimal("montant_total"));
        order.setReference(rs.getString("reference"));
        
        return order;
    }
}
