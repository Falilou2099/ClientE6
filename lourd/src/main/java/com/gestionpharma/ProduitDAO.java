package com.gestionpharma;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProduitDAO {
    // Créer un nouveau produit
    public void creerProduit(Produit produit) {
        String sql = "INSERT INTO produits (nom, description, prix, quantite_stock, categorie) VALUES (?, ?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            pstmt.setString(1, produit.getNom());
            pstmt.setString(2, produit.getDescription());
            pstmt.setDouble(3, produit.getPrix());
            pstmt.setInt(4, produit.getQuantiteStock());
            pstmt.setString(5, produit.getCategorie());
            
            pstmt.executeUpdate();
            
            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    produit.setId(generatedKeys.getInt(1));
                }
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la création du produit : " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Récupérer tous les produits
    public List<Produit> obtenirTousProduits() {
        List<Produit> produits = new ArrayList<>();
        String sql = "SELECT * FROM produits";
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                Produit produit = new Produit();
                produit.setId(rs.getInt("id"));
                produit.setNom(rs.getString("nom"));
                produit.setDescription(rs.getString("description"));
                produit.setPrix(rs.getDouble("prix"));
                produit.setQuantiteStock(rs.getInt("quantite_stock"));
                produit.setCategorie(rs.getString("categorie"));
                
                produits.add(produit);
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération des produits : " + e.getMessage());
            e.printStackTrace();
        }
        
        return produits;
    }

    // Mettre à jour un produit
    public void mettreAJourProduit(Produit produit) {
        String sql = "UPDATE produits SET nom=?, description=?, prix=?, quantite_stock=?, categorie=? WHERE id=?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, produit.getNom());
            pstmt.setString(2, produit.getDescription());
            pstmt.setDouble(3, produit.getPrix());
            pstmt.setInt(4, produit.getQuantiteStock());
            pstmt.setString(5, produit.getCategorie());
            pstmt.setInt(6, produit.getId());
            
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Erreur lors de la mise à jour du produit : " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Supprimer un produit
    public void supprimerProduit(int id) {
        String sql = "DELETE FROM produits WHERE id=?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Erreur lors de la suppression du produit : " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Rechercher un produit par ID
    public Produit obtenirProduitParId(int id) {
        String sql = "SELECT * FROM produits WHERE id=?";
        Produit produit = null;
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    produit = new Produit();
                    produit.setId(rs.getInt("id"));
                    produit.setNom(rs.getString("nom"));
                    produit.setDescription(rs.getString("description"));
                    produit.setPrix(rs.getDouble("prix"));
                    produit.setQuantiteStock(rs.getInt("quantite_stock"));
                    produit.setCategorie(rs.getString("categorie"));
                }
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération du produit : " + e.getMessage());
            e.printStackTrace();
        }
        
        return produit;
    }

    // Rechercher un produit par nom
    public Produit obtenirProduitParNom(String nom) {
        String sql = "SELECT * FROM produits WHERE nom = ?";
        Produit produit = null;
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, nom);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    produit = new Produit();
                    produit.setId(rs.getInt("id"));
                    produit.setNom(rs.getString("nom"));
                    produit.setDescription(rs.getString("description"));
                    produit.setPrix(rs.getDouble("prix"));
                    produit.setQuantiteStock(rs.getInt("quantite_stock"));
                    produit.setCategorie(rs.getString("categorie"));
                }
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération du produit : " + e.getMessage());
            e.printStackTrace();
        }
        
        return produit;
    }

    // Méthode pour obtenir les catégories
    public List<String> obtenirCategories() {
        List<String> categories = new ArrayList<>();
        String sql = "SELECT nom FROM categories";
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                categories.add(rs.getString("nom"));
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération des catégories : " + e.getMessage());
            e.printStackTrace();
        }
        
        return categories;
    }
}
