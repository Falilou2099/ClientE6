package com.gestionpharma;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.sql.*;
import com.gestionpharma.config.DatabaseConfigSimple;

/**
 * Application principale BigPharma avec système de connexion intégré
 */
public class BigPharmaApp extends JFrame {
    private JMenuBar menuBar;
    private JPanel mainPanel;
    private JLabel statusLabel;
    private JLabel userInfoLabel;
    
    public BigPharmaApp() {
        initComponents();
        setupLayout();
        setupMenus();
        updateUserInterface();
        
        setTitle("BigPharma - Gestion Pharmaceutique");
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setSize(1000, 700);
        setLocationRelativeTo(null);
        
        // Gestion de la fermeture
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                quitterApplication();
            }
        });
    }
    
    private void initComponents() {
        mainPanel = new JPanel(new BorderLayout());
        statusLabel = new JLabel("Prêt");
        userInfoLabel = new JLabel("Non connecté");
        
        // Panel de statut
        JPanel statusPanel = new JPanel(new BorderLayout());
        statusPanel.setBorder(BorderFactory.createEtchedBorder());
        statusPanel.add(statusLabel, BorderLayout.WEST);
        statusPanel.add(userInfoLabel, BorderLayout.EAST);
        
        add(mainPanel, BorderLayout.CENTER);
        add(statusPanel, BorderLayout.SOUTH);
    }
    
    private void setupLayout() {
        // Panel d'accueil
        JPanel welcomePanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        
        JLabel titleLabel = new JLabel("BigPharma");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 32));
        titleLabel.setForeground(new Color(0, 102, 204));
        
        JLabel subtitleLabel = new JLabel("Système de Gestion Pharmaceutique");
        subtitleLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        subtitleLabel.setForeground(Color.GRAY);
        
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.insets = new Insets(20, 20, 10, 20);
        welcomePanel.add(titleLabel, gbc);
        
        gbc.gridy = 1;
        gbc.insets = new Insets(0, 20, 20, 20);
        welcomePanel.add(subtitleLabel, gbc);
        
        if (!SessionManager.isUserConnected()) {
            JButton connexionButton = new JButton("Se connecter");
            connexionButton.setFont(new Font("Arial", Font.BOLD, 14));
            connexionButton.setPreferredSize(new Dimension(150, 40));
            connexionButton.addActionListener(e -> ouvrirConnexion());
            
            gbc.gridy = 2;
            gbc.insets = new Insets(20, 20, 20, 20);
            welcomePanel.add(connexionButton, gbc);
        }
        
        mainPanel.add(welcomePanel, BorderLayout.CENTER);
    }
    
    private void setupMenus() {
        menuBar = new JMenuBar();
        
        // Menu Fichier
        JMenu fichierMenu = new JMenu("Fichier");
        
        JMenuItem connexionItem = new JMenuItem("Se connecter");
        connexionItem.addActionListener(e -> ouvrirConnexion());
        fichierMenu.add(connexionItem);
        
        JMenuItem deconnexionItem = new JMenuItem("Se déconnecter");
        deconnexionItem.addActionListener(e -> deconnecter());
        fichierMenu.add(deconnexionItem);
        
        fichierMenu.addSeparator();
        
        JMenuItem quitterItem = new JMenuItem("Quitter");
        quitterItem.addActionListener(e -> quitterApplication());
        fichierMenu.add(quitterItem);
        
        menuBar.add(fichierMenu);
        
        // Menu Gestion (visible seulement si connecté)
        JMenu gestionMenu = new JMenu("Gestion");
        
        JMenuItem produitsItem = new JMenuItem("Gestion des Produits");
        produitsItem.addActionListener(e -> ouvrirGestionProduits());
        gestionMenu.add(produitsItem);
        
        JMenuItem stockItem = new JMenuItem("Gestion du Stock");
        stockItem.addActionListener(e -> ouvrirGestionStock());
        gestionMenu.add(stockItem);
        
        JMenuItem commandesItem = new JMenuItem("Nouvelle Commande");
        commandesItem.addActionListener(e -> ouvrirNouvelleCommande());
        gestionMenu.add(commandesItem);
        
        menuBar.add(gestionMenu);
        
        // Menu Admin (visible seulement pour les admins)
        JMenu adminMenu = new JMenu("Administration");
        
        JMenuItem utilisateursItem = new JMenuItem("Gestion des Utilisateurs");
        utilisateursItem.addActionListener(e -> ouvrirGestionUtilisateurs());
        adminMenu.add(utilisateursItem);
        
        JMenuItem syncItem = new JMenuItem("Synchroniser avec PHP");
        syncItem.addActionListener(e -> synchroniserAvecPHP());
        adminMenu.add(syncItem);
        
        menuBar.add(adminMenu);
        
        // Menu Aide
        JMenu aideMenu = new JMenu("Aide");
        
        JMenuItem aproposItem = new JMenuItem("À propos");
        aproposItem.addActionListener(e -> afficherAPropos());
        aideMenu.add(aproposItem);
        
        menuBar.add(aideMenu);
        
        setJMenuBar(menuBar);
        updateMenuVisibility();
    }
    
    private void updateUserInterface() {
        if (SessionManager.isUserConnected()) {
            userInfoLabel.setText(SessionManager.getSessionInfo());
            statusLabel.setText("Connecté");
        } else {
            userInfoLabel.setText("Non connecté");
            statusLabel.setText("Prêt - Veuillez vous connecter");
        }
        
        updateMenuVisibility();
        repaint();
    }
    
    private void updateMenuVisibility() {
        if (menuBar != null) {
            // Menu Gestion visible seulement si connecté
            menuBar.getMenu(1).setVisible(SessionManager.isUserConnected());
            
            // Menu Admin visible seulement pour les admins
            menuBar.getMenu(2).setVisible(SessionManager.isAdmin());
        }
    }
    
    private void ouvrirConnexion() {
        ConnexionDialog dialog = new ConnexionDialog(this);
        dialog.setVisible(true);
        
        if (dialog.estConnexionReussie()) {
            // Récupérer les informations utilisateur complètes
            String email = recupererEmailUtilisateur(dialog.getUserId());
            String role = recupererRoleUtilisateur(dialog.getUserId());
            
            SessionManager.initSession(
                dialog.getUserId(), 
                dialog.getPharmacieId(), 
                dialog.getNomUtilisateur(),
                email,
                role
            );
            
            updateUserInterface();
            
            JOptionPane.showMessageDialog(this, 
                "Connexion réussie !\nBienvenue " + dialog.getNomUtilisateur(), 
                "Connexion", JOptionPane.INFORMATION_MESSAGE);
        }
    }
    
    private String recupererEmailUtilisateur(int userId) {
        String query = "SELECT email FROM utilisateurs WHERE id = ?";
        try (Connection conn = DatabaseConfigSimple.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return rs.getString("email");
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération de l'email: " + e.getMessage());
        }
        return "";
    }
    
    private String recupererRoleUtilisateur(int userId) {
        String query = "SELECT role FROM utilisateurs WHERE id = ?";
        try (Connection conn = DatabaseConfigSimple.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return rs.getString("role");
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération du rôle: " + e.getMessage());
        }
        return "user";
    }
    
    private void deconnecter() {
        int option = JOptionPane.showConfirmDialog(this, 
            "Êtes-vous sûr de vouloir vous déconnecter ?", 
            "Déconnexion", JOptionPane.YES_NO_OPTION);
        
        if (option == JOptionPane.YES_OPTION) {
            SessionManager.closeSession();
            updateUserInterface();
            
            // Réinitialiser l'interface
            mainPanel.removeAll();
            setupLayout();
            mainPanel.revalidate();
            mainPanel.repaint();
        }
    }
    
    private void ouvrirGestionProduits() {
        if (!SessionManager.isUserConnected()) {
            JOptionPane.showMessageDialog(this, "Veuillez vous connecter d'abord.", 
                "Accès refusé", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        try {
            GestionProduitFrame frame = new GestionProduitFrame();
            frame.setVisible(true);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, 
                "Erreur lors de l'ouverture de la gestion des produits: " + e.getMessage(), 
                "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void ouvrirGestionStock() {
        if (!SessionManager.isUserConnected()) {
            JOptionPane.showMessageDialog(this, "Veuillez vous connecter d'abord.", 
                "Accès refusé", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        try {
            GestionStockFrame frame = new GestionStockFrame();
            frame.setVisible(true);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, 
                "Erreur lors de l'ouverture de la gestion du stock: " + e.getMessage(), 
                "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void ouvrirNouvelleCommande() {
        if (!SessionManager.isUserConnected()) {
            JOptionPane.showMessageDialog(this, "Veuillez vous connecter d'abord.", 
                "Accès refusé", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        try {
            NouvelleCommandeDialog dialog = new NouvelleCommandeDialog(this);
            dialog.setVisible(true);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, 
                "Erreur lors de l'ouverture de la nouvelle commande: " + e.getMessage(), 
                "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void ouvrirGestionUtilisateurs() {
        if (!SessionManager.isAdmin()) {
            JOptionPane.showMessageDialog(this, "Accès réservé aux administrateurs.", 
                "Accès refusé", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        JOptionPane.showMessageDialog(this, "Fonctionnalité en cours de développement.", 
            "Information", JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void synchroniserAvecPHP() {
        if (!SessionManager.isAdmin()) {
            JOptionPane.showMessageDialog(this, "Accès réservé aux administrateurs.", 
                "Accès refusé", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int option = JOptionPane.showConfirmDialog(this, 
            "Synchroniser les données avec l'application PHP ?\n" +
            "Cette opération peut prendre quelques minutes.", 
            "Synchronisation", JOptionPane.YES_NO_OPTION);
        
        if (option == JOptionPane.YES_OPTION) {
            // Ici on pourrait appeler le script PHP de synchronisation
            JOptionPane.showMessageDialog(this, 
                "Synchronisation lancée.\n" +
                "Veuillez exécuter le script sync_database.php côté serveur.", 
                "Synchronisation", JOptionPane.INFORMATION_MESSAGE);
        }
    }
    
    private void afficherAPropos() {
        String message = "BigPharma v2.0\n" +
                        "Système de Gestion Pharmaceutique\n\n" +
                        "Fonctionnalités:\n" +
                        "• Gestion des produits et stocks\n" +
                        "• Système de commandes\n" +
                        "• Authentification sécurisée\n" +
                        "• Synchronisation PHP/Java\n" +
                        "• Gestion des tentatives de connexion\n" +
                        "• Réinitialisation de mot de passe\n\n" +
                        "Développé pour BTS SIO SLAM";
        
        JOptionPane.showMessageDialog(this, message, "À propos", JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void quitterApplication() {
        int option = JOptionPane.showConfirmDialog(this, 
            "Êtes-vous sûr de vouloir quitter l'application ?", 
            "Quitter", JOptionPane.YES_NO_OPTION);
        
        if (option == JOptionPane.YES_OPTION) {
            SessionManager.closeSession();
            System.exit(0);
        }
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }
            
            new BigPharmaApp().setVisible(true);
        });
    }
}
