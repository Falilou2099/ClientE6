package com.bigpharma.admin.models;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Modèle représentant une commande dans le système
 * Cette classe correspond à la table 'commandes' dans la base de données
 */
public class Order {
    private Integer id;
    private Integer clientId;
    private Integer pharmacyId;
    private Date dateCommande;
    private String statut;
    private BigDecimal montantTotal;
    private String reference;
    private List<OrderItem> items;
    
    // Constructeur par défaut
    public Order() {
        this.items = new ArrayList<>();
    }
    
    // Constructeur complet
    public Order(Integer id, Integer clientId, Integer pharmacyId, Date dateCommande, 
                String statut, BigDecimal montantTotal, String reference) {
        this.id = id;
        this.clientId = clientId;
        this.pharmacyId = pharmacyId;
        this.dateCommande = dateCommande;
        this.statut = statut;
        this.montantTotal = montantTotal;
        this.reference = reference;
        this.items = new ArrayList<>();
    }
    
    // Getters et Setters
    public Integer getId() {
        return id;
    }
    
    public void setId(Integer id) {
        this.id = id;
    }
    
    public Integer getClientId() {
        return clientId;
    }
    
    public void setClientId(Integer clientId) {
        this.clientId = clientId;
    }
    
    public Integer getPharmacyId() {
        return pharmacyId;
    }
    
    public void setPharmacyId(Integer pharmacyId) {
        this.pharmacyId = pharmacyId;
    }
    
    public Date getDateCommande() {
        return dateCommande;
    }
    
    public void setDateCommande(Date dateCommande) {
        this.dateCommande = dateCommande;
    }
    
    public String getStatut() {
        return statut;
    }
    
    public void setStatut(String statut) {
        this.statut = statut;
    }
    
    public BigDecimal getMontantTotal() {
        return montantTotal;
    }
    
    public void setMontantTotal(BigDecimal montantTotal) {
        this.montantTotal = montantTotal;
    }
    
    public String getReference() {
        return reference;
    }
    
    public void setReference(String reference) {
        this.reference = reference;
    }
    
    public List<OrderItem> getItems() {
        return items;
    }
    
    public void setItems(List<OrderItem> items) {
        this.items = items;
    }
    
    /**
     * Ajoute un article à la commande
     * @param item L'article à ajouter
     */
    public void addItem(OrderItem item) {
        this.items.add(item);
    }
    
    /**
     * Calcule le montant total de la commande à partir des articles
     * @return Le montant total calculé
     */
    public BigDecimal calculateTotal() {
        BigDecimal total = BigDecimal.ZERO;
        for (OrderItem item : items) {
            total = total.add(item.getPrixUnitaire().multiply(new BigDecimal(item.getQuantite())));
        }
        this.montantTotal = total;
        return total;
    }
    
    @Override
    public String toString() {
        return "Order{" +
                "id=" + id +
                ", clientId=" + clientId +
                ", pharmacyId=" + pharmacyId +
                ", dateCommande=" + dateCommande +
                ", statut='" + statut + '\'' +
                ", montantTotal=" + montantTotal +
                ", reference='" + reference + '\'' +
                ", items=" + items.size() +
                '}';
    }
}
