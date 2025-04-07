package com.gestionpharma.models;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Commande {
    private int id;
    private int fournisseurId;
    private String fournisseurNom;
    private Fournisseur fournisseur; // Référence à l'objet Fournisseur
    private LocalDate dateCommande; // Changé de LocalDateTime à LocalDate
    private LocalDate dateLivraison;
    private String statut;
    private String notes; // Ajout des notes
    private double montantTotal;
    private int pharmacieId;
    private List<DetailCommande> detailsCommande; // Ajout des détails de commande
    
    public Commande() {
        // Constructeur par défaut
        this.detailsCommande = new ArrayList<>();
        this.dateCommande = LocalDate.now();
        this.statut = "En attente";
    }
    
    public Commande(int id, int fournisseurId, String fournisseurNom, LocalDate dateCommande, 
                   LocalDate dateLivraison, String statut, String notes, double montantTotal, int pharmacieId) {
        this.id = id;
        this.fournisseurId = fournisseurId;
        this.fournisseurNom = fournisseurNom;
        this.dateCommande = dateCommande;
        this.dateLivraison = dateLivraison;
        this.statut = statut;
        this.notes = notes;
        this.montantTotal = montantTotal;
        this.pharmacieId = pharmacieId;
        this.detailsCommande = new ArrayList<>();
    }
    
    // Getters et Setters
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public int getFournisseurId() {
        return fournisseurId;
    }
    
    public void setFournisseurId(int fournisseurId) {
        this.fournisseurId = fournisseurId;
    }
    
    public String getFournisseurNom() {
        return fournisseurNom;
    }
    
    public void setFournisseurNom(String fournisseurNom) {
        this.fournisseurNom = fournisseurNom;
    }
    
    public LocalDate getDateCommande() {
        return dateCommande;
    }
    
    public void setDateCommande(LocalDate dateCommande) {
        this.dateCommande = dateCommande;
    }
    
    public Fournisseur getFournisseur() {
        return fournisseur;
    }
    
    public void setFournisseur(Fournisseur fournisseur) {
        this.fournisseur = fournisseur;
        if (fournisseur != null) {
            this.fournisseurId = fournisseur.getId();
            this.fournisseurNom = fournisseur.getNom();
        }
    }
    
    public String getNotes() {
        return notes;
    }
    
    public void setNotes(String notes) {
        this.notes = notes;
    }
    
    public List<DetailCommande> getDetailsCommande() {
        return detailsCommande;
    }
    
    public void setDetailsCommande(List<DetailCommande> detailsCommande) {
        this.detailsCommande = detailsCommande;
    }
    
    public void addDetailCommande(DetailCommande detail) {
        if (this.detailsCommande == null) {
            this.detailsCommande = new ArrayList<>();
        }
        this.detailsCommande.add(detail);
    }
    
    public LocalDate getDateLivraison() {
        return dateLivraison;
    }
    
    public void setDateLivraison(LocalDate dateLivraison) {
        this.dateLivraison = dateLivraison;
    }
    
    public String getStatut() {
        return statut;
    }
    
    public void setStatut(String statut) {
        this.statut = statut;
    }
    
    public double getMontantTotal() {
        return montantTotal;
    }
    
    public void setMontantTotal(double montantTotal) {
        this.montantTotal = montantTotal;
    }
    
    public int getPharmacieId() {
        return pharmacieId;
    }
    
    public void setPharmacieId(int pharmacieId) {
        this.pharmacieId = pharmacieId;
    }
    
    @Override
    public String toString() {
        return "Commande #" + id + " - " + fournisseurNom;
    }
}
