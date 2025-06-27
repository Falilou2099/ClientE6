<?php
namespace Models;

use Config\SecurityService;

class Pharmacy {
    private $id;
    private $nom;
    private $adresse;
    private $phoneNumber;
    private $email;
    private $registrationNumber;
    private $statut;
    private $createdAt;
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
    public function __construct($id = null, $nom = null, $adresse = null, $phoneNumber = null, $email = null, $registrationNumber = null, $statut = 'pending', $createdAt = null) {
        $this->id = $id;
        $this->nom = $nom ? SecurityService::sanitizeInput($nom) : null;
        $this->adresse = $adresse ? SecurityService::sanitizeInput($adresse) : null;
        $this->phoneNumber = $phoneNumber ? $this->formatPhoneNumber($phoneNumber) : null;
        $this->email = $email ? SecurityService::sanitizeInput($email) : null;
        $this->registrationNumber = $registrationNumber ? SecurityService::preventSQLInjection($registrationNumber) : null;
        $this->statut = $statut; // Par défaut, en attente de validation
        $this->createdAt = $createdAt ?? date('Y-m-d H:i:s');
    }

    // Validation des données de la pharmacie
    public function validate() {
        $errors = [];

        // Validation du nom
        if (empty($this->nom) || strlen($this->nom) < 2) {
            $errors[] = "Le nom de la pharmacie est invalide";
        }

        // Validation de l'adresse
        if (empty($this->adresse) || strlen($this->adresse) < 5) {
            $errors[] = "L'adresse est invalide";
        }

        // Validation du numéro de téléphone
        if (!$this->validatePhoneNumber($this->phoneNumber)) {
            $errors[] = "Le numéro de téléphone est invalide";
        }

        // Validation de l'email
        if (!SecurityService::validateEmail($this->email)) {
            $errors[] = "L'email est invalide";
        }

        // Validation du numéro d'enregistrement
        if (empty($this->registrationNumber) || !preg_match('/^[A-Z0-9-]+$/', $this->registrationNumber)) {
            $errors[] = "Le numéro d'enregistrement est invalide";
        }

        return $errors;
    }

    // Formater le numéro de téléphone
    private function formatPhoneNumber($phoneNumber) {
        // Supprimer tous les caractères non numériques
        $cleaned = preg_replace('/[^0-9]/', '', $phoneNumber);
        
        // Vérifier la longueur
        if (strlen($cleaned) === 10) {
            return preg_replace('/(\d{2})(\d{2})(\d{2})(\d{2})(\d{2})/', '$1 $2 $3 $4 $5', $cleaned);
        }
        
        return null;
    }

    // Valider le numéro de téléphone
    private function validatePhoneNumber($phoneNumber) {
        return preg_match('/^(0|\+33)[1-9]([-. ]?[0-9]{2}){4}$/', $phoneNumber);
    }

    // Mettre à jour les informations de la pharmacie
    public function updateProfile($data) {
        foreach ($data as $key => $value) {
            switch ($key) {
                case 'nom':
                    $this->nom = SecurityService::sanitizeInput($value);
                    break;
                case 'adresse':
                    $this->adresse = SecurityService::sanitizeInput($value);
                    break;
                case 'phoneNumber':
                    $this->phoneNumber = $this->formatPhoneNumber($value);
                    break;
                case 'email':
                    if (SecurityService::validateEmail($value)) {
                        $this->email = SecurityService::sanitizeInput($value);
                    }
                    break;
                case 'statut':
                    $allowedStatuses = ['pending', 'active', 'suspended'];
                    $this->statut = in_array($value, $allowedStatuses) ? $value : $this->statut;
                    break;
            }
        }
        
        // Mettre à jour dans la base de données
        $this->save();
    }

    // Sauvegarder la pharmacie dans la base de données
    public function save() {
        $pdo = self::initPDO();
        
        if ($this->id === null) {
            // Nouvelle pharmacie - Insertion
            $stmt = $pdo->prepare("
                INSERT INTO pharmacies (nom, adresse, telephone, email, numero_enregistrement, statut, date_creation) 
                VALUES (:nom, :adresse, :telephone, :email, :numero_enregistrement, :statut, :date_creation)
            ");
            
            $result = $stmt->execute([
                ':nom' => $this->nom,
                ':adresse' => $this->adresse,
                ':telephone' => $this->phoneNumber,
                ':email' => $this->email,
                ':numero_enregistrement' => $this->registrationNumber,
                ':statut' => $this->statut,
                ':date_creation' => $this->createdAt
            ]);
            
            if ($result) {
                $this->id = $pdo->lastInsertId();
            }
            
            return $result;
        } else {
            // Pharmacie existante - Mise à jour
            $stmt = $pdo->prepare("
                UPDATE pharmacies 
                SET nom = :nom, 
                    adresse = :adresse, 
                    telephone = :telephone, 
                    email = :email, 
                    numero_enregistrement = :numero_enregistrement, 
                    statut = :statut 
                WHERE id = :id
            ");
            
            return $stmt->execute([
                ':nom' => $this->nom,
                ':adresse' => $this->adresse,
                ':telephone' => $this->phoneNumber,
                ':email' => $this->email,
                ':numero_enregistrement' => $this->registrationNumber,
                ':statut' => $this->statut,
                ':id' => $this->id
            ]);
        }
    }

    // Méthodes de gestion du statut
    public function activate() {
        $this->statut = 'active';
        $this->save();
        SecurityService::logSecurityEvent('PHARMACY_STATUS', "Pharmacie {$this->nom} activée");
    }

    public function suspend() {
        $this->statut = 'suspended';
        $this->save();
        SecurityService::logSecurityEvent('PHARMACY_STATUS', "Pharmacie {$this->nom} suspendue");
    }
    
    // Méthode statique pour trouver une pharmacie par ID
    public static function findById($id) {
        $pdo = self::initPDO();
        $stmt = $pdo->prepare("SELECT * FROM pharmacies WHERE id = :id");
        $stmt->execute([':id' => $id]);
        
        $pharmacyData = $stmt->fetch(\PDO::FETCH_ASSOC);
        if (!$pharmacyData) {
            return null;
        }
        
        return self::createFromArray($pharmacyData);
    }
    
    // Méthode statique pour trouver une pharmacie par email
    public static function findByEmail($email) {
        $pdo = self::initPDO();
        $stmt = $pdo->prepare("SELECT * FROM pharmacies WHERE email = :email");
        $stmt->execute([':email' => $email]);
        
        $pharmacyData = $stmt->fetch(\PDO::FETCH_ASSOC);
        if (!$pharmacyData) {
            return null;
        }
        
        return self::createFromArray($pharmacyData);
    }
    
    // Méthode pour créer une pharmacie à partir d'un tableau
    public static function createFromArray($data) {
        return new self(
            $data['id'] ?? null,
            $data['nom'] ?? null,
            $data['adresse'] ?? null,
            $data['telephone'] ?? null,
            $data['email'] ?? null,
            $data['numero_enregistrement'] ?? null,
            $data['statut'] ?? 'pending',
            $data['date_creation'] ?? null
        );
    }

    // Getters
    public function getId() { return $this->id; }
    public function getName() { return $this->nom; }
    public function getAddress() { return $this->adresse; }
    public function getPhoneNumber() { return $this->phoneNumber; }
    public function getEmail() { return $this->email; }
    public function getRegistrationNumber() { return $this->registrationNumber; }
    public function getStatus() { return $this->statut; }
    public function getCreatedAt() { return $this->createdAt; }
}
