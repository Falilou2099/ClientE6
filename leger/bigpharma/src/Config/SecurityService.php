<?php
namespace Config;

class SecurityService {
    /**
     * Nettoie les entrées utilisateur pour prévenir les attaques XSS
     * 
     * @param string $input Entrée à nettoyer
     * @return string Entrée nettoyée
     */
    public static function sanitizeInput($input) {
        if (is_string($input)) {
            // Supprimer les espaces en début et fin de chaîne
            $input = trim($input);
            // Convertir les caractères spéciaux en entités HTML
            $input = htmlspecialchars($input, ENT_QUOTES, 'UTF-8');
            return $input;
        }
        return $input;
    }
    
    /**
     * Prévient les injections SQL en échappant les caractères spéciaux
     * 
     * @param string $input Entrée à sécuriser
     * @return string Entrée sécurisée
     */
    public static function preventSQLInjection($input) {
        if (is_string($input)) {
            // Échapper les caractères spéciaux pour éviter les injections SQL
            $input = addslashes($input);
            return $input;
        }
        return $input;
    }
    
    /**
     * Valide une adresse email
     * 
     * @param string $email Adresse email à valider
     * @return bool True si l'email est valide, false sinon
     */
    public static function validateEmail($email) {
        return filter_var($email, FILTER_VALIDATE_EMAIL) !== false;
    }
    
    /**
     * Hache un mot de passe
     * 
     * @param string $password Mot de passe à hacher
     * @return string Mot de passe haché
     */
    public static function hashPassword($password) {
        return password_hash($password, PASSWORD_DEFAULT);
    }
    
    /**
     * Vérifie si un mot de passe correspond à son hash
     * 
     * @param string $password Mot de passe à vérifier
     * @param string $hash Hash à comparer
     * @return bool True si le mot de passe correspond, false sinon
     */
    public static function verifyPassword($password, $hash) {
        return password_verify($password, $hash);
    }
    
    /**
     * Génère un token aléatoire
     * 
     * @param int $length Longueur du token
     * @return string Token généré
     */
    public static function generateToken($length = 32) {
        return bin2hex(random_bytes($length / 2));
    }
    
    /**
     * Journalise un événement de sécurité
     * 
     * @param string $eventType Type d'événement (LOGIN, LOGIN_FAILED, etc.)
     * @param string $message Message décrivant l'événement
     * @param array $context Contexte supplémentaire (optionnel)
     * @return void
     */
    public static function logSecurityEvent($eventType, $message, $context = []) {
        $logFile = __DIR__ . '/../../logs/security.log';
        $logDir = dirname($logFile);
        
        // Créer le répertoire des logs s'il n'existe pas
        if (!file_exists($logDir)) {
            mkdir($logDir, 0755, true);
        }
        
        $timestamp = date('Y-m-d H:i:s');
        $ip = $_SERVER['REMOTE_ADDR'] ?? 'unknown';
        $userAgent = $_SERVER['HTTP_USER_AGENT'] ?? 'unknown';
        
        $contextStr = !empty($context) ? ' | Context: ' . json_encode($context) : '';
        $logMessage = "[$timestamp] [$ip] [$userAgent] [$eventType] $message$contextStr" . PHP_EOL;
        
        // Écrire dans le fichier de log
        file_put_contents($logFile, $logMessage, FILE_APPEND);
    }
}
