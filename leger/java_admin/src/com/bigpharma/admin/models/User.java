package com.bigpharma.admin.models;

import java.util.Date;

/**
 * Modèle représentant un utilisateur du système
 * Cette classe correspond à la table 'users' dans la base de données
 */
public class User {
    private int id;
    private String email;
    private String password; // Stocké en haché
    private Integer pharmacyId;
    private String role;
    private String status;
    private Date createdAt;
    private Date lastLogin;
    private String appAccess; // 'both', 'light', 'heavy'
    
    // Constructeur par défaut
    public User() {
    }
    
    // Constructeur complet
    public User(int id, String email, String password, Integer pharmacyId, String role, 
                String status, Date createdAt, Date lastLogin, String appAccess) {
        this.id = id;
        this.email = email;
        this.password = password;
        this.pharmacyId = pharmacyId;
        this.role = role;
        this.status = status;
        this.createdAt = createdAt;
        this.lastLogin = lastLogin;
        this.appAccess = appAccess;
    }
    
    // Getters et Setters
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
    
    public Integer getPharmacyId() {
        return pharmacyId;
    }
    
    public void setPharmacyId(Integer pharmacyId) {
        this.pharmacyId = pharmacyId;
    }
    
    public String getRole() {
        return role;
    }
    
    public void setRole(String role) {
        this.role = role;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public Date getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }
    
    public Date getLastLogin() {
        return lastLogin;
    }
    
    public void setLastLogin(Date lastLogin) {
        this.lastLogin = lastLogin;
    }
    
    public String getAppAccess() {
        return appAccess;
    }
    
    public void setAppAccess(String appAccess) {
        this.appAccess = appAccess;
    }
    
    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", email='" + email + '\'' +
                ", pharmacyId=" + pharmacyId +
                ", role='" + role + '\'' +
                ", status='" + status + '\'' +
                ", appAccess='" + appAccess + '\'' +
                '}';
    }
}
