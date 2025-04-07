package com.gestionpharma;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class CommandeDAO {
    // Créer une nouvelle commande
    public Commande creerCommande(Commande commande) {
        String sqlCommande = "INSERT INTO commandes (client_id, date_commande, statut, total_prix, duree_livraison) VALUES (?, ?, ?, ?, ?)";
        String sqlLigneCommande = "INSERT INTO lignes_commande (commande_id, produit_id, quantite, prix_unitaire) VALUES (?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConnection.getConnection()) {
            // Désactiver l'auto-commit pour gérer la transaction
            conn.setAutoCommit(false);
            
            // Insérer la commande
            try (PreparedStatement pstmtCommande = conn.prepareStatement(sqlCommande, Statement.RETURN_GENERATED_KEYS)) {
                pstmtCommande.setInt(1, commande.getClientId());
                pstmtCommande.setTimestamp(2, Timestamp.valueOf(commande.getDateCommande()));
                pstmtCommande.setString(3, commande.getStatut());
                pstmtCommande.setDouble(4, commande.getTotalPrix());
                pstmtCommande.setInt(5, commande.getDureeLivraison());
                
                pstmtCommande.executeUpdate();
                
                // Récupérer l'ID généré
                try (ResultSet generatedKeys = pstmtCommande.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        commande.setId(generatedKeys.getInt(1));
                    }
                }
                
                // Insérer les lignes de commande
                try (PreparedStatement pstmtLigneCommande = conn.prepareStatement(sqlLigneCommande)) {
                    for (LigneCommande ligne : commande.getLignesCommande()) {
                        pstmtLigneCommande.setInt(1, commande.getId());
                        pstmtLigneCommande.setInt(2, ligne.getProduit().getId());
                        pstmtLigneCommande.setInt(3, ligne.getQuantite());
                        pstmtLigneCommande.setDouble(4, ligne.getPrixUnitaire());
                        pstmtLigneCommande.executeUpdate();
                    }
                }
                
                // Valider la transaction
                conn.commit();
            } catch (SQLException e) {
                // Annuler la transaction en cas d'erreur
                conn.rollback();
                System.err.println("Erreur lors de la création de la commande : " + e.getMessage());
                e.printStackTrace();
            } finally {
                // Restaurer l'auto-commit
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            System.err.println("Erreur de connexion à la base de données : " + e.getMessage());
            e.printStackTrace();
        }
        
        return commande;
    }

    // Récupérer toutes les commandes
    public List<Commande> obtenirToutesCommandes() {
        List<Commande> commandes = new ArrayList<>();
        String sqlCommandes = "SELECT * FROM commandes";
        String sqlLignesCommande = "SELECT lc.*, p.* FROM lignes_commande lc JOIN produits p ON lc.produit_id = p.id WHERE lc.commande_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rsCommandes = stmt.executeQuery(sqlCommandes)) {
            
            while (rsCommandes.next()) {
                Commande commande = new Commande();
                commande.setId(rsCommandes.getInt("id"));
                commande.setClientId(rsCommandes.getInt("client_id"));
                commande.setDateCommande(rsCommandes.getTimestamp("date_commande").toLocalDateTime());
                commande.setStatut(rsCommandes.getString("statut"));
                commande.setTotalPrix(rsCommandes.getDouble("total_prix"));
                commande.setDureeLivraison(rsCommandes.getInt("duree_livraison"));
                
                // Récupérer les lignes de commande
                try (PreparedStatement pstmtLignes = conn.prepareStatement(sqlLignesCommande)) {
                    pstmtLignes.setInt(1, commande.getId());
                    try (ResultSet rsLignes = pstmtLignes.executeQuery()) {
                        List<LigneCommande> lignesCommande = new ArrayList<>();
                        while (rsLignes.next()) {
                            LigneCommande ligne = new LigneCommande();
                            ligne.setId(rsLignes.getInt("id"));
                            
                            Produit produit = new Produit();
                            produit.setId(rsLignes.getInt("produit_id"));
                            produit.setNom(rsLignes.getString("nom"));
                            produit.setPrix(rsLignes.getDouble("prix"));
                            
                            ligne.setProduit(produit);
                            ligne.setQuantite(rsLignes.getInt("quantite"));
                            ligne.setPrixUnitaire(rsLignes.getDouble("prix_unitaire"));
                            
                            lignesCommande.add(ligne);
                        }
                        commande.setLignesCommande(lignesCommande);
                    }
                }
                
                commandes.add(commande);
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération des commandes : " + e.getMessage());
            e.printStackTrace();
        }
        
        return commandes;
    }

    // Mettre à jour le statut d'une commande
    public void mettreAJourStatutCommande(int commandeId, String statut) {
        String sql = "UPDATE commandes SET statut = ? WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, statut);
            pstmt.setInt(2, commandeId);
            
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Erreur lors de la mise à jour du statut de la commande : " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Supprimer une commande
    public void supprimerCommande(int commandeId) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            // Désactiver l'auto-commit pour gérer la transaction
            conn.setAutoCommit(false);
            
            try {
                // Supprimer d'abord les lignes de commande
                String sqlLignes = "DELETE FROM lignes_commande WHERE commande_id = ?";
                try (PreparedStatement pstmtLignes = conn.prepareStatement(sqlLignes)) {
                    pstmtLignes.setInt(1, commandeId);
                    pstmtLignes.executeUpdate();
                }
                
                // Puis supprimer la commande
                String sqlCommande = "DELETE FROM commandes WHERE id = ?";
                try (PreparedStatement pstmtCommande = conn.prepareStatement(sqlCommande)) {
                    pstmtCommande.setInt(1, commandeId);
                    pstmtCommande.executeUpdate();
                }
                
                // Valider la transaction
                conn.commit();
            } catch (SQLException e) {
                // Annuler la transaction en cas d'erreur
                conn.rollback();
                System.err.println("Erreur lors de la suppression de la commande : " + e.getMessage());
                e.printStackTrace();
            } finally {
                // Restaurer l'auto-commit
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            System.err.println("Erreur de connexion à la base de données : " + e.getMessage());
            e.printStackTrace();
        }
    }
}
