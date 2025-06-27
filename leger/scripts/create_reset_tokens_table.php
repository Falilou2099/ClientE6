<?php
// Connexion à la base de données
require_once __DIR__ . '/../config/database.php';

try {
    // Vérifier si la table existe déjà
    $stmt = $pdo->query("SHOW TABLES LIKE 'password_reset_tokens'");
    $tableExists = $stmt->rowCount() > 0;

    if (!$tableExists) {
        // Créer la table des tokens de réinitialisation
        $pdo->exec("
            CREATE TABLE password_reset_tokens (
                id INT AUTO_INCREMENT PRIMARY KEY,
                user_id INT NOT NULL,
                token VARCHAR(64) NOT NULL,
                created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                expires_at DATETIME NOT NULL,
                used BOOLEAN DEFAULT FALSE,
                FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
                INDEX (token),
                INDEX (expires_at)
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
        ");
        echo "Table 'password_reset_tokens' créée avec succès.\n";
    } else {
        echo "La table 'password_reset_tokens' existe déjà.\n";
    }
} catch (PDOException $e) {
    die("Erreur lors de la création de la table: " . $e->getMessage());
}
