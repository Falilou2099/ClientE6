@echo off
echo Compilation de BigPharma avec système de connexion...

REM Compilation des classes de base
javac -cp . src\main\java\com\gestionpharma\config\DatabaseConfigSimple.java
if %errorlevel% neq 0 goto error

REM Compilation des modèles
javac -cp . src\main\java\com\gestionpharma\models\*.java
if %errorlevel% neq 0 goto error

REM Compilation des services
javac -cp . src\main\java\com\gestionpharma\services\*.java
if %errorlevel% neq 0 goto error

REM Compilation des dialogues
javac -cp . src\main\java\com\gestionpharma\SessionManager.java
if %errorlevel% neq 0 goto error

javac -cp . src\main\java\com\gestionpharma\MotDePasseOublieDialog.java
if %errorlevel% neq 0 goto error

javac -cp . src\main\java\com\gestionpharma\ConnexionDialog.java
if %errorlevel% neq 0 goto error

javac -cp . src\main\java\com\gestionpharma\AjoutProduitDialog.java
if %errorlevel% neq 0 goto error

javac -cp . src\main\java\com\gestionpharma\AjoutFournisseurDialog.java
if %errorlevel% neq 0 goto error

javac -cp . src\main\java\com\gestionpharma\NouvelleCommandeDialog.java
if %errorlevel% neq 0 goto error

REM Compilation des frames
javac -cp . src\main\java\com\gestionpharma\GestionProduitFrame.java
if %errorlevel% neq 0 goto error

javac -cp . src\main\java\com\gestionpharma\GestionStockFrame.java
if %errorlevel% neq 0 goto error

REM Compilation de l'application principale
javac -cp . src\main\java\com\gestionpharma\BigPharmaApp.java
if %errorlevel% neq 0 goto error

REM Compilation des tests
javac -cp . TestConnexion.java
if %errorlevel% neq 0 goto error

echo.
echo ✅ Compilation réussie !
echo.
echo Pour tester l'application :
echo   java com.gestionpharma.BigPharmaApp
echo.
echo Pour tester la connexion :
echo   java TestConnexion
echo.
goto end

:error
echo.
echo ❌ Erreur de compilation !
echo.

:end
pause
