<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title><?= isset($pageTitle) ? htmlspecialchars($pageTitle) . ' - ' : '' ?>BigPharma</title>
    
    <!-- Bootstrap CSS -->
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0-alpha1/dist/css/bootstrap.min.css" rel="stylesheet">
    
    <!-- Bootstrap Icons -->
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.10.0/font/bootstrap-icons.css">
    
    <!-- Styles personnalisés -->
    <style>
        body { background-color: #f4f6f9; }
        .dashboard-card {
            transition: transform 0.3s ease;
            box-shadow: 0 4px 6px rgba(0,0,0,0.1);
        }
        .badge-prescription {
            position: absolute;
            top: 10px;
            right: 10px;
        }
        footer {
            margin-top: 50px;
            padding: 20px 0;
            background-color: #f8f9fa;
        }
    </style>
    
    <!-- Scripts Bootstrap -->
    <script src="https://cdn.jsdelivr.net/npm/@popperjs/core@2.11.6/dist/umd/popper.min.js"></script>
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0-alpha1/dist/js/bootstrap.bundle.min.js"></script>
    
    <!-- Scripts personnalisés -->
    <script src="/bigpharma/public/js/global.js"></script>
    <script src="/bigpharma/public/js/menu-fix.js"></script>
</head>
<body>
    <!-- Barre de navigation -->
    <nav class="navbar navbar-dark bg-primary">
        <div class="container-fluid d-flex justify-content-between align-items-center">
            <div class="d-flex align-items-center">
                <a class="navbar-brand me-3" href="/bigpharma/public">
                    <i class="bi bi-capsule me-2"></i>BigPharma
                </a>
                
                <?php if (isset($_SESSION['user_id'])): ?>
                <!-- Liens de navigation simplifiés comme dans l'image -->
                <div class="d-flex">
                    <a class="btn btn-primary me-2 <?= empty($_GET['url']) || $_GET['url'] === 'home' ? 'active' : '' ?>" href="/bigpharma/public">
                        <i class="bi bi-house-fill me-1"></i> Accueil
                    </a>
                    <a class="btn btn-primary me-2 <?= isset($_GET['url']) && strpos($_GET['url'], 'products') === 0 ? 'active' : '' ?>" href="/bigpharma/public/products">
                        <i class="bi bi-box2-fill me-1"></i> Produits
                    </a>
                    <a class="btn btn-primary me-2 <?= isset($_GET['url']) && strpos($_GET['url'], 'clients') === 0 ? 'active' : '' ?>" href="/bigpharma/public/clients">
                        <i class="bi bi-people-fill me-1"></i> Clients
                    </a>
                    <a class="btn btn-primary <?= isset($_GET['url']) && strpos($_GET['url'], 'sales') === 0 ? 'active' : '' ?>" href="/bigpharma/public/sales">
                        <i class="bi bi-graph-up-arrow me-1"></i> Historique des ventes
                    </a>
                </div>
                <?php endif; ?>
            </div>
            
            <!-- Menu utilisateur en haut à droite comme dans l'image -->
            <?php if (isset($_SESSION['user_id'])): ?>
            <div class="d-flex align-items-center">
                <div class="dropdown">
                    <button class="btn btn-light dropdown-toggle" type="button" id="userDropdown" data-bs-toggle="dropdown" aria-expanded="false">
                        <i class="bi bi-person-circle me-1"></i>
                        <?php 
                        // Récupérer le nom de la pharmacie associée à l'utilisateur
                        if(isset($_SESSION['user_id'])) {
                            $user = \Models\User::findById($_SESSION['user_id']);
                            if ($user && $user->getPharmacyName()) {
                                echo htmlspecialchars($user->getPharmacyName());
                            } else {
                                echo 'Utilisateur';
                            }
                        } else {
                            echo 'Utilisateur';
                        }
                        ?>
                    </button>
                    <ul class="dropdown-menu dropdown-menu-end" aria-labelledby="userDropdown" id="userDropdownMenu">
                        <li><a class="dropdown-item" href="/bigpharma/public/profile"><i class="bi bi-person me-2"></i> Mon profil</a></li>
                        <li><a class="dropdown-item" href="/bigpharma/public/settings"><i class="bi bi-gear me-2"></i> Paramètres</a></li>
                        <li><hr class="dropdown-divider"></li>
                        <li><a class="dropdown-item" href="/bigpharma/public/logout"><i class="bi bi-box-arrow-right me-2"></i> Déconnexion</a></li>
                    </ul>
                </div>
            </div>
            <?php else: ?>
            <!-- Menu pour visiteurs non connectés -->
            <div class="d-flex">
                <a href="/bigpharma/public/login" class="btn btn-outline-light me-2">Connexion</a>
                <a href="/bigpharma/public/register" class="btn btn-light">Inscription</a>
            </div>
            <?php endif; ?>
        </div>
    </nav>
    <!-- Fin de la barre de navigation -->

<?php require_once 'C:\xampp\htdocs\bigpharma/templates/footer.php'; ?>
