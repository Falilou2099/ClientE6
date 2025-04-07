package com.gestionpharma;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class AccueilAdminFrame extends JFrame {
    private JLabel lblTitre;
    private JLabel lblSousTitre;
    private JLabel lblDate;
    private JLabel lblUtilisateur;
    private JPanel panelStatistiques;
    private JButton btnConnexion;

    public AccueilAdminFrame() {
        initComponents();
    }

    private void initComponents() {
        // Configuration de base de la fenêtre
        setTitle("BigPharma - Interface Administrateur");
        setSize(1000, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        
        // Layout principal
        setLayout(new BorderLayout(10, 10));
        getContentPane().setBackground(new Color(240, 248, 255)); // Bleu très clair

        // Police personnalisée
        Font titreFont = new Font("Arial", Font.BOLD, 36);
        Font sousTitreFont = new Font("Arial", Font.PLAIN, 18);
        Font boutonFont = new Font("Arial", Font.BOLD, 16);

        // Panneau d'en-tête
        JPanel panelEntete = new JPanel(new BorderLayout());
        panelEntete.setBackground(new Color(51, 102, 153)); // Bleu foncé
        panelEntete.setPreferredSize(new Dimension(getWidth(), 150));

        // Titre principal
        lblTitre = new JLabel("BigPharma");
        lblTitre.setFont(titreFont);
        lblTitre.setForeground(Color.WHITE);
        lblTitre.setHorizontalAlignment(SwingConstants.CENTER);
        panelEntete.add(lblTitre, BorderLayout.CENTER);

        // Sous-titre
        lblSousTitre = new JLabel("Système de Gestion Pharmaceutique - Interface Administrateur");
        lblSousTitre.setFont(sousTitreFont);
        lblSousTitre.setForeground(Color.WHITE);
        lblSousTitre.setHorizontalAlignment(SwingConstants.CENTER);
        panelEntete.add(lblSousTitre, BorderLayout.SOUTH);

        // Panneau d'informations
        JPanel panelInfos = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        panelInfos.setBackground(new Color(51, 102, 153));
        
        // Date et utilisateur
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        
        lblDate = new JLabel("Date : " + now.format(formatter));
        lblDate.setForeground(Color.WHITE);
        
        lblUtilisateur = new JLabel("Utilisateur : Administrateur Principal");
        lblUtilisateur.setForeground(Color.WHITE);
        
        panelInfos.add(lblDate);
        panelInfos.add(Box.createHorizontalStrut(20));
        panelInfos.add(lblUtilisateur);
        
        panelEntete.add(panelInfos, BorderLayout.NORTH);

        // Panneau central des statistiques
        panelStatistiques = new JPanel(new GridLayout(2, 3, 10, 10));
        panelStatistiques.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Création des cartes de statistiques
        ajouterCarteStatistique("Produits en Stock", "0", new Color(60, 179, 113)); // Vert
        ajouterCarteStatistique("Commandes en Cours", "0", new Color(255, 165, 0)); // Orange
        ajouterCarteStatistique("Fournisseurs", "0", new Color(100, 149, 237)); // Bleu clair
        ajouterCarteStatistique("Ventes Totales", "0 €", new Color(147, 112, 219)); // Violet
        ajouterCarteStatistique("Nouveaux Produits", "0", new Color(255, 99, 71)); // Rouge
        ajouterCarteStatistique("Alertes Stock", "0", new Color(220, 20, 60)); // Cramoisi

        // Bouton de connexion aux modules
        btnConnexion = new JButton("Accéder aux Modules");
        btnConnexion.setFont(boutonFont);
        btnConnexion.setBackground(new Color(51, 102, 153)); // Bleu foncé correspondant à l'en-tête
        btnConnexion.setForeground(Color.WHITE); // Texte en blanc pour un contraste élevé
        btnConnexion.setPreferredSize(new Dimension(300, 50)); // Taille plus grande
        btnConnexion.setBorderPainted(false); // Désactive la bordure peinte par défaut
        btnConnexion.setContentAreaFilled(false); // Désactive le remplissage par défaut
        btnConnexion.setOpaque(true); // Rend le bouton opaque pour montrer la couleur de fond
        btnConnexion.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20)); // Padding interne
        btnConnexion.setFocusPainted(false); // Enlève l'effet de focus par défaut
        btnConnexion.setCursor(new Cursor(Cursor.HAND_CURSOR)); // Change le curseur au survol
        btnConnexion.addActionListener(e -> ouvrirMenuPrincipal());

        // Ajout des composants
        add(panelEntete, BorderLayout.NORTH);
        add(panelStatistiques, BorderLayout.CENTER);
        add(btnConnexion, BorderLayout.SOUTH);

        // Mise à jour des statistiques
        mettreAJourStatistiques();
    }

    private void ajouterCarteStatistique(String titre, String valeur, Color couleur) {
        JPanel carte = new JPanel(new BorderLayout());
        carte.setBackground(couleur);
        carte.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel lblTitreCarte = new JLabel(titre);
        lblTitreCarte.setForeground(Color.WHITE);
        lblTitreCarte.setFont(new Font("Arial", Font.BOLD, 16));
        
        JLabel lblValeur = new JLabel(valeur);
        lblValeur.setForeground(Color.WHITE);
        lblValeur.setFont(new Font("Arial", Font.BOLD, 24));
        lblValeur.setHorizontalAlignment(SwingConstants.CENTER);

        carte.add(lblTitreCarte, BorderLayout.NORTH);
        carte.add(lblValeur, BorderLayout.CENTER);

        panelStatistiques.add(carte);
    }

    private void mettreAJourStatistiques() {
        // TODO: Implémenter la récupération réelle des statistiques depuis la base de données
        ProduitDAO produitDAO = new ProduitDAO();
        CommandeDAO commandeDAO = new CommandeDAO();
        FournisseurDAO fournisseurDAO = new FournisseurDAO();

        // Mettre à jour les valeurs des cartes
        // Exemple de mise à jour (à remplacer par des requêtes réelles)
        ((JLabel)((JPanel)panelStatistiques.getComponent(0)).getComponent(1)).setText(
            String.valueOf(produitDAO.obtenirTousProduits().size())
        );
        ((JLabel)((JPanel)panelStatistiques.getComponent(1)).getComponent(1)).setText(
            String.valueOf(commandeDAO.obtenirToutesCommandes().size())
        );
        ((JLabel)((JPanel)panelStatistiques.getComponent(2)).getComponent(1)).setText(
            String.valueOf(fournisseurDAO.obtenirTousFournisseurs().size())
        );
    }

    private void ouvrirMenuPrincipal() {
        new MenuPrincipalFrame().setVisible(true);
        this.dispose(); // Ferme la fenêtre d'accueil
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                // Utiliser le look and feel du système
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }
            
            new AccueilAdminFrame().setVisible(true);
        });
    }
}
