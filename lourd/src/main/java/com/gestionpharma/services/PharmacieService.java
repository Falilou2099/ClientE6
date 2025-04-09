package com.gestionpharma.services;

import com.gestionpharma.config.DatabaseConfig;
import com.gestionpharma.models.Pharmacie;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Service pour gérer les opérations liées aux pharmacies
 */
public class PharmacieService {
    
    /**
     * Récupère une pharmacie par son identifiant
     * @param id L'identifiant de la pharmacie
     * @return La pharmacie correspondant à l'identifiant
     */
    public Pharmacie getPharmacieById(int id) {
        Pharmacie pharmacie = null;
        String sql = "SELECT * FROM pharmacies WHERE id = ?";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, id);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    pharmacie = new Pharmacie(
                        rs.getInt("id"),
                        rs.getString("nom"),
                        rs.getString("adresse"),
                        rs.getString("telephone"),
                        rs.getString("email"),
                        rs.getString("siret"),
                        "Lundi-Vendredi: 9h-19h, Samedi: 9h-12h" // Valeur par défaut pour horaires
                    );
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération de la pharmacie: " + e.getMessage());
            e.printStackTrace();
        }
        
        return pharmacie;
    }
    
    /**
     * Récupère toutes les pharmacies
     * @return La liste des pharmacies
     */
    public List<Pharmacie> getAllPharmacies() {
        List<Pharmacie> pharmacies = new ArrayList<>();
        String sql = "SELECT * FROM pharmacies";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            
            while (rs.next()) {
                Pharmacie pharmacie = new Pharmacie(
                    rs.getInt("id"),
                    rs.getString("nom"),
                    rs.getString("adresse"),
                    rs.getString("telephone"),
                    rs.getString("email"),
                    rs.getString("siret"),
                    "Lundi-Vendredi: 9h-19h, Samedi: 9h-12h" // Valeur par défaut pour horaires
                );
                pharmacies.add(pharmacie);
            }
            
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération des pharmacies: " + e.getMessage());
            e.printStackTrace();
        }
        
        return pharmacies;
    }
    
    /**
     * Met à jour les informations d'une pharmacie
     * @param pharmacie La pharmacie avec les nouvelles informations
     * @return true si la mise à jour a réussi, false sinon
     */
    public boolean updatePharmacie(Pharmacie pharmacie) {
        String sql = "UPDATE pharmacies SET nom = ?, adresse = ?, telephone = ?, email = ?, siret = ? WHERE id = ?";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, pharmacie.getNom());
            pstmt.setString(2, pharmacie.getAdresse());
            pstmt.setString(3, pharmacie.getTelephone());
            pstmt.setString(4, pharmacie.getEmail());
            pstmt.setString(5, pharmacie.getSiret());
            pstmt.setInt(6, pharmacie.getId());
            
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            System.err.println("Erreur lors de la mise à jour de la pharmacie: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Ajoute une nouvelle pharmacie
     * @param pharmacie La pharmacie à ajouter
     * @return L'identifiant de la pharmacie ajoutée, -1 en cas d'erreur
     */
    public int addPharmacie(Pharmacie pharmacie) {
        String sql = "INSERT INTO pharmacies (nom, adresse, telephone, email, siret) VALUES (?, ?, ?, ?, ?) RETURNING id";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, pharmacie.getNom());
            pstmt.setString(2, pharmacie.getAdresse());
            pstmt.setString(3, pharmacie.getTelephone());
            pstmt.setString(4, pharmacie.getEmail());
            pstmt.setString(5, pharmacie.getSiret());
            pstmt.setString(6, pharmacie.getHoraires());
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Erreur lors de l'ajout de la pharmacie: " + e.getMessage());
            e.printStackTrace();
        }
        
        return -1;
    }
    
    /**
     * Supprime une pharmacie
     * @param id L'identifiant de la pharmacie à supprimer
     * @return true si la suppression a réussi, false sinon
     */
    public boolean deletePharmacie(int id) {
        String sql = "DELETE FROM pharmacies WHERE id = ?";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, id);
            
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            System.err.println("Erreur lors de la suppression de la pharmacie: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}
