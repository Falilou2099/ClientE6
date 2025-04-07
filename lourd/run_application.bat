@echo off
echo ===== Lancement de l'application de gestion pharmaceutique =====

REM Définir le chemin vers Maven
set MAVEN_HOME=C:\Users\toure\Downloads\apache-maven-3.9.9
set PATH=%PATH%;%MAVEN_HOME%\bin

REM Vérifier si Java est installé
java -version >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    echo ERREUR: Java n'est pas installé ou n'est pas dans le PATH.
    echo Veuillez installer Java JDK 17 ou supérieur.
    echo Vous pouvez le télécharger depuis: https://www.oracle.com/java/technologies/downloads/
    pause
    exit /b 1
)

REM Compiler et empaqueter l'application
echo Compilation et empaquetage de l'application...
call %MAVEN_HOME%\bin\mvn clean package -DskipTests

if %ERRORLEVEL% NEQ 0 (
    echo ERREUR: La compilation a échoué.
    pause
    exit /b 1
)

REM Lancer l'application
echo Lancement de l'application...
java -jar target\gestion-produits-pharma-1.0-SNAPSHOT.jar

pause
