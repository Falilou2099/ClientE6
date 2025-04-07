package com.gestionpharma;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class MenuPrincipalFrame extends JFrame {
    public MenuPrincipalFrame() {
        initComponents();
    }

    private void initComponents() {
        setTitle("BigPharma - Tableau de Bord");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        // Utilisation d'un BorderLayout pour plus de flexibilité
        setLayout(new BorderLayout(10, 10));
        getContentPane().setBackground(new Color(240, 248, 255)); // Bleu très clair

        // Panneau d'en-tête
        JPanel panelEntete = new JPanel(new BorderLayout());
        panelEntete.setBackground(new Color(51, 102, 153)); // Bleu foncé
        panelEntete.setPreferredSize(new Dimension(getWidth(), 100));

        JLabel lblTitre = new JLabel("BigPharma - Tableau de Bord");
        lblTitre.setFont(new Font("Arial", Font.BOLD, 24));
        lblTitre.setForeground(Color.WHITE);
        lblTitre.setHorizontalAlignment(SwingConstants.CENTER);
        panelEntete.add(lblTitre, BorderLayout.CENTER);

        // Panneau central avec les boutons
        JPanel panelBoutons = new JPanel(new GridLayout(2, 2, 20, 20));
        panelBoutons.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        panelBoutons.setBackground(getContentPane().getBackground());

        // Style des boutons
        Font buttonFont = new Font("Arial", Font.BOLD, 16);
        Color buttonColor = new Color(51, 102, 153);
        Color textColor = Color.WHITE;

        // Bouton Gestion des Produits
        JButton btnProduits = creerBoutonPersonnalise("Gestion des Produits", buttonFont, buttonColor, textColor);
        btnProduits.addActionListener(e -> ouvrirGestionProduits());
        panelBoutons.add(btnProduits);

        // Bouton Gestion des Fournisseurs
        JButton btnFournisseurs = creerBoutonPersonnalise("Gestion des Fournisseurs", buttonFont, buttonColor, textColor);
        btnFournisseurs.addActionListener(e -> ouvrirGestionFournisseurs());
        panelBoutons.add(btnFournisseurs);

        // Bouton Gestion des Commandes
        JButton btnCommandes = creerBoutonPersonnalise("Gestion des Commandes", buttonFont, buttonColor, textColor);
        btnCommandes.addActionListener(e -> ouvrirGestionCommandes());
        panelBoutons.add(btnCommandes);

        // Bouton Retour Accueil
        JButton btnRetourAccueil = creerBoutonPersonnalise("Retour Accueil", buttonFont, new Color(220, 20, 60), textColor);
        btnRetourAccueil.addActionListener(e -> retourAccueil());
        panelBoutons.add(btnRetourAccueil);

        // Ajout des composants
        add(panelEntete, BorderLayout.NORTH);
        add(panelBoutons, BorderLayout.CENTER);
    }

    // Méthode pour créer des boutons personnalisés
    private JButton creerBoutonPersonnalise(String texte, Font font, Color backgroundColor, Color textColor) {
        JButton bouton = new JButton(texte);
        bouton.setFont(font);
        bouton.setBackground(backgroundColor);
        bouton.setForeground(textColor);
        bouton.setFocusPainted(false);
        bouton.setBorderPainted(false);
        bouton.setOpaque(true);
        
        // Effet de survol
        bouton.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent evt) {
                bouton.setBackground(backgroundColor.darker());
            }
            public void mouseExited(MouseEvent evt) {
                bouton.setBackground(backgroundColor);
            }
        });
        
        return bouton;
    }

    private void retourAccueil() {
        new AccueilAdminFrame().setVisible(true);
        this.dispose();
    }

    private void ouvrirGestionProduits() {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new GestionProduitFrame().setVisible(true);
            }
        });
    }

    private void ouvrirGestionFournisseurs() {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new GestionFournisseursFrame().setVisible(true);
            }
        });
    }

    private void ouvrirGestionCommandes() {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new GestionCommandesFrame().setVisible(true);
            }
        });
    }

    public static void main(String[] args) {
        // Utilisation de Swing Event Dispatch Thread pour la création de l'interface
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                // Définir le look and feel natif du système
                try {
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                } catch (Exception e) {
                    e.printStackTrace();
                }
                
                // Créer et afficher le menu principal
                MenuPrincipalFrame menuPrincipal = new MenuPrincipalFrame();
                menuPrincipal.setVisible(true);
            }
        });
    }
}
