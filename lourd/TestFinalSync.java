import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Test final de synchronisation pour l'application Java BigPharma
 * Vérifie la cohérence des données avec l'application PHP
 */
public class TestFinalSync extends JFrame {
    private JTextArea logArea;
    private JButton testButton;
    private JButton syncButton;
    private Connection connection;
    
    // Configuration de la base de données
    private static final String DB_URL = "jdbc:mysql://localhost:3306/bigpharma";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "";
    private static final String TARGET_EMAIL = "tourefaliloumbacke12345@gmail.com";
    
    public TestFinalSync() {
        initializeUI();
        connectToDatabase();
    }
    
    private void initializeUI() {
        setTitle("🔍 Test Final de Synchronisation BigPharma");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);
        
        // Layout principal
        setLayout(new BorderLayout());
        
        // Panel du haut avec boutons
        JPanel topPanel = new JPanel(new FlowLayout());
        testButton = new JButton("🔍 Tester la Synchronisation");
        syncButton = new JButton("🔄 Synchroniser les Données");
        
        testButton.addActionListener(e -> runSyncTest());
        syncButton.addActionListener(e -> synchronizeData());
        
        topPanel.add(testButton);
        topPanel.add(syncButton);
        add(topPanel, BorderLayout.NORTH);
        
        // Zone de logs
        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        logArea.setBackground(new Color(248, 248, 248));
        
        JScrollPane scrollPane = new JScrollPane(logArea);
        scrollPane.setBorder(BorderFactory.createTitledBorder("📋 Logs de Test"));
        add(scrollPane, BorderLayout.CENTER);
        
        // Panel du bas avec informations
        JPanel bottomPanel = new JPanel();
        bottomPanel.add(new JLabel("🏥 BigPharma - Test de Synchronisation Java ↔ PHP"));
        add(bottomPanel, BorderLayout.SOUTH);
        
        log("🚀 Interface de test initialisée");
        log("📊 Prêt pour les tests de synchronisation");
    }
    
    private void connectToDatabase() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
            log("✅ Connexion à la base bigpharma établie");
        } catch (Exception e) {
            log("❌ Erreur de connexion à la base: " + e.getMessage());
            log("⚠️ Certains tests ne pourront pas être effectués");
        }
    }
    
    private void runSyncTest() {
        log("\n🔍 === DÉBUT DU TEST DE SYNCHRONISATION ===");
        
        if (connection == null) {
            log("❌ Pas de connexion à la base de données");
            return;
        }
        
        try {
            // Test 1: Vérifier l'utilisateur cible
            log("\n📋 Test 1: Vérification de l'utilisateur cible");
            testTargetUser();
            
            // Test 2: Compter les produits
            log("\n📋 Test 2: Vérification des produits");
            testProducts();
            
            // Test 3: Compter les catégories
            log("\n📋 Test 3: Vérification des catégories");
            testCategories();
            
            // Test 4: Compter les fournisseurs
            log("\n📋 Test 4: Vérification des fournisseurs");
            testSuppliers();
            
            // Test 5: Vérifier les tables nécessaires
            log("\n📋 Test 5: Vérification des tables");
            testTables();
            
            log("\n🎉 === TEST DE SYNCHRONISATION TERMINÉ ===");
            
        } catch (Exception e) {
            log("❌ Erreur pendant les tests: " + e.getMessage());
        }
    }
    
    private void testTargetUser() {
        try {
            String sql = "SELECT * FROM utilisateurs WHERE email = ?";
            PreparedStatement stmt = connection.prepareStatement(sql);
            stmt.setString(1, TARGET_EMAIL);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                log("✅ Utilisateur trouvé: " + rs.getString("nom") + " " + rs.getString("prenom"));
                log("📧 Email: " + rs.getString("email"));
                log("👑 Rôle: " + rs.getString("role"));
                log("🏥 Pharmacie ID: " + rs.getInt("pharmacie_id"));
                
                // Test du mot de passe
                String storedPassword = rs.getString("mot_de_passe");
                String testPassword = hashPassword("password");
                if (storedPassword.equals(testPassword)) {
                    log("✅ Mot de passe correct (SHA-256)");
                } else {
                    log("⚠️ Mot de passe incorrect ou format différent");
                }
            } else {
                log("❌ Utilisateur " + TARGET_EMAIL + " non trouvé");
                log("💡 Exécutez la synchronisation pour créer l'utilisateur");
            }
            
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            log("❌ Erreur lors du test utilisateur: " + e.getMessage());
        }
    }
    
    private void testProducts() {
        try {
            String sql = "SELECT COUNT(*) as count FROM produits";
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            
            if (rs.next()) {
                int count = rs.getInt("count");
                log("📦 Nombre de produits: " + count);
                
                if (count > 0) {
                    log("✅ Des produits sont présents dans la base");
                    
                    // Afficher quelques produits
                    String detailSql = "SELECT nom, prix_vente, stock FROM produits LIMIT 5";
                    ResultSet detailRs = stmt.executeQuery(detailSql);
                    log("📋 Exemples de produits:");
                    while (detailRs.next()) {
                        log("  • " + detailRs.getString("nom") + 
                            " - " + detailRs.getDouble("prix_vente") + "€" +
                            " (Stock: " + detailRs.getInt("stock") + ")");
                    }
                    detailRs.close();
                } else {
                    log("⚠️ Aucun produit trouvé");
                    log("💡 Exécutez la synchronisation pour ajouter des produits");
                }
            }
            
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            log("❌ Erreur lors du test produits: " + e.getMessage());
        }
    }
    
    private void testCategories() {
        try {
            String sql = "SELECT COUNT(*) as count FROM categories";
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            
            if (rs.next()) {
                int count = rs.getInt("count");
                log("🏷️ Nombre de catégories: " + count);
                
                if (count >= 17) {
                    log("✅ Catégories complètes (17 attendues)");
                } else if (count > 0) {
                    log("⚠️ Catégories incomplètes (" + count + "/17)");
                } else {
                    log("❌ Aucune catégorie trouvée");
                }
            }
            
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            log("❌ Erreur lors du test catégories: " + e.getMessage());
        }
    }
    
    private void testSuppliers() {
        try {
            String sql = "SELECT COUNT(*) as count FROM fournisseurs";
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            
            if (rs.next()) {
                int count = rs.getInt("count");
                log("🏭 Nombre de fournisseurs: " + count);
                
                if (count >= 5) {
                    log("✅ Fournisseurs suffisants (5+ attendus)");
                } else if (count > 0) {
                    log("⚠️ Peu de fournisseurs (" + count + ")");
                } else {
                    log("❌ Aucun fournisseur trouvé");
                }
            }
            
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            log("❌ Erreur lors du test fournisseurs: " + e.getMessage());
        }
    }
    
    private void testTables() {
        String[] requiredTables = {"utilisateurs", "produits", "categories", "fournisseurs", "pharmacies"};
        
        try {
            DatabaseMetaData metaData = connection.getMetaData();
            
            for (String tableName : requiredTables) {
                ResultSet rs = metaData.getTables(null, null, tableName, null);
                if (rs.next()) {
                    log("✅ Table " + tableName + " existe");
                } else {
                    log("❌ Table " + tableName + " manquante");
                }
                rs.close();
            }
        } catch (SQLException e) {
            log("❌ Erreur lors de la vérification des tables: " + e.getMessage());
        }
    }
    
    private void synchronizeData() {
        log("\n🔄 === DÉBUT DE LA SYNCHRONISATION ===");
        
        if (connection == null) {
            log("❌ Pas de connexion à la base de données");
            return;
        }
        
        try {
            // Créer l'utilisateur s'il n'existe pas
            createTargetUser();
            
            // Insérer des données de base
            insertBasicData();
            
            log("✅ Synchronisation terminée");
            log("💡 Relancez le test pour vérifier les résultats");
            
        } catch (Exception e) {
            log("❌ Erreur pendant la synchronisation: " + e.getMessage());
        }
    }
    
    private void createTargetUser() {
        try {
            // Vérifier si l'utilisateur existe
            String checkSql = "SELECT COUNT(*) FROM utilisateurs WHERE email = ?";
            PreparedStatement checkStmt = connection.prepareStatement(checkSql);
            checkStmt.setString(1, TARGET_EMAIL);
            ResultSet rs = checkStmt.executeQuery();
            
            rs.next();
            int count = rs.getInt(1);
            rs.close();
            checkStmt.close();
            
            if (count == 0) {
                // Créer l'utilisateur
                String insertSql = "INSERT INTO utilisateurs (nom, prenom, email, mot_de_passe, role, pharmacie_id, actif) VALUES (?, ?, ?, ?, ?, ?, ?)";
                PreparedStatement insertStmt = connection.prepareStatement(insertSql);
                insertStmt.setString(1, "Mbacke");
                insertStmt.setString(2, "Toure Falilou");
                insertStmt.setString(3, TARGET_EMAIL);
                insertStmt.setString(4, hashPassword("password"));
                insertStmt.setString(5, "admin");
                insertStmt.setInt(6, 1);
                insertStmt.setBoolean(7, true);
                
                insertStmt.executeUpdate();
                insertStmt.close();
                
                log("✅ Utilisateur " + TARGET_EMAIL + " créé");
            } else {
                log("ℹ️ Utilisateur " + TARGET_EMAIL + " existe déjà");
            }
            
        } catch (SQLException e) {
            log("❌ Erreur lors de la création de l'utilisateur: " + e.getMessage());
        }
    }
    
    private void insertBasicData() {
        try {
            // Insérer une pharmacie par défaut
            String pharmacieSql = "INSERT IGNORE INTO pharmacies (id, nom, adresse, telephone, email) VALUES (1, 'Pharmacie BigPharma', '123 Rue de la Santé, 75000 Paris', '01.23.45.67.89', 'contact@bigpharma.fr')";
            Statement stmt = connection.createStatement();
            stmt.executeUpdate(pharmacieSql);
            
            log("✅ Pharmacie par défaut configurée");
            stmt.close();
            
        } catch (SQLException e) {
            log("❌ Erreur lors de l'insertion des données de base: " + e.getMessage());
        }
    }
    
    private String hashPassword(String password) {
        try {
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(password.getBytes("UTF-8"));
            StringBuilder hexString = new StringBuilder();
            
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            
            return hexString.toString();
        } catch (Exception e) {
            return password; // Fallback
        }
    }
    
    private void log(String message) {
        SwingUtilities.invokeLater(() -> {
            logArea.append(message + "\n");
            logArea.setCaretPosition(logArea.getDocument().getLength());
        });
        System.out.println(message); // Aussi dans la console
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                // Utiliser le look par défaut
            }
            
            new TestFinalSync().setVisible(true);
        });
    }
}
