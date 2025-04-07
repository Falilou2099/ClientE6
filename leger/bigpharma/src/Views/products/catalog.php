<?php
// Vérifier si la variable $products existe
if (!isset($products) || empty($products)) {
    echo '<div class="alert alert-warning">Aucun produit disponible actuellement.</div>';
}
?>

<div class="container mt-4">
    <h1 class="mb-4">Catalogue des Produits Pharmaceutiques</h1>

    <div class="row mb-4">
        <div class="col-md-8">
            <form id="searchForm" class="d-flex">
                <input 
                    class="form-control me-2" 
                    type="search" 
                    placeholder="Rechercher un médicament" 
                    aria-label="Search"
                    id="searchInput"
                >
                <button class="btn btn-outline-primary" type="submit">Rechercher</button>
            </form>
        </div>
        <div class="col-md-4">
            <select class="form-select" id="categoryFilter">
                <option selected>Toutes les catégories</option>
                <option>Antalgiques</option>
                <option>Antibiotiques</option>
                <option>Anti-inflammatoires</option>
                <option>Vaccins</option>
            </select>
        </div>
    </div>

    <div class="row" id="productGrid">
        <?php if (isset($products) && !empty($products)): ?>
            <?php foreach($products as $product): ?>
                <div class="col-md-4 mb-4">
                    <div class="card product-card position-relative h-100">
                        <?php if($product->getEstOrdonnance()): ?>
                            <span class="badge bg-danger badge-prescription">Ordonnance</span>
                        <?php endif; ?>
                        
                        <img src="<?= $product->getImageUrl() ?>" 
                             class="card-img-top" alt="<?= htmlspecialchars($product->getNom()) ?>"
                             style="height: 200px; object-fit: cover;">
                        
                        <div class="card-body d-flex flex-column">
                            <h5 class="card-title"><?= htmlspecialchars($product->getNom()) ?></h5>
                            <p class="card-text flex-grow-1">
                                <?= htmlspecialchars($product->getDescription()) ?>
                                <br>
                                <strong>Catégorie :</strong> <?= htmlspecialchars($product->getCategorie()) ?>
                            </p>
                            
                            <div class="d-flex justify-content-between align-items-center mt-auto">
                                <span class="h5 mb-0"><?= number_format($product->getPrix(), 2) ?> €</span>
                                
                                <?php if($product->getStock() > 0): ?>
                                    <?php if($product->getStock() < 10): ?>
                                        <span class="badge bg-warning text-dark">Stock faible: <?= $product->getStock() ?></span>
                                    <?php else: ?>
                                        <span class="badge bg-success">En stock: <?= $product->getStock() ?></span>
                                    <?php endif; ?>
                                <?php else: ?>
                                    <span class="badge bg-danger">Rupture de stock</span>
                                <?php endif; ?>
                            </div>
                            
                            <div class="mt-3 d-flex justify-content-between">
                                <a href="/bigpharma/products/detail?id=<?= $product->getId() ?>" class="btn btn-outline-primary">
                                    Détails
                                </a>
                                <?php if($product->getStock() > 0 && !$product->getEstOrdonnance()): ?>
                                    <div class="input-group" style="max-width: 150px;">
                                        <button class="btn btn-outline-secondary decrease-quantity" type="button" data-product-id="<?= $product->getId() ?>">-</button>
                                        <input type="number" class="form-control text-center product-quantity" value="1" min="1" max="<?= $product->getStock() ?>" data-product-id="<?= $product->getId() ?>">
                                        <button class="btn btn-primary add-to-cart" type="button" data-product-id="<?= $product->getId() ?>">+</button>
                                    </div>
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
        <?php else: ?>
            <div class="col-12">
                <div class="alert alert-info">Aucun produit trouvé.</div>
            </div>
        <?php endif; ?>
    </div>

    <!-- Pagination -->
    <nav aria-label="Navigation des pages de produits">
        <ul class="pagination justify-content-center">
            <?php
            $currentPage = isset($_GET['page']) ? (int)$_GET['page'] : 1;
            $totalPages = ceil(count($products) / 12); // 12 produits par page
            
            // Bouton précédent
            if ($currentPage > 1): ?>
                <li class="page-item">
                    <a class="page-link" href="?page=<?= $currentPage - 1 ?><?= isset($_GET['category']) ? '&category=' . urlencode($_GET['category']) : '' ?>">Précédent</a>
                </li>
            <?php else: ?>
                <li class="page-item disabled">
                    <a class="page-link" href="#" tabindex="-1" aria-disabled="true">Précédent</a>
                </li>
            <?php endif; 
            
            // Pages numérotées
            for ($i = 1; $i <= $totalPages; $i++):
                if ($i == $currentPage): ?>
                    <li class="page-item active" aria-current="page">
                        <a class="page-link" href="#"><?= $i ?></a>
                    </li>
                <?php else: ?>
                    <li class="page-item">
                        <a class="page-link" href="?page=<?= $i ?><?= isset($_GET['category']) ? '&category=' . urlencode($_GET['category']) : '' ?>"><?= $i ?></a>
                    </li>
                <?php endif;
            endfor;
            
            // Bouton suivant
            if ($currentPage < $totalPages): ?>
                <li class="page-item">
                    <a class="page-link" href="?page=<?= $currentPage + 1 ?><?= isset($_GET['category']) ? '&category=' . urlencode($_GET['category']) : '' ?>">Suivant</a>
                </li>
            <?php else: ?>
                <li class="page-item disabled">
                    <a class="page-link" href="#" tabindex="-1" aria-disabled="true">Suivant</a>
                </li>
            <?php endif; ?>
        </ul>
    </nav>

    <script>
        document.addEventListener('DOMContentLoaded', function() {
            const searchForm = document.getElementById('searchForm');
            const searchInput = document.getElementById('searchInput');
            const categoryFilter = document.getElementById('categoryFilter');
            const productGrid = document.getElementById('productGrid');

            // Empêcher la soumission du formulaire
            searchForm.addEventListener('submit', function(e) {
                e.preventDefault();
                filterProducts();
            });

            // Filtrage dynamique
            function filterProducts() {
                const searchTerm = searchInput.value.toLowerCase();
                const selectedCategory = categoryFilter.value;

                document.querySelectorAll('.product-card').forEach(card => {
                    const productName = card.querySelector('.card-title').textContent.toLowerCase();
                    const productCategory = card.querySelector('.card-text').textContent.toLowerCase();
                    
                    const matchesSearch = productName.includes(searchTerm) || 
                                          productCategory.includes(searchTerm);
                    const matchesCategory = selectedCategory === 'Toutes les catégories' || 
                                            productCategory.includes(selectedCategory.toLowerCase());

                    card.closest('.col-md-4').style.display = (matchesSearch && matchesCategory) 
                        ? 'block' 
                        : 'none';
                });
            }

            searchInput.addEventListener('input', filterProducts);
            categoryFilter.addEventListener('change', filterProducts);

            // Gestion du panier
            document.querySelectorAll('.add-to-cart').forEach(button => {
                button.addEventListener('click', function() {
                    const productId = this.getAttribute('data-product-id');
                    const quantityInput = this.parentNode.querySelector('.product-quantity');
                    const quantity = parseInt(quantityInput.value);
                    
                    // TODO: Implémenter la logique d'ajout au panier
                    alert(`Produit ${productId} ajouté au panier (quantité: ${quantity})`);
                });
            });

            // Gestion de la quantité
            document.querySelectorAll('.decrease-quantity').forEach(button => {
                button.addEventListener('click', function() {
                    const quantityInput = this.parentNode.querySelector('.product-quantity');
                    const currentQuantity = parseInt(quantityInput.value);
                    const newQuantity = Math.max(1, currentQuantity - 1);
                    quantityInput.value = newQuantity;
                });
            });

            document.querySelectorAll('.product-quantity').forEach(input => {
                input.addEventListener('input', function() {
                    const newQuantity = parseInt(this.value);
                    if (isNaN(newQuantity) || newQuantity < 1) {
                        this.value = 1;
                    } else if (newQuantity > parseInt(this.getAttribute('max'))) {
                        this.value = parseInt(this.getAttribute('max'));
                    }
                });
            });
        });
    </script>
