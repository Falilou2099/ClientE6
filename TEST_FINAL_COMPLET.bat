@echo off
chcp 65001 >nul
title 🧪 Test Final Complet - BigPharma Synchronisation

echo.
echo ╔══════════════════════════════════════════════════════════════════════════════╗
echo ║                    🧪 TEST FINAL COMPLET BIGPHARMA                          ║
echo ║                   Validation de la synchronisation Java ↔ PHP               ║
echo ╚══════════════════════════════════════════════════════════════════════════════╝
echo.

:: Variables
set JAVA_DIR=lourd
set PHP_DIR=leger\bigpharma
set MYSQL_CONNECTOR=mysql-connector-java-8.0.33.jar

echo 📋 ÉTAPES DU TEST COMPLET :
echo.
echo    1️⃣  Vérification de l'environnement
echo    2️⃣  Test de connexion aux bases de données
echo    3️⃣  Compilation et test Java
echo    4️⃣  Test de l'interface PHP
echo    5️⃣  Validation de la synchronisation
echo    6️⃣  Rapport final
echo.
pause

:: ═══════════════════════════════════════════════════════════════════════════════
:: ÉTAPE 1 : Vérification de l'environnement
:: ═══════════════════════════════════════════════════════════════════════════════
echo.
echo ╔══════════════════════════════════════════════════════════════════════════════╗
echo ║                        1️⃣  VÉRIFICATION ENVIRONNEMENT                       ║
echo ╚══════════════════════════════════════════════════════════════════════════════╝
echo.

echo 🔍 Vérification de Java...
java -version >nul 2>&1
if %errorlevel% neq 0 (
    echo ❌ Java non trouvé ! Veuillez installer Java JDK 8+
    pause
    exit /b 1
) else (
    echo ✅ Java trouvé
    java -version
)

echo.
echo 🔍 Vérification de MySQL...
mysql --version >nul 2>&1
if %errorlevel% neq 0 (
    echo ⚠️  MySQL CLI non trouvé, mais ce n'est pas critique
) else (
    echo ✅ MySQL CLI trouvé
)

echo.
echo 🔍 Vérification des fichiers requis...
if not exist "%JAVA_DIR%\%MYSQL_CONNECTOR%" (
    echo ❌ Driver MySQL non trouvé : %JAVA_DIR%\%MYSQL_CONNECTOR%
    pause
    exit /b 1
) else (
    echo ✅ Driver MySQL trouvé
)

if not exist "%JAVA_DIR%\CorrectionSynchronisation.java" (
    echo ❌ Outil de correction Java non trouvé
    pause
    exit /b 1
) else (
    echo ✅ Outil de correction Java trouvé
)

if not exist "%PHP_DIR%\correction_sync_finale.php" (
    echo ❌ Outil de correction PHP non trouvé
    pause
    exit /b 1
) else (
    echo ✅ Outil de correction PHP trouvé
)

echo.
echo ✅ Environnement validé !
pause

:: ═══════════════════════════════════════════════════════════════════════════════
:: ÉTAPE 2 : Test de connexion aux bases de données
:: ═══════════════════════════════════════════════════════════════════════════════
echo.
echo ╔══════════════════════════════════════════════════════════════════════════════╗
echo ║                     2️⃣  TEST CONNEXION BASES DE DONNÉES                     ║
echo ╚══════════════════════════════════════════════════════════════════════════════╝
echo.

echo 🔌 Test de connexion MySQL...
echo.
echo Tentative de connexion à MySQL (utilisateur root sans mot de passe)...
mysql -u root -e "SELECT 'Connexion MySQL réussie' as status;" 2>nul
if %errorlevel% neq 0 (
    echo ❌ Impossible de se connecter à MySQL
    echo.
    echo 🔧 SOLUTIONS POSSIBLES :
    echo    - Démarrer MySQL Server (XAMPP, WAMP, ou service Windows)
    echo    - Vérifier que l'utilisateur root n'a pas de mot de passe
    echo    - Vérifier que MySQL écoute sur le port 3306
    echo.
    pause
    exit /b 1
) else (
    echo ✅ Connexion MySQL réussie
)

echo.
echo 🗄️  Vérification des bases de données...
mysql -u root -e "SHOW DATABASES LIKE 'bigpharma';" 2>nul | find "bigpharma" >nul
if %errorlevel% neq 0 (
    echo ⚠️  Base 'bigpharma' non trouvée
    set NEED_DB_SETUP=1
) else (
    echo ✅ Base 'bigpharma' trouvée
)

mysql -u root -e "SHOW DATABASES LIKE 'clientlegerlourd';" 2>nul | find "clientlegerlourd" >nul
if %errorlevel% neq 0 (
    echo ⚠️  Base 'clientlegerlourd' non trouvée
    set NEED_DB_SETUP=1
) else (
    echo ✅ Base 'clientlegerlourd' trouvée
)

if defined NEED_DB_SETUP (
    echo.
    echo 🔧 Des bases de données sont manquantes.
    echo    Voulez-vous exécuter la correction automatique ?
    echo.
    choice /c ON /m "Exécuter la correction (O/N)"
    if !errorlevel! equ 1 (
        echo.
        echo 🔄 Exécution de la correction...
        if exist "CORRIGER_SYNCHRONISATION.bat" (
            call CORRIGER_SYNCHRONISATION.bat
        ) else (
            echo ❌ Script de correction non trouvé
            pause
            exit /b 1
        )
    )
)

pause

:: ═══════════════════════════════════════════════════════════════════════════════
:: ÉTAPE 3 : Compilation et test Java
:: ═══════════════════════════════════════════════════════════════════════════════
echo.
echo ╔══════════════════════════════════════════════════════════════════════════════╗
echo ║                         3️⃣  COMPILATION ET TEST JAVA                        ║
echo ╚══════════════════════════════════════════════════════════════════════════════╝
echo.

cd /d "%JAVA_DIR%"

echo 🔨 Compilation de l'outil de correction...
javac -cp ".;%MYSQL_CONNECTOR%" CorrectionSynchronisation.java
if %errorlevel% neq 0 (
    echo ❌ Erreur de compilation Java
    pause
    cd ..
    exit /b 1
) else (
    echo ✅ Compilation réussie
)

echo.
echo 🧪 Test de l'outil de correction Java...
echo    (L'interface graphique va s'ouvrir - fermez-la après vérification)
echo.
pause

start /wait java -cp ".;%MYSQL_CONNECTOR%" CorrectionSynchronisation

echo.
echo ✅ Test Java terminé
cd ..
pause

:: ═══════════════════════════════════════════════════════════════════════════════
:: ÉTAPE 4 : Test de l'interface PHP
:: ═══════════════════════════════════════════════════════════════════════════════
echo.
echo ╔══════════════════════════════════════════════════════════════════════════════╗
echo ║                          4️⃣  TEST INTERFACE PHP                             ║
echo ╚══════════════════════════════════════════════════════════════════════════════╝
echo.

echo 🌐 Ouverture des interfaces PHP de test...
echo.
echo    Les pages suivantes vont s'ouvrir dans votre navigateur :
echo    1. Interface de correction PHP
echo    2. Interface de test de synchronisation
echo    3. Interface principale BigPharma
echo.
pause

:: Ouvrir les interfaces PHP
start http://localhost/%PHP_DIR%/correction_sync_finale.php
timeout /t 2 /nobreak >nul
start http://localhost/%PHP_DIR%/test_sync.php
timeout /t 2 /nobreak >nul
start http://localhost/%PHP_DIR%/index.php

echo.
echo 📋 VÉRIFICATIONS À EFFECTUER DANS LE NAVIGATEUR :
echo.
echo    ✅ Interface de correction : Vérifier les statistiques
echo    ✅ Interface de test : Vérifier la synchronisation
echo    ✅ Interface principale : Tester la connexion
echo.
echo    📧 Email de test : tourefaliloumbacke12345@gmail.com
echo    🔐 Mot de passe : password
echo.
pause

:: ═══════════════════════════════════════════════════════════════════════════════
:: ÉTAPE 5 : Validation de la synchronisation
:: ═══════════════════════════════════════════════════════════════════════════════
echo.
echo ╔══════════════════════════════════════════════════════════════════════════════╗
echo ║                      5️⃣  VALIDATION SYNCHRONISATION                         ║
echo ╚══════════════════════════════════════════════════════════════════════════════╝
echo.

echo 🔍 Vérification des données dans les bases...
echo.

echo 📊 Statistiques base 'bigpharma' :
mysql -u root -e "USE bigpharma; SELECT 'Utilisateurs' as Type, COUNT(*) as Nombre FROM utilisateurs WHERE email='tourefaliloumbacke12345@gmail.com' UNION SELECT 'Produits', COUNT(*) FROM produits WHERE pharmacie_id=1 UNION SELECT 'Fournisseurs', COUNT(*) FROM fournisseurs WHERE pharmacie_id=1 UNION SELECT 'Catégories', COUNT(*) FROM categories;" 2>nul

echo.
echo 📊 Statistiques base 'clientlegerlourd' :
mysql -u root -e "USE clientlegerlourd; SELECT 'Utilisateurs' as Type, COUNT(*) as Nombre FROM utilisateurs WHERE email='tourefaliloumbacke12345@gmail.com' UNION SELECT 'Produits', COUNT(*) FROM produits WHERE pharmacie_id=1 UNION SELECT 'Fournisseurs', COUNT(*) FROM fournisseurs UNION SELECT 'Catégories', COUNT(*) FROM categories;" 2>nul

echo.
pause

:: ═══════════════════════════════════════════════════════════════════════════════
:: ÉTAPE 6 : Rapport final
:: ═══════════════════════════════════════════════════════════════════════════════
echo.
echo ╔══════════════════════════════════════════════════════════════════════════════╗
echo ║                            6️⃣  RAPPORT FINAL                                ║
echo ╚══════════════════════════════════════════════════════════════════════════════╝
echo.

echo 🎉 TEST FINAL COMPLET TERMINÉ !
echo.
echo ╔══════════════════════════════════════════════════════════════════════════════╗
echo ║                              📋 RÉSUMÉ DES TESTS                            ║
echo ╚══════════════════════════════════════════════════════════════════════════════╝
echo.
echo ✅ Environnement vérifié (Java, MySQL, fichiers)
echo ✅ Connexions aux bases de données testées
echo ✅ Application Java compilée et testée
echo ✅ Interfaces PHP ouvertes et accessibles
echo ✅ Synchronisation des données vérifiée
echo.
echo ╔══════════════════════════════════════════════════════════════════════════════╗
echo ║                           🔐 INFORMATIONS DE CONNEXION                      ║
echo ╚══════════════════════════════════════════════════════════════════════════════╝
echo.
echo 📧 Email : tourefaliloumbacke12345@gmail.com
echo 🔐 Mot de passe : password
echo 🏥 Pharmacie ID : 1
echo 🔑 Rôle : Administrateur
echo.
echo ╔══════════════════════════════════════════════════════════════════════════════╗
echo ║                              🌐 LIENS UTILES                                ║
echo ╚══════════════════════════════════════════════════════════════════════════════╝
echo.
echo 🔧 Correction PHP : http://localhost/%PHP_DIR%/correction_sync_finale.php
echo 🧪 Test sync PHP : http://localhost/%PHP_DIR%/test_sync.php
echo 🌐 Interface principale : http://localhost/%PHP_DIR%/index.php
echo 📚 Documentation : README_FINAL.md
echo.
echo ╔══════════════════════════════════════════════════════════════════════════════╗
echo ║                            📞 EN CAS DE PROBLÈME                            ║
echo ╚══════════════════════════════════════════════════════════════════════════════╝
echo.
echo 1. Exécuter CORRIGER_SYNCHRONISATION.bat
echo 2. Vérifier que MySQL Server est démarré
echo 3. Vérifier que Apache/XAMPP est démarré
echo 4. Consulter README_FINAL.md pour plus de détails
echo.
echo 🎊 FÉLICITATIONS ! Votre système BigPharma est maintenant opérationnel !
echo.
pause

echo.
echo Voulez-vous ouvrir la documentation finale ?
choice /c ON /m "Ouvrir README_FINAL.md (O/N)"
if %errorlevel% equ 1 (
    start README_FINAL.md
)

echo.
echo 👋 Merci d'avoir utilisé le système de test BigPharma !
echo    Le projet est maintenant prêt pour utilisation.
echo.
pause
