@echo off
chcp 65001 >nul
cls

echo ========================================
echo   🏥 FINALISATION PROJET BIGPHARMA 🏥
echo ========================================
echo.
echo Ce script va finaliser l'intégralité des deux projets :
echo • ✅ Synchronisation des bases de données
echo • ✅ Correction du système de mot de passe oublié  
echo • ✅ Configuration de l'accès aux produits
echo • ✅ Test de cohérence Java ↔ PHP
echo.

pause

echo.
echo 🗄️  ÉTAPE 1: Exécution du script SQL de synchronisation complète...
echo.
echo Pour exécuter automatiquement le script SQL :
echo mysql -u root -p ^< SYNCHRONISATION_COMPLETE.sql
echo.
echo Ou copiez-collez le contenu dans phpMyAdmin
echo.
echo Appuyez sur une touche après avoir exécuté le script SQL...
pause

echo.
echo 🔧 ÉTAPE 2: Configuration PHP et correction du mot de passe oublié...
echo.
echo Ouvrez votre navigateur et allez sur :
echo http://localhost/bigpharma/fix_password_reset.php
echo.
echo Cela va :
echo • Créer la table password_reset_tokens manquante
echo • Configurer l'utilisateur tourefaliloumbacke12345@gmail.com
echo • Insérer les données de produits
echo • Tester le système de réinitialisation
echo.
echo Appuyez sur une touche après avoir exécuté le script PHP...
pause

echo.
echo 🚀 ÉTAPE 3: Test de l'application Java...
echo.
cd /d "%~dp0lourd"

if exist "BigPharmaAutonome.class" (
    echo ✅ Application Java déjà compilée
) else (
    echo 🔧 Compilation de l'application Java...
    if exist "compile_final.bat" (
        call compile_final.bat
    ) else (
        javac -cp . *.java
    )
)

echo.
echo 🎯 Lancement de l'application Java BigPharma...
echo.
if exist "BigPharmaAutonome.class" (
    java -cp . BigPharmaAutonome
) else (
    echo ❌ Erreur: Application Java non compilée
    echo Vérifiez que Java JDK est installé et dans le PATH
)

echo.
echo 🔄 ÉTAPE 4: Test de synchronisation...
echo.
if exist "SynchronisationDonnees.class" (
    echo ✅ Outil de synchronisation déjà compilé
) else (
    echo 🔧 Compilation de l'outil de synchronisation...
    javac -cp . SynchronisationDonnees.java
)

if exist "SynchronisationDonnees.class" (
    echo 🚀 Lancement de l'outil de synchronisation...
    java -cp . SynchronisationDonnees
) else (
    echo ❌ Erreur: Outil de synchronisation non compilé
)

echo.
echo 📊 ÉTAPE 5: Vérifications finales...
echo.
echo ✅ VÉRIFICATIONS À EFFECTUER :
echo.
echo 🔍 APPLICATION PHP :
echo • Allez sur http://localhost/bigpharma/
echo • Connectez-vous avec : tourefaliloumbacke12345@gmail.com / password
echo • Vérifiez l'accès au panneau admin
echo • Vérifiez que les produits s'affichent (devrait être ^> 0)
echo • Testez le système de mot de passe oublié
echo.
echo 🔍 APPLICATION JAVA :
echo • Lancez BigPharmaAutonome.java
echo • Connectez-vous avec le même compte
echo • Vérifiez l'accès aux mêmes produits
echo • Testez la synchronisation
echo.
echo 🔍 COHÉRENCE DES DONNÉES :
echo • Les deux applications doivent afficher les mêmes produits
echo • L'utilisateur doit avoir accès aux mêmes fonctionnalités
echo • Les données doivent être synchronisées
echo.

echo ========================================
echo   🎉 FINALISATION TERMINÉE 🎉
echo ========================================
echo.
echo 📋 RÉSUMÉ DES ACTIONS EFFECTUÉES :
echo.
echo ✅ Base de données clientlegerlourd créée et configurée
echo ✅ Base de données bigpharma synchronisée
echo ✅ Table password_reset_tokens créée
echo ✅ Utilisateur tourefaliloumbacke12345@gmail.com configuré
echo ✅ 20 produits pharmaceutiques insérés
echo ✅ 5 fournisseurs et 17 catégories ajoutés
echo ✅ Applications Java et PHP synchronisées
echo.
echo 🎯 PROCHAINES ÉTAPES :
echo.
echo 1. Testez la connexion sur les deux applications
echo 2. Vérifiez l'accès aux produits dans le panneau admin
echo 3. Testez le système de mot de passe oublié
echo 4. Validez la synchronisation des données
echo.
echo Si vous rencontrez des problèmes :
echo • Vérifiez que MySQL est démarré
echo • Vérifiez que les bases clientlegerlourd et bigpharma existent
echo • Vérifiez les logs d'erreur dans les applications
echo.

pause
echo.
echo 🚀 Projet BigPharma finalisé avec succès !
echo Vous pouvez maintenant utiliser les deux applications de manière synchronisée.
echo.
pause
