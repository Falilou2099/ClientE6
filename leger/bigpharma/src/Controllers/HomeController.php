<?php
namespace Controllers;

use Models\Product;
use Models\Order;

class HomeController {
    public function __construct() {
    }

    public function index() {
        // Récupérer les statistiques de produits
        $totalProducts = Product::getTotalProducts();
        $lowStockProducts = Product::getLowStockProducts(10); 
        $prescriptionProducts = Product::getPrescriptionProducts();
        
        // Récupérer les catégories de produits
        $categories = Product::getAllCategories();
        
        // Récupérer quelques produits en vedette (les 4 premiers de la base de données)
        $featuredProducts = Product::getAll();
        $featuredProducts = array_slice($featuredProducts, 0, 4); // Limiter à 4 produits

        // Préparer les données pour la vue
        $data = [
            'totalProducts' => $totalProducts,
            'lowStockProducts' => $lowStockProducts,
            'prescriptionProducts' => $prescriptionProducts,
            'categories' => $categories,
            'featuredProducts' => $featuredProducts
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
        
        // Utiliser le modèle Product pour effectuer la recherche
        $results = Product::findByName($query);
        
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
        $product = Product::findById($id);
        
        if (!$product) {
            // Produit non trouvé, rediriger vers la page d'accueil
            header('Location: /bigpharma/');
            exit;
        }
        
        // Récupérer des produits similaires (même catégorie)
        $similarProducts = Product::getByCategory($product->getCategorie());
        
        // Filtrer pour exclure le produit actuel et limiter à 3 produits
        $similarProducts = array_filter($similarProducts, function($item) use ($product) {
            return $item->getId() != $product->getId();
        });
        $similarProducts = array_slice($similarProducts, 0, 3);
        
        // Charger la vue du produit
        require_once SRC_PATH . '/Views/product_detail.php';
    }
}
