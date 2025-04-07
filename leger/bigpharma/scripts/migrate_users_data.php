<?php
// Connexion à la base de données
try {
    $pdo = new PDO("mysql:host=localhost;dbname=clientlegerlourd", "root", "");
    $pdo->setAttribute(PDO::ATTR_ERRMODE, PDO::ERRMODE_EXCEPTION);
    echo "Connexion à la base de données réussie.\n\n";
} catch (PDOException $e) {
    die("Erreur de connexion à la base de données: " . $e->getMessage());
}

// Vérifier si les tables existent
$usersExists = $pdo->query("SHOW TABLES LIKE 'users'")->rowCount() > 0;
$utilisateursExists = $pdo->query("SHOW TABLES LIKE 'utilisateurs'")->rowCount() > 0;

if (!$usersExists) {
    die("La table 'users' n'existe pas. Migration impossible.\n");
}

if (!$utilisateursExists) {
    die("La table 'utilisateurs' n'existe pas. Migration impossible.\n");
}

// Vérifier si la table users est vide
$usersCount = $pdo->query("SELECT COUNT(*) FROM users")->fetchColumn();
if ($usersCount > 0) {
    echo "ATTENTION: La table 'users' contient déjà " . $usersCount . " enregistrements.\n";
    echo "Voulez-vous continuer la migration? (Exécutez ce script avec l'argument 'force' pour continuer)\n";
    
    if (!isset($argv[1]) || $argv[1] !== 'force') {
        exit;
    }
    
    echo "Migration forcée...\n\n";
}

// Récupérer les données de la table utilisateurs
$stmt = $pdo->query("SELECT * FROM utilisateurs");
$utilisateurs = $stmt->fetchAll(PDO::FETCH_ASSOC);

echo "Migration des données de 'utilisateurs' vers 'users'...\n";
echo "Nombre d'enregistrements à migrer: " . count($utilisateurs) . "\n\n";

// Migrer les données
$migratedCount = 0;
$errorCount = 0;

foreach ($utilisateurs as $utilisateur) {
    try {
        // Mapper les rôles
        $role = 'employee'; // Valeur par défaut
        switch ($utilisateur['role']) {
            case 'ADMIN':
                $role = 'admin';
                break;
            case 'GESTIONNAIRE':
                $role = 'admin';
                break;
            case 'EMPLOYE':
                $role = 'employee';
                break;
        }
        
        // Mapper le statut
        $status = $utilisateur['actif'] ? 'active' : 'inactive';
        
        // Insérer dans la table users
        $stmt = $pdo->prepare("
            INSERT INTO users (
                email, 
                password, 
                role, 
                status, 
                last_login, 
                created_at, 
                updated_at
            ) VALUES (
                :email, 
                :password, 
                :role, 
                :status, 
                :last_login, 
                NOW(), 
                NOW()
            )
        ");
        
        $stmt->execute([
            ':email' => $utilisateur['email'],
            ':password' => $utilisateur['mot_de_passe'],
            ':role' => $role,
            ':status' => $status,
            ':last_login' => $utilisateur['derniere_connexion']
        ]);
        
        echo "Utilisateur migré: " . $utilisateur['nom_utilisateur'] . " (ID: " . $utilisateur['id'] . ")\n";
        $migratedCount++;
    } catch (PDOException $e) {
        echo "Erreur lors de la migration de l'utilisateur " . $utilisateur['nom_utilisateur'] . ": " . $e->getMessage() . "\n";
        $errorCount++;
    }
}

echo "\nRésumé de la migration:\n";
echo "- " . $migratedCount . " utilisateurs migrés avec succès\n";
echo "- " . $errorCount . " erreurs lors de la migration\n\n";

if ($migratedCount > 0 && $errorCount == 0) {
    echo "Migration réussie. Vous pouvez maintenant supprimer la table 'utilisateurs'.\n";
    echo "Pour supprimer la table 'utilisateurs', exécutez le script analyze_user_tables.php avec l'argument 'confirm':\n";
    echo "php scripts/analyze_user_tables.php confirm\n";
} else if ($errorCount > 0) {
    echo "Des erreurs sont survenues lors de la migration. Veuillez les corriger avant de supprimer la table 'utilisateurs'.\n";
} else {
    echo "Aucun utilisateur n'a été migré. Vérifiez que la table 'utilisateurs' contient des données.\n";
}

echo "\nTerminé.\n";
