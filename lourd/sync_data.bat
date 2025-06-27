@echo off
echo ========================================
echo   SYNCHRONISATION BIGPHARMA JAVA ↔ PHP
echo ========================================
echo.

echo 🔧 ÉTAPE 1: Compilation de l'outil de synchronisation...
javac -cp . SynchronisationDonnees.java
if %errorlevel% neq 0 (
    echo ❌ Erreur de compilation !
    goto end
)
echo ✅ Compilation réussie

echo.
echo 🗄️  ÉTAPE 2: Exécution du script SQL de synchronisation...
echo.
echo Pour exécuter le script SQL automatiquement :
echo mysql -u root -p bigpharma ^< sync_database.sql
echo.
echo Ou copiez-collez le contenu de sync_database.sql dans phpMyAdmin
echo.

echo 🚀 ÉTAPE 3: Lancement de l'outil de synchronisation...
echo.
echo L'outil va :
echo • Vérifier la connexion à la base de données
echo • Créer/vérifier l'utilisateur tourefaliloumbacke12345@gmail.com
echo • Synchroniser les produits, fournisseurs et catégories
echo • Assurer la cohérence entre Java et PHP
echo.

pause
java SynchronisationDonnees

:end
echo.
echo Synchronisation terminée.
pause
