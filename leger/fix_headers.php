<?php
/**
 * Script pour corriger les inclusions de header dans tous les fichiers de vue
 * en utilisant des chemins absolus pour éviter les problèmes de chemins relatifs
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

// Chemin absolu vers le header
$headerPath = $basePath . '/templates/header.php';

// Compter les fichiers modifiés
$modifiedFiles = 0;

// Traiter chaque fichier
foreach ($phpFiles as $file) {
    // Lire le contenu du fichier
    $content = file_get_contents($file);
    $originalContent = $content;
    
    // Patterns à rechercher
    $patterns = [
        // Pattern pour include '../../templates/header.php'
        '/include\s+[\'"]\.\.\/\.\.\/templates\/header\.php[\'"]\s*;/',
        
        // Pattern pour include __DIR__ . '/../../templates/header.php'
        '/include\s+__DIR__\s*\.\s*[\'"]\/\.\.\/\.\.\/templates\/header\.php[\'"]\s*;/',
        
        // Pattern pour require_once '../../templates/header.php'
        '/require_once\s+[\'"]\.\.\/\.\.\/templates\/header\.php[\'"]\s*;/',
        
        // Pattern pour require_once __DIR__ . '/../../templates/header.php'
        '/require_once\s+__DIR__\s*\.\s*[\'"]\/\.\.\/\.\.\/templates\/header\.php[\'"]\s*;/',
        
        // Pattern pour require_once __DIR__ . '/../layouts/header.php'
        '/require_once\s+__DIR__\s*\.\s*[\'"]\/\.\.\/layouts\/header\.php[\'"]\s*;/',
    ];
    
    // Remplacement avec le chemin absolu
    $replacement = "require_once '{$headerPath}';";
    
    // Appliquer les remplacements
    $modified = false;
    foreach ($patterns as $pattern) {
        $newContent = preg_replace($pattern, $replacement, $content);
        if ($newContent !== $content) {
            $content = $newContent;
            $modified = true;
        }
    }
    
    // Si le contenu a été modifié, écrire le nouveau contenu
    if ($modified) {
        file_put_contents($file, $content);
        echo "Fichier mis à jour : " . $file . PHP_EOL;
        $modifiedFiles++;
    }
}

echo "Terminé ! $modifiedFiles fichiers ont été mis à jour." . PHP_EOL;
echo "Tous les fichiers de vue utilisent maintenant le même header avec le menu utilisateur et l'option de déconnexion." . PHP_EOL;
