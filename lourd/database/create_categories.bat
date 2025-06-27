@echo off
echo ======================================================
echo    Création de la table des catégories BigPharma
echo ======================================================
echo.

REM Vérifier si MySQL est accessible
echo Vérification de l'accès à MySQL...
mysql --version > nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    echo ERREUR: MySQL n'est pas accessible depuis la ligne de commande.
    echo Assurez-vous que MySQL est installé et que le chemin est dans la variable PATH.
    echo.
    echo Si vous utilisez XAMPP, essayez d'exécuter ce script depuis le dossier:
    echo C:\xampp\mysql\bin
    pause
    exit /b 1
)

echo MySQL est accessible.
echo.

REM Exécuter le script SQL pour créer la table des catégories
echo Création de la table des catégories dans la base de données...
mysql -u root < create_categories.sql

if %ERRORLEVEL% NEQ 0 (
    echo ERREUR: Impossible d'exécuter le script SQL.
    pause
    exit /b 1
)

echo.
echo Table des catégories créée avec succès!
echo.
echo Vous pouvez maintenant relancer l'application.
echo.
pause
