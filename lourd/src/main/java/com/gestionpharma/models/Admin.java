package com.gestionpharma.models;

public class Admin {
    private int id;
    private String username;
    private String nom;
    private String prenom;
    private String email;
    private int pharmacieId;

    public Admin(int id, String username, String nom, String prenom, String email, int pharmacieId) {
        this.id = id;
        this.username = username;
        this.nom = nom;
        this.prenom = prenom;
        this.email = email;
        this.pharmacieId = pharmacieId;
    }

    // Getters
    public int getId() { return id; }
    public String getUsername() { return username; }
    public String getNom() { return nom; }
    public String getPrenom() { return prenom; }
    public String getEmail() { return email; }
    public int getPharmacieId() { return pharmacieId; }
}
