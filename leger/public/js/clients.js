/**
 * BigPharma - Gestion des clients
 * Ce fichier contient les fonctionnalités JavaScript pour la gestion des clients
 */

document.addEventListener('DOMContentLoaded', function() {
    // Initialisation des composants
    initSearchForm();
    initDeleteConfirmation();
    initFormValidation();
});

/**
 * Initialise le formulaire de recherche
 */
function initSearchForm() {
    const searchForm = document.getElementById('searchForm');
    const searchInput = document.getElementById('searchInput');
    
    if (searchForm) {
        searchForm.addEventListener('submit', function(e) {
            // Empêcher la soumission si le champ est vide
            if (searchInput && searchInput.value.trim() === '') {
                e.preventDefault();
                searchInput.focus();
            }
        });
    }
    
    // Recherche en temps réel (optionnel)
    if (searchInput) {
        searchInput.addEventListener('input', debounce(function() {
            if (this.value.trim().length >= 3) {
                // Vous pouvez implémenter une recherche AJAX ici
                // searchClients(this.value.trim());
            }
        }, 500));
    }
}

/**
 * Initialise les confirmations de suppression
 */
function initDeleteConfirmation() {
    const deleteButtons = document.querySelectorAll('[data-bs-target^="#deleteModal"]');
    
    deleteButtons.forEach(button => {
        button.addEventListener('click', function() {
            // Vous pouvez ajouter une logique supplémentaire ici si nécessaire
        });
    });
}

/**
 * Initialise la validation des formulaires
 */
function initFormValidation() {
    const forms = document.querySelectorAll('.needs-validation');
    
    forms.forEach(form => {
        form.addEventListener('submit', function(event) {
            if (!form.checkValidity()) {
                event.preventDefault();
                event.stopPropagation();
            }
            
            form.classList.add('was-validated');
        }, false);
    });
    
    // Validation spécifique pour le téléphone
    const phoneInputs = document.querySelectorAll('input[type="tel"]');
    
    phoneInputs.forEach(input => {
        input.addEventListener('input', function() {
            // Nettoyer le numéro de téléphone (garder uniquement les chiffres, espaces et certains caractères)
            this.value = this.value.replace(/[^\d\s+\-\.()]/g, '');
        });
    });
}

/**
 * Fonction utilitaire pour limiter le nombre d'appels à une fonction
 */
function debounce(func, wait) {
    let timeout;
    return function() {
        const context = this, args = arguments;
        clearTimeout(timeout);
        timeout = setTimeout(() => {
            func.apply(context, args);
        }, wait);
    };
}

/**
 * Recherche des clients via AJAX (à implémenter si nécessaire)
 */
function searchClients(query) {
    fetch(`/bigpharma/api/clients?search=${encodeURIComponent(query)}`)
        .then(response => {
            if (!response.ok) {
                throw new Error(`Erreur HTTP: ${response.status}`);
            }
            return response.json();
        })
        .then(data => {
            if (data.success) {
                // Mettre à jour l'interface avec les résultats
                updateClientsList(data.clients);
            } else {
                console.error('Erreur lors de la recherche:', data.message);
            }
        })
        .catch(error => {
            console.error('Erreur lors de la recherche de clients:', error);
        });
}

/**
 * Met à jour la liste des clients dans l'interface (à implémenter si nécessaire)
 */
function updateClientsList(clients) {
    const clientsList = document.querySelector('.table tbody');
    
    if (!clientsList) return;
    
    // Vider la liste actuelle
    clientsList.innerHTML = '';
    
    if (clients.length === 0) {
        // Afficher un message si aucun client n'est trouvé
        const row = document.createElement('tr');
        row.innerHTML = '<td colspan="6" class="text-center">Aucun client trouvé</td>';
        clientsList.appendChild(row);
        return;
    }
    
    // Ajouter chaque client à la liste
    clients.forEach(client => {
        const row = document.createElement('tr');
        row.innerHTML = `
            <td>${client.id}</td>
            <td>${client.nom}</td>
            <td>${client.prenom}</td>
            <td>${client.email || 'Non renseigné'}</td>
            <td>${client.telephone || 'Non renseigné'}</td>
            <td>
                <div class="btn-group" role="group">
                    <a href="/bigpharma/clients/show/${client.id}" class="btn btn-sm btn-info text-white">
                        <i class="bi bi-eye"></i>
                    </a>
                    <a href="/bigpharma/clients/edit/${client.id}" class="btn btn-sm btn-warning text-white">
                        <i class="bi bi-pencil"></i>
                    </a>
                    <button type="button" class="btn btn-sm btn-danger" data-bs-toggle="modal" data-bs-target="#deleteModal${client.id}">
                        <i class="bi bi-trash"></i>
                    </button>
                </div>
            </td>
        `;
        clientsList.appendChild(row);
    });
}
