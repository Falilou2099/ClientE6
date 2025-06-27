@echo off
echo ===== Test des améliorations Stock et Commandes =====
echo.

set JAVA_HOME=C:\Program Files\Java\jdk-17
set PATH=%JAVA_HOME%\bin;%PATH%

set SRC_DIR=src\main\java
set LIB_DIR=lib
set BUILD_DIR=build\classes

echo Création du répertoire de build...
if not exist "%BUILD_DIR%" mkdir "%BUILD_DIR%"

echo.
echo ===== Compilation des fichiers modifiés =====

echo Compilation des modèles...
javac -d "%BUILD_DIR%" -cp "%LIB_DIR%\*" "%SRC_DIR%\com\gestionpharma\models\*.java"

echo Compilation des services...
javac -d "%BUILD_DIR%" -cp "%LIB_DIR%\*;%BUILD_DIR%" "%SRC_DIR%\com\gestionpharma\services\*.java"

echo Compilation de la configuration simplifiée...
javac -d "%BUILD_DIR%" -cp "%LIB_DIR%\*;%BUILD_DIR%" "%SRC_DIR%\com\gestionpharma\config\DatabaseConfigSimple.java"

echo Compilation des dialogues...
javac -d "%BUILD_DIR%" -cp "%LIB_DIR%\*;%BUILD_DIR%" "%SRC_DIR%\com\gestionpharma\AjoutProduitDialog.java"
javac -d "%BUILD_DIR%" -cp "%LIB_DIR%\*;%BUILD_DIR%" "%SRC_DIR%\com\gestionpharma\AjoutFournisseurDialog.java"
javac -d "%BUILD_DIR%" -cp "%LIB_DIR%\*;%BUILD_DIR%" "%SRC_DIR%\com\gestionpharma\NouvelleCommandeDialog.java"

echo Compilation du frame de gestion des stocks...
javac -d "%BUILD_DIR%" -cp "%LIB_DIR%\*;%BUILD_DIR%" "%SRC_DIR%\com\gestionpharma\GestionStockFrame.java"

echo Compilation de l'utilitaire SessionManager...
javac -d "%BUILD_DIR%" -cp "%LIB_DIR%\*;%BUILD_DIR%" "%SRC_DIR%\com\gestionpharma\utils\SessionManager.java"

echo.
if %ERRORLEVEL% EQU 0 (
    echo ===== Compilation réussie ! =====
    echo.
    echo Pour tester les dialogues avec fournisseurs, exécutez :
    echo java -cp "%BUILD_DIR%;%LIB_DIR%\*" com.gestionpharma.TestDialoguesAvecDB
    echo.
    echo Pour tester la gestion des stocks, exécutez :
    echo java -cp "%BUILD_DIR%;%LIB_DIR%\*" com.gestionpharma.GestionStockFrame
    echo.
    echo Pour tester le dialogue de nouvelle commande, exécutez :
    echo java -cp "%BUILD_DIR%;%LIB_DIR%\*" com.gestionpharma.NouvelleCommandeDialog
) else (
    echo ===== Erreur de compilation =====
)

pause
