<?php
// Configuration de connexion à la base de données partagée entre client lourd et léger
$host = 'localhost';
$db_name = 'clientlegerlourd';
$username = 'root';
$password = '';

try {
    // Créer la connexion PDO avec les paramètres d'encodage
    $pdo = new PDO(
        "mysql:host={$host};dbname={$db_name};charset=utf8mb4", 
        $username, 
        $password,
        [
            PDO::MYSQL_ATTR_INIT_COMMAND => "SET NAMES utf8mb4 COLLATE utf8mb4_unicode_ci",
            PDO::ATTR_EMULATE_PREPARES => false
        ]
    );
    
    // Configurer les attributs PDO
    $pdo->setAttribute(PDO::ATTR_ERRMODE, PDO::ERRMODE_EXCEPTION);
    $pdo->setAttribute(PDO::ATTR_DEFAULT_FETCH_MODE, PDO::FETCH_ASSOC);
    $pdo->exec("SET NAMES utf8mb4 COLLATE utf8mb4_unicode_ci");

    // Rendre $pdo disponible globalement
    $GLOBALS['pdo'] = $pdo;

} catch(PDOException $e) {
    // Gestion des erreurs de connexion
    error_log("Erreur de connexion à la base de données : " . $e->getMessage());
    die("Impossible de se connecter à la base de données. Veuillez vérifier vos paramètres de connexion.");
}

// Retourner la connexion pour permettre l'inclusion du fichier
return $pdo;
?>
