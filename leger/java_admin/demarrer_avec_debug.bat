@echo off
setlocal EnableDelayedExpansion

echo =============================================
echo Demarrage BigPharma Admin avec debug
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

REM Creer les repertoires necessaires
if not exist "build" mkdir build
if not exist "lib" mkdir lib
if not exist "javafx-sdk" mkdir javafx-sdk

REM Telecharger les dependances si elles n'existent pas
if not exist "lib\mysql-connector-j-8.0.33.jar" (
    echo Telechargement des dependances...
    powershell -Command "& {Invoke-WebRequest -Uri 'https://repo1.maven.org/maven2/com/mysql/mysql-connector-j/8.0.33/mysql-connector-j-8.0.33.jar' -OutFile 'lib\mysql-connector-j-8.0.33.jar'}"
    powershell -Command "& {Invoke-WebRequest -Uri 'https://repo1.maven.org/maven2/org/mindrot/jbcrypt/0.4/jbcrypt-0.4.jar' -OutFile 'lib\jbcrypt-0.4.jar'}"
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

REM Verifier si les fichiers de ressources existent
if not exist "src\resources\css\styles.css" (
    echo Creation du fichier CSS...
    if not exist "src\resources\css" mkdir src\resources\css
    echo /* Styles CSS pour l'application BigPharma Admin */ > src\resources\css\styles.css
    echo. >> src\resources\css\styles.css
    echo .root { >> src\resources\css\styles.css
    echo     -fx-font-family: "Arial"; >> src\resources\css\styles.css
    echo     -fx-background-color: #f5f5f5; >> src\resources\css\styles.css
    echo } >> src\resources\css\styles.css
    echo. >> src\resources\css\styles.css
    echo .button { >> src\resources\css\styles.css
    echo     -fx-background-color: #4CAF50; >> src\resources\css\styles.css
    echo     -fx-text-fill: white; >> src\resources\css\styles.css
    echo } >> src\resources\css\styles.css
)

if not exist "src\resources\images\logo.png" (
    echo Creation du logo...
    if not exist "src\resources\images" mkdir src\resources\images
    powershell -Command "& {Invoke-WebRequest -Uri 'https://cdn-icons-png.flaticon.com/512/2966/2966327.png' -OutFile 'src\resources\images\logo.png'}"
)

if not exist "src\resources\images\product_default.png" (
    echo Creation de l'image par defaut...
    powershell -Command "& {Invoke-WebRequest -Uri 'https://cdn-icons-png.flaticon.com/512/1170/1170576.png' -OutFile 'src\resources\images\product_default.png'}"
)

REM Definir les chemins
set SRC_DIR=src
set BUILD_DIR=build
set LIB_DIR=lib
set JAVAFX_SDK=javafx-sdk
set JAVAFX_MODS=!JAVAFX_SDK!\lib

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

echo.
echo Compilation du code source...
echo.

REM Compiler le code source
javac -d !BUILD_DIR! --module-path !JAVAFX_MODS! --add-modules !JAVAFX_MODULES! -cp !CLASSPATH! !SRC_DIR!\com\bigpharma\admin\Main.java

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo ERREUR: La compilation a echoue.
    echo.
    echo Tentative d'execution directe a partir des sources...
    echo.
    
    java --module-path !JAVAFX_MODS! --add-modules !JAVAFX_MODULES! -cp !SRC_DIR!;!CLASSPATH! com.bigpharma.admin.Main
) else (
    echo.
    echo Compilation reussie. Execution de l'application...
    echo.
    
    java --module-path !JAVAFX_MODS! --add-modules !JAVAFX_MODULES! -cp !BUILD_DIR!;!CLASSPATH! com.bigpharma.admin.Main
)

echo.
echo Code d'erreur: %ERRORLEVEL%
echo.

if %ERRORLEVEL% NEQ 0 (
    echo Analyse des erreurs:
    echo 1. Verifiez que toutes les dependances sont presentes
    echo 2. Verifiez que la base de donnees MySQL est en cours d'execution
    echo 3. Verifiez que la base de donnees "clientlegerlourd" existe
    
    REM Tester la connexion a la base de donnees
    echo.
    echo Test de connexion a la base de donnees...
    echo.
    
    echo import java.sql.*; > TestDB.java
    echo public class TestDB { >> TestDB.java
    echo     public static void main(String[] args) { >> TestDB.java
    echo         try { >> TestDB.java
    echo             Class.forName("com.mysql.cj.jdbc.Driver"); >> TestDB.java
    echo             Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/clientlegerlourd?useUnicode=true&characterEncoding=utf8", "root", ""); >> TestDB.java
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
)

echo.
echo =============================================
echo Fin du script de demarrage
echo =============================================

pause
