<?php
// Script pour créer une pharmacie de test et un utilisateur associé
require_once __DIR__ . '/../config/database.php';
require_once __DIR__ . '/../src/Models/Pharmacy.php';
require_once __DIR__ . '/../src/Models/User.php';
require_once __DIR__ . '/../config/security.php';

use Models\Pharmacy;
use Models\User;
use Config\SecurityService;

// Données de la pharmacie de test
$pharmacyData = [
    'name' => 'Pharmacie Test',
    'address' => '3 rue de Test, 75000 Paris',
    'phone_number' => '0909090909',
    'email' => 'test@pharmacie.com',
    'registration_number' => 'TEST123456',
    'status' => 'active'
];

// Vérifier si la pharmacie existe déjà
$existingPharmacy = Pharmacy::findByEmail($pharmacyData['email']);

if ($existingPharmacy) {
    echo "La pharmacie de test existe déjà avec l'ID: " . $existingPharmacy->getId() . "\n";
    $pharmacy = $existingPharmacy;
} else {
    // Créer la pharmacie
    $pharmacy = new Pharmacy(
        null,
        $pharmacyData['name'],
        $pharmacyData['address'],
        $pharmacyData['phone_number'],
        $pharmacyData['email'],
        $pharmacyData['registration_number'],
        $pharmacyData['status']
    );

    // Valider les données
    $errors = $pharmacy->validate();
    if (!empty($errors)) {
        echo "Erreurs de validation de la pharmacie:\n";
        foreach ($errors as $error) {
            echo "- " . $error . "\n";
        }
        exit;
    }

    // Sauvegarder la pharmacie
    if ($pharmacy->save()) {
        echo "Pharmacie de test créée avec succès! ID: " . $pharmacy->getId() . "\n";
    } else {
        echo "Erreur lors de la création de la pharmacie de test\n";
        exit;
    }
}

// Données de l'utilisateur de test
$userData = [
    'email' => 'admin123@pharmacie.com',
    'password' => 'Admin123!',
    'role' => 'admin'
];

// Vérifier si l'utilisateur existe déjà
$existingUser = User::findByEmail($userData['email']);

if ($existingUser) {
    echo "L'utilisateur de test existe déjà avec l'ID: " . $existingUser->getId() . "\n";
} else {
    // Créer l'utilisateur
    $hashedPassword = SecurityService::hashPassword($userData['password']);
    $user = new User(
        null,
        $userData['email'],
        $hashedPassword,
        $pharmacy->getId(),
        $userData['role']
    );

    // Sauvegarder l'utilisateur
    if ($user->save()) {
        echo "Utilisateur de test créé avec succès! ID: " . $user->getId() . "\n";
        echo "Email: " . $userData['email'] . "\n";
        echo "Mot de passe: " . $userData['password'] . "\n";
    } else {
        echo "Erreur lors de la création de l'utilisateur de test\n";
    }
}

echo "\nVous pouvez maintenant vous connecter à l'application web et à l'application Java avec les identifiants suivants:\n";
echo "Email: " . $userData['email'] . "\n";
echo "Mot de passe: " . $userData['password'] . "\n";
?>
