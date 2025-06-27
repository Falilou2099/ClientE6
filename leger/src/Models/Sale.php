<?php
namespace Models;

class Sale {
    private $id;
    private $product_id;
    private $client_id;
    private $pharmacy_id;
    private $quantite;
    private $prix_unitaire;
    private $prix_total;
    private $date_vente;
    private static $pdo;

    // Initialiser la connexion PDO
    private static function initPDO() {
        if (self::$pdo === null) {
            $dbConfig = require_once __DIR__ . '/../../config/database.php';
            self::$pdo = $GLOBALS['pdo'] ?? null;
            
            if (self::$pdo === null) {
                // Connexion de secours si $pdo n'est pas disponible globalement
                try {
                    $host = 'localhost';
                    $db_name = 'clientlegerlourd';
                    $username = 'root';
                    $password = '';
                    
                    self::$pdo = new \PDO(
                        "mysql:host={$host};dbname={$db_name}", 
                        $username, 
                        $password
                    );
                    
                    self::$pdo->setAttribute(\PDO::ATTR_ERRMODE, \PDO::ERRMODE_EXCEPTION);
                    self::$pdo->setAttribute(\PDO::ATTR_DEFAULT_FETCH_MODE, \PDO::FETCH_ASSOC);
                    self::$pdo->exec("SET NAMES utf8");
                } catch(\PDOException $e) {
                    error_log("Erreur de connexion à la base de données : " . $e->getMessage());
                    die("Impossible de se connecter à la base de données. Veuillez vérifier vos paramètres de connexion.");
                }
            }
        }
        return self::$pdo;
    }

    // Constructeur
    public function __construct(
        $id = null,
        $product_id = null,
        $client_id = null,
        $pharmacy_id = null,
        $quantite = 1,
        $prix_unitaire = null,
        $prix_total = null,
        $date_vente = null
    ) {
        $this->id = $id;
        $this->product_id = $product_id;
        $this->client_id = $client_id;
        $this->pharmacy_id = $pharmacy_id;
        $this->quantite = $quantite;
        $this->prix_unitaire = $prix_unitaire;
        $this->prix_total = $prix_total ?? ($prix_unitaire * $quantite);
        $this->date_vente = $date_vente ?? date('Y-m-d H:i:s');
    }

    // Getters
    public function getId() { return $this->id; }
    public function getProductId() { return $this->product_id; }
    public function getClientId() { return $this->client_id; }
    public function getPharmacyId() { return $this->pharmacy_id; }
    public function getQuantite() { return $this->quantite; }
    public function getPrixUnitaire() { return $this->prix_unitaire; }
    public function getPrixTotal() { return $this->prix_total; }
    public function getDateVente() { return $this->date_vente; }

    // Méthode pour sauvegarder une vente
    public function save() {
        $pdo = self::initPDO();
        
        try {
            // Validation des données avant de commencer la transaction
            if (!$this->product_id || !$this->client_id || !$this->pharmacy_id) {
                throw new \InvalidArgumentException("Données de vente incomplètes: product_id, client_id et pharmacy_id sont requis");
            }
            
            if ($this->quantite <= 0) {
                throw new \InvalidArgumentException("La quantité doit être supérieure à zéro");
            }
            
            // Démarrer une transaction pour assurer l'intégrité des données
            $pdo->beginTransaction();
            
            // 1. Vérifier si le produit existe et a suffisamment de stock avec un verrouillage pour éviter les conditions de concurrence
            $stmt = $pdo->prepare("SELECT quantite_stock, prix, est_ordonnance FROM produits WHERE id = :product_id FOR UPDATE");
            $stmt->execute([':product_id' => $this->product_id]);
            $product = $stmt->fetch(\PDO::FETCH_ASSOC);
            
            if (!$product) {
                throw new \Exception("Le produit avec l'ID {$this->product_id} n'existe pas");
            }
            
            // Vérifier si le produit nécessite une ordonnance
            if ($product['est_ordonnance']) {
                throw new \Exception("Ce produit nécessite une ordonnance et ne peut pas être vendu directement");
            }
            
            // Vérifier le stock disponible
            if ($product['quantite_stock'] <= 0) {
                throw new \Exception("Le produit est en rupture de stock");
            }
            
            if ($product['quantite_stock'] < $this->quantite) {
                throw new \Exception("Stock insuffisant. Quantité disponible: {$product['quantite_stock']} unité(s)");
            }
            
            // 2. Mettre à jour le prix unitaire si non défini ou vérifier sa validité
            if ($this->prix_unitaire === null) {
                if ($product['prix'] <= 0) {
                    throw new \Exception("Le prix du produit est invalide: {$product['prix']}");
                }
                $this->prix_unitaire = $product['prix'];
                $this->prix_total = $this->prix_unitaire * $this->quantite;
            } else if ($this->prix_unitaire <= 0) {
                throw new \InvalidArgumentException("Le prix unitaire doit être supérieur à zéro");
            }
            
            // Vérifier si le client existe
            $stmt = $pdo->prepare("SELECT id, pharmacie_id FROM clients WHERE id = :client_id");
            $stmt->execute([':client_id' => $this->client_id]);
            $client = $stmt->fetch(\PDO::FETCH_ASSOC);
            
            if (!$client) {
                throw new \Exception("Le client avec l'ID {$this->client_id} n'existe pas");
            }
            
            // Vérifier si le client appartient à la pharmacie
            if ($client['pharmacie_id'] && $client['pharmacie_id'] != $this->pharmacy_id) {
                throw new \Exception("Le client n'appartient pas à cette pharmacie");
            }
            
            // Vérifier si la pharmacie existe
            $stmt = $pdo->prepare("SELECT id FROM pharmacies WHERE id = :pharmacy_id");
            $stmt->execute([':pharmacy_id' => $this->pharmacy_id]);
            if (!$stmt->fetch()) {
                throw new \Exception("La pharmacie avec l'ID {$this->pharmacy_id} n'existe pas");
            }
            
            // 3. Insérer la vente
            $stmt = $pdo->prepare("
                INSERT INTO ventes (product_id, client_id, pharmacy_id, quantite, prix_unitaire, prix_total, date_vente) 
                VALUES (:product_id, :client_id, :pharmacy_id, :quantite, :prix_unitaire, :prix_total, :date_vente)
            ");
            
            $insertResult = $stmt->execute([
                ':product_id' => $this->product_id,
                ':client_id' => $this->client_id,
                ':pharmacy_id' => $this->pharmacy_id,
                ':quantite' => $this->quantite,
                ':prix_unitaire' => $this->prix_unitaire,
                ':prix_total' => $this->prix_total,
                ':date_vente' => $this->date_vente
            ]);
            
            if (!$insertResult) {
                throw new \Exception("Échec de l'insertion de la vente dans la base de données");
            }
            
            $this->id = $pdo->lastInsertId();
            
            if (!$this->id) {
                throw new \Exception("Échec de récupération de l'ID de la vente");
            }
            
            // 4. Mettre à jour le stock du produit
            $newStock = $product['quantite_stock'] - $this->quantite;
            
            // S'assurer que le stock ne devient pas négatif (double vérification)
            if ($newStock < 0) {
                throw new \Exception("Erreur de calcul du stock: le stock deviendrait négatif");
            }
            
            $updateStmt = $pdo->prepare("UPDATE produits SET quantite_stock = :new_stock, date_modification = NOW() WHERE id = :product_id");
            $updateResult = $updateStmt->execute([
                ':new_stock' => $newStock,
                ':product_id' => $this->product_id
            ]);
            
            if (!$updateResult) {
                throw new \Exception("Échec de la mise à jour du stock du produit");
            }
            
            // Vérifier que la mise à jour a bien affecté une ligne
            if ($updateStmt->rowCount() !== 1) {
                throw new \Exception("La mise à jour du stock n'a affecté aucune ligne");
            }
            
            // Valider la transaction
            $pdo->commit();
            
            // Journaliser la vente réussie
            error_log("Vente réussie - ID: {$this->id}, Produit: {$this->product_id}, Client: {$this->client_id}, Quantité: {$this->quantite}, Total: {$this->prix_total}");
            
            return true;
        } catch (\PDOException $e) {
            // Annuler la transaction en cas d'erreur de base de données
            if ($pdo->inTransaction()) {
                $pdo->rollBack();
            }
            error_log("Erreur PDO lors de la vente - " . $e->getMessage());
            throw new \Exception("Erreur de base de données lors de la vente: " . $e->getMessage(), 0, $e);
        } catch (\Exception $e) {
            // Annuler la transaction en cas d'autres erreurs
            if ($pdo->inTransaction()) {
                $pdo->rollBack();
            }
            error_log("Exception lors de la vente - " . $e->getMessage());
            throw $e;
        } catch (\Throwable $t) {
            // Annuler la transaction en cas d'erreur fatale
            if ($pdo->inTransaction()) {
                $pdo->rollBack();
            }
            error_log("Erreur fatale lors de la vente - " . $t->getMessage());
            throw new \Exception("Erreur système lors de la vente", 0, $t);
        }
    }

    // Méthode statique pour obtenir toutes les ventes d'une pharmacie
    public static function getAllByPharmacy($pharmacy_id, $limit = 100, $offset = 0) {
        if (!$pharmacy_id || !is_numeric($pharmacy_id) || $pharmacy_id <= 0) {
            throw new \InvalidArgumentException("ID de pharmacie invalide");
        }
        
        // Valider et nettoyer les paramètres de pagination
        $limit = intval($limit);
        $offset = intval($offset);
        
        if ($limit <= 0 || $limit > 1000) { // Limite maximale pour éviter les requêtes trop lourdes
            $limit = 100; // Valeur par défaut
        }
        
        if ($offset < 0) {
            $offset = 0;
        }
        
        try {
            $pdo = self::initPDO();
            $stmt = $pdo->prepare("
                SELECT v.*, p.nom as product_name, c.nom as client_nom, c.prenom as client_prenom 
                FROM ventes v
                JOIN produits p ON v.product_id = p.id
                JOIN clients c ON v.client_id = c.id
                WHERE v.pharmacy_id = :pharmacy_id
                ORDER BY v.date_vente DESC
                LIMIT :limit OFFSET :offset
            ");
            
            $stmt->bindValue(':pharmacy_id', $pharmacy_id, \PDO::PARAM_INT);
            $stmt->bindValue(':limit', $limit, \PDO::PARAM_INT);
            $stmt->bindValue(':offset', $offset, \PDO::PARAM_INT);
            $stmt->execute();
            
            $sales = [];
            while ($row = $stmt->fetch(\PDO::FETCH_ASSOC)) {
                $sale = new self(
                    $row['id'],
                    $row['product_id'],
                    $row['client_id'],
                    $row['pharmacy_id'],
                    $row['quantite'],
                    $row['prix_unitaire'],
                    $row['prix_total'],
                    $row['date_vente']
                );
                
                // Ajouter des informations supplémentaires
                $sale->product_name = $row['product_name'];
                $sale->client_name = $row['client_prenom'] . ' ' . $row['client_nom'];
                
                $sales[] = $sale;
            }
            
            return $sales;
        } catch (\PDOException $e) {
            error_log("Erreur lors de la récupération des ventes: " . $e->getMessage());
            throw new \Exception("Erreur lors de la récupération des ventes", 0, $e);
        }
    }

    // Méthode statique pour obtenir les ventes d'un client
    public static function getByClient($client_id, $limit = 50, $offset = 0) {
        if (!$client_id || !is_numeric($client_id) || $client_id <= 0) {
            throw new \InvalidArgumentException("ID de client invalide");
        }
        
        // Valider et nettoyer les paramètres de pagination
        $limit = intval($limit);
        $offset = intval($offset);
        
        if ($limit <= 0 || $limit > 500) { // Limite maximale pour éviter les requêtes trop lourdes
            $limit = 50; // Valeur par défaut
        }
        
        if ($offset < 0) {
            $offset = 0;
        }
        
        try {
            $pdo = self::initPDO();
            
            // Vérifier d'abord si le client existe
            $checkStmt = $pdo->prepare("SELECT id FROM clients WHERE id = :client_id");
            $checkStmt->execute([':client_id' => $client_id]);
            
            if (!$checkStmt->fetch()) {
                throw new \Exception("Le client avec l'ID {$client_id} n'existe pas");
            }
            
            $stmt = $pdo->prepare("
                SELECT v.*, p.nom as product_name 
                FROM ventes v
                JOIN produits p ON v.product_id = p.id
                WHERE v.client_id = :client_id
                ORDER BY v.date_vente DESC
                LIMIT :limit OFFSET :offset
            ");
            
            $stmt->bindValue(':client_id', $client_id, \PDO::PARAM_INT);
            $stmt->bindValue(':limit', $limit, \PDO::PARAM_INT);
            $stmt->bindValue(':offset', $offset, \PDO::PARAM_INT);
            $stmt->execute();
            
            $sales = [];
            while ($row = $stmt->fetch(\PDO::FETCH_ASSOC)) {
                $sale = new self(
                    $row['id'],
                    $row['product_id'],
                    $row['client_id'],
                    $row['pharmacy_id'],
                    $row['quantite'],
                    $row['prix_unitaire'],
                    $row['prix_total'],
                    $row['date_vente']
                );
                
                // Ajouter des informations supplémentaires
                $sale->product_name = $row['product_name'];
                
                $sales[] = $sale;
            }
            
            return $sales;
        } catch (\PDOException $e) {
            error_log("Erreur lors de la récupération des ventes du client: " . $e->getMessage());
            throw new \Exception("Erreur lors de la récupération des ventes du client", 0, $e);
        }
    }

    // Méthode statique pour créer une table ventes si elle n'existe pas
    public static function createTableIfNotExists() {
        $pdo = self::initPDO();
        
        try {
            // Vérifier si la table existe déjà
            $checkTableQuery = "SHOW TABLES LIKE 'ventes'";
            $stmt = $pdo->prepare($checkTableQuery);
            $stmt->execute();
            
            if ($stmt->rowCount() == 0) {
                // La table n'existe pas, on la crée
                $query = "
                    CREATE TABLE ventes (
                        id INT AUTO_INCREMENT PRIMARY KEY,
                        product_id INT NOT NULL,
                        client_id INT NOT NULL,
                        pharmacy_id INT NOT NULL,
                        quantite INT NOT NULL DEFAULT 1,
                        prix_unitaire DECIMAL(10,2) NOT NULL,
                        prix_total DECIMAL(10,2) NOT NULL,
                        date_vente DATETIME NOT NULL,
                        FOREIGN KEY (product_id) REFERENCES produits(id) ON DELETE RESTRICT,
                        FOREIGN KEY (client_id) REFERENCES clients(id) ON DELETE RESTRICT,
                        FOREIGN KEY (pharmacy_id) REFERENCES pharmacies(id) ON DELETE RESTRICT,
                        INDEX idx_product (product_id),
                        INDEX idx_client (client_id),
                        INDEX idx_pharmacy (pharmacy_id),
                        INDEX idx_date (date_vente)
                    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
                ";
                
                $pdo->exec($query);
                error_log("Table 'ventes' créée avec succès");
                return true;
            }
            
            return false;
        } catch (\PDOException $e) {
            error_log("Erreur lors de la création de la table 'ventes': " . $e->getMessage());
            throw new \Exception("Erreur lors de la création de la table des ventes", 0, $e);
        }
    }
    
    // Méthode pour vérifier si une vente existe
    public static function exists($id) {
        if (!$id || !is_numeric($id) || $id <= 0) {
            return false;
        }
        
        try {
            $pdo = self::initPDO();
            $stmt = $pdo->prepare("SELECT id FROM ventes WHERE id = :id");
            $stmt->execute([':id' => $id]);
            return $stmt->fetch() !== false;
        } catch (\Exception $e) {
            error_log("Erreur lors de la vérification de l'existence de la vente: " . $e->getMessage());
            return false;
        }
    }
    
    // Méthode pour annuler une vente (si nécessaire dans le futur)
    public static function cancel($id) {
        if (!$id || !is_numeric($id) || $id <= 0) {
            throw new \InvalidArgumentException("ID de vente invalide");
        }
        
        $pdo = self::initPDO();
        
        try {
            // Démarrer une transaction
            $pdo->beginTransaction();
            
            // Récupérer les informations de la vente
            $stmt = $pdo->prepare("
                SELECT product_id, quantite 
                FROM ventes 
                WHERE id = :id
            ");
            $stmt->execute([':id' => $id]);
            $vente = $stmt->fetch(\PDO::FETCH_ASSOC);
            
            if (!$vente) {
                throw new \Exception("Vente non trouvée");
            }
            
            // Mettre à jour le stock du produit (remettre la quantité en stock)
            $updateStmt = $pdo->prepare("
                UPDATE produits 
                SET quantite_stock = quantite_stock + :quantite,
                    date_modification = NOW() 
                WHERE id = :product_id
            ");
            $updateResult = $updateStmt->execute([
                ':quantite' => $vente['quantite'],
                ':product_id' => $vente['product_id']
            ]);
            
            if (!$updateResult || $updateStmt->rowCount() !== 1) {
                throw new \Exception("Échec de la mise à jour du stock");
            }
            
            // Marquer la vente comme annulée (ou la supprimer)
            // Option 1: Supprimer la vente
            $deleteStmt = $pdo->prepare("DELETE FROM ventes WHERE id = :id");
            $deleteResult = $deleteStmt->execute([':id' => $id]);
            
            if (!$deleteResult || $deleteStmt->rowCount() !== 1) {
                throw new \Exception("Échec de la suppression de la vente");
            }
            
            // Valider la transaction
            $pdo->commit();
            
            error_log("Vente ID: {$id} annulée avec succès");
            return true;
            
        } catch (\PDOException $e) {
            if ($pdo->inTransaction()) {
                $pdo->rollBack();
            }
            error_log("Erreur PDO lors de l'annulation de la vente: " . $e->getMessage());
            throw new \Exception("Erreur de base de données lors de l'annulation de la vente", 0, $e);
        } catch (\Exception $e) {
            if ($pdo->inTransaction()) {
                $pdo->rollBack();
            }
            error_log("Exception lors de l'annulation de la vente: " . $e->getMessage());
            throw $e;
        }
    }
}
