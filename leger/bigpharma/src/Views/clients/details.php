<?php require_once TEMPLATES_PATH . '/header.php'; ?>

<div class="container mt-4">
    <div class="d-flex justify-content-between align-items-center mb-4">
        <h1>Détails du client</h1>
        <div>
            <a href="/bigpharma/clients/modifier/<?= $client['id'] ?>" class="btn btn-warning">
                <i class="fas fa-edit"></i> Modifier
            </a>
            <a href="/bigpharma/clients" class="btn btn-secondary">
                <i class="fas fa-arrow-left"></i> Retour à la liste
            </a>
        </div>
    </div>

    <div class="row">
        <!-- Informations du client -->
        <div class="col-md-6 mb-4">
            <div class="card">
                <div class="card-header">
                    <h5 class="card-title mb-0">Informations personnelles</h5>
                </div>
                <div class="card-body">
                    <p><strong>Nom :</strong> <?= htmlspecialchars($client['nom']) ?></p>
                    <p><strong>Prénom :</strong> <?= htmlspecialchars($client['prenom']) ?></p>
                    <p><strong>Email :</strong> <?= htmlspecialchars($client['email']) ?></p>
                    <p><strong>Téléphone :</strong> <?= htmlspecialchars($client['telephone']) ?></p>
                    <p><strong>Client depuis :</strong> <?= date('d/m/Y', strtotime($client['date_creation'])) ?></p>
                </div>
            </div>
        </div>

        <!-- Statistiques d'achats -->
        <div class="col-md-6 mb-4">
            <?php 
            $stats = $clientModel->getStatistiquesClient($client['id']);
            ?>
            <div class="card">
                <div class="card-header">
                    <h5 class="card-title mb-0">Statistiques d'achats</h5>
                </div>
                <div class="card-body">
                    <div class="row">
                        <div class="col-6 mb-3">
                            <div class="border rounded p-3 text-center">
                                <h6>Nombre d'achats</h6>
                                <h3><?= $stats['nombre_achats'] ?? 0 ?></h3>
                            </div>
                        </div>
                        <div class="col-6 mb-3">
                            <div class="border rounded p-3 text-center">
                                <h6>Total des achats</h6>
                                <h3><?= number_format($stats['total_achats'] ?? 0, 2, ',', ' ') ?> €</h3>
                            </div>
                        </div>
                        <div class="col-6">
                            <div class="border rounded p-3 text-center">
                                <h6>Moyenne par achat</h6>
                                <h3><?= number_format($stats['moyenne_achats'] ?? 0, 2, ',', ' ') ?> €</h3>
                            </div>
                        </div>
                        <div class="col-6">
                            <div class="border rounded p-3 text-center">
                                <h6>Dernier achat</h6>
                                <h3><?= $stats['dernier_achat'] ? date('d/m/Y', strtotime($stats['dernier_achat'])) : 'Aucun' ?></h3>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>

    <!-- Historique des achats -->
    <div class="card mb-4">
        <div class="card-header">
            <h5 class="card-title mb-0">Historique des achats</h5>
        </div>
        <div class="card-body">
            <div class="table-responsive">
                <table class="table" id="historiqueTable">
                    <thead>
                        <tr>
                            <th>Date</th>
                            <th>Produit</th>
                            <th>Quantité</th>
                            <th>Prix unitaire</th>
                            <th>Total</th>
                        </tr>
                    </thead>
                    <tbody>
                        <?php foreach ($historique_achats as $achat): ?>
                            <tr>
                                <td><?= date('d/m/Y H:i', strtotime($achat['date_vente'])) ?></td>
                                <td><?= htmlspecialchars($achat['nom_produit']) ?></td>
                                <td><?= $achat['quantite'] ?></td>
                                <td><?= number_format($achat['prix_unitaire'], 2, ',', ' ') ?> €</td>
                                <td><?= number_format($achat['prix_unitaire'] * $achat['quantite'], 2, ',', ' ') ?> €</td>
                            </tr>
                        <?php endforeach; ?>
                        <?php if (empty($historique_achats)): ?>
                            <tr>
                                <td colspan="5" class="text-center">Aucun achat enregistré</td>
                            </tr>
                        <?php endif; ?>
                    </tbody>
                </table>
            </div>
        </div>
    </div>
</div>

<script>
document.addEventListener('DOMContentLoaded', function() {
    // Initialiser DataTables pour l'historique
    $('#historiqueTable').DataTable({
        language: {
            url: '//cdn.datatables.net/plug-ins/1.10.24/i18n/French.json'
        },
        order: [[0, 'desc']],
        pageLength: 10,
        responsive: true
    });
});
</script>

<?php require_once TEMPLATES_PATH . '/footer.php'; ?>
