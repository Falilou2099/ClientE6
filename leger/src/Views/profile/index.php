<div class="container mt-5">
    <div class="row">
        <div class="col-md-12">
            <div class="card">
                <div class="card-header bg-primary text-white">
                    <h2>Mon Profil</h2>
                </div>
                <div class="card-body">
                    <div class="row">
                        <div class="col-md-6">
                            <h4>Informations du compte</h4>
                            <p><strong>Email:</strong> <?= htmlspecialchars($userData['email']) ?></p>
                            <p><strong>Rôle:</strong> <?= htmlspecialchars(ucfirst($userData['role'])) ?></p>
                            <p><strong>Date d'inscription:</strong> <?= htmlspecialchars(date('d/m/Y', strtotime($userData['createdAt']))) ?></p>
                            <?php if ($userData['pharmacyName']): ?>
                                <p><strong>Pharmacie:</strong> <?= htmlspecialchars($userData['pharmacyName']) ?></p>
                            <?php endif; ?>
                        </div>
                        <div class="col-md-6">
                            <h4>Accès aux applications</h4>
                            <p><strong>Type d'accès:</strong> 
                                <?php 
                                switch($userData['appAccess']) {
                                    case 'both':
                                        echo 'Application Web et Java';
                                        break;
                                    case 'light':
                                        echo 'Application Web uniquement';
                                        break;
                                    case 'heavy':
                                        echo 'Application Java uniquement';
                                        break;
                                    default:
                                        echo 'Non défini';
                                }
                                ?>
                            </p>
                            <p><strong>Statut du compte:</strong> 
                                <?php if ($userData['status'] === 'active'): ?>
                                    <span class="badge bg-success">Actif</span>
                                <?php else: ?>
                                    <span class="badge bg-danger">Inactif</span>
                                <?php endif; ?>
                            </p>
                            <?php if ($userData['lastLogin']): ?>
                                <p><strong>Dernière connexion:</strong> <?= htmlspecialchars(date('d/m/Y H:i', strtotime($userData['lastLogin']))) ?></p>
                            <?php endif; ?>
                        </div>
                    </div>
                    
                    <div class="row mt-4">
                        <div class="col-md-12">
                            <button type="button" class="btn btn-primary" id="showProfileModal">
                                Afficher les détails du compte
                            </button>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>

<!-- Modal pour afficher les informations du compte -->
<div class="modal fade" id="profileModal" tabindex="-1" aria-labelledby="profileModalLabel" aria-hidden="true">
    <div class="modal-dialog modal-lg">
        <div class="modal-content">
            <div class="modal-header bg-primary text-white">
                <h5 class="modal-title" id="profileModalLabel">Détails du compte</h5>
                <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
            </div>
            <div class="modal-body">
                <div class="row">
                    <div class="col-md-6">
                        <h5>Informations personnelles</h5>
                        <table class="table table-striped">
                            <tr>
                                <th>ID Utilisateur:</th>
                                <td><?= htmlspecialchars($userData['id']) ?></td>
                            </tr>
                            <tr>
                                <th>Email:</th>
                                <td><?= htmlspecialchars($userData['email']) ?></td>
                            </tr>
                            <tr>
                                <th>Rôle:</th>
                                <td><?= htmlspecialchars(ucfirst($userData['role'])) ?></td>
                            </tr>
                            <tr>
                                <th>Statut:</th>
                                <td>
                                    <?php if ($userData['status'] === 'active'): ?>
                                        <span class="badge bg-success">Actif</span>
                                    <?php else: ?>
                                        <span class="badge bg-danger">Inactif</span>
                                    <?php endif; ?>
                                </td>
                            </tr>
                        </table>
                    </div>
                    <div class="col-md-6">
                        <h5>Informations de la pharmacie</h5>
                        <?php if ($userData['pharmacyName']): ?>
                            <table class="table table-striped">
                                <tr>
                                    <th>Nom de la pharmacie:</th>
                                    <td><?= htmlspecialchars($userData['pharmacyName']) ?></td>
                                </tr>
                                <tr>
                                    <th>ID Pharmacie:</th>
                                    <td><?= htmlspecialchars($pharmacyId) ?></td>
                                </tr>
                            </table>
                        <?php else: ?>
                            <div class="alert alert-warning">
                                Aucune pharmacie associée à ce compte.
                            </div>
                        <?php endif; ?>
                    </div>
                </div>
                <div class="row mt-3">
                    <div class="col-md-12">
                        <h5>Informations d'accès</h5>
                        <table class="table table-striped">
                            <tr>
                                <th>Type d'accès:</th>
                                <td>
                                    <?php 
                                    switch($userData['appAccess']) {
                                        case 'both':
                                            echo 'Application Web et Java';
                                            break;
                                        case 'light':
                                            echo 'Application Web uniquement';
                                            break;
                                        case 'heavy':
                                            echo 'Application Java uniquement';
                                            break;
                                        default:
                                            echo 'Non défini';
                                    }
                                    ?>
                                </td>
                            </tr>
                            <tr>
                                <th>Date d'inscription:</th>
                                <td><?= htmlspecialchars(date('d/m/Y H:i', strtotime($userData['createdAt']))) ?></td>
                            </tr>
                            <?php if ($userData['lastLogin']): ?>
                                <tr>
                                    <th>Dernière connexion:</th>
                                    <td><?= htmlspecialchars(date('d/m/Y H:i', strtotime($userData['lastLogin']))) ?></td>
                                </tr>
                            <?php endif; ?>
                        </table>
                    </div>
                </div>
            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Fermer</button>
            </div>
        </div>
    </div>
</div>

<script>
    // Script pour afficher la modal au chargement de la page
    document.addEventListener('DOMContentLoaded', function() {
        // Récupérer le bouton et la modal
        const showProfileModalBtn = document.getElementById('showProfileModal');
        const profileModal = new bootstrap.Modal(document.getElementById('profileModal'));
        
        // Afficher la modal au clic sur le bouton
        showProfileModalBtn.addEventListener('click', function() {
            profileModal.show();
        });
        
        // Afficher automatiquement la modal au chargement de la page
        profileModal.show();
    });
</script>
