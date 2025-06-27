<?php
namespace Controllers;

use Models\Product;
use Models\Client;
use Models\Sale;
use Models\Pharmacy;
use Config\SecurityService;

class ApiController extends BaseController {
    private $productModel;
    private $clientModel;
    private $saleModel;

    public function __construct() {
        parent::__construct();
        $this->productModel = new Product();
        $this->clientModel = new Client();
    }

    /**
     * Méthode pour traiter la vente d'un produit
     * Cette méthode est appelée par l'API /api/sale/process
     */
    public function processSale() {
        // Vérifier la méthode HTTP
        if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
            $this->sendJsonResponse(['success' => false, 'message' => 'Méthode non autorisée. Seules les requêtes POST sont acceptées.'], 405);
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
        
        // Utiliser une valeur par défaut pour l'ID de la pharmacie
        $pharmacyId = 1; // Valeur par défaut pour le développement
        
        // Validation des données
        if ($productId <= 0) {
            $this->sendJsonResponse(['success' => false, 'message' => 'ID de produit invalide.'], 400);
            return;
        }

        if ($clientId <= 0) {
            $this->sendJsonResponse(['success' => false, 'message' => 'ID de client invalide.'], 400);
            return;
        }

        if ($quantity <= 0) {
            $this->sendJsonResponse(['success' => false, 'message' => 'Quantité invalide.'], 400);
            return;
        }
        
        if ($quantity > 100) { // Limite arbitraire pour éviter les erreurs
            $this->sendJsonResponse(['success' => false, 'message' => 'Quantité trop élevée.'], 400);
            return;
        }

        try {
            // Récupérer le produit
            $product = Product::getById($productId);
            if (!$product) {
                $this->sendJsonResponse(['success' => false, 'message' => 'Produit non trouvé.'], 404);
                return;
            }
            
            // Vérifier si le produit nécessite une ordonnance
            if ($product->getEstOrdonnance()) {
                $this->sendJsonResponse(['success' => false, 'message' => 'Ce produit nécessite une ordonnance.'], 400);
                return;
            }
            
            // Vérifier le stock
            if ($product->getStock() < $quantity) {
                $this->sendJsonResponse(['success' => false, 'message' => 'Stock insuffisant.'], 400);
                return;
            }
            
            // Récupérer le client
            $client = Client::findById($clientId);
            if (!$client) {
                $this->sendJsonResponse(['success' => false, 'message' => 'Client non trouvé.'], 404);
                return;
            }
            
            // Créer la vente
            $sale = new Sale(
                null,
                $productId,
                $clientId,
                $pharmacyId,
                $quantity,
                $product->getPrix(),
                $product->getPrix() * $quantity
            );
            
            // Sauvegarder la vente
            $saleId = $sale->save();
            
            if (!$saleId) {
                $this->sendJsonResponse(['success' => false, 'message' => 'Erreur lors de l\'enregistrement de la vente.'], 500);
                return;
            }
            
            // Mettre à jour le stock du produit
            $newStock = $product->getStock() - $quantity;
            $product->setStock($newStock);
            $product->save();
            
            // Retourner la réponse
            $this->sendJsonResponse([
                'success' => true, 
                'message' => 'Vente effectuée avec succès.',
                'sale' => [
                    'id' => $saleId,
                    'product' => $product->getNom(),
                    'client' => $client->getNomComplet(),
                    'quantity' => $quantity,
                    'total' => $sale->getPrixTotal(),
                    'new_stock' => $newStock
                ]
            ]);

        } catch (\Exception $e) {
            // Log l'erreur pour le débogage
            error_log("Erreur API processSale: " . $e->getMessage());
            $this->sendJsonResponse(['success' => false, 'message' => 'Erreur lors du traitement de la vente: ' . $e->getMessage()], 500);
        }
    }

    /**
     * Méthode pour obtenir la liste des clients (pour la popup)
     * Cette méthode est appelée par l'API /api/clients
     */
    public function getClients() {
        // Récupérer et nettoyer le terme de recherche
        $search = isset($_GET['search']) ? SecurityService::sanitizeInput($_GET['search']) : '';
        
        // Récupérer l'ID de la pharmacie depuis la session ou utiliser une valeur par défaut
        $pharmacyId = isset($_SESSION['pharmacy_id']) ? intval($_SESSION['pharmacy_id']) : 1; // Valeur par défaut pour le développement
        
        try {
            // Vérifier si la recherche contient des caractères spéciaux ou des injections SQL
            if (!empty($search) && !preg_match('/^[a-zA-Z0-9\s\-\.\@]+$/', $search)) {
                $this->sendJsonResponse(['success' => false, 'message' => 'Terme de recherche invalide.'], 400);
                return;
            }
            
            // Limiter la longueur de la recherche
            if (strlen($search) > 50) {
                $search = substr($search, 0, 50);
            }
            
            // Récupérer les clients selon le terme de recherche
            if (!empty($search)) {
                $clients = Client::findByName($search, $pharmacyId);
            } else {
                $clients = Client::getAll($pharmacyId);
            }
            
            // Vérifier si des clients ont été trouvés
            if (empty($clients)) {
                $this->sendJsonResponse([
                    'success' => true,
                    'clients' => [],
                    'message' => !empty($search) ? 'Aucun client trouvé pour cette recherche.' : 'Aucun client enregistré.'
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
            // Log l'erreur pour le débogage
            error_log("Erreur API getClients: " . $e->getMessage());
            $this->sendJsonResponse(['success' => false, 'message' => 'Erreur lors de la récupération des clients: ' . $e->getMessage()], 500);
        }
    }

    /**
     * Méthode pour envoyer une réponse JSON
     */
    private function sendJsonResponse($data, $statusCode = 200) {
        http_response_code($statusCode);
        header('Content-Type: application/json');
        echo json_encode($data);
        exit;
    }
}
