package com.gestionpharma;

import com.gestionpharma.models.Commande;
import com.gestionpharma.models.DetailCommande;
import com.gestionpharma.models.Produit;
import com.gestionpharma.services.CommandeService;
import com.gestionpharma.utils.SessionManager;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Frame pour la gestion des commandes
 * Permet de créer, visualiser et supprimer des commandes
 */
public class GestionCommandesFrame extends JFrame {

    private JTable tableCommandes;
    private DefaultTableModel modelCommandes;
    private CommandeService commandeService;
    private JComboBox<String> cbFiltreStatut;
    
    private static final String[] STATUTS = {"Tous", "En attente", "En cours", "Livrée", "Annulée"};
    private static final String[] COLONNES = {"ID", "Date", "Fournisseur", "Nb Produits", "Montant Total", "Statut", "Notes"};
    
    /**
     * Constructeur de la fenêtre de gestion des commandes
     */
    public GestionCommandesFrame() {
        this.commandeService = new CommandeService();
        
        setTitle("Gestion des Commandes");
        setSize(900, 600);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        
        initComponents();
        chargerCommandes();
    }
    
    /**
     * Initialise les composants de l'interface
     */
    private void initComponents() {
        // Panel principal avec BorderLayout
        JPanel mainPanel = new JPanel(new BorderLayout());
        
        // Panel du haut pour les boutons et filtres
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        
        // Bouton pour créer une nouvelle commande
        JButton btnNouvelleCommande = new JButton("Nouvelle Commande");
        btnNouvelleCommande.addActionListener(e -> ouvrirDialogueNouvelleCommande());
        
        // Filtre par statut
        JLabel lblFiltreStatut = new JLabel("Filtrer par statut:");
        cbFiltreStatut = new JComboBox<>(STATUTS);
        cbFiltreStatut.addActionListener(e -> filtrerCommandes());
        
        // Bouton pour rafraîchir la liste
        JButton btnRafraichir = new JButton("Rafraîchir");
        btnRafraichir.addActionListener(e -> chargerCommandes());
        
        // Bouton pour supprimer une commande
        JButton btnSupprimer = new JButton("Supprimer");
        btnSupprimer.addActionListener(e -> supprimerCommande());
        
        // Bouton pour retourner au menu principal
        JButton btnRetour = new JButton("Retour");
        btnRetour.addActionListener(e -> dispose());
        
        // Ajout des composants au panel du haut
        topPanel.add(btnNouvelleCommande);
        topPanel.add(lblFiltreStatut);
        topPanel.add(cbFiltreStatut);
        topPanel.add(btnRafraichir);
        topPanel.add(btnSupprimer);
        topPanel.add(btnRetour);
        
        // Tableau des commandes
        modelCommandes = new DefaultTableModel(COLONNES, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        tableCommandes = new JTable(modelCommandes);
        tableCommandes.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tableCommandes.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    afficherDetailsCommande();
                }
            }
        });
        
        JScrollPane scrollPane = new JScrollPane(tableCommandes);
        
        // Ajout des panels au panel principal
        mainPanel.add(topPanel, BorderLayout.NORTH);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        
        // Ajout du panel principal à la fenêtre
        setContentPane(mainPanel);
    }
    
    /**
     * Ouvre la boîte de dialogue pour créer une nouvelle commande
     */
    private void ouvrirDialogueNouvelleCommande() {
        NouvelleCommandeDialog dialog = new NouvelleCommandeDialog(this);
        dialog.setVisible(true);
        
        // Rafraîchir la liste des commandes après la fermeture du dialogue
        if (dialog.estConfirme()) {
            chargerCommandes();
        }
    }
    
    /**
     * Filtre les commandes selon le statut sélectionné
     */
    private void filtrerCommandes() {
        String statut = (String) cbFiltreStatut.getSelectedItem();
        if (statut.equals("Tous")) {
            chargerCommandes();
        } else {
            chargerCommandesParStatut(statut);
        }
    }
    
    /**
     * Charge les commandes filtrées par statut
     * @param statut Le statut à filtrer
     */
    private void chargerCommandesParStatut(String statut) {
        try {
            int pharmacieId = SessionManager.getPharmacieId();
            List<Commande> commandes = commandeService.getCommandesByStatus(pharmacieId, statut);
            afficherCommandes(commandes);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Erreur lors du chargement des commandes: " + e.getMessage(),
                    "Erreur", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
    
    /**
     * Charge toutes les commandes
     */
    private void chargerCommandes() {
        try {
            int pharmacieId = SessionManager.getPharmacieId();
            List<Commande> commandes = commandeService.getAllCommandes(pharmacieId);
            afficherCommandes(commandes);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Erreur lors du chargement des commandes: " + e.getMessage(),
                    "Erreur", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
    
    /**
     * Affiche les commandes dans le tableau
     * @param commandes Liste des commandes à afficher
     */
    private void afficherCommandes(List<Commande> commandes) {
        // Vider le tableau
        modelCommandes.setRowCount(0);
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        
        for (Commande commande : commandes) {
            int nbProduits = commande.getDetailsCommande().size();
            String date = commande.getDateCommande().format(formatter);
            String fournisseur = commande.getFournisseur().getNom();
            double montantTotal = commande.getMontantTotal();
            
            modelCommandes.addRow(new Object[] {
                commande.getId(),
                date,
                fournisseur,
                nbProduits,
                String.format("%.2f €", montantTotal),
                commande.getStatut(),
                commande.getNotes()
            });
        }
    }
    
    /**
     * Supprime la commande sélectionnée
     */
    private void supprimerCommande() {
        int selectedRow = tableCommandes.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Veuillez sélectionner une commande à supprimer.",
                    "Information", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        int commandeId = (int) tableCommandes.getValueAt(selectedRow, 0);
        
        int confirmation = JOptionPane.showConfirmDialog(this,
                "Êtes-vous sûr de vouloir supprimer la commande n°" + commandeId + " ?",
                "Confirmation", JOptionPane.YES_NO_OPTION);
        
        if (confirmation == JOptionPane.YES_OPTION) {
            try {
                commandeService.supprimerCommande(commandeId);
                chargerCommandes();
                JOptionPane.showMessageDialog(this, "Commande supprimée avec succès.",
                        "Succès", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Erreur lors de la suppression de la commande: " + e.getMessage(),
                        "Erreur", JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            }
        }
    }
    
    /**
     * Affiche les détails de la commande sélectionnée
     */
    private void afficherDetailsCommande() {
        int selectedRow = tableCommandes.getSelectedRow();
        if (selectedRow == -1) {
            return;
        }
        
        int commandeId = (int) tableCommandes.getValueAt(selectedRow, 0);
        
        try {
            Commande commande = commandeService.getCommandeById(commandeId);
            if (commande == null) {
                JOptionPane.showMessageDialog(this, "Commande introuvable.",
                        "Erreur", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            // Créer une fenêtre de détails
            JDialog detailsDialog = new JDialog(this, "Détails de la commande n°" + commandeId, true);
            detailsDialog.setSize(600, 400);
            detailsDialog.setLocationRelativeTo(this);
            
            JPanel mainPanel = new JPanel(new BorderLayout());
            
            // Panel d'informations générales
            JPanel infoPanel = new JPanel(new GridLayout(5, 2, 5, 5));
            infoPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
            
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            
            infoPanel.add(new JLabel("ID:"));
            infoPanel.add(new JLabel(String.valueOf(commande.getId())));
            
            infoPanel.add(new JLabel("Date:"));
            infoPanel.add(new JLabel(commande.getDateCommande().format(formatter)));
            
            infoPanel.add(new JLabel("Fournisseur:"));
            infoPanel.add(new JLabel(commande.getFournisseur().getNom()));
            
            infoPanel.add(new JLabel("Statut:"));
            infoPanel.add(new JLabel(commande.getStatut()));
            
            infoPanel.add(new JLabel("Montant total:"));
            infoPanel.add(new JLabel(String.format("%.2f €", commande.getMontantTotal())));
            
            // Panel de notes
            JPanel notesPanel = new JPanel(new BorderLayout());
            notesPanel.setBorder(BorderFactory.createTitledBorder("Notes"));
            
            JTextArea txtNotes = new JTextArea(commande.getNotes());
            txtNotes.setEditable(false);
            txtNotes.setLineWrap(true);
            txtNotes.setWrapStyleWord(true);
            
            notesPanel.add(new JScrollPane(txtNotes), BorderLayout.CENTER);
            
            // Tableau des produits commandés
            DefaultTableModel modelDetails = new DefaultTableModel(
                    new String[] {"Produit", "Quantité", "Prix unitaire", "Total"}, 0) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            };
            
            JTable tableDetails = new JTable(modelDetails);
            
            for (DetailCommande detail : commande.getDetailsCommande()) {
                Produit produit = detail.getProduit();
                modelDetails.addRow(new Object[] {
                    produit.getNom(),
                    detail.getQuantite(),
                    String.format("%.2f €", detail.getPrixUnitaire()),
                    String.format("%.2f €", detail.getQuantite() * detail.getPrixUnitaire())
                });
            }
            
            JScrollPane scrollDetails = new JScrollPane(tableDetails);
            scrollDetails.setBorder(BorderFactory.createTitledBorder("Produits commandés"));
            
            // Bouton de fermeture
            JButton btnFermer = new JButton("Fermer");
            btnFermer.addActionListener(e -> detailsDialog.dispose());
            
            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            buttonPanel.add(btnFermer);
            
            // Assemblage des panels
            JPanel topPanel = new JPanel(new BorderLayout());
            topPanel.add(infoPanel, BorderLayout.NORTH);
            topPanel.add(notesPanel, BorderLayout.CENTER);
            
            mainPanel.add(topPanel, BorderLayout.NORTH);
            mainPanel.add(scrollDetails, BorderLayout.CENTER);
            mainPanel.add(buttonPanel, BorderLayout.SOUTH);
            
            detailsDialog.setContentPane(mainPanel);
            detailsDialog.setVisible(true);
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Erreur lors du chargement des détails: " + e.getMessage(),
                    "Erreur", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
}
