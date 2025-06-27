@echo off
echo Compilation des fichiers essentiels pour les dialogues...

REM Créer le répertoire de sortie
if not exist "target\classes" mkdir target\classes

REM Compiler dans l'ordre de dépendance
echo Compilation des modèles...
javac -cp "target\classes" -d target\classes src\main\java\com\gestionpharma\models\*.java

echo Compilation des utilitaires...
javac -cp "target\classes" -d target\classes src\main\java\com\gestionpharma\utils\*.java

echo Compilation de la configuration...
javac -cp "target\classes" -d target\classes src\main\java\com\gestionpharma\config\*.java

echo Compilation des services...
javac -cp "target\classes" -d target\classes src\main\java\com\gestionpharma\services\*.java

echo Compilation des dialogues Swing...
javac -cp "target\classes" -d target\classes src\main\java\com\gestionpharma\AjoutProduitDialog.java
javac -cp "target\classes" -d target\classes src\main\java\com\gestionpharma\AjoutFournisseurDialog.java
javac -cp "target\classes" -d target\classes src\main\java\com\gestionpharma\TestDialoguesSimple.java

echo Compilation des frames Swing...
javac -cp "target\classes" -d target\classes src\main\java\com\gestionpharma\GestionProduitFrame.java

echo Compilation terminée.
pause
