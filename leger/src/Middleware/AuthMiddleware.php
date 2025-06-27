<?php
namespace Middleware;

class AuthMiddleware {
    /**
     * Vérifie si l'utilisateur est authentifié
     * 
     * @return bool True si l'utilisateur est authentifié, false sinon
     */
    public static function isAuthenticated() {
        // Vérifier si la session est démarrée
        if (session_status() === PHP_SESSION_NONE) {
            session_start();
        }
        
        // Vérifier si l'utilisateur est connecté
        return isset($_SESSION['user_id']) && !empty($_SESSION['user_id']);
    }
    
    /**
     * Redirige vers la page de connexion si l'utilisateur n'est pas authentifié
     * 
     * @param array $publicRoutes Routes accessibles sans authentification
     * @param string $currentRoute Route actuelle
     * @return bool True si l'utilisateur est authentifié ou si la route est publique, false sinon
     */
    public static function requireAuth($publicRoutes, $currentRoute) {
        // Si la route est publique, autoriser l'accès
        if (in_array($currentRoute, $publicRoutes)) {
            return true;
        }
        
        // Si l'utilisateur est authentifié, autoriser l'accès
        if (self::isAuthenticated()) {
            return true;
        }
        
        // Sinon, rediriger vers la page de connexion
        header('Location: /bigpharma/public/login');
        exit;
    }
}
