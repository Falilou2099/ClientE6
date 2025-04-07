<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <title>BigPharma - Tableau de Bord</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.2.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0/css/all.min.css" rel="stylesheet">
    <style>
        body { background-color: #f4f6f9; }
        .dashboard-card {
            transition: transform 0.3s ease;
            box-shadow: 0 4px 6px rgba(0,0,0,0.1);
        }
        .dashboard-card:hover {
            transform: scale(1.03);
        }
        .featured-product {
            background-color: white;
            border-radius: 10px;
            padding: 15px;
            margin-bottom: 15px;
        }
        .product-card {
            transition: transform 0.3s ease;
            box-shadow: 0 4px 6px rgba(0,0,0,0.1);
            height: 100%;
        }
        .product-card:hover {
            transform: scale(1.03);
        }
        .badge-prescription {
            position: absolute;
            top: 10px;
            right: 10px;
        }
    </style>
</head>
<body>
    <?php include __DIR__ . '/../../templates/header.php'; ?>

    <div class="container mt-4">
        <h1 class="mb-4">Bienvenue sur BigPharma</h1>

        <div class="row">
            <div class="col-md-4 mb-4">
                <div class="card dashboard-card">
                    <div class="card-body text-center">
                        <h5 class="card-title">Produits Totaux</h5>
                        <div class="h2 text-primary"><?= $totalProducts ?></div>
                        <p class="text-muted">Nombre de médicaments en stock</p>
                    </div>
                </div>
            </div>

            <div class="col-md-4 mb-4">
                <div class="card dashboard-card">
                    <div class="card-body text-center">
                        <h5 class="card-title">Stock Limité</h5>
                        <div class="h2 text-warning"><?= count($lowStockProducts) ?></div>
                        <p class="text-muted">Produits nécessitant réapprovisionnement</p>
                    </div>
                </div>
            </div>

            <div class="col-md-4 mb-4">
                <div class="card dashboard-card">
                    <div class="card-body text-center">
                        <h5 class="card-title">Produits sur Ordonnance</h5>
                        <div class="h2 text-danger"><?= count($prescriptionProducts) ?></div>
                        <p class="text-muted">Médicaments réglementés</p>
                    </div>
                </div>
            </div>
        </div>

        <!-- Liste des produits disponibles en stock -->
        <h3 class="mb-3 mt-4">Produits disponibles en stock</h3>
        <div class="row" id="productsList">
            <?php foreach ($featuredProducts as $product): ?>
                <div class="col-md-4 mb-4 product-item">
                    <div class="card product-card position-relative h-100">
                        <?php if($product->getEstOrdonnance()): ?>
                            <span class="badge bg-danger badge-prescription">Ordonnance</span>
                        <?php endif; ?>
                        
                        <img src="<?= $product->getImageUrl() ?>" 
                             class="card-img-top" alt="<?= htmlspecialchars($product->getNom()) ?>"
                             style="height: 200px; object-fit: cover;">
                        
                        <div class="card-body d-flex flex-column">
                            <h5 class="card-title product-name"><?= htmlspecialchars($product->getNom()) ?></h5>
                            <p class="card-text product-description flex-grow-1">
                                <?= htmlspecialchars($product->getDescription()) ?><br>
                                <strong>Catégorie :</strong> <?= htmlspecialchars($product->getCategorie()) ?>
                            </p>
                            
                            <div class="d-flex justify-content-between align-items-center mt-auto">
                                <span class="h5 mb-0 product-price"><?= number_format($product->getPrix(), 2) ?> €</span>
                                
                                <?php if($product->getStock() > 0): ?>
                                    <span class="badge bg-success product-stock">En stock: <?= $product->getStock() ?></span>
                                <?php else: ?>
                                    <span class="badge bg-danger product-stock">Rupture de stock</span>
                                <?php endif; ?>
                            </div>
                            
                            <div class="mt-3 d-flex justify-content-between">
                                <a href="/bigpharma/products/detail?id=<?= $product->getId() ?>" class="btn btn-outline-primary">
                                    Détails
                                </a>
                                <?php if($product->getStock() > 0 && !$product->getEstOrdonnance()): ?>
                                    <button class="btn btn-primary add-to-cart" data-product-id="<?= $product->getId() ?>">
                                        <i class="fas fa-shopping-cart"></i>
                                    </button>
                                <?php else: ?>
                                    <button class="btn btn-secondary" disabled>
                                        <?= $product->getEstOrdonnance() ? 'Ordonnance' : 'Rupture' ?>
                                    </button>
                                <?php endif; ?>
                            </div>
                        </div>
                    </div>
                </div>
            <?php endforeach; ?>
        </div>

        <div class="row mt-4">
            <div class="col-md-8">
                <h3 class="mb-3">Produits à Stock Limité</h3>
                <?php if (!empty($lowStockProducts)): ?>
                    <?php foreach ($lowStockProducts as $product): ?>
                        <div class="featured-product d-flex justify-content-between align-items-center">
                            <div>
                                <h5 class="mb-1"><?= htmlspecialchars($product->getNom()) ?></h5>
                                <p class="text-muted mb-0">
                                    <?= htmlspecialchars($product->getCategorie()) ?> 
                                </p>
                            </div>
                            <div>
                                <span class="badge bg-primary"><?= number_format($product->getPrix(), 2) ?> €</span>
                                <span class="badge bg-warning ms-2">Stock: <?= $product->getStock() ?></span>
                            </div>
                        </div>
                    <?php endforeach; ?>
                <?php else: ?>
                    <div class="alert alert-success">Tous les produits sont bien approvisionnés</div>
                <?php endif; ?>
            </div>

            <div class="col-md-4">
                <h3 class="mb-3">Produits sur Ordonnance</h3>
                <?php if (!empty($prescriptionProducts)): ?>
                    <?php foreach ($prescriptionProducts as $product): ?>
                        <div class="featured-product d-flex justify-content-between align-items-center">
                            <div>
                                <h5 class="mb-1"><?= htmlspecialchars($product->getNom()) ?></h5>
                                <p class="text-muted mb-0">
                                    <?= htmlspecialchars($product->getCategorie()) ?> 
                                </p>
                            </div>
                            <div>
                                <span class="badge bg-danger">Ordonnance</span>
                            </div>
                        </div>
                    <?php endforeach; ?>
                <?php else: ?>
                    <div class="alert alert-info">Aucun produit sur ordonnance</div>
                <?php endif; ?>
            </div>
        </div>
    </div>

    <?php include __DIR__ . '/../../templates/footer.php'; ?>

    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.2.3/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
