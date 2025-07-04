<?php require_once 'C:\xampp\htdocs\bigpharma/templates/header.php'; ?>

<div class="container" id="catalogue-section">
    <div class="row mb-4">
        <div class="col-12">
            <div class="card bg-primary text-white">
                <div class="card-body d-flex align-items-center">
                    <i class="fas fa-box-open fa-3x me-3"></i>
                    <div>
                        <h1 class="card-title">Catalogue des Produits</h1>
                        <p class="card-text">Découvrez notre gamme complète de médicaments</p>
                    </div>
                </div>
            </div>
        </div>
    </div>
    
    <div id="sale-notification-area"></div>

    <div class="row">
        <?php if (empty($products)): ?>
            <div class="col-12">
                <div class="alert alert-info text-center">
                    <i class="fas fa-info-circle me-2"></i>
                    Aucun produit n'est actuellement disponible.
                </div>
            </div>
        <?php else: ?>
            <?php foreach ($products as $product): ?>
                <div class="col-md-4 mb-4">
                    <div class="card h-100 shadow-sm">
                        <div class="card-body">
                            <h5 class="card-title"><?= htmlspecialchars($product['nom']) ?></h5>
                            <h6 class="card-subtitle mb-2 text-muted">
                                <?= htmlspecialchars($product['categorie']) ?>
                            </h6>
                            <p class="card-text">
                                <strong>Prix :</strong> <?= number_format($product['prix'], 2) ?> €<br>
                                <strong>Stock :</strong> <?= $product['quantite_stock'] ?>
                            </p>
                            <div class="d-flex justify-content-between align-items-center">
                                <a href="/products/detail?id=<?= $product['id'] ?>" class="btn btn-sm btn-outline-primary">
                                    <i class="fas fa-info-circle"></i> Détails
                                </a>
                                <?php if ($product['quantite_stock'] > 0): ?>
                                    <button 
                                        class="btn btn-sm btn-success sell-product" 
                                        data-product-id="<?= $product['id'] ?>"
                                        data-product-name="<?= htmlspecialchars($product['nom']) ?>"
                                        data-product-price="<?= $product['prix'] ?>"
                                        data-product-stock="<?= $product['quantite_stock'] ?>"
                                        data-bs-toggle="modal" 
                                        data-bs-target="#clientSelectionModal"
                                    >
                                        <i class="bi bi-cash-coin"></i> Vendre
                                    </button>
                                <?php else: ?>
                                    <span class="badge bg-danger">Rupture</span>
                                <?php endif; ?>
                            </div>
                        </div>
                    </div>
                </div>
            <?php endforeach; ?>
        <?php endif; ?>
    </div>
</div>

<?php
// Démarrer la session pour les messages flash
if (session_status() == PHP_SESSION_NONE) {
    session_start();
}
?>

<div class="row">
    <div class="col-12">
        <h1 class="mb-4"><i class="fas fa-box-open"></i> Gestion des Produits</h1>

        <?php if (isset($_SESSION['error'])): ?>
            <div class="alert alert-danger alert-dismissible fade show" role="alert">
                <?= htmlspecialchars($_SESSION['error']) ?>
                <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
            </div>
            <?php unset($_SESSION['error']); ?>
        <?php endif; ?>

        <?php if (isset($_SESSION['success'])): ?>
            <div class="alert alert-success alert-dismissible fade show" role="alert">
                <?= htmlspecialchars($_SESSION['success']) ?>
                <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
            </div>
            <?php unset($_SESSION['success']); ?>
        <?php endif; ?>

        <div class="card">
            <div class="card-header bg-primary text-white">
                <h5 class="card-title mb-0"><i class="fas fa-list"></i> Liste des Produits</h5>
            </div>
            <div class="card-body">
                <div class="table-responsive">
                    <table class="table table-striped table-hover">
                        <thead class="table-light">
                            <tr>
                                <th>ID</th>
                                <th>Nom</th>
                                <th>Description</th>
                                <th>Prix</th>
                                <th>Stock</th>
                                <th>Statut</th>
                                <th>Actions</th>
                            </tr>
                        </thead>
                        <tbody>
                            <?php foreach ($productsData as $product): ?>
                                <tr>
                                    <td><?= htmlspecialchars($product['id']) ?></td>
                                    <td><?= htmlspecialchars($product['nom']) ?></td>
                                    <td><?= htmlspecialchars($product['description']) ?></td>
                                    <td><?= number_format($product['prix'], 2) ?> €</td>
                                    <td><?= htmlspecialchars($product['quantite_stock']) ?></td>
                                    <td>
                                        <span class="badge <?= $product['stock_class'] ?>">
                                            <?= $product['stock_status'] ?>
                                        </span>
                                    </td>
                                    <td>
                                        <?php if ($product['quantite_stock'] < 20): ?>
                                            <button type="button" class="btn btn-warning btn-sm" data-bs-toggle="modal" data-bs-target="#restockModal<?= $product['id'] ?>">
                                                <i class="fas fa-plus-circle"></i> Réapprovisionner
                                            </button>
                                        <?php endif; ?>
                                    </td>
                                </tr>

                                <!-- Modal de réapprovisionnement -->
                                <div class="modal fade" id="restockModal<?= $product['id'] ?>" tabindex="-1">
                                    <div class="modal-dialog">
                                        <div class="modal-content">
                                            <div class="modal-header">
                                                <h5 class="modal-title">Réapprovisionner <?= htmlspecialchars($product['nom']) ?></h5>
                                                <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
                                            </div>
                                            <form action="/products/restock" method="POST">
                                                <div class="modal-body">
                                                    <input type="hidden" name="product_id" value="<?= $product['id'] ?>">
                                                    <div class="mb-3">
                                                        <label class="form-label">Quantité à ajouter</label>
                                                        <input type="number" name="quantity" class="form-control" 
                                                               min="1" 
                                                               max="<?= 100 - $product['quantite_stock'] ?>" 
                                                               required>
                                                        <small class="form-text text-muted">
                                                            Stock actuel : <?= $product['quantite_stock'] ?> 
                                                            | Limite maximale : <?= 100 - $product['quantite_stock'] ?>
                                                        </small>
                                                    </div>
                                                </div>
                                                <div class="modal-footer">
                                                    <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Annuler</button>
                                                    <button type="submit" class="btn btn-primary">Réapprovisionner</button>
                                                </div>
                                            </form>
                                        </div>
                                    </div>
                                </div>
                            <?php endforeach; ?>
                        </tbody>
                    </table>
                </div>
            </div>
        </div>
    </div>
</div>

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

<!-- Bootstrap JS et dépendances -->
<script src="https://cdn.jsdelivr.net/npm/@popperjs/core@2.11.6/dist/umd/popper.min.js"></script>
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.2.3/dist/js/bootstrap.min.js"></script>

<!-- Inclusion du fichier JavaScript pour la gestion des ventes -->
<script src="/bigpharma/public/js/sales.js"></script>

<?php require_once 'C:\xampp\htdocs\bigpharma/templates/footer.php'; ?>
