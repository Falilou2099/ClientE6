/**
 * BigPharma - Gestion des ventes de produits pharmaceutiques
 * Ce fichier contient toutes les fonctionnalités JavaScript pour la gestion des ventes
 */

// Variable globale pour stocker l'ID du produit sélectionné
let selectedProductId = null;

/**
 * Initialisation des événements lorsque le DOM est chargé
 */
document.addEventListener('DOMContentLoaded', function() {
    // Vérifier s'il y a un message de vente réussie dans sessionStorage
    checkSaleSuccessMessage();
    
    // Initialiser les gestionnaires d'événements pour les boutons de vente
    initSellButtons();
    
    // Initialiser les gestionnaires d'événements pour le modal de sélection de client
    initClientSelectionModal();
    
    // Initialiser les gestionnaires d'événements pour la recherche de clients
    initClientSearch();
    
    // Initialiser les gestionnaires d'événements pour la gestion de la quantité
    initQuantityControls();
});

/**
 * Vérifie s'il y a un message de vente réussie dans l'URL et l'affiche
 */
function checkSaleSuccessMessage() {
    try {
        // Vérifier si l'URL contient un paramètre de succès de vente
        const urlParams = new URLSearchParams(window.location.search);
        const saleSuccessParam = urlParams.get('sale_success');
        
        if (saleSuccessParam) {
            // Décoder les données de la vente depuis l'URL
            const saleData = JSON.parse(atob(decodeURIComponent(saleSuccessParam)));
            
            // Nettoyer l'URL pour enlever le paramètre de succès
            // Cela évite que le message s'affiche à nouveau si l'utilisateur actualise la page
            const cleanUrl = window.location.pathname + 
                             window.location.search.replace(/[?&]sale_success=[^&]+/, '').replace(/^&/, '?') + 
                             window.location.hash;
            window.history.replaceState({}, document.title, cleanUrl);
            
            // Afficher le message de succès
            showSuccessMessage(saleData);
            
            // Mettre à jour l'affichage du stock si nécessaire
            if (saleData.sale && saleData.sale.new_stock !== undefined && saleData.sale.product_id) {
                updateProductStock(saleData.sale.product_id, saleData.sale.new_stock);
            }
        }
    } catch (e) {
        console.error('Erreur lors de la récupération du message de vente:', e);
    }
}

/**
 * Initialise les boutons de vente sur les cartes de produits
 */
function initSellButtons() {
    document.querySelectorAll('.sell-product').forEach(button => {
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
 * Initialise les événements du modal de sélection de client
 */
function initClientSelectionModal() {
    // Gestion du formulaire de vente
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
        
        // Envoi de la requête avec gestion d'erreurs améliorée
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
                // Rediriger vers la même page avec un paramètre de succès
                try {
                    // Encoder les données de la vente en base64 pour les passer dans l'URL
                    const saleData = btoa(JSON.stringify(data));
                    
                    // Construire l'URL de redirection
                    let redirectUrl = window.location.href;
                    
                    // Ajouter le paramètre de succès
                    if (redirectUrl.indexOf('?') > -1) {
                        // L'URL contient déjà des paramètres
                        redirectUrl += '&sale_success=' + encodeURIComponent(saleData);
                    } else {
                        // L'URL ne contient pas encore de paramètres
                        redirectUrl += '?sale_success=' + encodeURIComponent(saleData);
                    }
                    
                    // Rediriger vers la nouvelle URL
                    window.location.href = redirectUrl;
                    
                } catch (e) {
                    console.error('Erreur lors de la redirection après vente:', e);
                    
                    // En cas d'erreur, essayer de fermer le modal et afficher le message directement
                    try {
                        // Forcer la fermeture de tous les modaux et backdrops
                        document.querySelectorAll('.modal').forEach(modal => {
                            modal.style.display = 'none';
                        });
                        document.querySelectorAll('.modal-backdrop').forEach(backdrop => {
                            backdrop.remove();
                        });
                        document.body.classList.remove('modal-open');
                        document.body.style.overflow = '';
                        document.body.style.paddingRight = '';
                        
                        // Afficher le message de succès directement
                        showSuccessMessage(data);
                    } catch (modalError) {
                        console.error('Erreur lors de la fermeture du modal:', modalError);
                    }
                }
                
                // Mettre à jour l'affichage du stock
                if (data.sale && data.sale.new_stock !== undefined) {
                    updateProductStock(selectedProductId, data.sale.new_stock);
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
 * Initialise les contrôles de quantité dans le modal
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
 * Initialise la recherche de clients
 */
function initClientSearch() {
    // Recherche de clients lors de la saisie
    document.getElementById('clientSearchInput').addEventListener('input', function() {
        const searchTerm = this.value.trim();
        if (searchTerm.length >= 2) {
            loadClients(searchTerm);
        } else if (searchTerm.length === 0) {
            // Charger les 5 premiers clients par défaut
            loadClients('');
        }
    });
    
    // Recherche de clients lors du clic sur le bouton de recherche
    document.getElementById('searchClientBtn').addEventListener('click', function() {
        const searchTerm = document.getElementById('clientSearchInput').value.trim();
        if (searchTerm.length >= 2) {
            loadClients(searchTerm);
        } else {
            document.getElementById('clientsList').innerHTML = '<div class="alert alert-warning">Veuillez saisir au moins 2 caractères pour rechercher un client.</div>';
        }
    });
}

/**
 * Charge la liste des clients avec gestion d'erreurs améliorée
 * @param {string} searchTerm - Terme de recherche optionnel
 */
function loadClients(searchTerm = '') {
    const clientsList = document.getElementById('clientsList');
    const errorContainer = document.getElementById('clientErrorContainer');
    const confirmButton = document.getElementById('confirmSaleBtn');
    
    // Désactiver le bouton de confirmation jusqu'à ce qu'un client soit sélectionné
    if (confirmButton) {
        confirmButton.disabled = true;
    }
    
    // Réinitialiser les conteneurs d'erreur
    if (errorContainer) {
        errorContainer.innerHTML = '';
    }
    
    // Afficher l'indicateur de chargement
    clientsList.innerHTML = '<div class="text-center"><div class="spinner-border" role="status"><span class="visually-hidden">Chargement...</span></div></div>';
    
    // Validation du terme de recherche
    if (searchTerm && searchTerm.length > 50) {
        searchTerm = searchTerm.substring(0, 50);
        if (errorContainer) {
            errorContainer.innerHTML = '<div class="alert alert-warning">Le terme de recherche a été tronqué à 50 caractères.</div>';
        }
    }
    
    // Vérifier si le terme de recherche contient des caractères spéciaux non autorisés
    if (searchTerm && !/^[a-zA-Z0-9\s\-\.\@]+$/.test(searchTerm)) {
        clientsList.innerHTML = '<div class="alert alert-danger">Terme de recherche invalide. Utilisez uniquement des lettres, chiffres, espaces et caractères spéciaux autorisés.</div>';
        return;
    }
    
    // Effectuer la requête avec gestion des erreurs améliorée
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
                    // Trier les clients par ordre alphabétique (nom, puis prénom)
                    data.clients.sort((a, b) => {
                        // D'abord comparer les noms
                        if (a.nom !== b.nom) {
                            return a.nom.localeCompare(b.nom, 'fr', {sensitivity: 'base'});
                        }
                        // Si les noms sont identiques, comparer les prénoms
                        return a.prenom.localeCompare(b.prenom, 'fr', {sensitivity: 'base'});
                    });
                    
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
                    
                    // Ajouter un message si la liste a été limitée
                    if (data.limited) {
                        const limitMessage = document.createElement('div');
                        limitMessage.className = 'alert alert-info mt-2 mb-0';
                        limitMessage.innerHTML = 'Affichage limité à 5 clients. Précisez votre recherche pour des résultats plus pertinents.';
                        clientsList.appendChild(limitMessage);
                    }
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
 * @param {string} message - Message d'erreur à afficher
 * @param {HTMLElement} container - Conteneur optionnel pour afficher l'erreur
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
 * Affiche un message de succès avec une apparence améliorée
 * @param {Object} data - Données de la vente
 */
function showSuccessMessage(data) {
    // Créer un conteneur personnalisé pour le message
    const alertContainer = document.createElement('div');
    alertContainer.className = 'card border-success mb-3 shadow';
    alertContainer.style.maxWidth = '100%';
    alertContainer.style.animation = 'fadeIn 0.5s';
    alertContainer.style.position = 'fixed';
    alertContainer.style.top = '20px';
    alertContainer.style.left = '50%';
    alertContainer.style.transform = 'translateX(-50%)';
    alertContainer.style.zIndex = '1050';
    alertContainer.style.width = '80%';
    alertContainer.style.maxWidth = '600px';
    
    // Ajouter du CSS pour l'animation
    if (!document.getElementById('sale-success-styles')) {
        const styleEl = document.createElement('style');
        styleEl.id = 'sale-success-styles';
        styleEl.textContent = `
            @keyframes fadeIn {
                from { opacity: 0; transform: translate(-50%, -20px); }
                to { opacity: 1; transform: translate(-50%, 0); }
            }
            @keyframes fadeOut {
                from { opacity: 1; transform: translate(-50%, 0); }
                to { opacity: 0; transform: translate(-50%, -20px); }
            }
            .sale-success-icon {
                font-size: 2rem;
                color: #198754;
                margin-right: 10px;
            }
            .sale-details-row {
                display: flex;
                justify-content: space-between;
                margin: 8px 0;
                border-bottom: 1px solid #e9ecef;
                padding-bottom: 8px;
            }
            .sale-details-row:last-child {
                border-bottom: none;
            }
            .sale-details-label {
                font-weight: 500;
                color: #495057;
            }
            .sale-details-value {
                font-weight: 600;
                color: #212529;
            }
            .sale-total-row {
                background-color: #f8f9fa;
                font-weight: 700;
                color: #198754;
                padding: 8px;
                border-radius: 4px;
                margin-top: 10px;
            }
        `;
        document.head.appendChild(styleEl);
    }
    
    // Créer l'en-tête du message
    const cardHeader = document.createElement('div');
    cardHeader.className = 'card-header bg-success text-white d-flex align-items-center';
    cardHeader.innerHTML = `
        <i class="bi bi-check-circle-fill sale-success-icon"></i>
        <div>
            <h5 class="mb-0">Vente réussie!</h5>
            <small>${data.message || 'La vente a été traitée avec succès.'}</small>
        </div>
        <button type="button" class="btn-close btn-close-white ms-auto" data-bs-dismiss="alert" aria-label="Close"></button>
    `;
    
    // Créer le corps du message
    const cardBody = document.createElement('div');
    cardBody.className = 'card-body';
    
    if (data.sale) {
        // Créer un contenu détaillé pour la vente
        const detailsContent = document.createElement('div');
        detailsContent.className = 'sale-details';
        
        // Ajouter les détails de la vente
        const details = [
            { label: 'Produit', value: data.sale.product || 'N/A' },
            { label: 'Client', value: data.sale.client || 'N/A' },
            { label: 'Quantité', value: data.sale.quantity || 'N/A' },
            { label: 'Nouveau stock', value: data.sale.new_stock !== undefined ? data.sale.new_stock : 'N/A' }
        ];
        
        details.forEach(detail => {
            const row = document.createElement('div');
            row.className = 'sale-details-row';
            row.innerHTML = `
                <span class="sale-details-label">${detail.label}:</span>
                <span class="sale-details-value">${detail.value}</span>
            `;
            detailsContent.appendChild(row);
        });
        
        // Ajouter le total avec une mise en évidence spéciale
        if (data.sale.total) {
            const totalRow = document.createElement('div');
            totalRow.className = 'sale-details-row sale-total-row';
            totalRow.innerHTML = `
                <span class="sale-details-label">Total:</span>
                <span class="sale-details-value">${data.sale.total} €</span>
            `;
            detailsContent.appendChild(totalRow);
        }
        
        cardBody.appendChild(detailsContent);
    }
    
    // Assembler le message
    alertContainer.appendChild(cardHeader);
    alertContainer.appendChild(cardBody);
    
    // Ajouter le message dans la zone dédiée aux notifications de vente
    const notificationArea = document.querySelector('#sale-notification-area');
    
    if (notificationArea) {
        // Insérer dans la zone dédiée aux notifications
        notificationArea.appendChild(alertContainer);
    } else {
        // Fallback: essayer de trouver la section du catalogue
        const catalogueSection = document.querySelector('#catalogue-section') || document.querySelector('h1')?.closest('div') || document.querySelector('.container');
        
        if (catalogueSection) {
            // Insérer après le titre du catalogue
            const catalogueTitle = catalogueSection.querySelector('h1') || catalogueSection.firstChild;
            if (catalogueTitle && catalogueTitle.nextSibling) {
                catalogueSection.insertBefore(alertContainer, catalogueTitle.nextSibling);
            } else {
                catalogueSection.insertBefore(alertContainer, catalogueSection.firstChild);
            }
        } else {
            // Dernier recours: insérer au début du conteneur principal
            const container = document.querySelector('.container');
            container.insertBefore(alertContainer, container.firstChild);
        }
    }
    
    // Ne pas faire défiler vers le haut, la notification est maintenant fixe et visible
    
    // Configurer la fermeture automatique après 5 secondes
    setTimeout(() => {
        if (alertContainer.parentNode) {
            alertContainer.style.animation = 'fadeOut 0.5s';
            setTimeout(() => alertContainer.remove(), 500);
        }
    }, 5000);
    
    // Ajouter un gestionnaire d'événement pour le bouton de fermeture
    cardHeader.querySelector('.btn-close').addEventListener('click', () => {
        alertContainer.style.animation = 'fadeOut 0.5s';
        setTimeout(() => alertContainer.remove(), 500);
    });
}

/**
 * Met à jour l'affichage du stock d'un produit
 * @param {string} productId - ID du produit
 * @param {number} newStock - Nouveau stock
 */
function updateProductStock(productId, newStock) {
    const productCards = document.querySelectorAll('.sell-product');
    productCards.forEach(card => {
        if (card.getAttribute('data-product-id') === productId) {
            card.setAttribute('data-product-stock', newStock);
            
            // Mettre à jour le badge de stock
            const stockBadge = card.closest('.card').querySelector('.badge');
            if (stockBadge) {
                if (newStock > 0) {
                    if (newStock < 10) {
                        stockBadge.className = 'badge bg-warning text-dark';
                        stockBadge.textContent = `Stock faible: ${newStock}`;
                    } else {
                        stockBadge.className = 'badge bg-success';
                        stockBadge.textContent = `En stock: ${newStock}`;
                    }
                } else {
                    stockBadge.className = 'badge bg-danger';
                    stockBadge.textContent = 'Rupture de stock';
                    
                    // Désactiver le bouton de vente
                    card.className = 'btn btn-secondary';
                    card.disabled = true;
                    card.innerHTML = 'Rupture de stock';
                    card.removeAttribute('data-bs-toggle');
                    card.removeAttribute('data-bs-target');
                }
            }
        }
    });
}
