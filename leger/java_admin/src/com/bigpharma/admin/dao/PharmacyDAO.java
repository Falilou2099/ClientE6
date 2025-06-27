package com.bigpharma.admin.dao;

import com.bigpharma.admin.models.Pharmacy;
import com.bigpharma.admin.utils.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Classe DAO pour les opérations sur les pharmacies
 */
public class PharmacyDAO implements DAO<Pharmacy, Integer> {
    private static final Logger LOGGER = Logger.getLogger(PharmacyDAO.class.getName());
    private final Connection connection;
    
    public PharmacyDAO() {
        this.connection = DatabaseConnection.getInstance().getConnection();
    }
    
    @Override
    public Pharmacy findById(Integer id) {
        String query = "SELECT * FROM pharmacies WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return mapResultSetToPharmacy(rs);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de la recherche de la pharmacie par ID", e);
        }
        return null;
    }
    
    @Override
    public List<Pharmacy> findAll() {
        List<Pharmacy> pharmacies = new ArrayList<>();
        String query = "SELECT * FROM pharmacies ORDER BY nom";
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                pharmacies.add(mapResultSetToPharmacy(rs));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de la récupération de toutes les pharmacies", e);
        }
        
        return pharmacies;
    }
    
    @Override
    public Pharmacy save(Pharmacy pharmacy) {
        String query = "INSERT INTO pharmacies (nom, adresse, telephone, email, numero_enregistrement, " +
                      "statut, date_creation) VALUES (?, ?, ?, ?, ?, ?, ?)";
        
        try (PreparedStatement stmt = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, pharmacy.getNom());
            stmt.setString(2, pharmacy.getAdresse());
            stmt.setString(3, pharmacy.getTelephone());
            stmt.setString(4, pharmacy.getEmail());
            stmt.setString(5, pharmacy.getNumeroEnregistrement());
            stmt.setString(6, pharmacy.getStatut());
            
            // Utiliser la date actuelle si non spécifiée
            Timestamp dateCreation = pharmacy.getDateCreation() != null 
                ? new Timestamp(pharmacy.getDateCreation().getTime()) 
                : new Timestamp(System.currentTimeMillis());
            stmt.setTimestamp(7, dateCreation);
            
            int affectedRows = stmt.executeUpdate();
            
            if (affectedRows == 0) {
                throw new SQLException("La création de la pharmacie a échoué, aucune ligne affectée.");
            }
            
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    pharmacy.setId(generatedKeys.getInt(1));
                } else {
                    throw new SQLException("La création de la pharmacie a échoué, aucun ID obtenu.");
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de la sauvegarde de la pharmacie", e);
            return null;
        }
        
        return pharmacy;
    }
    
    @Override
    public Pharmacy update(Pharmacy pharmacy) {
        String query = "UPDATE pharmacies SET nom = ?, adresse = ?, telephone = ?, email = ?, " +
                      "numero_enregistrement = ?, statut = ? WHERE id = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, pharmacy.getNom());
            stmt.setString(2, pharmacy.getAdresse());
            stmt.setString(3, pharmacy.getTelephone());
            stmt.setString(4, pharmacy.getEmail());
            stmt.setString(5, pharmacy.getNumeroEnregistrement());
            stmt.setString(6, pharmacy.getStatut());
            stmt.setInt(7, pharmacy.getId());
            
            int affectedRows = stmt.executeUpdate();
            
            if (affectedRows == 0) {
                throw new SQLException("La mise à jour de la pharmacie a échoué, aucune ligne affectée.");
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de la mise à jour de la pharmacie", e);
            return null;
        }
        
        return pharmacy;
    }
    
    @Override
    public boolean delete(Integer id) {
        String query = "DELETE FROM pharmacies WHERE id = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, id);
            
            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de la suppression de la pharmacie", e);
            return false;
        }
    }
    
    /**
     * Recherche une pharmacie par son nom
     * @param name Le nom à rechercher (recherche partielle)
     * @return Liste des pharmacies correspondantes
     */
    public List<Pharmacy> findByName(String name) {
        List<Pharmacy> pharmacies = new ArrayList<>();
        String query = "SELECT * FROM pharmacies WHERE nom LIKE ? ORDER BY nom";
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, "%" + name + "%");
            
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                pharmacies.add(mapResultSetToPharmacy(rs));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de la recherche de pharmacies par nom", e);
        }
        
        return pharmacies;
    }
    
    /**
     * Récupère les pharmacies actives
     * @return Liste des pharmacies actives
     */
    public List<Pharmacy> findActivePharmacies() {
        List<Pharmacy> pharmacies = new ArrayList<>();
        String query = "SELECT * FROM pharmacies WHERE statut = 'active' ORDER BY nom";
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                pharmacies.add(mapResultSetToPharmacy(rs));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de la récupération des pharmacies actives", e);
        }
        
        return pharmacies;
    }
    
    /**
     * Change le statut d'une pharmacie
     * @param id L'ID de la pharmacie
     * @param status Le nouveau statut ('active', 'pending', 'suspended')
     * @return true si la mise à jour a réussi, false sinon
     */
    public boolean updateStatus(int id, String status) {
        String query = "UPDATE pharmacies SET statut = ? WHERE id = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, status);
            stmt.setInt(2, id);
            
            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de la mise à jour du statut de la pharmacie", e);
            return false;
        }
    }
    
    /**
     * Convertit un ResultSet en objet Pharmacy
     * @param rs Le ResultSet contenant les données de la pharmacie
     * @return L'objet Pharmacy créé
     * @throws SQLException En cas d'erreur SQL
     */
    private Pharmacy mapResultSetToPharmacy(ResultSet rs) throws SQLException {
        Pharmacy pharmacy = new Pharmacy();
        pharmacy.setId(rs.getInt("id"));
        pharmacy.setNom(rs.getString("nom"));
        pharmacy.setAdresse(rs.getString("adresse"));
        pharmacy.setTelephone(rs.getString("telephone"));
        pharmacy.setEmail(rs.getString("email"));
        pharmacy.setNumeroEnregistrement(rs.getString("numero_enregistrement"));
        pharmacy.setStatut(rs.getString("statut"));
        
        Timestamp dateCreation = rs.getTimestamp("date_creation");
        if (dateCreation != null) {
            pharmacy.setDateCreation(new java.util.Date(dateCreation.getTime()));
        }
        
        return pharmacy;
    }
}
