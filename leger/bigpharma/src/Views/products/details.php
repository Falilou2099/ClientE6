<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <title><?= htmlspecialchars($product->getName()) ?> - BigPharma</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.2.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <style>
        body { background-color: #f4f6f9; }
        .product-detail-container {
            background-color: white;
            border-radius: 10px;
            box-shadow: 0 4px 6px rgba(0,0,0,0.1);
            padding: 30px;
        }
        .product-image {
            max-width: 100%;
            height: auto;
            border-radius: 10px;
        }
        .stock-status {
            display: inline-block;
            padding: 5px 10px;
            border-radius: 5px;
        }
        .stock-high { background-color: #d4edda; color: #155724; }
        .stock-low { background-color: #fff3cd; color: #856404; }
        .stock-out { background-color: #f8d7da; color: #721c24; }
    </style>
</head>
<body>
    <?php include __DIR__ . '/../../templates/header.php'; ?>

    <div class="container mt-4">
        <div class="row product-detail-container">
            <div class="col-md-5">
                <img 
                    src="<?= htmlspecialchars($product->getImageUrl() ?? '/assets/img/default-product.png') ?>" 
                    alt="<?= htmlspecialchars($product->getName()) ?>" 
                    class="product-image"
                >
            </div>
            <div class="col-md-7">
                <h1 class="mb-3"><?= htmlspecialchars($product->getName()) ?></h1>
                
                <?php if($product->isPrescription()): ?>
                    <div class="alert alert-warning" role="alert">
                        <i class="bi bi-prescription"></i> Médicament soumis à prescription médicale
                    </div>
                <?php endif; ?>

                <div class="mb-3">
                    <strong>Catégorie :</strong> 
                    <?= htmlspecialchars($product->getCategory()) ?>
                </div>

                <div class="mb-3">
                    <strong>Laboratoire :</strong> 
                    <?= htmlspecialchars($product->getManufacturer()) ?>
                </div>

                <div class="mb-3">
                    <strong>Dosage :</strong> 
                    <?= htmlspecialchars($product->getDosage()) ?>
                </div>

                <div class="mb-3">
                    <strong>Description :</strong>
                    <?= htmlspecialchars($product->getDescription()) ?>
                </div>

                <div class="mb-3">
                    <strong>Stock :</strong>
                    <?php 
                    $stockStatus = $product->getStatus();
                    $stockClass = match($stockStatus) {
                        'available' => 'stock-high',
                        'low_stock' => 'stock-low',
                        'out_of_stock' => 'stock-out',
                        default => ''
                    };
                    ?>
                    <span class="stock-status <?= $stockClass ?>">
                        <?php 
                        echo match($stockStatus) {
                            'available' => 'Disponible',
                            'low_stock' => 'Stock faible',
+                            'out_of_stock' => 'Rupture de stock',
                            default => 'Statut inconnu'
                        };
                        ?> 
                        (<?= $product->getStock() ?> unités)
                    </span>
                </div>

                <div class="mb-3">
                    <h2 class="text-primary"><?= number_format($product->getPrice(), 2) ?> €</h2>
                </div>

                <div class="mb-3">
                    <?php if($product->getStock() > 0 && !$product->isPrescription()): ?>
                        <button 
                            class="btn btn-primary btn-lg add-to-cart" 
                            data-product-id="<?= $product->getId() ?>"
                        >
                            Ajouter au panier
                        </button>
                    <?php elseif($product->isPrescription()): ?>
                        <div class="alert alert-info">
                            Ce médicament nécessite une ordonnance. Veuillez consulter votre médecin.
                        </div>
                    <?php else: ?>
                        <div class="alert alert-danger">
                            Produit momentanément indisponible
                        </div>
                    <?php endif; ?>
                </div>

                <?php if($similarProducts): ?>
                    <div class="mt-4">
                        <h3>Produits similaires</h3>
                        <div class="row">
                            <?php foreach($similarProducts as $similar): ?>
                                <div class="col-md-4">
                                    <div class="card mb-3">
                                        <div class="card-body">
                                            <h5 class="card-title"><?= htmlspecialchars($similar->getName()) ?></h5>
                                            <p class="card-text"><?= htmlspecialchars($similar->getCategory()) ?></p>
                                            <a href="/product/<?= $similar->getId() ?>" class="btn btn-sm btn-outline-primary">Voir</a>
                                        </div>
                                    </div>
                                </div>
                            <?php endforeach; ?>
                        </div>
                    </div>
                <?php endif; ?>
            </div>
        </div>
    </div>

    <?php include __DIR__ . '/../../templates/footer.php'; ?>

    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.2.3/dist/js/bootstrap.bundle.min.js"></script>
    <script>
        document.addEventListener('DOMContentLoaded', function() {
            const addToCartButtons = document.querySelectorAll('.add-to-cart');
            
            addToCartButtons.forEach(button => {
                button.addEventListener('click', function() {
                    const productId = this.getAttribute('data-product-id');
                    
                    // TODO: Implémenter la logique d'ajout au panier
                    alert(`Produit ${productId} ajouté au panier`);
                });
            });
        });
    </script>
</body>
</html>
