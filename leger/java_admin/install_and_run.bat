@echo off
setlocal EnableDelayedExpansion

echo =============================================
echo Installation et demarrage de BigPharma Admin
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

REM Creer le dossier lib s'il n'existe pas
if not exist "lib" mkdir lib

REM Telecharger les dependances si elles n'existent pas
if not exist "lib\mysql-connector-j-8.0.33.jar" (
    echo Telechargement des dependances...
    powershell -Command "& {Invoke-WebRequest -Uri 'https://repo1.maven.org/maven2/com/mysql/mysql-connector-j/8.0.33/mysql-connector-j-8.0.33.jar' -OutFile 'lib\mysql-connector-j-8.0.33.jar'}"
    powershell -Command "& {Invoke-WebRequest -Uri 'https://repo1.maven.org/maven2/org/mindrot/jbcrypt/0.4/jbcrypt-0.4.jar' -OutFile 'lib\jbcrypt-0.4.jar'}"
)

echo.
echo Verification de la configuration de la base de donnees...
echo.

REM Verifier et mettre a jour le fichier de configuration de la base de donnees
if not exist "src\com\bigpharma\admin\utils\DatabaseConfig.java" (
    echo ERREUR: Le fichier de configuration de la base de donnees est introuvable.
    pause
    exit /b 1
)

echo Configuration de la base de donnees:
echo - URL: jdbc:mysql://localhost:3306/clientlegerlourd
echo - Utilisateur: root
echo - Mot de passe: (vide)
echo.
echo Si ces parametres sont incorrects, veuillez modifier le fichier:
echo src\com\bigpharma\admin\utils\DatabaseConfig.java
echo.

REM Verifier si la base de donnees est accessible
echo Test de connexion a la base de donnees...
java -cp "lib\mysql-connector-j-8.0.33.jar" com.mysql.cj.jdbc.admin.MysqlCheck jdbc:mysql://localhost:3306/clientlegerlourd root "" >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    echo ATTENTION: Impossible de se connecter a la base de donnees.
    echo Assurez-vous que:
    echo 1. MySQL est en cours d'execution
    echo 2. La base de donnees "clientlegerlourd" existe
    echo 3. L'utilisateur "root" a acces a la base de donnees
    echo.
    echo Voulez-vous continuer quand meme? (O/N)
    set /p continue=
    if /i "!continue!" NEQ "O" (
        exit /b 1
    )
)

echo.
echo Verification de la structure de la base de donnees...
echo.

REM Verifier si les tables necessaires existent
echo Les tables suivantes sont necessaires pour l'application:
echo - produits (avec colonnes: id, nom, description, prix, quantite_stock, categorie, est_ordonnance, image)
echo - categories (avec colonnes: id, nom, description)
echo - users (avec colonnes: id, email, password, role)
echo - pharmacies (avec colonnes: id, nom, adresse, telephone, email)
echo - commandes (avec colonnes: id, reference, date_commande, statut, client_id, pharmacy_id, montant_total)
echo - lignes_commande (avec colonnes: id, order_id, product_id, quantite, prix_unitaire, prix_total)
echo.

echo Preparation de l'environnement d'execution...
echo.

REM Creer le dossier pour les images de produits s'il n'existe pas
if not exist "C:\xampp\htdocs\bigpharma\public\images\products" (
    mkdir "C:\xampp\htdocs\bigpharma\public\images\products"
)

echo.
echo Demarrage de l'application...
echo.

REM Lancer l'application avec les dependances
java -jar -Dfile.encoding=UTF-8 -Djavafx.modules=javafx.controls,javafx.fxml,javafx.web,javafx.media,javafx.graphics BigPharmaAdmin.jar

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
