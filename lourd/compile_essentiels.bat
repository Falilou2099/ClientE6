@echo off
echo ===== Compilation des fichiers essentiels =====
echo.

set JAVA_HOME=C:\Program Files\Java\jdk-17
set PATH=%JAVA_HOME%\bin;%PATH%

set SRC_DIR=src\main\java
set LIB_DIR=lib
set BUILD_DIR=build\classes

echo Création du répertoire de build...
if not exist "%BUILD_DIR%" mkdir "%BUILD_DIR%"

echo.
echo ===== Compilation dans l'ordre des dépendances =====

echo 1. Compilation des modèles...
javac -d "%BUILD_DIR%" -cp "%LIB_DIR%\*" "%SRC_DIR%\com\gestionpharma\models\*.java"

echo 2. Compilation de la configuration simplifiée...
javac -d "%BUILD_DIR%" -cp "%LIB_DIR%\*;%BUILD_DIR%" "%SRC_DIR%\com\gestionpharma\config\DatabaseConfigSimple.java"

echo 3. Compilation des utilitaires...
javac -d "%BUILD_DIR%" -cp "%LIB_DIR%\*;%BUILD_DIR%" "%SRC_DIR%\com\gestionpharma\utils\*.java"

echo 4. Compilation du service Stock...
javac -d "%BUILD_DIR%" -cp "%LIB_DIR%\*;%BUILD_DIR%" "%SRC_DIR%\com\gestionpharma\services\StockService.java"

echo 5. Compilation du service Fournisseur...
javac -d "%BUILD_DIR%" -cp "%LIB_DIR%\*;%BUILD_DIR%" "%SRC_DIR%\com\gestionpharma\services\FournisseurService.java"

echo 6. Compilation du service Produit...
javac -d "%BUILD_DIR%" -cp "%LIB_DIR%\*;%BUILD_DIR%" "%SRC_DIR%\com\gestionpharma\services\ProduitService.java"

echo 7. Compilation du service Commande...
javac -d "%BUILD_DIR%" -cp "%LIB_DIR%\*;%BUILD_DIR%" "%SRC_DIR%\com\gestionpharma\services\CommandeService.java"

echo 8. Compilation du frame de gestion des stocks...
javac -d "%BUILD_DIR%" -cp "%LIB_DIR%\*;%BUILD_DIR%" "%SRC_DIR%\com\gestionpharma\GestionStockFrame.java"

echo 9. Compilation du dialogue de nouvelle commande...
javac -d "%BUILD_DIR%" -cp "%LIB_DIR%\*;%BUILD_DIR%" "%SRC_DIR%\com\gestionpharma\NouvelleCommandeDialog.java"

echo 10. Compilation du test des améliorations...
javac -d "%BUILD_DIR%" -cp "%LIB_DIR%\*;%BUILD_DIR%" "%SRC_DIR%\com\gestionpharma\TestAméliorations.java"

echo.
if %ERRORLEVEL% EQU 0 (
    echo ===== Compilation réussie ! =====
    echo.
    echo Pour tester toutes les améliorations, exécutez :
    echo java -cp "%BUILD_DIR%;%LIB_DIR%\*" com.gestionpharma.TestAméliorations
    echo.
    echo Pour tester uniquement la gestion des stocks :
    echo java -cp "%BUILD_DIR%;%LIB_DIR%\*" com.gestionpharma.GestionStockFrame
) else (
    echo ===== Erreur de compilation =====
)

pause
