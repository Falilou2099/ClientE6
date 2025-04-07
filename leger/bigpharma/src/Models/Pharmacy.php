z<?php
namespace Models;

use Config\SecurityService;

class Pharmacy {
    private $id;
    private $name;
    private $address;
    private $phoneNumber;
    private $email;
    private $registrationNumber;
    private $status;
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
    public function __construct($id = null, $name = null, $address = null, $phoneNumber = null, $email = null, $registrationNumber = null, $status = 'pending', $createdAt = null) {
        $this->id = $id;
        $this->name = $name ? SecurityService::sanitizeInput($name) : null;
        $this->address = $address ? SecurityService::sanitizeInput($address) : null;
        $this->phoneNumber = $phoneNumber ? $this->formatPhoneNumber($phoneNumber) : null;
        $this->email = $email ? SecurityService::sanitizeInput($email) : null;
        $this->registrationNumber = $registrationNumber ? SecurityService::preventSQLInjection($registrationNumber) : null;
        $this->status = $status; // Par défaut, en attente de validation
        $this->createdAt = $createdAt ?? date('Y-m-d H:i:s');
    }

    // Validation des données de la pharmacie
    public function validate() {
        $errors = [];

        // Validation du nom
        if (empty($this->name) || strlen($this->name) < 2) {
            $errors[] = "Le nom de la pharmacie est invalide";
        }

        // Validation de l'adresse
        if (empty($this->address) || strlen($this->address) < 5) {
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
                case 'name':
                    $this->name = SecurityService::sanitizeInput($value);
                    break;
                case 'address':
                    $this->address = SecurityService::sanitizeInput($value);
                    break;
                case 'phoneNumber':
                    $this->phoneNumber = $this->formatPhoneNumber($value);
                    break;
                case 'email':
                    if (SecurityService::validateEmail($value)) {
                        $this->email = SecurityService::sanitizeInput($value);
                    }
                    break;
                case 'status':
                    $allowedStatuses = ['pending', 'active', 'suspended'];
                    $this->status = in_array($value, $allowedStatuses) ? $value : $this->status;
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
                INSERT INTO pharmacies (name, address, phone_number, email, registration_number, status, created_at) 
                VALUES (:name, :address, :phone_number, :email, :registration_number, :status, :created_at)
            ");
            
            $result = $stmt->execute([
                ':name' => $this->name,
                ':address' => $this->address,
                ':phone_number' => $this->phoneNumber,
                ':email' => $this->email,
                ':registration_number' => $this->registrationNumber,
                ':status' => $this->status,
                ':created_at' => $this->createdAt
            ]);
            
            if ($result) {
                $this->id = $pdo->lastInsertId();
            }
            
            return $result;
        } else {
            // Pharmacie existante - Mise à jour
            $stmt = $pdo->prepare("
                UPDATE pharmacies 
                SET name = :name, 
                    address = :address, 
                    phone_number = :phone_number, 
                    email = :email, 
                    registration_number = :registration_number, 
                    status = :status 
                WHERE id = :id
            ");
            
            return $stmt->execute([
                ':name' => $this->name,
                ':address' => $this->address,
                ':phone_number' => $this->phoneNumber,
                ':email' => $this->email,
                ':registration_number' => $this->registrationNumber,
                ':status' => $this->status,
                ':id' => $this->id
            ]);
        }
    }

    // Méthodes de gestion du statut
    public function activate() {
        $this->status = 'active';
        $this->save();
        SecurityService::logSecurityEvent('PHARMACY_STATUS', "Pharmacie {$this->name} activée");
    }

    public function suspend() {
        $this->status = 'suspended';
        $this->save();
        SecurityService::logSecurityEvent('PHARMACY_STATUS', "Pharmacie {$this->name} suspendue");
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
            $data['name'] ?? null,
            $data['address'] ?? null,
            $data['phone_number'] ?? null,
            $data['email'] ?? null,
            $data['registration_number'] ?? null,
            $data['status'] ?? 'pending',
            $data['created_at'] ?? null
        );
    }

    // Getters
    public function getId() { return $this->id; }
    public function getName() { return $this->name; }
    public function getAddress() { return $this->address; }
    public function getPhoneNumber() { return $this->phoneNumber; }
    public function getEmail() { return $this->email; }
    public function getRegistrationNumber() { return $this->registrationNumber; }
    public function getStatus() { return $this->status; }
    public function getCreatedAt() { return $this->createdAt; }
}
