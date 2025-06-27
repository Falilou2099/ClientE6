package com.gestionpharma.services;

import com.gestionpharma.config.DatabaseConfigSimple;
import com.gestionpharma.models.Commande;
import com.gestionpharma.models.DetailCommande;
import com.gestionpharma.models.Fournisseur;
import com.gestionpharma.models.Produit;

import javax.swing.JOptionPane;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Service pour gérer les opérations liées aux commandes fournisseurs
 */
public class CommandeService {
    
    /**
     * Récupère toutes les commandes d'une pharmacie
     * @param pharmacieId ID de la pharmacie
     * @return Liste des commandes
     */
    public List<Commande> getAllCommandes(int pharmacieId) {
        List<Commande> commandes = new ArrayList<>();
        String query = "SELECT c.*, f.nom as fournisseur_nom FROM commandes c " +
                       "JOIN fournisseurs f ON c.fournisseur_id = f.id " +
                       "WHERE c.pharmacie_id = ? ORDER BY c.date_commande DESC";
        
        try (Connection conn = DatabaseConfigSimple.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setInt(1, pharmacieId);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                Commande commande = mapResultSetToCommande(rs);
                commandes.add(commande);
            }
            
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Erreur de base de données", 
                    "Impossible de récupérer les commandes : " + e.getMessage(), JOptionPane.ERROR_MESSAGE);
        }
        
        return commandes;
    }
    
    /**
     * Vérifie et crée les tables nécessaires pour les commandes
     * @param conn Connexion à la base de données
     * @throws SQLException En cas d'erreur SQL
     */
    private void verifierTablesCommande(Connection conn) throws SQLException {
        DatabaseMetaData metaData = conn.getMetaData();
        
        // Vérifier si la table commandes existe
        ResultSet tables = metaData.getTables(null, null, "commandes", null);
        if (!tables.next()) {
            // Créer la table commandes
            try (Statement stmt = conn.createStatement()) {
                String createTableSQL = "CREATE TABLE commandes (" +
                                      "id INT AUTO_INCREMENT PRIMARY KEY, " +
                                      "fournisseur_id INT NOT NULL, " +
                                      "date_commande DATE NOT NULL, " +
                                      "date_livraison DATE NULL, " +
                                      "statut VARCHAR(50) NOT NULL, " +
                                      "notes TEXT NULL, " +
                                      "montant_total DECIMAL(10, 2) NOT NULL, " +
                                      "pharmacie_id INT NOT NULL, " +
                                      "FOREIGN KEY (fournisseur_id) REFERENCES fournisseurs(id), " +
                                      "FOREIGN KEY (pharmacie_id) REFERENCES pharmacies(id)" +
                                      ")";
                stmt.execute(createTableSQL);
                System.out.println("Table 'commandes' créée avec succès.");
            }
        } else {
            // Vérifier si la colonne notes existe
            ResultSet notesColumn = metaData.getColumns(null, null, "commandes", "notes");
            if (!notesColumn.next()) {
                try (Statement stmt = conn.createStatement()) {
                    stmt.execute("ALTER TABLE commandes ADD COLUMN notes TEXT NULL");
                    System.out.println("Colonne 'notes' ajoutée à la table commandes.");
                }
            }
            notesColumn.close();
        }
        tables.close();
        
        // Vérifier si la table details_commandes existe
        tables = metaData.getTables(null, null, "details_commandes", null);
        if (!tables.next()) {
            // Créer la table details_commandes
            try (Statement stmt = conn.createStatement()) {
                String createTableSQL = "CREATE TABLE details_commandes (" +
                                      "id INT AUTO_INCREMENT PRIMARY KEY, " +
                                      "commande_id INT NOT NULL, " +
                                      "produit_id INT NOT NULL, " +
                                      "quantite INT NOT NULL, " +
                                      "prix_unitaire DECIMAL(10, 2) NOT NULL, " +
                                      "FOREIGN KEY (commande_id) REFERENCES commandes(id), " +
                                      "FOREIGN KEY (produit_id) REFERENCES produits(id)" +
                                      ")";
                stmt.execute(createTableSQL);
                System.out.println("Table 'details_commandes' créée avec succès.");
            }
        }
        tables.close();
        
        // Vérifier si la table stocks existe
        tables = metaData.getTables(null, null, "stocks", null);
        if (!tables.next()) {
            // Créer la table stocks
            try (Statement stmt = conn.createStatement()) {
                String createTableSQL = "CREATE TABLE stocks (" +
                                      "id INT AUTO_INCREMENT PRIMARY KEY, " +
                                      "produit_id INT NOT NULL, " +
                                      "pharmacie_id INT NOT NULL, " +
                                      "quantite INT NOT NULL DEFAULT 0, " +
                                      "seuil_alerte INT NOT NULL DEFAULT 10, " +
                                      "date_derniere_maj TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP, " +
                                      "FOREIGN KEY (produit_id) REFERENCES produits(id), " +
                                      "FOREIGN KEY (pharmacie_id) REFERENCES pharmacies(id), " +
                                      "UNIQUE KEY unique_produit_pharmacie (produit_id, pharmacie_id)" +
                                      ")";
                stmt.execute(createTableSQL);
                System.out.println("Table 'stocks' créée avec succès.");
            }
        }
        tables.close();
    }
    
    /**
     * Ajoute une nouvelle commande
     * @param commande Commande à ajouter
     * @return true si l'ajout a réussi, false sinon
     */
    public boolean ajouterCommande(Commande commande) {
        // Vérifier si les tables existent et les créer si nécessaire
        try (Connection conn = DatabaseConfigSimple.getConnection()) {
            verifierTablesCommande(conn);
            
            // Démarrer une transaction
            conn.setAutoCommit(false);
            
            // Insérer la commande
            String query = "INSERT INTO commandes (fournisseur_id, date_commande, date_livraison, " +
                          "statut, notes, montant_total, pharmacie_id) VALUES (?, ?, ?, ?, ?, ?, ?)";
            
            try (PreparedStatement pstmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
                pstmt.setInt(1, commande.getFournisseurId());
                pstmt.setDate(2, Date.valueOf(commande.getDateCommande()));
                pstmt.setDate(3, commande.getDateLivraison() != null ? 
                        Date.valueOf(commande.getDateLivraison()) : null);
                pstmt.setString(4, commande.getStatut());
                pstmt.setString(5, commande.getNotes());
                pstmt.setDouble(6, commande.getMontantTotal());
                pstmt.setInt(7, commande.getPharmacieId());
                
                int affectedRows = pstmt.executeUpdate();
                
                if (affectedRows > 0) {
                    ResultSet generatedKeys = pstmt.getGeneratedKeys();
                    if (generatedKeys.next()) {
                        int commandeId = generatedKeys.getInt(1);
                        commande.setId(commandeId);
                        
                        // Insérer les détails de commande
                        if (commande.getDetailsCommande() != null && !commande.getDetailsCommande().isEmpty()) {
                            ajouterDetailsCommande(conn, commandeId, commande.getDetailsCommande());
                        }
                        
                        // Valider la transaction
                        conn.commit();
                        
                        // Ajouter une activité
                        try {
                            String fournisseurNom = commande.getFournisseurNom() != null ? 
                                    commande.getFournisseurNom() : "Fournisseur inconnu";
                            String activityQuery = "INSERT INTO activites (type, description, utilisateur, pharmacie_id) " +
                                                  "VALUES (?, ?, ?, ?)";
                            try (PreparedStatement activityStmt = conn.prepareStatement(activityQuery)) {
                                activityStmt.setString(1, "Commande");
                                activityStmt.setString(2, "Nouvelle commande créée pour le fournisseur: " + fournisseurNom);
                                activityStmt.setString(3, "Admin"); // À remplacer par le nom de l'administrateur connecté
                                activityStmt.setInt(4, commande.getPharmacieId());
                                activityStmt.executeUpdate();
                            }
                        } catch (SQLException e) {
                            System.out.println("Erreur lors de l'ajout de l'activité: " + e.getMessage());
                            // Ne pas bloquer l'opération si l'ajout d'activité échoue
                        }
                        
                        return true;
                    }
                }
                
                // En cas d'erreur, annuler la transaction
                conn.rollback();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
            
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Erreur de base de données", 
                    "Impossible d'ajouter la commande : " + e.getMessage(), JOptionPane.ERROR_MESSAGE);
        }
        
        return false;
    }
    
    /**
     * Met à jour le statut d'une commande
     * @param commandeId ID de la commande
     * @param statut Nouveau statut
     * @return true si la mise à jour a réussi, false sinon
     */
    public boolean updateStatutCommande(int commandeId, String statut) {
        String query = "UPDATE commandes SET statut = ? WHERE id = ?";
        
        try (Connection conn = DatabaseConfigSimple.getConnection()) {
            conn.setAutoCommit(false); // Démarrer une transaction
            
            try (PreparedStatement pstmt = conn.prepareStatement(query)) {
                pstmt.setString(1, statut);
                pstmt.setInt(2, commandeId);
                
                int affectedRows = pstmt.executeUpdate();
                
                if (affectedRows > 0) {
                    // Si le statut devient "Livré", ajouter les produits au stock
                    if ("Livré".equalsIgnoreCase(statut) || "Livrée".equalsIgnoreCase(statut)) {
                        ajouterProduitsEnStock(conn, commandeId);
                    }
                    
                    conn.commit(); // Valider la transaction
                    return true;
                } else {
                    conn.rollback(); // Annuler la transaction
                    return false;
                }
                
            } catch (SQLException e) {
                conn.rollback(); // Annuler la transaction en cas d'erreur
                throw e;
            }
            
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Erreur de base de données", 
                    "Impossible de mettre à jour le statut de la commande : " + e.getMessage(), JOptionPane.ERROR_MESSAGE);
        }
        
        return false;
    }
    
    /**
     * Met à jour la date de livraison d'une commande
     * @param commandeId ID de la commande
     * @param dateLivraison Nouvelle date de livraison
     * @return true si la mise à jour a réussi, false sinon
     */
    public boolean updateDateLivraison(int commandeId, LocalDate dateLivraison) {
        String query = "UPDATE commandes SET date_livraison = ? WHERE id = ?";
        
        try (Connection conn = DatabaseConfigSimple.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setDate(1, Date.valueOf(dateLivraison));
            pstmt.setInt(2, commandeId);
            
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
            
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Erreur de base de données", 
                    "Impossible de mettre à jour la date de livraison : " + e.getMessage(), JOptionPane.ERROR_MESSAGE);
        }
        
        return false;
    }
    
    /**
     * Récupère une commande par son ID
     * @param commandeId ID de la commande
     * @return Commande trouvée ou null
     */
    public Commande getCommandeById(int commandeId) {
        String query = "SELECT c.*, f.nom as fournisseur_nom FROM commandes c " +
                       "JOIN fournisseurs f ON c.fournisseur_id = f.id " +
                       "WHERE c.id = ?";
        
        try (Connection conn = DatabaseConfigSimple.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setInt(1, commandeId);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return mapResultSetToCommande(rs);
            }
            
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Erreur de base de données", 
                    "Impossible de récupérer la commande : " + e.getMessage(), JOptionPane.ERROR_MESSAGE);
        }
        
        return null;
    }
    
    /**
     * Recherche des commandes par fournisseur ou statut
     * @param searchTerm Terme de recherche
     * @param pharmacieId ID de la pharmacie
     * @return Liste des commandes correspondant à la recherche
     */
    public List<Commande> rechercherCommandes(String searchTerm, int pharmacieId) {
        List<Commande> commandes = new ArrayList<>();
        String query = "SELECT c.*, f.nom as fournisseur_nom FROM commandes c " +
                       "JOIN fournisseurs f ON c.fournisseur_id = f.id " +
                       "WHERE c.pharmacie_id = ? AND (f.nom LIKE ? OR c.statut LIKE ?) " +
                       "ORDER BY c.date_commande DESC";
        
        try (Connection conn = DatabaseConfigSimple.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            String searchPattern = "%" + searchTerm + "%";
            pstmt.setInt(1, pharmacieId);
            pstmt.setString(2, searchPattern);
            pstmt.setString(3, searchPattern);
            
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                Commande commande = mapResultSetToCommande(rs);
                commandes.add(commande);
            }
            
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Erreur de base de données", 
                    "Impossible de rechercher les commandes : " + e.getMessage(), JOptionPane.ERROR_MESSAGE);
        }
        
        return commandes;
    }
    
    /**
     * Récupère les commandes en attente de livraison
     * @param pharmacieId ID de la pharmacie
     * @return Liste des commandes en attente
     */
    public List<Commande> getCommandesEnAttente(int pharmacieId) {
        List<Commande> commandes = new ArrayList<>();
        String query = "SELECT c.*, f.nom as fournisseur_nom FROM commandes c " +
                       "JOIN fournisseurs f ON c.fournisseur_id = f.id " +
                       "WHERE c.pharmacie_id = ? AND c.statut = 'En attente' " +
                       "ORDER BY c.date_commande DESC";
        
        try (Connection conn = DatabaseConfigSimple.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setInt(1, pharmacieId);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                Commande commande = mapResultSetToCommande(rs);
                commandes.add(commande);
            }
            
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Erreur de base de données", 
                    "Impossible de récupérer les commandes en attente : " + e.getMessage(), JOptionPane.ERROR_MESSAGE);
        }
        
        return commandes;
    }
    
    /**
     * Modifie une commande existante
     * @param commande Commande à modifier
     * @return true si la modification a réussi, false sinon
     */
    public boolean modifierCommande(Commande commande) {
        try (Connection conn = DatabaseConfigSimple.getConnection()) {
            // Démarrer une transaction
            conn.setAutoCommit(false);
            
            // Mettre à jour la commande
            String query = "UPDATE commandes SET fournisseur_id = ?, date_commande = ?, date_livraison = ?, " +
                         "statut = ?, notes = ?, montant_total = ? WHERE id = ?";
            
            try (PreparedStatement pstmt = conn.prepareStatement(query)) {
                pstmt.setInt(1, commande.getFournisseurId());
                pstmt.setDate(2, Date.valueOf(commande.getDateCommande()));
                pstmt.setDate(3, commande.getDateLivraison() != null ? 
                        Date.valueOf(commande.getDateLivraison()) : null);
                pstmt.setString(4, commande.getStatut());
                pstmt.setString(5, commande.getNotes());
                pstmt.setDouble(6, commande.getMontantTotal());
                pstmt.setInt(7, commande.getId());
                
                int affectedRows = pstmt.executeUpdate();
                
                if (affectedRows > 0) {
                    // Supprimer les anciens détails de commande
                    String deleteQuery = "DELETE FROM details_commandes WHERE commande_id = ?";
                    try (PreparedStatement deleteStmt = conn.prepareStatement(deleteQuery)) {
                        deleteStmt.setInt(1, commande.getId());
                        deleteStmt.executeUpdate();
                    }
                    
                    // Ajouter les nouveaux détails de commande
                    if (commande.getDetailsCommande() != null && !commande.getDetailsCommande().isEmpty()) {
                        ajouterDetailsCommande(conn, commande.getId(), commande.getDetailsCommande());
                    }
                    
                    // Valider la transaction
                    conn.commit();
                    
                    // Ajouter une activité
                    try {
                        String fournisseurNom = commande.getFournisseurNom() != null ? 
                                commande.getFournisseurNom() : "Fournisseur inconnu";
                        String activityQuery = "INSERT INTO activites (type, description, utilisateur, pharmacie_id) " +
                                              "VALUES (?, ?, ?, ?)";
                        try (PreparedStatement activityStmt = conn.prepareStatement(activityQuery)) {
                            activityStmt.setString(1, "Commande");
                            activityStmt.setString(2, "Modification de la commande #" + commande.getId() + " pour le fournisseur: " + fournisseurNom);
                            activityStmt.setString(3, "Admin"); // À remplacer par le nom de l'administrateur connecté
                            activityStmt.setInt(4, commande.getPharmacieId());
                            activityStmt.executeUpdate();
                        }
                    } catch (SQLException e) {
                        System.out.println("Erreur lors de l'ajout de l'activité: " + e.getMessage());
                        // Ne pas bloquer l'opération si l'ajout d'activité échoue
                    }
                    
                    return true;
                }
                
                // En cas d'erreur, annuler la transaction
                conn.rollback();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
            
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Erreur de base de données", 
                    "Impossible de modifier la commande : " + e.getMessage(), JOptionPane.ERROR_MESSAGE);
        }
        
        return false;
    }
    
    /**
     * Convertit un ResultSet en objet Commande
     * @param rs ResultSet contenant les données de la commande
     * @return Objet Commande
     * @throws SQLException En cas d'erreur d'accès aux données
     */
    private Commande mapResultSetToCommande(ResultSet rs) throws SQLException {
        Commande commande = new Commande();
        commande.setId(rs.getInt("id"));
        commande.setFournisseurId(rs.getInt("fournisseur_id"));
        commande.setFournisseurNom(rs.getString("fournisseur_nom"));
        
        // Charger le fournisseur complet si disponible
        FournisseurService fournisseurService = new FournisseurService();
        Fournisseur fournisseur = fournisseurService.getFournisseurById(rs.getInt("fournisseur_id"));
        if (fournisseur != null) {
            commande.setFournisseur(fournisseur);
        }
        
        Date dateCommande = rs.getDate("date_commande");
        if (dateCommande != null) {
            commande.setDateCommande(dateCommande.toLocalDate());
        }
        
        Date dateLivraison = rs.getDate("date_livraison");
        if (dateLivraison != null) {
            commande.setDateLivraison(dateLivraison.toLocalDate());
        }
        
        commande.setStatut(rs.getString("statut"));
        
        // Charger les notes si la colonne existe
        try {
            commande.setNotes(rs.getString("notes"));
        } catch (SQLException e) {
            // La colonne n'existe pas, ignorer
            commande.setNotes("");
        }
        
        commande.setMontantTotal(rs.getDouble("montant_total"));
        commande.setPharmacieId(rs.getInt("pharmacie_id"));
        
        // Charger les détails de commande
        List<DetailCommande> details = getDetailsCommande(commande.getId());
        commande.setDetailsCommande(details);
        
        return commande;
    }
    
    /**
     * Récupère les détails d'une commande
     * @param commandeId ID de la commande
     * @return Liste des détails de commande
     */
    public List<DetailCommande> getDetailsCommande(int commandeId) {
        List<DetailCommande> details = new ArrayList<>();
        String query = "SELECT dc.*, p.nom as produit_nom, p.description, p.prix_achat, p.prix_vente, p.categorie " +
                       "FROM details_commandes dc " +
                       "JOIN produits p ON dc.produit_id = p.id " +
                       "WHERE dc.commande_id = ?";
        
        try (Connection conn = DatabaseConfigSimple.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setInt(1, commandeId);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                DetailCommande detail = new DetailCommande();
                detail.setId(rs.getInt("id"));
                detail.setCommandeId(rs.getInt("commande_id"));
                detail.setProduitId(rs.getInt("produit_id"));
                detail.setQuantite(rs.getInt("quantite"));
                detail.setPrixUnitaire(rs.getDouble("prix_unitaire"));
                
                // Créer et configurer l'objet Produit
                Produit produit = new Produit();
                produit.setId(rs.getInt("produit_id"));
                produit.setNom(rs.getString("produit_nom"));
                produit.setDescription(rs.getString("description"));
                produit.setPrixAchat(rs.getDouble("prix_achat"));
                produit.setPrixVente(rs.getDouble("prix_vente"));
                produit.setCategorie(rs.getString("categorie"));
                
                detail.setProduit(produit);
                details.add(detail);
            }
            
        } catch (SQLException e) {
            System.out.println("Erreur lors de la récupération des détails de commande: " + e.getMessage());
        }
        
        return details;
    }
    
    /**
     * Ajoute les détails d'une commande dans la base de données
     * @param conn Connexion à la base de données
     * @param commandeId ID de la commande
     * @param details Liste des détails de commande
     * @throws SQLException En cas d'erreur SQL
     */
    private void ajouterDetailsCommande(Connection conn, int commandeId, List<DetailCommande> details) throws SQLException {
        String query = "INSERT INTO details_commandes (commande_id, produit_id, quantite, prix_unitaire) " +
                       "VALUES (?, ?, ?, ?)";
        
        try (PreparedStatement pstmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            for (DetailCommande detail : details) {
                pstmt.setInt(1, commandeId);
                pstmt.setInt(2, detail.getProduit().getId());
                pstmt.setInt(3, detail.getQuantite());
                pstmt.setDouble(4, detail.getPrixUnitaire());
                pstmt.addBatch();
            }
            
            pstmt.executeBatch();
        }
    }
    
    /**
     * Supprime une commande et ses détails associés
     * @param commandeId ID de la commande à supprimer
     * @return true si la suppression a réussi, false sinon
     */
    public boolean supprimerCommande(int commandeId) {
        try (Connection conn = DatabaseConfigSimple.getConnection()) {
            conn.setAutoCommit(false);
            
            try {
                // Supprimer d'abord les détails de la commande (pour maintenir l'intégrité référentielle)
                String deleteDetailsQuery = "DELETE FROM details_commandes WHERE commande_id = ?";
                try (PreparedStatement pstmt = conn.prepareStatement(deleteDetailsQuery)) {
                    pstmt.setInt(1, commandeId);
                    pstmt.executeUpdate();
                }
                
                // Ensuite, supprimer la commande elle-même
                String deleteCommandeQuery = "DELETE FROM commandes WHERE id = ?";
                try (PreparedStatement pstmt = conn.prepareStatement(deleteCommandeQuery)) {
                    pstmt.setInt(1, commandeId);
                    int affectedRows = pstmt.executeUpdate();
                    
                    // Valider la transaction uniquement si la commande a été supprimée
                    if (affectedRows > 0) {
                        conn.commit();
                        return true;
                    } else {
                        // Si aucune ligne n'a été affectée, annuler la transaction
                        conn.rollback();
                        return false;
                    }
                }
            } catch (SQLException e) {
                // En cas d'erreur, annuler la transaction
                conn.rollback();
                JOptionPane.showMessageDialog(null, "Erreur de suppression", 
                        "Impossible de supprimer la commande : " + e.getMessage(), JOptionPane.ERROR_MESSAGE);
                return false;
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Erreur de connexion", 
                    "Impossible de se connecter à la base de données : " + e.getMessage(), JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }
    
    /**
     * Ajoute les produits d'une commande livrée au stock
     * @param conn Connexion à la base de données
     * @param commandeId ID de la commande livrée
     * @throws SQLException En cas d'erreur SQL
     */
    private void ajouterProduitsEnStock(Connection conn, int commandeId) throws SQLException {
        // Récupérer les détails de la commande
        String queryDetails = "SELECT dc.produit_id, dc.quantite, c.pharmacie_id " +
                             "FROM details_commandes dc " +
                             "JOIN commandes c ON dc.commande_id = c.id " +
                             "WHERE dc.commande_id = ?";
        
        try (PreparedStatement pstmtDetails = conn.prepareStatement(queryDetails)) {
            pstmtDetails.setInt(1, commandeId);
            ResultSet rs = pstmtDetails.executeQuery();
            
            while (rs.next()) {
                int produitId = rs.getInt("produit_id");
                int quantite = rs.getInt("quantite");
                int pharmacieId = rs.getInt("pharmacie_id");
                
                // Vérifier si le produit existe déjà en stock
                String queryStock = "SELECT quantite FROM stocks WHERE produit_id = ? AND pharmacie_id = ?";
                try (PreparedStatement pstmtStock = conn.prepareStatement(queryStock)) {
                    pstmtStock.setInt(1, produitId);
                    pstmtStock.setInt(2, pharmacieId);
                    ResultSet rsStock = pstmtStock.executeQuery();
                    
                    if (rsStock.next()) {
                        // Le produit existe déjà en stock, mettre à jour la quantité
                        int quantiteActuelle = rsStock.getInt("quantite");
                        String updateStock = "UPDATE stocks SET quantite = ?, date_derniere_maj = NOW() " +
                                           "WHERE produit_id = ? AND pharmacie_id = ?";
                        try (PreparedStatement pstmtUpdate = conn.prepareStatement(updateStock)) {
                            pstmtUpdate.setInt(1, quantiteActuelle + quantite);
                            pstmtUpdate.setInt(2, produitId);
                            pstmtUpdate.setInt(3, pharmacieId);
                            pstmtUpdate.executeUpdate();
                        }
                    } else {
                        // Le produit n'existe pas en stock, l'ajouter
                        String insertStock = "INSERT INTO stocks (produit_id, pharmacie_id, quantite, " +
                                           "seuil_alerte, date_derniere_maj) VALUES (?, ?, ?, 10, NOW())";
                        try (PreparedStatement pstmtInsert = conn.prepareStatement(insertStock)) {
                            pstmtInsert.setInt(1, produitId);
                            pstmtInsert.setInt(2, pharmacieId);
                            pstmtInsert.setInt(3, quantite);
                            pstmtInsert.executeUpdate();
                        }
                    }
                }
            }
        }
        
        System.out.println("Produits de la commande " + commandeId + " ajoutés au stock avec succès.");
    }
    
    /**
     * Récupère les commandes par statut
     * @param pharmacieId ID de la pharmacie
     * @param status Statut de commande (en cours, livrée, annulée, etc.)
     * @return Liste des commandes ayant le statut spécifié
     */
    public List<Commande> getCommandesByStatus(int pharmacieId, String status) {
        List<Commande> commandes = new ArrayList<>();
        String query = "SELECT c.*, f.nom as fournisseur_nom FROM commandes c " +
                       "JOIN fournisseurs f ON c.fournisseur_id = f.id " +
                       "WHERE c.pharmacie_id = ? AND c.statut = ? ORDER BY c.date_commande DESC";
        
        try (Connection conn = DatabaseConfigSimple.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setInt(1, pharmacieId);
            pstmt.setString(2, status);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                Commande commande = mapResultSetToCommande(rs);
                commandes.add(commande);
            }
            
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Erreur de base de données", 
                    "Impossible de récupérer les commandes par statut : " + e.getMessage(), JOptionPane.ERROR_MESSAGE);
        }
        
        return commandes;
    }
}
