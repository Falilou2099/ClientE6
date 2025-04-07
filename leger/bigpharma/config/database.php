<?php
// Configuration de connexion à la base de données du client lourd
$host = 'localhost';
$db_name = 'clientlegerlourd';
$username = 'root';
$password = '';

try {
    // Créer la connexion PDO
    $pdo = new PDO(
        "mysql:host={$host};dbname={$db_name}", 
        $username, 
        $password
    );
    
    // Configurer les attributs PDO
    $pdo->setAttribute(PDO::ATTR_ERRMODE, PDO::ERRMODE_EXCEPTION);
    $pdo->setAttribute(PDO::ATTR_DEFAULT_FETCH_MODE, PDO::FETCH_ASSOC);
    $pdo->exec("SET NAMES utf8");

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
