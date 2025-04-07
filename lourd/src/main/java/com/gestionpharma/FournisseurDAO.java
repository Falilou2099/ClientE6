package com.gestionpharma;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class FournisseurDAO {
    // Créer un nouveau fournisseur
    public Fournisseur creerFournisseur(Fournisseur fournisseur) {
        String sql = "INSERT INTO fournisseurs (nom, contact, telephone, email, types_medicaments) VALUES (?, ?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            pstmt.setString(1, fournisseur.getNom());
            pstmt.setString(2, fournisseur.getContact());
            pstmt.setString(3, fournisseur.getTelephone());
            pstmt.setString(4, fournisseur.getEmail());
            pstmt.setString(5, fournisseur.getTypesMedicaments());
            
            pstmt.executeUpdate();
            
            // Récupérer l'ID généré
            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    fournisseur.setId(generatedKeys.getInt(1));
                }
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la création du fournisseur : " + e.getMessage());
            e.printStackTrace();
        }
        
        return fournisseur;
    }

    // Obtenir tous les fournisseurs
    public List<Fournisseur> obtenirTousFournisseurs() {
        List<Fournisseur> fournisseurs = new ArrayList<>();
        String sql = "SELECT * FROM fournisseurs";
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                Fournisseur fournisseur = new Fournisseur();
                fournisseur.setId(rs.getInt("id"));
                fournisseur.setNom(rs.getString("nom"));
                fournisseur.setContact(rs.getString("contact"));
                fournisseur.setTelephone(rs.getString("telephone"));
                fournisseur.setEmail(rs.getString("email"));
                fournisseur.setTypesMedicaments(rs.getString("types_medicaments"));
                
                fournisseurs.add(fournisseur);
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération des fournisseurs : " + e.getMessage());
            e.printStackTrace();
        }
        
        return fournisseurs;
    }

    // Obtenir un fournisseur par son ID
    public Fournisseur obtenirFournisseurParId(int id) {
        Fournisseur fournisseur = null;
        String sql = "SELECT * FROM fournisseurs WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, id);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    fournisseur = new Fournisseur();
                    fournisseur.setId(rs.getInt("id"));
                    fournisseur.setNom(rs.getString("nom"));
                    fournisseur.setContact(rs.getString("contact"));
                    fournisseur.setTelephone(rs.getString("telephone"));
                    fournisseur.setEmail(rs.getString("email"));
                    fournisseur.setTypesMedicaments(rs.getString("types_medicaments"));
                }
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération du fournisseur : " + e.getMessage());
            e.printStackTrace();
        }
        
        return fournisseur;
    }

    // Modifier un fournisseur existant
    public void modifierFournisseur(Fournisseur fournisseur) {
        String sql = "UPDATE fournisseurs SET nom = ?, contact = ?, telephone = ?, email = ?, types_medicaments = ? WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, fournisseur.getNom());
            pstmt.setString(2, fournisseur.getContact());
            pstmt.setString(3, fournisseur.getTelephone());
            pstmt.setString(4, fournisseur.getEmail());
            pstmt.setString(5, fournisseur.getTypesMedicaments());
            pstmt.setInt(6, fournisseur.getId());
            
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Erreur lors de la modification du fournisseur : " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Supprimer un fournisseur
    public void supprimerFournisseur(int id) {
        String sql = "DELETE FROM fournisseurs WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Erreur lors de la suppression du fournisseur : " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Rechercher des fournisseurs par type de médicaments
    public List<Fournisseur> rechercherFournisseursParTypeMedicament(String typeMedicament) {
        List<Fournisseur> fournisseurs = new ArrayList<>();
        String sql = "SELECT * FROM fournisseurs WHERE types_medicaments LIKE ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, "%" + typeMedicament + "%");
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Fournisseur fournisseur = new Fournisseur();
                    fournisseur.setId(rs.getInt("id"));
                    fournisseur.setNom(rs.getString("nom"));
                    fournisseur.setContact(rs.getString("contact"));
                    fournisseur.setTelephone(rs.getString("telephone"));
                    fournisseur.setEmail(rs.getString("email"));
                    fournisseur.setTypesMedicaments(rs.getString("types_medicaments"));
                    
                    fournisseurs.add(fournisseur);
                }
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la recherche des fournisseurs : " + e.getMessage());
            e.printStackTrace();
        }
        
        return fournisseurs;
    }
}
