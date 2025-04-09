@echo off
echo ===== Lancement de l'application de gestion pharmaceutique =====

REM Définir le chemin vers Maven
set MAVEN_HOME=C:\Users\toure\Downloads\apache-maven-3.9.9
set PATH=%PATH%;%MAVEN_HOME%\bin

REM Vérifier si Java est installé ou rechercher des installations Java communes
java -version >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    echo Recherche d'installations Java...
    
    REM Chemins communs pour Java 24
    set JAVA_PATHS=C:\Program Files\Java\jdk-24 C:\Program Files\Java\jdk-24.0.0 C:\Program Files\Java\jdk-24.0.1 C:\Program Files\Java\jdk-24.0.2 C:\Program Files\Java\jdk24 C:\Program Files\Eclipse Adoptium\jdk-24
    
    set JAVA_FOUND=0
    
    for %%p in (%JAVA_PATHS%) do (
        if exist "%%p\bin\java.exe" (
            echo Java trouvé dans: %%p
            set JAVA_HOME=%%p
            set PATH=%%p\bin;%PATH%
            set JAVA_FOUND=1
            goto :java_found
        )
    )
    
    if %JAVA_FOUND% EQU 0 (
        echo ERREUR: Java n'est pas trouvé dans les chemins communs.
        echo Veuillez installer Java JDK 17 ou supérieur.
        echo Vous pouvez le télécharger depuis: https://www.oracle.com/java/technologies/downloads/
        echo.
        echo Après l'installation, veuillez ajouter Java à votre PATH ou spécifier le chemin dans ce script.
        pause
        exit /b 1
    )
    
    :java_found
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
