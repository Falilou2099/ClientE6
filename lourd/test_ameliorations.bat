@echo off
echo ========================================
echo TEST DES AMELIORATIONS - GESTION PHARMA
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
echo 🔧 Compilation des composants essentiels...
echo.

REM Compilation silencieuse des composants essentiels
javac -d "%BUILD_DIR%" -cp "%CLASSPATH%" "%SRC_DIR%\com\gestionpharma\models\*.java" 2>nul
javac -d "%BUILD_DIR%" -cp "%CLASSPATH%" "%SRC_DIR%\com\gestionpharma\config\DatabaseConfigSimple.java" 2>nul
javac -d "%BUILD_DIR%" -cp "%CLASSPATH%" "%SRC_DIR%\com\gestionpharma\utils\SessionManager.java" 2>nul
javac -d "%BUILD_DIR%" -cp "%CLASSPATH%" "%SRC_DIR%\com\gestionpharma\utils\CategoriesProduits.java" 2>nul
javac -d "%BUILD_DIR%" -cp "%CLASSPATH%" "%SRC_DIR%\com\gestionpharma\services\*.java" 2>nul
javac -d "%BUILD_DIR%" -cp "%CLASSPATH%" "%SRC_DIR%\com\gestionpharma\AjoutProduitDialog.java" 2>nul
javac -d "%BUILD_DIR%" -cp "%CLASSPATH%" "%SRC_DIR%\com\gestionpharma\AjoutFournisseurDialog.java" 2>nul
javac -d "%BUILD_DIR%" -cp "%CLASSPATH%" "%SRC_DIR%\com\gestionpharma\NouvelleCommandeDialog.java" 2>nul
javac -d "%BUILD_DIR%" -cp "%CLASSPATH%" "%SRC_DIR%\com\gestionpharma\GestionStockFrame.java" 2>nul
javac -d "%BUILD_DIR%" -cp "%CLASSPATH%" "%SRC_DIR%\com\gestionpharma\TestAmeliorations.java" 2>nul

if %ERRORLEVEL% EQU 0 (
    echo ✅ Compilation réussie !
    echo.
    echo 🚀 Lancement de l'application de test...
    echo.
    echo ========================================
    echo FONCTIONNALITES TESTABLES :
    echo ========================================
    echo 1. Test connexion DB
    echo 2. Nouvelle commande (popup avec fournisseurs)
    echo 3. Gestion du stock (frame complète)
    echo 4. Ajout produit (dialogue avec catégories)
    echo 5. Ajout fournisseur (dialogue complet)
    echo ========================================
    echo.
    
    REM Lancement de l'application
    java -cp "%CLASSPATH%" com.gestionpharma.TestAmeliorations
    
) else (
    echo ❌ Erreur de compilation
    echo Vérifiez les erreurs ci-dessus
    pause
)
