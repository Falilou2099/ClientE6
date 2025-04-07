<?php
namespace Controllers;

use Models\Product;
use Config\SecurityService;

class ProductController {
    private $productModel;

    public function __construct() {
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
        // Récupérer tous les produits
        $products = Product::getAll();

        // Charger la vue des produits avec les produits récupérés
        include ROOT_PATH . '/templates/header.php';
        include ROOT_PATH . '/src/Views/products/catalog.php';
        include ROOT_PATH . '/templates/footer.php';
    }

    public function detail() {
        $productId = $_GET['id'] ?? null;
        if (!$productId) {
            header('Location: /bigpharma/products');
            exit();
        }
        
        $product = Product::getById($productId);
        
        if (!$product) {
            // Produit non trouvé, rediriger vers la liste des produits avec un message d'erreur
            header('Location: /bigpharma/products?error=product_not_found');
            exit();
        }
        
        // Récupérer des produits similaires (même catégorie)
        $similarProducts = Product::getByCategory($product->getCategorie(), 4);
        
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
                header('Location: /products');
                exit;
            }

            // Vérifier la quantité maximale autorisée
            $maxQuantity = 100 - $product['quantite_stock']; // Exemple de limite maximale

            if ($quantity > $maxQuantity) {
                $_SESSION['error'] = "La quantité demandée dépasse la limite maximale possible.";
                header('Location: /products');
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
