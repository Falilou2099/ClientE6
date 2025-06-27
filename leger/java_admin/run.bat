@echo off
echo Démarrage de l'application BigPharma Admin...
echo.

REM Vérifier si Java est installé
java -version >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    echo ERREUR: Java n'est pas installé ou n'est pas dans le PATH.
    echo Veuillez installer Java 11 ou supérieur et réessayer.
    pause
    exit /b 1
)

REM Vérifier si Maven est installé
mvn -version >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    echo Maven n'est pas installé ou n'est pas dans le PATH.
    echo Utilisation de la méthode alternative...
    
    REM Vérifier si le JAR existe déjà
    if exist "dist\BigPharmaAdmin.jar" (
        echo Exécution du JAR existant...
        java -jar dist\BigPharmaAdmin.jar
    ) else (
        echo Compilation du projet avec Ant...
        
        REM Vérifier si Ant est installé
        ant -version >nul 2>&1
        if %ERRORLEVEL% NEQ 0 (
            echo ERREUR: Ni Maven ni Ant ne sont installés.
            echo Veuillez installer Maven ou Ant et réessayer.
            pause
            exit /b 1
        )
        
        echo Compilation et exécution avec Ant...
        ant run
    )
) else (
    echo Compilation et exécution avec Maven...
    mvn clean javafx:run
)

pause
