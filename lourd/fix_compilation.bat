@echo off
echo Correction automatique des erreurs de compilation...

REM Définir les chemins des fichiers
set ADMIN_PANEL_CONTROLLER=src\main\java\com\gestionpharma\controllers\AdminPanelController.java
set STOCK_SERVICE=src\main\java\com\gestionpharma\services\StockService.java

REM Correction des appels à loadStocksData() dans AdminPanelController.java
echo Correction des appels à loadStocksData() dans AdminPanelController.java...
powershell -Command "(Get-Content '%ADMIN_PANEL_CONTROLLER%') -replace 'loadStocksData\(\)', 'loadStockData()' | Set-Content '%ADMIN_PANEL_CONTROLLER%'"

REM Correction des problèmes de logger dans StockService.java
echo Correction des problèmes de logger dans StockService.java...
powershell -Command "$content = Get-Content '%STOCK_SERVICE%' -Raw; if (!($content -match 'import java.util.logging.Logger')) { $content = $content -replace 'import java.util.List;', 'import java.util.List;^nimport java.util.logging.Logger;'; $content | Set-Content '%STOCK_SERVICE%' }"

REM Ajouter la définition du logger si elle n'existe pas
powershell -Command "$content = Get-Content '%STOCK_SERVICE%' -Raw; if (!($content -match 'private static final Logger logger')) { $content = $content -replace 'public class StockService \{', 'public class StockService {^n    // Logger pour tracer les opérations^n    private static final Logger logger = Logger.getLogger(StockService.class.getName());'; $content | Set-Content '%STOCK_SERVICE%' }"

REM Remplacer ConnectionPool par DatabaseConfig.getConnection()
powershell -Command "(Get-Content '%STOCK_SERVICE%') -replace 'ConnectionPool.getInstance\(\).getConnection\(\)', 'DatabaseConfig.getConnection()' | Set-Content '%STOCK_SERVICE%'"
powershell -Command "(Get-Content '%STOCK_SERVICE%') -replace 'ConnectionPool.getConnection\(\)', 'DatabaseConfig.getConnection()' | Set-Content '%STOCK_SERVICE%'"

echo Corrections terminées. Tentative de compilation...

REM Compiler le projet
call mvn clean compile

echo.
echo Si la compilation a réussi, vous pouvez maintenant démarrer l'application avec:
echo mvn javafx:run
echo.

pause
