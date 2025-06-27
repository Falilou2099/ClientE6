<?php
namespace Controllers;

use Models\Client;

class ClientController extends BaseController {
    public function __construct() {
        parent::__construct();
    }
    /**
     * Affiche la liste des clients
     */
    public function index() {
        // Vérifier si l'utilisateur est connecté
        if (!isset($_SESSION['user_id'])) {
            header('Location: /bigpharma/public/auth/login');
            exit;
        }

        // Récupérer les paramètres de recherche et de pagination
        $search = isset($_GET['search']) ? trim($_GET['search']) : '';
        $page = isset($_GET['page']) ? (int)$_GET['page'] : 1;
        $perPage = 10; // Nombre de clients par page
        
        try {
            // Récupérer tous les clients (pour le comptage et la pagination)
            if (!empty($search)) {
                $allClients = Client::findByName($search);
            } else {
                $allClients = Client::getAll();
            }
            
            // Compter le nombre total de clients
            $totalClients = count($allClients);
            
            // Calculer le nombre total de pages
            $totalPages = ceil($totalClients / $perPage);
            
            // Vérifier que la page demandée existe
            if ($page > $totalPages && $totalPages > 0) {
                $page = $totalPages;
            } elseif ($page < 1) {
                $page = 1;
            }
            
            // Calculer l'offset pour la pagination
            $offset = ($page - 1) * $perPage;
            
            // Récupérer les clients pour la page courante
            $clients = array_slice($allClients, $offset, $perPage);
            
            // Charger la vue avec les données
            require_once __DIR__ . '/../Views/clients/index.php';
        } catch (\Exception $e) {
            // Journaliser l'erreur
            error_log('Erreur lors de la récupération des clients: ' . $e->getMessage());
            
            // Afficher un message d'erreur
            $error = 'Une erreur est survenue lors de la récupération des clients.';
            require_once __DIR__ . '/../Views/clients/index.php';
        }
    }
    
    /**
     * Affiche le formulaire d'ajout d'un client
     */
    public function create() {
        $data = $this->addPharmacyIdToData($data);
        // Vérifier si l'utilisateur est connecté
        if (!isset($_SESSION['user_id'])) {
            header('Location: /bigpharma/public/auth/login');
            exit;
        }
        
        // Charger la vue du formulaire
        require_once __DIR__ . '/../Views/clients/create.php';
    }
    
    /**
     * Traite l'ajout d'un nouveau client
     */
    public function store() {
        $data = $this->addPharmacyIdToData($data);
        // Vérifier si l'utilisateur est connecté
        if (!isset($_SESSION['user_id'])) {
            header('Location: /bigpharma/public/auth/login');
            exit;
        }
        
        // Vérifier la méthode de requête
        if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
            header('Location: /bigpharma/public/clients');
            exit;
        }
        
        // Récupérer et valider les données du formulaire
        $nom = isset($_POST['nom']) ? trim($_POST['nom']) : '';
        $prenom = isset($_POST['prenom']) ? trim($_POST['prenom']) : '';
        $email = isset($_POST['email']) ? trim($_POST['email']) : '';
        $telephone = isset($_POST['telephone']) ? trim($_POST['telephone']) : '';
        $adresse = isset($_POST['adresse']) ? trim($_POST['adresse']) : '';
        
        // Validation basique
        $errors = [];
        
        if (empty($nom)) {
            $errors[] = 'Le nom est obligatoire.';
        }
        
        if (empty($prenom)) {
            $errors[] = 'Le prénom est obligatoire.';
        }
        
        if (!empty($email) && !filter_var($email, FILTER_VALIDATE_EMAIL)) {
            $errors[] = 'L\'adresse email n\'est pas valide.';
        }
        
        // Si des erreurs sont présentes, rediriger vers le formulaire avec les erreurs
        if (!empty($errors)) {
            $_SESSION['errors'] = $errors;
            $_SESSION['form_data'] = $_POST;
            header('Location: /bigpharma/public/clients/create');
            exit;
        }
        
        try {
            // Créer et sauvegarder le client
            $client = new Client(
                null,
                $nom,
                $prenom,
                $email,
                $telephone,
                $adresse,
                $_SESSION['pharmacie_id'] ?? null
            );
            
            if ($client->save()) {
                // Rediriger avec un message de succès
                $_SESSION['success'] = 'Le client a été ajouté avec succès.';
                header('Location: /bigpharma/public/clients');
                exit;
            } else {
                // Erreur lors de la sauvegarde
                $_SESSION['errors'] = ['Une erreur est survenue lors de l\'enregistrement du client.'];
                $_SESSION['form_data'] = $_POST;
                header('Location: /bigpharma/public/clients/create');
                exit;
            }
        } catch (\Exception $e) {
            // Journaliser l'erreur
            error_log('Erreur lors de l\'ajout d\'un client: ' . $e->getMessage());
            
            // Rediriger avec un message d'erreur
            $_SESSION['errors'] = ['Une erreur est survenue lors de l\'enregistrement du client.'];
            $_SESSION['form_data'] = $_POST;
            header('Location: /bigpharma/public/clients/create');
            exit;
        }
    }
    
    /**
     * Affiche les détails d'un client
     */
    public function show($id) {
        // Vérifier si l'utilisateur est connecté
        if (!isset($_SESSION['user_id'])) {
            header('Location: /bigpharma/public/auth/login');
            exit;
        }
        
        try {
            // Récupérer le client par son ID
            $client = Client::findById($id);
            
            if (!$client) {
                // Client non trouvé
                $_SESSION['errors'] = ['Le client demandé n\'existe pas.'];
                header('Location: /bigpharma/public/clients');
                exit;
            }
            
            // Charger la vue avec les données du client
            require_once __DIR__ . '/../Views/clients/show.php';
        } catch (\Exception $e) {
            // Journaliser l'erreur
            error_log('Erreur lors de la récupération du client: ' . $e->getMessage());
            
            // Rediriger avec un message d'erreur
            $_SESSION['errors'] = ['Une erreur est survenue lors de la récupération du client.'];
            header('Location: /bigpharma/public/clients');
            exit;
        }
    }
    
    /**
     * Affiche le formulaire de modification d'un client
     */
    public function edit($id) {
        // Vérifier si l'utilisateur est connecté
        if (!isset($_SESSION['user_id'])) {
            header('Location: /bigpharma/public/auth/login');
            exit;
        }
        
        try {
            // Récupérer le client par son ID
            $client = Client::findById($id);
            
            if (!$client) {
                // Client non trouvé
                $_SESSION['errors'] = ['Le client demandé n\'existe pas.'];
                header('Location: /bigpharma/public/clients');
                exit;
            }
            
            // Charger la vue avec les données du client
            require_once __DIR__ . '/../Views/clients/edit.php';
        } catch (\Exception $e) {
            // Journaliser l'erreur
            error_log('Erreur lors de la récupération du client: ' . $e->getMessage());
            
            // Rediriger avec un message d'erreur
            $_SESSION['errors'] = ['Une erreur est survenue lors de la récupération du client.'];
            header('Location: /bigpharma/public/clients');
            exit;
        }
    }
    
    /**
     * Traite la modification d'un client
     */
    public function update($id) {
        // Vérifier si l'utilisateur est connecté
        if (!isset($_SESSION['user_id'])) {
            if ($this->isAjaxRequest()) {
                $this->sendJsonResponse(false, 'Vous devez être connecté pour effectuer cette action.');
            } else {
                header('Location: /bigpharma/public/auth/login');
                exit;
            }
        }
        
        // Vérifier la méthode de requête
        if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
            if ($this->isAjaxRequest()) {
                $this->sendJsonResponse(false, 'Méthode non autorisée.');
            } else {
                header('Location: /bigpharma/public/clients');
                exit;
            }
        }
        
        try {
            // Récupérer le client par son ID
            $client = Client::findById($id);
            
            if (!$client) {
                // Client non trouvé
                if ($this->isAjaxRequest()) {
                    $this->sendJsonResponse(false, 'Le client demandé n\'existe pas.');
                } else {
                    $_SESSION['errors'] = ['Le client demandé n\'existe pas.'];
                    header('Location: /bigpharma/public/clients');
                    exit;
                }
            }
            
            // Récupérer et valider les données du formulaire
            $nom = isset($_POST['nom']) ? trim($_POST['nom']) : '';
            $prenom = isset($_POST['prenom']) ? trim($_POST['prenom']) : '';
            $email = isset($_POST['email']) ? trim($_POST['email']) : '';
            $telephone = isset($_POST['telephone']) ? trim($_POST['telephone']) : '';
            $adresse = isset($_POST['adresse']) ? trim($_POST['adresse']) : '';
            
            // Validation basique
            $errors = [];
            
            if (empty($nom)) {
                $errors[] = 'Le nom est obligatoire.';
            }
            
            if (empty($prenom)) {
                $errors[] = 'Le prénom est obligatoire.';
            }
            
            if (!empty($email) && !filter_var($email, FILTER_VALIDATE_EMAIL)) {
                $errors[] = 'L\'adresse email n\'est pas valide.';
            }
            
            // Si des erreurs sont présentes
            if (!empty($errors)) {
                if ($this->isAjaxRequest()) {
                    $this->sendJsonResponse(false, 'Erreurs de validation.', ['errors' => $errors]);
                } else {
                    $_SESSION['errors'] = $errors;
                    $_SESSION['form_data'] = $_POST;
                    header('Location: /bigpharma/public/clients/edit/' . $id);
                    exit;
                }
            }
            
            // Mettre à jour les données du client
            $client->setNom($nom)
                  ->setPrenom($prenom)
                  ->setEmail($email)
                  ->setTelephone($telephone)
                  ->setAdresse($adresse);
            
            if ($client->save()) {
                // Succès
                if ($this->isAjaxRequest()) {
                    $this->sendJsonResponse(true, 'Le client a été modifié avec succès.', [
                        'client' => [
                            'id' => $client->getId(),
                            'nom' => $client->getNom(),
                            'prenom' => $client->getPrenom(),
                            'email' => $client->getEmail(),
                            'telephone' => $client->getTelephone(),
                            'adresse' => $client->getAdresse()
                        ]
                    ]);
                } else {
                    $_SESSION['success'] = 'Le client a été modifié avec succès.';
                    header('Location: /bigpharma/public/clients');
                    exit;
                }
            } else {
                // Erreur lors de la sauvegarde
                if ($this->isAjaxRequest()) {
                    $this->sendJsonResponse(false, 'Une erreur est survenue lors de la modification du client.');
                } else {
                    $_SESSION['errors'] = ['Une erreur est survenue lors de la modification du client.'];
                    $_SESSION['form_data'] = $_POST;
                    header('Location: /bigpharma/public/clients/edit/' . $id);
                    exit;
                }
            }
        } catch (\Exception $e) {
            // Journaliser l'erreur
            error_log('Erreur lors de la modification d\'un client: ' . $e->getMessage());
            
            // Erreur
            if ($this->isAjaxRequest()) {
                $this->sendJsonResponse(false, 'Une erreur est survenue lors de la modification du client.');
            } else {
                $_SESSION['errors'] = ['Une erreur est survenue lors de la modification du client.'];
                $_SESSION['form_data'] = $_POST;
                header('Location: /bigpharma/public/clients/edit/' . $id);
                exit;
            }
        }
    }
    
    /**
     * Supprime un client
     */
    public function delete($id) {
        // Vérifier si l'utilisateur est connecté
        if (!isset($_SESSION['user_id'])) {
            header('Location: /bigpharma/public/auth/login');
            exit;
        }
        
        // Vérifier la méthode de requête
        if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
            header('Location: /bigpharma/public/clients');
            exit;
        }
        
        try {
            // Récupérer le client par son ID
            $client = Client::findById($id);
            
            if (!$client) {
                // Client non trouvé
                $_SESSION['errors'] = ['Le client demandé n\'existe pas.'];
                header('Location: /bigpharma/public/clients');
                exit;
            }
            
            if ($client->delete()) {
                // Rediriger avec un message de succès
                $_SESSION['success'] = 'Le client a été supprimé avec succès.';
            } else {
                // Erreur lors de la suppression
                $_SESSION['errors'] = ['Une erreur est survenue lors de la suppression du client.'];
            }
        } catch (\Exception $e) {
            // Journaliser l'erreur
            error_log('Erreur lors de la suppression d\'un client: ' . $e->getMessage());
            
            // Rediriger avec un message d'erreur
            $_SESSION['errors'] = ['Une erreur est survenue lors de la suppression du client.'];
        }
        
        header('Location: /bigpharma/public/clients');
        exit;
    }
    
    /**
     * API pour rechercher des clients
     */
    public function search() {
        // Vérifier si l'utilisateur est connecté
        if (!isset($_SESSION['user_id'])) {
            header('Content-Type: application/json');
            echo json_encode(['success' => false, 'message' => 'Non autorisé']);
            exit;
        }
        
        // Récupérer le terme de recherche
        $search = isset($_GET['search']) ? trim($_GET['search']) : '';
        
        try {
            // Rechercher les clients
            if (!empty($search)) {
                // Recherche par terme de recherche
                $clients = Client::findByName($search);
            } else {
                // Sans terme de recherche, récupérer tous les clients
                $clients = Client::getAll();
            }
            
            // Formater les résultats pour l'API
            $results = [];
            foreach ($clients as $client) {
                $results[] = [
                    'id' => $client->getId(),
                    'nom_complet' => $client->getNomComplet(),
                    'email' => $client->getEmail(),
                    'telephone' => $client->getTelephone(),
                    'adresse' => $client->getAdresse()
                ];
            }
            
            // Trier les clients par ordre alphabétique (nom, puis prénom)
            usort($results, function($a, $b) {
                return $a['nom_complet'] <=> $b['nom_complet'];
            });
            
            // Limiter à 5 clients maximum
            $totalResults = count($results);
            $results = array_slice($results, 0, 5);
            
            // Retourner les résultats en JSON
            header('Content-Type: application/json');
            echo json_encode([
                'success' => true,
                'clients' => $results,
                'count' => count($results),
                'limited' => $totalResults > 5 // Indique si la liste a été limitée
            ]);
        } catch (\Exception $e) {
            // Journaliser l'erreur
            error_log('Erreur lors de la recherche de clients: ' . $e->getMessage());
            
            // Retourner une erreur en JSON
            header('Content-Type: application/json');
            echo json_encode([
                'success' => false,
                'message' => 'Une erreur est survenue lors de la recherche de clients.'
            ]);
        }
    }
    
    /**
     * Vérifie si la requête est une requête AJAX
     * @return bool True si c'est une requête AJAX, false sinon
     */
    private function isAjaxRequest() {
        return (!empty($_SERVER['HTTP_X_REQUESTED_WITH']) && 
                strtolower($_SERVER['HTTP_X_REQUESTED_WITH']) === 'xmlhttprequest');
    }
    
    /**
     * Envoie une réponse JSON
     * @param bool $success Indique si l'opération a réussi
     * @param string $message Message à afficher
     * @param array $data Données supplémentaires
     */
    private function sendJsonResponse($success, $message, $data = []) {
        header('Content-Type: application/json');
        echo json_encode([
            'success' => $success,
            'message' => $message,
            'data' => $data
        ]);
        exit;
    }
}
