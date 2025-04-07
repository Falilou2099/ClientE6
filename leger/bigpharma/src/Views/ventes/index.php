<?php require_once TEMPLATES_PATH . '/header.php'; ?>

<div class="container mt-4">
    <div class="d-flex justify-content-between align-items-center mb-4">
        <h1>Gestion des ventes</h1>
        <a href="/bigpharma/ventes/nouvelle" class="btn btn-primary">
            <i class="fas fa-plus"></i> Nouvelle vente
        </a>
    </div>

    <?php if (isset($_SESSION['success'])): ?>
        <div class="alert alert-success">
            <?= htmlspecialchars($_SESSION['success']) ?>
            <?php unset($_SESSION['success']); ?>
        </div>
    <?php endif; ?>

    <?php if (isset($_SESSION['error'])): ?>
        <div class="alert alert-danger">
            <?= htmlspecialchars($_SESSION['error']) ?>
            <?php unset($_SESSION['error']); ?>
        </div>
    <?php endif; ?>

    <div class="card">
        <div class="card-body">
            <div class="table-responsive">
                <table class="table table-striped">
                    <thead>
                        <tr>
                            <th>N° Vente</th>
                            <th>Date</th>
                            <th>Client</th>
                            <th>Total</th>
                            <th>Actions</th>
                        </tr>
                    </thead>
                    <tbody>
                        <?php foreach ($ventes as $vente): ?>
                            <tr>
                                <td><?= htmlspecialchars($vente['id']) ?></td>
                                <td><?= htmlspecialchars(date('d/m/Y H:i', strtotime($vente['date_vente']))) ?></td>
                                <td><?= htmlspecialchars($vente['nom_client'] . ' ' . $vente['prenom_client']) ?></td>
                                <td><?= number_format($vente['total'], 2, ',', ' ') ?> €</td>
                                <td>
                                    <a href="/bigpharma/ventes/details/<?= $vente['id'] ?>" class="btn btn-info btn-sm">
                                        <i class="fas fa-eye"></i> Détails
                                    </a>
                                </td>
                            </tr>
                        <?php endforeach; ?>
                        <?php if (empty($ventes)): ?>
                            <tr>
                                <td colspan="5" class="text-center">Aucune vente enregistrée</td>
                            </tr>
                        <?php endif; ?>
                    </tbody>
                </table>
            </div>
        </div>
    </div>
</div>

<?php require_once TEMPLATES_PATH . '/footer.php'; ?>
