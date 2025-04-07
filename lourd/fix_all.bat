@echo off
echo ================================================================
echo     REPARATION COMPLETE DE L'APPLICATION BIGPHARMA
echo ================================================================
echo.

REM Définir les chemins
set MYSQL_PATH=C:\xampp\mysql\bin
set PROJECT_PATH=%~dp0
set DB_SCRIPT=%PROJECT_PATH%database\complete_database_rebuild.sql

echo ETAPE 1: Verification de MySQL...
echo ----------------------------------------------------------------
IF NOT EXIST "%MYSQL_PATH%\mysql.exe" (
    echo ERREUR: MySQL non trouve dans %MYSQL_PATH%
    echo Verifiez que XAMPP est installe correctement.
    echo Si vous utilisez un autre serveur MySQL, modifiez la variable MYSQL_PATH.
    goto error
)

echo ETAPE 2: Demarrage des services XAMPP...
echo ----------------------------------------------------------------
start /wait xampp-control.exe

echo ETAPE 3: Reconstruction de la base de donnees...
echo ----------------------------------------------------------------
cd /d "%MYSQL_PATH%"

REM Créer la base de données bigpharma
echo -- Creation de la base de donnees bigpharma...
mysql -u root -e "CREATE DATABASE IF NOT EXISTS bigpharma;"
if %ERRORLEVEL% NEQ 0 (
    echo ERREUR: Impossible de creer la base de donnees.
    echo Verifiez que MySQL est en cours d'execution dans XAMPP.
    goto error
)

REM Exécuter le script SQL complet
echo -- Execution du script de reconstruction...
mysql -u root bigpharma < "%DB_SCRIPT%"
if %ERRORLEVEL% NEQ 0 (
    echo ERREUR: Impossible d'executer le script SQL.
    goto error
)

REM Créer un alias pour que clientlegerlourd pointe vers bigpharma
echo -- Configuration de l'alias clientlegerlourd...
mysql -u root -e "CREATE DATABASE IF NOT EXISTS clientlegerlourd;"
mysql -u root -e "DROP DATABASE IF EXISTS clientlegerlourd;"
mysql -u root -e "CREATE DATABASE clientlegerlourd;"

REM Copier toutes les tables de bigpharma vers clientlegerlourd
echo -- Synchronisation des bases de donnees...
for /f %%i in ('mysql -u root -N -e "SHOW TABLES FROM bigpharma"') do (
    echo   - Copie de la table %%i...
    mysql -u root -e "CREATE TABLE clientlegerlourd.%%i LIKE bigpharma.%%i; INSERT INTO clientlegerlourd.%%i SELECT * FROM bigpharma.%%i;"
)

echo Base de donnees reconstruite avec succes!
echo.

echo ETAPE 4: Nettoyage et recompilation de l'application...
echo ----------------------------------------------------------------
cd /d "%PROJECT_PATH%"
call mvn clean
if %ERRORLEVEL% NEQ 0 (
    echo ERREUR: Impossible de nettoyer le projet.
    goto error
)

echo Compilation du projet...
call mvn compile
if %ERRORLEVEL% NEQ 0 (
    echo ERREUR: Impossible de compiler le projet.
    goto error
)

echo Projet compile avec succes!
echo.

echo ETAPE 5: Execution de l'application...
echo ----------------------------------------------------------------
echo L'application va demarrer avec les informations suivantes:
echo.
echo Base de donnees: bigpharma
echo Utilisateur: admin
echo Mot de passe: Admin123!
echo.
echo Appuyez sur une touche pour lancer l'application...
pause > nul

call mvn javafx:run
if %ERRORLEVEL% NEQ 0 (
    echo ERREUR: Impossible de lancer l'application.
    goto error
)

goto end

:error
echo.
echo ================================================================
echo ERREUR: La reparation a echoue. Voici quelques solutions:
echo 1. Verifiez que XAMPP est en cours d'execution
echo 2. Assurez-vous que MySQL est accessible
echo 3. Verifiez que Java et Maven sont correctement installes
echo 4. Essayez d'executer les commandes suivantes manuellement:
echo    - cd "%PROJECT_PATH%"
echo    - mvn clean javafx:run
echo ================================================================
echo.
pause
exit /b 1

:end
echo.
echo ================================================================
echo FELICITATIONS! L'application est maintenant reparee et en cours
echo d'execution. Toutes les fonctionnalites devraient etre operationnelles.
echo ================================================================
echo.
pause
