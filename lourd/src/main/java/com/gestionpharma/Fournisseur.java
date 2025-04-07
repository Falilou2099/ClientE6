package com.gestionpharma;

public class Fournisseur {
    private int id;
    private String nom;
    private String contact;
    private String telephone;
    private String email;
    private String typesMedicaments;

    // Constructeurs
    public Fournisseur() {}

    public Fournisseur(String nom, String contact, String telephone, String email, String typesMedicaments) {
        this.nom = nom;
        this.contact = contact;
        this.telephone = telephone;
        this.email = email;
        this.typesMedicaments = typesMedicaments;
    }

    // Getters et Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getContact() { return contact; }
    public void setContact(String contact) { this.contact = contact; }

    public String getTelephone() { return telephone; }
    public void setTelephone(String telephone) { this.telephone = telephone; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getTypesMedicaments() { return typesMedicaments; }
    public void setTypesMedicaments(String typesMedicaments) { this.typesMedicaments = typesMedicaments; }

    @Override
    public String toString() {
        return "Fournisseur{" +
                "id=" + id +
                ", nom='" + nom + '\'' +
                ", contact='" + contact + '\'' +
                ", telephone='" + telephone + '\'' +
                ", email='" + email + '\'' +
                ", typesMedicaments='" + typesMedicaments + '\'' +
                '}';
    }
}
