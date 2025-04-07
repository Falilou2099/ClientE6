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
                                <button class="btn btn-primary btn-lg w-100 add-to-cart" data-product-id="<?= $product->getId() ?>">
                                    <i class="fas fa-shopping-cart me-2"></i> Ajouter au panier
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

<script>
    document.addEventListener('DOMContentLoaded', function() {
        // Gestion de l'ajout au panier
        const addToCartButtons = document.querySelectorAll('.add-to-cart');
        addToCartButtons.forEach(button => {
            button.addEventListener('click', function() {
                const productId = this.getAttribute('data-product-id');
                alert(`Produit ${productId} ajouté au panier`);
                // TODO: Implémenter la logique d'ajout au panier
            });
        });
    });
</script>
