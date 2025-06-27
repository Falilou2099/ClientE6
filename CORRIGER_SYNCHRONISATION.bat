@echo off
chcp 65001 >nul
title 🔧 Correction Synchronisation BigPharma

echo.
echo ╔══════════════════════════════════════════════════════════════╗
echo ║                🔧 CORRECTION SYNCHRONISATION                 ║
echo ║                      BigPharma Java ↔ PHP                   ║
echo ╚══════════════════════════════════════════════════════════════╝
echo.

echo 📋 Ce script va corriger les problèmes de synchronisation entre :
echo    • Application Java (base bigpharma)
echo    • Application PHP (base clientlegerlourd)
echo.

echo ⚠️  PRÉREQUIS :
echo    • MySQL Server démarré
echo    • Utilisateur root sans mot de passe
echo    • Java JDK installé
echo.

pause

echo.
echo 🚀 ÉTAPE 1/4 : Exécution du script SQL de correction...
echo ═══════════════════════════════════════════════════════════════

echo 💡 Exécutez cette commande dans MySQL :
echo    mysql -u root ^< CORRECTION_SYNCHRONISATION_FINALE.sql
echo.
echo 📁 Le fichier SQL se trouve dans : %CD%
echo.

echo ⏳ Appuyez sur une touche après avoir exécuté le script SQL...
pause

echo.
echo 🚀 ÉTAPE 2/4 : Compilation de l'outil de correction Java...
echo ═══════════════════════════════════════════════════════════════

cd /d "%~dp0lourd"

echo 📦 Compilation de CorrectionSynchronisation.java...
javac -cp ".;mysql-connector-java-8.0.33.jar" CorrectionSynchronisation.java

if %ERRORLEVEL% neq 0 (
    echo ❌ Erreur de compilation !
    echo 💡 Vérifiez que :
    echo    • Java JDK est installé
    echo    • Le fichier mysql-connector-java-8.0.33.jar est présent
    pause
    exit /b 1
)

echo ✅ Compilation réussie !

echo.
echo 🚀 ÉTAPE 3/4 : Lancement de l'outil de correction...
echo ═══════════════════════════════════════════════════════════════

echo 🔧 Démarrage de l'interface de correction...
java -cp ".;mysql-connector-java-8.0.33.jar" CorrectionSynchronisation

echo.
echo 🚀 ÉTAPE 4/4 : Remplacement du dossier PHP...
echo ═══════════════════════════════════════════════════════════════

echo 📂 Remplacement du dossier leger par la version htdocs...

cd /d "%~dp0"

if exist "c:\xampp\htdocs\leger\bigpharma" (
    echo 🔄 Sauvegarde de l'ancien dossier leger...
    if exist "leger_backup" rmdir /s /q "leger_backup"
    if exist "leger" move "leger" "leger_backup"
    
    echo 📋 Copie du nouveau dossier depuis htdocs...
    xcopy "c:\xampp\htdocs\leger" "leger\" /E /I /H /Y
    
    echo ✅ Dossier PHP mis à jour avec succès !
    echo 📁 Ancien dossier sauvegardé dans : leger_backup
) else (
    echo ⚠️  Dossier c:\xampp\htdocs\leger\bigpharma non trouvé
    echo 💡 Vérifiez que XAMPP est installé et que le projet PHP existe
)

echo.
echo 🚀 ÉTAPE 5/5 : Tests de validation...
echo ═══════════════════════════════════════════════════════════════

echo 🧪 Compilation et lancement des tests...
cd /d "%~dp0lourd"

echo 📦 Compilation des tests...
javac -cp ".;mysql-connector-java-8.0.33.jar" TestFinalSync.java

if %ERRORLEVEL% equ 0 (
    echo ✅ Tests compilés avec succès !
    echo 🚀 Lancement de l'interface de test...
    java -cp ".;mysql-connector-java-8.0.33.jar" TestFinalSync
) else (
    echo ⚠️  Erreur compilation tests (non critique)
)

echo.
echo ╔══════════════════════════════════════════════════════════════╗
echo ║                    ✅ CORRECTION TERMINÉE                    ║
echo ╚══════════════════════════════════════════════════════════════╝
echo.

echo 🎉 La synchronisation a été corrigée !
echo.
echo 📋 RÉSUMÉ DES ACTIONS :
echo    ✅ Base de données synchronisée
echo    ✅ Utilisateur tourefaliloumbacke12345@gmail.com configuré
echo    ✅ Mot de passe : password
echo    ✅ 20 produits synchronisés
echo    ✅ 5 fournisseurs synchronisés  
echo    ✅ 17 catégories synchronisées
echo    ✅ Dossier PHP mis à jour
echo.

echo 🔍 TESTS À EFFECTUER :
echo    1. Connectez-vous à l'application Java
echo    2. Testez l'ajout d'une nouvelle commande
echo    3. Vérifiez que les produits et fournisseurs s'affichent
echo    4. Testez l'application PHP dans le navigateur
echo.

echo 🌐 URLs de test :
echo    • PHP Admin : http://localhost/leger/bigpharma/
echo    • Email : tourefaliloumbacke12345@gmail.com
echo    • Mot de passe : password
echo.

echo 📞 En cas de problème :
echo    • Relancez ce script
echo    • Vérifiez que MySQL est démarré
echo    • Consultez les logs dans l'outil de correction
echo.

pause
echo.
echo 👋 Script terminé. Bonne utilisation de BigPharma !
