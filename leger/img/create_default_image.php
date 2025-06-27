<?php
// Script pour créer une image par défaut pour les produits sans images

// Dimensions de l'image
$width = 300;
$height = 300;

// Créer une image vide
$image = imagecreatetruecolor($width, $height);

// Définir les couleurs
$bgColor = imagecolorallocate($image, 240, 240, 240); // Gris clair
$textColor = imagecolorallocate($image, 100, 100, 100); // Gris foncé
$borderColor = imagecolorallocate($image, 200, 200, 200); // Gris moyen

// Remplir l'arrière-plan
imagefill($image, 0, 0, $bgColor);

// Dessiner un cadre
imagerectangle($image, 0, 0, $width - 1, $height - 1, $borderColor);

// Dessiner un logo de médicament stylisé
$rx = imagecolorallocate($image, 70, 130, 180); // Bleu
imageline($image, 100, 120, 200, 120, $rx);
imageline($image, 120, 120, 120, 180, $rx);
imagearc($image, 120, 100, 40, 40, 0, 360, $rx);

// Ajouter le texte "Image non disponible"
$font = 5; // Utiliser une police intégrée
$text = "Image non disponible";
$textWidth = imagefontwidth($font) * strlen($text);
$textHeight = imagefontheight($font);
$x = ($width - $textWidth) / 2;
$y = 220;
imagestring($image, $font, $x, $y, $text, $textColor);

// Ajouter le logo BigPharma
$text2 = "BigPharma";
$textWidth2 = imagefontwidth($font) * strlen($text2);
$x2 = ($width - $textWidth2) / 2;
$y2 = 240;
imagestring($image, $font, $x2, $y2, $text2, $rx);

// Sauvegarder l'image
imagepng($image, __DIR__ . '/default_product.png');
imagedestroy($image);

echo "Image par défaut créée avec succès: " . __DIR__ . '/default_product.png';
?>
