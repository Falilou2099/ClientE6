<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <title>Résultats de Recherche - BigPharma</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.2.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <style>
        body { background-color: #f4f6f9; }
        .product-card {
            transition: transform 0.3s ease;
            box-shadow: 0 4px 6px rgba(0,0,0,0.1);
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
        <div class="row mb-4">
            <div class="col-md-8">
                <h1 class="mb-3">
                    Résultats de Recherche 
                    <?= !empty($query) ? "pour \"" . htmlspecialchars($query) . "\"" : "" ?>
                </h1>
            </div>
            <div class="col-md-4">
                <form action="/bigpharma/search" method="GET" class="d-flex">
                    <input 
                        class="form-control me-2" 
                        type="search" 
                        name="query" 
                        placeholder="Rechercher un médicament" 
                        aria-label="Search"
                        value="<?= htmlspecialchars($query ?? '') ?>"
                    >
                    <button class="btn btn-outline-primary" type="submit">Rechercher</button>
                </form>
            </div>
        </div>

        <div class="row">
            <div class="col-md-3">
                <div class="card mb-4">
                    <div class="card-header">Filtres</div>
                    <div class="card-body">
                        <h6>Catégories</h6>
                        <div class="list-group">
                            <?php if (!empty($categories)): ?>
                                <?php foreach ($categories as $cat): ?>
                                <a href="/bigpharma/search?category=<?= urlencode($cat) ?>" class="list-group-item list-group-item-action <?= ($category === $cat) ? 'active' : '' ?>">
                                    <?= htmlspecialchars($cat) ?>
                                </a>
                                <?php endforeach; ?>
                            <?php else: ?>
                                <a href="/bigpharma/search?category=medicaments" class="list-group-item list-group-item-action">
                                    Médicaments
                                </a>
                                <a href="/bigpharma/search?category=soins" class="list-group-item list-group-item-action">
                                    Soins du corps
                                </a>
                            <?php endif; ?>
                        </div>

                        <h6 class="mt-3">Trier par</h6>
                        <div class="list-group">
                            <a href="/bigpharma/search?sort=price<?= isset($query) ? '&query=' . urlencode($query) : '' ?>" class="list-group-item list-group-item-action">
                                Prix
                            </a>
                            <a href="/bigpharma/search?sort=stock<?= isset($query) ? '&query=' . urlencode($query) : '' ?>" class="list-group-item list-group-item-action">
                                Stock
                            </a>
                        </div>
                    </div>
                </div>
            </div>

            <div class="col-md-9">
                <?php if (empty($results)): ?>
                    <div class="alert alert-info">
                        Aucun résultat trouvé pour votre recherche.
                    </div>
                <?php else: ?>
                    <div class="row">
                        <?php foreach ($results as $product): ?>
                            <div class="col-md-4 mb-4">
                                <div class="card product-card position-relative">
                                    <?php if($product->getEstOrdonnance()): ?>
                                        <span class="badge bg-danger badge-prescription">Ordonnance</span>
                                    <?php endif; ?>
                                    
                                    <div class="card-body">
                                        <h5 class="card-title"><?= htmlspecialchars($product->getNom()) ?></h5>
                                        <p class="card-text">
                                            <?= htmlspecialchars($product->getDescription()) ?><br>
                                            <strong>Catégorie :</strong> <?= htmlspecialchars($product->getCategorie()) ?>
                                        </p>
                                        
                                        <div class="d-flex justify-content-between align-items-center">
                                            <span class="h5 mb-0"><?= number_format($product->getPrix(), 2) ?> €</span>
                                            
                                            <?php if($product->getStock() > 0): ?>
                                                <span class="badge bg-success">En stock</span>
                                            <?php else: ?>
                                                <span class="badge bg-danger">Rupture</span>
                                            <?php endif; ?>
                                        </div>
                                        
                                        <div class="mt-3">
                                            <a 
                                                href="/bigpharma/products/detail?id=<?= $product->getId() ?>" 
                                                class="btn btn-primary w-100"
                                                <?= $product->getEstOrdonnance() ? 'disabled' : '' ?>
                                            >
                                                <?= $product->getEstOrdonnance() ? 'Ordonnance requise' : 'Détails' ?>
                                            </a>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        <?php endforeach; ?>
                    </div>

                    <!-- Pagination -->
                    <nav aria-label="Navigation des résultats de recherche">
                        <ul class="pagination justify-content-center mt-4">
                            <?php
                            $currentPage = isset($_GET['page']) ? (int)$_GET['page'] : 1;
                            $totalResults = count($results);
                            $resultsPerPage = 12;
                            $totalPages = ceil($totalResults / $resultsPerPage);
                            
                            // Bouton précédent
                            if ($currentPage > 1): ?>
                                <li class="page-item">
                                    <a class="page-link" href="?query=<?= urlencode($query ?? '') ?>&page=<?= $currentPage - 1 ?><?= isset($_GET['category']) ? '&category=' . urlencode($_GET['category']) : '' ?>">Précédent</a>
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
                                        <a class="page-link" href="?query=<?= urlencode($query ?? '') ?>&page=<?= $i ?><?= isset($_GET['category']) ? '&category=' . urlencode($_GET['category']) : '' ?>"><?= $i ?></a>
                                    </li>
                                <?php endif;
                            endfor;
                            
                            // Bouton suivant
                            if ($currentPage < $totalPages): ?>
                                <li class="page-item">
                                    <a class="page-link" href="?query=<?= urlencode($query ?? '') ?>&page=<?= $currentPage + 1 ?><?= isset($_GET['category']) ? '&category=' . urlencode($_GET['category']) : '' ?>">Suivant</a>
                                </li>
                            <?php else: ?>
                                <li class="page-item disabled">
                                    <a class="page-link" href="#" tabindex="-1" aria-disabled="true">Suivant</a>
                                </li>
                            <?php endif; ?>
                        </ul>
                    </nav>
                <?php endif; ?>
            </div>
        </div>
    </div>

    <?php include __DIR__ . '/../../templates/footer.php'; ?>

    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.2.3/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
