<?php
/**
 * Script de synchronisation des données entre l'application PHP et Java
 * Ce script met à jour la base de données pour assurer la cohérence entre les deux applications
 */

require_once '../config/database.php';

class DatabaseSync {
    private $pdo;
    
    public function __construct() {
        $this->pdo = Database::getInstance()->getConnection();
    }
    
    /**
     * Synchronise toutes les données entre PHP et Java
     */
    public function syncAll() {
        echo "=== SYNCHRONISATION BASE DE DONNÉES ===\n";
        
        $this->createMissingTables();
        $this->syncUsers();
        $this->syncProducts();
        $this->syncStock();
        $this->addMissingColumns();
        
        echo "=== SYNCHRONISATION TERMINÉE ===\n";
    }
    
    /**
     * Crée les tables manquantes pour la synchronisation
     */
    private function createMissingTables() {
        echo "Création des tables manquantes...\n";
        
        // Table des tentatives de connexion
        $this->pdo->exec("
            CREATE TABLE IF NOT EXISTS login_attempts (
                id INT AUTO_INCREMENT PRIMARY KEY,
                email VARCHAR(255) NOT NULL,
                attempt_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                success BOOLEAN NOT NULL DEFAULT FALSE,
                ip_address VARCHAR(45),
                INDEX idx_email_time (email, attempt_time),
                INDEX idx_success (success)
            )
        ");
        
        // Table des tokens de réinitialisation
        $this->pdo->exec("
            CREATE TABLE IF NOT EXISTS password_resets (
                id INT AUTO_INCREMENT PRIMARY KEY,
                email VARCHAR(255) NOT NULL,
                token VARCHAR(255) NOT NULL,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                INDEX idx_email (email),
                INDEX idx_token (token)
            )
        ");
        
        // Table des activités (si elle n'existe pas)
        $this->pdo->exec("
            CREATE TABLE IF NOT EXISTS activites (
                id INT AUTO_INCREMENT PRIMARY KEY,
                type VARCHAR(50) NOT NULL,
                description TEXT NOT NULL,
                utilisateur VARCHAR(100) NOT NULL,
                pharmacie_id INT NOT NULL,
                date_creation TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                INDEX idx_pharmacie (pharmacie_id),
                INDEX idx_type (type)
            )
        ");
        
        echo "Tables créées avec succès.\n";
    }
    
    /**
     * Synchronise les utilisateurs entre PHP et Java
     */
    private function syncUsers() {
        echo "Synchronisation des utilisateurs...\n";
        
        // Vérifier que tous les utilisateurs ont un pharmacie_id
        $stmt = $this->pdo->query("SELECT COUNT(*) FROM utilisateurs WHERE pharmacie_id IS NULL OR pharmacie_id = 0");
        $usersWithoutPharmacy = $stmt->fetchColumn();
        
        if ($usersWithoutPharmacy > 0) {
            // Assigner pharmacie_id = 1 par défaut aux utilisateurs sans pharmacie
            $this->pdo->exec("UPDATE utilisateurs SET pharmacie_id = 1 WHERE pharmacie_id IS NULL OR pharmacie_id = 0");
            echo "Assigné pharmacie_id = 1 à $usersWithoutPharmacy utilisateurs.\n";
        }
        
        // Vérifier que l'utilisateur test existe
        $stmt = $this->pdo->prepare("SELECT COUNT(*) FROM utilisateurs WHERE email = ?");
        $stmt->execute(['tourefalilou12345@gmail.com']);
        
        if ($stmt->fetchColumn() == 0) {
            // Créer l'utilisateur test
            $hashedPassword = hash('sha256', 'password123');
            $stmt = $this->pdo->prepare("
                INSERT INTO utilisateurs (nom, prenom, email, mot_de_passe, role, pharmacie_id, created_at) 
                VALUES (?, ?, ?, ?, ?, ?, NOW())
            ");
            $stmt->execute(['Toure', 'Falilou', 'tourefalilou12345@gmail.com', $hashedPassword, 'admin', 1]);
            echo "Utilisateur test créé : tourefalilou12345@gmail.com\n";
        }
        
        echo "Synchronisation des utilisateurs terminée.\n";
    }
    
    /**
     * Synchronise les produits entre PHP et Java
     */
    private function syncProducts() {
        echo "Synchronisation des produits...\n";
        
        // Vérifier que tous les produits ont un pharmacie_id
        $stmt = $this->pdo->query("SELECT COUNT(*) FROM produits WHERE pharmacie_id IS NULL OR pharmacie_id = 0");
        $productsWithoutPharmacy = $stmt->fetchColumn();
        
        if ($productsWithoutPharmacy > 0) {
            $this->pdo->exec("UPDATE produits SET pharmacie_id = 1 WHERE pharmacie_id IS NULL OR pharmacie_id = 0");
            echo "Assigné pharmacie_id = 1 à $productsWithoutPharmacy produits.\n";
        }
        
        // Ajouter des produits de test si la table est vide
        $stmt = $this->pdo->query("SELECT COUNT(*) FROM produits");
        $productCount = $stmt->fetchColumn();
        
        if ($productCount == 0) {
            $this->addSampleProducts();
        }
        
        echo "Synchronisation des produits terminée.\n";
    }
    
    /**
     * Ajoute des produits d'exemple
     */
    private function addSampleProducts() {
        echo "Ajout de produits d'exemple...\n";
        
        $products = [
            ['Paracétamol 500mg', 'Analgésique et antipyrétique', 2.50, 3.50, 'Analgésiques', 100, 'https://via.placeholder.com/150x150?text=Paracetamol'],
            ['Ibuprofène 400mg', 'Anti-inflammatoire non stéroïdien', 3.20, 4.80, 'Anti-inflammatoires', 75, 'https://via.placeholder.com/150x150?text=Ibuprofene'],
            ['Amoxicilline 500mg', 'Antibiotique à large spectre', 8.50, 12.00, 'Antibiotiques', 50, 'https://via.placeholder.com/150x150?text=Amoxicilline'],
            ['Vitamine C 1000mg', 'Complément vitaminique', 5.00, 7.50, 'Vitamines', 80, 'https://via.placeholder.com/150x150?text=VitamineC'],
            ['Aspirine 500mg', 'Analgésique et anticoagulant', 2.80, 4.20, 'Analgésiques', 90, 'https://via.placeholder.com/150x150?text=Aspirine']
        ];
        
        $stmt = $this->pdo->prepare("
            INSERT INTO produits (nom, description, prix_achat, prix_vente, categorie, quantite_stock, pharmacie_id, image_url, seuil_alerte) 
            VALUES (?, ?, ?, ?, ?, ?, 1, ?, 10)
        ");
        
        foreach ($products as $product) {
            $stmt->execute($product);
        }
        
        echo "Ajouté " . count($products) . " produits d'exemple.\n";
    }
    
    /**
     * Synchronise les stocks entre PHP et Java
     */
    private function syncStock() {
        echo "Synchronisation des stocks...\n";
        
        // Créer la table stocks si elle n'existe pas
        $this->pdo->exec("
            CREATE TABLE IF NOT EXISTS stocks (
                id INT AUTO_INCREMENT PRIMARY KEY,
                produit_id INT NOT NULL,
                pharmacie_id INT NOT NULL,
                quantite INT NOT NULL DEFAULT 0,
                seuil_minimum INT NOT NULL DEFAULT 10,
                derniere_maj TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                FOREIGN KEY (produit_id) REFERENCES produits(id) ON DELETE CASCADE,
                UNIQUE KEY unique_produit_pharmacie (produit_id, pharmacie_id),
                INDEX idx_pharmacie (pharmacie_id)
            )
        ");
        
        // Synchroniser les stocks avec les produits
        $this->pdo->exec("
            INSERT IGNORE INTO stocks (produit_id, pharmacie_id, quantite, seuil_minimum)
            SELECT id, pharmacie_id, quantite_stock, seuil_alerte 
            FROM produits 
            WHERE pharmacie_id IS NOT NULL
        ");
        
        echo "Synchronisation des stocks terminée.\n";
    }
    
    /**
     * Ajoute les colonnes manquantes aux tables existantes
     */
    private function addMissingColumns() {
        echo "Ajout des colonnes manquantes...\n";
        
        try {
            // Ajouter image_url aux produits si elle n'existe pas
            $this->pdo->exec("ALTER TABLE produits ADD COLUMN image_url TEXT");
            echo "Colonne image_url ajoutée à la table produits.\n";
        } catch (PDOException $e) {
            if (strpos($e->getMessage(), 'Duplicate column') === false) {
                echo "Erreur lors de l'ajout de image_url: " . $e->getMessage() . "\n";
            }
        }
        
        try {
            // Ajouter seuil_alerte aux produits si elle n'existe pas
            $this->pdo->exec("ALTER TABLE produits ADD COLUMN seuil_alerte INT DEFAULT 10");
            echo "Colonne seuil_alerte ajoutée à la table produits.\n";
        } catch (PDOException $e) {
            if (strpos($e->getMessage(), 'Duplicate column') === false) {
                echo "Erreur lors de l'ajout de seuil_alerte: " . $e->getMessage() . "\n";
            }
        }
        
        try {
            // Ajouter date_expiration aux produits si elle n'existe pas
            $this->pdo->exec("ALTER TABLE produits ADD COLUMN date_expiration DATE");
            echo "Colonne date_expiration ajoutée à la table produits.\n";
        } catch (PDOException $e) {
            if (strpos($e->getMessage(), 'Duplicate column') === false) {
                echo "Erreur lors de l'ajout de date_expiration: " . $e->getMessage() . "\n";
            }
        }
        
        echo "Ajout des colonnes terminé.\n";
    }
    
    /**
     * Met à jour les URLs d'images par défaut pour les produits sans image
     */
    public function updateDefaultImages() {
        echo "Mise à jour des images par défaut...\n";
        
        $this->pdo->exec("
            UPDATE produits 
            SET image_url = 'https://via.placeholder.com/150x150?text=Produit' 
            WHERE image_url IS NULL OR image_url = ''
        ");
        
        echo "Images par défaut mises à jour.\n";
    }
    
    /**
     * Nettoie les anciens tokens de réinitialisation (plus de 24h)
     */
    public function cleanupOldTokens() {
        echo "Nettoyage des anciens tokens...\n";
        
        $stmt = $this->pdo->exec("DELETE FROM password_resets WHERE created_at < DATE_SUB(NOW(), INTERVAL 24 HOUR)");
        echo "Supprimé $stmt anciens tokens.\n";
    }
}

// Exécution du script
if (php_sapi_name() === 'cli' || isset($_GET['sync'])) {
    $sync = new DatabaseSync();
    $sync->syncAll();
    $sync->updateDefaultImages();
    $sync->cleanupOldTokens();
    
    if (isset($_GET['sync'])) {
        echo "<pre>Synchronisation terminée avec succès!</pre>";
    }
} else {
    echo "Ce script doit être exécuté en ligne de commande ou avec le paramètre ?sync";
}
?>
