@echo off
setlocal EnableDelayedExpansion

echo Demarrage de l'application BigPharma Admin...
echo.

REM Definir les chemins
set BUILD_DIR=build
set JAVAFX_SDK=javafx-sdk
set JAVAFX_MODS=!JAVAFX_SDK!\lib
set LIB_DIR=lib

REM Definir le classpath
set CLASSPATH=.;!BUILD_DIR!
for %%f in (!LIB_DIR!\*.jar) do (
    set CLASSPATH=!CLASSPATH!;!LIB_DIR!\%%~nxf
)

REM Ajouter les JAR de JavaFX au classpath
for %%f in (!JAVAFX_MODS!\*.jar) do (
    set CLASSPATH=!CLASSPATH!;!JAVAFX_MODS!\%%~nxf
)

REM Definir les modules JavaFX
set JAVAFX_MODULES=javafx.controls,javafx.fxml,javafx.web,javafx.media,javafx.graphics

echo Classpath: !CLASSPATH!
echo.
echo Execution de l'application...
echo.

java --module-path !JAVAFX_MODS! --add-modules !JAVAFX_MODULES! -cp !CLASSPATH! com.bigpharma.admin.Main

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo ERREUR: L'application n'a pas pu demarrer.
    echo Code d'erreur: %ERRORLEVEL%
    echo.
    echo Verifiez que:
    echo 1. Java est bien installe (version 11 ou superieure)
    echo 2. Les fichiers ont ete correctement compiles
    echo 3. Tous les fichiers JAR sont presents dans les dossiers lib et javafx-sdk\lib
    echo.
    echo Affichage des details de l'erreur:
    java --module-path !JAVAFX_MODS! --add-modules !JAVAFX_MODULES! -cp !CLASSPATH! com.bigpharma.admin.Main
)

pause
