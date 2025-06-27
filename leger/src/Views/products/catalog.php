<?php
// Accéder aux données passées par le contrôleur
$products = $data['products'] ?? [];
$filterTitle = $data['filterTitle'] ?? 'Catalogue des Produits Pharmaceutiques';
$hasProducts = $data['hasProducts'] ?? true;
$pharmacyName = $data['pharmacyName'] ?? 'Pharmacie';
?>

<div class="container mt-4" id="catalogue-section">
    <h1 class="mb-4"><?= isset($filterTitle) ? htmlspecialchars($filterTitle) : 'Catalogue des Produits Pharmaceutiques' ?></h1>
    <div id="sale-notification-area"></div>

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

    <?php if (!$hasProducts): ?>
    <div class="alert alert-info">
        <i class="fas fa-info-circle me-2"></i>
        <strong>Aucun médicament enregistré</strong> - La pharmacie <?= htmlspecialchars($pharmacyName) ?> n'a pas encore de médicaments enregistrés.
    </div>
    <?php else: ?>
    
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
                                <span class="h5 mb-0"><?= $product->getPrix() !== null ? number_format($product->getPrix(), 2) : '0.00' ?> €</span>
                                
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
                                <?php if($product->getStock() > 0): ?>
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
                                        Rupture de stock
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
    <?php endif; ?>

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
    
    <!-- Inclusion du fichier JavaScript pour la gestion des ventes -->
    <script src="/bigpharma/public/js/sales.js"></script>

<?php require_once 'C:\xampp\htdocs\bigpharma/templates/footer.php'; ?>
