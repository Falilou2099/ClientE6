package com.gestionpharma.models;

import java.time.LocalDateTime;

public class Activite {
    private int id;
    private String type;
    private String description;
    private LocalDateTime date;
    private String utilisateur;
    private int pharmacieId;
    
    public Activite() {
        // Constructeur par défaut
    }
    
    public Activite(int id, String type, String description, LocalDateTime date, String utilisateur, int pharmacieId) {
        this.id = id;
        this.type = type;
        this.description = description;
        this.date = date;
        this.utilisateur = utilisateur;
        this.pharmacieId = pharmacieId;
    }
    
    // Getters et Setters
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public LocalDateTime getDate() {
        return date;
    }
    
    public void setDate(LocalDateTime date) {
        this.date = date;
    }
    
    public String getUtilisateur() {
        return utilisateur;
    }
    
    public void setUtilisateur(String utilisateur) {
        this.utilisateur = utilisateur;
    }
    
    public int getPharmacieId() {
        return pharmacieId;
    }
    
    public void setPharmacieId(int pharmacieId) {
        this.pharmacieId = pharmacieId;
    }
    
    @Override
    public String toString() {
        return date + " - " + type + " - " + description;
    }
}
