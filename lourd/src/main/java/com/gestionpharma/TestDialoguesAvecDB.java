package com.gestionpharma;

import com.gestionpharma.config.DatabaseConfigSimple;
import com.gestionpharma.models.Produit;
import com.gestionpharma.models.Fournisseur;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Test des dialogues avec connexion base de données simplifiée
 */
public class TestDialoguesAvecDB extends JFrame {
    
    public TestDialoguesAvecDB() {
        setTitle("Test des Dialogues - Gestion Pharma");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400, 300);
        setLocationRelativeTo(null);
        
        // Tester la connexion DB au démarrage
        testConnexionDB();
        
        initComponents();
    }
    
    private void testConnexionDB() {
        if (DatabaseConfigSimple.testConnection()) {
            System.out.println("✓ Connexion à la base de données réussie");
        } else {
            System.out.println("✗ Connexion à la base de données échouée");
            JOptionPane.showMessageDialog(this, 
                "Attention: Connexion à la base de données échouée.\n" +
                "Les dialogues fonctionneront mais sans sauvegarde.",
                "Avertissement DB", JOptionPane.WARNING_MESSAGE);
        }
    }
    
    private void initComponents() {
        setLayout(new GridLayout(4, 1, 10, 10));
        
        JButton btnTestProduit = new JButton("Tester Dialogue Ajout Produit");
        btnTestProduit.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                testerDialogueProduit();
            }
        });
        
        JButton btnTestFournisseur = new JButton("Tester Dialogue Ajout Fournisseur");
        btnTestFournisseur.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                testerDialogueFournisseur();
            }
        });
        
        JButton btnTestConnexion = new JButton("Tester Connexion DB");
        btnTestConnexion.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                testConnexionDB();
            }
        });
        
        JButton btnQuitter = new JButton("Quitter");
        btnQuitter.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });
        
        add(btnTestProduit);
        add(btnTestFournisseur);
        add(btnTestConnexion);
        add(btnQuitter);
    }
    
    private void testerDialogueProduit() {
        try {
            AjoutProduitDialog dialog = new AjoutProduitDialog(this);
            dialog.setVisible(true);
            
            if (dialog.estConfirme()) {
                Produit produit = dialog.getProduit();
                System.out.println("Produit créé:");
                System.out.println("- Nom: " + produit.getNom());
                System.out.println("- Prix: " + produit.getPrixVente());
                System.out.println("- Catégorie: " + produit.getCategorie());
                System.out.println("- Description: " + produit.getDescription());
                
                JOptionPane.showMessageDialog(this, 
                    "Produit créé avec succès:\n" + produit.getNom(),
                    "Succès", JOptionPane.INFORMATION_MESSAGE);
            } else {
                System.out.println("Création de produit annulée");
            }
        } catch (Exception e) {
            System.err.println("Erreur lors du test du dialogue produit: " + e.getMessage());
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, 
                "Erreur: " + e.getMessage(),
                "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void testerDialogueFournisseur() {
        try {
            AjoutFournisseurDialog dialog = new AjoutFournisseurDialog(this);
            dialog.setVisible(true);
            
            if (dialog.estConfirme()) {
                Fournisseur fournisseur = dialog.getFournisseur();
                System.out.println("Fournisseur créé:");
                System.out.println("- Nom: " + fournisseur.getNom());
                System.out.println("- Adresse: " + fournisseur.getAdresse());
                System.out.println("- Téléphone: " + fournisseur.getTelephone());
                System.out.println("- Email: " + fournisseur.getEmail());
                System.out.println("- SIRET: " + fournisseur.getSiret());
                
                JOptionPane.showMessageDialog(this, 
                    "Fournisseur créé avec succès:\n" + fournisseur.getNom(),
                    "Succès", JOptionPane.INFORMATION_MESSAGE);
            } else {
                System.out.println("Création de fournisseur annulée");
            }
        } catch (Exception e) {
            System.err.println("Erreur lors du test du dialogue fournisseur: " + e.getMessage());
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, 
                "Erreur: " + e.getMessage(),
                "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            System.err.println("Impossible de définir le Look and Feel: " + e.getMessage());
        }
        
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new TestDialoguesAvecDB().setVisible(true);
            }
        });
    }
}
