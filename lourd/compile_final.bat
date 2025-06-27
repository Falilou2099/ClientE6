@echo off
echo ========================================
echo   COMPILATION BIGPHARMA VERSION FINALE
echo ========================================
echo.

echo Compilation des composants de connexion...
javac -cp . MotDePasseOublieSimple.java
if %errorlevel% neq 0 goto error

javac -cp . ConnexionSimple.java
if %errorlevel% neq 0 goto error

echo Compilation de l'application principale...
javac -cp . BigPharmaAutonome.java
if %errorlevel% neq 0 goto error

echo.
echo COMPILATION RÉUSSIE !
echo ========================================
echo.
echo APPLICATION PRÊTE À L'UTILISATION :
echo   java BigPharmaAutonome
echo.
echo FONCTIONNALITÉS IMPLÉMENTÉES :
echo   • Système de connexion sécurisé (SHA-256)
echo   • Gestion des tentatives échouées (max 5)
echo   • Suspension temporaire (30 minutes)
echo   • Réinitialisation de mot de passe par email
echo   • Interface utilisateur moderne Swing
echo   • Gestion de session utilisateur
echo   • Test de connexion à la base de données
echo   • Synchronisation avec application PHP
echo.
echo PRÉREQUIS :
echo   • MySQL Server démarré
echo   • Base de données 'bigpharma' créée
echo   • Driver MySQL JDBC disponible
echo.
echo UTILISATION :
echo   1. Lancez MySQL et créez la base 'bigpharma'
echo   2. Exécutez : java BigPharmaAutonome
echo   3. Cliquez sur "Se connecter" pour tester
echo   4. Utilisez "Test Connexion" pour vérifier la DB
echo.
goto end

:error
echo.
echo ERREUR DE COMPILATION !
echo Vérifiez que tous les fichiers sont présents :
echo   - MotDePasseOublieSimple.java
echo   - ConnexionSimple.java  
echo   - BigPharmaAutonome.java
echo.

:end
pause
