<?php
// Titre de la page
$pageTitle = "Détails du Client";

// Inclure l'en-tête
require_once 'C:\xampp\htdocs\bigpharma/templates/header.php';
?>

<div class="container mt-4">
    <div class="d-flex justify-content-between align-items-center mb-4">
        <h1>Détails du Client</h1>
        <div>
            <a href="/bigpharma/public/clients/edit/<?= $client->getId() ?>" class="btn btn-warning text-white">
                <i class="bi bi-pencil"></i> Modifier
            </a>
            <a href="/bigpharma/public/clients" class="btn btn-secondary ms-2">
                <i class="bi bi-arrow-left"></i> Retour à la liste
            </a>
        </div>
    </div>

    <div class="card">
        <div class="card-body">
            <div class="row">
                <div class="col-md-6">
                    <h5 class="card-title">Informations personnelles</h5>
                    <table class="table table-borderless">
                        <tr>
                            <th style="width: 150px;">ID:</th>
                            <td><?= htmlspecialchars($client->getId()) ?></td>
                        </tr>
                        <tr>
                            <th>Nom:</th>
                            <td><?= htmlspecialchars($client->getNom()) ?></td>
                        </tr>
                        <tr>
                            <th>Prénom:</th>
                            <td><?= htmlspecialchars($client->getPrenom()) ?></td>
                        </tr>
                        <tr>
                            <th>Nom complet:</th>
                            <td><?= htmlspecialchars($client->getNomComplet()) ?></td>
                        </tr>
                        <tr>
                            <th>Email:</th>
                            <td><?= htmlspecialchars($client->getEmail() ?: 'Non renseigné') ?></td>
                        </tr>
                        <tr>
                            <th>Téléphone:</th>
                            <td><?= htmlspecialchars($client->getTelephone() ?: 'Non renseigné') ?></td>
                        </tr>
                        <tr>
                            <th>Adresse:</th>
                            <td><?= nl2br(htmlspecialchars($client->getAdresse() ?: 'Non renseignée')) ?></td>
                        </tr>
                        <tr>
                            <th>Date de création:</th>
                            <td><?= htmlspecialchars(date('d/m/Y H:i', strtotime($client->getDateCreation()))) ?></td>
                        </tr>
                    </table>
                </div>
                
                <div class="col-md-6">
                    <h5 class="card-title">Historique des achats</h5>
                    <div class="alert alert-info">
                        Fonctionnalité à venir : Historique des achats du client.
                    </div>
                    
                    <!-- Ici, vous pourriez ajouter un tableau des achats récents du client -->
                </div>
            </div>
            
            <hr>
            
            <div class="d-flex justify-content-between mt-3">
                <button type="button" class="btn btn-danger" data-bs-toggle="modal" data-bs-target="#deleteModal">
                    <i class="bi bi-trash"></i> Supprimer ce client
                </button>
                
                <a href="/bigpharma/clients" class="btn btn-secondary">
                    <i class="bi bi-arrow-left"></i> Retour à la liste
                </a>
            </div>
        </div>
    </div>
</div>

<!-- Modal de confirmation de suppression -->
<div class="modal fade" id="deleteModal" tabindex="-1" aria-labelledby="deleteModalLabel" aria-hidden="true">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                <h5 class="modal-title" id="deleteModalLabel">Confirmer la suppression</h5>
                <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
            </div>
            <div class="modal-body">
                Êtes-vous sûr de vouloir supprimer le client <strong><?= htmlspecialchars($client->getNomComplet()) ?></strong> ?
                <p class="text-danger mt-2">Cette action est irréversible.</p>
            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Annuler</button>
                <form action="/bigpharma/clients/delete/<?= $client->getId() ?>" method="POST">
                    <button type="submit" class="btn btn-danger">Supprimer</button>
                </form>
            </div>
        </div>
    </div>
</div>

<?php
// Inclure le pied de page
require_once 'C:\xampp\htdocs\bigpharma/templates/footer.php';
?>
