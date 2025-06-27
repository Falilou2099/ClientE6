@echo off
setlocal EnableDelayedExpansion

echo =============================================
echo Solution complete pour BigPharma Admin
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

echo.
echo Verification de la base de donnees...
echo.

REM Verifier si la base de donnees existe
echo Verification de l'existence de la base de donnees...
echo CREATE DATABASE IF NOT EXISTS clientlegerlourd; > create_db.sql
mysql -u root < create_db.sql 2>nul
if %ERRORLEVEL% NEQ 0 (
    echo ATTENTION: Impossible de creer la base de donnees.
    echo Veuillez verifier que:
    echo 1. MySQL est en cours d'execution
    echo 2. L'utilisateur "root" a les droits necessaires
    echo.
    echo Voulez-vous continuer quand meme? (O/N)
    set /p continue=
    if /i "!continue!" NEQ "O" (
        exit /b 1
    )
) else (
    echo Base de donnees "clientlegerlourd" creee ou existante.
)
del create_db.sql

REM Mettre a jour le fichier de configuration de la base de donnees
echo Mise a jour de la configuration de la base de donnees...
if exist "src\com\bigpharma\admin\utils\DatabaseConfig.java" (
    echo package com.bigpharma.admin.utils;> src\com\bigpharma\admin\utils\DatabaseConfig.java
    echo.>> src\com\bigpharma\admin\utils\DatabaseConfig.java
    echo /**>> src\com\bigpharma\admin\utils\DatabaseConfig.java
    echo  * Configuration de la base de donnees>> src\com\bigpharma\admin\utils\DatabaseConfig.java
    echo  * Contient les parametres de connexion a la base de donnees>> src\com\bigpharma\admin\utils\DatabaseConfig.java
    echo  */>> src\com\bigpharma\admin\utils\DatabaseConfig.java
    echo public class DatabaseConfig {>> src\com\bigpharma\admin\utils\DatabaseConfig.java
    echo     public static final String DB_URL = "jdbc:mysql://localhost:3306/clientlegerlourd?useUnicode=true&characterEncoding=utf8";>> src\com\bigpharma\admin\utils\DatabaseConfig.java
    echo     public static final String DB_USER = "root";>> src\com\bigpharma\admin\utils\DatabaseConfig.java
    echo     public static final String DB_PASSWORD = "";>> src\com\bigpharma\admin\utils\DatabaseConfig.java
    echo }>> src\com\bigpharma\admin\utils\DatabaseConfig.java
    echo Configuration de la base de donnees mise a jour.
)

echo.
echo Creation des tables necessaires...
echo.

REM Creer les tables necessaires
echo -- Creation des tables > create_tables.sql
echo CREATE TABLE IF NOT EXISTS categories ( >> create_tables.sql
echo   id INT AUTO_INCREMENT PRIMARY KEY, >> create_tables.sql
echo   nom VARCHAR(100) NOT NULL, >> create_tables.sql
echo   description TEXT >> create_tables.sql
echo ); >> create_tables.sql
echo. >> create_tables.sql

echo CREATE TABLE IF NOT EXISTS categorie ( >> create_tables.sql
echo   id INT AUTO_INCREMENT PRIMARY KEY, >> create_tables.sql
echo   nom VARCHAR(100) NOT NULL, >> create_tables.sql
echo   description TEXT >> create_tables.sql
echo ); >> create_tables.sql
echo. >> create_tables.sql

echo CREATE TABLE IF NOT EXISTS category ( >> create_tables.sql
echo   id INT AUTO_INCREMENT PRIMARY KEY, >> create_tables.sql
echo   nom VARCHAR(100) NOT NULL, >> create_tables.sql
echo   description TEXT >> create_tables.sql
echo ); >> create_tables.sql
echo. >> create_tables.sql

echo CREATE TABLE IF NOT EXISTS categorie_medicament ( >> create_tables.sql
echo   id INT AUTO_INCREMENT PRIMARY KEY, >> create_tables.sql
echo   nom VARCHAR(100) NOT NULL, >> create_tables.sql
echo   description TEXT >> create_tables.sql
echo ); >> create_tables.sql
echo. >> create_tables.sql

echo CREATE TABLE IF NOT EXISTS users ( >> create_tables.sql
echo   id INT AUTO_INCREMENT PRIMARY KEY, >> create_tables.sql
echo   email VARCHAR(255) NOT NULL UNIQUE, >> create_tables.sql
echo   password VARCHAR(255) NOT NULL, >> create_tables.sql
echo   role VARCHAR(50) DEFAULT 'user', >> create_tables.sql
echo   date_creation DATETIME DEFAULT CURRENT_TIMESTAMP, >> create_tables.sql
echo   date_derniere_connexion DATETIME >> create_tables.sql
echo ); >> create_tables.sql
echo. >> create_tables.sql

echo CREATE TABLE IF NOT EXISTS pharmacies ( >> create_tables.sql
echo   id INT AUTO_INCREMENT PRIMARY KEY, >> create_tables.sql
echo   nom VARCHAR(255) NOT NULL, >> create_tables.sql
echo   adresse TEXT, >> create_tables.sql
echo   telephone VARCHAR(20), >> create_tables.sql
echo   email VARCHAR(255), >> create_tables.sql
echo   notes TEXT >> create_tables.sql
echo ); >> create_tables.sql
echo. >> create_tables.sql

echo CREATE TABLE IF NOT EXISTS produits ( >> create_tables.sql
echo   id INT AUTO_INCREMENT PRIMARY KEY, >> create_tables.sql
echo   nom VARCHAR(255) NOT NULL, >> create_tables.sql
echo   description TEXT, >> create_tables.sql
echo   prix DECIMAL(10,2) NOT NULL, >> create_tables.sql
echo   quantite_stock INT DEFAULT 0, >> create_tables.sql
echo   categorie VARCHAR(100), >> create_tables.sql
echo   categorie_id INT, >> create_tables.sql
echo   fournisseur_id INT, >> create_tables.sql
echo   est_ordonnance BOOLEAN DEFAULT FALSE, >> create_tables.sql
echo   image VARCHAR(255), >> create_tables.sql
echo   date_ajout DATETIME DEFAULT CURRENT_TIMESTAMP, >> create_tables.sql
echo   date_modification DATETIME DEFAULT CURRENT_TIMESTAMP >> create_tables.sql
echo ); >> create_tables.sql
echo. >> create_tables.sql

echo CREATE TABLE IF NOT EXISTS commandes ( >> create_tables.sql
echo   id INT AUTO_INCREMENT PRIMARY KEY, >> create_tables.sql
echo   reference VARCHAR(50) NOT NULL, >> create_tables.sql
echo   date_commande DATETIME DEFAULT CURRENT_TIMESTAMP, >> create_tables.sql
echo   statut VARCHAR(50) DEFAULT 'pending', >> create_tables.sql
echo   client_id INT, >> create_tables.sql
echo   pharmacy_id INT, >> create_tables.sql
echo   montant_total DECIMAL(10,2) DEFAULT 0, >> create_tables.sql
echo   notes TEXT >> create_tables.sql
echo ); >> create_tables.sql
echo. >> create_tables.sql

echo CREATE TABLE IF NOT EXISTS lignes_commande ( >> create_tables.sql
echo   id INT AUTO_INCREMENT PRIMARY KEY, >> create_tables.sql
echo   order_id INT NOT NULL, >> create_tables.sql
echo   product_id INT NOT NULL, >> create_tables.sql
echo   quantite INT NOT NULL, >> create_tables.sql
echo   prix_unitaire DECIMAL(10,2) NOT NULL, >> create_tables.sql
echo   prix_total DECIMAL(10,2) NOT NULL >> create_tables.sql
echo ); >> create_tables.sql
echo. >> create_tables.sql

echo -- Insertion de donnees de test >> create_tables.sql
echo INSERT IGNORE INTO users (email, password, role) VALUES >> create_tables.sql
echo ('admin@bigpharma.com', '$2a$12$tGzVf7LR47YDvb8YBk6xIe3o3UNMmVLJ.zRmcq.Jo8d3z.xJiQTl2', 'admin'); >> create_tables.sql
echo. >> create_tables.sql

echo -- Insertion des categories >> create_tables.sql
echo INSERT IGNORE INTO categories (nom, description) VALUES >> create_tables.sql
echo ('Antibiotiques', 'Médicaments qui combattent les infections bactériennes'), >> create_tables.sql
echo ('Analgésiques', 'Médicaments qui soulagent la douleur'), >> create_tables.sql
echo ('Anti-inflammatoires', 'Médicaments qui réduisent l''inflammation'), >> create_tables.sql
echo ('Antihistaminiques', 'Médicaments qui traitent les allergies'), >> create_tables.sql
echo ('Antidépresseurs', 'Médicaments qui traitent la dépression'); >> create_tables.sql
echo. >> create_tables.sql

echo -- Copier les categories dans les autres tables >> create_tables.sql
echo INSERT IGNORE INTO categorie (nom, description) >> create_tables.sql
echo SELECT nom, description FROM categories; >> create_tables.sql
echo. >> create_tables.sql

echo INSERT IGNORE INTO category (nom, description) >> create_tables.sql
echo SELECT nom, description FROM categories; >> create_tables.sql
echo. >> create_tables.sql

echo INSERT IGNORE INTO categorie_medicament (nom, description) >> create_tables.sql
echo SELECT nom, description FROM categories; >> create_tables.sql
echo. >> create_tables.sql

echo -- Insertion de produits de test >> create_tables.sql
echo INSERT IGNORE INTO produits (nom, description, prix, quantite_stock, categorie, est_ordonnance, image) VALUES >> create_tables.sql
echo ('Amoxicilline', 'Antibiotique à large spectre', 12.99, 100, 'Antibiotiques', 1, 'amoxicilline.jpg'), >> create_tables.sql
echo ('Ibuprofène', 'Anti-inflammatoire non stéroïdien', 5.99, 200, 'Anti-inflammatoires', 0, 'ibuprofen.jpg'), >> create_tables.sql
echo ('Paracétamol', 'Analgésique et antipyrétique', 3.99, 300, 'Analgésiques', 0, 'paracetamol.jpg'); >> create_tables.sql

mysql -u root clientlegerlourd < create_tables.sql 2>nul
if %ERRORLEVEL% NEQ 0 (
    echo ATTENTION: Impossible de creer les tables.
    echo Les tables seront peut-etre creees lors de l'execution de l'application.
) else (
    echo Tables creees avec succes.
)
del create_tables.sql

echo.
echo Preparation de l'environnement d'execution...
echo.

REM Creer le dossier pour les images de produits s'il n'existe pas
if not exist "C:\xampp\htdocs\bigpharma\public\images\products" (
    mkdir "C:\xampp\htdocs\bigpharma\public\images\products"
)

REM Creer le dossier pour les images par defaut s'il n'existe pas
if not exist "C:\xampp\htdocs\bigpharma\img" (
    mkdir "C:\xampp\htdocs\bigpharma\img"
)

REM Creer une image par defaut si elle n'existe pas
if not exist "C:\xampp\htdocs\bigpharma\img\default.png" (
    echo Telechargement de l'image par defaut...
    powershell -Command "& {Invoke-WebRequest -Uri 'https://cdn-icons-png.flaticon.com/512/1170/1170576.png' -OutFile 'C:\xampp\htdocs\bigpharma\img\default.png'}"
)

echo.
echo Compilation et execution de l'application...
echo.

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

echo Compilation du code source...
echo Recherche des fichiers Java...
dir /s /b !SRC_DIR!\*.java > sources.txt
javac -d !BUILD_DIR! --module-path !JAVAFX_MODS! --add-modules !JAVAFX_MODULES! -cp !CLASSPATH! @sources.txt
del sources.txt

if %ERRORLEVEL% NEQ 0 (
    echo ERREUR: La compilation a echoue.
    echo Tentative d'execution directe a partir des sources...
    
    java --module-path !JAVAFX_MODS! --add-modules !JAVAFX_MODULES! -cp !SRC_DIR!;!CLASSPATH! com.bigpharma.admin.Main
) else (
    echo Compilation reussie. Execution de l'application...
    
    java --module-path !JAVAFX_MODS! --add-modules !JAVAFX_MODULES! -cp !BUILD_DIR!;!CLASSPATH! com.bigpharma.admin.Main
)

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo ERREUR: L'application n'a pas pu demarrer.
    echo Code d'erreur: %ERRORLEVEL%
    echo.
    echo Veuillez verifier que:
    echo 1. Java 11 ou superieur est installe
    echo 2. Les dependances ont ete correctement telechargees
    echo 3. La base de donnees est accessible
    echo.
)

pause
