@echo off
echo Démarrage de l'application de gestion pharmaceutique...

REM Vérifier si le wrapper Maven existe
if exist "mvnw.cmd" (
    echo Utilisation du wrapper Maven...
    call mvnw.cmd clean javafx:run
    goto :end
)

REM Vérifier les emplacements communs de Maven
set MAVEN_PATHS=^
C:\Program Files\Maven\bin\mvn.cmd;^
C:\apache-maven\bin\mvn.cmd;^
C:\ProgramData\chocolatey\bin\mvn.cmd;^
%USERPROFILE%\apache-maven\bin\mvn.cmd

for %%i in (%MAVEN_PATHS%) do (
    if exist "%%i" (
        echo Maven trouvé à: %%i
        call "%%i" clean javafx:run
        goto :end
    )
)

REM Si Maven n'est pas trouvé, essayer avec le chemin Java
echo Maven non trouvé, tentative avec Java...

REM Vérifier si le jar existe
if exist "target\gestion-produits-pharma-1.0-SNAPSHOT.jar" (
    echo Lancement du jar avec Java...
    java -jar target\gestion-produits-pharma-1.0-SNAPSHOT.jar
) else (
    echo Le fichier jar n'existe pas. Compilation nécessaire.
    echo.
    echo Pour compiler et exécuter l'application, vous devez installer Maven:
    echo 1. Téléchargez Maven depuis https://maven.apache.org/download.cgi
    echo 2. Extrayez l'archive dans un dossier (ex: C:\apache-maven)
    echo 3. Ajoutez le dossier bin de Maven à votre PATH système
    echo 4. Redémarrez votre terminal et exécutez à nouveau ce script
)

:end
pause
