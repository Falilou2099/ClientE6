package com.gestionpharma;

import com.gestionpharma.models.Commande;
import com.gestionpharma.models.DetailCommande;
import com.gestionpharma.models.Fournisseur;
import com.gestionpharma.models.Produit;
import com.gestionpharma.services.CommandeService;
import com.gestionpharma.services.FournisseurService;
import com.gestionpharma.services.ProduitService;
import com.gestionpharma.utils.SessionManager;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Boîte de dialogue pour créer une nouvelle commande avec possibilité de commander
 * des produits auprès de plusieurs fournisseurs
 */
public class NouvelleCommandeDialog extends JDialog {
    // Services
    private final CommandeService commandeService;
    private final FournisseurService fournisseurService;
    private final ProduitService produitService;
    
    // Composants d'interface
    private JComboBox<Fournisseur> cbFournisseurs;
    private JComboBox<String> cbCategories;
    private JComboBox<Produit> cbProduits;
    private JSpinner spinnerQuantite;
    private JTable tablePanier;
    private DefaultTableModel modelPanier;
    private JTextField txtMontantTotal;
    private JTextArea txtNotes;
    private JButton btnAjouterProduit;
    private JButton btnSupprimerProduit;
    private JButton btnValiderCommande;
    private JButton btnAnnuler;
    
    // Données
    private Map<Fournisseur, List<DetailCommande>> commandesParFournisseur;
    private List<Produit> allProduits;
    private boolean estConfirme = false;
    
    /**
     * Constructeur
     * @param parent Fenêtre parente
     */
    public NouvelleCommandeDialog(Frame parent) {
        super(parent, "Nouvelle commande", true);
        
        // Initialisation des services
        this.commandeService = new CommandeService();
        this.fournisseurService = new FournisseurService();
        this.produitService = new ProduitService();
        
        // Initialisation des données
        this.commandesParFournisseur = new HashMap<>();
        
        // Configuration de la boîte de dialogue
        initialiserComposants();
        chargerDonnees();
        
        // Affichage
        pack();
        setLocationRelativeTo(parent);
        setResizable(true);
        setMinimumSize(new Dimension(800, 600));
    }
    
    /**
     * Initialise les composants de l'interface
     */
    private void initialiserComposants() {
        // Configuration du layout principal
        setLayout(new BorderLayout(10, 10));
        
        // Panneau de sélection des produits
        JPanel panelSelection = new JPanel(new GridBagLayout());
        panelSelection.setBorder(BorderFactory.createTitledBorder("Sélection des produits"));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // Fournisseur
        gbc.gridx = 0;
        gbc.gridy = 0;
        panelSelection.add(new JLabel("Fournisseur:"), gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        cbFournisseurs = new JComboBox<>();
        cbFournisseurs.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                if (value instanceof Fournisseur) {
                    value = ((Fournisseur) value).getNom();
                }
                return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            }
        });
        cbFournisseurs.addActionListener(e -> filtrerProduits());
        panelSelection.add(cbFournisseurs, gbc);
        
        // Catégorie
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0.0;
        panelSelection.add(new JLabel("Catégorie:"), gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        cbCategories = new JComboBox<>();
        cbCategories.addItem("Toutes les catégories");
        for (String categorie : getCategories()) {
            cbCategories.addItem(categorie);
        }
        cbCategories.addActionListener(e -> filtrerProduits());
        panelSelection.add(cbCategories, gbc);
        
        // Produit
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 0.0;
        panelSelection.add(new JLabel("Produit:"), gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        cbProduits = new JComboBox<>();
        cbProduits.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                if (value instanceof Produit) {
                    Produit produit = (Produit) value;
                    value = produit.getNom() + " - " + String.format("%.2f €", produit.getPrixAchat());
                }
                return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            }
        });
        panelSelection.add(cbProduits, gbc);
        
        // Quantité
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.weightx = 0.0;
        panelSelection.add(new JLabel("Quantité:"), gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        spinnerQuantite = new JSpinner(new SpinnerNumberModel(1, 1, 1000, 1));
        panelSelection.add(spinnerQuantite, gbc);
        
        // Bouton d'ajout
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        btnAjouterProduit = new JButton("Ajouter au panier");
        btnAjouterProduit.addActionListener(e -> ajouterProduitAuPanier());
        panelSelection.add(btnAjouterProduit, gbc);
        
        // Panneau du panier
        JPanel panelPanier = new JPanel(new BorderLayout(5, 5));
        panelPanier.setBorder(BorderFactory.createTitledBorder("Panier"));
        
        // Tableau du panier
        String[] colonnes = {"Fournisseur", "Produit", "Prix unitaire", "Quantité", "Total"};
        modelPanier = new DefaultTableModel(colonnes, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tablePanier = new JTable(modelPanier);
        JScrollPane scrollPanier = new JScrollPane(tablePanier);
        panelPanier.add(scrollPanier, BorderLayout.CENTER);
        
        // Bouton de suppression
        JPanel panelBoutonsPanier = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnSupprimerProduit = new JButton("Supprimer du panier");
        btnSupprimerProduit.addActionListener(e -> supprimerProduitDuPanier());
        panelBoutonsPanier.add(btnSupprimerProduit);
        panelPanier.add(panelBoutonsPanier, BorderLayout.SOUTH);
        
        // Panneau des informations de commande
        JPanel panelInfos = new JPanel(new GridBagLayout());
        panelInfos.setBorder(BorderFactory.createTitledBorder("Informations de commande"));
        
        gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // Montant total
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.0;
        panelInfos.add(new JLabel("Montant total:"), gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        txtMontantTotal = new JTextField("0.00 €");
        txtMontantTotal.setEditable(false);
        panelInfos.add(txtMontantTotal, gbc);
        
        // Notes
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0.0;
        panelInfos.add(new JLabel("Notes:"), gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1.0;
        txtNotes = new JTextArea(3, 20);
        txtNotes.setLineWrap(true);
        txtNotes.setWrapStyleWord(true);
        JScrollPane scrollNotes = new JScrollPane(txtNotes);
        panelInfos.add(scrollNotes, gbc);
        
        // Panneau des boutons
        JPanel panelBoutons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnValiderCommande = new JButton("Valider la commande");
        btnValiderCommande.addActionListener(e -> validerCommande());
        btnAnnuler = new JButton("Annuler");
        btnAnnuler.addActionListener(e -> dispose());
        
        panelBoutons.add(btnValiderCommande);
        panelBoutons.add(btnAnnuler);
        
        // Assemblage des panneaux
        JPanel panelGauche = new JPanel(new BorderLayout(5, 5));
        panelGauche.add(panelSelection, BorderLayout.NORTH);
        panelGauche.add(panelInfos, BorderLayout.CENTER);
        
        JPanel panelPrincipal = new JPanel(new BorderLayout(10, 10));
        panelPrincipal.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panelPrincipal.add(panelGauche, BorderLayout.WEST);
        panelPrincipal.add(panelPanier, BorderLayout.CENTER);
        
        add(panelPrincipal, BorderLayout.CENTER);
        add(panelBoutons, BorderLayout.SOUTH);
    }
    
    /**
     * Charge les données (fournisseurs et produits)
     */
    private void chargerDonnees() {
        try {
            System.out.println("Début du chargement des données...");
            
            // Charger les fournisseurs avec gestion du pharmacieId
            int pharmacieId = SessionManager.getPharmacieId();
            if (pharmacieId <= 0) {
                pharmacieId = 1; // Utiliser l'ID par défaut si SessionManager n'est pas initialisé
                System.out.println("SessionManager non initialisé, utilisation de pharmacieId par défaut: " + pharmacieId);
            }
            System.out.println("ID de la pharmacie: " + pharmacieId);
            
            List<Fournisseur> fournisseurs = fournisseurService.getAllFournisseurs(pharmacieId);
            System.out.println("Nombre de fournisseurs récupérés: " + (fournisseurs != null ? fournisseurs.size() : "null"));
            
            DefaultComboBoxModel<Fournisseur> modelFournisseurs = new DefaultComboBoxModel<>();
            
            if (fournisseurs != null && !fournisseurs.isEmpty()) {
                for (Fournisseur fournisseur : fournisseurs) {
                    modelFournisseurs.addElement(fournisseur);
                    System.out.println("Fournisseur ajouté: " + fournisseur.getNom());
                }
            } else {
                System.out.println("Aucun fournisseur trouvé, ajout d'un fournisseur par défaut");
                // Ajouter un fournisseur par défaut pour les tests
                Fournisseur fournisseurDefaut = new Fournisseur();
                fournisseurDefaut.setId(1);
                fournisseurDefaut.setNom("Fournisseur par défaut");
                fournisseurDefaut.setAdresse("Adresse test");
                fournisseurDefaut.setTelephone("0123456789");
                fournisseurDefaut.setEmail("test@fournisseur.com");
                modelFournisseurs.addElement(fournisseurDefaut);
            }
            
            cbFournisseurs.setModel(modelFournisseurs);
            System.out.println("Modèle des fournisseurs appliqué. Nombre d'éléments: " + modelFournisseurs.getSize());
            
            // Charger les catégories
            String[] categories = getCategories();
            DefaultComboBoxModel<String> modelCategories = new DefaultComboBoxModel<>(categories);
            cbCategories.setModel(modelCategories);
            System.out.println("Catégories chargées: " + categories.length);
            
            // Configurer le spinner de quantité
            SpinnerNumberModel modelQuantite = new SpinnerNumberModel(1, 1, 1000, 1);
            spinnerQuantite.setModel(modelQuantite);
            
            // Configurer le modèle du tableau panier
            modelPanier = new DefaultTableModel(new Object[]{
                "Fournisseur", "Produit", "Prix unitaire", "Quantité", "Montant"
            }, 0);
            
            tablePanier.setModel(modelPanier);
            
            // Charger tous les produits
            allProduits = produitService.getAllProduits(pharmacieId);
            System.out.println("Nombre de produits récupérés: " + (allProduits != null ? allProduits.size() : "null"));
            
            // Appliquer le filtrage initial
            filtrerProduits();
            
            System.out.println("Chargement des données terminé.");
            
        } catch (Exception e) {
            System.err.println("Erreur lors du chargement des données: " + e.getMessage());
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, 
                "Erreur lors du chargement des données: " + e.getMessage(), 
                "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * Filtre les produits en fonction du fournisseur et de la catégorie sélectionnés
     */
    private void filtrerProduits() {
        Fournisseur fournisseurSelectionne = (Fournisseur) cbFournisseurs.getSelectedItem();
        String categorieSelectionnee = (String) cbCategories.getSelectedItem();
        
        if (fournisseurSelectionne == null) return;
        
        DefaultComboBoxModel<Produit> modelProduits = new DefaultComboBoxModel<>();
        
        // Filtrer les produits par fournisseur et catégorie
        for (Produit produit : allProduits) {
            boolean ajouterProduit = true;
            
            // Filtrer par catégorie si une catégorie spécifique est sélectionnée
            if (categorieSelectionnee != null && !categorieSelectionnee.equals("Toutes les catégories")) {
                if (!produit.getCategorie().equals(categorieSelectionnee)) {
                    ajouterProduit = false;
                }
            }
            
            if (ajouterProduit) {
                modelProduits.addElement(produit);
            }
        }
        
        cbProduits.setModel(modelProduits);
        
        // Si aucun produit n'est disponible après filtrage, afficher un message
        if (modelProduits.getSize() == 0) {
            JOptionPane.showMessageDialog(this,
                "Aucun produit ne correspond aux critères de filtrage.",
                "Information", JOptionPane.INFORMATION_MESSAGE);
        }
    }
    
    /**
     * Retourne la liste des catégories de produits
     * @return Liste des catégories
     */
    private String[] getCategories() {
        // Liste statique des catégories (comme dans AjoutProduitDialog)
        return new String[] {
            "Analgésiques", 
            "Anti-inflammatoires", 
            "Antibiotiques", 
            "Antihistaminiques",
            "Gastro-entérologie", 
            "Dermatologie", 
            "Cardiologie", 
            "Vitamines",
            "Compléments alimentaires", 
            "Homéopathie", 
            "Hygiène", 
            "Premiers soins",
            "Ophtalmologie", 
            "ORL", 
            "Contraception", 
            "Nutrition", 
            "Autres"
        };
    }
    /**
     * Ajoute un produit au panier
     */
    private void ajouterProduitAuPanier() {
        Fournisseur fournisseur = (Fournisseur) cbFournisseurs.getSelectedItem();
        Produit produit = (Produit) cbProduits.getSelectedItem();
        int quantite = (int) spinnerQuantite.getValue();
        
        if (fournisseur == null || produit == null || quantite <= 0) {
            JOptionPane.showMessageDialog(this, 
                "Veuillez sélectionner un fournisseur, un produit et une quantité valide.", 
                "Erreur", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // Vérifier si le produit est déjà dans le panier pour ce fournisseur
        boolean produitExistant = false;
        if (commandesParFournisseur.containsKey(fournisseur)) {
            for (DetailCommande detailExistant : commandesParFournisseur.get(fournisseur)) {
                if (detailExistant.getProduit().getId() == produit.getId()) {
                    // Mettre à jour la quantité du produit existant
                    int nouvelleQuantite = detailExistant.getQuantite() + quantite;
                    detailExistant.setQuantite(nouvelleQuantite);
                    
                    // Mettre à jour la ligne dans le tableau
                    for (int i = 0; i < modelPanier.getRowCount(); i++) {
                        String nomFournisseur = (String) modelPanier.getValueAt(i, 0);
                        String nomProduit = (String) modelPanier.getValueAt(i, 1);
                        
                        if (nomFournisseur.equals(fournisseur.getNom()) && nomProduit.equals(produit.getNom())) {
                            modelPanier.setValueAt(nouvelleQuantite, i, 3);
                            modelPanier.setValueAt(String.format("%.2f €", detailExistant.getMontantTotal()), i, 4);
                            break;
                        }
                    }
                    
                    produitExistant = true;
                    break;
                }
            }
        }
        
        if (!produitExistant) {
            // Créer un détail de commande
            DetailCommande detail = new DetailCommande();
            detail.setProduit(produit);
            detail.setProduitId(produit.getId());
            detail.setQuantite(quantite);
            double prixUnitaire = produit.getPrixAchat();
            detail.setPrixUnitaire(prixUnitaire);
            
            // Ajouter à la map des commandes par fournisseur
            if (!commandesParFournisseur.containsKey(fournisseur)) {
                commandesParFournisseur.put(fournisseur, new ArrayList<>());
            }
            commandesParFournisseur.get(fournisseur).add(detail);
            
            // Ajouter au tableau
            modelPanier.addRow(new Object[]{
                fournisseur.getNom(),
                produit.getNom(),
                String.format("%.2f €", produit.getPrixAchat()),
                quantite,
                String.format("%.2f €", detail.getMontantTotal())
            });
        }
        
        // Mettre à jour le montant total
        mettreAJourMontantTotal();
        
        // Afficher un message de confirmation
        JOptionPane.showMessageDialog(this, 
            "Produit ajouté au panier avec succès.", 
            "Succès", JOptionPane.INFORMATION_MESSAGE);
        
        // Réinitialiser la quantité
        spinnerQuantite.setValue(1);
    }
    /**
     * Supprime un produit du panier
     */
    private void supprimerProduitDuPanier() {
        int ligneSelectionnee = tablePanier.getSelectedRow();
        if (ligneSelectionnee == -1) {
            JOptionPane.showMessageDialog(this, 
                "Veuillez sélectionner un produit à supprimer du panier.", 
                "Erreur", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // Récupérer les informations de la ligne
        String nomFournisseur = (String) modelPanier.getValueAt(ligneSelectionnee, 0);
        String nomProduit = (String) modelPanier.getValueAt(ligneSelectionnee, 1);
        
        // Supprimer de la map des commandes par fournisseur
        for (Map.Entry<Fournisseur, List<DetailCommande>> entry : commandesParFournisseur.entrySet()) {
            if (entry.getKey().getNom().equals(nomFournisseur)) {
                List<DetailCommande> details = entry.getValue();
                details.removeIf(detail -> detail.getProduit().getNom().equals(nomProduit));
                
                if (details.isEmpty()) {
                    commandesParFournisseur.remove(entry.getKey());
                }
                
                break;
            }
        }
        
        // Supprimer du tableau
        modelPanier.removeRow(ligneSelectionnee);
        
        // Mettre à jour le montant total
        mettreAJourMontantTotal();
    }
    
    /**
     * Met à jour le montant total de la commande
     */
    private void mettreAJourMontantTotal() {
        double montantTotal = 0.0;
        
        for (List<DetailCommande> details : commandesParFournisseur.values()) {
            for (DetailCommande detail : details) {
                montantTotal += detail.getMontantTotal();
            }
        }
        
        txtMontantTotal.setText(String.format("%.2f €", montantTotal));
    }
    
    /**
     * Valide la commande et crée les commandes par fournisseur
     */
    private void validerCommande() {
        if (commandesParFournisseur.isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "Veuillez ajouter au moins un produit au panier.", 
                "Erreur", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        int confirmation = JOptionPane.showConfirmDialog(this, 
            "Êtes-vous sûr de vouloir valider cette commande ?", 
            "Confirmation", JOptionPane.YES_NO_OPTION);
        
        if (confirmation == JOptionPane.YES_OPTION) {
            try {
                // Créer une commande par fournisseur
                for (Map.Entry<Fournisseur, List<DetailCommande>> entry : commandesParFournisseur.entrySet()) {
                    Fournisseur fournisseur = entry.getKey();
                    List<DetailCommande> details = entry.getValue();
                    
                    // Calculer le montant total pour ce fournisseur
                    double montantTotal = details.stream()
                        .mapToDouble(DetailCommande::getMontantTotal)
                        .sum();
                    
                    // Créer la commande
                    Commande commande = new Commande();
                    commande.setFournisseur(fournisseur);
                    commande.setDateCommande(LocalDate.now());
                    commande.setStatut("En attente");
                    commande.setNotes(txtNotes.getText());
                    commande.setMontantTotal(montantTotal);
                    commande.setPharmacieId(SessionManager.getPharmacieId());
                    
                    // Ajouter les détails
                    for (DetailCommande detail : details) {
                        commande.addDetailCommande(detail);
                    }
                    
                    // Enregistrer la commande
                    boolean success = commandeService.ajouterCommande(commande);
                    if (!success) {
                        throw new Exception("Erreur lors de l'enregistrement de la commande pour le fournisseur " + fournisseur.getNom());
                    }
                }
                
                JOptionPane.showMessageDialog(this, 
                    "Commande(s) créée(s) avec succès !", 
                    "Succès", JOptionPane.INFORMATION_MESSAGE);
                
                estConfirme = true;
                dispose();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, 
                    "Erreur lors de la création des commandes : " + e.getMessage(), 
                    "Erreur", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    /**
     * Vérifie si l'utilisateur a confirmé la commande
     * @return true si la commande a été confirmée, false sinon
     */
    public boolean estConfirme() {
        return estConfirme;
    }
}
