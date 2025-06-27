package com.gestionpharma;

import javax.swing.*;
import com.gestionpharma.models.Produit;
import com.gestionpharma.models.Fournisseur;

/**
 * Test simple des dialogues Swing sans dépendances
 */
public class TestDialoguesSimple {
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                // Définir le look and feel
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                
                // Créer la fenêtre principale
                JFrame frame = new JFrame("Test des Dialogues");
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.setSize(400, 300);
                frame.setLocationRelativeTo(null);
                
                // Créer un panneau avec des boutons
                JPanel panel = new JPanel();
                panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
                
                // Bouton pour tester l'ajout de produit
                JButton btnProduit = new JButton("Tester Ajout Produit");
                btnProduit.addActionListener(e -> {
                    AjoutProduitDialog dialog = new AjoutProduitDialog(frame);
                    dialog.setVisible(true);
                    
                    if (dialog.estConfirme()) {
                        Produit produit = dialog.getProduit();
                        if (produit != null) {
                            JOptionPane.showMessageDialog(frame, 
                                "Produit créé: " + produit.getNom() + 
                                "\nPrix de vente: " + produit.getPrixVente() + "€" +
                                "\nCatégorie: " + produit.getCategorie(),
                                "Produit créé", JOptionPane.INFORMATION_MESSAGE);
                        }
                    }
                });
                
                // Bouton pour tester l'ajout de fournisseur
                JButton btnFournisseur = new JButton("Tester Ajout Fournisseur");
                btnFournisseur.addActionListener(e -> {
                    AjoutFournisseurDialog dialog = new AjoutFournisseurDialog(frame);
                    dialog.setVisible(true);
                    
                    if (dialog.estConfirme()) {
                        Fournisseur fournisseur = dialog.getFournisseur();
                        if (fournisseur != null) {
                            JOptionPane.showMessageDialog(frame, 
                                "Fournisseur créé: " + fournisseur.getNom() + 
                                "\nTéléphone: " + fournisseur.getTelephone() + 
                                "\nEmail: " + fournisseur.getEmail(),
                                "Fournisseur créé", JOptionPane.INFORMATION_MESSAGE);
                        }
                    }
                });
                
                // Bouton pour quitter
                JButton btnQuitter = new JButton("Quitter");
                btnQuitter.addActionListener(e -> System.exit(0));
                
                // Ajouter les boutons au panneau
                panel.add(Box.createVerticalStrut(20));
                panel.add(btnProduit);
                panel.add(Box.createVerticalStrut(10));
                panel.add(btnFournisseur);
                panel.add(Box.createVerticalStrut(20));
                panel.add(btnQuitter);
                panel.add(Box.createVerticalStrut(20));
                
                frame.add(panel);
                frame.setVisible(true);
                
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(null, 
                    "Erreur lors du démarrage: " + e.getMessage(), 
                    "Erreur", JOptionPane.ERROR_MESSAGE);
            }
        });
    }
}
