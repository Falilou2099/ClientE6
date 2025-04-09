package com.gestionpharma.models;

/**
 * Classe représentant une pharmacie dans le système
 */
public class Pharmacie {
    private int id;
    private String nom;
    private String adresse;
    private String telephone;
    private String email;
    private String siret;
    private String horaires;
    
    // Constructeur par défaut
    public Pharmacie() {
    }
    
    // Constructeur avec paramètres
    public Pharmacie(int id, String nom, String adresse, String telephone, String email, String siret, String horaires) {
        this.id = id;
        this.nom = nom;
        this.adresse = adresse;
        this.telephone = telephone;
        this.email = email;
        this.siret = siret;
        this.horaires = horaires;
    }
    
    // Getters et setters
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
    
    public String getSiret() {
        return siret;
    }
    
    public void setSiret(String siret) {
        this.siret = siret;
    }
    
    public String getHoraires() {
        return horaires;
    }
    
    public void setHoraires(String horaires) {
        this.horaires = horaires;
    }
    
    @Override
    public String toString() {
        return "Pharmacie [id=" + id + ", nom=" + nom + ", adresse=" + adresse + ", telephone=" + telephone + ", email="
                + email + ", siret=" + siret + ", horaires=" + horaires + "]";
    }
}
