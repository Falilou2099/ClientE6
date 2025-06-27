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
    <?php require_once 'C:\xampp\htdocs\bigpharma/templates/header.php'; ?>

    <div class="container mt-4">
        <h1 class="mb-4">Bienvenue sur BigPharma</h1>

        <div class="row">
            <div class="col-md-4 mb-4">
                <a href="/bigpharma/public/products" class="text-decoration-none">
                    <div class="card dashboard-card">
                        <div class="card-body text-center">
                            <h5 class="card-title">Produits Totaux</h5>
                            <div class="h2 text-primary"><?= $totalProducts ?></div>
                            <p class="text-muted">Nombre de médicaments en stock</p>
                        </div>
                    </div>
                </a>
            </div>

            <div class="col-md-4 mb-4">
                <a href="/bigpharma/public/products?filter=low_stock" class="text-decoration-none">
                    <div class="card dashboard-card">
                        <div class="card-body text-center">
                            <h5 class="card-title">Stock Limité</h5>
                            <div class="h2 text-warning"><?= count($lowStockProducts) ?></div>
                            <p class="text-muted">Produits nécessitant réapprovisionnement</p>
                        </div>
                    </div>
                </a>
            </div>

            <div class="col-md-4 mb-4">
                <a href="/bigpharma/public/products?filter=prescription" class="text-decoration-none">
                    <div class="card dashboard-card">
                        <div class="card-body text-center">
                            <h5 class="card-title">Produits sur Ordonnance</h5>
                            <div class="h2 text-danger"><?= count($prescriptionProducts) ?></div>
                            <p class="text-muted">Médicaments réglementés</p>
                        </div>
                    </div>
                </a>
            </div>
        </div>

        <!-- Liste des produits disponibles en stock -->
        <h3 class="mb-3 mt-4">Produits disponibles en stock</h3>
        
        <?php if (!$hasProducts): ?>
        <div class="alert alert-info">
            <i class="fas fa-info-circle me-2"></i>
            <strong>Aucun médicament enregistré</strong> - La pharmacie <?= htmlspecialchars($pharmacyName) ?> n'a pas encore de médicaments enregistrés.
        </div>
        <?php elseif (empty($featuredProducts)): ?>
        <div class="alert alert-warning">
            <i class="fas fa-exclamation-triangle me-2"></i>
            <strong>Stock vide</strong> - Aucun produit n'est actuellement disponible en stock.
        </div>
        <?php else: ?>
        
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
                                    <button 
                                        class="btn btn-success sell-product" 
                                        data-product-id="<?= $product->getId() ?>"
                                        data-product-name="<?= htmlspecialchars($product->getNom()) ?>"
                                        data-product-price="<?= $product->getPrix() ?>"
                                        data-product-stock="<?= $product->getStock() ?>"
                                        data-bs-toggle="modal" 
                                        data-bs-target="#clientSelectionModal"
                                    >
                                        <i class="bi bi-cash-coin"></i> Vendre
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
        <?php endif; ?>

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

    <?php require_once 'C:\xampp\htdocs\bigpharma/templates/footer.php'; ?>

    <!-- Modal de sélection de client -->
    <div class="modal fade" id="clientSelectionModal" tabindex="-1" aria-labelledby="clientSelectionModalLabel" aria-hidden="true">
        <div class="modal-dialog modal-lg">
            <div class="modal-content">
                <div class="modal-header">
                    <h5 class="modal-title" id="clientSelectionModalLabel">Sélectionner un client pour <span id="selectedProductName"></span></h5>
                    <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
                </div>
                <div class="modal-body">
                    <!-- Conteneur pour les messages d'erreur liés à la vente -->
                    <div id="saleErrorContainer"></div>
                    
                    <form id="saleForm">
                        <input type="hidden" id="selectedClientId" value="">
                        <input type="hidden" id="productId" value="">
                        
                        <div class="mb-3">
                            <label for="saleQuantity" class="form-label">Quantité</label>
                            <div class="input-group">
                                <button type="button" class="btn btn-outline-secondary" id="decreaseQuantity">-</button>
                                <input type="number" class="form-control" id="saleQuantity" value="1" min="1" max="10">
                                <button type="button" class="btn btn-outline-secondary" id="increaseQuantity">+</button>
                            </div>
                            <small class="form-text text-muted">Quantité disponible: <span id="availableStock">0</span></small>
                        </div>
                        
                        <div class="mb-3">
                            <label for="clientSearchInput" class="form-label">Rechercher un client</label>
                            <div class="input-group">
                                <input type="text" class="form-control" id="clientSearchInput" placeholder="Nom, prénom, email...">
                                <button class="btn btn-outline-primary" type="button" id="searchClientBtn">
                                    <i class="bi bi-search"></i> Rechercher
                                </button>
                            </div>
                        </div>
                        
                        <!-- Conteneur pour les messages d'erreur liés à la recherche de clients -->
                        <div id="clientErrorContainer"></div>
                        
                        <!-- Liste des clients avec hauteur fixe et défilement -->
                        <div style="max-height: 300px; overflow-y: auto; margin-bottom: 15px; border: 1px solid #dee2e6; border-radius: 0.25rem;">
                            <div class="list-group" id="clientsList">
                                <!-- La liste des clients sera chargée ici dynamiquement -->
                                <div class="text-center p-3">
                                    <div class="spinner-border" role="status">
                                        <span class="visually-hidden">Chargement...</span>
                                    </div>
                                </div>
                            </div>
                        </div>
                        
                        <div class="d-grid gap-2 mt-3">
                            <button type="submit" class="btn btn-primary" id="confirmSaleBtn" disabled>Confirmer la vente</button>
                        </div>
                    </form>
                </div>
            </div>
        </div>
    </div>

    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.2.3/dist/js/bootstrap.bundle.min.js"></script>
    
    <!-- Inclusion du fichier JavaScript pour la gestion des ventes -->
    <script src="/bigpharma/public/js/sales.js"></script>
</body>
</html>
