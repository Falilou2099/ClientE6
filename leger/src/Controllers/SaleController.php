<?php
namespace Controllers;

use Models\Product;
use Models\Client;
use Models\Sale;
use Models\Pharmacy;
use Config\SecurityService;

class SaleController extends BaseController {
    private $productModel;
    private $clientModel;
    private $saleModel;

    public function __construct() {
        parent::__construct();
        $this->productModel = new Product();
        $this->clientModel = new Client();
    }

    // Méthode pour traiter la vente d'un produit
    public function processSale() {
        // Vérifier la méthode HTTP
        if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
            $this->sendJsonResponse(['success' => false, 'message' => 'Méthode non autorisée. Seules les requêtes POST sont acceptées.'], 405);
            return;
        }
        
        // Vérifier si l'utilisateur est authentifié
        if (!isset($_SESSION['authenticated']) || $_SESSION['authenticated'] !== true) {
            $this->sendJsonResponse(['success' => false, 'message' => 'Vous devez être connecté pour effectuer une vente.'], 401);
            return;
        }

        // Récupérer et valider les données
        if (!isset($_POST['product_id']) || !isset($_POST['client_id']) || !isset($_POST['quantity'])) {
            $this->sendJsonResponse(['success' => false, 'message' => 'Données manquantes. Veuillez fournir product_id, client_id et quantity.'], 400);
            return;
        }
        
        $productId = intval($_POST['product_id']);
        $clientId = intval($_POST['client_id']);
        $quantity = intval($_POST['quantity']);
        
        // Récupérer l'ID de la pharmacie depuis la session ou utiliser une valeur par défaut
        $pharmacyId = isset($_SESSION['pharmacy_id']) ? intval($_SESSION['pharmacy_id']) : null;
        
        // Si l'ID de la pharmacie n'est pas disponible, essayer de le récupérer à partir de l'utilisateur connecté
        if (!$pharmacyId && isset($_SESSION['user_id'])) {
            $user = \Models\User::findById($_SESSION['user_id']);
            if ($user) {
                $pharmacyId = $user->getPharmacyId();
            }
        }
        
        // Si toujours pas d'ID de pharmacie, utiliser une valeur par défaut pour le développement
        if (!$pharmacyId) {
            $pharmacyId = 1; // Valeur par défaut pour le développement
            error_log("Attention: Utilisation d'un ID de pharmacie par défaut pour la vente. Utilisateur non associé à une pharmacie.");
        }

        // Validation des données
        if ($productId <= 0) {
            $this->sendJsonResponse(['success' => false, 'message' => 'ID de produit invalide. L\'ID doit être un nombre positif.'], 400);
            return;
        }

        if ($clientId <= 0) {
            $this->sendJsonResponse(['success' => false, 'message' => 'ID de client invalide. L\'ID doit être un nombre positif.'], 400);
            return;
        }

        if ($quantity <= 0) {
            $this->sendJsonResponse(['success' => false, 'message' => 'Quantité invalide. La quantité doit être un nombre positif.'], 400);
            return;
        }
        
        if ($quantity > 100) { // Limite arbitraire pour éviter les erreurs
            $this->sendJsonResponse(['success' => false, 'message' => 'Quantité trop élevée. La quantité maximum est de 100 unités par vente.'], 400);
            return;
        }
        
        if ($pharmacyId <= 0) {
            $this->sendJsonResponse(['success' => false, 'message' => 'ID de pharmacie invalide. Veuillez contacter l\'administrateur.'], 400);
            return;
        }

        try {
            // Récupérer le produit
            $product = Product::getById($productId);
            if (!$product) {
                $this->sendJsonResponse(['success' => false, 'message' => 'Produit non trouvé. L\'ID du produit n\'existe pas dans la base de données.'], 404);
                return;
            }
            
            // Vérifier si le produit nécessite une ordonnance
            if ($product->getEstOrdonnance()) {
                $this->sendJsonResponse(['success' => false, 'message' => 'Ce produit nécessite une ordonnance. Veuillez utiliser le processus de vente avec ordonnance.'], 403);
                return;
            }

            // Vérifier le stock
            $stockDisponible = $product->getStock();
            if ($stockDisponible <= 0) {
                $this->sendJsonResponse([
                    'success' => false, 
                    'message' => 'Produit en rupture de stock. Veuillez mettre à jour le catalogue.'
                ], 400);
                return;
            }
            
            if ($stockDisponible < $quantity) {
                $this->sendJsonResponse([
                    'success' => false, 
                    'message' => 'Stock insuffisant. Quantité disponible: ' . $stockDisponible . ' unité(s).'
                ], 400);
                return;
            }

            // Récupérer le client
            $client = Client::findById($clientId);
            if (!$client) {
                $this->sendJsonResponse(['success' => false, 'message' => 'Client non trouvé. L\'ID du client n\'existe pas dans la base de données.'], 404);
                return;
            }
            
            // Vérifier si le client appartient à la pharmacie
            $clientPharmacyId = $client->getPharmacieId();
            if ($clientPharmacyId && $clientPharmacyId != $pharmacyId) {
                $this->sendJsonResponse(['success' => false, 'message' => 'Ce client n\'appartient pas à votre pharmacie.'], 403);
                return;
            }

            // Vérifier que le prix du produit est valide
            $prix = $product->getPrix();
            if ($prix <= 0) {
                $this->sendJsonResponse(['success' => false, 'message' => 'Prix du produit invalide. Veuillez vérifier la configuration du produit.'], 400);
                return;
            }
            
            // Créer et enregistrer la vente
            $sale = new Sale(
                null,
                $productId,
                $clientId,
                $pharmacyId,
                $quantity,
                $prix
            );

            $result = $sale->save();
            
            if (!$result) {
                $this->sendJsonResponse(['success' => false, 'message' => 'Erreur lors de l\'enregistrement de la vente. Veuillez réessayer.'], 500);
                return;
            }

            // Le stock a déjà été mis à jour dans la méthode save() de Sale
            // Récupérer le nouveau stock pour l'affichage
            $newStock = $product->getStock() - $quantity;
            
            // Double vérification que le stock n'est pas négatif
            if ($newStock < 0) {
                $newStock = 0;
                error_log("Erreur: Stock négatif détecté pour le produit ID: {$productId}. Stock corrigé à 0.");
            }

            $this->sendJsonResponse([
                'success' => true,
                'message' => 'Vente effectuée avec succès',
                'sale' => [
                    'id' => $sale->getId(),
                    'product' => $product->getNom(),
                    'client' => $client->getNomComplet(),
                    'quantity' => $quantity,
                    'total' => $sale->getPrixTotal(),
                    'new_stock' => $newStock
                ]
            ]);

        } catch (\Exception $e) {
            $this->sendJsonResponse(['success' => false, 'message' => $e->getMessage()], 500);
        }
    }

    // Méthode pour obtenir la liste des clients (pour la popup)
    public function getClients() {
        // Vérifier si l'utilisateur est authentifié
        if (!isset($_SESSION['authenticated']) || $_SESSION['authenticated'] !== true) {
            $this->sendJsonResponse(['success' => false, 'message' => 'Vous devez être connecté pour accéder à la liste des clients.'], 401);
            return;
        }
        
        // Récupérer et nettoyer le terme de recherche
        $search = isset($_GET['search']) ? SecurityService::sanitizeInput($_GET['search']) : '';
        
        // Récupérer l'ID de la pharmacie depuis la session ou utiliser une valeur par défaut
        $pharmacyId = isset($_SESSION['pharmacy_id']) ? intval($_SESSION['pharmacy_id']) : null;
        
        // Si l'ID de la pharmacie n'est pas disponible, essayer de le récupérer à partir de l'utilisateur connecté
        if (!$pharmacyId && isset($_SESSION['user_id'])) {
            $user = \Models\User::findById($_SESSION['user_id']);
            if ($user) {
                $pharmacyId = $user->getPharmacyId();
            }
        }
        
        // Si toujours pas d'ID de pharmacie, utiliser une valeur par défaut pour le développement
        if (!$pharmacyId) {
            $pharmacyId = 1; // Valeur par défaut pour le développement
            error_log("Attention: Utilisation d'un ID de pharmacie par défaut pour la liste des clients. Utilisateur non associé à une pharmacie.");
        }

        try {
            // Vérifier si la recherche contient des caractères spéciaux ou des injections SQL
            if (!empty($search) && !preg_match('/^[a-zA-Z0-9\s\-\.\@]+$/', $search)) {
                $this->sendJsonResponse(['success' => false, 'message' => 'Terme de recherche invalide. Utilisez uniquement des lettres, chiffres, espaces et caractères spéciaux autorisés.'], 400);
                return;
            }
            
            // Limiter la longueur de la recherche
            if (strlen($search) > 50) {
                $search = substr($search, 0, 50);
            }
            
            // Récupérer les clients selon le terme de recherche
            if (!empty($search)) {
                $clients = Client::findByName($search);
            } else {
                $clients = Client::getAll($pharmacyId);
            }
            
            // Vérifier si des clients ont été trouvés
            if (empty($clients)) {
                $this->sendJsonResponse([
                    'success' => true,
                    'clients' => [],
                    'message' => !empty($search) ? 'Aucun client trouvé pour cette recherche.' : 'Aucun client enregistré dans cette pharmacie.'
                ]);
                return;
            }

            $clientsList = [];
            foreach ($clients as $client) {
                $clientsList[] = [
                    'id' => $client->getId(),
                    'nom' => $client->getNom(),
                    'prenom' => $client->getPrenom(),
                    'nom_complet' => $client->getNomComplet(),
                    'email' => $client->getEmail(),
                    'telephone' => $client->getTelephone()
                ];
            }

            $this->sendJsonResponse([
                'success' => true,
                'clients' => $clientsList
            ]);

        } catch (\Exception $e) {
            $this->sendJsonResponse(['success' => false, 'message' => $e->getMessage()], 500);
        }
    }

    // Méthode pour envoyer une réponse JSON
    private function sendJsonResponse($data, $statusCode = 200) {
        http_response_code($statusCode);
        header('Content-Type: application/json');
        echo json_encode($data);
        exit;
    }
}
