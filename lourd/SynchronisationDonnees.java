import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Utilitaire de synchronisation des données entre Java et PHP
 * Synchronise les produits, utilisateurs et autres données
 */
public class SynchronisationDonnees extends JFrame {
    private JTextArea logArea;
    private JButton syncButton;
    private JButton testButton;
    private JProgressBar progressBar;
    private JLabel statusLabel;
    
    // Configuration base de données
    private static final String DB_URL = "jdbc:mysql://localhost:3306/bigpharma";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "";
    
    public SynchronisationDonnees() {
        initComponents();
        setupLayout();
        
        setTitle("Synchronisation BigPharma - Java ↔ PHP");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);
    }
    
    private void initComponents() {
        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setFont(new Font("Consolas", Font.PLAIN, 12));
        logArea.setBackground(new Color(248, 248, 248));
        
        syncButton = new JButton("🔄 Synchroniser Toutes les Données");
        syncButton.setFont(new Font("Arial", Font.BOLD, 14));
        syncButton.setBackground(new Color(0, 123, 255));
        syncButton.setForeground(Color.WHITE);
        syncButton.addActionListener(e -> synchroniserToutesDonnees());
        
        testButton = new JButton("🔍 Tester Connexion DB");
        testButton.addActionListener(e -> testerConnexion());
        
        progressBar = new JProgressBar();
        progressBar.setStringPainted(true);
        
        statusLabel = new JLabel("Prêt pour la synchronisation");
        statusLabel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
    }
    
    private void setupLayout() {
        setLayout(new BorderLayout());
        
        // Panel du haut
        JPanel topPanel = new JPanel(new GridLayout(2, 1, 5, 5));
        topPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(testButton);
        buttonPanel.add(syncButton);
        
        topPanel.add(buttonPanel);
        topPanel.add(progressBar);
        
        // Zone de logs
        JScrollPane scrollPane = new JScrollPane(logArea);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Logs de Synchronisation"));
        
        // Panel du bas
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.add(statusLabel, BorderLayout.CENTER);
        
        add(topPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);
    }
    
    private void log(String message) {
        SwingUtilities.invokeLater(() -> {
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
            logArea.append("[" + timestamp + "] " + message + "\n");
            logArea.setCaretPosition(logArea.getDocument().getLength());
        });
    }
    
    private void updateStatus(String status) {
        SwingUtilities.invokeLater(() -> statusLabel.setText(status));
    }
    
    private void updateProgress(int value, String text) {
        SwingUtilities.invokeLater(() -> {
            progressBar.setValue(value);
            progressBar.setString(text);
        });
    }
    
    private void testerConnexion() {
        new Thread(() -> {
            try {
                updateStatus("Test de connexion en cours...");
                log("🔍 Test de connexion à la base de données...");
                
                Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
                if (conn != null && !conn.isClosed()) {
                    log("✅ Connexion à la base de données réussie !");
                    
                    // Test des tables principales
                    String[] tables = {"utilisateurs", "produits", "pharmacies", "fournisseurs", "commandes"};
                    for (String table : tables) {
                        try {
                            Statement stmt = conn.createStatement();
                            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM " + table);
                            if (rs.next()) {
                                int count = rs.getInt(1);
                                log("📊 Table " + table + ": " + count + " enregistrements");
                            }
                            rs.close();
                            stmt.close();
                        } catch (SQLException e) {
                            log("⚠️  Table " + table + " non trouvée ou erreur: " + e.getMessage());
                        }
                    }
                    
                    conn.close();
                    updateStatus("Connexion testée avec succès");
                } else {
                    log("❌ Impossible de se connecter à la base de données");
                    updateStatus("Erreur de connexion");
                }
            } catch (Exception e) {
                log("❌ Erreur de connexion: " + e.getMessage());
                updateStatus("Erreur: " + e.getMessage());
            }
        }).start();
    }
    
    private void synchroniserToutesDonnees() {
        new Thread(() -> {
            try {
                syncButton.setEnabled(false);
                updateStatus("Synchronisation en cours...");
                updateProgress(0, "Initialisation...");
                
                log("🚀 Début de la synchronisation complète");
                log("📧 Recherche du compte: tourefaliloumbacke12345@gmail.com");
                
                Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
                
                // Étape 1: Vérifier/créer l'utilisateur
                updateProgress(10, "Vérification utilisateur...");
                int userId = verifierOuCreerUtilisateur(conn, "tourefaliloumbacke12345@gmail.com");
                
                // Étape 2: Vérifier/créer la pharmacie
                updateProgress(20, "Vérification pharmacie...");
                int pharmacieId = verifierOuCreerPharmacie(conn, userId);
                
                // Étape 3: Synchroniser les catégories
                updateProgress(30, "Synchronisation catégories...");
                synchroniserCategories(conn);
                
                // Étape 4: Synchroniser les fournisseurs
                updateProgress(50, "Synchronisation fournisseurs...");
                synchroniserFournisseurs(conn, pharmacieId);
                
                // Étape 5: Synchroniser les produits
                updateProgress(70, "Synchronisation produits...");
                synchroniserProduits(conn, pharmacieId);
                
                // Étape 6: Vérifier les données finales
                updateProgress(90, "Vérification finale...");
                verifierDonneesFinales(conn, pharmacieId);
                
                conn.close();
                
                updateProgress(100, "Synchronisation terminée !");
                log("✅ Synchronisation complète terminée avec succès !");
                updateStatus("Synchronisation réussie");
                
                JOptionPane.showMessageDialog(this, 
                    "Synchronisation terminée avec succès !\n\n" +
                    "Le compte tourefaliloumbacke12345@gmail.com\n" +
                    "a maintenant accès aux mêmes données\n" +
                    "dans les applications Java et PHP.", 
                    "Synchronisation Réussie", JOptionPane.INFORMATION_MESSAGE);
                
            } catch (Exception e) {
                log("❌ Erreur lors de la synchronisation: " + e.getMessage());
                updateStatus("Erreur de synchronisation");
                e.printStackTrace();
                
                JOptionPane.showMessageDialog(this, 
                    "Erreur lors de la synchronisation:\n" + e.getMessage(), 
                    "Erreur", JOptionPane.ERROR_MESSAGE);
            } finally {
                syncButton.setEnabled(true);
            }
        }).start();
    }
    
    private int verifierOuCreerUtilisateur(Connection conn, String email) throws SQLException {
        log("👤 Vérification de l'utilisateur: " + email);
        
        // Vérifier si l'utilisateur existe
        PreparedStatement checkStmt = conn.prepareStatement(
            "SELECT id, nom FROM utilisateurs WHERE email = ?");
        checkStmt.setString(1, email);
        ResultSet rs = checkStmt.executeQuery();
        
        if (rs.next()) {
            int userId = rs.getInt("id");
            String nom = rs.getString("nom");
            log("✅ Utilisateur trouvé: " + nom + " (ID: " + userId + ")");
            rs.close();
            checkStmt.close();
            return userId;
        }
        
        rs.close();
        checkStmt.close();
        
        // Créer l'utilisateur s'il n'existe pas
        log("➕ Création de l'utilisateur...");
        PreparedStatement insertStmt = conn.prepareStatement(
            "INSERT INTO utilisateurs (nom, email, mot_de_passe, pharmacie_id) VALUES (?, ?, ?, ?)",
            Statement.RETURN_GENERATED_KEYS);
        insertStmt.setString(1, "Toure Falilou Mbacke");
        insertStmt.setString(2, email);
        insertStmt.setString(3, "5e884898da28047151d0e56f8dc6292773603d0d6aabbdd62a11ef721d1542d8"); // "password" en SHA-256
        insertStmt.setInt(4, 1);
        
        insertStmt.executeUpdate();
        ResultSet generatedKeys = insertStmt.getGeneratedKeys();
        int userId = 0;
        if (generatedKeys.next()) {
            userId = generatedKeys.getInt(1);
            log("✅ Utilisateur créé avec l'ID: " + userId);
        }
        
        generatedKeys.close();
        insertStmt.close();
        return userId;
    }
    
    private int verifierOuCreerPharmacie(Connection conn, int userId) throws SQLException {
        log("🏥 Vérification de la pharmacie...");
        
        // Vérifier si la pharmacie existe
        PreparedStatement checkStmt = conn.prepareStatement(
            "SELECT id, nom FROM pharmacies WHERE id = 1");
        ResultSet rs = checkStmt.executeQuery();
        
        if (rs.next()) {
            String nom = rs.getString("nom");
            log("✅ Pharmacie trouvée: " + nom + " (ID: 1)");
            rs.close();
            checkStmt.close();
            return 1;
        }
        
        rs.close();
        checkStmt.close();
        
        // Créer la pharmacie si elle n'existe pas
        log("➕ Création de la pharmacie par défaut...");
        PreparedStatement insertStmt = conn.prepareStatement(
            "INSERT INTO pharmacies (id, nom, adresse, telephone, email) VALUES (?, ?, ?, ?, ?)");
        insertStmt.setInt(1, 1);
        insertStmt.setString(2, "Pharmacie BigPharma");
        insertStmt.setString(3, "123 Rue de la Santé, 75000 Paris");
        insertStmt.setString(4, "01.23.45.67.89");
        insertStmt.setString(5, "contact@bigpharma.com");
        
        insertStmt.executeUpdate();
        insertStmt.close();
        
        log("✅ Pharmacie créée avec l'ID: 1");
        return 1;
    }
    
    private void synchroniserCategories(Connection conn) throws SQLException {
        log("📂 Synchronisation des catégories...");
        
        String[] categories = {
            "Analgésiques", "Anti-inflammatoires", "Antibiotiques", "Antihistaminiques",
            "Antispasmodiques", "Cardiovasculaires", "Dermatologiques", "Digestifs",
            "Endocrinologiques", "Gynécologiques", "Neurologiques", "Ophtalmologiques",
            "ORL", "Pneumologiques", "Psychiatriques", "Urologiques", "Vitamines"
        };
        
        for (String categorie : categories) {
            // Vérifier si la catégorie existe
            PreparedStatement checkStmt = conn.prepareStatement(
                "SELECT id FROM categories WHERE nom = ?");
            checkStmt.setString(1, categorie);
            ResultSet rs = checkStmt.executeQuery();
            
            if (!rs.next()) {
                // Créer la catégorie
                PreparedStatement insertStmt = conn.prepareStatement(
                    "INSERT INTO categories (nom, description) VALUES (?, ?)");
                insertStmt.setString(1, categorie);
                insertStmt.setString(2, "Catégorie " + categorie);
                insertStmt.executeUpdate();
                insertStmt.close();
                log("➕ Catégorie créée: " + categorie);
            }
            
            rs.close();
            checkStmt.close();
        }
        
        log("✅ Synchronisation des catégories terminée");
    }
    
    private void synchroniserFournisseurs(Connection conn, int pharmacieId) throws SQLException {
        log("🏢 Synchronisation des fournisseurs...");
        
        // Fournisseurs par défaut
        String[][] fournisseurs = {
            {"Laboratoires Sanofi", "54 Rue La Boétie, 75008 Paris", "01.53.77.40.00", "contact@sanofi.fr", "12345678901234"},
            {"Pfizer France", "23-25 Avenue du Dr Lannelongue, 75014 Paris", "01.58.07.34.40", "info@pfizer.fr", "23456789012345"},
            {"Novartis Pharma", "2-4 Rue Lionel Terray, 92500 Rueil-Malmaison", "01.55.47.60.00", "contact@novartis.fr", "34567890123456"},
            {"Roche France", "30 Cours de l'Île Seguin, 92100 Boulogne-Billancourt", "01.46.40.50.00", "info@roche.fr", "45678901234567"}
        };
        
        for (String[] fournisseur : fournisseurs) {
            // Vérifier si le fournisseur existe
            PreparedStatement checkStmt = conn.prepareStatement(
                "SELECT id FROM fournisseurs WHERE nom = ? AND pharmacie_id = ?");
            checkStmt.setString(1, fournisseur[0]);
            checkStmt.setInt(2, pharmacieId);
            ResultSet rs = checkStmt.executeQuery();
            
            if (!rs.next()) {
                // Créer le fournisseur
                PreparedStatement insertStmt = conn.prepareStatement(
                    "INSERT INTO fournisseurs (nom, adresse, telephone, email, siret, pharmacie_id) VALUES (?, ?, ?, ?, ?, ?)");
                insertStmt.setString(1, fournisseur[0]);
                insertStmt.setString(2, fournisseur[1]);
                insertStmt.setString(3, fournisseur[2]);
                insertStmt.setString(4, fournisseur[3]);
                insertStmt.setString(5, fournisseur[4]);
                insertStmt.setInt(6, pharmacieId);
                insertStmt.executeUpdate();
                insertStmt.close();
                log("➕ Fournisseur créé: " + fournisseur[0]);
            }
            
            rs.close();
            checkStmt.close();
        }
        
        log("✅ Synchronisation des fournisseurs terminée");
    }
    
    private void synchroniserProduits(Connection conn, int pharmacieId) throws SQLException {
        log("💊 Synchronisation des produits...");
        
        // Produits d'exemple
        String[][] produits = {
            {"Doliprane 1000mg", "Analgésiques", "Paracétamol 1000mg - Boîte de 8 comprimés", "3.50", "5.20", "100", "1"},
            {"Advil 400mg", "Anti-inflammatoires", "Ibuprofène 400mg - Boîte de 20 comprimés", "4.80", "7.20", "75", "1"},
            {"Amoxicilline 500mg", "Antibiotiques", "Amoxicilline 500mg - Boîte de 12 gélules", "8.90", "12.50", "50", "1"},
            {"Cetirizine 10mg", "Antihistaminiques", "Cetirizine 10mg - Boîte de 15 comprimés", "3.20", "4.80", "80", "1"},
            {"Spasfon 80mg", "Antispasmodiques", "Phloroglucinol 80mg - Boîte de 30 comprimés", "6.40", "9.60", "60", "1"},
            {"Kardegic 75mg", "Cardiovasculaires", "Aspirine 75mg - Boîte de 30 comprimés", "2.90", "4.35", "90", "1"},
            {"Biafine", "Dermatologiques", "Émulsion pour application cutanée - Tube 93g", "7.20", "10.80", "40", "1"},
            {"Smecta", "Digestifs", "Diosmectite - Boîte de 30 sachets", "5.60", "8.40", "70", "1"}
        };
        
        for (String[] produit : produits) {
            // Vérifier si le produit existe
            PreparedStatement checkStmt = conn.prepareStatement(
                "SELECT id FROM produits WHERE nom = ? AND pharmacie_id = ?");
            checkStmt.setString(1, produit[0]);
            checkStmt.setInt(2, pharmacieId);
            ResultSet rs = checkStmt.executeQuery();
            
            if (!rs.next()) {
                // Obtenir l'ID de la catégorie
                PreparedStatement catStmt = conn.prepareStatement(
                    "SELECT id FROM categories WHERE nom = ?");
                catStmt.setString(1, produit[1]);
                ResultSet catRs = catStmt.executeQuery();
                int categorieId = 1; // Par défaut
                if (catRs.next()) {
                    categorieId = catRs.getInt("id");
                }
                catRs.close();
                catStmt.close();
                
                // Créer le produit
                PreparedStatement insertStmt = conn.prepareStatement(
                    "INSERT INTO produits (nom, description, prix_achat, prix_vente, stock_actuel, stock_minimum, categorie_id, pharmacie_id, date_creation) VALUES (?, ?, ?, ?, ?, ?, ?, ?, NOW())");
                insertStmt.setString(1, produit[0]);
                insertStmt.setString(2, produit[2]);
                insertStmt.setDouble(3, Double.parseDouble(produit[3]));
                insertStmt.setDouble(4, Double.parseDouble(produit[4]));
                insertStmt.setInt(5, Integer.parseInt(produit[5]));
                insertStmt.setInt(6, Integer.parseInt(produit[6]));
                insertStmt.setInt(7, categorieId);
                insertStmt.setInt(8, pharmacieId);
                insertStmt.executeUpdate();
                insertStmt.close();
                log("➕ Produit créé: " + produit[0]);
            }
            
            rs.close();
            checkStmt.close();
        }
        
        log("✅ Synchronisation des produits terminée");
    }
    
    private void verifierDonneesFinales(Connection conn, int pharmacieId) throws SQLException {
        log("🔍 Vérification des données finales...");
        
        // Compter les produits
        PreparedStatement stmt = conn.prepareStatement(
            "SELECT COUNT(*) FROM produits WHERE pharmacie_id = ?");
        stmt.setInt(1, pharmacieId);
        ResultSet rs = stmt.executeQuery();
        if (rs.next()) {
            int count = rs.getInt(1);
            log("📊 Nombre total de produits pour la pharmacie " + pharmacieId + ": " + count);
        }
        rs.close();
        stmt.close();
        
        // Compter les fournisseurs
        stmt = conn.prepareStatement(
            "SELECT COUNT(*) FROM fournisseurs WHERE pharmacie_id = ?");
        stmt.setInt(1, pharmacieId);
        rs = stmt.executeQuery();
        if (rs.next()) {
            int count = rs.getInt(1);
            log("📊 Nombre total de fournisseurs pour la pharmacie " + pharmacieId + ": " + count);
        }
        rs.close();
        stmt.close();
        
        log("✅ Vérification terminée - Données synchronisées avec succès !");
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }
            
            new SynchronisationDonnees().setVisible(true);
        });
    }
}
