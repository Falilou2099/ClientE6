@echo off
echo Compilation de BigPharma Simplifié avec système de connexion...

REM Compilation des classes de base
echo Compilation de DatabaseConfigSimple...
javac -cp . src\main\java\com\gestionpharma\config\DatabaseConfigSimple.java
if %errorlevel% neq 0 goto error

echo Compilation de SessionManager...
javac -cp . src\main\java\com\gestionpharma\SessionManager.java
if %errorlevel% neq 0 goto error

echo Compilation de MotDePasseOublieDialog...
javac -cp . src\main\java\com\gestionpharma\MotDePasseOublieDialog.java
if %errorlevel% neq 0 goto error

echo Compilation de ConnexionDialog...
javac -cp . src\main\java\com\gestionpharma\ConnexionDialog.java
if %errorlevel% neq 0 goto error

echo Compilation de BigPharmaSimple...
javac -cp . BigPharmaSimple.java
if %errorlevel% neq 0 goto error

echo Compilation de TestConnexion...
javac -cp . TestConnexion.java
if %errorlevel% neq 0 goto error

echo.
echo Compilation réussie !
echo.
echo Applications disponibles :
echo   java BigPharmaSimple        - Application principale
echo   java TestConnexion          - Test du système de connexion
echo.
echo Fonctionnalités implémentées :
echo   • Système de connexion sécurisé (SHA-256)
echo   • Gestion des tentatives échouées (max 5)
echo   • Suspension temporaire (30 minutes)
echo   • Réinitialisation de mot de passe
echo   • Interface utilisateur moderne
echo   • Script de synchronisation PHP
echo.
goto end

:error
echo.
echo Erreur de compilation !
echo Vérifiez que toutes les classes nécessaires sont présentes.
echo.

:end
pause
