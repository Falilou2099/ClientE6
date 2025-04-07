<?php
// Configuration générale de l'application

// Paramètres de l'application
define('APP_NAME', 'BigPharma');
define('APP_VERSION', '1.0.0');
define('APP_ENVIRONMENT', 'development');

// Chemins
define('ROOT_PATH', dirname(__DIR__));
define('PUBLIC_PATH', ROOT_PATH . '/public');
define('SRC_PATH', ROOT_PATH . '/src');

// Configurations de session
ini_set('session.use_strict_mode', 1);
ini_set('session.cookie_httponly', 1);
ini_set('session.use_only_cookies', 1);

// Gestion des erreurs
error_reporting(E_ALL);
ini_set('display_errors', APP_ENVIRONMENT === 'development' ? 1 : 0);

// Fuseau horaire
date_default_timezone_set('Europe/Paris');

// Configurations de sécurité par défaut
return [
    'security' => [
        'password_hash_algo' => PASSWORD_ARGON2ID,
        'password_hash_options' => [
            'memory_cost' => 1024 * 64,
            'time_cost' => 4,
            'threads' => 3
        ],
        'session_lifetime' => 3600, // 1 heure
        'csrf_token_expiry' => 1800 // 30 minutes
    ]
];
