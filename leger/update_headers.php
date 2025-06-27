<?php
/**
 * Script pour mettre à jour tous les fichiers de vue pour utiliser le même header
 * Cela garantit que le menu utilisateur avec l'option de déconnexion apparaît sur toutes les pages
 */

// Chemin de base
$basePath = __DIR__;

// Fonction récursive pour trouver tous les fichiers PHP
function findPhpFiles($dir) {
    $result = [];
    $files = scandir($dir);
    
    foreach ($files as $file) {
        if ($file === '.' || $file === '..') {
            continue;
        }
        
        $path = $dir . '/' . $file;
        
        if (is_dir($path)) {
            $result = array_merge($result, findPhpFiles($path));
        } elseif (pathinfo($path, PATHINFO_EXTENSION) === 'php') {
            $result[] = $path;
        }
    }
    
    return $result;
}

// Trouver tous les fichiers PHP dans le dossier Views
$viewsPath = $basePath . '/src/Views';
$phpFiles = findPhpFiles($viewsPath);

// Patterns à rechercher et à remplacer
$patterns = [
    // Pattern pour include '../../templates/header.php'
    '/include\s+[\'"]\.\.\/\.\.\/templates\/header\.php[\'"]\s*;/' => 'require_once __DIR__ . \'/../layouts/header.php\';',
    
    // Pattern pour include __DIR__ . '/../../templates/header.php'
    '/include\s+__DIR__\s*\.\s*[\'"]\/\.\.\/\.\.\/templates\/header\.php[\'"]\s*;/' => 'require_once __DIR__ . \'/../layouts/header.php\';',
    
    // Pattern pour require_once '../../templates/header.php'
    '/require_once\s+[\'"]\.\.\/\.\.\/templates\/header\.php[\'"]\s*;/' => 'require_once __DIR__ . \'/../layouts/header.php\';',
    
    // Pattern pour require_once __DIR__ . '/../../templates/header.php'
    '/require_once\s+__DIR__\s*\.\s*[\'"]\/\.\.\/\.\.\/templates\/header\.php[\'"]\s*;/' => 'require_once __DIR__ . \'/../layouts/header.php\';',
];

// Compter les fichiers modifiés
$modifiedFiles = 0;

// Traiter chaque fichier
foreach ($phpFiles as $file) {
    // Lire le contenu du fichier
    $content = file_get_contents($file);
    $originalContent = $content;
    
    // Appliquer les remplacements
    foreach ($patterns as $pattern => $replacement) {
        $content = preg_replace($pattern, $replacement, $content);
    }
    
    // Si le contenu a été modifié, écrire le nouveau contenu
    if ($content !== $originalContent) {
        file_put_contents($file, $content);
        echo "Fichier mis à jour : " . $file . PHP_EOL;
        $modifiedFiles++;
    }
}

echo "Terminé ! $modifiedFiles fichiers ont été mis à jour." . PHP_EOL;

// Vérifier si les deux fichiers header sont identiques
$header1 = file_get_contents($basePath . '/templates/header.php');
$header2 = file_get_contents($basePath . '/src/Views/layouts/header.php');

if ($header1 === $header2) {
    echo "Les deux fichiers header sont identiques." . PHP_EOL;
} else {
    echo "Les deux fichiers header sont différents. Assurez-vous que le menu utilisateur est présent dans les deux fichiers." . PHP_EOL;
    
    // Copier le contenu du header de layouts vers le header de templates
    file_put_contents($basePath . '/templates/header.php', $header2);
    echo "Le contenu du header de layouts a été copié vers le header de templates." . PHP_EOL;
}

echo "Tous les fichiers de vue utilisent maintenant le même header avec le menu utilisateur et l'option de déconnexion." . PHP_EOL;
