<?php
/**
 * Script pour ajouter 10 000 clients dans la base de données
 */

// Chemins absolus pour les inclusions
define('ROOT_PATH', dirname(__DIR__));
define('CONFIG_PATH', ROOT_PATH . '/config');

// Connexion à la base de données
require_once CONFIG_PATH . '/database.php';

echo "Début de l'ajout de 10 000 clients...\n";

// Fonction pour générer un nom aléatoire
function getRandomFirstName() {
    $firstNames = [
        'Jean', 'Marie', 'Pierre', 'Sophie', 'Michel', 'Isabelle', 'Philippe', 'Nathalie', 
        'François', 'Catherine', 'Nicolas', 'Sylvie', 'David', 'Christine', 'Thomas', 'Julie', 
        'Laurent', 'Céline', 'Stéphane', 'Valérie', 'Éric', 'Caroline', 'Patrick', 'Sandrine', 
        'Christophe', 'Véronique', 'Daniel', 'Martine', 'Frédéric', 'Anne', 'Olivier', 'Émilie',
        'Thierry', 'Aurélie', 'Pascal', 'Stéphanie', 'Sébastien', 'Patricia', 'Alexandre', 'Delphine',
        'Julien', 'Mélanie', 'Gérard', 'Chantal', 'Alain', 'Monique', 'Yves', 'Françoise',
        'Bernard', 'Nicole', 'Didier', 'Jacqueline', 'Gilles', 'Brigitte', 'Henri', 'Jeanne',
        'Jacques', 'Élisabeth', 'André', 'Hélène', 'Christian', 'Dominique', 'Serge', 'Corinne',
        'Luc', 'Laurence', 'Marc', 'Virginie', 'Mathieu', 'Claire', 'Bertrand', 'Pauline',
        'Benoît', 'Camille', 'Fabien', 'Audrey', 'Jérôme', 'Laure', 'Rémi', 'Manon',
        'Maxime', 'Élodie', 'Romain', 'Justine', 'Ludovic', 'Anaïs', 'Vincent', 'Marion',
        'Florian', 'Léa', 'Cédric', 'Laura', 'Guillaume', 'Sarah', 'Loïc', 'Charlotte',
        'Arnaud', 'Lucie', 'Damien', 'Amandine', 'Franck', 'Mathilde', 'Cyril', 'Chloé',
        'Michaël', 'Aurélia', 'Kevin', 'Morgane', 'Anthony', 'Élise', 'Jonathan', 'Fanny'
    ];
    
    return $firstNames[array_rand($firstNames)];
}

// Fonction pour générer un nom de famille aléatoire
function getRandomLastName() {
    $lastNames = [
        'Martin', 'Bernard', 'Dubois', 'Thomas', 'Robert', 'Richard', 'Petit', 'Durand',
        'Leroy', 'Moreau', 'Simon', 'Laurent', 'Lefebvre', 'Michel', 'Garcia', 'David',
        'Bertrand', 'Roux', 'Vincent', 'Fournier', 'Morel', 'Girard', 'André', 'Lefevre',
        'Mercier', 'Dupont', 'Lambert', 'Bonnet', 'François', 'Martinez', 'Legrand', 'Garnier',
        'Faure', 'Rousseau', 'Blanc', 'Guérin', 'Muller', 'Henry', 'Roussel', 'Nicolas',
        'Perrin', 'Morin', 'Mathieu', 'Clément', 'Gauthier', 'Dumont', 'Lopez', 'Fontaine',
        'Chevalier', 'Robin', 'Masson', 'Sanchez', 'Gérard', 'Nguyen', 'Boyer', 'Denis',
        'Lemaire', 'Duval', 'Joly', 'Gautier', 'Roger', 'Roche', 'Roy', 'Noël',
        'Meyer', 'Lucas', 'Meunier', 'Jean', 'Perez', 'Marchand', 'Dufour', 'Blanchard',
        'Lemoine', 'Olivier', 'Philippe', 'Bourgeois', 'Pierre', 'Benoît', 'Rey', 'Leclerc',
        'Payet', 'Rolland', 'Leclercq', 'Guillaume', 'Lecomte', 'Vidal', 'Caron', 'Picard',
        'Giraud', 'Schmitt', 'Colin', 'Fernandez', 'Leroux', 'Renard', 'Arnaud', 'Aubert',
        'Hubert', 'Rivière', 'Brun', 'Brunet', 'Schmitt', 'Rey', 'Hoarau', 'Maillard',
        'Menard', 'Rodriguez', 'Guichard', 'Gillet', 'Étienne', 'Grondin', 'Poulain', 'Tessier'
    ];
    
    return $lastNames[array_rand($lastNames)];
}

// Fonction pour générer une adresse email aléatoire
function getRandomEmail($firstName, $lastName) {
    $domains = ['gmail.com', 'yahoo.fr', 'hotmail.com', 'outlook.fr', 'orange.fr', 'free.fr', 'sfr.fr', 'laposte.net'];
    $domain = $domains[array_rand($domains)];
    
    // Normaliser les noms (enlever les accents, etc.)
    $firstName = strtolower(iconv('UTF-8', 'ASCII//TRANSLIT', $firstName));
    $lastName = strtolower(iconv('UTF-8', 'ASCII//TRANSLIT', $lastName));
    
    $formats = [
        "{$firstName}.{$lastName}@{$domain}",
        "{$firstName}{$lastName}@{$domain}",
        "{$firstName}.{$lastName}" . rand(1, 99) . "@{$domain}",
        "{$firstName[0]}{$lastName}@{$domain}",
        "{$firstName}{$lastName[0]}@{$domain}",
        "{$lastName}.{$firstName}@{$domain}"
    ];
    
    return $formats[array_rand($formats)];
}

// Fonction pour générer un numéro de téléphone français aléatoire
function getRandomPhoneNumber() {
    $prefixes = ['06', '07'];
    $prefix = $prefixes[array_rand($prefixes)];
    
    return $prefix . ' ' . implode(' ', [
        substr(str_shuffle('0123456789'), 0, 2),
        substr(str_shuffle('0123456789'), 0, 2),
        substr(str_shuffle('0123456789'), 0, 2),
        substr(str_shuffle('0123456789'), 0, 2)
    ]);
}

// Fonction pour générer une adresse aléatoire
function getRandomAddress() {
    $streetNumbers = range(1, 150);
    $streetTypes = ['rue', 'avenue', 'boulevard', 'place', 'impasse', 'allée', 'chemin'];
    $streetNames = [
        'de la Paix', 'des Lilas', 'Victor Hugo', 'Jean Jaurès', 'du Général de Gaulle',
        'de la République', 'des Roses', 'de la Liberté', 'du Moulin', 'de l\'Église',
        'des Écoles', 'de la Gare', 'des Champs', 'du Château', 'de la Fontaine',
        'du Stade', 'des Tilleuls', 'des Acacias', 'du Maréchal Foch', 'de la Mairie',
        'des Cerisiers', 'Pasteur', 'du 8 Mai 1945', 'de Verdun', 'des Peupliers'
    ];
    $cities = [
        'Paris', 'Lyon', 'Marseille', 'Toulouse', 'Nice', 'Nantes', 'Strasbourg', 'Montpellier',
        'Bordeaux', 'Lille', 'Rennes', 'Reims', 'Saint-Étienne', 'Toulon', 'Le Havre',
        'Grenoble', 'Dijon', 'Angers', 'Nîmes', 'Villeurbanne', 'Clermont-Ferrand', 'Le Mans',
        'Aix-en-Provence', 'Brest', 'Tours', 'Amiens', 'Limoges', 'Annecy', 'Perpignan', 'Besançon'
    ];
    $zipCodes = [
        '75000', '69000', '13000', '31000', '06000', '44000', '67000', '34000',
        '33000', '59000', '35000', '51100', '42000', '83000', '76600', '38000',
        '21000', '49000', '30000', '69100', '63000', '72000', '13100', '29200',
        '37000', '80000', '87000', '74000', '66000', '25000'
    ];
    
    $streetNumber = $streetNumbers[array_rand($streetNumbers)];
    $streetType = $streetTypes[array_rand($streetTypes)];
    $streetName = $streetNames[array_rand($streetNames)];
    $cityIndex = array_rand($cities);
    
    return $streetNumber . ' ' . $streetType . ' ' . $streetName . ', ' . $zipCodes[$cityIndex] . ' ' . $cities[$cityIndex];
}

// Préparer la requête d'insertion
$stmt = $pdo->prepare("
    INSERT INTO clients (
        nom, prenom, email, telephone, adresse, pharmacie_id, date_creation
    ) VALUES (
        :nom, :prenom, :email, :telephone, :adresse, :pharmacie_id, :date_creation
    )
");

// Définir le nombre de clients à ajouter
$clientCount = 10000;
$batchSize = 100; // Nombre de clients à insérer par lot
$totalInserted = 0;
$startTime = microtime(true);

// Récupérer les IDs des pharmacies existantes
$pharmacyStmt = $pdo->query("SELECT id FROM pharmacies");
$pharmacyIds = $pharmacyStmt->fetchAll(PDO::FETCH_COLUMN);

// S'il n'y a pas de pharmacies, en créer une par défaut
if (empty($pharmacyIds)) {
    $pdo->exec("INSERT INTO pharmacies (nom, adresse, telephone, email) VALUES ('Pharmacie Principale', '1 rue de la Santé, 75000 Paris', '01 23 45 67 89', 'contact@pharmacie-principale.fr')");
    $pharmacyIds = [1];
    echo "Aucune pharmacie trouvée, création d'une pharmacie par défaut (ID: 1)\n";
}

echo "Ajout de {$clientCount} clients en cours...\n";

try {
    // Commencer une transaction
    $pdo->beginTransaction();
    
    for ($i = 0; $i < $clientCount; $i++) {
        $firstName = getRandomFirstName();
        $lastName = getRandomLastName();
        
        $params = [
            ':nom' => $lastName,
            ':prenom' => $firstName,
            ':email' => getRandomEmail($firstName, $lastName),
            ':telephone' => getRandomPhoneNumber(),
            ':adresse' => getRandomAddress(),
            ':pharmacie_id' => $pharmacyIds[array_rand($pharmacyIds)],
            ':date_creation' => date('Y-m-d H:i:s')
        ];
        
        $stmt->execute($params);
        $totalInserted++;
        
        // Afficher la progression tous les 500 clients
        if ($totalInserted % 500 === 0) {
            $progress = ($totalInserted / $clientCount) * 100;
            $elapsedTime = microtime(true) - $startTime;
            $estimatedTotalTime = ($elapsedTime / $totalInserted) * $clientCount;
            $remainingTime = $estimatedTotalTime - $elapsedTime;
            
            echo sprintf(
                "Progression: %d/%d (%.2f%%) - Temps écoulé: %.2f s - Temps restant estimé: %.2f s\n",
                $totalInserted, $clientCount, $progress, $elapsedTime, $remainingTime
            );
            
            // Valider la transaction par lots pour éviter une transaction trop grande
            if ($totalInserted % $batchSize === 0) {
                $pdo->commit();
                $pdo->beginTransaction();
            }
        }
    }
    
    // Valider la dernière transaction
    $pdo->commit();
    
    $totalTime = microtime(true) - $startTime;
    echo "\nAjout terminé avec succès!\n";
    echo "Nombre total de clients ajoutés: {$totalInserted}\n";
    echo "Temps total d'exécution: " . number_format($totalTime, 2) . " secondes\n";
    echo "Moyenne: " . number_format($totalInserted / $totalTime, 2) . " clients/seconde\n";
    
} catch (Exception $e) {
    // En cas d'erreur, annuler la transaction
    $pdo->rollBack();
    echo "Erreur lors de l'ajout des clients: " . $e->getMessage() . "\n";
    exit(1);
}
