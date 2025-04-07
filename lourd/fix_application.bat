@echo off
echo ======================================================
echo    Reparation complete de l'application BigPharma
echo ======================================================
echo.

REM Definir le chemin vers MySQL (ajuster selon votre installation)
set MYSQL_PATH=C:\xampp\mysql\bin
set PROJECT_PATH=%~dp0
set DB_SCRIPT=%PROJECT_PATH%database\complete_database_rebuild.sql

echo Etape 1: Reconstruction de la base de donnees...
echo ------------------------------------------------------
cd /d %MYSQL_PATH%
mysql -u root < "%DB_SCRIPT%"

if %ERRORLEVEL% NEQ 0 (
    echo ERREUR: Impossible de reconstruire la base de donnees.
    echo Verifiez que MySQL est en cours d'execution dans XAMPP.
    pause
    exit /b 1
)

echo Base de donnees reconstruite avec succes!
echo.

echo Etape 2: Nettoyage et recompilation de l'application...
echo ------------------------------------------------------
cd /d %PROJECT_PATH%
call mvn clean

if %ERRORLEVEL% NEQ 0 (
    echo ERREUR: Impossible de nettoyer le projet.
    pause
    exit /b 1
)

echo Compilation du projet...
call mvn compile

if %ERRORLEVEL% NEQ 0 (
    echo ERREUR: Impossible de compiler le projet.
    pause
    exit /b 1
)

echo Projet compile avec succes!
echo.

echo Etape 3: Execution de l'application...
echo ------------------------------------------------------
echo L'application va demarrer avec les informations suivantes:
echo.
echo Base de donnees: bigpharma
echo Utilisateur admin: admin
echo Mot de passe: Admin123!
echo.
echo Appuyez sur une touche pour lancer l'application...
pause > nul

call mvn javafx:run

echo.
echo Si l'application ne se lance pas correctement, vous pouvez essayer:
echo 1. Verifier que XAMPP est en cours d'execution
echo 2. Verifier que Java 17+ et Maven sont correctement installes
echo 3. Executer la commande 'mvn clean javafx:run' manuellement
echo.
pause
