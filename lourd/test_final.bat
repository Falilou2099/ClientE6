@echo off
chcp 65001 >nul
cls

echo ========================================
echo   TEST FINAL SYNCHRONISATION 
echo ========================================
echo.
echo Ce script va tester la synchronisation complète
echo entre les applications Java et PHP BigPharma
echo.

cd /d "%~dp0"

echo Compilation du test de synchronisation...
javac -cp . TestFinalSync.java

if exist "TestFinalSync.class" (
    echo Compilation réussie
    echo.
    echo Lancement du test de synchronisation...
    echo.
    java -cp . TestFinalSync
) else (
    echo Erreur de compilation
    echo Vérifiez que Java JDK est installé et dans le PATH
    pause
)

echo.
echo INSTRUCTIONS POUR LE TEST COMPLET :
echo.
echo 1. Utilisez l'interface graphique qui s'est ouverte
echo 2. Cliquez sur "Tester la Synchronisation" pour vérifier l'état
echo 3. Si des problèmes sont détectés, cliquez sur "Synchroniser les Données"
echo 4. Retestez pour vérifier les corrections
echo.
echo 5. Ensuite, testez l'application PHP :
echo    • Allez sur http://localhost/bigpharma/test_sync.php
echo    • Vérifiez que les données sont cohérentes
echo.
echo 6. Testez la connexion sur les deux applications :
echo    • PHP: tourefaliloumbacke12345@gmail.com / password
echo    • Java: même identifiants
echo.

pause
