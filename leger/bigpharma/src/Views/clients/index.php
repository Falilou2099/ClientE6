<?php require_once TEMPLATES_PATH . '/header.php'; ?>

<div class="container mt-4">
    <div class="d-flex justify-content-between align-items-center mb-4">
        <h1>Gestion des clients</h1>
        <a href="/bigpharma/clients/ajouter" class="btn btn-primary">
            <i class="fas fa-user-plus"></i> Nouveau client
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
                <table class="table table-striped" id="clientsTable">
                    <thead>
                        <tr>
                            <th>Nom</th>
                            <th>Prénom</th>
                            <th>Email</th>
                            <th>Téléphone</th>
                            <th>Actions</th>
                        </tr>
                    </thead>
                    <tbody>
                        <?php foreach ($clients as $client): ?>
                            <tr>
                                <td><?= htmlspecialchars($client['nom']) ?></td>
                                <td><?= htmlspecialchars($client['prenom']) ?></td>
                                <td><?= htmlspecialchars($client['email']) ?></td>
                                <td><?= htmlspecialchars($client['telephone']) ?></td>
                                <td>
                                    <div class="btn-group">
                                        <a href="/bigpharma/clients/details/<?= $client['id'] ?>" 
                                           class="btn btn-info btn-sm" title="Détails">
                                            <i class="fas fa-eye"></i>
                                        </a>
                                        <a href="/bigpharma/clients/modifier/<?= $client['id'] ?>" 
                                           class="btn btn-warning btn-sm" title="Modifier">
                                            <i class="fas fa-edit"></i>
                                        </a>
                                        <button type="button" 
                                                class="btn btn-danger btn-sm" 
                                                title="Supprimer"
                                                onclick="confirmerSuppression(<?= $client['id'] ?>)">
                                            <i class="fas fa-trash"></i>
                                        </button>
                                    </div>
                                </td>
                            </tr>
                        <?php endforeach; ?>
                        <?php if (empty($clients)): ?>
                            <tr>
                                <td colspan="5" class="text-center">Aucun client enregistré</td>
                            </tr>
                        <?php endif; ?>
                    </tbody>
                </table>
            </div>
        </div>
    </div>
</div>

<script>
function confirmerSuppression(id) {
    if (confirm('Êtes-vous sûr de vouloir supprimer ce client ?')) {
        window.location.href = '/bigpharma/clients/supprimer/' + id;
    }
}

document.addEventListener('DOMContentLoaded', function() {
    // Initialiser DataTables
    $('#clientsTable').DataTable({
        language: {
            url: '//cdn.datatables.net/plug-ins/1.10.24/i18n/French.json'
        },
        order: [[0, 'asc']],
        pageLength: 10,
        responsive: true
    });
});
</script>

<?php require_once TEMPLATES_PATH . '/footer.php'; ?>
