<?php
namespace Controllers;

use Models\Product;
use Config\SecurityService;

class ProductController extends BaseController {
    private $productModel;

    public function __construct() {
        parent::__construct();
        // Initialisation sans injection de dépendance
        $this->productModel = new Product();
    }

    // Rechercher des produits
    public function searchProducts($criteria) {
        // Critères de recherche possibles :
        // - nom
        // - catégorie
        // - dosage
        // - fabricant
        // - disponibilité
        // - nécessité d'ordonnance

        $searchResults = [];

        // Nettoyer et valider les critères de recherche
        $sanitizedCriteria = [];
        foreach ($criteria as $key => $value) {
            switch ($key) {
                case 'name':
                    $sanitizedCriteria['name'] = SecurityService::sanitizeInput($value);
                    break;
                case 'category':
                    $sanitizedCriteria['category'] = SecurityService::sanitizeInput($value);
                    break;
                case 'manufacturer':
                    $sanitizedCriteria['manufacturer'] = SecurityService::sanitizeInput($value);
                    break;
                case 'minPrice':
                    $sanitizedCriteria['minPrice'] = floatval($value);
                    break;
                case 'maxPrice':
                    $sanitizedCriteria['maxPrice'] = floatval($value);
                    break;
                case 'prescriptionRequired':
                    $sanitizedCriteria['prescriptionRequired'] = (bool)$value;
                    break;
            }
        }

        // Simulation de recherche (sans base de données)
        // TODO: Implémenter la logique de recherche réelle
        $products = $this->productModel->getAllProducts();

        foreach ($products as $product) {
            $match = true;

            // Filtrer par nom
            if (isset($sanitizedCriteria['name']) && 
                stripos($product->getName(), $sanitizedCriteria['name']) === false) {
                $match = false;
            }

            // Filtrer par catégorie
            if ($match && isset($sanitizedCriteria['category']) && 
                $product->getCategory() !== $sanitizedCriteria['category']) {
                $match = false;
            }

            // Filtrer par fabricant
            if ($match && isset($sanitizedCriteria['manufacturer']) && 
                $product->getManufacturer() !== $sanitizedCriteria['manufacturer']) {
                $match = false;
            }

            // Filtrer par prix minimum
            if ($match && isset($sanitizedCriteria['minPrice']) && 
                $product->getPrice() < $sanitizedCriteria['minPrice']) {
                $match = false;
            }

            // Filtrer par prix maximum
            if ($match && isset($sanitizedCriteria['maxPrice']) && 
                $product->getPrice() > $sanitizedCriteria['maxPrice']) {
                $match = false;
            }

            // Filtrer par nécessité d'ordonnance
            if ($match && isset($sanitizedCriteria['prescriptionRequired']) && 
                $product->isPrescrition() !== $sanitizedCriteria['prescriptionRequired']) {
                $match = false;
            }

            if ($match) {
                $searchResults[] = $product;
            }
        }

        return [
            'success' => true,
            'products' => $searchResults,
            'total' => count($searchResults)
        ];
    }

    // Obtenir les détails d'un produit
    public function getProductDetails($productId) {
        // Simulation de récupération de produit (sans base de données)
        // TODO: Implémenter la logique de récupération réelle
        $product = $this->productModel->getProductById($productId);

        return [
            'success' => true,
            'product' => [
                'id' => $product->getId(),
                'name' => $product->getName(),
                'description' => $product->getDescription(),
                'category' => $product->getCategory(),
                'dosage' => $product->getDosage(),
                'manufacturer' => $product->getManufacturer(),
                'price' => $product->getPrice(),
                'stock' => $product->getStock(),
                'status' => $product->getStatus(),
                'prescriptionRequired' => $product->isPrescrition()
            ]
        ];
    }

    // Vérifier la disponibilité d'un produit
    public function checkProductAvailability($productId) {
        // Simulation de vérification de disponibilité (sans base de données)
        // TODO: Implémenter la logique de vérification réelle
        $product = $this->productModel->getProductById($productId);

        return [
            'success' => true,
            'available' => $product->getStock() > 0,
            'stock' => $product->getStock(),
            'status' => $product->getStatus()
        ];
    }

    // Obtenir les produits nécessitant une ordonnance
    public function getPrescriptionProducts() {
        // Simulation de récupération de produits (sans base de données)
        // TODO: Implémenter la logique de récupération réelle
        $products = $this->productModel->getPrescriptionProducts();

        return [
            'success' => true,
            'products' => $products,
            'total' => count($products)
        ];
    }

    // Obtenir les produits à faible stock
    public function getLowStockProducts() {
        // Simulation de récupération de produits à faible stock (sans base de données)
        // TODO: Implémenter la logique de récupération réelle
        $products = $this->productModel->getLowStockProducts();

        return [
            'success' => true,
            'products' => $products,
            'total' => count($products)
        ];
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
        
        // Vérifier s'il y a un filtre appliqué
        $filter = $_GET['filter'] ?? null;
        
        // Récupérer les produits selon le filtre et la pharmacie
        if ($filter === 'prescription') {
            // Produits sur ordonnance
            $products = Product::search([
                'prescription' => true,
                'pharmacy_id' => $pharmacy_id
            ]);
            $filterTitle = 'Produits sur Ordonnance';
        } elseif ($filter === 'low_stock') {
            // Produits à stock limité
            $products = Product::getLowStockProducts(10, $pharmacy_id);
            $filterTitle = 'Produits à Stock Limité';
        } else {
            // Tous les produits
            $products = Product::getAll($pharmacy_id);
            $filterTitle = 'Tous les Produits';
        }

        // Préparer les données pour la vue
        $data = [
            'products' => $products,
            'filterTitle' => $filterTitle,
            'hasProducts' => $hasProducts,
            'pharmacyName' => $pharmacyName
        ];

        // Charger la vue des produits avec les produits récupérés
        include ROOT_PATH . '/templates/header.php';
        include ROOT_PATH . '/src/Views/products/catalog.php';
        include ROOT_PATH . '/templates/footer.php';
    }

    public function detail() {
        // Récupérer la pharmacie de l'utilisateur connecté
        $pharmacy_id = null;
        if (isset($_SESSION['user_id'])) {
            $user = \Models\User::findById($_SESSION['user_id']);
            if ($user) {
                $pharmacy_id = $user->getPharmacyId();
            }
        }
        
        $productId = $_GET['id'] ?? null;
        if (!$productId) {
            header('Location: /bigpharma/public/products');
            exit();
        }
        
        // Récupérer le produit en vérifiant qu'il appartient à la pharmacie de l'utilisateur
        $product = Product::getById($productId, $pharmacy_id);
        
        // Si le produit n'existe pas ou n'appartient pas à la pharmacie de l'utilisateur
        if (!$product) {
            // Rediriger vers la liste des produits avec un message d'erreur
            header('Location: /bigpharma/public/products?error=product_not_found');
            exit();
        }
        
        // Récupérer des produits similaires (même catégorie) de la même pharmacie
        $similarProducts = Product::getByCategory($product->getCategorie(), 4, $pharmacy_id);
        
        // Charger la vue de détail du produit
        include ROOT_PATH . '/templates/header.php';
        include ROOT_PATH . '/src/Views/product_detail.php';
        include ROOT_PATH . '/templates/footer.php';
    }

    public function restock() {
        // Vérifier si la requête est une requête POST
        if ($_SERVER['REQUEST_METHOD'] === 'POST') {
            $productId = $_POST['product_id'] ?? null;
            $quantity = $_POST['quantity'] ?? 0;

            // Récupérer les informations du produit
            $product = $this->productModel->getProductById($productId);

            if (!$product) {
                // Gérer l'erreur si le produit n'existe pas
                $_SESSION['error'] = "Produit non trouvé.";
                header('Location: /bigpharma/public/products');
                exit;
            }

            // Vérifier la quantité maximale autorisée
            $maxQuantity = 100 - $product['quantite_stock']; // Exemple de limite maximale

            if ($quantity > $maxQuantity) {
                $_SESSION['error'] = "La quantité demandée dépasse la limite maximale possible.";
                header('Location: /bigpharma/public/products');
                exit;
            }

            // Mettre à jour le stock
            $result = $this->productModel->updateStock($productId, $quantity);

            if ($result) {
                $_SESSION['success'] = "Stock mis à jour avec succès.";
            } else {
                $_SESSION['error'] = "Erreur lors de la mise à jour du stock.";
            }

            header('Location: /products');
            exit;
        }
    }
}
