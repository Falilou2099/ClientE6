<?php
return [
    '/' => [
        'controller' => 'HomeController',
        'method' => 'index'
    ],
    '' => [
        'controller' => 'HomeController',
        'method' => 'index'
    ],
    'home' => [
        'controller' => 'HomeController',
        'method' => 'index'
    ],
    'index' => [
        'controller' => 'HomeController',
        'method' => 'index'
    ],
    'search' => [
        'controller' => 'SearchController',
        'method' => 'index'
    ],
    'search/query' => [
        'controller' => 'SearchController',
        'method' => 'index'
    ],
    'products' => [
        'controller' => 'ProductController',
        'method' => 'index'
    ],
    'products/detail' => [
        'controller' => 'ProductController',
        'method' => 'detail'
    ],
    'products/restock' => [
        'controller' => 'ProductController', 
        'method' => 'restock'
    ],
    // Routes d'authentification
    'login' => [
        'controller' => 'AuthController',
        'method' => 'showLoginForm'
    ],
    'login/process' => [
        'controller' => 'AuthController',
        'method' => 'processLogin'
    ],
    'register' => [
        'controller' => 'AuthController',
        'method' => 'showRegisterForm'
    ],
    'register/process' => [
        'controller' => 'AuthController',
        'method' => 'processRegister'
    ],
    'logout' => [
        'controller' => 'AuthController',
        'method' => 'logout'
    ],
    // Routes de réinitialisation de mot de passe
    'password-reset' => [
        'controller' => 'AuthController',
        'method' => 'showPasswordResetRequestForm'
    ],
    'password-reset/request' => [
        'controller' => 'AuthController',
        'method' => 'processPasswordResetRequest'
    ],
    'password-reset/form' => [
        'controller' => 'AuthController',
        'method' => 'showPasswordResetForm'
    ],
    'password-reset/update' => [
        'controller' => 'AuthController',
        'method' => 'processPasswordReset'
    ],
    // Routes pour les ventes
    'api/clients' => [
        'controller' => 'ApiController',
        'method' => 'getClients'
    ],
    'api/sale/process' => [
        'controller' => 'ApiController',
        'method' => 'processSale'
    ],
    
    // Route pour la page de profil
    'profile' => [
        'controller' => 'ProfileController',
        'method' => 'index'
    ],
    
    // Routes pour la gestion des clients
    'clients' => [
        'controller' => 'ClientController',
        'method' => 'index'
    ],
    'clients/create' => [
        'controller' => 'ClientController',
        'method' => 'create'
    ],
    'clients/store' => [
        'controller' => 'ClientController',
        'method' => 'store'
    ],
    'clients/show' => [
        'controller' => 'ClientController',
        'method' => 'show'
    ],
    'clients/edit' => [
        'controller' => 'ClientController',
        'method' => 'edit'
    ],
    'clients/update' => [
        'controller' => 'ClientController',
        'method' => 'update'
    ],
    'clients/delete' => [
        'controller' => 'ClientController',
        'method' => 'delete'
    ],
    'api/clients/search' => [
        'controller' => 'ClientController',
        'method' => 'search'
    ],
    
    // Routes pour les ventes (historique)
    'sales' => [
        'controller' => 'SalesController',
        'method' => 'index'
    ],
    'sales/show' => [
        'controller' => 'SalesController',
        'method' => 'show'
    ],
    'sales/show/{id}' => [
        'controller' => 'SalesController',
        'method' => 'show'
    ],
    'sales/delete' => [
        'controller' => 'SalesController',
        'method' => 'delete'
    ],
    'sales/delete/{id}' => [
        'controller' => 'SalesController',
        'method' => 'delete'
    ],
    'sales/print/{id}' => [
        'controller' => 'SalesController',
        'method' => 'printSale'
    ],
    'sales/export/{id}' => [
        'controller' => 'SalesController',
        'method' => 'exportSale'
    ]
];
?>
