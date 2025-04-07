<?php
namespace Models;

use Config\SecurityService;

class User {
    private $id;
    private $email;
    private $password;
    private $pharmacyId;
    private $role;
    private $status;
    private $createdAt;
    private $lastLogin;
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
    public function __construct($id = null, $email = null, $password = null, $pharmacyId = null, $role = 'pharmacist', $status = 'active', $createdAt = null, $lastLogin = null) {
        $this->id = $id;
        $this->email = $email ? SecurityService::sanitizeInput($email) : null;
        $this->password = $password;
        $this->pharmacyId = $pharmacyId;
        $this->role = $role;
        $this->status = $status;
        $this->createdAt = $createdAt ?? date('Y-m-d H:i:s');
        $this->lastLogin = $lastLogin;
    }

    // Validation des données utilisateur
    public function validate() {
        $errors = [];

        // Validation de l'email
        if (!SecurityService::validateEmail($this->email)) {
            $errors[] = "Email invalide";
        }

        // Vérifier si l'email existe déjà
        if ($this->emailExists($this->email)) {
            $errors[] = "Cet email est déjà utilisé";
        }

        // Validation du mot de passe (uniquement pour les nouveaux utilisateurs)
        if ($this->id === null && strlen($this->password) < 8) {
            $errors[] = "Le mot de passe doit contenir au moins 8 caractères";
        }

        return $errors;
    }

    // Vérifier si un email existe déjà
    private function emailExists($email) {
        if ($this->id !== null) {
            // Si l'utilisateur existe déjà, vérifier si l'email appartient à un autre utilisateur
            $pdo = self::initPDO();
            $stmt = $pdo->prepare("SELECT id FROM users WHERE email = :email AND id != :id");
            $stmt->execute([':email' => $email, ':id' => $this->id]);
        } else {
            // Pour un nouvel utilisateur, vérifier si l'email existe
            $pdo = self::initPDO();
            $stmt = $pdo->prepare("SELECT id FROM users WHERE email = :email");
            $stmt->execute([':email' => $email]);
        }
        
        return $stmt->fetch() !== false;
    }

    // Méthodes de gestion du profil
    public function updateProfile($data) {
        // Mettre à jour les informations du profil avec validation
        foreach ($data as $key => $value) {
            switch ($key) {
                case 'email':
                    if (SecurityService::validateEmail($value)) {
                        $this->email = SecurityService::sanitizeInput($value);
                    }
                    break;
                case 'pharmacyId':
                    $this->pharmacyId = SecurityService::preventSQLInjection($value);
                    break;
                case 'role':
                    // Limiter les rôles possibles
                    $allowedRoles = ['pharmacist', 'admin', 'manager'];
                    $this->role = in_array($value, $allowedRoles) ? $value : $this->role;
                    break;
            }
        }
        
        // Mettre à jour dans la base de données
        $this->save();
    }

    // Sauvegarder l'utilisateur dans la base de données
    public function save() {
        $pdo = self::initPDO();
        
        if ($this->id === null) {
            // Nouvel utilisateur - Insertion
            $stmt = $pdo->prepare("
                INSERT INTO users (email, password, pharmacy_id, role, status, created_at, last_login) 
                VALUES (:email, :password, :pharmacy_id, :role, :status, :created_at, :last_login)
            ");
            
            $result = $stmt->execute([
                ':email' => $this->email,
                ':password' => $this->password,
                ':pharmacy_id' => $this->pharmacyId,
                ':role' => $this->role,
                ':status' => $this->status,
                ':created_at' => $this->createdAt,
                ':last_login' => $this->lastLogin
            ]);
            
            if ($result) {
                $this->id = $pdo->lastInsertId();
            }
            
            return $result;
        } else {
            // Utilisateur existant - Mise à jour
            $stmt = $pdo->prepare("
                UPDATE users 
                SET email = :email, 
                    password = :password, 
                    pharmacy_id = :pharmacy_id, 
                    role = :role, 
                    status = :status, 
                    last_login = :last_login 
                WHERE id = :id
            ");
            
            return $stmt->execute([
                ':email' => $this->email,
                ':password' => $this->password,
                ':pharmacy_id' => $this->pharmacyId,
                ':role' => $this->role,
                ':status' => $this->status,
                ':last_login' => $this->lastLogin,
                ':id' => $this->id
            ]);
        }
    }

    // Méthode de connexion
    public function login($password) {
        // Vérifier le mot de passe
        if (SecurityService::verifyPassword($password, $this->password)) {
            // Mettre à jour la dernière connexion
            $this->lastLogin = date('Y-m-d H:i:s');
            
            // Mettre à jour dans la base de données
            $pdo = self::initPDO();
            $stmt = $pdo->prepare("UPDATE users SET last_login = :last_login WHERE id = :id");
            $stmt->execute([':last_login' => $this->lastLogin, ':id' => $this->id]);
            
            // Journaliser la connexion réussie
            SecurityService::logSecurityEvent('LOGIN', "Connexion réussie pour {$this->email}");
            
            return true;
        }
        
        // Journaliser la tentative de connexion échouée
        SecurityService::logSecurityEvent('LOGIN_FAILED', "Tentative de connexion échouée pour {$this->email}");
        
        return false;
    }
    
    // Méthode statique pour trouver un utilisateur par email
    public static function findByEmail($email) {
        $pdo = self::initPDO();
        $stmt = $pdo->prepare("SELECT * FROM users WHERE email = :email");
        $stmt->execute([':email' => $email]);
        
        $userData = $stmt->fetch(\PDO::FETCH_ASSOC);
        if (!$userData) {
            return null;
        }
        
        return self::createFromArray($userData);
    }
    
    // Méthode statique pour trouver un utilisateur par ID
    public static function findById($id) {
        $pdo = self::initPDO();
        $stmt = $pdo->prepare("SELECT * FROM users WHERE id = :id");
        $stmt->execute([':id' => $id]);
        
        $userData = $stmt->fetch(\PDO::FETCH_ASSOC);
        if (!$userData) {
            return null;
        }
        
        return self::createFromArray($userData);
    }
    
    // Méthode pour créer un utilisateur à partir d'un tableau
    public static function createFromArray($data) {
        return new self(
            $data['id'] ?? null,
            $data['email'] ?? null,
            $data['password'] ?? null,
            $data['pharmacy_id'] ?? null,
            $data['role'] ?? 'pharmacist',
            $data['status'] ?? 'active',
            $data['created_at'] ?? null,
            $data['last_login'] ?? null
        );
    }
    
    // Méthode pour enregistrer un nouvel utilisateur
    public static function register($email, $password, $pharmacyId = null, $role = 'pharmacist') {
        $hashedPassword = SecurityService::hashPassword($password);
        
        $user = new self(null, $email, $hashedPassword, $pharmacyId, $role);
        $errors = $user->validate();
        
        if (empty($errors)) {
            $user->save();
            return ['success' => true, 'user' => $user];
        }
        
        return ['success' => false, 'errors' => $errors];
    }

    // Getters
    public function getId() { return $this->id; }
    public function getEmail() { return $this->email; }
    public function getPharmacyId() { return $this->pharmacyId; }
    public function getRole() { return $this->role; }
    public function getStatus() { return $this->status; }
    public function getCreatedAt() { return $this->createdAt; }
    public function getLastLogin() { return $this->lastLogin; }
    
    // Récupérer le nom de la pharmacie associée à l'utilisateur
    public function getPharmacyName() {
        if (!$this->pharmacyId) {
            return null;
        }
        
        $pharmacy = Pharmacy::findById($this->pharmacyId);
        return $pharmacy ? $pharmacy->getName() : null;
    }
    
    // Méthode pour créer un token de réinitialisation de mot de passe
    public function createPasswordResetToken() {
        $pdo = self::initPDO();
        
        // Générer un token unique
        $token = bin2hex(random_bytes(32));
        
        // Définir la date d'expiration (24 heures)
        $expiresAt = date('Y-m-d H:i:s', strtotime('+24 hours'));
        
        // Supprimer les anciens tokens non utilisés pour cet utilisateur
        $stmt = $pdo->prepare("
            DELETE FROM password_reset_tokens 
            WHERE user_id = :user_id AND used = 0
        ");
        $stmt->execute([':user_id' => $this->id]);
        
        // Insérer le nouveau token
        $stmt = $pdo->prepare("
            INSERT INTO password_reset_tokens (user_id, token, expires_at) 
            VALUES (:user_id, :token, :expires_at)
        ");
        
        $result = $stmt->execute([
            ':user_id' => $this->id,
            ':token' => $token,
            ':expires_at' => $expiresAt
        ]);
        
        if ($result) {
            return $token;
        }
        
        return false;
    }
    
    // Méthode pour vérifier si un token est valide
    public static function verifyPasswordResetToken($token) {
        $pdo = self::initPDO();
        
        // Récupérer le token
        $stmt = $pdo->prepare("
            SELECT * FROM password_reset_tokens 
            WHERE token = :token 
            AND used = 0 
            AND expires_at > NOW()
        ");
        
        $stmt->execute([':token' => $token]);
        $tokenData = $stmt->fetch(\PDO::FETCH_ASSOC);
        
        if (!$tokenData) {
            return false;
        }
        
        return $tokenData;
    }
    
    // Méthode pour réinitialiser le mot de passe avec un token
    public static function resetPasswordWithToken($token, $newPassword) {
        $tokenData = self::verifyPasswordResetToken($token);
        
        if (!$tokenData) {
            return [
                'success' => false,
                'error' => 'Token invalide ou expiré'
            ];
        }
        
        $userId = $tokenData['user_id'];
        $user = self::findById($userId);
        
        if (!$user) {
            return [
                'success' => false,
                'error' => 'Utilisateur introuvable'
            ];
        }
        
        // Hacher le nouveau mot de passe
        $hashedPassword = SecurityService::hashPassword($newPassword);
        
        // Mettre à jour le mot de passe
        $pdo = self::initPDO();
        $stmt = $pdo->prepare("
            UPDATE users 
            SET password = :password 
            WHERE id = :id
        ");
        
        $passwordUpdated = $stmt->execute([
            ':password' => $hashedPassword,
            ':id' => $userId
        ]);
        
        if (!$passwordUpdated) {
            return [
                'success' => false,
                'error' => 'Erreur lors de la mise à jour du mot de passe'
            ];
        }
        
        // Marquer le token comme utilisé
        $stmt = $pdo->prepare("
            UPDATE password_reset_tokens 
            SET used = 1 
            WHERE id = :id
        ");
        
        $stmt->execute([':id' => $tokenData['id']]);
        
        return [
            'success' => true,
            'message' => 'Mot de passe mis à jour avec succès'
        ];
    }
}
