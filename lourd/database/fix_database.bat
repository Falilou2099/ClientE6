@echo off
echo ======================================================
echo    Correction de la base de données BigPharma
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

REM Exécuter le script SQL pour créer les tables manquantes
echo Création des tables manquantes dans la base de données...
mysql -u root < create_missing_tables.sql

if %ERRORLEVEL% NEQ 0 (
    echo ERREUR: Impossible d'exécuter le script SQL.
    pause
    exit /b 1
)

echo.
echo Base de données mise à jour avec succès!
echo Les tables manquantes ont été créées.
echo.
echo Vous pouvez maintenant relancer l'application.
echo.
pause
