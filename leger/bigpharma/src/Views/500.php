<?php 
// Chemins absolus pour les templates
$headerPath = $_SERVER['DOCUMENT_ROOT'] . '/bigpharma/templates/header.php';
$footerPath = $_SERVER['DOCUMENT_ROOT'] . '/bigpharma/templates/footer.php';

include $headerPath; 
?>

<div class="container text-center my-5">
    <div class="row">
        <div class="col-md-8 offset-md-2">
            <div class="card shadow-lg border-0 rounded-3 border-danger">
                <div class="card-body p-5">
                    <h1 class="display-1 text-danger mb-4"><i class="fas fa-server"></i> 500</h1>
                    <h2 class="h3 mb-3">Erreur Serveur Interne</h2>
                    <p class="lead text-muted mb-4">
                        Nous sommes désolés, une erreur inattendue s'est produite. 
                        Notre équipe technique a été informée et travaille sur le problème.
                    </p>
                    <div class="d-flex justify-content-center">
                        <a href="/" class="btn btn-primary me-3">
                            <i class="fas fa-home"></i> Retour à l'Accueil
                        </a>
                        <a href="/contact" class="btn btn-outline-secondary">
                            <i class="fas fa-envelope"></i> Contactez le Support
                        </a>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>

<?php 
include $footerPath; 
?>
