<?php
// Fonctions et utilitaires de sécurité

class SecurityService {
    // Génération de token CSRF
    public static function generateCSRFToken() {
        if (session_status() == PHP_SESSION_NONE) {
            session_start();
        }
        
        // Générer un token unique
        $token = bin2hex(random_bytes(32));
        
        // Stocker le token dans la session
        $_SESSION['csrf_token'] = $token;
        $_SESSION['csrf_token_expiry'] = time() + 1800; // Valide 30 minutes
        
        return $token;
    }

    // Validation du token CSRF
    public static function validateCSRFToken($token) {
        if (session_status() == PHP_SESSION_NONE) {
            session_start();
        }
        
        // Vérifier si le token existe et n'est pas expiré
        return isset($_SESSION['csrf_token']) && 
               $_SESSION['csrf_token'] === $token && 
               time() < $_SESSION['csrf_token_expiry'];
    }

    // Hachage sécurisé des mots de passe
    public static function hashPassword($password) {
        return password_hash($password, PASSWORD_ARGON2ID, [
            'memory_cost' => 1024 * 64,
            'time_cost' => 4,
            'threads' => 3
        ]);
    }

    // Vérification du mot de passe
    public static function verifyPassword($password, $hash) {
        return password_verify($password, $hash);
    }

    // Nettoyage des entrées utilisateur
    public static function sanitizeInput($input) {
        // Supprimer les espaces en début et fin
        $input = trim($input);
        
        // Convertir les caractères spéciaux en entités HTML
        $input = htmlspecialchars($input, ENT_QUOTES, 'UTF-8');
        
        return $input;
    }

    // Validation des emails
    public static function validateEmail($email) {
        return filter_var($email, FILTER_VALIDATE_EMAIL) !== false;
    }

    // Protection contre les injections SQL (à utiliser avec PDO)
    public static function preventSQLInjection($input) {
        // Supprimer les caractères spéciaux potentiellement dangereux
        $input = preg_replace('/[^\p{L}\p{N}\s-]/u', '', $input);
        
        return $input;
    }

    // Journalisation des événements de sécurité
    public static function logSecurityEvent($type, $message) {
        $logFile = ROOT_PATH . '/logs/security.log';
        $timestamp = date('Y-m-d H:i:s');
        
        $logEntry = "[{$timestamp}] [{$type}] {$message}\n";
        
        // Ajouter l'entrée au fichier journal
        file_put_contents($logFile, $logEntry, FILE_APPEND);
    }
}

// Fonctions de protection contre les attaques courantes
function preventClickjacking() {
    header('X-Frame-Options: DENY');
}

function enableXSSProtection() {
    header('X-XSS-Protection: 1; mode=block');
    header('Content-Security-Policy: default-src \'self\'; script-src \'self\' \'unsafe-inline\' \'unsafe-eval\'');
}

// Appeler ces fonctions au début de chaque script critique
preventClickjacking();
enableXSSProtection();
