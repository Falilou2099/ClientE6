package com.gestionpharma;

import com.gestionpharma.models.Stock;
import com.gestionpharma.services.StockService;
import com.gestionpharma.utils.SessionManager;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

/**
 * Interface Swing pour gérer les stocks de produits
 */
public class GestionStockFrame extends JFrame {
    private StockService stockService;
    private JTable tableStock;
    private DefaultTableModel modelTable;
    private JTextField txtRecherche;
    private JButton btnActualiser;
    private JButton btnAjusterStock;
    private int pharmacieId;

    public GestionStockFrame() {
        this.pharmacieId = SessionManager.getPharmacieId();
        if (this.pharmacieId <= 0) {
            this.pharmacieId = 1; // Valeur par défaut
        }
        
        this.stockService = new StockService();
        initComponents();
        chargerStocks();
    }

    public GestionStockFrame(int pharmacieId) {
        this.pharmacieId = pharmacieId;
        this.stockService = new StockService();
        initComponents();
        chargerStocks();
    }

    private void initComponents() {
        setTitle("Gestion des Stocks - Pharmacie ID: " + pharmacieId);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(900, 600);
        setLocationRelativeTo(null);

        // Panel principal
        JPanel mainPanel = new JPanel(new BorderLayout());

        // Panel de recherche en haut
        JPanel panelRecherche = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panelRecherche.add(new JLabel("Rechercher:"));
        txtRecherche = new JTextField(20);
        panelRecherche.add(txtRecherche);
        
        btnActualiser = new JButton("Actualiser");
        btnActualiser.addActionListener(e -> chargerStocks());
        panelRecherche.add(btnActualiser);

        btnAjusterStock = new JButton("Ajuster Stock");
        btnAjusterStock.addActionListener(e -> ajusterStockSelectionne());
        panelRecherche.add(btnAjusterStock);

        mainPanel.add(panelRecherche, BorderLayout.NORTH);

        // Table des stocks
        String[] colonnes = {
            "ID", "Produit", "Quantité", "Seuil Min", "Statut", 
            "Date Expiration", "Dernier Mouvement"
        };
        
        modelTable = new DefaultTableModel(colonnes, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Rendre la table non éditable
            }
        };
        
        tableStock = new JTable(modelTable);
        tableStock.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        // Personnaliser l'affichage des colonnes
        tableStock.getColumnModel().getColumn(0).setPreferredWidth(50);  // ID
        tableStock.getColumnModel().getColumn(1).setPreferredWidth(200); // Produit
        tableStock.getColumnModel().getColumn(2).setPreferredWidth(80);  // Quantité
        tableStock.getColumnModel().getColumn(3).setPreferredWidth(80);  // Seuil Min
        tableStock.getColumnModel().getColumn(4).setPreferredWidth(80);  // Statut
        tableStock.getColumnModel().getColumn(5).setPreferredWidth(120); // Date Expiration
        tableStock.getColumnModel().getColumn(6).setPreferredWidth(150); // Dernier Mouvement

        JScrollPane scrollPane = new JScrollPane(tableStock);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        // Panel des boutons en bas
        JPanel panelBoutons = new JPanel(new FlowLayout());
        
        JButton btnFermer = new JButton("Fermer");
        btnFermer.addActionListener(e -> dispose());
        panelBoutons.add(btnFermer);

        mainPanel.add(panelBoutons, BorderLayout.SOUTH);

        add(mainPanel);

        // Ajouter un listener pour la recherche
        txtRecherche.addActionListener(e -> filtrerStocks());
    }

    /**
     * Charge tous les stocks depuis la base de données
     */
    private void chargerStocks() {
        try {
            System.out.println("Chargement des stocks pour la pharmacie ID: " + pharmacieId);
            List<Stock> stocks = stockService.getAllStocks(pharmacieId);
            
            // Vider la table
            modelTable.setRowCount(0);
            
            // Ajouter les stocks à la table
            for (Stock stock : stocks) {
                Object[] row = {
                    stock.getId(),
                    stock.getProduitNom(),
                    stock.getQuantite(),
                    stock.getSeuilMinimum(),
                    stock.getStatut(),
                    stock.getDateExpiration() != null ? stock.getDateExpiration().toString() : "N/A",
                    stock.getDernierMouvement() != null ? stock.getDernierMouvement().toString() : "N/A"
                };
                modelTable.addRow(row);
            }
            
            System.out.println("Nombre de stocks chargés: " + stocks.size());
            
            // Mettre à jour le titre avec le nombre de produits
            setTitle("Gestion des Stocks - Pharmacie ID: " + pharmacieId + " (" + stocks.size() + " produits)");
            
        } catch (Exception e) {
            System.err.println("Erreur lors du chargement des stocks: " + e.getMessage());
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, 
                "Erreur lors du chargement des stocks: " + e.getMessage(), 
                "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Filtre les stocks selon le texte de recherche
     */
    private void filtrerStocks() {
        String recherche = txtRecherche.getText().toLowerCase().trim();
        
        if (recherche.isEmpty()) {
            chargerStocks(); // Recharger tous les stocks
            return;
        }

        try {
            List<Stock> tousLesStocks = stockService.getAllStocks(pharmacieId);
            
            // Vider la table
            modelTable.setRowCount(0);
            
            // Filtrer et ajouter les stocks correspondants
            for (Stock stock : tousLesStocks) {
                if (stock.getProduitNom().toLowerCase().contains(recherche) ||
                    stock.getStatut().toLowerCase().contains(recherche)) {
                    
                    Object[] row = {
                        stock.getId(),
                        stock.getProduitNom(),
                        stock.getQuantite(),
                        stock.getSeuilMinimum(),
                        stock.getStatut(),
                        stock.getDateExpiration() != null ? stock.getDateExpiration().toString() : "N/A",
                        stock.getDernierMouvement() != null ? stock.getDernierMouvement().toString() : "N/A"
                    };
                    modelTable.addRow(row);
                }
            }
            
        } catch (Exception e) {
            System.err.println("Erreur lors du filtrage des stocks: " + e.getMessage());
            JOptionPane.showMessageDialog(this, 
                "Erreur lors du filtrage: " + e.getMessage(), 
                "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Ajuste le stock du produit sélectionné
     */
    private void ajusterStockSelectionne() {
        int selectedRow = tableStock.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, 
                "Veuillez sélectionner un produit dans la table.", 
                "Aucune sélection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            int stockId = (Integer) modelTable.getValueAt(selectedRow, 0);
            String produitNom = (String) modelTable.getValueAt(selectedRow, 1);
            int quantiteActuelle = (Integer) modelTable.getValueAt(selectedRow, 2);

            String input = JOptionPane.showInputDialog(this, 
                "Nouvelle quantité pour " + produitNom + " (actuelle: " + quantiteActuelle + "):", 
                "Ajuster Stock", JOptionPane.QUESTION_MESSAGE);

            if (input != null && !input.trim().isEmpty()) {
                try {
                    int nouvelleQuantite = Integer.parseInt(input.trim());
                    if (nouvelleQuantite < 0) {
                        JOptionPane.showMessageDialog(this, 
                            "La quantité ne peut pas être négative.", 
                            "Erreur", JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    boolean success = stockService.updateQuantiteStock(stockId, nouvelleQuantite);
                    if (success) {
                        JOptionPane.showMessageDialog(this, 
                            "Stock mis à jour avec succès.", 
                            "Succès", JOptionPane.INFORMATION_MESSAGE);
                        chargerStocks(); // Recharger la table
                    } else {
                        JOptionPane.showMessageDialog(this, 
                            "Erreur lors de la mise à jour du stock.", 
                            "Erreur", JOptionPane.ERROR_MESSAGE);
                    }

                } catch (NumberFormatException e) {
                    JOptionPane.showMessageDialog(this, 
                        "Veuillez entrer un nombre valide.", 
                        "Erreur", JOptionPane.ERROR_MESSAGE);
                }
            }

        } catch (Exception e) {
            System.err.println("Erreur lors de l'ajustement du stock: " + e.getMessage());
            JOptionPane.showMessageDialog(this, 
                "Erreur lors de l'ajustement: " + e.getMessage(), 
                "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Méthode main pour tester l'interface
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }
            
            new GestionStockFrame().setVisible(true);
        });
    }
}
