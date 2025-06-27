@echo off
setlocal EnableDelayedExpansion

echo Application BigPharma Admin - Demarrage rapide
echo =============================================
echo.

REM Verifier si Java est installe
java -version >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    echo ERREUR: Java n'est pas installe ou n'est pas dans le PATH.
    echo Veuillez installer Java 11 ou superieur et reessayer.
    pause
    exit /b 1
)

REM Creer les repertoires necessaires
if not exist "build" mkdir build
if not exist "lib" mkdir lib
if not exist "javafx-sdk" mkdir javafx-sdk

REM Telecharger les dependances si elles n'existent pas
if not exist "lib\mysql-connector-j-8.0.33.jar" (
    echo Telechargement des dependances...
    powershell -Command "& {Invoke-WebRequest -Uri 'https://repo1.maven.org/maven2/com/mysql/mysql-connector-j/8.0.33/mysql-connector-j-8.0.33.jar' -OutFile 'lib\mysql-connector-j-8.0.33.jar'}"
    powershell -Command "& {Invoke-WebRequest -Uri 'https://repo1.maven.org/maven2/org/mindrot/jbcrypt/0.4/jbcrypt-0.4.jar' -OutFile 'lib\jbcrypt-0.4.jar'}"
    powershell -Command "& {Invoke-WebRequest -Uri 'https://repo1.maven.org/maven2/commons-io/commons-io/2.11.0/commons-io-2.11.0.jar' -OutFile 'lib\commons-io-2.11.0.jar'}"
    powershell -Command "& {Invoke-WebRequest -Uri 'https://repo1.maven.org/maven2/org/apache/commons/commons-lang3/3.12.0/commons-lang3-3.12.0.jar' -OutFile 'lib\commons-lang3-3.12.0.jar'}"
)

REM Telecharger JavaFX si necessaire
if not exist "javafx-sdk\lib" (
    echo Telechargement de JavaFX (cela peut prendre quelques minutes)...
    powershell -Command "& {Invoke-WebRequest -Uri 'https://download2.gluonhq.com/openjfx/17.0.2/openjfx-17.0.2_windows-x64_bin-sdk.zip' -OutFile 'javafx-sdk.zip'}"
    echo Extraction de JavaFX...
    powershell -Command "& {Expand-Archive -Path 'javafx-sdk.zip' -DestinationPath '.' -Force}"
    powershell -Command "& {Move-Item -Path 'javafx-sdk-17.0.2\*' -Destination 'javafx-sdk' -Force}"
    powershell -Command "& {Remove-Item -Path 'javafx-sdk-17.0.2' -Recurse -Force}"
    powershell -Command "& {Remove-Item -Path 'javafx-sdk.zip' -Force}"
    echo JavaFX a ete telecharge et configure avec succes.
)

REM Definir les chemins
set SRC_DIR=src
set LIB_DIR=lib
set JAVAFX_SDK=javafx-sdk
set JAVAFX_MODS=!JAVAFX_SDK!\lib

REM Definir le classpath
set CLASSPATH=.;!SRC_DIR!
for %%f in (!LIB_DIR!\*.jar) do (
    set CLASSPATH=!CLASSPATH!;!LIB_DIR!\%%~nxf
)

REM Ajouter les JAR de JavaFX au classpath
for %%f in (!JAVAFX_MODS!\*.jar) do (
    set CLASSPATH=!CLASSPATH!;!JAVAFX_MODS!\%%~nxf
)

REM Definir les modules JavaFX
set JAVAFX_MODULES=javafx.controls,javafx.fxml,javafx.web,javafx.media,javafx.graphics

echo.
echo Configuration terminee. Demarrage de l'application...
echo.

REM Essayer de lancer l'application avec une version simplifiee
echo java --module-path !JAVAFX_MODS! --add-modules !JAVAFX_MODULES! -cp !CLASSPATH! com.bigpharma.admin.Main
java --module-path !JAVAFX_MODS! --add-modules !JAVAFX_MODULES! -cp !CLASSPATH! com.bigpharma.admin.Main

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo ERREUR: L'application n'a pas pu demarrer.
    echo Code d'erreur: %ERRORLEVEL%
    echo.
    echo Affichage des details de l'erreur:
    echo.
    java --module-path !JAVAFX_MODS! --add-modules !JAVAFX_MODULES! -cp !CLASSPATH! com.bigpharma.admin.Main 2>&1
    echo.
    echo =============================================
    echo Tentative alternative avec compilation directe...
    echo.
    
    REM Essayer une compilation directe
    echo Compilation du code source...
    dir /s /b !SRC_DIR!\*.java > sources.txt
    javac -d build --module-path !JAVAFX_MODS! --add-modules !JAVAFX_MODULES! -cp !CLASSPATH! @sources.txt
    del sources.txt
    
    if %ERRORLEVEL% EQU 0 (
        echo.
        echo Compilation reussie. Execution de l'application...
        echo.
        java --module-path !JAVAFX_MODS! --add-modules !JAVAFX_MODULES! -cp build;!CLASSPATH! com.bigpharma.admin.Main
    ) else (
        echo.
        echo ERREUR: La compilation a echoue.
        echo.
        echo Veuillez verifier que:
        echo 1. Java 11 ou superieur est installe
        echo 2. Les dependances ont ete correctement telechargees
        echo 3. Le code source est correct
    )
)

pause
