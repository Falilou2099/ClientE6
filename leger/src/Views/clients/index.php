<?php
// Titre de la page
$pageTitle = "Liste des Clients";

// Inclure l'en-tête
require_once 'C:\xampp\htdocs\bigpharma/templates/header.php';
?>

<div class="container mt-4">
    <div class="d-flex justify-content-between align-items-center mb-4">
        <h1>Liste des Clients</h1>
        <a href="/bigpharma/public/clients/create" class="btn btn-primary">
            <i class="bi bi-plus-circle"></i> Ajouter un client
        </a>
    </div>

    <?php if (isset($_SESSION['success'])): ?>
        <div class="alert alert-success alert-dismissible fade show" role="alert">
            <?= htmlspecialchars($_SESSION['success']) ?>
            <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
        </div>
        <?php unset($_SESSION['success']); ?>
    <?php endif; ?>

    <?php if (isset($_SESSION['errors'])): ?>
        <div class="alert alert-danger alert-dismissible fade show" role="alert">
            <ul class="mb-0">
                <?php foreach ($_SESSION['errors'] as $error): ?>
                    <li><?= htmlspecialchars($error) ?></li>
                <?php endforeach; ?>
            </ul>
            <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
        </div>
        <?php unset($_SESSION['errors']); ?>
    <?php endif; ?>

    <?php if (isset($error)): ?>
        <div class="alert alert-danger alert-dismissible fade show" role="alert">
            <?= htmlspecialchars($error) ?>
            <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
        </div>
    <?php endif; ?>

    <!-- Formulaire de recherche -->
    <div class="card mb-4">
        <div class="card-body">
            <form action="/bigpharma/public/clients" method="GET" class="row g-3">
                <div class="col-md-8">
                    <div class="input-group">
                        <input type="text" name="search" class="form-control" placeholder="Rechercher un client..." value="<?= htmlspecialchars($search ?? '') ?>">
                        <button class="btn btn-outline-primary" type="submit">
                            <i class="bi bi-search"></i> Rechercher
                        </button>
                    </div>
                </div>
                <?php if (!empty($search)): ?>
                    <div class="col-md-4">
                        <a href="/bigpharma/public/clients" class="btn btn-outline-secondary">
                            <i class="bi bi-x-circle"></i> Effacer la recherche
                        </a>
                    </div>
                <?php endif; ?>
            </form>
        </div>
    </div>

    <!-- Liste des clients -->
    <div class="card">
        <div class="card-body">
            <?php if (isset($clients) && !empty($clients)): ?>
                <div class="table-responsive">
                    <table class="table table-striped table-hover">
                        <thead>
                            <tr>
                                <th>ID</th>
                                <th>Nom</th>
                                <th>Prénom</th>
                                <th>Email</th>
                                <th>Téléphone</th>
                                <th>Actions</th>
                            </tr>
                        </thead>
                        <tbody>
                            <?php foreach ($clients as $client): ?>
                                <tr data-client-id="<?= $client->getId() ?>">
                                    <td><?= htmlspecialchars($client->getId()) ?></td>
                                    <td><?= htmlspecialchars($client->getNom()) ?></td>
                                    <td><?= htmlspecialchars($client->getPrenom()) ?></td>
                                    <td><?= htmlspecialchars($client->getEmail() ?: 'Non renseigné') ?></td>
                                    <td><?= htmlspecialchars($client->getTelephone() ?: 'Non renseigné') ?></td>
                                    <td>
                                        <div class="btn-group" role="group">
                                            <button type="button" class="btn btn-sm btn-info text-white" data-bs-toggle="modal" data-bs-target="#viewModal<?= $client->getId() ?>">
                                                <i class="bi bi-eye-fill"></i> Voir
                                            </button>
                                            <button type="button" class="btn btn-sm btn-warning text-white" data-bs-toggle="modal" data-bs-target="#editModal<?= $client->getId() ?>">
                                                <i class="bi bi-pencil-fill"></i> Modifier
                                            </button>
                                            <button type="button" class="btn btn-sm btn-danger" data-bs-toggle="modal" data-bs-target="#deleteModal<?= $client->getId() ?>">
                                                <i class="bi bi-trash-fill"></i> Supprimer
                                            </button>
                                        </div>

                                        <!-- Modal de consultation -->
                                        <div class="modal fade" id="viewModal<?= $client->getId() ?>" tabindex="-1" aria-labelledby="viewModalLabel<?= $client->getId() ?>" aria-hidden="true">
                                            <div class="modal-dialog">
                                                <div class="modal-content">
                                                    <div class="modal-header">
                                                        <h5 class="modal-title" id="viewModalLabel<?= $client->getId() ?>">Informations du client</h5>
                                                        <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
                                                    </div>
                                                    <div class="modal-body">
                                                        <div class="card mb-3">
                                                            <div class="card-header bg-primary text-white">
                                                                <h5 class="mb-0">Informations personnelles</h5>
                                                            </div>
                                                            <div class="card-body">
                                                                <p><strong>Nom :</strong> <span class="client-info" data-field="nom"><?= htmlspecialchars($client->getNom()) ?></span></p>
                                                                <p><strong>Prénom :</strong> <span class="client-info" data-field="prenom"><?= htmlspecialchars($client->getPrenom()) ?></span></p>
                                                                <p><strong>Email :</strong> <span class="client-info" data-field="email"><?= htmlspecialchars($client->getEmail() ?: 'Non renseigné') ?></span></p>
                                                                <p><strong>Téléphone :</strong> <span class="client-info" data-field="telephone"><?= htmlspecialchars($client->getTelephone() ?: 'Non renseigné') ?></span></p>
                                                                <p><strong>Adresse :</strong> <span class="client-info" data-field="adresse"><?= htmlspecialchars($client->getAdresse() ?: 'Non renseignée') ?></span></p>
                                                            </div>
                                                        </div>
                                                    </div>
                                                    <div class="modal-footer">
                                                        <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Fermer</button>
                                                    </div>
                                                </div>
                                            </div>
                                        </div>

                                        <!-- Modal de modification -->
                                        <div class="modal fade" id="editModal<?= $client->getId() ?>" tabindex="-1" aria-labelledby="editModalLabel<?= $client->getId() ?>" aria-hidden="true">
                                            <div class="modal-dialog">
                                                <div class="modal-content">
                                                    <div class="modal-header">
                                                        <h5 class="modal-title" id="editModalLabel<?= $client->getId() ?>">Modifier le client</h5>
                                                        <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
                                                    </div>
                                                    <div class="modal-body">
                                                        <form action="/bigpharma/public/clients/update/<?= $client->getId() ?>" method="POST" id="editForm<?= $client->getId() ?>" class="ajax-form">
                                                            <div class="mb-3">
                                                                <label for="nom<?= $client->getId() ?>" class="form-label">Nom <span class="text-danger">*</span></label>
                                                                <input type="text" id="nom<?= $client->getId() ?>" name="nom" class="form-control" value="<?= htmlspecialchars($client->getNom()) ?>" required>
                                                                <div class="invalid-feedback">Le nom est obligatoire.</div>
                                                            </div>
                                                            <div class="mb-3">
                                                                <label for="prenom<?= $client->getId() ?>" class="form-label">Prénom <span class="text-danger">*</span></label>
                                                                <input type="text" id="prenom<?= $client->getId() ?>" name="prenom" class="form-control" value="<?= htmlspecialchars($client->getPrenom()) ?>" required>
                                                                <div class="invalid-feedback">Le prénom est obligatoire.</div>
                                                            </div>
                                                            <div class="mb-3">
                                                                <label for="email<?= $client->getId() ?>" class="form-label">Email</label>
                                                                <input type="email" id="email<?= $client->getId() ?>" name="email" class="form-control" value="<?= htmlspecialchars($client->getEmail() ?: '') ?>">
                                                                <div class="invalid-feedback">Veuillez entrer une adresse email valide.</div>
                                                            </div>
                                                            <div class="mb-3">
                                                                <label for="telephone<?= $client->getId() ?>" class="form-label">Téléphone</label>
                                                                <input type="text" id="telephone<?= $client->getId() ?>" name="telephone" class="form-control" value="<?= htmlspecialchars($client->getTelephone() ?: '') ?>">
                                                            </div>
                                                            <div class="mb-3">
                                                                <label for="adresse<?= $client->getId() ?>" class="form-label">Adresse</label>
                                                                <textarea id="adresse<?= $client->getId() ?>" name="adresse" class="form-control" rows="3"><?= htmlspecialchars($client->getAdresse() ?: '') ?></textarea>
                                                            </div>
                                                            <div class="alert alert-danger d-none" role="alert" id="editFormErrors<?= $client->getId() ?>"></div>
                                                        </form>
                                                    </div>
                                                    <div class="modal-footer">
                                                        <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Annuler</button>
                                                        <button type="button" class="btn btn-primary" onclick="submitClientForm('editForm<?= $client->getId() ?>', <?= $client->getId() ?>);">Enregistrer les modifications</button>
                                                    </div>
                                                </div>
                                            </div>
                                        </div>

                                        <!-- Modal de confirmation de suppression -->
                                        <div class="modal fade" id="deleteModal<?= $client->getId() ?>" tabindex="-1" aria-labelledby="deleteModalLabel<?= $client->getId() ?>" aria-hidden="true">
                                            <div class="modal-dialog">
                                                <div class="modal-content">
                                                    <div class="modal-header">
                                                        <h5 class="modal-title" id="deleteModalLabel<?= $client->getId() ?>">Confirmer la suppression</h5>
                                                        <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
                                                    </div>
                                                    <div class="modal-body">
                                                        Êtes-vous sûr de vouloir supprimer le client <strong><?= htmlspecialchars($client->getNomComplet()) ?></strong> ?
                                                        <p class="text-danger mt-2">Cette action est irréversible.</p>
                                                    </div>
                                                    <div class="modal-footer">
                                                        <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Annuler</button>
                                                        <form action="/bigpharma/public/clients/delete/<?= $client->getId() ?>" method="POST">
                                                            <button type="submit" class="btn btn-danger">Supprimer</button>
                                                        </form>
                                                    </div>
                                                </div>
                                            </div>
                                        </div>
                                    </td>
                                </tr>
                            <?php endforeach; ?>
                        </tbody>
                    </table>
                </div>



            <?php else: ?>
                <div class="alert alert-info">
                    <?php if (!empty($search)): ?>
                        Aucun client ne correspond à votre recherche "<?= htmlspecialchars($search) ?>".
                    <?php else: ?>
                        Aucun client n'est enregistré pour le moment.
                    <?php endif; ?>
                </div>
            <?php endif; ?>
        </div>
        
        <!-- Pagination (déplacée en bas) -->
        <?php if (isset($totalPages) && $totalPages > 1): ?>
            <div class="card-footer bg-white">
                <nav aria-label="Navigation des pages" class="mt-2">
                    <ul class="pagination pagination-sm justify-content-center mb-0">
                        <li class="page-item <?= ($page <= 1) ? 'disabled' : '' ?>">
                            <a class="page-link" href="/bigpharma/public/clients?page=1<?= !empty($search) ? '&search=' . urlencode($search) : '' ?>" aria-label="Première page">
                                <span aria-hidden="true">&laquo;&laquo;</span>
                            </a>
                        </li>
                        <li class="page-item <?= ($page <= 1) ? 'disabled' : '' ?>">
                            <a class="page-link" href="/bigpharma/public/clients?page=<?= max(1, $page - 1) ?><?= !empty($search) ? '&search=' . urlencode($search) : '' ?>" aria-label="Précédent">
                                <span aria-hidden="true">&laquo;</span>
                            </a>
                        </li>
                        
                        <?php 
                        // Afficher un nombre limité de pages avec ellipsis
                        $startPage = max(1, $page - 2);
                        $endPage = min($totalPages, $page + 2);
                        
                        // Assurer qu'on affiche au moins 5 pages si possible
                        if ($endPage - $startPage + 1 < 5) {
                            if ($startPage == 1) {
                                $endPage = min($totalPages, $startPage + 4);
                            } elseif ($endPage == $totalPages) {
                                $startPage = max(1, $endPage - 4);
                            }
                        }
                        
                        // Afficher ellipsis au début si nécessaire
                        if ($startPage > 1): ?>
                            <li class="page-item">
                                <a class="page-link" href="/bigpharma/public/clients?page=1<?= !empty($search) ? '&search=' . urlencode($search) : '' ?>">1</a>
                            </li>
                            <?php if ($startPage > 2): ?>
                                <li class="page-item disabled"><span class="page-link">...</span></li>
                            <?php endif; ?>
                        <?php endif; ?>
                        
                        <?php for ($i = $startPage; $i <= $endPage; $i++): ?>
                            <li class="page-item <?= $i === $page ? 'active' : '' ?>">
                                <a class="page-link" href="/bigpharma/public/clients?page=<?= $i ?><?= !empty($search) ? '&search=' . urlencode($search) : '' ?>">
                                    <?= $i ?>
                                </a>
                            </li>
                        <?php endfor; ?>
                        
                        <!-- Afficher ellipsis à la fin si nécessaire -->
                        <?php if ($endPage < $totalPages): ?>
                            <?php if ($endPage < $totalPages - 1): ?>
                                <li class="page-item disabled"><span class="page-link">...</span></li>
                            <?php endif; ?>
                            <li class="page-item">
                                <a class="page-link" href="/bigpharma/public/clients?page=<?= $totalPages ?><?= !empty($search) ? '&search=' . urlencode($search) : '' ?>"><?= $totalPages ?></a>
                            </li>
                        <?php endif; ?>
                        
                        <li class="page-item <?= ($page >= $totalPages) ? 'disabled' : '' ?>">
                            <a class="page-link" href="/bigpharma/public/clients?page=<?= min($totalPages, $page + 1) ?><?= !empty($search) ? '&search=' . urlencode($search) : '' ?>" aria-label="Suivant">
                                <span aria-hidden="true">&raquo;</span>
                            </a>
                        </li>
                        <li class="page-item <?= ($page >= $totalPages) ? 'disabled' : '' ?>">
                            <a class="page-link" href="/bigpharma/public/clients?page=<?= $totalPages ?><?= !empty($search) ? '&search=' . urlencode($search) : '' ?>" aria-label="Dernière page">
                                <span aria-hidden="true">&raquo;&raquo;</span>
                            </a>
                        </li>
                    </ul>
                </nav>
                <div class="text-center text-muted small mt-2">
                    Page <?= $page ?> sur <?= $totalPages ?> (<?= $totalClients ?> client<?= $totalClients > 1 ? 's' : '' ?>)
                </div>
            </div>
        <?php endif; ?>
    </div>
</div>

<script>
// Fonction pour afficher un toast de confirmation
function showToast(message, type = 'success') {
    // Créer l'élément toast
    const toastContainer = document.createElement('div');
    toastContainer.className = 'position-fixed bottom-0 end-0 p-3';
    toastContainer.style.zIndex = '11';
    
    const toastElement = document.createElement('div');
    toastElement.className = `toast align-items-center text-white bg-${type} border-0`;
    toastElement.setAttribute('role', 'alert');
    toastElement.setAttribute('aria-live', 'assertive');
    toastElement.setAttribute('aria-atomic', 'true');
    
    const toastFlex = document.createElement('div');
    toastFlex.className = 'd-flex';
    
    const toastBody = document.createElement('div');
    toastBody.className = 'toast-body';
    toastBody.textContent = message;
    
    const closeButton = document.createElement('button');
    closeButton.type = 'button';
    closeButton.className = 'btn-close btn-close-white me-2 m-auto';
    closeButton.setAttribute('data-bs-dismiss', 'toast');
    closeButton.setAttribute('aria-label', 'Close');
    
    // Assembler les éléments
    toastFlex.appendChild(toastBody);
    toastFlex.appendChild(closeButton);
    toastElement.appendChild(toastFlex);
    toastContainer.appendChild(toastElement);
    document.body.appendChild(toastContainer);
    
    // Initialiser et afficher le toast
    const toast = new bootstrap.Toast(toastElement, { delay: 5000 });
    toast.show();
    
    // Supprimer le toast après qu'il soit caché
    toastElement.addEventListener('hidden.bs.toast', function () {
        document.body.removeChild(toastContainer);
    });
}

// Fonction pour mettre à jour une ligne du tableau des clients
function updateClientRow(clientId, clientData) {
    // Trouver la ligne du client dans le tableau
    const row = document.querySelector(`tr[data-client-id="${clientId}"]`);
    if (!row) return;
    
    // Mettre à jour les cellules du tableau
    const cells = row.querySelectorAll('td');
    cells[1].textContent = clientData.nom;
    cells[2].textContent = clientData.prenom;
    cells[3].textContent = clientData.email;
    cells[4].textContent = clientData.telephone;
    
    // Mettre à jour les données dans le modal de consultation
    const viewModal = document.getElementById('viewModal' + clientId);
    if (viewModal) {
        const modalBody = viewModal.querySelector('.modal-body');
        const infoElements = modalBody.querySelectorAll('.client-info');
        
        // Mettre à jour les informations
        for (let el of infoElements) {
            if (el.dataset.field === 'nom') el.textContent = clientData.nom;
            if (el.dataset.field === 'prenom') el.textContent = clientData.prenom;
            if (el.dataset.field === 'email') el.textContent = clientData.email;
            if (el.dataset.field === 'telephone') el.textContent = clientData.telephone;
            if (el.dataset.field === 'adresse') el.textContent = clientData.adresse;
        }
    }
}

// Fonction pour soumettre un formulaire en AJAX
function submitClientForm(formId, clientId) {
    const form = document.getElementById(formId);
    const formData = new FormData(form);
    const errorContainer = document.getElementById('editFormErrors' + clientId);
    
    // Ajouter les en-têtes pour indiquer que c'est une requête AJAX
    const headers = new Headers({
        'X-Requested-With': 'XMLHttpRequest'
    });
    
    // Désactiver les boutons pendant la soumission
    const submitButton = form.closest('.modal-content').querySelector('.btn-primary');
    const cancelButton = form.closest('.modal-content').querySelector('.btn-secondary');
    submitButton.disabled = true;
    cancelButton.disabled = true;
    submitButton.innerHTML = '<span class="spinner-border spinner-border-sm" role="status" aria-hidden="true"></span> Enregistrement...';
    
    // Envoyer la requête AJAX
    fetch(form.action, {
        method: 'POST',
        headers: headers,
        body: formData
    })
    .then(response => {
        if (!response.ok) {
            throw new Error('Erreur réseau');
        }
        return response.json();
    })
    .then(data => {
        // Réactiver les boutons
        submitButton.disabled = false;
        cancelButton.disabled = false;
        submitButton.innerHTML = 'Enregistrer les modifications';
        
        if (data.success) {
            // Fermer le modal
            const modal = bootstrap.Modal.getInstance(document.getElementById('editModal' + clientId));
            modal.hide();
            
            // Mettre à jour les données dans le tableau
            updateClientRow(clientId, data.data.client);
            
            // Afficher un message de succès
            showToast(data.message, 'success');
        } else {
            // Afficher les erreurs
            errorContainer.innerHTML = '';
            if (data.data && data.data.errors) {
                errorContainer.innerHTML = '<ul class="mb-0">';
                data.data.errors.forEach(error => {
                    errorContainer.innerHTML += `<li>${error}</li>`;
                });
                errorContainer.innerHTML += '</ul>';
            } else {
                errorContainer.innerHTML = data.message;
            }
            errorContainer.classList.remove('d-none');
        }
    })
    .catch(error => {
        console.error('Erreur:', error);
        // Réactiver les boutons
        submitButton.disabled = false;
        cancelButton.disabled = false;
        submitButton.innerHTML = 'Enregistrer les modifications';
        
        // Afficher un message d'erreur générique
        errorContainer.innerHTML = 'Une erreur est survenue lors de la communication avec le serveur.';
        errorContainer.classList.remove('d-none');
        console.error('Erreur:', error);
    });
}

// Fonction pour mettre à jour la ligne du client dans le tableau
function updateClientRow(clientId, clientData) {
    const row = document.querySelector(`tr[data-client-id="${clientId}"]`);
    if (row) {
        // Mettre à jour les cellules du tableau
        const cells = row.querySelectorAll('td');
        cells[1].textContent = clientData.nom;
        cells[2].textContent = clientData.prenom;
        cells[3].textContent = clientData.email || 'Non renseigné';
        cells[4].textContent = clientData.telephone || 'Non renseigné';
        
        // Mettre à jour les données dans le modal de consultation
        const viewModal = document.getElementById('viewModal' + clientId);
        if (viewModal) {
            const infoFields = viewModal.querySelectorAll('.card-body p');
            infoFields[0].innerHTML = `<strong>Nom :</strong> ${clientData.nom}`;
            infoFields[1].innerHTML = `<strong>Prénom :</strong> ${clientData.prenom}`;
            infoFields[2].innerHTML = `<strong>Email :</strong> ${clientData.email || 'Non renseigné'}`;
            infoFields[3].innerHTML = `<strong>Téléphone :</strong> ${clientData.telephone || 'Non renseigné'}`;
            infoFields[4].innerHTML = `<strong>Adresse :</strong> ${clientData.adresse || 'Non renseignée'}`;
        }
    }
}

// Écouter les soumissions de formulaires pour afficher des confirmations
document.addEventListener('DOMContentLoaded', function() {
    // Formulaires d'ajout
    const addForms = document.querySelectorAll('form[action*="/clients/store"]');
    addForms.forEach(form => {
        form.addEventListener('submit', function(e) {
            // Stocker l'action dans sessionStorage pour afficher un message après redirection
            sessionStorage.setItem('clientAction', 'add');
        });
    });
    
    // Formulaires de suppression
    const deleteForms = document.querySelectorAll('form[action*="/clients/delete"]');
    deleteForms.forEach(form => {
        form.addEventListener('submit', function(e) {
            // Stocker l'action dans sessionStorage pour afficher un message après redirection
            sessionStorage.setItem('clientAction', 'delete');
        });
    });
    
    // Vérifier s'il y a une action stockée dans sessionStorage
    const clientAction = sessionStorage.getItem('clientAction');
    if (clientAction) {
        switch(clientAction) {
            case 'add':
                showToast('Le client a été ajouté avec succès !', 'success');
                break;
            case 'edit':
                showToast('Les informations du client ont été mises à jour avec succès !', 'success');
                break;
            case 'delete':
                showToast('Le client a été supprimé avec succès !', 'danger');
                break;
        }
        // Supprimer l'action après affichage
        sessionStorage.removeItem('clientAction');
    }
});
</script>

<?php
// Inclure le pied de page
require_once 'C:\xampp\htdocs\bigpharma/templates/footer.php';
?>
