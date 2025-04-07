package com.gestionpharma;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

public class GestionCommandesFrame extends JFrame {
    private JTable tableCommandes;
    private DefaultTableModel modelCommandes;
    private CommandeDAO commandeDAO;
    private ProduitDAO produitDAO;
    
    // Composants pour la création de commande
    private JComboBox<String> cbProduits;
    private JTextField txtQuantite;
    private JTextField txtPrixTotal;
    private JButton btnAjouterProduit;
    private JButton btnCreerCommande;
    private JButton btnSupprimerCommande;
    
    // Liste temporaire des lignes de commande
    private List<LigneCommande> lignesCommande = new ArrayList<>();

    public GestionCommandesFrame() {
        commandeDAO = new CommandeDAO();
        produitDAO = new ProduitDAO();
        
        initComponents();
        chargerCommandes();
        chargerProduits();
    }

    private void initComponents() {
        setTitle("Gestion des Commandes Pharmaceutiques");
        setSize(1000, 600);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        // Panneau de saisie des commandes
        JPanel panelSaisie = new JPanel(new GridLayout(5, 2, 5, 5));
        panelSaisie.setBorder(BorderFactory.createTitledBorder("Création de Commande"));

        // Sélection des produits
        panelSaisie.add(new JLabel("Produit :"));
        cbProduits = new JComboBox<>();
        panelSaisie.add(cbProduits);

        // Quantité
        panelSaisie.add(new JLabel("Quantité :"));
        txtQuantite = new JTextField();
        panelSaisie.add(txtQuantite);

        // Bouton pour ajouter un produit à la commande
        btnAjouterProduit = new JButton("Ajouter Produit");
        panelSaisie.add(btnAjouterProduit);

        // Prix total
        panelSaisie.add(new JLabel("Prix Total :"));
        txtPrixTotal = new JTextField();
        txtPrixTotal.setEditable(false);
        panelSaisie.add(txtPrixTotal);

        // Boutons de commande
        JPanel panelBoutons = new JPanel(new FlowLayout());
        btnCreerCommande = new JButton("Créer Commande");
        btnSupprimerCommande = new JButton("Supprimer Commande");
        
        panelBoutons.add(btnCreerCommande);
        panelBoutons.add(btnSupprimerCommande);

        // Tableau des commandes
        String[] colonnes = {"ID", "Date", "Statut", "Prix Total", "Durée Livraison"};
        modelCommandes = new DefaultTableModel(colonnes, 0);
        tableCommandes = new JTable(modelCommandes);
        JScrollPane scrollCommandes = new JScrollPane(tableCommandes);

        // Disposition des composants
        add(panelSaisie, BorderLayout.NORTH);
        add(scrollCommandes, BorderLayout.CENTER);
        add(panelBoutons, BorderLayout.SOUTH);

        // Écouteurs d'événements
        btnAjouterProduit.addActionListener(e -> ajouterProduitACommande());
        btnCreerCommande.addActionListener(e -> creerCommande());
        btnSupprimerCommande.addActionListener(e -> supprimerCommande());

        // Sélection dans le tableau
        tableCommandes.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int ligneSelectionnee = tableCommandes.getSelectedRow();
                if (ligneSelectionnee != -1) {
                    // Logique de sélection si nécessaire
                }
            }
        });
    }

    private void chargerProduits() {
        List<Produit> produits = produitDAO.obtenirTousProduits();
        cbProduits.removeAllItems();
        for (Produit produit : produits) {
            cbProduits.addItem(produit.getNom());
        }
    }

    private void ajouterProduitACommande() {
        try {
            // Récupérer le produit sélectionné
            String nomProduit = (String) cbProduits.getSelectedItem();
            Produit produit = produitDAO.obtenirProduitParNom(nomProduit);
            
            // Vérifier la quantité
            int quantite = Integer.parseInt(txtQuantite.getText());
            if (quantite <= 0) {
                JOptionPane.showMessageDialog(this, "Veuillez saisir une quantité valide.", "Erreur", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Créer une ligne de commande
            LigneCommande ligne = new LigneCommande();
            ligne.setProduit(produit);
            ligne.setQuantite(quantite);
            ligne.setPrixUnitaire(produit.getPrix());

            // Ajouter à la liste des lignes de commande
            lignesCommande.add(ligne);

            // Mettre à jour le prix total
            double prixTotal = lignesCommande.stream()
                .mapToDouble(l -> l.getQuantite() * l.getPrixUnitaire())
                .sum();
            txtPrixTotal.setText(String.format("%.2f €", prixTotal));

            // Réinitialiser la quantité
            txtQuantite.setText("");
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Veuillez saisir une quantité valide.", "Erreur", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Erreur lors de l'ajout du produit : " + ex.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void creerCommande() {
        if (lignesCommande.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Veuillez ajouter des produits à la commande.", "Erreur", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            // Créer une nouvelle commande
            Commande commande = new Commande();
            commande.setClientId(1); // TODO: Gérer la sélection du client
            commande.setDateCommande(LocalDateTime.now());
            commande.setStatut("En cours");
            
            // Calculer le prix total
            double prixTotal = lignesCommande.stream()
                .mapToDouble(l -> l.getQuantite() * l.getPrixUnitaire())
                .sum();
            commande.setTotalPrix(prixTotal);
            
            // Définir une durée de livraison par défaut
            commande.setDureeLivraison(3);
            
            // Ajouter les lignes de commande
            commande.setLignesCommande(lignesCommande);

            // Enregistrer la commande
            commandeDAO.creerCommande(commande);

            // Actualiser l'affichage
            chargerCommandes();

            // Réinitialiser
            lignesCommande.clear();
            txtPrixTotal.setText("");
            JOptionPane.showMessageDialog(this, "Commande créée avec succès !", "Succès", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Erreur lors de la création de la commande : " + ex.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void supprimerCommande() {
        int ligneSelectionnee = tableCommandes.getSelectedRow();
        if (ligneSelectionnee == -1) {
            JOptionPane.showMessageDialog(this, "Veuillez sélectionner une commande à supprimer.", "Erreur", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int commandeId = (int) modelCommandes.getValueAt(ligneSelectionnee, 0);
        
        int confirmation = JOptionPane.showConfirmDialog(this, 
            "Êtes-vous sûr de vouloir supprimer cette commande ?", 
            "Confirmation de suppression", 
            JOptionPane.YES_NO_OPTION);
        
        if (confirmation == JOptionPane.YES_OPTION) {
            commandeDAO.supprimerCommande(commandeId);
            chargerCommandes();
        }
    }

    private void chargerCommandes() {
        // Vider le modèle existant
        modelCommandes.setRowCount(0);

        // Charger les commandes depuis la base de données
        List<Commande> commandes = commandeDAO.obtenirToutesCommandes();
        
        for (Commande commande : commandes) {
            modelCommandes.addRow(new Object[]{
                commande.getId(),
                commande.getDateCommande(),
                commande.getStatut(),
                String.format("%.2f €", commande.getTotalPrix()),
                commande.getDureeLivraison() + " jours"
            });
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new GestionCommandesFrame().setVisible(true);
        });
    }
}
