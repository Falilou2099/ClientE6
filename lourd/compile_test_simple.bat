@echo off
echo ========================================
echo Compilation simplifiee pour test
echo ========================================

REM Configuration des variables d'environnement
set JAVA_HOME=C:\Program Files\Java\jdk-17
set PATH=%JAVA_HOME%\bin;%PATH%
set SRC_DIR=src\main\java
set LIB_DIR=lib
set BUILD_DIR=build\classes

REM Création du répertoire de build
if not exist "%BUILD_DIR%" mkdir "%BUILD_DIR%"

REM Configuration du classpath avec toutes les dépendances
set CLASSPATH=%BUILD_DIR%;%LIB_DIR%\*

echo.
echo Compilation des modeles...
javac -d "%BUILD_DIR%" -cp "%CLASSPATH%" "%SRC_DIR%\com\gestionpharma\models\*.java"

echo.
echo Compilation de la configuration...
javac -d "%BUILD_DIR%" -cp "%CLASSPATH%" "%SRC_DIR%\com\gestionpharma\config\DatabaseConfigSimple.java"

echo.
echo Compilation des utilitaires...
javac -d "%BUILD_DIR%" -cp "%CLASSPATH%" "%SRC_DIR%\com\gestionpharma\utils\SessionManager.java"
javac -d "%BUILD_DIR%" -cp "%CLASSPATH%" "%SRC_DIR%\com\gestionpharma\utils\CategoriesProduits.java"

echo.
echo Compilation des services...
javac -d "%BUILD_DIR%" -cp "%CLASSPATH%" "%SRC_DIR%\com\gestionpharma\services\StockService.java"
javac -d "%BUILD_DIR%" -cp "%CLASSPATH%" "%SRC_DIR%\com\gestionpharma\services\CommandeService.java"
javac -d "%BUILD_DIR%" -cp "%CLASSPATH%" "%SRC_DIR%\com\gestionpharma\services\FournisseurService.java"
javac -d "%BUILD_DIR%" -cp "%CLASSPATH%" "%SRC_DIR%\com\gestionpharma\services\ProduitService.java"

echo.
echo Compilation des dialogues...
javac -d "%BUILD_DIR%" -cp "%CLASSPATH%" "%SRC_DIR%\com\gestionpharma\AjoutProduitDialog.java"
javac -d "%BUILD_DIR%" -cp "%CLASSPATH%" "%SRC_DIR%\com\gestionpharma\AjoutFournisseurDialog.java"
javac -d "%BUILD_DIR%" -cp "%CLASSPATH%" "%SRC_DIR%\com\gestionpharma\NouvelleCommandeDialog.java"

echo.
echo Compilation des frames...
javac -d "%BUILD_DIR%" -cp "%CLASSPATH%" "%SRC_DIR%\com\gestionpharma\GestionStockFrame.java"

echo.
echo Compilation du test...
javac -d "%BUILD_DIR%" -cp "%CLASSPATH%" "%SRC_DIR%\com\gestionpharma\TestAmeliorations.java"

if %ERRORLEVEL% EQU 0 (
    echo.
    echo ========================================
    echo ✅ COMPILATION REUSSIE !
    echo ========================================
    echo.
    echo Pour tester les ameliorations :
    echo java -cp "%CLASSPATH%" com.gestionpharma.TestAmeliorations
    echo.
    echo Voulez-vous lancer le test maintenant ? (O/N)
    set /p choice=
    if /i "%choice%"=="O" (
        echo.
        echo Lancement du test...
        java -cp "%CLASSPATH%" com.gestionpharma.TestAmeliorations
    )
) else (
    echo.
    echo ========================================
    echo ❌ ERREURS DE COMPILATION
    echo ========================================
    echo Verifiez les erreurs ci-dessus
)

pause
