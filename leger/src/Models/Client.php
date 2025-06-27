<?php
namespace Models;

class Client {
    private $id;
    private $nom;
    private $prenom;
    private $email;
    private $telephone;
    private $adresse;
    private $pharmacie_id;
    private $date_creation;
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
        $nom = null,
        $prenom = null,
        $email = null,
        $telephone = null,
        $adresse = null,
        $pharmacie_id = null,
        $date_creation = null
    ) {
        $this->id = $id;
        $this->nom = $nom;
        $this->prenom = $prenom;
        $this->email = $email;
        $this->telephone = $telephone;
        $this->adresse = $adresse;
        $this->pharmacie_id = $pharmacie_id;
        $this->date_creation = $date_creation ?? date('Y-m-d H:i:s');
    }

    // Getters
    public function getId() { return $this->id; }
    public function getNom() { return $this->nom; }
    public function getPrenom() { return $this->prenom; }
    public function getEmail() { return $this->email; }
    public function getTelephone() { return $this->telephone; }
    public function getAdresse() { return $this->adresse; }
    public function getPharmacieId() { return $this->pharmacie_id; }
    public function getDateCreation() { return $this->date_creation; }
    
    // Méthode pour obtenir le nom complet
    public function getNomComplet() {
        return $this->prenom . ' ' . $this->nom;
    }

    // Setters
    public function setNom($nom) { $this->nom = $nom; return $this; }
    public function setPrenom($prenom) { $this->prenom = $prenom; return $this; }
    public function setEmail($email) { $this->email = $email; return $this; }
    public function setTelephone($telephone) { $this->telephone = $telephone; return $this; }
    public function setAdresse($adresse) { $this->adresse = $adresse; return $this; }
    public function setPharmacieId($pharmacie_id) { $this->pharmacie_id = $pharmacie_id; return $this; }

    // Méthode pour sauvegarder un client
    public function save() {
        $pdo = self::initPDO();
        
        try {
            if ($this->id === null) {
                // Nouveau client
                $stmt = $pdo->prepare("
                    INSERT INTO clients (nom, prenom, email, telephone, adresse, pharmacie_id, date_creation) 
                    VALUES (:nom, :prenom, :email, :telephone, :adresse, :pharmacie_id, :date_creation)
                ");
                
                $params = [
                    ':nom' => $this->nom,
                    ':prenom' => $this->prenom,
                    ':email' => $this->email,
                    ':telephone' => $this->telephone,
                    ':adresse' => $this->adresse,
                    ':pharmacie_id' => $this->pharmacie_id,
                    ':date_creation' => $this->date_creation
                ];
                
                $result = $stmt->execute($params);
                
                if ($result) {
                    $this->id = $pdo->lastInsertId();
                }
            } else {
                // Mise à jour d'un client existant
                $stmt = $pdo->prepare("
                    UPDATE clients 
                    SET nom = :nom, 
                        prenom = :prenom, 
                        email = :email, 
                        telephone = :telephone, 
                        adresse = :adresse, 
                        pharmacie_id = :pharmacie_id 
                    WHERE id = :id
                ");
                
                $params = [
                    ':nom' => $this->nom,
                    ':prenom' => $this->prenom,
                    ':email' => $this->email,
                    ':telephone' => $this->telephone,
                    ':adresse' => $this->adresse,
                    ':pharmacie_id' => $this->pharmacie_id,
                    ':id' => $this->id
                ];
                
                $result = $stmt->execute($params);
            }
            
            return $result;
        } catch (\PDOException $e) {
            error_log("Erreur lors de la sauvegarde du client: " . $e->getMessage());
            return false;
        }
    }

    // Méthode pour supprimer un client
    public function delete() {
        if ($this->id === null) {
            return false;
        }
        
        $pdo = self::initPDO();
        $stmt = $pdo->prepare("DELETE FROM clients WHERE id = :id");
        return $stmt->execute([':id' => $this->id]);
    }

    // Méthode statique pour trouver un client par ID
    public static function findById($id) {
        $pdo = self::initPDO();
        $stmt = $pdo->prepare("SELECT * FROM clients WHERE id = :id");
        $stmt->execute([':id' => $id]);
        
        $clientData = $stmt->fetch(\PDO::FETCH_ASSOC);
        if (!$clientData) {
            return null;
        }
        
        return self::createFromArray($clientData);
    }

    // Méthode statique pour trouver des clients par nom ou prénom
    public static function findByName($name, $pharmacie_id = null) {
        $pdo = self::initPDO();
        
        if ($pharmacie_id !== null) {
            $stmt = $pdo->prepare("
                SELECT * FROM clients 
                WHERE (nom LIKE :name OR prenom LIKE :name) AND pharmacie_id = :pharmacie_id 
                ORDER BY nom, prenom
            ");
            $stmt->execute([
                ':name' => "%$name%",
                ':pharmacie_id' => $pharmacie_id
            ]);
        } else {
            $stmt = $pdo->prepare("
                SELECT * FROM clients 
                WHERE nom LIKE :name OR prenom LIKE :name 
                ORDER BY nom, prenom
            ");
            $stmt->execute([':name' => "%$name%"]);
        }
        
        $clients = [];
        while ($row = $stmt->fetch(\PDO::FETCH_ASSOC)) {
            $clients[] = self::createFromArray($row);
        }
        
        return $clients;
    }

    // Méthode statique pour obtenir tous les clients
    public static function getAll($pharmacie_id = null) {
        $pdo = self::initPDO();
        
        if ($pharmacie_id !== null) {
            $stmt = $pdo->prepare("SELECT * FROM clients WHERE pharmacie_id = :pharmacie_id ORDER BY nom, prenom");
            $stmt->execute([':pharmacie_id' => $pharmacie_id]);
        } else {
            $stmt = $pdo->query("SELECT * FROM clients ORDER BY nom, prenom");
        }
        
        $clients = [];
        while ($row = $stmt->fetch(\PDO::FETCH_ASSOC)) {
            $clients[] = self::createFromArray($row);
        }
        
        return $clients;
    }

    // Méthode pour créer un objet Client à partir d'un tableau
    private static function createFromArray($data) {
        return new self(
            $data['id'] ?? null,
            $data['nom'] ?? null,
            $data['prenom'] ?? null,
            $data['email'] ?? null,
            $data['telephone'] ?? null,
            $data['adresse'] ?? null,
            $data['pharmacie_id'] ?? null,
            $data['date_creation'] ?? null
        );
    }
}
