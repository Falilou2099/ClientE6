<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>BigPharma - Gestion Pharmaceutique</title>
    
    <!-- Bootstrap 5 CSS -->
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.2.3/dist/css/bootstrap.min.css" rel="stylesheet">
    
    <!-- Font Awesome pour les icônes -->
    <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0/css/all.min.css" rel="stylesheet">
    
    <style>
        :root {
            --primary-color: #007bff;
            --secondary-color: #6c757d;
            --success-color: #28a745;
            --warning-color: #ffc107;
        }

        body {
            font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
            background-color: #f4f6f9;
            color: #333;
        }

        .navbar {
            background-color: var(--primary-color);
            box-shadow: 0 2px 4px rgba(0,0,0,0.1);
        }

        .navbar-brand {
            color: white !important;
            font-weight: bold;
        }

        .card {
            border: none;
            border-radius: 10px;
            box-shadow: 0 4px 6px rgba(0,0,0,0.1);
            transition: transform 0.3s;
        }

        .card:hover {
            transform: translateY(-5px);
        }

        .table-striped tbody tr:nth-of-type(odd) {
            background-color: rgba(0,123,255,0.05);
        }

        .alert-low-stock {
            background-color: rgba(255,193,7,0.1);
            border-left: 4px solid var(--warning-color);
        }

        /* Styles pour les cartes de produits */
        .product-card {
            transition: transform 0.3s ease;
            box-shadow: 0 4px 6px rgba(0,0,0,0.1);
        }
        .product-card:hover {
            transform: scale(1.03);
        }
        .badge-prescription {
            position: absolute;
            top: 10px;
            right: 10px;
        }
    </style>
</head>
<body>
    <nav class="navbar navbar-expand-lg navbar-dark">
        <div class="container">
            <a class="navbar-brand" href="/bigpharma/public/">
                <i class="fas fa-pills"></i> BigPharma
            </a>
            <button class="navbar-toggler" type="button" data-bs-toggle="collapse" data-bs-target="#navbarNav">
                <span class="navbar-toggler-icon"></span>
            </button>
            <div class="collapse navbar-collapse" id="navbarNav">
                <?php if(isset($_SESSION['user_id'])): ?>
                <!-- Menu pour utilisateurs connectés -->
                <ul class="navbar-nav me-auto">
                    <li class="nav-item">
                        <a class="nav-link" href="/bigpharma/public/"><i class="fas fa-home"></i> Accueil</a>
                    </li>
                    <li class="nav-item">
                        <a class="nav-link" href="/bigpharma/products"><i class="fas fa-box"></i> Produits</a>
                    </li>
                    <li class="nav-item">
                        <a class="nav-link" href="/bigpharma/search"><i class="fas fa-search"></i> Recherche</a>
                    </li>
                </ul>
                
                <!-- Barre de recherche dans la navbar -->
                <form class="d-flex me-3" action="/bigpharma/search" method="GET">
                    <input class="form-control me-2" type="search" name="query" placeholder="Rechercher un produit" aria-label="Search">
                    <button class="btn btn-outline-light" type="submit"><i class="fas fa-search"></i></button>
                </form>
                
                <!-- Bouton de déconnexion pour utilisateurs connectés -->
                <div class="navbar-nav">
                    <div class="nav-item dropdown">
                        <a class="nav-link dropdown-toggle" href="#" id="navbarDropdown" role="button" data-bs-toggle="dropdown" aria-expanded="false">
                            <i class="fas fa-user"></i> 
                            <?php 
                                if(isset($_SESSION['user_id'])) {
                                    $user = \Models\User::findById($_SESSION['user_id']);
                                    echo htmlspecialchars($user->getPharmacyName() ?: $user->getEmail());
                                } else {
                                    echo 'Mon compte';
                                }
                            ?>
                        </a>
                        <ul class="dropdown-menu dropdown-menu-end" aria-labelledby="navbarDropdown">
                            <li><a class="dropdown-item" href="/bigpharma/profile"><i class="fas fa-user-circle"></i> Profil</a></li>
                            <li><hr class="dropdown-divider"></li>
                            <li><a class="dropdown-item" href="/bigpharma/logout"><i class="fas fa-sign-out-alt"></i> Déconnexion</a></li>
                        </ul>
                    </div>
                </div>
                <?php else: ?>
                <!-- Menu pour visiteurs non connectés -->
                <ul class="navbar-nav ms-auto">
                    <li class="nav-item">
                        <a class="nav-link" href="/bigpharma/login"><i class="fas fa-sign-in-alt"></i> Connexion</a>
                    </li>
                    <li class="nav-item">
                        <a class="nav-link" href="/bigpharma/register"><i class="fas fa-user-plus"></i> Inscription</a>
                    </li>
                </ul>
                <?php endif; ?>
            </div>
        </div>
    </nav>

    <div class="container mt-4">
