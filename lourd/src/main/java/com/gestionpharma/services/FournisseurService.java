package com.gestionpharma.services;

import com.gestionpharma.config.DatabaseConfigSimple;
import com.gestionpharma.models.Fournisseur;

import javax.swing.JOptionPane;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Service pour gérer les opérations liées aux fournisseurs
 */
public class FournisseurService {
    
    /**
     * Récupère tous les fournisseurs d'une pharmacie
     * @param pharmacieId ID de la pharmacie
     * @return Liste des fournisseurs
     */
    public List<Fournisseur> getAllFournisseurs(int pharmacieId) {
        List<Fournisseur> fournisseurs = new ArrayList<>();
        String query = "SELECT * FROM fournisseurs WHERE pharmacie_id = ?";
        
        try (Connection conn = DatabaseConfigSimple.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setInt(1, pharmacieId);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                Fournisseur fournisseur = new Fournisseur();
                fournisseur.setId(rs.getInt("id"));
                fournisseur.setNom(rs.getString("nom"));
                fournisseur.setAdresse(rs.getString("adresse"));
                fournisseur.setTelephone(rs.getString("telephone"));
                fournisseur.setEmail(rs.getString("email"));
                fournisseur.setSiret(rs.getString("siret"));
                
                fournisseurs.add(fournisseur);
            }
            
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Impossible de récupérer les fournisseurs : " + e.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
        }
        
        return fournisseurs;
    }
    
    /**
     * Ajoute un nouveau fournisseur
     * @param fournisseur Fournisseur à ajouter
     * @param pharmacieId ID de la pharmacie
     * @return true si l'ajout a réussi, false sinon
     */
    public boolean ajouterFournisseur(Fournisseur fournisseur, int pharmacieId) {
        String query = "INSERT INTO fournisseurs (nom, adresse, telephone, email, siret, pharmacie_id) " +
                       "VALUES (?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConfigSimple.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            
            pstmt.setString(1, fournisseur.getNom());
            pstmt.setString(2, fournisseur.getAdresse());
            pstmt.setString(3, fournisseur.getTelephone());
            pstmt.setString(4, fournisseur.getEmail());
            pstmt.setString(5, fournisseur.getSiret());
            pstmt.setInt(6, pharmacieId);
            
            int affectedRows = pstmt.executeUpdate();
            
            if (affectedRows > 0) {
                ResultSet generatedKeys = pstmt.getGeneratedKeys();
                if (generatedKeys.next()) {
                    fournisseur.setId(generatedKeys.getInt(1));
                    return true;
                }
            }
            
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Impossible d'ajouter le fournisseur : " + e.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
        }
        
        return false;
    }
    
    /**
     * Met à jour un fournisseur existant
     * @param fournisseur Fournisseur à mettre à jour
     * @return true si la mise à jour a réussi, false sinon
     */
    public boolean modifierFournisseur(Fournisseur fournisseur) {
        String query = "UPDATE fournisseurs SET nom = ?, adresse = ?, telephone = ?, email = ?, siret = ? " +
                       "WHERE id = ?";
        
        try (Connection conn = DatabaseConfigSimple.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setString(1, fournisseur.getNom());
            pstmt.setString(2, fournisseur.getAdresse());
            pstmt.setString(3, fournisseur.getTelephone());
            pstmt.setString(4, fournisseur.getEmail());
            pstmt.setString(5, fournisseur.getSiret());
            pstmt.setInt(6, fournisseur.getId());
            
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
            
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Impossible de modifier le fournisseur : " + e.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
        }
        
        return false;
    }
    
    /**
     * Supprime un fournisseur
     * @param fournisseurId ID du fournisseur à supprimer
     * @return true si la suppression a réussi, false sinon
     */
    public boolean supprimerFournisseur(int fournisseurId) {
        String query = "DELETE FROM fournisseurs WHERE id = ?";
        
        try (Connection conn = DatabaseConfigSimple.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setInt(1, fournisseurId);
            
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
            
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Impossible de supprimer le fournisseur : " + e.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
        }
        
        return false;
    }
    
    /**
     * Recherche des fournisseurs par nom
     * @param searchTerm Terme de recherche
     * @param pharmacieId ID de la pharmacie
     * @return Liste des fournisseurs correspondant à la recherche
     */
    public List<Fournisseur> rechercherFournisseurs(String searchTerm, int pharmacieId) {
        List<Fournisseur> fournisseurs = new ArrayList<>();
        String query = "SELECT * FROM fournisseurs WHERE pharmacie_id = ? AND " +
                      "(nom LIKE ? OR email LIKE ? OR siret LIKE ?)";
        
        try (Connection conn = DatabaseConfigSimple.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            String searchPattern = "%" + searchTerm + "%";
            pstmt.setInt(1, pharmacieId);
            pstmt.setString(2, searchPattern);
            pstmt.setString(3, searchPattern);
            pstmt.setString(4, searchPattern);
            
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                Fournisseur fournisseur = new Fournisseur();
                fournisseur.setId(rs.getInt("id"));
                fournisseur.setNom(rs.getString("nom"));
                fournisseur.setAdresse(rs.getString("adresse"));
                fournisseur.setTelephone(rs.getString("telephone"));
                fournisseur.setEmail(rs.getString("email"));
                fournisseur.setSiret(rs.getString("siret"));
                
                fournisseurs.add(fournisseur);
            }
            
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Impossible de rechercher les fournisseurs : " + e.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
        }
        
        return fournisseurs;
    }
    
    /**
     * Récupère un fournisseur par son ID
     * @param fournisseurId ID du fournisseur
     * @return Fournisseur trouvé ou null
     */
    public Fournisseur getFournisseurById(int fournisseurId) {
        String query = "SELECT * FROM fournisseurs WHERE id = ?";
        
        try (Connection conn = DatabaseConfigSimple.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setInt(1, fournisseurId);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                Fournisseur fournisseur = new Fournisseur();
                fournisseur.setId(rs.getInt("id"));
                fournisseur.setNom(rs.getString("nom"));
                fournisseur.setAdresse(rs.getString("adresse"));
                fournisseur.setTelephone(rs.getString("telephone"));
                fournisseur.setEmail(rs.getString("email"));
                fournisseur.setSiret(rs.getString("siret"));
                
                return fournisseur;
            }
            
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Impossible de récupérer le fournisseur : " + e.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
        }
        
        return null;
    }
    
    /**
     * Récupère tous les fournisseurs d'une pharmacie (alias pour getAllFournisseurs)
     * @param pharmacieId ID de la pharmacie
     * @return Liste des fournisseurs
     */
    public List<Fournisseur> getFournisseursByPharmacie(int pharmacieId) {
        return getAllFournisseurs(pharmacieId);
    }
    
    /**
     * Ajoute un nouveau fournisseur (alias pour ajouterFournisseur)
     * @param fournisseur Fournisseur à ajouter
     * @return true si l'ajout a réussi, false sinon
     */
    public boolean addFournisseur(Fournisseur fournisseur) {
        return ajouterFournisseur(fournisseur, fournisseur.getPharmacieId());
    }
    
    /**
     * Met à jour un fournisseur existant (alias pour modifierFournisseur)
     * @param fournisseur Fournisseur à mettre à jour
     * @return true si la mise à jour a réussi, false sinon
     */
    public boolean updateFournisseur(Fournisseur fournisseur) {
        return modifierFournisseur(fournisseur);
    }
    
    /**
     * Supprime un fournisseur (alias pour supprimerFournisseur)
     * @param fournisseurId ID du fournisseur à supprimer
     * @return true si la suppression a réussi, false sinon
     */
    public boolean deleteFournisseur(int fournisseurId) {
        return supprimerFournisseur(fournisseurId);
    }
}
