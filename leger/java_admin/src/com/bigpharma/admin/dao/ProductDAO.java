package com.bigpharma.admin.dao;

import com.bigpharma.admin.models.Product;
import com.bigpharma.admin.utils.DatabaseConnection;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Classe DAO pour les opérations sur les produits
 */
public class ProductDAO implements DAO<Product, Integer> {
    private static final Logger LOGGER = Logger.getLogger(ProductDAO.class.getName());
    private final Connection connection;
    
    public ProductDAO() {
        this.connection = DatabaseConnection.getInstance().getConnection();
    }
    
    @Override
    public Product findById(Integer id) {
        String query = "SELECT * FROM produits WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return mapResultSetToProduct(rs);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de la recherche du produit par ID", e);
        }
        return null;
    }
    
    @Override
    public List<Product> findAll() {
        return findAllByPharmacyId(null);
    }
    
    /**
     * Récupère tous les produits d'une pharmacie
     * @param pharmacyId L'ID de la pharmacie ou null pour tous les produits
     * @return Liste des produits
     */
    public List<Product> findAllByPharmacyId(Integer pharmacyId) {
        List<Product> products = new ArrayList<>();
        String query = pharmacyId != null 
            ? "SELECT * FROM produits WHERE pharmacy_id = ? ORDER BY nom"
            : "SELECT * FROM produits ORDER BY nom";
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            if (pharmacyId != null) {
                stmt.setInt(1, pharmacyId);
            }
            
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                products.add(mapResultSetToProduct(rs));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de la récupération des produits", e);
        }
        
        return products;
    }
    
    @Override
    public Product save(Product product) {
        String query = "INSERT INTO produits (nom, description, prix, quantite_stock, categorie, " +
                      "categorie_id, fournisseur_id, est_ordonnance, image, date_ajout, " +
                      "date_modification, pharmacy_id) " +
                      "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (PreparedStatement stmt = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            setProductParameters(stmt, product);
            
            int affectedRows = stmt.executeUpdate();
            
            if (affectedRows == 0) {
                throw new SQLException("La création du produit a échoué, aucune ligne affectée.");
            }
            
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    product.setId(generatedKeys.getInt(1));
                } else {
                    throw new SQLException("La création du produit a échoué, aucun ID obtenu.");
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de la sauvegarde du produit", e);
            return null;
        }
        
        return product;
    }
    
    @Override
    public Product update(Product product) {
        String query = "UPDATE produits SET nom = ?, description = ?, prix = ?, quantite_stock = ?, " +
                      "categorie = ?, categorie_id = ?, fournisseur_id = ?, est_ordonnance = ?, " +
                      "image = ?, date_modification = ?, pharmacy_id = ? WHERE id = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            setProductParameters(stmt, product);
            stmt.setInt(12, product.getId());
            
            int affectedRows = stmt.executeUpdate();
            
            if (affectedRows == 0) {
                throw new SQLException("La mise à jour du produit a échoué, aucune ligne affectée.");
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de la mise à jour du produit", e);
            return null;
        }
        
        return product;
    }
    
    @Override
    public boolean delete(Integer id) {
        String query = "DELETE FROM produits WHERE id = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, id);
            
            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de la suppression du produit", e);
            return false;
        }
    }
    
    /**
     * Met à jour le stock d'un produit
     * @param productId L'ID du produit
     * @param quantity La quantité à ajouter (positif) ou à soustraire (négatif)
     * @return true si la mise à jour a réussi, false sinon
     */
    public boolean updateStock(int productId, int quantity) {
        String query = "UPDATE produits SET quantite_stock = quantite_stock + ?, " +
                      "date_modification = ? WHERE id = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, quantity);
            stmt.setTimestamp(2, new Timestamp(System.currentTimeMillis()));
            stmt.setInt(3, productId);
            
            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de la mise à jour du stock", e);
            return false;
        }
    }
    
    /**
     * Recherche des produits par nom
     * @param name Le nom à rechercher (recherche partielle)
     * @param pharmacyId L'ID de la pharmacie ou null pour toutes les pharmacies
     * @return Liste des produits correspondants
     */
    public List<Product> findByName(String name, Integer pharmacyId) {
        List<Product> products = new ArrayList<>();
        String query = pharmacyId != null 
            ? "SELECT * FROM produits WHERE nom LIKE ? AND pharmacy_id = ? ORDER BY nom"
            : "SELECT * FROM produits WHERE nom LIKE ? ORDER BY nom";
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, "%" + name + "%");
            if (pharmacyId != null) {
                stmt.setInt(2, pharmacyId);
            }
            
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                products.add(mapResultSetToProduct(rs));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de la recherche de produits par nom", e);
        }
        
        return products;
    }
    
    /**
     * Récupère les produits par catégorie
     * @param category La catégorie à rechercher
     * @param pharmacyId L'ID de la pharmacie ou null pour toutes les pharmacies
     * @return Liste des produits correspondants
     */
    public List<Product> findByCategory(String category, Integer pharmacyId) {
        List<Product> products = new ArrayList<>();
        String query = pharmacyId != null 
            ? "SELECT * FROM produits WHERE categorie = ? AND pharmacy_id = ? ORDER BY nom"
            : "SELECT * FROM produits WHERE categorie = ? ORDER BY nom";
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, category);
            if (pharmacyId != null) {
                stmt.setInt(2, pharmacyId);
            }
            
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                products.add(mapResultSetToProduct(rs));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de la recherche de produits par catégorie", e);
        }
        
        return products;
    }
    
    /**
     * Récupère toutes les catégories distinctes
     * @param pharmacyId L'ID de la pharmacie ou null pour toutes les pharmacies
     * @return Liste des catégories
     */
    public List<String> getAllCategories(Integer pharmacyId) {
        List<String> categories = new ArrayList<>();
        String query = pharmacyId != null 
            ? "SELECT DISTINCT categorie FROM produits WHERE pharmacy_id = ? ORDER BY categorie"
            : "SELECT DISTINCT categorie FROM produits ORDER BY categorie";
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            if (pharmacyId != null) {
                stmt.setInt(1, pharmacyId);
            }
            
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                String category = rs.getString("categorie");
                if (category != null && !category.isEmpty()) {
                    categories.add(category);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de la récupération des catégories", e);
        }
        
        return categories;
    }
    
    /**
     * Récupère les produits à faible stock
     * @param threshold Le seuil de stock minimum
     * @param pharmacyId L'ID de la pharmacie ou null pour toutes les pharmacies
     * @return Liste des produits à faible stock
     */
    public List<Product> getLowStockProducts(int threshold, Integer pharmacyId) {
        List<Product> products = new ArrayList<>();
        String query = pharmacyId != null 
            ? "SELECT * FROM produits WHERE quantite_stock < ? AND pharmacy_id = ? ORDER BY quantite_stock"
            : "SELECT * FROM produits WHERE quantite_stock < ? ORDER BY quantite_stock";
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, threshold);
            if (pharmacyId != null) {
                stmt.setInt(2, pharmacyId);
            }
            
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                products.add(mapResultSetToProduct(rs));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de la récupération des produits à faible stock", e);
        }
        
        return products;
    }
    
    /**
     * Définit les paramètres d'un produit dans une requête préparée
     * @param stmt La requête préparée
     * @param product Le produit
     * @throws SQLException En cas d'erreur SQL
     */
    private void setProductParameters(PreparedStatement stmt, Product product) throws SQLException {
        stmt.setString(1, product.getNom());
        stmt.setString(2, product.getDescription());
        stmt.setBigDecimal(3, product.getPrix());
        stmt.setInt(4, product.getQuantiteStock());
        stmt.setString(5, product.getCategorie());
        
        if (product.getCategorieId() != null) {
            stmt.setInt(6, product.getCategorieId());
        } else {
            stmt.setNull(6, Types.INTEGER);
        }
        
        if (product.getFournisseurId() != null) {
            stmt.setInt(7, product.getFournisseurId());
        } else {
            stmt.setNull(7, Types.INTEGER);
        }
        
        stmt.setBoolean(8, product.getEstOrdonnance());
        stmt.setString(9, product.getImage());
        
        // Date d'ajout (utiliser la date actuelle si null)
        Timestamp dateAjout = product.getDateAjout() != null 
            ? new Timestamp(product.getDateAjout().getTime()) 
            : new Timestamp(System.currentTimeMillis());
        stmt.setTimestamp(10, dateAjout);
        
        // Date de modification (toujours la date actuelle)
        stmt.setTimestamp(11, new Timestamp(System.currentTimeMillis()));
        
        if (product.getPharmacyId() != null) {
            stmt.setInt(12, product.getPharmacyId());
        } else {
            stmt.setNull(12, Types.INTEGER);
        }
    }
    
    /**
     * Convertit un ResultSet en objet Product
     * @param rs Le ResultSet contenant les données du produit
     * @return L'objet Product créé
     * @throws SQLException En cas d'erreur SQL
     */
    private Product mapResultSetToProduct(ResultSet rs) throws SQLException {
        Product product = new Product();
        product.setId(rs.getInt("id"));
        product.setNom(rs.getString("nom"));
        product.setDescription(rs.getString("description"));
        product.setPrix(rs.getBigDecimal("prix"));
        product.setQuantiteStock(rs.getInt("quantite_stock"));
        product.setCategorie(rs.getString("categorie"));
        
        // Gérer les valeurs NULL pour categorie_id
        int categorieId = rs.getInt("categorie_id");
        if (!rs.wasNull()) {
            product.setCategorieId(categorieId);
        }
        
        // Gérer les valeurs NULL pour fournisseur_id
        int fournisseurId = rs.getInt("fournisseur_id");
        if (!rs.wasNull()) {
            product.setFournisseurId(fournisseurId);
        }
        
        product.setEstOrdonnance(rs.getBoolean("est_ordonnance"));
        product.setImage(rs.getString("image"));
        
        Timestamp dateAjout = rs.getTimestamp("date_ajout");
        if (dateAjout != null) {
            product.setDateAjout(new java.util.Date(dateAjout.getTime()));
        }
        
        Timestamp dateModification = rs.getTimestamp("date_modification");
        if (dateModification != null) {
            product.setDateModification(new java.util.Date(dateModification.getTime()));
        }
        
        // Gérer les valeurs NULL pour pharmacy_id
        int pharmacyId = rs.getInt("pharmacy_id");
        if (!rs.wasNull()) {
            product.setPharmacyId(pharmacyId);
        }
        
        return product;
    }
}
