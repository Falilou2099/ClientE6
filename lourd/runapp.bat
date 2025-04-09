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
echo Compilation de l'application...

REM Définir le chemin vers Maven
set MAVEN_HOME=C:\Users\toure\Downloads\apache-maven-3.9.9
if exist "%MAVEN_HOME%\bin\mvn.cmd" (
    echo Maven trouvé dans %MAVEN_HOME%
    call "%MAVEN_HOME%\bin\mvn.cmd" clean package -DskipTests
) else (
    echo ERREUR: Maven n'est pas trouvé dans %MAVEN_HOME%
    echo Veuillez installer Maven ou corriger le chemin dans ce script.
    pause
    exit /b 1
)

if %ERRORLEVEL% NEQ 0 (
    echo ERREUR: La compilation a échoué.
    pause
    exit /b 1
)

REM Lancer l'application
echo Lancement de l'application...
java -jar target\gestion-produits-pharma-1.0-SNAPSHOT.jar

pause
