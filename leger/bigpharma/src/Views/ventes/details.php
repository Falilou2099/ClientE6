<?php require_once TEMPLATES_PATH . '/header.php'; ?>

<div class="container mt-4">
    <div class="d-flex justify-content-between align-items-center mb-4">
        <h1>Détails de la vente #<?= htmlspecialchars($vente['id']) ?></h1>
        <a href="/bigpharma/ventes" class="btn btn-secondary">
            <i class="fas fa-arrow-left"></i> Retour aux ventes
        </a>
    </div>

    <div class="row">
        <div class="col-md-6 mb-4">
            <div class="card">
                <div class="card-header">
                    <h5 class="card-title mb-0">Informations de la vente</h5>
                </div>
                <div class="card-body">
                    <p><strong>Date :</strong> <?= htmlspecialchars(date('d/m/Y H:i', strtotime($vente['date_vente']))) ?></p>
                    <p><strong>Client :</strong> <?= htmlspecialchars($client['nom'] . ' ' . $client['prenom']) ?></p>
                    <p><strong>Total :</strong> <?= number_format($vente['total'], 2, ',', ' ') ?> €</p>
                </div>
            </div>
        </div>

        <div class="col-md-6 mb-4">
            <div class="card">
                <div class="card-header">
                    <h5 class="card-title mb-0">Informations du client</h5>
                </div>
                <div class="card-body">
                    <p><strong>Email :</strong> <?= htmlspecialchars($client['email']) ?></p>
                    <p><strong>Téléphone :</strong> <?= htmlspecialchars($client['telephone']) ?></p>
                </div>
            </div>
        </div>
    </div>

    <div class="card">
        <div class="card-header">
            <h5 class="card-title mb-0">Produits vendus</h5>
        </div>
        <div class="card-body">
            <div class="table-responsive">
                <table class="table">
                    <thead>
                        <tr>
                            <th>Produit</th>
                            <th>Prix unitaire</th>
                            <th>Quantité</th>
                            <th>Total</th>
                        </tr>
                    </thead>
                    <tbody>
                        <?php foreach ($details as $detail): ?>
                            <tr>
                                <td><?= htmlspecialchars($detail['nom_produit']) ?></td>
                                <td><?= number_format($detail['prix_unitaire'], 2, ',', ' ') ?> €</td>
                                <td><?= htmlspecialchars($detail['quantite']) ?></td>
                                <td><?= number_format($detail['prix_unitaire'] * $detail['quantite'], 2, ',', ' ') ?> €</td>
                            </tr>
                        <?php endforeach; ?>
                    </tbody>
                    <tfoot>
                        <tr>
                            <td colspan="3" class="text-end"><strong>Total</strong></td>
                            <td><strong><?= number_format($vente['total'], 2, ',', ' ') ?> €</strong></td>
                        </tr>
                    </tfoot>
                </table>
            </div>
        </div>
    </div>
</div>

<?php require_once TEMPLATES_PATH . '/footer.php'; ?>
