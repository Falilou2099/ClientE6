import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Outil de correction de la synchronisation des données
 * Corrige les problèmes de chargement des produits et fournisseurs
 */
public class CorrectionSynchronisation extends JFrame {
    private JTextArea logArea;
    private JButton btnCorrigerSync;
    private JButton btnTesterDonnees;
    private JButton btnVerifierUtilisateur;
    
    // Configuration de la base de données
    private static final String DB_URL = "jdbc:mysql://localhost:3306/";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "";
    
    public CorrectionSynchronisation() {
        initializeUI();
    }
    
    private void initializeUI() {
        setTitle("🔧 Correction Synchronisation BigPharma");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);
        
        // Panel principal
        JPanel mainPanel = new JPanel(new BorderLayout());
        
        // Panel des boutons
        JPanel buttonPanel = new JPanel(new FlowLayout());
        
        btnCorrigerSync = new JButton("🔄 Corriger Synchronisation");
        btnTesterDonnees = new JButton("🧪 Tester Données");
        btnVerifierUtilisateur = new JButton("👤 Vérifier Utilisateur");
        
        btnCorrigerSync.addActionListener(e -> corrigerSynchronisation());
        btnTesterDonnees.addActionListener(e -> testerDonnees());
        btnVerifierUtilisateur.addActionListener(e -> verifierUtilisateur());
        
        buttonPanel.add(btnCorrigerSync);
        buttonPanel.add(btnTesterDonnees);
        buttonPanel.add(btnVerifierUtilisateur);
        
        // Zone de logs
        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        JScrollPane scrollPane = new JScrollPane(logArea);
        scrollPane.setBorder(BorderFactory.createTitledBorder("📋 Logs de correction"));
        
        mainPanel.add(buttonPanel, BorderLayout.NORTH);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        
        add(mainPanel);
        
        // Message initial
        log("🚀 Outil de correction de synchronisation BigPharma");
        log("📌 Cliquez sur 'Corriger Synchronisation' pour résoudre les problèmes");
        log("=" * 60);
    }
    
    private void log(String message) {
        SwingUtilities.invokeLater(() -> {
            logArea.append(message + "\n");
            logArea.setCaretPosition(logArea.getDocument().getLength());
        });
    }
    
    private void corrigerSynchronisation() {
        log("\n🔄 DÉBUT DE LA CORRECTION DE SYNCHRONISATION");
        log("=" * 50);
        
        try {
            // 1. Vérifier les connexions aux bases
            log("1️⃣ Vérification des connexions aux bases de données...");
            verifierConnexions();
            
            // 2. Corriger la structure des tables
            log("\n2️⃣ Correction de la structure des tables...");
            corrigerStructureTables();
            
            // 3. Synchroniser les données de base
            log("\n3️⃣ Synchronisation des données de base...");
            synchroniserDonneesBase();
            
            // 4. Corriger l'utilisateur cible
            log("\n4️⃣ Correction de l'utilisateur cible...");
            corrigerUtilisateurCible();
            
            // 5. Vérifier les résultats
            log("\n5️⃣ Vérification des résultats...");
            verifierResultats();
            
            log("\n✅ CORRECTION TERMINÉE AVEC SUCCÈS !");
            log("🎉 Les données sont maintenant synchronisées entre Java et PHP");
            
        } catch (Exception e) {
            log("❌ ERREUR lors de la correction : " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void verifierConnexions() throws SQLException {
        // Test connexion bigpharma
        try (Connection conn = DriverManager.getConnection(DB_URL + "bigpharma", DB_USER, DB_PASSWORD)) {
            log("✅ Connexion à bigpharma : OK");
        } catch (SQLException e) {
            log("❌ Erreur connexion bigpharma : " + e.getMessage());
            throw e;
        }
        
        // Test connexion clientlegerlourd
        try (Connection conn = DriverManager.getConnection(DB_URL + "clientlegerlourd", DB_USER, DB_PASSWORD)) {
            log("✅ Connexion à clientlegerlourd : OK");
        } catch (SQLException e) {
            log("⚠️ Base clientlegerlourd non trouvée, elle sera créée");
        }
    }
    
    private void corrigerStructureTables() throws SQLException {
        try (Connection conn = DriverManager.getConnection(DB_URL + "bigpharma", DB_USER, DB_PASSWORD)) {
            
            // Ajouter pharmacie_id aux fournisseurs si manquant
            try {
                String alterFournisseurs = "ALTER TABLE fournisseurs ADD COLUMN pharmacie_id INT DEFAULT 1";
                conn.createStatement().executeUpdate(alterFournisseurs);
                log("✅ Colonne pharmacie_id ajoutée à fournisseurs");
            } catch (SQLException e) {
                if (e.getMessage().contains("Duplicate column")) {
                    log("ℹ️ Colonne pharmacie_id existe déjà dans fournisseurs");
                } else {
                    log("⚠️ Erreur ajout colonne fournisseurs : " + e.getMessage());
                }
            }
            
            // Ajouter colonnes manquantes aux produits
            String[] colonnesProduits = {
                "pharmacie_id INT DEFAULT 1",
                "categorie VARCHAR(100)",
                "quantite_stock INT DEFAULT 0",
                "prix_achat DECIMAL(10,2) DEFAULT 0.00",
                "image_url VARCHAR(500)"
            };
            
            for (String colonne : colonnesProduits) {
                try {
                    String alterProduits = "ALTER TABLE produits ADD COLUMN " + colonne;
                    conn.createStatement().executeUpdate(alterProduits);
                    log("✅ Colonne ajoutée à produits : " + colonne.split(" ")[0]);
                } catch (SQLException e) {
                    if (e.getMessage().contains("Duplicate column")) {
                        log("ℹ️ Colonne existe déjà : " + colonne.split(" ")[0]);
                    } else {
                        log("⚠️ Erreur ajout colonne : " + e.getMessage());
                    }
                }
            }
            
            // Créer table categories si manquante
            try {
                String createCategories = """
                    CREATE TABLE IF NOT EXISTS categories (
                        id INT PRIMARY KEY AUTO_INCREMENT,
                        nom VARCHAR(100) NOT NULL,
                        description TEXT,
                        pharmacie_id INT DEFAULT 1,
                        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                    )
                """;
                conn.createStatement().executeUpdate(createCategories);
                log("✅ Table categories créée/vérifiée");
            } catch (SQLException e) {
                log("⚠️ Erreur création table categories : " + e.getMessage());
            }
        }
    }
    
    private void synchroniserDonneesBase() throws SQLException {
        try (Connection conn = DriverManager.getConnection(DB_URL + "bigpharma", DB_USER, DB_PASSWORD)) {
            
            // Insérer pharmacie par défaut
            String insertPharmacie = """
                INSERT IGNORE INTO pharmacies (id, nom, adresse, telephone, email) 
                VALUES (1, 'Pharmacie BigPharma', '123 Rue de la Santé, 75000 Paris', 
                        '01.23.45.67.89', 'contact@bigpharma.fr')
            """;
            conn.createStatement().executeUpdate(insertPharmacie);
            log("✅ Pharmacie par défaut configurée");
            
            // Insérer catégories
            String[] categories = {
                "Analgésiques", "Anti-inflammatoires", "Antibiotiques", "Antihistaminiques",
                "Vitamines", "Antispasmodiques", "Antiseptiques", "Cardiovasculaires",
                "Dermatologiques", "Digestifs", "Neurologiques", "Ophtalmologiques",
                "ORL", "Respiratoires", "Urologiques", "Gynécologiques", "Pédiatriques"
            };
            
            for (int i = 0; i < categories.length; i++) {
                String insertCategorie = """
                    INSERT IGNORE INTO categories (id, nom, description, pharmacie_id) 
                    VALUES (?, ?, ?, 1)
                """;
                try (PreparedStatement pstmt = conn.prepareStatement(insertCategorie)) {
                    pstmt.setInt(1, i + 1);
                    pstmt.setString(2, categories[i]);
                    pstmt.setString(3, "Catégorie " + categories[i]);
                    pstmt.executeUpdate();
                }
            }
            log("✅ " + categories.length + " catégories synchronisées");
            
            // Insérer fournisseurs
            String[][] fournisseurs = {
                {"1", "Laboratoires Sanofi", "54 Rue La Boétie, 75008 Paris", "01.53.77.40.00", "contact@sanofi.com", "12345678901234"},
                {"2", "Pfizer France", "23-25 Avenue du Docteur Lannelongue, 75014 Paris", "01.58.07.34.40", "contact@pfizer.fr", "23456789012345"},
                {"3", "Laboratoires Novartis", "8-10 Rue Henri Sainte-Claire Deville, 92500 Rueil-Malmaison", "01.55.47.60.00", "contact@novartis.fr", "34567890123456"},
                {"4", "Roche France", "30 Cours de l'Île Seguin, 92100 Boulogne-Billancourt", "01.46.40.50.00", "contact@roche.fr", "45678901234567"},
                {"5", "Merck France", "37 Rue Saint-Romain, 69008 Lyon", "04.72.78.09.00", "contact@merck.fr", "56789012345678"}
            };
            
            for (String[] fournisseur : fournisseurs) {
                String insertFournisseur = """
                    INSERT IGNORE INTO fournisseurs (id, nom, adresse, telephone, email, siret, pharmacie_id) 
                    VALUES (?, ?, ?, ?, ?, ?, 1)
                """;
                try (PreparedStatement pstmt = conn.prepareStatement(insertFournisseur)) {
                    pstmt.setInt(1, Integer.parseInt(fournisseur[0]));
                    pstmt.setString(2, fournisseur[1]);
                    pstmt.setString(3, fournisseur[2]);
                    pstmt.setString(4, fournisseur[3]);
                    pstmt.setString(5, fournisseur[4]);
                    pstmt.setString(6, fournisseur[5]);
                    pstmt.executeUpdate();
                }
            }
            log("✅ " + fournisseurs.length + " fournisseurs synchronisés");
            
            // Mettre à jour les pharmacie_id existants
            conn.createStatement().executeUpdate("UPDATE fournisseurs SET pharmacie_id = 1 WHERE pharmacie_id IS NULL OR pharmacie_id = 0");
            conn.createStatement().executeUpdate("UPDATE produits SET pharmacie_id = 1 WHERE pharmacie_id IS NULL OR pharmacie_id = 0");
            log("✅ IDs de pharmacie mis à jour");
        }
    }
    
    private void corrigerUtilisateurCible() throws SQLException {
        try (Connection conn = DriverManager.getConnection(DB_URL + "bigpharma", DB_USER, DB_PASSWORD)) {
            
            // Supprimer l'utilisateur existant s'il existe
            String deleteUser = "DELETE FROM utilisateurs WHERE email = 'tourefaliloumbacke12345@gmail.com'";
            conn.createStatement().executeUpdate(deleteUser);
            
            // Insérer l'utilisateur avec le bon mot de passe
            String insertUser = """
                INSERT INTO utilisateurs (nom, prenom, email, mot_de_passe, role, pharmacie_id, actif) 
                VALUES ('Mbacke', 'Toure Falilou', 'tourefaliloumbacke12345@gmail.com', 
                        SHA2('password', 256), 'admin', 1, 1)
            """;
            conn.createStatement().executeUpdate(insertUser);
            log("✅ Utilisateur cible configuré avec mot de passe : password");
        }
    }
    
    private void verifierResultats() throws SQLException {
        try (Connection conn = DriverManager.getConnection(DB_URL + "bigpharma", DB_USER, DB_PASSWORD)) {
            
            // Compter les données
            ResultSet rs = conn.createStatement().executeQuery(
                "SELECT COUNT(*) as count FROM utilisateurs WHERE email = 'tourefaliloumbacke12345@gmail.com'"
            );
            rs.next();
            log("👤 Utilisateurs trouvés : " + rs.getInt("count"));
            
            rs = conn.createStatement().executeQuery(
                "SELECT COUNT(*) as count FROM produits WHERE pharmacie_id = 1"
            );
            rs.next();
            log("📦 Produits pour pharmacie 1 : " + rs.getInt("count"));
            
            rs = conn.createStatement().executeQuery(
                "SELECT COUNT(*) as count FROM fournisseurs WHERE pharmacie_id = 1"
            );
            rs.next();
            log("🏭 Fournisseurs pour pharmacie 1 : " + rs.getInt("count"));
            
            rs = conn.createStatement().executeQuery(
                "SELECT COUNT(*) as count FROM categories WHERE pharmacie_id = 1"
            );
            rs.next();
            log("📂 Catégories pour pharmacie 1 : " + rs.getInt("count"));
        }
    }
    
    private void testerDonnees() {
        log("\n🧪 TEST DES DONNÉES");
        log("=" * 30);
        
        try {
            // Tester le chargement des produits
            log("📦 Test chargement produits...");
            List<String> produits = chargerProduits(1);
            log("✅ Produits chargés : " + produits.size());
            for (int i = 0; i < Math.min(5, produits.size()); i++) {
                log("   - " + produits.get(i));
            }
            if (produits.size() > 5) {
                log("   ... et " + (produits.size() - 5) + " autres");
            }
            
            // Tester le chargement des fournisseurs
            log("\n🏭 Test chargement fournisseurs...");
            List<String> fournisseurs = chargerFournisseurs(1);
            log("✅ Fournisseurs chargés : " + fournisseurs.size());
            for (String fournisseur : fournisseurs) {
                log("   - " + fournisseur);
            }
            
            // Tester le chargement des catégories
            log("\n📂 Test chargement catégories...");
            List<String> categories = chargerCategories(1);
            log("✅ Catégories chargées : " + categories.size());
            for (int i = 0; i < Math.min(10, categories.size()); i++) {
                log("   - " + categories.get(i));
            }
            if (categories.size() > 10) {
                log("   ... et " + (categories.size() - 10) + " autres");
            }
            
        } catch (Exception e) {
            log("❌ Erreur lors du test : " + e.getMessage());
        }
    }
    
    private void verifierUtilisateur() {
        log("\n👤 VÉRIFICATION UTILISATEUR CIBLE");
        log("=" * 40);
        
        try (Connection conn = DriverManager.getConnection(DB_URL + "bigpharma", DB_USER, DB_PASSWORD)) {
            String query = """
                SELECT nom, prenom, email, role, pharmacie_id, actif, 
                       DATE_FORMAT(created_at, '%d/%m/%Y %H:%i') as date_creation
                FROM utilisateurs 
                WHERE email = 'tourefaliloumbacke12345@gmail.com'
            """;
            
            ResultSet rs = conn.createStatement().executeQuery(query);
            if (rs.next()) {
                log("✅ Utilisateur trouvé :");
                log("   📧 Email : " + rs.getString("email"));
                log("   👤 Nom : " + rs.getString("prenom") + " " + rs.getString("nom"));
                log("   🔑 Rôle : " + rs.getString("role"));
                log("   🏥 Pharmacie ID : " + rs.getInt("pharmacie_id"));
                log("   ✅ Actif : " + (rs.getBoolean("actif") ? "Oui" : "Non"));
                log("   📅 Créé le : " + rs.getString("date_creation"));
                log("   🔐 Mot de passe : password (SHA-256)");
            } else {
                log("❌ Utilisateur non trouvé !");
                log("💡 Cliquez sur 'Corriger Synchronisation' pour le créer");
            }
            
        } catch (SQLException e) {
            log("❌ Erreur vérification utilisateur : " + e.getMessage());
        }
    }
    
    private List<String> chargerProduits(int pharmacieId) throws SQLException {
        List<String> produits = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(DB_URL + "bigpharma", DB_USER, DB_PASSWORD)) {
            String query = "SELECT nom, prix_vente, quantite_stock FROM produits WHERE pharmacie_id = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(query)) {
                pstmt.setInt(1, pharmacieId);
                ResultSet rs = pstmt.executeQuery();
                while (rs.next()) {
                    produits.add(rs.getString("nom") + " - " + rs.getDouble("prix_vente") + "€ (Stock: " + rs.getInt("quantite_stock") + ")");
                }
            }
        }
        return produits;
    }
    
    private List<String> chargerFournisseurs(int pharmacieId) throws SQLException {
        List<String> fournisseurs = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(DB_URL + "bigpharma", DB_USER, DB_PASSWORD)) {
            String query = "SELECT nom, telephone, email FROM fournisseurs WHERE pharmacie_id = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(query)) {
                pstmt.setInt(1, pharmacieId);
                ResultSet rs = pstmt.executeQuery();
                while (rs.next()) {
                    fournisseurs.add(rs.getString("nom") + " - " + rs.getString("telephone"));
                }
            }
        }
        return fournisseurs;
    }
    
    private List<String> chargerCategories(int pharmacieId) throws SQLException {
        List<String> categories = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(DB_URL + "bigpharma", DB_USER, DB_PASSWORD)) {
            String query = "SELECT nom FROM categories WHERE pharmacie_id = ? ORDER BY nom";
            try (PreparedStatement pstmt = conn.prepareStatement(query)) {
                pstmt.setInt(1, pharmacieId);
                ResultSet rs = pstmt.executeQuery();
                while (rs.next()) {
                    categories.add(rs.getString("nom"));
                }
            }
        }
        return categories;
    }
    
    public static void main(String[] args) {
        // Charger le driver MySQL
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("Driver MySQL non trouvé : " + e.getMessage());
            System.err.println("Veuillez installer mysql-connector-java");
        }
        
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                // Utiliser le look par défaut
            }
            
            new CorrectionSynchronisation().setVisible(true);
        });
    }
}
