<?php
session_start();

// Chemins absolus pour les inclusions
define('ROOT_PATH', dirname(__DIR__));
define('CONFIG_PATH', ROOT_PATH . '/config');
define('SRC_PATH', ROOT_PATH . '/src');

// Autoloader
require_once CONFIG_PATH . '/database.php';

// Charger les routes
$routes = require_once CONFIG_PATH . '/routes.php';

// Autoloader personnalisé
spl_autoload_register(function($class) {
    $prefixes = [
        'Controllers\\' => SRC_PATH . '/Controllers/',
        'Models\\' => SRC_PATH . '/Models/',
        'Middleware\\' => SRC_PATH . '/Middleware/',
        'Config\\' => SRC_PATH . '/Config/'
    ];

    foreach ($prefixes as $prefix => $base_dir) {
        $len = strlen($prefix);
        if (strncmp($prefix, $class, $len) === 0) {
            $relative_class = substr($class, $len);
            $file = $base_dir . str_replace('\\', '/', $relative_class) . '.php';
            
            if (file_exists($file)) {
                require $file;
                return true;
            }
        }
    }
    return false;
});

// Gestion des erreurs
error_reporting(E_ALL);
ini_set('display_errors', 1);

// Routage
$url = $_GET['url'] ?? '';
$url = trim($url, '/');

// Débogage
error_log("URL reçue : " . $url);
error_log("Routes disponibles : " . print_r(array_keys($routes), true));

// Routes publiques (accessibles sans authentification)
$publicRoutes = [
    'login',
    'login/process',
    'register',
    'register/process',
    'password-reset',
    'password-reset/request',
    'password-reset/form',
    'password-reset/update'
];

// Vérifier l'authentification pour les routes protégées
use Middleware\AuthMiddleware;

// Si l'URL est vide, rediriger vers la page de connexion si non authentifié
if (empty($url)) {
    if (!AuthMiddleware::isAuthenticated()) {
        header('Location: /bigpharma/public/login');
        exit;
    }
}

// Vérifier l'authentification pour les routes non publiques
if (!in_array($url, $publicRoutes)) {
    AuthMiddleware::requireAuth($publicRoutes, $url);
}

// Rechercher la route correspondante
$found_route = false;
$controllerName = null;
$methodName = null;
$params = [];

// D'abord, essayer une correspondance exacte
if (isset($routes[$url])) {
    $controllerName = "Controllers\\" . $routes[$url]['controller'];
    $methodName = $routes[$url]['method'];
    $found_route = true;
} else {
    // Sinon, essayer de faire correspondre les routes avec des paramètres
    foreach ($routes as $route => $config) {
        // Vérifier si la route contient des paramètres (indiqués par {...})
        if (strpos($route, '{') !== false) {
            // Convertir la route en expression régulière
            $pattern = preg_replace('/{([^}]+)}/', '([^/]+)', $route);
            $pattern = '#^' . str_replace('/', '\/', $pattern) . '$#';
            
            // Tester si l'URL correspond au pattern
            if (preg_match($pattern, $url, $matches)) {
                // Extraire les valeurs des paramètres
                $routeParts = explode('/', $route);
                $urlParts = explode('/', $url);
                
                // Parcourir les parties de la route pour trouver les paramètres
                foreach ($routeParts as $index => $part) {
                    if (strpos($part, '{') === 0 && strpos($part, '}') === strlen($part) - 1) {
                        // C'est un paramètre, extraire sa valeur
                        $paramName = substr($part, 1, -1);
                        $params[$paramName] = isset($urlParts[$index]) ? $urlParts[$index] : null;
                    }
                }
                
                $controllerName = "Controllers\\" . $config['controller'];
                $methodName = $config['method'];
                $found_route = true;
                break;
            }
        }
    }
}

// Si aucune route n'est trouvée, utiliser la route par défaut
if (!$found_route) {
    error_log("Aucune route trouvée, utilisation de la route par défaut");
    $controllerName = "Controllers\\HomeController";
    $methodName = 'index';
}

try {
    // Vérification de l'existence du contrôleur
    if (!class_exists($controllerName)) {
        throw new Exception("Contrôleur non trouvé : $controllerName");
    }

    // Instanciation du contrôleur sans injection de dépendance
    $controller = new $controllerName();

    if (method_exists($controller, $methodName)) {
        // Passer les paramètres extraits de l'URL
        if (!empty($params)) {
            // Si nous avons un paramètre 'id', le passer directement
            if (isset($params['id'])) {
                error_log("Appel de méthode avec paramètre id : " . $params['id']);
                $controller->$methodName($params['id']);
            } else {
                // Sinon, passer tous les paramètres
                error_log("Appel de méthode avec paramètres : " . print_r($params, true));
                $controller->$methodName($params);
            }
        }
        // Passer les paramètres de requête si nécessaire
        elseif ($methodName === 'index' && isset($_GET['query'])) {
            error_log("Appel de méthode avec requête : " . $_GET['query']);
            $controller->$methodName($_GET['query']);
        } elseif ($methodName === 'search' && isset($_GET['query'])) {
            error_log("Appel de méthode search avec requête : " . $_GET['query']);
            $controller->$methodName($_GET['query']);
        } else {
            error_log("Appel de méthode sans paramètre : $methodName");
            $controller->$methodName();
        }
    } else {
        throw new Exception("Méthode non trouvée : $methodName dans $controllerName");
    }
} catch (Exception $e) {
    // Gestion des erreurs générales
    error_log("Erreur de routage : " . $e->getMessage());
    header("HTTP/1.0 500 Internal Server Error");
    echo "Erreur : " . $e->getMessage();
    exit();
}
?>
