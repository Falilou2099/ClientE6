<?php include '../../templates/header.php'; ?>

<div class="container">
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
                                    <button class="btn btn-sm btn-success">
                                        <i class="fas fa-shopping-cart"></i> Ajouter
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

<!-- Bootstrap JS et dépendances -->
<script src="https://cdn.jsdelivr.net/npm/@popperjs/core@2.11.6/dist/umd/popper.min.js"></script>
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.2.3/dist/js/bootstrap.min.js"></script>

<?php include '../../templates/footer.php'; ?>
