@echo off
echo Compilation de BigPharma Version Simplifiée...

REM Compilation des classes de base
echo Compilation de SessionManager...
javac -cp . src\main\java\com\gestionpharma\SessionManager.java
if %errorlevel% neq 0 goto error

echo Compilation de MotDePasseOublieSimple...
javac -cp . MotDePasseOublieSimple.java
if %errorlevel% neq 0 goto error

echo Compilation de ConnexionSimple...
javac -cp . ConnexionSimple.java
if %errorlevel% neq 0 goto error

echo Compilation de BigPharmaSimple...
javac -cp . BigPharmaSimple.java
if %errorlevel% neq 0 goto error

echo.
echo ✅ Compilation réussie !
echo.
echo Application disponible :
echo   java BigPharmaSimple        - Application principale simplifiée
echo.
echo Fonctionnalités implémentées :
echo   • Système de connexion sécurisé (SHA-256)
echo   • Gestion des tentatives échouées (max 5)
echo   • Suspension temporaire (30 minutes)
echo   • Réinitialisation de mot de passe par email
echo   • Interface utilisateur moderne
echo   • Gestion de session utilisateur
echo   • Test de connexion à la base de données
echo.
echo Pour tester l'application :
echo   1. Assurez-vous que MySQL est démarré
echo   2. Créez la base 'bigpharma' si nécessaire
echo   3. Lancez : java BigPharmaSimple
echo.
goto end

:error
echo.
echo ❌ Erreur de compilation !
echo Vérifiez que toutes les classes nécessaires sont présentes.
echo.

:end
pause
