<?php
namespace Controllers;

use Models\User;
use Models\Pharmacy;
use Config\SecurityService;

class AuthController {
    private $user;
    private $pharmacy;

    public function __construct() {
        // Initialisation sans créer d'instances
    }

    // Afficher la page de connexion
    public function showLoginForm() {
        require_once SRC_PATH . '/Views/auth/login.php';
    }

    // Afficher la page d'inscription
    public function showRegisterForm() {
        require_once SRC_PATH . '/Views/auth/register.php';
    }

    // Traiter la soumission du formulaire de connexion
    public function processLogin() {
        if (session_status() === PHP_SESSION_NONE) {
            session_start();
        }
        
        $email = $_POST['email'] ?? '';
        $password = $_POST['password'] ?? '';
        $remember = isset($_POST['remember']);

        $result = $this->login($email, $password);

        if ($result['success']) {
            // Définir les variables de session
            $_SESSION['user_id'] = $result['user']->getId();
            $_SESSION['user_email'] = $result['user']->getEmail();
            $_SESSION['user_role'] = $result['user']->getRole();
            $_SESSION['authenticated'] = true;
            
            // Redirection vers la page publique
            header('Location: /bigpharma/');
            exit();
        } else {
            // Afficher la page de connexion avec l'erreur
            $error = $result['error'];
            require_once SRC_PATH . '/Views/auth/login.php';
        }
    }

    // Traiter la soumission du formulaire d'inscription
    public function processRegister() {
        // Récupérer les données du formulaire
        $data = [
            'name' => $_POST['pharmacy_name'] ?? '',
            'address' => $_POST['address'] ?? '',
            'phoneNumber' => $_POST['phone_number'] ?? '',
            'email' => $_POST['email'] ?? '',
            'registrationNumber' => $_POST['registration_number'] ?? '',
            'password' => $_POST['password'] ?? '',
            'confirmPassword' => $_POST['confirm_password'] ?? ''
        ];

        // Vérifier que les mots de passe correspondent
        if ($data['password'] !== $data['confirmPassword']) {
            $errors = ['Les mots de passe ne correspondent pas'];
            require_once SRC_PATH . '/Views/auth/register.php';
            return;
        }

        // Enregistrer la pharmacie
        $result = $this->registerPharmacy($data);

        if ($result['success']) {
            // Connecter l'utilisateur automatiquement
            $_SESSION['user_id'] = $result['user']->getId();
            $_SESSION['user_email'] = $result['user']->getEmail();
            $_SESSION['user_role'] = $result['user']->getRole();

            // Rediriger vers la page d'accueil
            header('Location: /bigpharma/');
            exit;
        } else {
            // Afficher la page d'inscription avec les erreurs
            $errors = $result['errors'];
            require_once SRC_PATH . '/Views/auth/register.php';
        }
    }

    // Déconnexion
    public function logout() {
        // Détruire la session
        session_start();
        session_destroy();

        // Rediriger vers la page d'accueil
        header('Location: /bigpharma/');
        exit;
    }

    // Inscription d'une pharmacie
    public function registerPharmacy($data) {
        // Créer une nouvelle pharmacie
        $pharmacy = new Pharmacy(
            null, // id
            $data['name'] ?? null,
            $data['address'] ?? null,
            $data['phoneNumber'] ?? null,
            $data['email'] ?? null,
            $data['registrationNumber'] ?? null
        );

        // Valider les données de la pharmacie
        $pharmacyErrors = $pharmacy->validate();
        if (!empty($pharmacyErrors)) {
            return [
                'success' => false,
                'errors' => $pharmacyErrors
            ];
        }

        // Enregistrer la pharmacie dans la base de données
        if (!$pharmacy->save()) {
            return [
                'success' => false,
                'errors' => ['Erreur lors de l\'enregistrement de la pharmacie']
            ];
        }

        // Créer un utilisateur administrateur pour la pharmacie
        $result = User::register(
            $data['email'],
            $data['password'],
            $pharmacy->getId(),
            'admin'
        );

        if (!$result['success']) {
            return [
                'success' => false,
                'errors' => $result['errors']
            ];
        }

        return [
            'success' => true,
            'pharmacy' => $pharmacy,
            'user' => $result['user']
        ];
    }

    // Connexion
    public function login($email, $password) {
        // Validation basique de l'email
        if (!SecurityService::validateEmail($email)) {
            return [
                'success' => false,
                'error' => 'Email invalide'
            ];
        }

        // Récupérer l'utilisateur depuis la base de données
        $user = User::findByEmail($email);
        
        if (!$user) {
            return [
                'success' => false,
                'error' => 'Identifiants incorrects'
            ];
        }

        // Vérifier le mot de passe
        if ($user->login($password)) {
            // Démarrer la session
            session_start();
            $_SESSION['user_id'] = $user->getId();
            $_SESSION['user_email'] = $user->getEmail();
            $_SESSION['user_role'] = $user->getRole();

            return [
                'success' => true,
                'user' => $user
            ];
        }

        return [
            'success' => false,
            'error' => 'Identifiants incorrects'
        ];
    }

    // Réinitialisation de mot de passe
    public function resetPassword($email) {
        // Validation de l'email
        if (!SecurityService::validateEmail($email)) {
            return [
                'success' => false,
                'error' => 'Email invalide'
            ];
        }
        
        // Vérifier si l'utilisateur existe
        $user = User::findByEmail($email);
        if (!$user) {
            return [
                'success' => false,
                'error' => 'Aucun compte associé à cet email'
            ];
        }
        
        // Générer un token de réinitialisation
        $resetToken = $user->createPasswordResetToken();
        
        if (!$resetToken) {
            return [
                'success' => false,
                'error' => 'Erreur lors de la génération du token de réinitialisation'
            ];
        }
        
        // Dans un environnement de production, on enverrait un email avec le lien
        // Pour ce prototype, nous allons simplement retourner le lien
        $resetLink = '/bigpharma/password-reset/form?token=' . $resetToken;
        
        return [
            'success' => true,
            'message' => 'Un email de réinitialisation a été envoyé',
            'link' => $resetLink // Pour le développement uniquement
        ];
    }
    
    // Afficher le formulaire de demande de réinitialisation de mot de passe
    public function showPasswordResetRequestForm() {
        require_once SRC_PATH . '/Views/auth/password_reset_request.php';
    }
    
    // Traiter la demande de réinitialisation de mot de passe
    public function processPasswordResetRequest() {
        $email = $_POST['email'] ?? '';
        
        $result = $this->resetPassword($email);
        
        if ($result['success']) {
            // En production, on afficherait simplement un message de succès
            // Pour le développement, nous allons afficher le lien de réinitialisation
            $success = $result['message'] . ' <br><a href="' . $result['link'] . '">Cliquez ici pour réinitialiser votre mot de passe</a>';
            require_once SRC_PATH . '/Views/auth/password_reset_request.php';
        } else {
            $error = $result['error'];
            require_once SRC_PATH . '/Views/auth/password_reset_request.php';
        }
    }
    
    // Afficher le formulaire de réinitialisation de mot de passe
    public function showPasswordResetForm() {
        $token = $_GET['token'] ?? '';
        
        // Vérifier si le token est valide
        $tokenData = User::verifyPasswordResetToken($token);
        
        if (!$tokenData) {
            $error = 'Token invalide ou expiré';
        }
        
        require_once SRC_PATH . '/Views/auth/password_reset_form.php';
    }
    
    // Traiter la réinitialisation de mot de passe
    public function processPasswordReset() {
        $token = $_POST['token'] ?? '';
        $password = $_POST['password'] ?? '';
        $confirmPassword = $_POST['confirm_password'] ?? '';
        
        // Vérifier que les mots de passe correspondent
        if ($password !== $confirmPassword) {
            $error = 'Les mots de passe ne correspondent pas';
            require_once SRC_PATH . '/Views/auth/password_reset_form.php';
            return;
        }
        
        // Vérifier la complexité du mot de passe
        if (strlen($password) < 8) {
            $error = 'Le mot de passe doit contenir au moins 8 caractères';
            require_once SRC_PATH . '/Views/auth/password_reset_form.php';
            return;
        }
        
        // Réinitialiser le mot de passe
        $result = User::resetPasswordWithToken($token, $password);
        
        if ($result['success']) {
            // Rediriger vers la page de connexion avec un message de succès
            $_SESSION['reset_success'] = 'Votre mot de passe a été réinitialisé avec succès. Vous pouvez maintenant vous connecter avec votre nouveau mot de passe.';
            header('Location: /bigpharma/login');
            exit;
        } else {
            $error = $result['error'];
            require_once SRC_PATH . '/Views/auth/password_reset_form.php';
        }
    }
}
