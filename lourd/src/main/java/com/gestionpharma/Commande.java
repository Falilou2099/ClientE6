package com.gestionpharma;

import java.time.LocalDateTime;
import java.util.List;

public class Commande {
    private int id;
    private int clientId;
    private LocalDateTime dateCommande;
    private String statut;
    private double totalPrix;
    private int dureeLivraison;
    private List<LigneCommande> lignesCommande;

    // Constructeurs
    public Commande() {}

    public Commande(int clientId, LocalDateTime dateCommande, String statut, double totalPrix, int dureeLivraison) {
        this.clientId = clientId;
        this.dateCommande = dateCommande;
        this.statut = statut;
        this.totalPrix = totalPrix;
        this.dureeLivraison = dureeLivraison;
    }

    // Getters et Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getClientId() { return clientId; }
    public void setClientId(int clientId) { this.clientId = clientId; }

    public LocalDateTime getDateCommande() { return dateCommande; }
    public void setDateCommande(LocalDateTime dateCommande) { this.dateCommande = dateCommande; }

    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }

    public double getTotalPrix() { return totalPrix; }
    public void setTotalPrix(double totalPrix) { this.totalPrix = totalPrix; }

    public int getDureeLivraison() { return dureeLivraison; }
    public void setDureeLivraison(int dureeLivraison) { this.dureeLivraison = dureeLivraison; }

    public List<LigneCommande> getLignesCommande() { return lignesCommande; }
    public void setLignesCommande(List<LigneCommande> lignesCommande) { this.lignesCommande = lignesCommande; }

    // Méthode pour calculer et mettre à jour le statut
    public void mettreAJourStatut() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime finReception = dateCommande.plusHours(4);
        LocalDateTime finPreparation = finReception.plusHours(4);
        LocalDateTime finLivraison = finPreparation.plusDays(3);

        if (now.isBefore(finReception)) {
            statut = "Reçu";
        } else if (now.isBefore(finPreparation)) {
            statut = "En préparation";
        } else if (now.isBefore(finLivraison)) {
            statut = "En livraison";
        } else {
            statut = "Livrée";
        }
    }

    @Override
    public String toString() {
        return "Commande{" +
                "id=" + id +
                ", clientId=" + clientId +
                ", dateCommande=" + dateCommande +
                ", statut='" + statut + '\'' +
                ", totalPrix=" + totalPrix +
                ", dureeLivraison=" + dureeLivraison +
                ", lignesCommande=" + lignesCommande +
                '}';
    }
}
