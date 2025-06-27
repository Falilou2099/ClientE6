<?php
// Script pour implémenter les restrictions d'accès par pharmacie
require_once 'config/database.php';

try {
    $pdo = $GLOBALS['pdo'];
    
    echo "<h1>Implémentation des restrictions d'accès par pharmacie</h1>";
    
    // Étape 1: Vérifier si la colonne pharmacy_id existe dans la table users
    $stmt = $pdo->query("DESCRIBE users");
    $userColumns = $stmt->fetchAll(PDO::FETCH_ASSOC);
    
    $hasPharmacyIdColumn = false;
    foreach ($userColumns as $column) {
        if ($column['Field'] === 'pharmacy_id') {
            $hasPharmacyIdColumn = true;
            break;
        }
    }
    
    // Ajouter la colonne pharmacy_id si elle n'existe pas
    if (!$hasPharmacyIdColumn) {
        $pdo->exec("ALTER TABLE users ADD COLUMN pharmacy_id INT NULL AFTER role");
        $pdo->exec("ALTER TABLE users ADD CONSTRAINT fk_users_pharmacy FOREIGN KEY (pharmacy_id) REFERENCES pharmacies(id) ON DELETE SET NULL");
        echo "<p class='success'>La colonne 'pharmacy_id' a été ajoutée à la table 'users' avec une contrainte de clé étrangère.</p>";
    } else {
        echo "<p class='info'>La colonne 'pharmacy_id' existe déjà dans la table 'users'.</p>";
    }
    
    // Étape 2: Ajouter une colonne pharmacy_id à toutes les tables qui doivent être filtrées par pharmacie
    $tablesToModify = [
        'produits' => 'products',
        'clients' => 'clients',
        'commandes' => 'orders',
        'stocks' => 'stocks'
    ];
    
    foreach ($tablesToModify as $tableName => $entityName) {
        // Vérifier si la table existe
        $stmt = $pdo->query("SHOW TABLES LIKE '$tableName'");
        $tableExists = $stmt->rowCount() > 0;
        
        if ($tableExists) {
            // Vérifier si la colonne pharmacy_id existe déjà
            $stmt = $pdo->query("DESCRIBE $tableName");
            $columns = $stmt->fetchAll(PDO::FETCH_ASSOC);
            
            $hasPharmacyIdColumn = false;
            foreach ($columns as $column) {
                if ($column['Field'] === 'pharmacy_id') {
                    $hasPharmacyIdColumn = true;
                    break;
                }
            }
            
            if (!$hasPharmacyIdColumn) {
                $pdo->exec("ALTER TABLE $tableName ADD COLUMN pharmacy_id INT NULL");
                $pdo->exec("ALTER TABLE $tableName ADD CONSTRAINT fk_{$tableName}_pharmacy FOREIGN KEY (pharmacy_id) REFERENCES pharmacies(id) ON DELETE CASCADE");
                echo "<p class='success'>La colonne 'pharmacy_id' a été ajoutée à la table '$tableName'.</p>";
            } else {
                echo "<p class='info'>La colonne 'pharmacy_id' existe déjà dans la table '$tableName'.</p>";
            }
        } else {
            echo "<p class='warning'>La table '$tableName' n'existe pas dans la base de données.</p>";
        }
    }
    
    // Étape 3: Modifier le modèle User pour prendre en compte la relation avec Pharmacy
    $userModelPath = __DIR__ . '/src/Models/User.php';
    if (file_exists($userModelPath)) {
        $userModelContent = file_get_contents($userModelPath);
        
        // Vérifier si le modèle contient déjà la propriété pharmacy_id
        if (strpos($userModelContent, 'private $pharmacy_id;') === false) {
            // Ajouter la propriété pharmacy_id
            $userModelContent = preg_replace(
                '/(private \$role;)/',
                "$1\n    private \$pharmacy_id;",
                $userModelContent
            );
            
            // Mettre à jour le constructeur
            $userModelContent = preg_replace(
                '/(public function __construct\([^)]*\))/',
                'public function __construct($id = null, $email = null, $password = null, $role = null, $pharmacy_id = null)',
                $userModelContent
            );
            
            // Mettre à jour l'initialisation des propriétés dans le constructeur
            $userModelContent = preg_replace(
                '/(        \$this->role = \$role;)/',
                "$1\n        \$this->pharmacy_id = \$pharmacy_id;",
                $userModelContent
            );
            
            // Ajouter le getter pour pharmacy_id
            $userModelContent = preg_replace(
                '/(    public function getRole\(\) { return \$this->role; })/',
                "$1\n    public function getPharmacyId() { return \$this->pharmacy_id; }",
                $userModelContent
            );
            
            // Mettre à jour la méthode save pour inclure pharmacy_id
            $userModelContent = str_replace(
                'INSERT INTO users (email, password, role) VALUES (:email, :password, :role)',
                'INSERT INTO users (email, password, role, pharmacy_id) VALUES (:email, :password, :role, :pharmacy_id)',
                $userModelContent
            );
            
            $userModelContent = str_replace(
                "':role' => \$this->role",
                "':role' => \$this->role,\n                ':pharmacy_id' => \$this->pharmacy_id",
                $userModelContent
            );
            
            $userModelContent = str_replace(
                'UPDATE users SET email = :email, password = :password, role = :role',
                'UPDATE users SET email = :email, password = :password, role = :role, pharmacy_id = :pharmacy_id',
                $userModelContent
            );
            
            // Mettre à jour la méthode createFromArray
            $userModelContent = str_replace(
                'return new self($data[\'id\'] ?? null, $data[\'email\'] ?? null, $data[\'password\'] ?? null, $data[\'role\'] ?? null);',
                'return new self($data[\'id\'] ?? null, $data[\'email\'] ?? null, $data[\'password\'] ?? null, $data[\'role\'] ?? null, $data[\'pharmacy_id\'] ?? null);',
                $userModelContent
            );
            
            file_put_contents($userModelPath, $userModelContent);
            echo "<p class='success'>Le modèle User a été mis à jour pour prendre en compte la relation avec Pharmacy.</p>";
        } else {
            echo "<p class='info'>Le modèle User contient déjà la propriété pharmacy_id.</p>";
        }
    } else {
        echo "<p class='error'>Le fichier du modèle User n'a pas été trouvé à l'emplacement attendu.</p>";
    }
    
    // Étape 4: Modifier le contrôleur d'authentification pour associer l'utilisateur à sa pharmacie lors de l'inscription
    $authControllerPath = __DIR__ . '/src/Controllers/AuthController.php';
    if (file_exists($authControllerPath)) {
        $authControllerContent = file_get_contents($authControllerPath);
        
        // Vérifier si le contrôleur associe déjà l'utilisateur à sa pharmacie
        if (strpos($authControllerContent, '$user->pharmacy_id = $pharmacy->getId()') === false && 
            strpos($authControllerContent, '$user->setPharmacyId($pharmacy->getId())') === false) {
            
            // Mettre à jour la méthode registerPharmacy pour associer l'utilisateur à sa pharmacie
            $authControllerContent = preg_replace(
                '/(            \/\/ Créer un utilisateur pour la pharmacie[^}]*?)(\s+return \[)/',
                "$1\n            // Associer l'utilisateur à la pharmacie\n            \$user->pharmacy_id = \$pharmacy->getId();\n            \$user->save();\n$2",
                $authControllerContent
            );
            
            // Mettre à jour la méthode processLogin pour stocker l'ID de la pharmacie dans la session
            $authControllerContent = preg_replace(
                '/(\$_SESSION\[\'user_role\'\] = \$result\[\'user\'\]->getRole\(\);)/',
                "$1\n            \$_SESSION['pharmacy_id'] = \$result['user']->getPharmacyId();",
                $authControllerContent
            );
            
            file_put_contents($authControllerPath, $authControllerContent);
            echo "<p class='success'>Le contrôleur d'authentification a été mis à jour pour associer l'utilisateur à sa pharmacie.</p>";
        } else {
            echo "<p class='info'>Le contrôleur d'authentification associe déjà l'utilisateur à sa pharmacie.</p>";
        }
    } else {
        echo "<p class='error'>Le fichier du contrôleur d'authentification n'a pas été trouvé à l'emplacement attendu.</p>";
    }
    
    // Étape 5: Créer une classe BaseController pour implémenter la restriction d'accès par pharmacie
    $baseControllerPath = __DIR__ . '/src/Controllers/BaseController.php';
    $baseControllerContent = <<<'EOT'
<?php
namespace Controllers;

class BaseController {
    protected $currentUser;
    protected $pharmacyId;
    
    public function __construct() {
        // Initialiser les informations de l'utilisateur connecté
        $this->initCurrentUser();
    }
    
    protected function initCurrentUser() {
        if (session_status() === PHP_SESSION_NONE) {
            session_start();
        }
        
        $this->currentUser = isset($_SESSION['user_id']) ? $_SESSION['user_id'] : null;
        $this->pharmacyId = isset($_SESSION['pharmacy_id']) ? $_SESSION['pharmacy_id'] : null;
    }
    
    /**
     * Filtre les données en fonction de la pharmacie de l'utilisateur connecté
     * @param array $data Tableau de données à filtrer
     * @param string $pharmacyIdField Nom du champ contenant l'ID de la pharmacie
     * @return array Données filtrées
     */
    protected function filterDataByPharmacy($data, $pharmacyIdField = 'pharmacy_id') {
        // Si l'utilisateur n'est pas connecté ou n'a pas de pharmacie associée, retourner un tableau vide
        if (!$this->currentUser || !$this->pharmacyId) {
            return [];
        }
        
        // Si l'utilisateur a le rôle 'admin', ne pas filtrer les données
        if (isset($_SESSION['user_role']) && $_SESSION['user_role'] === 'admin') {
            return $data;
        }
        
        // Filtrer les données pour ne garder que celles de la pharmacie de l'utilisateur
        return array_filter($data, function($item) use ($pharmacyIdField) {
            // Si l'élément est un objet
            if (is_object($item)) {
                $method = 'get' . ucfirst($pharmacyIdField);
                if (method_exists($item, $method)) {
                    return $item->$method() == $this->pharmacyId;
                }
                return isset($item->$pharmacyIdField) && $item->$pharmacyIdField == $this->pharmacyId;
            }
            
            // Si l'élément est un tableau
            if (is_array($item)) {
                return isset($item[$pharmacyIdField]) && $item[$pharmacyIdField] == $this->pharmacyId;
            }
            
            return false;
        });
    }
    
    /**
     * Vérifie si l'utilisateur a accès à une ressource spécifique
     * @param int $resourcePharmacyId ID de la pharmacie associée à la ressource
     * @return bool True si l'utilisateur a accès, false sinon
     */
    protected function hasAccessToResource($resourcePharmacyId) {
        // Si l'utilisateur n'est pas connecté, il n'a pas accès
        if (!$this->currentUser) {
            return false;
        }
        
        // Si l'utilisateur est admin, il a accès à toutes les ressources
        if (isset($_SESSION['user_role']) && $_SESSION['user_role'] === 'admin') {
            return true;
        }
        
        // Sinon, vérifier si la ressource appartient à la pharmacie de l'utilisateur
        return $this->pharmacyId && $resourcePharmacyId == $this->pharmacyId;
    }
    
    /**
     * Ajoute l'ID de la pharmacie aux données avant de les sauvegarder
     * @param array|object $data Données à modifier
     * @return array|object Données avec l'ID de la pharmacie ajouté
     */
    protected function addPharmacyIdToData($data) {
        if (!$this->pharmacyId) {
            return $data;
        }
        
        if (is_object($data)) {
            $method = 'setPharmacyId';
            if (method_exists($data, $method)) {
                $data->$method($this->pharmacyId);
            } else {
                $data->pharmacy_id = $this->pharmacyId;
            }
        } elseif (is_array($data)) {
            $data['pharmacy_id'] = $this->pharmacyId;
        }
        
        return $data;
    }
}
EOT;

    if (!file_exists($baseControllerPath)) {
        file_put_contents($baseControllerPath, $baseControllerContent);
        echo "<p class='success'>La classe BaseController a été créée pour implémenter la restriction d'accès par pharmacie.</p>";
    } else {
        echo "<p class='info'>La classe BaseController existe déjà.</p>";
    }
    
    // Étape 6: Mettre à jour les contrôleurs existants pour hériter de BaseController
    $controllersDir = __DIR__ . '/src/Controllers';
    if (is_dir($controllersDir)) {
        $controllers = glob($controllersDir . '/*.php');
        
        foreach ($controllers as $controllerPath) {
            $controllerName = basename($controllerPath, '.php');
            
            // Ne pas modifier BaseController ou AuthController (déjà modifié)
            if ($controllerName === 'BaseController' || $controllerName === 'AuthController') {
                continue;
            }
            
            $controllerContent = file_get_contents($controllerPath);
            
            // Vérifier si le contrôleur hérite déjà de BaseController
            if (strpos($controllerContent, 'extends BaseController') === false) {
                // Ajouter l'héritage de BaseController
                $controllerContent = preg_replace(
                    '/(class ' . $controllerName . ' {)/',
                    'class ' . $controllerName . ' extends BaseController {',
                    $controllerContent
                );
                
                // Ajouter l'appel au constructeur parent
                if (strpos($controllerContent, 'public function __construct') !== false) {
                    $controllerContent = preg_replace(
                        '/(public function __construct[^{]*{)/',
                        "$1\n        parent::__construct();",
                        $controllerContent
                    );
                } else {
                    $controllerContent = preg_replace(
                        '/(class ' . $controllerName . ' extends BaseController {)/',
                        "$1\n    public function __construct() {\n        parent::__construct();\n    }",
                        $controllerContent
                    );
                }
                
                // Modifier les méthodes qui récupèrent des données pour appliquer le filtre par pharmacie
                $methodsToModify = [
                    'getAll' => 'return $this->filterDataByPharmacy($result);',
                    'findAll' => 'return $this->filterDataByPharmacy($result);',
                    'index' => '$data = $this->filterDataByPharmacy($data);',
                    'list' => '$items = $this->filterDataByPharmacy($items);'
                ];
                
                foreach ($methodsToModify as $methodName => $filterCode) {
                    if (preg_match('/public function ' . $methodName . '[^{]*{/', $controllerContent)) {
                        $controllerContent = preg_replace(
                            '/(public function ' . $methodName . '[^{]*{[^}]*return \$[^;]+;)/',
                            "$1\n        $filterCode",
                            $controllerContent
                        );
                    }
                }
                
                // Modifier les méthodes qui sauvegardent des données pour ajouter l'ID de la pharmacie
                $saveMethodsToModify = [
                    'save' => '$data = $this->addPharmacyIdToData($data);',
                    'store' => '$data = $this->addPharmacyIdToData($data);',
                    'create' => '$data = $this->addPharmacyIdToData($data);',
                    'add' => '$data = $this->addPharmacyIdToData($data);'
                ];
                
                foreach ($saveMethodsToModify as $methodName => $addPharmacyIdCode) {
                    if (preg_match('/public function ' . $methodName . '[^{]*{/', $controllerContent)) {
                        $controllerContent = preg_replace(
                            '/(public function ' . $methodName . '[^{]*{)/',
                            "$1\n        $addPharmacyIdCode",
                            $controllerContent
                        );
                    }
                }
                
                file_put_contents($controllerPath, $controllerContent);
                echo "<p class='success'>Le contrôleur $controllerName a été mis à jour pour hériter de BaseController.</p>";
            } else {
                echo "<p class='info'>Le contrôleur $controllerName hérite déjà de BaseController.</p>";
            }
        }
    } else {
        echo "<p class='error'>Le répertoire des contrôleurs n'a pas été trouvé à l'emplacement attendu.</p>";
    }
    
    echo "<h2>Implémentation terminée</h2>";
    echo "<p>Les modifications nécessaires ont été apportées pour que chaque compte soit rattaché à sa pharmacie et n'ait accès qu'aux données liées à son compte.</p>";
    echo "<p>Vous pouvez maintenant créer des comptes pour différentes pharmacies, et chaque utilisateur ne verra que les données de sa propre pharmacie.</p>";
    
} catch (PDOException $e) {
    echo "<h1>Erreur</h1>";
    echo "<p class='error'>Une erreur s'est produite : " . htmlspecialchars($e->getMessage()) . "</p>";
}
?>

<style>
    body {
        font-family: Arial, sans-serif;
        margin: 20px;
        line-height: 1.6;
    }
    h1, h2 {
        color: #333;
    }
    .success {
        color: green;
        font-weight: bold;
    }
    .info {
        color: blue;
    }
    .warning {
        color: orange;
        font-weight: bold;
    }
    .error {
        color: red;
        font-weight: bold;
    }
</style>
