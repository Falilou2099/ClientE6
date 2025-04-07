@echo off
echo ===== Configuration de l'environnement pour BigPharma =====

REM Vérifier si Java est installé
java -version >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    echo Java n'est pas détecté. Téléchargement d'AdoptOpenJDK 17...
    
    REM Créer un dossier temporaire pour le téléchargement
    mkdir "%TEMP%\java_setup" 2>nul
    
    REM Télécharger AdoptOpenJDK 17
    powershell -Command "& {Invoke-WebRequest -Uri 'https://github.com/adoptium/temurin17-binaries/releases/download/jdk-17.0.8%2B7/OpenJDK17U-jdk_x64_windows_hotspot_17.0.8_7.msi' -OutFile '%TEMP%\java_setup\jdk17.msi'}"
    
    echo Installation de Java...
    start /wait msiexec /i "%TEMP%\java_setup\jdk17.msi" /quiet
    
    echo Configuration des variables d'environnement...
    setx JAVA_HOME "C:\Program Files\Eclipse Adoptium\jdk-17.0.8.7-hotspot" /M
    setx PATH "%PATH%;C:\Program Files\Eclipse Adoptium\jdk-17.0.8.7-hotspot\bin" /M
    
    echo Java a été installé et configuré.
) else (
    echo Java est déjà installé.
)

REM Vérifier si Maven est configuré
call %MAVEN_HOME%\bin\mvn -version >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    echo Configuration de Maven...
    setx MAVEN_HOME "C:\Users\toure\Downloads\apache-maven-3.9.9" /M
    setx PATH "%PATH%;C:\Users\toure\Downloads\apache-maven-3.9.9\bin" /M
    echo Maven a été configuré.
) else (
    echo Maven est déjà configuré.
)

echo ===== Configuration terminée =====
echo Vous pouvez maintenant exécuter l'application avec run_application.bat
pause
