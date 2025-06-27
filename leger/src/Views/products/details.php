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
    <?php require_once 'C:\xampp\htdocs\bigpharma/templates/header.php'; ?>

    <div class="container mt-4" id="catalogue-section">
        <div id="sale-notification-area"></div>
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
                            class="btn btn-success btn-lg sell-product" 
                            data-product-id="<?= $product->getId() ?>"
                            data-product-name="<?= htmlspecialchars($product->getName()) ?>"
                            data-product-price="<?= $product->getPrice() ?>"
                            data-product-stock="<?= $product->getStock() ?>"
                            data-bs-toggle="modal" 
                            data-bs-target="#clientSelectionModal"
                        >
                            <i class="bi bi-cash-coin"></i> Vendre
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
                        
                        <div class="list-group" id="clientsList">
                            <!-- La liste des clients sera chargée ici dynamiquement -->
                            <div class="text-center">
                                <div class="spinner-border" role="status">
                                    <span class="visually-hidden">Chargement...</span>
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

    <?php require_once 'C:\xampp\htdocs\bigpharma/templates/footer.php'; ?>

    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.2.3/dist/js/bootstrap.bundle.min.js"></script>
    <script>
        // Variable globale pour stocker l'ID du produit sélectionné
        let selectedProductId = null;
        
        document.addEventListener('DOMContentLoaded', function() {
            // Initialiser les gestionnaires d'événements pour les boutons de vente
            initSellButtons();
            
            // Initialiser les contrôles de quantité
            initQuantityControls();
            
            // Initialiser le formulaire de vente
            initSaleForm();
        });
        
        /**
         * Initialise les boutons de vente
         */
        function initSellButtons() {
            const sellButtons = document.querySelectorAll('.sell-product');
            
            sellButtons.forEach(button => {
                button.addEventListener('click', function() {
                    // Récupérer les informations du produit
                    selectedProductId = this.getAttribute('data-product-id');
                    const productName = this.getAttribute('data-product-name');
                    const productStock = this.getAttribute('data-product-stock');
                    
                    // Réinitialiser les conteneurs d'erreur
                    const saleErrorContainer = document.getElementById('saleErrorContainer');
                    const clientErrorContainer = document.getElementById('clientErrorContainer');
                    if (saleErrorContainer) saleErrorContainer.innerHTML = '';
                    if (clientErrorContainer) clientErrorContainer.innerHTML = '';
                    
                    // Réinitialiser le formulaire
                    document.getElementById('saleForm').reset();
                    document.getElementById('selectedClientId').value = '';
                    document.getElementById('productId').value = selectedProductId;
                    
                    // Mettre à jour le titre du modal
                    document.getElementById('selectedProductName').textContent = productName || 'ce produit';
                    
                    // Définir la quantité maximale disponible
                    const maxStock = parseInt(productStock) || 0;
                    document.getElementById('saleQuantity').max = maxStock;
                    document.getElementById('saleQuantity').value = 1;
                    document.getElementById('availableStock').textContent = maxStock;
                    
                    // Désactiver le bouton de confirmation jusqu'à ce qu'un client soit sélectionné
                    const confirmButton = document.getElementById('confirmSaleBtn');
                    if (confirmButton) confirmButton.disabled = true;
                    
                    // Charger la liste des clients
                    loadClients();
                });
            });
        }
        
        /**
         * Initialise les contrôles de quantité
         */
        function initQuantityControls() {
            // Diminuer la quantité
            document.getElementById('decreaseQuantity').addEventListener('click', function() {
                const quantityInput = document.getElementById('saleQuantity');
                const currentQuantity = parseInt(quantityInput.value);
                quantityInput.value = Math.max(1, currentQuantity - 1);
            });

            // Augmenter la quantité
            document.getElementById('increaseQuantity').addEventListener('click', function() {
                const quantityInput = document.getElementById('saleQuantity');
                const currentQuantity = parseInt(quantityInput.value);
                const maxQuantity = parseInt(quantityInput.max);
                quantityInput.value = Math.min(maxQuantity, currentQuantity + 1);
            });

            // Validation de la saisie de quantité
            document.getElementById('saleQuantity').addEventListener('input', function() {
                const newQuantity = parseInt(this.value);
                if (isNaN(newQuantity) || newQuantity < 1) {
                    this.value = 1;
                } else if (newQuantity > parseInt(this.max)) {
                    this.value = parseInt(this.max);
                }
            });
        }
        
        /**
         * Initialise le formulaire de vente
         */
        function initSaleForm() {
            // Recherche de clients lors de la saisie
            document.getElementById('clientSearchInput').addEventListener('input', function() {
                const searchTerm = this.value.trim();
                if (searchTerm.length >= 2) {
                    loadClients(searchTerm);
                } else if (searchTerm.length === 0) {
                    loadClients();
                }
            });
            
            // Recherche de clients lors du clic sur le bouton de recherche
            document.getElementById('searchClientBtn').addEventListener('click', function() {
                const searchTerm = document.getElementById('clientSearchInput').value.trim();
                loadClients(searchTerm);
            });
            
            // Traitement du formulaire de vente
            document.getElementById('saleForm').addEventListener('submit', function(e) {
                e.preventDefault();
                
                const clientId = document.getElementById('selectedClientId').value;
                const quantity = document.getElementById('saleQuantity').value;
                const errorContainer = document.getElementById('saleErrorContainer');
                
                // Réinitialiser les messages d'erreur
                if (errorContainer) {
                    errorContainer.innerHTML = '';
                }
                
                // Validation des entrées
                let hasErrors = false;
                let errorMessages = [];
                
                if (!clientId) {
                    errorMessages.push('Veuillez sélectionner un client');
                    hasErrors = true;
                }
                
                if (!quantity) {
                    errorMessages.push('Veuillez saisir une quantité');
                    hasErrors = true;
                } else if (isNaN(quantity) || parseInt(quantity) <= 0) {
                    errorMessages.push('La quantité doit être un nombre positif');
                    hasErrors = true;
                } else if (parseInt(quantity) > parseInt(document.getElementById('saleQuantity').max)) {
                    errorMessages.push(`La quantité ne peut pas dépasser ${document.getElementById('saleQuantity').max} unités`);
                    hasErrors = true;
                }
                
                if (hasErrors) {
                    // Afficher les erreurs
                    if (errorContainer) {
                        errorContainer.innerHTML = `
                            <div class="alert alert-danger">
                                <ul class="mb-0">
                                    ${errorMessages.map(msg => `<li>${msg}</li>`).join('')}
                                </ul>
                            </div>
                        `;
                    } else {
                        alert(errorMessages.join('\n'));
                    }
                    return;
                }
                
                // Désactiver le bouton de soumission pendant le traitement
                const submitButton = document.querySelector('#saleForm button[type="submit"]');
                const originalButtonText = submitButton.innerHTML;
                submitButton.disabled = true;
                submitButton.innerHTML = '<span class="spinner-border spinner-border-sm" role="status" aria-hidden="true"></span> Traitement en cours...';
                
                // Préparation des données
                const formData = new FormData();
                formData.append('product_id', selectedProductId);
                formData.append('client_id', clientId);
                formData.append('quantity', quantity);
                
                // Envoi de la requête avec gestion des erreurs améliorée
                fetch('/bigpharma/api/sale/process', {
                    method: 'POST',
                    body: formData
                })
                .then(response => {
                    if (!response.ok) {
                        throw new Error(`Erreur HTTP: ${response.status} - ${response.statusText}`);
                    }
                    return response.json();
                })
                .then(data => {
                    if (data.success) {
                        // Fermer la modal
                        const modal = bootstrap.Modal.getInstance(document.getElementById('clientSelectionModal'));
                        modal.hide();
                        
                        // Afficher un message de succès
                        showSuccessMessage(data);
                        
                        // Mettre à jour l'affichage du stock
                        if (data.sale && data.sale.new_stock !== undefined) {
                            // Recharger la page pour mettre à jour les informations de stock
                            setTimeout(() => {
                                window.location.reload();
                            }, 2000); // Attendre 2 secondes pour que l'utilisateur puisse voir le message de succès
                        }
                    } else {
                        // Afficher l'erreur
                        if (errorContainer) {
                            showErrorMessage(data.message || 'Une erreur est survenue lors du traitement de la vente.', errorContainer);
                        } else {
                            showErrorMessage(data.message || 'Une erreur est survenue lors du traitement de la vente.');
                        }
                    }
                })
                .catch(error => {
                    console.error('Erreur lors du traitement de la vente:', error);
                    if (errorContainer) {
                        showErrorMessage(`Erreur lors du traitement de la vente: ${error.message}`, errorContainer);
                    } else {
                        showErrorMessage(`Erreur lors du traitement de la vente: ${error.message}`);
                    }
                })
                .finally(() => {
                    // Réactiver le bouton quoi qu'il arrive
                    submitButton.disabled = false;
                    submitButton.innerHTML = originalButtonText;
                });
            });
        }
        
        /**
         * Charge la liste des clients
         * @param {string} searchTerm - Terme de recherche optionnel
         */
        function loadClients(searchTerm = '') {
            const clientsList = document.getElementById('clientsList');
            const errorContainer = document.getElementById('clientErrorContainer');
            
            // Réinitialiser les conteneurs d'erreur
            if (errorContainer) {
                errorContainer.innerHTML = '';
            }
            
            // Afficher l'indicateur de chargement
            clientsList.innerHTML = '<div class="text-center"><div class="spinner-border" role="status"><span class="visually-hidden">Chargement...</span></div></div>';
            
            // Effectuer la requête
            fetch(`/bigpharma/api/clients${searchTerm ? `?search=${encodeURIComponent(searchTerm)}` : ''}`)
                .then(response => {
                    if (!response.ok) {
                        throw new Error(`Erreur HTTP: ${response.status} - ${response.statusText}`);
                    }
                    return response.json();
                })
                .then(data => {
                    if (data.success) {
                        if (data.clients && data.clients.length > 0) {
                            clientsList.innerHTML = '';
                            data.clients.forEach(client => {
                                const clientElement = document.createElement('div');
                                clientElement.className = 'list-group-item list-group-item-action client-item';
                                clientElement.setAttribute('data-client-id', client.id);
                                clientElement.innerHTML = `
                                    <div class="d-flex w-100 justify-content-between">
                                        <h5 class="mb-1">${client.nom_complet}</h5>
                                    </div>
                                    <p class="mb-1">${client.email || 'Pas d\'email'}</p>
                                    <small>${client.telephone || 'Pas de téléphone'}</small>
                                `;
                                clientElement.addEventListener('click', function() {
                                    document.querySelectorAll('.client-item').forEach(item => {
                                        item.classList.remove('active');
                                    });
                                    this.classList.add('active');
                                    document.getElementById('selectedClientId').value = client.id;
                                    document.getElementById('confirmSaleBtn').disabled = false;
                                });
                                clientsList.appendChild(clientElement);
                            });
                        } else {
                            clientsList.innerHTML = `<div class="alert alert-info">${data.message || 'Aucun client trouvé.'}</div>`;
                        }
                    } else {
                        clientsList.innerHTML = `<div class="alert alert-warning">${data.message || 'Aucun client trouvé.'}</div>`;
                    }
                })
                .catch(error => {
                    console.error('Erreur lors du chargement des clients:', error);
                    clientsList.innerHTML = `<div class="alert alert-danger">Erreur lors du chargement des clients: ${error.message}</div>`;
                });
        }
        
        /**
         * Affiche un message d'erreur
         * @param {string} message - Message d'erreur
         * @param {HTMLElement} container - Conteneur pour le message
         */
        function showErrorMessage(message, container = null) {
            if (container) {
                container.innerHTML = `<div class="alert alert-danger">${message}</div>`;
            } else {
                const errorAlert = document.createElement('div');
                errorAlert.className = 'alert alert-danger alert-dismissible fade show';
                errorAlert.setAttribute('role', 'alert');
                errorAlert.innerHTML = `
                    <strong>Erreur!</strong> ${message}
                    <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
                `;
                document.querySelector('.container').insertBefore(errorAlert, document.querySelector('.container').firstChild);
                window.scrollTo({ top: 0, behavior: 'smooth' });
            }
        }
        
        /**
         * Affiche un message de succès
         * @param {Object} data - Données de la vente
         */
        function showSuccessMessage(data) {
            const alertContainer = document.createElement('div');
            alertContainer.className = 'alert alert-success alert-dismissible fade show';
            alertContainer.setAttribute('role', 'alert');
            
            // Vérifier que les données nécessaires sont présentes
            let alertContent = `<strong>Vente réussie!</strong> ${data.message || 'La vente a été traitée avec succès.'}`;
            
            if (data.sale) {
                alertContent += `
                    <br>Produit: ${data.sale.product || 'N/A'}
                    <br>Client: ${data.sale.client || 'N/A'}
                    <br>Quantité: ${data.sale.quantity || 'N/A'}
                    <br>Total: ${data.sale.total ? data.sale.total + ' €' : 'N/A'}
                    <br>Nouveau stock: ${data.sale.new_stock !== undefined ? data.sale.new_stock : 'N/A'}
                `;
            }
            
            alertContent += `<button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>`;
            alertContainer.innerHTML = alertContent;
            
            document.querySelector('.container').insertBefore(alertContainer, document.querySelector('.container').firstChild);
            window.scrollTo({ top: 0, behavior: 'smooth' });
        }
    </script>
    
    <!-- Inclusion du fichier JavaScript pour la gestion des ventes -->
    <script src="/bigpharma/public/js/sales.js"></script>
</body>
</html>
