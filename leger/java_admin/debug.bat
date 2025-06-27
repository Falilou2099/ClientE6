@echo off
setlocal EnableDelayedExpansion

echo =============================================
echo Mode debug pour BigPharma Admin
echo =============================================
echo.

REM Verifier si Java est installe
java -version
if %ERRORLEVEL% NEQ 0 (
    echo ERREUR: Java n'est pas installe ou n'est pas dans le PATH.
    echo Veuillez installer Java 11 ou superieur et reessayer.
    pause
    exit /b 1
)

REM Definir les chemins
set SRC_DIR=src
set BUILD_DIR=build
set LIB_DIR=lib
set JAVAFX_SDK=javafx-sdk
set JAVAFX_MODS=!JAVAFX_SDK!\lib

REM Verifier si les repertoires existent
if not exist "!SRC_DIR!" (
    echo ERREUR: Le repertoire source n'existe pas.
    pause
    exit /b 1
)

if not exist "!JAVAFX_SDK!\lib" (
    echo ERREUR: JavaFX n'est pas installe.
    pause
    exit /b 1
)

REM Definir le classpath
set CLASSPATH=.;!BUILD_DIR!
for %%f in (!LIB_DIR!\*.jar) do (
    set CLASSPATH=!CLASSPATH!;!LIB_DIR!\%%~nxf
)

REM Ajouter les JAR de JavaFX au classpath
for %%f in (!JAVAFX_MODS!\*.jar) do (
    set CLASSPATH=!CLASSPATH!;!JAVAFX_MODS!\%%~nxf
)

REM Afficher le classpath
echo Classpath:
echo !CLASSPATH!
echo.

REM Definir les modules JavaFX
set JAVAFX_MODULES=javafx.controls,javafx.fxml,javafx.web,javafx.media,javafx.graphics

echo Tentative d'execution directe a partir des sources...
echo.
echo java --module-path !JAVAFX_MODS! --add-modules !JAVAFX_MODULES! -cp !SRC_DIR!;!CLASSPATH! com.bigpharma.admin.Main
echo.

java --module-path !JAVAFX_MODS! --add-modules !JAVAFX_MODULES! -cp !SRC_DIR!;!CLASSPATH! com.bigpharma.admin.Main

echo.
echo Code d'erreur: %ERRORLEVEL%
echo.

if %ERRORLEVEL% NEQ 0 (
    echo Tentative d'execution a partir des fichiers compiles...
    echo.
    echo java --module-path !JAVAFX_MODS! --add-modules !JAVAFX_MODULES! -cp !BUILD_DIR!;!CLASSPATH! com.bigpharma.admin.Main
    echo.
    
    java --module-path !JAVAFX_MODS! --add-modules !JAVAFX_MODULES! -cp !BUILD_DIR!;!CLASSPATH! com.bigpharma.admin.Main
    
    echo.
    echo Code d'erreur: %ERRORLEVEL%
    echo.
)

echo Verification de la classe Main...
if exist "!SRC_DIR!\com\bigpharma\admin\Main.java" (
    echo La classe Main existe.
    echo Contenu de la classe Main:
    type !SRC_DIR!\com\bigpharma\admin\Main.java
) else (
    echo ERREUR: La classe Main n'existe pas.
)

echo.
echo Verification des fichiers compiles...
if exist "!BUILD_DIR!\com\bigpharma\admin\Main.class" (
    echo La classe Main est compilee.
) else (
    echo ERREUR: La classe Main n'est pas compilee.
)

echo.
echo Verification de la base de donnees...
echo.

REM Tester la connexion a la base de donnees avec le driver JDBC
echo import java.sql.*; > TestDB.java
echo public class TestDB { >> TestDB.java
echo     public static void main(String[] args) { >> TestDB.java
echo         try { >> TestDB.java
echo             Class.forName("com.mysql.cj.jdbc.Driver"); >> TestDB.java
echo             Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/clientlegerlourd", "root", ""); >> TestDB.java
echo             System.out.println("Connexion a la base de donnees reussie!"); >> TestDB.java
echo             conn.close(); >> TestDB.java
echo         } catch (Exception e) { >> TestDB.java
echo             System.out.println("Erreur de connexion a la base de donnees:"); >> TestDB.java
echo             e.printStackTrace(); >> TestDB.java
echo         } >> TestDB.java
echo     } >> TestDB.java
echo } >> TestDB.java

javac -cp !LIB_DIR!\mysql-connector-j-8.0.33.jar TestDB.java
java -cp .;!LIB_DIR!\mysql-connector-j-8.0.33.jar TestDB

del TestDB.java
del TestDB.class

echo.
echo =============================================
echo Fin du mode debug
echo =============================================

pause
