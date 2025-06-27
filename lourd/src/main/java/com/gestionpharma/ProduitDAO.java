package com.gestionpharma;

import java.sql.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ProduitDAO {
    // Créer un nouveau produit
    public void creerProduit(Produit produit) {
        // Vérifier si les colonnes prix_achat et date_expiration existent
        verifierEtCreerColonnes();
        
        String sql = "INSERT INTO produits (nom, description, prix, prix_achat, quantite_stock, categorie, date_expiration) VALUES (?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            pstmt.setString(1, produit.getNom());
            pstmt.setString(2, produit.getDescription());
            pstmt.setDouble(3, produit.getPrix());
            pstmt.setDouble(4, produit.getPrixAchat());
            pstmt.setInt(5, produit.getQuantiteStock());
            pstmt.setString(6, produit.getCategorie());
            
            // Gérer la date d'expiration (peut être null)
            if (produit.getDateExpiration() != null) {
                pstmt.setDate(7, new java.sql.Date(produit.getDateExpiration().getTime()));
            } else {
                pstmt.setNull(7, Types.DATE);
            }
            
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
        // Vérifier si les colonnes prix_achat et date_expiration existent
        verifierEtCreerColonnes();
        
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
                
                // Récupérer le prix d'achat s'il existe
                try {
                    produit.setPrixAchat(rs.getDouble("prix_achat"));
                } catch (SQLException e) {
                    // Si la colonne n'existe pas, utiliser le prix de vente par défaut
                    produit.setPrixAchat(rs.getDouble("prix"));
                }
                
                produit.setQuantiteStock(rs.getInt("quantite_stock"));
                produit.setCategorie(rs.getString("categorie"));
                
                // Récupérer la date d'expiration si elle existe
                try {
                    java.sql.Date dateSQL = rs.getDate("date_expiration");
                    if (dateSQL != null) {
                        produit.setDateExpiration(new Date(dateSQL.getTime()));
                    }
                } catch (SQLException e) {
                    // Ignorer si la colonne n'existe pas
                }
                
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
        // Vérifier si les colonnes prix_achat et date_expiration existent
        verifierEtCreerColonnes();
        
        String sql = "UPDATE produits SET nom=?, description=?, prix=?, prix_achat=?, quantite_stock=?, categorie=?, date_expiration=? WHERE id=?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, produit.getNom());
            pstmt.setString(2, produit.getDescription());
            pstmt.setDouble(3, produit.getPrix());
            pstmt.setDouble(4, produit.getPrixAchat());
            pstmt.setInt(5, produit.getQuantiteStock());
            pstmt.setString(6, produit.getCategorie());
            
            // Gérer la date d'expiration (peut être null)
            if (produit.getDateExpiration() != null) {
                pstmt.setDate(7, new java.sql.Date(produit.getDateExpiration().getTime()));
            } else {
                pstmt.setNull(7, Types.DATE);
            }
            
            pstmt.setInt(8, produit.getId());
            
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
        // Vérifier si les colonnes prix_achat et date_expiration existent
        verifierEtCreerColonnes();
        
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
                    
                    // Récupérer le prix d'achat s'il existe
                    try {
                        produit.setPrixAchat(rs.getDouble("prix_achat"));
                    } catch (SQLException e) {
                        // Si la colonne n'existe pas, utiliser le prix de vente par défaut
                        produit.setPrixAchat(rs.getDouble("prix"));
                    }
                    
                    produit.setQuantiteStock(rs.getInt("quantite_stock"));
                    produit.setCategorie(rs.getString("categorie"));
                    
                    // Récupérer la date d'expiration si elle existe
                    try {
                        java.sql.Date dateSQL = rs.getDate("date_expiration");
                        if (dateSQL != null) {
                            produit.setDateExpiration(new Date(dateSQL.getTime()));
                        }
                    } catch (SQLException e) {
                        // Ignorer si la colonne n'existe pas
                    }
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
        // Vérifier si les colonnes prix_achat et date_expiration existent
        verifierEtCreerColonnes();
        
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
                    
                    // Récupérer le prix d'achat s'il existe
                    try {
                        produit.setPrixAchat(rs.getDouble("prix_achat"));
                    } catch (SQLException e) {
                        // Si la colonne n'existe pas, utiliser le prix de vente par défaut
                        produit.setPrixAchat(rs.getDouble("prix"));
                    }
                    
                    produit.setQuantiteStock(rs.getInt("quantite_stock"));
                    produit.setCategorie(rs.getString("categorie"));
                    
                    // Récupérer la date d'expiration si elle existe
                    try {
                        java.sql.Date dateSQL = rs.getDate("date_expiration");
                        if (dateSQL != null) {
                            produit.setDateExpiration(new Date(dateSQL.getTime()));
                        }
                    } catch (SQLException e) {
                        // Ignorer si la colonne n'existe pas
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération du produit : " + e.getMessage());
            e.printStackTrace();
        }
        
        return produit;
    }

    // Vérifier si les colonnes prix_achat et date_expiration existent, et les créer si nécessaire
    private void verifierEtCreerColonnes() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            DatabaseMetaData meta = conn.getMetaData();
            ResultSet columns = meta.getColumns(null, null, "produits", "prix_achat");
            
            // Vérifier si la colonne prix_achat existe
            if (!columns.next()) {
                // La colonne n'existe pas, la créer
                try (Statement stmt = conn.createStatement()) {
                    stmt.executeUpdate("ALTER TABLE produits ADD COLUMN prix_achat DOUBLE DEFAULT 0");
                    System.out.println("Colonne prix_achat ajoutée à la table produits");
                }
            }
            
            // Vérifier si la colonne date_expiration existe
            columns = meta.getColumns(null, null, "produits", "date_expiration");
            if (!columns.next()) {
                // La colonne n'existe pas, la créer
                try (Statement stmt = conn.createStatement()) {
                    stmt.executeUpdate("ALTER TABLE produits ADD COLUMN date_expiration DATE");
                    System.out.println("Colonne date_expiration ajoutée à la table produits");
                }
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la vérification ou création des colonnes : " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    // Méthode pour obtenir les catégories
    public List<String> obtenirCategories() {
        List<String> categories = new ArrayList<>();
        String sql = "SELECT name FROM categories";
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                categories.add(rs.getString("name"));
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération des catégories : " + e.getMessage());
            e.printStackTrace();
        }
        
        // Si aucune catégorie n'est trouvée, créer la table et ajouter des catégories par défaut
        if (categories.isEmpty()) {
            try (Connection conn = DatabaseConnection.getConnection()) {
                // Vérifier si la table existe
                DatabaseMetaData meta = conn.getMetaData();
                ResultSet tables = meta.getTables(null, null, "categories", null);
                
                if (!tables.next()) {
                    // La table n'existe pas, la créer
                    try (Statement stmt = conn.createStatement()) {
                        stmt.executeUpdate("CREATE TABLE IF NOT EXISTS categories (id INT AUTO_INCREMENT PRIMARY KEY, name VARCHAR(100) NOT NULL UNIQUE)");
                    }
                }
                
                // Ajouter des catégories par défaut
                String[] defaultCategories = {
                    "Analgésiques", "Anti-inflammatoires", "Antibiotiques", "Antihistaminiques",
                    "Gastro-entérologie", "Dermatologie", "Cardiologie", "Vitamines",
                    "Compléments alimentaires", "Homéopathie", "Hygiène", "Premiers soins",
                    "Ophtalmologie", "ORL", "Contraception", "Nutrition", "Autres"
                };
                
                try (PreparedStatement pstmt = conn.prepareStatement("INSERT IGNORE INTO categories (name) VALUES (?)")) {
                    for (String categorie : defaultCategories) {
                        pstmt.setString(1, categorie);
                        pstmt.executeUpdate();
                        categories.add(categorie);
                    }
                }
            } catch (SQLException e) {
                System.err.println("Erreur lors de la création des catégories par défaut : " + e.getMessage());
                e.printStackTrace();
            }
        }
        
        return categories;
    }
}
