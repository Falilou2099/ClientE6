<?php
namespace Controllers;

use Models\Product;

class SearchController extends BaseController {
    
    public function __construct() {
        parent::__construct();
        // Pas besoin d'initialiser le modèle Product ici
    }

    public function index($query = null) {
        // Si aucune requête n'est fournie, utiliser la requête GET
        if ($query === null) {
            $query = $_GET['query'] ?? '';
        }
        
        // Récupérer les filtres supplémentaires
        $category = $_GET['category'] ?? '';
        $minPrice = isset($_GET['min_price']) && is_numeric($_GET['min_price']) ? floatval($_GET['min_price']) : null;
        $maxPrice = isset($_GET['max_price']) && is_numeric($_GET['max_price']) ? floatval($_GET['max_price']) : null;
        $inStock = isset($_GET['in_stock']) ? (bool)$_GET['in_stock'] : false;
        $prescription = isset($_GET['prescription']) ? (bool)$_GET['prescription'] : null;
        $sort = $_GET['sort'] ?? '';
        
        // Effectuer la recherche de produits
        $results = $this->searchProducts($query, $category, $minPrice, $maxPrice, $inStock, $prescription, $sort);
        
        // Récupérer toutes les catégories pour le filtre
        $categories = Product::getAllCategories();
        
        // Charger la vue de résultats de recherche
        require_once __DIR__ . '/../Views/search_results.php';
    }

    public function searchProducts($query, $category = '', $minPrice = null, $maxPrice = null, $inStock = false, $prescription = null, $sort = '') {
        // Préparer les critères de recherche
        $criteria = [
            'name' => $query
        ];
        
        // Ajouter les filtres supplémentaires
        if (!empty($category)) {
            $criteria['category'] = $category;
        }
        
        if ($minPrice !== null) {
            $criteria['min_price'] = $minPrice;
        }
        
        if ($maxPrice !== null) {
            $criteria['max_price'] = $maxPrice;
        }
        
        if ($inStock) {
            $criteria['in_stock'] = true;
        }
        
        if ($prescription !== null) {
            $criteria['prescription'] = $prescription;
        }
        
        if (!empty($sort)) {
            $criteria['sort'] = $sort;
        }
        
        // Utiliser la méthode de recherche du modèle Product
        return Product::search($criteria);
    }

    // Méthode pour filtrer les produits par catégorie
    public function filterByCategory($category) {
        return Product::getByCategory($category);
    }
    
    // Méthode pour obtenir les produits à faible stock
    public function getLowStockProducts($threshold = 10) {
        return Product::getLowStockProducts($threshold);
    }
    
    // Méthode pour obtenir tous les produits
    public function getAllProducts() {
        return Product::getAll();
    }
}
