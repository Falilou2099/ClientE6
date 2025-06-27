import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * Application BigPharma autonome avec système de connexion complet
 * Version simplifiée sans dépendances externes
 */
public class BigPharmaAutonome extends JFrame {
    // Gestion de session simple
    private static boolean userConnected = false;
    private static int currentUserId = -1;
    private static int currentPharmacieId = -1;
    private static String currentUserName = "";
    private static String currentUserEmail = "";
    
    private JMenuBar menuBar;
    private JPanel mainPanel;
    private JLabel statusLabel;
    private JLabel userInfoLabel;
    
    public BigPharmaAutonome() {
        initComponents();
        setupLayout();
        setupMenus();
        updateUserInterface();
        
        setTitle("BigPharma - Gestion Pharmaceutique (Version Autonome)");
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
        welcomePanel.setBackground(new Color(245, 245, 245));
        GridBagConstraints gbc = new GridBagConstraints();
        
        // Logo/Titre
        JLabel titleLabel = new JLabel("BigPharma");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 48));
        titleLabel.setForeground(new Color(0, 102, 204));
        
        JLabel subtitleLabel = new JLabel("Système de Gestion Pharmaceutique");
        subtitleLabel.setFont(new Font("Arial", Font.PLAIN, 18));
        subtitleLabel.setForeground(Color.GRAY);
        
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.insets = new Insets(50, 20, 10, 20);
        welcomePanel.add(titleLabel, gbc);
        
        gbc.gridy = 1;
        gbc.insets = new Insets(0, 20, 30, 20);
        welcomePanel.add(subtitleLabel, gbc);
        
        // Fonctionnalités disponibles
        if (userConnected) {
            JPanel featuresPanel = createFeaturesPanel();
            gbc.gridy = 2;
            gbc.insets = new Insets(20, 20, 20, 20);
            welcomePanel.add(featuresPanel, gbc);
        } else {
            JButton connexionButton = new JButton("Se connecter");
            connexionButton.setFont(new Font("Arial", Font.BOLD, 16));
            connexionButton.setPreferredSize(new Dimension(200, 50));
            connexionButton.setBackground(new Color(0, 102, 204));
            connexionButton.setForeground(Color.WHITE);
            connexionButton.addActionListener(e -> ouvrirConnexion());
            
            gbc.gridy = 2;
            gbc.insets = new Insets(30, 20, 20, 20);
            welcomePanel.add(connexionButton, gbc);
            
            // Informations sur les fonctionnalités
            JLabel infoLabel = new JLabel("<html><center>Fonctionnalités disponibles après connexion:<br>" +
                "• Système de connexion sécurisé<br>" +
                "• Gestion des tentatives de connexion (max 5)<br>" +
                "• Suspension temporaire (30 minutes)<br>" +
                "• Réinitialisation de mot de passe par email<br>" +
                "• Interface d'administration<br>" +
                "• Synchronisation avec l'application PHP</center></html>");
            infoLabel.setFont(new Font("Arial", Font.PLAIN, 12));
            infoLabel.setForeground(Color.DARK_GRAY);
            
            gbc.gridy = 3;
            gbc.insets = new Insets(30, 20, 20, 20);
            welcomePanel.add(infoLabel, gbc);
        }
        
        mainPanel.add(welcomePanel, BorderLayout.CENTER);
    }
    
    private JPanel createFeaturesPanel() {
        JPanel panel = new JPanel(new GridLayout(2, 2, 10, 10));
        panel.setBorder(BorderFactory.createTitledBorder("Fonctionnalités disponibles"));
        
        JButton produitBtn = new JButton("Gestion Produits");
        produitBtn.addActionListener(e -> afficherMessage("Gestion des Produits", 
            "Module de gestion des produits pharmaceutiques"));
        
        JButton stockBtn = new JButton("Gestion Stock");
        stockBtn.addActionListener(e -> afficherMessage("Gestion du Stock", 
            "Module de gestion des stocks et inventaires"));
        
        JButton commandeBtn = new JButton("Nouvelles Commandes");
        commandeBtn.addActionListener(e -> afficherMessage("Nouvelles Commandes", 
            "Module de création et gestion des commandes"));
        
        JButton syncBtn = new JButton("Synchronisation");
        syncBtn.addActionListener(e -> synchroniserDonnees());
        
        panel.add(produitBtn);
        panel.add(stockBtn);
        panel.add(commandeBtn);
        panel.add(syncBtn);
        
        return panel;
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
        
        // Menu Outils
        JMenu outilsMenu = new JMenu("Outils");
        
        JMenuItem testConnexionItem = new JMenuItem("Test Connexion");
        testConnexionItem.addActionListener(e -> testerConnexion());
        outilsMenu.add(testConnexionItem);
        
        JMenuItem syncItem = new JMenuItem("Synchroniser Données");
        syncItem.addActionListener(e -> synchroniserDonnees());
        outilsMenu.add(syncItem);
        
        menuBar.add(outilsMenu);
        
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
        if (userConnected) {
            userInfoLabel.setText("Connecté: " + currentUserName + " (ID: " + currentUserId + ")");
            statusLabel.setText("Connecté - " + currentUserName);
        } else {
            userInfoLabel.setText("Non connecté");
            statusLabel.setText("Prêt - Veuillez vous connecter");
        }
        
        updateMenuVisibility();
        
        // Recréer l'interface
        mainPanel.removeAll();
        setupLayout();
        mainPanel.revalidate();
        mainPanel.repaint();
    }
    
    private void updateMenuVisibility() {
        if (menuBar != null && menuBar.getMenuCount() > 1) {
            // Menu Outils visible seulement si connecté
            menuBar.getMenu(1).setVisible(userConnected);
        }
    }
    
    private void ouvrirConnexion() {
        try {
            ConnexionSimple dialog = new ConnexionSimple(this);
            dialog.setVisible(true);
            
            if (dialog.estConnexionReussie()) {
                // Initialiser la session avec les données récupérées
                userConnected = true;
                currentUserId = dialog.getUserId();
                currentPharmacieId = dialog.getPharmacieId();
                currentUserName = dialog.getNomUtilisateur();
                currentUserEmail = "user@example.com"; // Email par défaut
                
                updateUserInterface();
                
                JOptionPane.showMessageDialog(this, 
                    "Connexion réussie !\n\n" +
                    "Bienvenue " + dialog.getNomUtilisateur() + "\n" +
                    "ID Utilisateur: " + dialog.getUserId() + "\n" +
                    "ID Pharmacie: " + dialog.getPharmacieId(), 
                    "Connexion Réussie", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, 
                "Erreur lors de l'ouverture du dialogue de connexion:\n" + e.getMessage(), 
                "Erreur", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
    
    private void deconnecter() {
        if (!userConnected) {
            JOptionPane.showMessageDialog(this, "Aucune session active.", 
                "Information", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        int option = JOptionPane.showConfirmDialog(this, 
            "Êtes-vous sûr de vouloir vous déconnecter ?", 
            "Déconnexion", JOptionPane.YES_NO_OPTION);
        
        if (option == JOptionPane.YES_OPTION) {
            // Fermer la session
            userConnected = false;
            currentUserId = -1;
            currentPharmacieId = -1;
            currentUserName = "";
            currentUserEmail = "";
            
            updateUserInterface();
            
            JOptionPane.showMessageDialog(this, "Déconnexion réussie.", 
                "Déconnexion", JOptionPane.INFORMATION_MESSAGE);
        }
    }
    
    private void testerConnexion() {
        try {
            // Test simple de la base de données
            java.sql.Connection conn = java.sql.DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/bigpharma", "root", "");
            if (conn != null && !conn.isClosed()) {
                conn.close();
                JOptionPane.showMessageDialog(this, 
                    "✅ Connexion à la base de données réussie !\n\n" +
                    "La base de données est accessible et prête à être utilisée.", 
                    "Test de Connexion", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, 
                    "❌ Impossible de se connecter à la base de données.", 
                    "Erreur de Connexion", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, 
                "❌ Erreur de connexion à la base de données:\n\n" + e.getMessage() + 
                "\n\nVérifiez que MySQL est démarré et que la base 'bigpharma' existe.", 
                "Erreur de Connexion", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void synchroniserDonnees() {
        if (!userConnected) {
            JOptionPane.showMessageDialog(this, "Veuillez vous connecter d'abord.", 
                "Accès refusé", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int option = JOptionPane.showConfirmDialog(this, 
            "Synchroniser les données avec l'application PHP ?\n\n" +
            "Cette opération va :\n" +
            "• Créer les tables manquantes\n" +
            "• Synchroniser les utilisateurs et produits\n" +
            "• Mettre à jour les colonnes de la base\n" +
            "• Nettoyer les anciens tokens\n\n" +
            "Continuer ?", 
            "Synchronisation", JOptionPane.YES_NO_OPTION);
        
        if (option == JOptionPane.YES_OPTION) {
            JOptionPane.showMessageDialog(this, 
                "Synchronisation programmée !\n\n" +
                "Pour effectuer la synchronisation complète :\n" +
                "1. Ouvrez votre navigateur\n" +
                "2. Allez sur : http://localhost/bigpharma/scripts/sync_database.php?sync\n" +
                "3. Ou exécutez le script PHP en ligne de commande\n\n" +
                "Le script va synchroniser toutes les données entre Java et PHP.", 
                "Synchronisation", JOptionPane.INFORMATION_MESSAGE);
        }
    }
    
    private void afficherMessage(String titre, String message) {
        JOptionPane.showMessageDialog(this, 
            message + "\n\n(Module en cours de développement)", 
            titre, JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void afficherAPropos() {
        String message = "BigPharma v2.0 - Version Autonome\n" +
                        "Système de Gestion Pharmaceutique\n\n" +
                        "✅ Fonctionnalités implémentées :\n" +
                        "• Système de connexion sécurisé avec SHA-256\n" +
                        "• Gestion des tentatives de connexion (max 5)\n" +
                        "• Suspension temporaire après échecs (30 min)\n" +
                        "• Réinitialisation de mot de passe par email\n" +
                        "• Génération et gestion des tokens UUID\n" +
                        "• Interface utilisateur Swing moderne\n" +
                        "• Gestion de session utilisateur\n" +
                        "• Script de synchronisation PHP/Java\n\n" +
                        "🔧 En cours de développement :\n" +
                        "• Modules de gestion (produits, stock, commandes)\n" +
                        "• Synchronisation bidirectionnelle des données\n" +
                        "• Interface d'administration complète\n\n" +
                        "Développé pour BTS SIO SLAM\n" +
                        "Projet Client Lourd/Léger";
        
        JTextArea textArea = new JTextArea(message);
        textArea.setEditable(false);
        textArea.setFont(new Font("Arial", Font.PLAIN, 12));
        
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(500, 400));
        
        JOptionPane.showMessageDialog(this, scrollPane, "À propos de BigPharma", JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void quitterApplication() {
        int option = JOptionPane.showConfirmDialog(this, 
            "Êtes-vous sûr de vouloir quitter l'application ?", 
            "Quitter", JOptionPane.YES_NO_OPTION);
        
        if (option == JOptionPane.YES_OPTION) {
            if (userConnected) {
                userConnected = false;
                currentUserId = -1;
                currentPharmacieId = -1;
                currentUserName = "";
                currentUserEmail = "";
            }
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
            
            new BigPharmaAutonome().setVisible(true);
        });
    }
}
