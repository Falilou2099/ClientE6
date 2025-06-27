package com.bigpharma.admin.dao;

import com.bigpharma.admin.models.User;
import com.bigpharma.admin.utils.DatabaseConnection;
import com.bigpharma.admin.utils.SecurityUtils;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Classe DAO pour les opérations sur les utilisateurs
 */
public class UserDAO implements DAO<User, Integer> {
    private static final Logger LOGGER = Logger.getLogger(UserDAO.class.getName());
    private final Connection connection;
    
    public UserDAO() {
        this.connection = DatabaseConnection.getInstance().getConnection();
    }
    
    @Override
    public User findById(Integer id) {
        String query = "SELECT * FROM users WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return mapResultSetToUser(rs);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de la recherche de l'utilisateur par ID", e);
        }
        return null;
    }
    
    @Override
    public List<User> findAll() {
        List<User> users = new ArrayList<>();
        String query = "SELECT * FROM users ORDER BY email";
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                users.add(mapResultSetToUser(rs));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de la récupération de tous les utilisateurs", e);
        }
        
        return users;
    }
    
    @Override
    public User save(User user) {
        String query = "INSERT INTO users (email, password, pharmacy_id, role, status, created_at, app_access) " +
                      "VALUES (?, ?, ?, ?, ?, ?, ?)";
        
        try (PreparedStatement stmt = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, user.getEmail());
            stmt.setString(2, user.getPassword());
            
            if (user.getPharmacyId() != null) {
                stmt.setInt(3, user.getPharmacyId());
            } else {
                stmt.setNull(3, Types.INTEGER);
            }
            
            stmt.setString(4, user.getRole());
            stmt.setString(5, user.getStatus());
            
            // Utiliser la date actuelle si non spécifiée
            Timestamp createdAt = user.getCreatedAt() != null 
                ? new Timestamp(user.getCreatedAt().getTime()) 
                : new Timestamp(System.currentTimeMillis());
            stmt.setTimestamp(6, createdAt);
            
            stmt.setString(7, user.getAppAccess());
            
            int affectedRows = stmt.executeUpdate();
            
            if (affectedRows == 0) {
                throw new SQLException("La création de l'utilisateur a échoué, aucune ligne affectée.");
            }
            
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    user.setId(generatedKeys.getInt(1));
                } else {
                    throw new SQLException("La création de l'utilisateur a échoué, aucun ID obtenu.");
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de la sauvegarde de l'utilisateur", e);
            return null;
        }
        
        return user;
    }
    
    @Override
    public User update(User user) {
        String query = "UPDATE users SET email = ?, password = ?, pharmacy_id = ?, role = ?, " +
                      "status = ?, app_access = ? WHERE id = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, user.getEmail());
            stmt.setString(2, user.getPassword());
            
            if (user.getPharmacyId() != null) {
                stmt.setInt(3, user.getPharmacyId());
            } else {
                stmt.setNull(3, Types.INTEGER);
            }
            
            stmt.setString(4, user.getRole());
            stmt.setString(5, user.getStatus());
            stmt.setString(6, user.getAppAccess());
            stmt.setInt(7, user.getId());
            
            int affectedRows = stmt.executeUpdate();
            
            if (affectedRows == 0) {
                throw new SQLException("La mise à jour de l'utilisateur a échoué, aucune ligne affectée.");
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de la mise à jour de l'utilisateur", e);
            return null;
        }
        
        return user;
    }
    
    @Override
    public boolean delete(Integer id) {
        String query = "DELETE FROM users WHERE id = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, id);
            
            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de la suppression de l'utilisateur", e);
            return false;
        }
    }
    
    /**
     * Trouve un utilisateur par son email
     * @param email L'email de l'utilisateur
     * @return L'utilisateur ou null s'il n'existe pas
     */
    public User findByEmail(String email) {
        String query = "SELECT * FROM users WHERE email = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return mapResultSetToUser(rs);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de la recherche de l'utilisateur par email", e);
        }
        
        return null;
    }
    
    /**
     * Authentifie un utilisateur avec son email et son mot de passe
     * @param email L'email de l'utilisateur
     * @param password Le mot de passe en clair
     * @return L'utilisateur authentifié ou null si l'authentification échoue
     */
    public User authenticate(String email, String password) {
        User user = findByEmail(email);
        
        if (user != null) {
            // Vérifier si le mot de passe correspond
            if (SecurityUtils.verifyPassword(password, user.getPassword())) {
                // Mettre à jour la dernière connexion
                updateLastLogin(user.getId());
                return user;
            }
        }
        
        return null;
    }
    
    /**
     * Met à jour la date de dernière connexion d'un utilisateur
     * @param userId L'ID de l'utilisateur
     */
    private void updateLastLogin(int userId) {
        String query = "UPDATE users SET last_login = ? WHERE id = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setTimestamp(1, new Timestamp(System.currentTimeMillis()));
            stmt.setInt(2, userId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Erreur lors de la mise à jour de la dernière connexion", e);
        }
    }
    
    /**
     * Convertit un ResultSet en objet User
     * @param rs Le ResultSet contenant les données de l'utilisateur
     * @return L'objet User créé
     * @throws SQLException En cas d'erreur SQL
     */
    private User mapResultSetToUser(ResultSet rs) throws SQLException {
        User user = new User();
        user.setId(rs.getInt("id"));
        user.setEmail(rs.getString("email"));
        user.setPassword(rs.getString("password"));
        
        // Gérer les valeurs NULL pour pharmacy_id
        int pharmacyId = rs.getInt("pharmacy_id");
        if (!rs.wasNull()) {
            user.setPharmacyId(pharmacyId);
        }
        
        user.setRole(rs.getString("role"));
        user.setStatus(rs.getString("status"));
        
        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            user.setCreatedAt(new Date(createdAt.getTime()));
        }
        
        Timestamp lastLogin = rs.getTimestamp("last_login");
        if (lastLogin != null) {
            user.setLastLogin(new Date(lastLogin.getTime()));
        }
        
        user.setAppAccess(rs.getString("app_access"));
        
        return user;
    }
}
