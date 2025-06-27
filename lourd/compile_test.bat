@echo off
echo Compilation des fichiers de test pour les dialogues...

REM Créer le répertoire de sortie
if not exist "target\classes" mkdir target\classes

echo Compilation des modèles de base...
javac -cp "target\classes" -d target\classes src\main\java\com\gestionpharma\models\Produit.java
javac -cp "target\classes" -d target\classes src\main\java\com\gestionpharma\models\Fournisseur.java

echo Compilation de la configuration simplifiée...
javac -cp "target\classes" -d target\classes src\main\java\com\gestionpharma\config\DatabaseConfigSimple.java

echo Compilation des dialogues...
javac -cp "target\classes" -d target\classes src\main\java\com\gestionpharma\AjoutProduitDialog.java
javac -cp "target\classes" -d target\classes src\main\java\com\gestionpharma\AjoutFournisseurDialog.java

echo Compilation du test...
javac -cp "target\classes" -d target\classes src\main\java\com\gestionpharma\TestDialoguesAvecDB.java

if %ERRORLEVEL% EQU 0 (
    echo.
    echo ✓ Compilation réussie!
    echo.
    echo Pour tester les dialogues, exécutez:
    echo java -cp target\classes com.gestionpharma.TestDialoguesAvecDB
    echo.
) else (
    echo.
    echo ✗ Erreurs de compilation détectées.
    echo.
)

pause
