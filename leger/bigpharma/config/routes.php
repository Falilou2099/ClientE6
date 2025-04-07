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
    ]
];
?>
