<?php
// Titre de la page
$pageTitle = "Historique des Ventes";

// Inclure l'en-tête
require_once 'C:\xampp\htdocs\bigpharma/templates/header.php';
?>

<div class="container mt-4">
    <div class="d-flex justify-content-between align-items-center mb-4">
        <h1>Historique des Ventes</h1>
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

    <div class="alert alert-info text-center p-5 my-5">
        <i class="bi bi-tools" style="font-size: 3rem;"></i>
        <h2 class="mt-3">Fonctionnalité en cours de développement</h2>
        <p class="lead">L'historique des ventes est actuellement en cours de développement.</p>
        <p>Notre équipe travaille activement pour mettre en place cette fonctionnalité.</p>
        <p>Merci de votre patience et de votre compréhension.</p>
        <a href="/bigpharma/public/" class="btn btn-primary mt-3">
            <i class="bi bi-arrow-left"></i> Retour à l'accueil
        </a>
    </div>
</div>

<?php
// Inclure le pied de page
require_once 'C:\xampp\htdocs\bigpharma/templates/footer.php';
?>
