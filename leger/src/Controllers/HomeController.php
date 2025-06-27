<?php
namespace Controllers;

use Models\Product;
use Models\Order;

class HomeController extends BaseController {
    public function __construct() {
        parent::__construct();
    }

    public function index() {
        // Vérifier si l'utilisateur est connecté et récupérer sa pharmacie
        $pharmacy_id = null;
        $hasProducts = true;
        $pharmacyName = 'Pharmacie';
        
        if (isset($_SESSION['user_id'])) {
            // Récupérer l'utilisateur et sa pharmacie
            $user = \Models\User::findById($_SESSION['user_id']);
            if ($user) {
                $pharmacy_id = $user->getPharmacyId();
                $pharmacyName = $user->getPharmacyName() ?: 'Pharmacie';
                
                // Vérifier si la pharmacie a des produits
                $hasProducts = Product::hasProducts($pharmacy_id);
            }
        }
        
        // Si la pharmacie n'a pas de produits, initialiser les statistiques à zéro ou vide
        if (!$hasProducts) {
            $totalProducts = 0;
            $lowStockProducts = [];
            $prescriptionProducts = [];
            $categories = [];
            $featuredProducts = [];
        } else {
            // Récupérer les statistiques de produits filtrées par pharmacie
            $totalProducts = Product::getTotalProducts($pharmacy_id);
            
            // Récupérer les produits à stock limité filtrés par pharmacie
            $lowStockProducts = Product::getLowStockProducts(10, $pharmacy_id);
            
            // Récupérer les produits sous ordonnance filtrés par pharmacie
            $prescriptionProducts = Product::getPrescriptionProducts($pharmacy_id);
            
            // Récupérer les catégories de produits de la pharmacie
            $categories = Product::getAllCategories($pharmacy_id);
            
            // Récupérer quelques produits en vedette filtrés par pharmacie
            $featuredProducts = Product::getAll($pharmacy_id);
            $featuredProducts = array_slice($featuredProducts, 0, 4); // Limiter à 4 produits
        }

        // Préparer les données pour la vue
        $data = [
            'totalProducts' => $totalProducts,
            'lowStockProducts' => $lowStockProducts,
            'prescriptionProducts' => $prescriptionProducts,
            'categories' => $categories,
            'featuredProducts' => $featuredProducts,
            'hasProducts' => $hasProducts,
            'pharmacyName' => $pharmacyName
        ];

        // Charger la vue de la page d'accueil
        require_once __DIR__ . '/../Views/home.php';
    }

    // Méthode pour la recherche rapide
    public function quickSearch() {
        $query = $_GET['q'] ?? '';
        
        if (empty($query)) {
            return [];
        }
        
        // Récupérer la pharmacie de l'utilisateur connecté
        $pharmacy_id = null;
        if (isset($_SESSION['user_id'])) {
            $user = \Models\User::findById($_SESSION['user_id']);
            if ($user) {
                $pharmacy_id = $user->getPharmacyId();
            }
        }
        
        // Utiliser le modèle Product pour effectuer la recherche filtrée par pharmacie
        $results = Product::search([
            'name' => $query,
            'pharmacy_id' => $pharmacy_id
        ]);
        
        // Formater les résultats pour l'autocomplétion
        $formattedResults = [];
        foreach ($results as $product) {
            $formattedResults[] = [
                'id' => $product->getId(),
                'name' => $product->getNom(),
                'price' => $product->getPrix(),
                'category' => $product->getCategorie()
            ];
        }
        
        // Retourner les résultats au format JSON
        header('Content-Type: application/json');
        echo json_encode($formattedResults);
        exit;
    }
    
    // Méthode pour afficher la page d'un produit
    public function showProduct($id) {
        // Récupérer la pharmacie de l'utilisateur connecté
        $pharmacy_id = null;
        if (isset($_SESSION['user_id'])) {
            $user = \Models\User::findById($_SESSION['user_id']);
            if ($user) {
                $pharmacy_id = $user->getPharmacyId();
            }
        }
        
        $product = Product::findById($id);
        
        // Vérifier si le produit existe et appartient à la pharmacie de l'utilisateur
        if (!$product || ($pharmacy_id && $product->getPharmacyId() != $pharmacy_id)) {
            // Produit non trouvé ou n'appartient pas à la pharmacie de l'utilisateur, rediriger vers la page d'accueil
            header('Location: /bigpharma/');
            exit;
        }
        
        // Récupérer des produits similaires (même catégorie) de la même pharmacie
        $similarProducts = Product::getByCategory($product->getCategorie(), null, $pharmacy_id);
        
        // Filtrer pour exclure le produit actuel et limiter à 3 produits
        $similarProducts = array_filter($similarProducts, function($item) use ($product) {
            return $item->getId() != $product->getId();
        });
        $similarProducts = array_slice($similarProducts, 0, 3);
        
        // Charger la vue du produit
        require_once SRC_PATH . '/Views/product_detail.php';
    }
}
