<?php
namespace Controllers;

use Models\User;
use Models\Pharmacy;

class ProfileController {
    
    public function __construct() {
        // Vérifier si l'utilisateur est connecté
        if (session_status() === PHP_SESSION_NONE) {
            session_start();
        }
        
        if (!isset($_SESSION['user_id'])) {
            header('Location: /bigpharma/public/login');
            exit();
        }
    }
    
    // Afficher la page de profil
    public function index() {
        $userId = $_SESSION['user_id'] ?? null;
        $user = User::findById($userId);
        
        if (!$user) {
            header('Location: /bigpharma/public/login');
            exit();
        }
        
        // Récupérer les informations de la pharmacie si l'utilisateur est associé à une pharmacie
        $pharmacyId = $user->getPharmacyId();
        $pharmacy = null;
        
        if ($pharmacyId) {
            $pharmacy = Pharmacy::findById($pharmacyId);
        }
        
        // Préparer les données pour la vue
        $userData = [
            'id' => $user->getId(),
            'email' => $user->getEmail(),
            'role' => $user->getRole(),
            'status' => $user->getStatus(),
            'createdAt' => $user->getCreatedAt(),
            'lastLogin' => $user->getLastLogin(),
            'appAccess' => $user->getAppAccess(),
            'pharmacyName' => $user->getPharmacyName()
        ];
        
        // Charger la vue
        include ROOT_PATH . '/templates/header.php';
        include ROOT_PATH . '/src/Views/profile/index.php';
        include ROOT_PATH . '/templates/footer.php';
    }
    
    // Mettre à jour le profil (pour une future implémentation)
    public function update() {
        // Cette méthode sera implémentée ultérieurement pour permettre la mise à jour du profil
        header('Location: /bigpharma/public/profile');
        exit();
    }
}
