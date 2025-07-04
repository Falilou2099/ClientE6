<?php
namespace Models;

class Product {
    private $id;
    private $nom;
    private $categorie;
    private $description;
    private $prix;
    private $quantite_stock;
    private $image;
    private $est_ordonnance;
    private $date_ajout;
    private $date_modification;
    private $pharmacy_id;
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

    // Constructeur simplifié
    public function __construct(
        $id = null,
        $nom = null,
        $description = null,
        $prix = null,
        $quantite_stock = null,
        $categorie = null,
        $image = null,
        $est_ordonnance = false,
        $date_ajout = null,
        $date_modification = null,
        $pharmacy_id = null
    ) {
        $this->id = $id;
        $this->nom = $nom;
        $this->description = $description;
        $this->prix = $prix;
        $this->quantite_stock = $quantite_stock;
        $this->categorie = $categorie;
        $this->image = $image;
        $this->est_ordonnance = $est_ordonnance;
        $this->date_ajout = $date_ajout;
        $this->date_modification = $date_modification;
        $this->pharmacy_id = $pharmacy_id;
    }

    // Getters
    public function getId() { return $this->id; }
    public function getNom() { return $this->nom; }
    public function getCategorie() { return $this->categorie; }
    public function getDescription() { return $this->description; }
    public function getPrix() { return $this->prix; }
    public function getStock() { return $this->quantite_stock; }
    public function getImage() { return $this->image; }
    public function getEstOrdonnance() { return $this->est_ordonnance; }
    public function getDateAjout() { return $this->date_ajout; }
    public function getDateModification() { return $this->date_modification; }
    public function getPharmacyId() { return $this->pharmacy_id; }
    
    // Méthode pour obtenir l'URL de l'image
    public function getImageUrl() {
        if (!$this->image) {
            // Utiliser l'image par défaut existante dans le dossier images/products
            return '/bigpharma/public/images/products/imgDefault.jpg';
        }
        
        // Vérifier si l'image est une URL complète (commence par http:// ou https://)
        if (preg_match('/^https?:\/\//i', $this->image)) {
            return $this->image;
        }
        
        // Sinon, c'est un fichier local
        return '/bigpharma/public/images/products/' . $this->image;
    }

    // Setters
    public function setNom($nom) { $this->nom = $nom; }
    public function setDescription($description) { $this->description = $description; }
    public function setPrix($prix) { $this->prix = $prix; }
    public function setStock($quantite_stock) { $this->quantite_stock = $quantite_stock; return $this; }
    public function setCategorie($categorie) { $this->categorie = $categorie; }
    public function setImage($image) { $this->image = $image; return $this; }
    public function setEstOrdonnance($est_ordonnance) { $this->est_ordonnance = $est_ordonnance; return $this; }
    public function setDateAjout($date_ajout) { $this->date_ajout = $date_ajout; return $this; }
    public function setDateModification($date_modification) { $this->date_modification = $date_modification; return $this; }
    public function setPharmacyId($pharmacy_id) { $this->pharmacy_id = $pharmacy_id; return $this; }

    // Méthode pour sauvegarder un produit
    public function save() {
        $pdo = self::initPDO();
        
        // Préparer les données
        $data = [
            'nom' => $this->nom,
            'description' => $this->description,
            'prix' => $this->prix,
            'quantite_stock' => $this->quantite_stock,
            'categorie' => $this->categorie,
            'est_ordonnance' => $this->est_ordonnance ? 1 : 0,
            'pharmacy_id' => $this->pharmacy_id
        ];
        
        // Ajouter l'image si elle existe
        if ($this->image) {
            $data['image'] = $this->image;
        }
        
        if ($this->id) {
            // Mise à jour d'un produit existant
            $sql = "UPDATE produits SET ";
            $updates = [];
            
            foreach ($data as $key => $value) {
                $updates[] = "$key = :$key";
            }
            
            $sql .= implode(", ", $updates);
            $sql .= " WHERE id = :id";
            $data['id'] = $this->id;
            
            $stmt = $pdo->prepare($sql);
            $stmt->execute($data);
            
            return $this->id;
        } else {
            // Insertion d'un nouveau produit
            $columns = array_keys($data);
            $placeholders = array_map(function($col) { return ":$col"; }, $columns);
            
            $sql = "INSERT INTO produits (" . implode(", ", $columns) . ") ";
            $sql .= "VALUES (" . implode(", ", $placeholders) . ")";
            
            $stmt = $pdo->prepare($sql);
            $stmt->execute($data);
            
            $this->id = $pdo->lastInsertId();
            return $this->id;
        }
    }

    // Méthode pour supprimer un produit
    public function delete() {
        if ($this->id === null) {
            return false;
        }
        
        $pdo = self::initPDO();
        $stmt = $pdo->prepare("DELETE FROM produits WHERE id = :id");
        return $stmt->execute([':id' => $this->id]);
    }

    // Méthode pour mettre à jour le stock
    public function updateStock($quantity) {
        $this->quantite_stock += $quantity;
        
        $pdo = self::initPDO();
        $stmt = $pdo->prepare("UPDATE produits SET quantite_stock = :quantite_stock WHERE id = :id");
        return $stmt->execute([':quantite_stock' => $this->quantite_stock, ':id' => $this->id]);
    }

    // Méthode statique pour trouver un produit par ID
    public static function findById($id) {
        $pdo = self::initPDO();
        $stmt = $pdo->prepare("SELECT * FROM produits WHERE id = :id");
        $stmt->execute([':id' => $id]);
        
        $productData = $stmt->fetch(\PDO::FETCH_ASSOC);
        if (!$productData) {
            return null;
        }
        
        return self::createFromArray($productData);
    }

    // Méthode statique pour trouver des produits par nom
    public static function findByName($name) {
        $pdo = self::initPDO();
        $stmt = $pdo->prepare("SELECT * FROM produits WHERE nom LIKE :name");
        $stmt->execute([':name' => "%{$name}%"]);
        
        $products = [];
        while ($row = $stmt->fetch(\PDO::FETCH_ASSOC)) {
            $products[] = self::createFromArray($row);
        }
        
        return $products;
    }

    // Méthode statique pour obtenir tous les produits
    public static function getAll($pharmacy_id = null) {
        $pdo = self::initPDO();
        
        if ($pharmacy_id) {
            $stmt = $pdo->prepare("SELECT * FROM produits WHERE pharmacy_id = :pharmacy_id ORDER BY nom ASC");
            $stmt->execute([':pharmacy_id' => $pharmacy_id]);
        } else {
            $stmt = $pdo->query("SELECT * FROM produits ORDER BY nom ASC");
        }
        
        $products = [];
        while ($row = $stmt->fetch(\PDO::FETCH_ASSOC)) {
            $products[] = self::createFromArray($row);
        }
        
        return $products;
    }
    
    // Méthode pour vérifier si une pharmacie a des produits
    public static function hasProducts($pharmacy_id) {
        $pdo = self::initPDO();
        $stmt = $pdo->prepare("SELECT COUNT(*) FROM produits WHERE pharmacy_id = :pharmacy_id");
        $stmt->execute([':pharmacy_id' => $pharmacy_id]);
        return $stmt->fetchColumn() > 0;
    }

    // Méthode statique pour obtenir un produit par son ID
    // Le paramètre pharmacy_id est optionnel et permet de vérifier que le produit appartient à la pharmacie
    public static function getById($id, $pharmacy_id = null) {
        $pdo = self::initPDO();
        
        if ($pharmacy_id) {
            $stmt = $pdo->prepare("SELECT * FROM produits WHERE id = :id AND pharmacy_id = :pharmacy_id");
            $stmt->execute([
                ':id' => $id,
                ':pharmacy_id' => $pharmacy_id
            ]);
        } else {
            $stmt = $pdo->prepare("SELECT * FROM produits WHERE id = :id");
            $stmt->execute([':id' => $id]);
        }
        
        $row = $stmt->fetch(\PDO::FETCH_ASSOC);
        if (!$row) {
            return null;
        }
        
        return self::createFromArray($row);
    }
    
    // Méthode statique pour obtenir des produits par catégorie
    public static function getByCategory($category, $limit = null, $pharmacy_id = null) {
        $pdo = self::initPDO();
        $params = [':category' => $category];
        
        $query = "SELECT * FROM produits WHERE categorie = :category";
        
        if ($pharmacy_id) {
            $query .= " AND pharmacy_id = :pharmacy_id";
            $params[':pharmacy_id'] = $pharmacy_id;
        }
        
        $query .= " ORDER BY nom ASC";
        
        if ($limit && is_numeric($limit)) {
            $query .= " LIMIT " . intval($limit);
        }
        
        $stmt = $pdo->prepare($query);
        $stmt->execute($params);
        
        $products = [];
        while ($row = $stmt->fetch(\PDO::FETCH_ASSOC)) {
            $products[] = self::createFromArray($row);
        }
        
        return $products;
    }

    // Méthode statique pour rechercher des produits selon des critères
    public static function search($criteria = []) {
        $pdo = self::initPDO();
        
        // Requête de base
        $query = "SELECT * FROM produits WHERE 1=1";
        $params = [];
        
        // Filtrer par pharmacie
        if (isset($criteria['pharmacy_id']) && $criteria['pharmacy_id']) {
            $query .= " AND pharmacy_id = :pharmacy_id";
            $params[':pharmacy_id'] = $criteria['pharmacy_id'];
        }
        
        // Filtrer par nom
        if (!empty($criteria['name'])) {
            $query .= " AND nom LIKE :name";
            $params[':name'] = '%' . $criteria['name'] . '%';
        }
        
        // Filtrer par catégorie
        if (!empty($criteria['category'])) {
            $query .= " AND categorie = :category";
            $params[':category'] = $criteria['category'];
        }
        
        // Filtrer par prix minimum
        if (isset($criteria['min_price']) && is_numeric($criteria['min_price'])) {
            $query .= " AND prix >= :min_price";
            $params[':min_price'] = $criteria['min_price'];
        }
        
        // Filtrer par prix maximum
        if (isset($criteria['max_price']) && is_numeric($criteria['max_price'])) {
            $query .= " AND prix <= :max_price";
            $params[':max_price'] = $criteria['max_price'];
        }
        
        // Filtrer par disponibilité en stock
        if (isset($criteria['in_stock']) && $criteria['in_stock']) {
            $query .= " AND quantite_stock > 0";
        }
        
        // Filtrer par prescription
        if (isset($criteria['prescription'])) {
            $query .= " AND est_ordonnance = :prescription";
            $params[':prescription'] = $criteria['prescription'] ? 1 : 0;
        }
        
        // Tri
        if (!empty($criteria['sort'])) {
            switch ($criteria['sort']) {
                case 'price':
                    $query .= " ORDER BY prix ASC";
                    break;
                case 'stock':
                    $query .= " ORDER BY quantite_stock ASC";
                    break;
                default:
                    $query .= " ORDER BY nom ASC";
            }
        } else {
            $query .= " ORDER BY nom ASC";
        }
        
        // Exécuter la requête
        $stmt = $pdo->prepare($query);
        $stmt->execute($params);
        
        // Convertir les résultats en objets Product
        $products = [];
        while ($row = $stmt->fetch(\PDO::FETCH_ASSOC)) {
            $products[] = self::createFromArray($row);
        }
        
        return $products;
    }

    // Méthode statique pour obtenir le nombre total de produits
    public static function getTotalProducts($pharmacy_id = null) {
        $pdo = self::initPDO();
        
        if ($pharmacy_id) {
            $stmt = $pdo->prepare("SELECT COUNT(*) FROM produits WHERE pharmacy_id = :pharmacy_id");
            $stmt->execute([':pharmacy_id' => $pharmacy_id]);
        } else {
            $stmt = $pdo->query("SELECT COUNT(*) FROM produits");
        }
        
        return $stmt->fetchColumn();
    }

    // Méthode statique pour obtenir les produits à faible stock
    public static function getLowStockProducts($threshold = 10, $pharmacy_id = null) {
        $pdo = self::initPDO();
        
        if ($pharmacy_id) {
            $stmt = $pdo->prepare("SELECT * FROM produits WHERE quantite_stock < :threshold AND pharmacy_id = :pharmacy_id ORDER BY quantite_stock ASC");
            $stmt->execute([
                ':threshold' => $threshold,
                ':pharmacy_id' => $pharmacy_id
            ]);
        } else {
            $stmt = $pdo->prepare("SELECT * FROM produits WHERE quantite_stock < :threshold ORDER BY quantite_stock ASC");
            $stmt->execute([':threshold' => $threshold]);
        }
        
        $products = [];
        while ($row = $stmt->fetch(\PDO::FETCH_ASSOC)) {
            $products[] = self::createFromArray($row);
        }
        
        return $products;
    }

    // Méthode statique pour obtenir les produits par catégorie
    public static function getByCategoryOld($category) {
        $pdo = self::initPDO();
        $stmt = $pdo->prepare("SELECT * FROM produits WHERE categorie = :category ORDER BY nom ASC");
        $stmt->execute([':category' => $category]);
        
        $products = [];
        while ($row = $stmt->fetch(\PDO::FETCH_ASSOC)) {
            $products[] = self::createFromArray($row);
        }
        
        return $products;
    }

    // Méthode statique pour obtenir toutes les catégories
    public static function getAllCategories($pharmacy_id = null) {
        $pdo = self::initPDO();
        
        if ($pharmacy_id) {
            $stmt = $pdo->prepare("SELECT DISTINCT categorie FROM produits WHERE pharmacy_id = :pharmacy_id ORDER BY categorie ASC");
            $stmt->execute([':pharmacy_id' => $pharmacy_id]);
        } else {
            $stmt = $pdo->query("SELECT DISTINCT categorie FROM produits ORDER BY categorie ASC");
        }
        
        return $stmt->fetchAll(\PDO::FETCH_COLUMN);
    }

    // Méthode statique pour obtenir les produits sur ordonnance
    public static function getPrescriptionProducts($pharmacy_id = null) {
        $pdo = self::initPDO();
        
        if ($pharmacy_id) {
            $stmt = $pdo->prepare("SELECT * FROM produits WHERE est_ordonnance = 1 AND pharmacy_id = :pharmacy_id");
            $stmt->execute([':pharmacy_id' => $pharmacy_id]);
        } else {
            $stmt = $pdo->prepare("SELECT * FROM produits WHERE est_ordonnance = 1");
            $stmt->execute();
        }
        
        $products = [];
        while ($row = $stmt->fetch(\PDO::FETCH_ASSOC)) {
            $products[] = self::createFromArray($row);
        }
        
        return $products;
    }

    // Méthode pour créer un objet Product à partir d'un tableau
    private static function createFromArray($data) {
        return new self(
            $data['id'] ?? null,
            $data['nom'] ?? null,
            $data['description'] ?? null,
            $data['prix'] ?? null,
            $data['quantite_stock'] ?? null,
            $data['categorie'] ?? null,
            $data['image'] ?? null,
            $data['est_ordonnance'] ?? false,
            $data['date_ajout'] ?? null,
            $data['date_modification'] ?? null,
            $data['pharmacy_id'] ?? null
        );
    }
}
