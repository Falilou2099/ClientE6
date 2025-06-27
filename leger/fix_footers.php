<?php
/**
 * Script pour corriger les inclusions de footer dans tous les fichiers de vue
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

// Chemin absolu vers le footer
$footerPath = $basePath . '/templates/footer.php';

// Compter les fichiers modifiés
$modifiedFiles = 0;

// Traiter chaque fichier
foreach ($phpFiles as $file) {
    // Lire le contenu du fichier
    $content = file_get_contents($file);
    $originalContent = $content;
    
    // Patterns à rechercher
    $patterns = [
        // Pattern pour include '../../templates/footer.php'
        '/include\s+[\'"]\.\.\/\.\.\/templates\/footer\.php[\'"]\s*;/',
        
        // Pattern pour include __DIR__ . '/../../templates/footer.php'
        '/include\s+__DIR__\s*\.\s*[\'"]\/\.\.\/\.\.\/templates\/footer\.php[\'"]\s*;/',
        
        // Pattern pour require_once '../../templates/footer.php'
        '/require_once\s+[\'"]\.\.\/\.\.\/templates\/footer\.php[\'"]\s*;/',
        
        // Pattern pour require_once __DIR__ . '/../../templates/footer.php'
        '/require_once\s+__DIR__\s*\.\s*[\'"]\/\.\.\/\.\.\/templates\/footer\.php[\'"]\s*;/',
        
        // Pattern pour require_once __DIR__ . '/../layouts/footer.php'
        '/require_once\s+__DIR__\s*\.\s*[\'"]\/\.\.\/layouts\/footer\.php[\'"]\s*;/',
    ];
    
    // Remplacement avec le chemin absolu
    $replacement = "require_once '{$footerPath}';";
    
    // Appliquer les remplacements
    $modified = false;
    foreach ($patterns as $pattern) {
        $newContent = preg_replace($pattern, $replacement, $content);
        if ($newContent !== $content) {
            $content = $newContent;
            $modified = true;
        }
    }
    
    // Vérifier si le fichier contient déjà le footer
    $containsFooter = (strpos($content, 'footer.php') !== false);
    
    // Si le fichier ne contient pas de footer, ajouter le footer à la fin
    if (!$containsFooter && !preg_match('/\<\/html\>\s*$/', $content)) {
        // Ajouter le footer avant la fin du fichier
        $content .= "\n<?php require_once '{$footerPath}'; ?>\n";
        $modified = true;
    }
    
    // Si le contenu a été modifié, écrire le nouveau contenu
    if ($modified) {
        file_put_contents($file, $content);
        echo "Fichier mis à jour : " . $file . PHP_EOL;
        $modifiedFiles++;
    }
}

echo "Terminé ! $modifiedFiles fichiers ont été mis à jour." . PHP_EOL;
echo "Tous les fichiers de vue incluent maintenant correctement le footer avec les scripts JavaScript nécessaires." . PHP_EOL;

// Maintenant, créons un fichier JavaScript personnalisé pour garantir que les menus déroulants fonctionnent
$jsContent = <<<EOT
/**
 * Script personnalisé pour garantir que les menus déroulants Bootstrap fonctionnent correctement
 */
document.addEventListener('DOMContentLoaded', function() {
    // Initialiser tous les dropdowns Bootstrap
    var dropdownElementList = [].slice.call(document.querySelectorAll('.dropdown-toggle'));
    var dropdownList = dropdownElementList.map(function(dropdownToggleEl) {
        return new bootstrap.Dropdown(dropdownToggleEl);
    });
    
    // Ajouter un gestionnaire d'événements pour les clics sur les boutons dropdown
    document.querySelectorAll('.dropdown-toggle').forEach(function(element) {
        element.addEventListener('click', function(e) {
            e.stopPropagation();
            var dropdown = bootstrap.Dropdown.getInstance(element);
            if (!dropdown) {
                dropdown = new bootstrap.Dropdown(element);
            }
            dropdown.toggle();
        });
    });
    
    console.log('Dropdowns initialized successfully');
});
EOT;

// Créer le dossier js s'il n'existe pas
$jsDir = $basePath . '/public/js';
if (!file_exists($jsDir)) {
    mkdir($jsDir, 0777, true);
    echo "Dossier js créé : $jsDir" . PHP_EOL;
}

// Écrire le fichier JavaScript
$jsFile = $jsDir . '/dropdown.js';
file_put_contents($jsFile, $jsContent);
echo "Fichier JavaScript créé : $jsFile" . PHP_EOL;

// Mettre à jour le footer pour inclure le fichier JavaScript personnalisé
$footerContent = file_get_contents($footerPath);
if (strpos($footerContent, 'dropdown.js') === false) {
    $newFooterContent = str_replace(
        '<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.2.3/dist/js/bootstrap.min.js"></script>',
        '<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.2.3/dist/js/bootstrap.min.js"></script>' . PHP_EOL . '    <script src="/bigpharma/public/js/dropdown.js"></script>',
        $footerContent
    );
    file_put_contents($footerPath, $newFooterContent);
    echo "Footer mis à jour avec le script JavaScript personnalisé." . PHP_EOL;
}

echo "Terminé ! Le menu déroulant de l'utilisateur devrait maintenant fonctionner sur toutes les pages." . PHP_EOL;
