<?php
// Script pour créer plusieurs comptes de test pour l'application lourde
require_once __DIR__ . '/../config/database.php';
require_once __DIR__ . '/../src/Models/Pharmacy.php';
require_once __DIR__ . '/../src/Models/User.php';
require_once __DIR__ . '/../config/security.php';

use Models\Pharmacy;
use Models\User;
use Config\SecurityService;

// Fonction pour créer une pharmacie et un utilisateur associé
function createPharmacyAndUser($pharmacyData, $userData) {
    // Vérifier si la pharmacie existe déjà
    $existingPharmacy = Pharmacy::findByEmail($pharmacyData['email']);

    if ($existingPharmacy) {
        echo "La pharmacie {$pharmacyData['name']} existe déjà avec l'ID: " . $existingPharmacy->getId() . "<br>";
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
            echo "Erreurs de validation de la pharmacie {$pharmacyData['name']}:<br>";
            foreach ($errors as $error) {
                echo "- " . $error . "<br>";
            }
            return false;
        }

        // Sauvegarder la pharmacie
        if ($pharmacy->save()) {
            echo "Pharmacie {$pharmacyData['name']} créée avec succès! ID: " . $pharmacy->getId() . "<br>";
        } else {
            echo "Erreur lors de la création de la pharmacie {$pharmacyData['name']}<br>";
            return false;
        }
    }

    // Vérifier si l'utilisateur existe déjà
    $existingUser = User::findByEmail($userData['email']);

    if ($existingUser) {
        echo "L'utilisateur {$userData['email']} existe déjà avec l'ID: " . $existingUser->getId() . "<br>";
        return true;
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
            echo "Utilisateur {$userData['email']} créé avec succès! ID: " . $user->getId() . "<br>";
            echo "Email: " . $userData['email'] . "<br>";
            echo "Mot de passe: " . $userData['password'] . "<br><br>";
            return true;
        } else {
            echo "Erreur lors de la création de l'utilisateur {$userData['email']}<br>";
            return false;
        }
    }
}

// Compte administrateur principal
$adminPharmacy = [
    'name' => 'Pharmacie Centrale',
    'address' => '1 rue de la Paix, 75001 Paris',
    'phone_number' => '0123456789',
    'email' => 'contact@pharmaciecentrale.fr',
    'registration_number' => 'PC123456',
    'status' => 'active'
];

$adminUser = [
    'email' => 'admin@pharmacie.com',
    'password' => 'Admin2025!',
    'role' => 'admin'
];

// Compte gestionnaire
$managerPharmacy = [
    'name' => 'Pharmacie du Marché',
    'address' => '15 place du Marché, 69002 Lyon',
    'phone_number' => '0478123456',
    'email' => 'contact@pharmaciedumarche.fr',
    'registration_number' => 'PM789012',
    'status' => 'active'
];

$managerUser = [
    'email' => 'manager@pharmacie.com',
    'password' => 'Manager2025!',
    'role' => 'admin'
];

// Compte employé
$employeePharmacy = [
    'name' => 'Pharmacie des Fleurs',
    'address' => '8 boulevard des Fleurs, 33000 Bordeaux',
    'phone_number' => '0556789012',
    'email' => 'contact@pharmaciedesfleurs.fr',
    'registration_number' => 'PF345678',
    'status' => 'active'
];

$employeeUser = [
    'email' => 'employe@pharmacie.com',
    'password' => 'Employe2025!',
    'role' => 'employee'
];

// Créer les comptes
echo "<h2>Création des comptes de test pour l'application lourde</h2>";

echo "<h3>Compte Administrateur</h3>";
createPharmacyAndUser($adminPharmacy, $adminUser);

echo "<h3>Compte Gestionnaire</h3>";
createPharmacyAndUser($managerPharmacy, $managerUser);

echo "<h3>Compte Employé</h3>";
createPharmacyAndUser($employeePharmacy, $employeeUser);

echo "<h2>Récapitulatif des comptes créés</h2>";
echo "<table border='1' cellpadding='5'>";
echo "<tr><th>Type</th><th>Email</th><th>Mot de passe</th><th>Pharmacie</th></tr>";
echo "<tr><td>Administrateur</td><td>{$adminUser['email']}</td><td>{$adminUser['password']}</td><td>{$adminPharmacy['name']}</td></tr>";
echo "<tr><td>Gestionnaire</td><td>{$managerUser['email']}</td><td>{$managerUser['password']}</td><td>{$managerPharmacy['name']}</td></tr>";
echo "<tr><td>Employé</td><td>{$employeeUser['email']}</td><td>{$employeeUser['password']}</td><td>{$employeePharmacy['name']}</td></tr>";
echo "</table>";

echo "<p>Vous pouvez maintenant vous connecter à l'application lourde Java avec ces identifiants.</p>";
?>
