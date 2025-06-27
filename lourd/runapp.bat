@echo off
echo ===== Lancement de l'application de gestion pharmaceutique =====

REM Rechercher Java
set JAVA_FOUND=0

REM Vérifier l'installation courante
where java >nul 2>&1
if %ERRORLEVEL% EQU 0 (
    echo Java trouvé dans le PATH
    set JAVA_FOUND=1
    goto compile
)

REM Essayer des chemins communs pour Java
set JAVA_PATHS="C:\Program Files\Java\jdk-24\bin" "C:\Program Files\Java\jdk-24.0.0\bin" "C:\Program Files\Java\jdk-24.0.1\bin" "C:\Program Files\Java\jdk-24.0.2\bin" "C:\Program Files\Eclipse Adoptium\jdk-24\bin"

for %%i in (%JAVA_PATHS%) do (
    if exist %%i\java.exe (
        echo Java trouvé dans: %%i
        set PATH=%%i;%PATH%
        set JAVA_FOUND=1
        goto compile
    )
)

if %JAVA_FOUND% EQU 0 (
    echo ERREUR: Java n'est pas trouvé.
    echo Veuillez vous assurer que Java 17+ est installé et dans votre PATH.
    pause
    exit /b 1
)

:compile
echo Compilation de l'application en utilisant javac...

REM Créer le répertoire pour les fichiers compilés s'il n'existe pas
if not exist target\classes mkdir target\classes

REM Compiler toutes les classes Java
echo Compilation des classes Java...
javac -d target\classes -cp "src\main\java;lib\*" src\main\java\com\gestionpharma\controllers\ProduitDialogController.java

if %ERRORLEVEL% NEQ 0 (
    echo ERREUR: La compilation a échoué.
    pause
    exit /b 1
) else (
    echo Compilation réussie.
)

REM Lancer l'application
echo Lancement de l'application...
java -jar target\gestion-produits-pharma-1.0-SNAPSHOT.jar

pause
