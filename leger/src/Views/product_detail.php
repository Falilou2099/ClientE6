<?php
// Vérifier si le produit existe
if (!isset($product) || !$product) {
    echo "<div class='alert alert-danger'>Produit non trouvé</div>";
    exit;
}
?>

<div class="container mt-4">
    <div class="row">
        <div class="col-md-8">
            <div class="card mb-4 shadow-sm">
                <div class="card-header bg-primary text-white">
                    <h2 class="mb-0"><?= htmlspecialchars($product->getNom()) ?></h2>
                </div>
                <div class="card-body">
                    <div class="row">
                        <div class="col-md-5">
                            <img src="<?= $product->getImageUrl() ?>" 
                                 alt="<?= htmlspecialchars($product->getNom()) ?>" 
                                 class="img-fluid rounded mb-3 shadow">
                                 
                            <?php if($product->getEstOrdonnance()): ?>
                                <div class="alert alert-danger">
                                    <i class="fas fa-prescription"></i> Ce médicament nécessite une ordonnance
                                </div>
                            <?php endif; ?>
                            
                            <?php if($product->getStock() > 0 && !$product->getEstOrdonnance()): ?>
                                <button 
                                    class="btn btn-success btn-lg w-100 sell-product" 
                                    data-product-id="<?= $product->getId() ?>"
                                    data-product-name="<?= htmlspecialchars($product->getNom()) ?>"
                                    data-product-price="<?= $product->getPrix() ?>"
                                    data-product-stock="<?= $product->getStock() ?>"
                                    data-bs-toggle="modal" 
                                    data-bs-target="#clientSelectionModal"
                                >
                                    <i class="bi bi-cash-coin me-2"></i> Vendre
                                </button>
                            <?php else: ?>
                                <button class="btn btn-secondary btn-lg w-100" disabled>
                                    <?= $product->getEstOrdonnance() ? 'Ordonnance requise' : 'Rupture de stock' ?>
                                </button>
                            <?php endif; ?>
                        </div>
                        <div class="col-md-7">
                            <h4 class="card-title">Informations Produit</h4>
                            <ul class="list-group list-group-flush mb-3">
                                <li class="list-group-item d-flex justify-content-between align-items-center">
                                    <strong>Catégorie :</strong> 
                                    <span class="badge bg-info"><?= htmlspecialchars($product->getCategorie()) ?></span>
                                </li>
                                <li class="list-group-item d-flex justify-content-between align-items-center">
                                    <strong>Prix :</strong> 
                                    <span class="badge bg-primary"><?= number_format($product->getPrix(), 2) ?> €</span>
                                </li>
                                <li class="list-group-item d-flex justify-content-between align-items-center">
                                    <strong>Stock :</strong> 
                                    <?php if ($product->getStock() > 0): ?>
                                        <span class="badge bg-success"><?= $product->getStock() ?> disponibles</span>
                                    <?php else: ?>
                                        <span class="badge bg-danger">Rupture de stock</span>
                                    <?php endif; ?>
                                </li>
                                <?php if ($product->getDateAjout()): ?>
                                <li class="list-group-item d-flex justify-content-between align-items-center">
                                    <strong>Date d'ajout :</strong> 
                                    <span><?= date('d/m/Y', strtotime($product->getDateAjout())) ?></span>
                                </li>
                                <?php endif; ?>
                            </ul>
                            
                            <div class="card mb-3">
                                <div class="card-header bg-light">
                                    <h5 class="mb-0">Description détaillée</h5>
                                </div>
                                <div class="card-body">
                                    <p class="card-text"><?= nl2br(htmlspecialchars($product->getDescription())) ?></p>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
        
        <div class="col-md-4">
            <?php if (!empty($similarProducts)): ?>
            <div class="card shadow-sm">
                <div class="card-header bg-secondary text-white">
                    <h4 class="mb-0">Produits similaires</h4>
                </div>
                <div class="card-body">
                    <div class="list-group">
                        <?php foreach ($similarProducts as $similarProduct): ?>
                            <?php if ($similarProduct->getId() != $product->getId()): ?>
                                <a href="/bigpharma/products/detail?id=<?= $similarProduct->getId() ?>" 
                                   class="list-group-item list-group-item-action">
                                    <div class="d-flex w-100 justify-content-between">
                                        <h5 class="mb-1"><?= htmlspecialchars($similarProduct->getNom()) ?></h5>
                                        <small><?= number_format($similarProduct->getPrix(), 2) ?> €</small>
                                    </div>
                                    <p class="mb-1"><?= htmlspecialchars(substr($similarProduct->getDescription(), 0, 50)) ?>...</p>
                                    <?php if ($similarProduct->getEstOrdonnance()): ?>
                                        <small class="text-danger">Ordonnance requise</small>
                                    <?php endif; ?>
                                </a>
                            <?php endif; ?>
                        <?php endforeach; ?>
                    </div>
                </div>
            </div>
            <?php endif; ?>
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

<!-- Inclusion du fichier JavaScript pour la gestion des ventes -->
<script src="/bigpharma/public/js/sales.js"></script>

<?php require_once 'C:\xampp\htdocs\bigpharma/templates/footer.php'; ?>
