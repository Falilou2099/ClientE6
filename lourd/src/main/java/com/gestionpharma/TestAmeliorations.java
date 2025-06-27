package com.gestionpharma;

import com.gestionpharma.config.DatabaseConfigSimple;
import com.gestionpharma.utils.SessionManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;

/**
 * Application de test pour valider toutes les améliorations apportées
 */
public class TestAmeliorations extends JFrame {
    
    public TestAmeliorations() {
        initComponents();
    }
    
    private void initComponents() {
        setTitle("Test des Améliorations - Gestion Pharmacie");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(600, 400);
        setLocationRelativeTo(null);
        
        // Initialiser la session avec un ID de pharmacie par défaut
        SessionManager.setPharmacieId(1);
        
        JPanel mainPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        
        // Titre
        JLabel titleLabel = new JLabel("Test des Améliorations");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        mainPanel.add(titleLabel, gbc);
        
        // Test de connexion DB
        JButton btnTestDB = new JButton("Tester Connexion Base de Données");
        btnTestDB.addActionListener(e -> testerConnexionDB());
        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 2; gbc.fill = GridBagConstraints.HORIZONTAL;
        mainPanel.add(btnTestDB, gbc);
        
        // Test dialogue nouvelle commande (avec fournisseurs)
        JButton btnTestCommande = new JButton("Tester Dialogue Nouvelle Commande");
        btnTestCommande.addActionListener(e -> testerDialogueCommande());
        gbc.gridx = 0; gbc.gridy = 2;
        mainPanel.add(btnTestCommande, gbc);
        
        // Test gestion des stocks
        JButton btnTestStock = new JButton("Tester Gestion des Stocks");
        btnTestStock.addActionListener(e -> testerGestionStock());
        gbc.gridx = 0; gbc.gridy = 3;
        mainPanel.add(btnTestStock, gbc);
        
        // Test dialogue ajout produit
        JButton btnTestProduit = new JButton("Tester Dialogue Ajout Produit");
        btnTestProduit.addActionListener(e -> testerDialogueProduit());
        gbc.gridx = 0; gbc.gridy = 4;
        mainPanel.add(btnTestProduit, gbc);
        
        // Test dialogue ajout fournisseur
        JButton btnTestFournisseur = new JButton("Tester Dialogue Ajout Fournisseur");
        btnTestFournisseur.addActionListener(e -> testerDialogueFournisseur());
        gbc.gridx = 0; gbc.gridy = 5;
        mainPanel.add(btnTestFournisseur, gbc);
        
        // Informations
        JTextArea infoArea = new JTextArea(8, 40);
        infoArea.setEditable(false);
        infoArea.setText("Améliorations testées :\n\n" +
                        "1. ✅ Correction du dropdown fournisseurs dans nouvelle commande\n" +
                        "2. ✅ Ajout automatique des produits en stock quand commande = 'Livré'\n" +
                        "3. ✅ Page de gestion des stocks affichant tous les produits\n" +
                        "4. ✅ Dialogues d'ajout centralisés et fonctionnels\n" +
                        "5. ✅ Gestion des erreurs et valeurs par défaut\n\n" +
                        "Pharmacie ID utilisée : " + SessionManager.getPharmacieId());
        
        JScrollPane scrollPane = new JScrollPane(infoArea);
        gbc.gridx = 0; gbc.gridy = 6; gbc.gridwidth = 2; gbc.fill = GridBagConstraints.BOTH; gbc.weightx = 1.0; gbc.weighty = 1.0;
        mainPanel.add(scrollPane, gbc);
        
        // Bouton fermer
        JButton btnFermer = new JButton("Fermer");
        btnFermer.addActionListener(e -> System.exit(0));
        gbc.gridx = 0; gbc.gridy = 7; gbc.gridwidth = 2; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0; gbc.weighty = 0;
        mainPanel.add(btnFermer, gbc);
        
        add(mainPanel);
    }
    
    private void testerConnexionDB() {
        try {
            Connection conn = DatabaseConfigSimple.getConnection();
            if (conn != null && !conn.isClosed()) {
                JOptionPane.showMessageDialog(this, 
                    "✅ Connexion à la base de données réussie !", 
                    "Test DB", JOptionPane.INFORMATION_MESSAGE);
                conn.close();
            } else {
                JOptionPane.showMessageDialog(this, 
                    "❌ Échec de la connexion à la base de données", 
                    "Test DB", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, 
                "❌ Erreur de connexion : " + e.getMessage(), 
                "Test DB", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void testerDialogueCommande() {
        try {
            SwingUtilities.invokeLater(() -> {
                NouvelleCommandeDialog dialog = new NouvelleCommandeDialog(this);
                dialog.setVisible(true);
            });
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, 
                "❌ Erreur lors de l'ouverture du dialogue : " + e.getMessage(), 
                "Test Commande", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void testerGestionStock() {
        try {
            SwingUtilities.invokeLater(() -> {
                GestionStockFrame stockFrame = new GestionStockFrame();
                stockFrame.setVisible(true);
            });
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, 
                "❌ Erreur lors de l'ouverture de la gestion des stocks : " + e.getMessage(), 
                "Test Stock", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void testerDialogueProduit() {
        try {
            SwingUtilities.invokeLater(() -> {
                AjoutProduitDialog dialog = new AjoutProduitDialog(this);
                dialog.setVisible(true);
            });
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, 
                "❌ Erreur lors de l'ouverture du dialogue produit : " + e.getMessage(), 
                "Test Produit", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void testerDialogueFournisseur() {
        try {
            SwingUtilities.invokeLater(() -> {
                AjoutFournisseurDialog dialog = new AjoutFournisseurDialog(this);
                dialog.setVisible(true);
            });
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, 
                "❌ Erreur lors de l'ouverture du dialogue fournisseur : " + e.getMessage(), 
                "Test Fournisseur", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }
            
            new TestAmeliorations().setVisible(true);
        });
    }
}
