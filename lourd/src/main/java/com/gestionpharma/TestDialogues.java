package com.gestionpharma;

import javax.swing.*;
import java.awt.*;

/**
 * Programme de test pour vérifier les corrections des dialogues
 */
public class TestDialogues {
    
    public static void main(String[] args) {
        // Définir le look and feel natif du système
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        SwingUtilities.invokeLater(() -> {
            // Créer une fenêtre principale simple
            JFrame mainFrame = new JFrame("Test des Dialogues");
            mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            mainFrame.setSize(400, 200);
            mainFrame.setLocationRelativeTo(null);
            
            JPanel panel = new JPanel(new GridLayout(3, 1, 10, 10));
            panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
            
            // Bouton pour tester le dialogue d'ajout de produit
            JButton btnTestProduit = new JButton("Tester Dialogue Ajout Produit");
            btnTestProduit.addActionListener(e -> {
                System.out.println("=== Test du dialogue d'ajout de produit ===");
                try {
                    AjoutProduitDialog dialog = new AjoutProduitDialog(mainFrame);
                    dialog.setVisible(true);
                } catch (Exception ex) {
                    System.err.println("Erreur lors de l'ouverture du dialogue d'ajout de produit: " + ex.getMessage());
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(mainFrame, 
                        "Erreur: " + ex.getMessage(), 
                        "Erreur", JOptionPane.ERROR_MESSAGE);
                }
            });
            
            // Bouton pour tester le dialogue de nouvelle commande
            JButton btnTestCommande = new JButton("Tester Dialogue Nouvelle Commande");
            btnTestCommande.addActionListener(e -> {
                System.out.println("=== Test du dialogue de nouvelle commande ===");
                try {
                    NouvelleCommandeDialog dialog = new NouvelleCommandeDialog(mainFrame);
                    dialog.setVisible(true);
                } catch (Exception ex) {
                    System.err.println("Erreur lors de l'ouverture du dialogue de nouvelle commande: " + ex.getMessage());
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(mainFrame, 
                        "Erreur: " + ex.getMessage(), 
                        "Erreur", JOptionPane.ERROR_MESSAGE);
                }
            });
            
            // Bouton pour quitter
            JButton btnQuitter = new JButton("Quitter");
            btnQuitter.addActionListener(e -> System.exit(0));
            
            panel.add(btnTestProduit);
            panel.add(btnTestCommande);
            panel.add(btnQuitter);
            
            mainFrame.add(panel);
            mainFrame.setVisible(true);
            
            System.out.println("Application de test démarrée.");
            System.out.println("Cliquez sur les boutons pour tester les dialogues corrigés.");
        });
    }
}
