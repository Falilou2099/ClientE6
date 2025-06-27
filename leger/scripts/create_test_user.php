<?php
// Script pour créer un utilisateur de test avec accès aux deux applications

// Charger la configuration de la base de données
require_once __DIR__ . '/../config/database.php';
require_once __DIR__ . '/../src/Config/SecurityService.php';

try {
    // Vérifier si l'utilisateur de test existe déjà
    $checkUserQuery = "SELECT id FROM users WHERE email = 'test@bigpharma.com'";
    $stmt = $pdo->prepare($checkUserQuery);
    $stmt->execute();
    
    if ($stmt->rowCount() == 0) {
        // L'utilisateur n'existe pas, on le crée
        $hashedPassword = \Config\SecurityService::hashPassword('password123');
        $now = date('Y-m-d H:i:s');
        
        $createUserQuery = "INSERT INTO users (
            email, 
            password, 
            role, 
            status, 
            created_at, 
            app_access
        ) VALUES (
            'test@bigpharma.com',
            :password,
            'admin',
            'active',
            :created_at,
            'both'
        )";
        
        $stmt = $pdo->prepare($createUserQuery);
        $stmt->execute([
            ':password' => $hashedPassword,
            ':created_at' => $now
        ]);
        
        echo "Utilisateur de test créé avec succès.\n";
        echo "Email: test@bigpharma.com\n";
        echo "Mot de passe: password123\n";
        echo "Accès: Les deux applications (légère et lourde)\n";
    } else {
        echo "L'utilisateur de test existe déjà.\n";
    }
    
} catch (PDOException $e) {
    echo "Erreur lors de la création de l'utilisateur de test : " . $e->getMessage() . "\n";
}
?>
