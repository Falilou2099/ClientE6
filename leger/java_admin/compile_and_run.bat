@echo off
setlocal EnableDelayedExpansion
echo Compilation et execution de l'application BigPharma Admin...
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
if not exist "dist" mkdir dist
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
set BUILD_DIR=build
set DIST_DIR=dist
set LIB_DIR=lib
set JAVAFX_SDK=javafx-sdk
set JAVAFX_MODS=!JAVAFX_SDK!\lib

REM Definir le classpath et les modules JavaFX
set CLASSPATH=.;!BUILD_DIR!
for %%f in (!LIB_DIR!\*.jar) do (
    set CLASSPATH=!CLASSPATH!;!LIB_DIR!\%%~nxf
)

REM Ajouter les JAR de JavaFX au classpath
for %%f in (!JAVAFX_MODS!\*.jar) do (
    set CLASSPATH=!CLASSPATH!;!JAVAFX_MODS!\%%~nxf
)

echo Classpath: !CLASSPATH!

REM Definir les modules JavaFX
set JAVAFX_MODULES=javafx.controls,javafx.fxml,javafx.web,javafx.swing,javafx.media,javafx.graphics

echo Compilation du code source...
echo Recherche des fichiers Java...
dir /s /b !SRC_DIR!\*.java > sources.txt
javac -d !BUILD_DIR! --module-path !JAVAFX_MODS! --add-modules !JAVAFX_MODULES! -cp !CLASSPATH! @sources.txt
del sources.txt

if %ERRORLEVEL% NEQ 0 (
    echo ERREUR: La compilation a echoue.
    pause
    exit /b 1
)

echo Copie des ressources...
if exist "!SRC_DIR!\resources" (
    if not exist "!BUILD_DIR!\resources" mkdir !BUILD_DIR!\resources
    xcopy /E /Y !SRC_DIR!\resources !BUILD_DIR!\resources\
)

echo Execution de l'application...
java --module-path !JAVAFX_MODS! --add-modules !JAVAFX_MODULES! -cp !CLASSPATH! com.bigpharma.admin.Main

pause
