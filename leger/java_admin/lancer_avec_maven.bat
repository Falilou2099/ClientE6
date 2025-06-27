@echo off
setlocal EnableDelayedExpansion

echo =============================================
echo Lancement de BigPharma Admin avec Maven
echo =============================================
echo.

REM Vérifier si Maven est installé
mvn -v >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    echo ERREUR: Maven n'est pas installé ou n'est pas dans le PATH.
    echo Installation de Maven...
    
    REM Télécharger Maven si nécessaire
    if not exist "apache-maven-3.9.6" (
        echo Téléchargement de Maven...
        powershell -Command "& {Invoke-WebRequest -Uri 'https://dlcdn.apache.org/maven/maven-3/3.9.6/binaries/apache-maven-3.9.6-bin.zip' -OutFile 'maven.zip'}"
        echo Extraction de Maven...
        powershell -Command "& {Expand-Archive -Path 'maven.zip' -DestinationPath '.' -Force}"
        powershell -Command "& {Remove-Item -Path 'maven.zip' -Force}"
    )
    
    REM Définir le chemin Maven
    set "M2_HOME=%CD%\apache-maven-3.9.6"
    set "PATH=%M2_HOME%\bin;%PATH%"
    
    echo Maven installé avec succès.
)

echo.
echo Correction du fichier pom.xml...
echo.

REM Corriger le fichier pom.xml
powershell -Command "& {(Get-Content pom.xml) -replace '<n>BigPharma Admin</n>', '<name>BigPharma Admin</name>' | Set-Content pom.xml}"

echo.
echo Vérification de la base de données...
echo.

REM Vérifier si la base de données existe
echo CREATE DATABASE IF NOT EXISTS clientlegerlourd; > create_db.sql
mysql -u root < create_db.sql 2>nul
if %ERRORLEVEL% NEQ 0 (
    echo ATTENTION: Impossible de créer la base de données.
    echo Veuillez vérifier que:
    echo 1. MySQL est en cours d'exécution
    echo 2. L'utilisateur "root" a les droits nécessaires
    echo.
    echo Voulez-vous continuer quand même? (O/N)
    set /p continue=
    if /i "!continue!" NEQ "O" (
        exit /b 1
    )
) else (
    echo Base de données "clientlegerlourd" créée ou existante.
)
del create_db.sql

echo.
echo Création des tables nécessaires...
echo.

REM Créer les tables nécessaires
echo -- Création des tables > create_tables.sql
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
echo   app_access VARCHAR(20) DEFAULT 'both', >> create_tables.sql
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

echo -- Insertion de données de test >> create_tables.sql
echo INSERT IGNORE INTO users (email, password, role, app_access) VALUES >> create_tables.sql
echo ('admin@bigpharma.com', '$2a$12$tGzVf7LR47YDvb8YBk6xIe3o3UNMmVLJ.zRmcq.Jo8d3z.xJiQTl2', 'admin', 'both'); >> create_tables.sql
echo. >> create_tables.sql

echo -- Insertion des catégories >> create_tables.sql
echo INSERT IGNORE INTO categories (nom, description) VALUES >> create_tables.sql
echo ('Antibiotiques', 'Médicaments qui combattent les infections bactériennes'), >> create_tables.sql
echo ('Analgésiques', 'Médicaments qui soulagent la douleur'), >> create_tables.sql
echo ('Anti-inflammatoires', 'Médicaments qui réduisent l''inflammation'), >> create_tables.sql
echo ('Antihistaminiques', 'Médicaments qui traitent les allergies'), >> create_tables.sql
echo ('Antidépresseurs', 'Médicaments qui traitent la dépression'); >> create_tables.sql
echo. >> create_tables.sql

echo -- Copier les catégories dans les autres tables >> create_tables.sql
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
    echo ATTENTION: Impossible de créer les tables.
    echo Les tables seront peut-être créées lors de l'exécution de l'application.
) else (
    echo Tables créées avec succès.
)
del create_tables.sql

echo.
echo Préparation de l'environnement d'exécution...
echo.

REM Créer le dossier pour les images de produits s'il n'existe pas
if not exist "C:\xampp\htdocs\bigpharma\img" (
    mkdir "C:\xampp\htdocs\bigpharma\img"
)

REM Créer une image par défaut si elle n'existe pas
if not exist "C:\xampp\htdocs\bigpharma\img\default.png" (
    echo Téléchargement de l'image par défaut...
    powershell -Command "& {Invoke-WebRequest -Uri 'https://cdn-icons-png.flaticon.com/512/1170/1170576.png' -OutFile 'C:\xampp\htdocs\bigpharma\img\default.png'}"
)

REM Créer le dossier resources/images s'il n'existe pas
if not exist "src\resources\images" (
    mkdir "src\resources\images"
)

REM Créer le dossier resources/css s'il n'existe pas
if not exist "src\resources\css" (
    mkdir "src\resources\css"
)

REM Créer un fichier CSS par défaut s'il n'existe pas
if not exist "src\resources\css\styles.css" (
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

REM Créer un logo par défaut s'il n'existe pas
if not exist "src\resources\images\logo.png" (
    echo Téléchargement du logo...
    powershell -Command "& {Invoke-WebRequest -Uri 'https://cdn-icons-png.flaticon.com/512/2966/2966327.png' -OutFile 'src\resources\images\logo.png'}"
)

REM Créer un logo blanc par défaut s'il n'existe pas
if not exist "src\resources\images\logo_white.png" (
    echo Téléchargement du logo blanc...
    powershell -Command "& {Invoke-WebRequest -Uri 'https://cdn-icons-png.flaticon.com/512/2966/2966327.png' -OutFile 'src\resources\images\logo_white.png'}"
)

REM Créer une image par défaut pour les produits s'il n'existe pas
if not exist "src\resources\images\product_default.png" (
    echo Téléchargement de l'image par défaut pour les produits...
    powershell -Command "& {Invoke-WebRequest -Uri 'https://cdn-icons-png.flaticon.com/512/1170/1170576.png' -OutFile 'src\resources\images\product_default.png'}"
)

echo.
echo Compilation et exécution avec Maven...
echo.

REM Compiler et exécuter avec Maven
mvn clean javafx:run

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo ERREUR: L'application n'a pas pu démarrer.
    echo Code d'erreur: %ERRORLEVEL%
    echo.
    echo Tentative de débogage...
    echo.
    
    REM Afficher les erreurs de compilation
    mvn clean compile -e
    
    echo.
    echo Vérification de la base de données...
    echo.
    
    REM Tester la connexion à la base de données
    echo import java.sql.*; > TestDB.java
    echo public class TestDB { >> TestDB.java
    echo     public static void main(String[] args) { >> TestDB.java
    echo         try { >> TestDB.java
    echo             Class.forName("com.mysql.cj.jdbc.Driver"); >> TestDB.java
    echo             Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/clientlegerlourd?useUnicode=true&characterEncoding=utf8", "root", ""); >> TestDB.java
    echo             System.out.println("Connexion à la base de données réussie!"); >> TestDB.java
    echo             conn.close(); >> TestDB.java
    echo         } catch (Exception e) { >> TestDB.java
    echo             System.out.println("Erreur de connexion à la base de données:"); >> TestDB.java
    echo             e.printStackTrace(); >> TestDB.java
    echo         } >> TestDB.java
    echo     } >> TestDB.java
    echo } >> TestDB.java
    
    javac TestDB.java
    java -cp .;target\dependency\mysql-connector-java-8.0.28.jar TestDB
    
    del TestDB.java
    del TestDB.class
)

echo.
echo =============================================
echo Fin du script de lancement
echo =============================================

pause
