package com.bigpharma.admin.models;

import java.util.Date;

/**
 * Modèle représentant une pharmacie dans le système
 * Cette classe correspond à la table 'pharmacies' dans la base de données
 */
public class Pharmacy {
    private int id;
    private String nom;
    private String adresse;
    private String telephone;
    private String email;
    private String numeroEnregistrement;
    private String statut;
    private Date dateCreation;
    
    // Constructeur par défaut
    public Pharmacy() {
    }
    
    // Constructeur complet
    public Pharmacy(int id, String nom, String adresse, String telephone, String email, 
                   String numeroEnregistrement, String statut, Date dateCreation) {
        this.id = id;
        this.nom = nom;
        this.adresse = adresse;
        this.telephone = telephone;
        this.email = email;
        this.numeroEnregistrement = numeroEnregistrement;
        this.statut = statut;
        this.dateCreation = dateCreation;
    }
    
    // Getters et Setters
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public String getNom() {
        return nom;
    }
    
    public void setNom(String nom) {
        this.nom = nom;
    }
    
    public String getAdresse() {
        return adresse;
    }
    
    public void setAdresse(String adresse) {
        this.adresse = adresse;
    }
    
    public String getTelephone() {
        return telephone;
    }
    
    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getNumeroEnregistrement() {
        return numeroEnregistrement;
    }
    
    public void setNumeroEnregistrement(String numeroEnregistrement) {
        this.numeroEnregistrement = numeroEnregistrement;
    }
    
    public String getStatut() {
        return statut;
    }
    
    public void setStatut(String statut) {
        this.statut = statut;
    }
    
    public Date getDateCreation() {
        return dateCreation;
    }
    
    public void setDateCreation(Date dateCreation) {
        this.dateCreation = dateCreation;
    }
    
    @Override
    public String toString() {
        return "Pharmacy{" +
                "id=" + id +
                ", nom='" + nom + '\'' +
                ", adresse='" + adresse + '\'' +
                ", telephone='" + telephone + '\'' +
                ", email='" + email + '\'' +
                ", statut='" + statut + '\'' +
                '}';
    }
}
