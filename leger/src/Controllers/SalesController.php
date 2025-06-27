<?php
namespace Controllers;

use Models\Order;
use Models\Client;
use Models\Product;

class SalesController extends BaseController {
    public function __construct() {
        parent::__construct();
    }
    /**
     * Affiche la liste des ventes (historique)
     */
    public function index() {
        // Vérifier si l'utilisateur est connecté
        if (!isset($_SESSION['user_id'])) {
            header('Location: /bigpharma/login');
            exit;
        }
        
        // Afficher simplement la vue avec le message de maintenance
        require_once __DIR__ . '/../Views/sales/index.php';
    }

    /**
     * Affiche les détails d'une vente
     */
    public function show($id) {
        // Vérifier si l'utilisateur est connecté
        if (!isset($_SESSION['user_id'])) {
            $_SESSION['error'] = "Vous devez être connecté pour accéder à cette page.";
            header('Location: /bigpharma/login');
            exit;
        }
        
        // Afficher simplement la vue avec le message de maintenance
        require_once __DIR__ . '/../Views/sales/show.php';
    }

    /**
     * Récupère les ventes avec pagination, recherche, filtrage et tri
     */
    private function getSales($search = '', $page = 1, $perPage = 10, $pharmacieId = null, $dateDebut = '', $dateFin = '', $produitId = 0, $clientId = 0, $sort = 'date_desc') {
        // Calculer l'offset pour la pagination
        $offset = ($page - 1) * $perPage;
        
        // Connexion à la base de données
        $pdo = $this->initPDO();
        
        try {
            // Vérifier si la table commandes existe
            $stmt = $pdo->query("SHOW TABLES LIKE 'commandes'");
            $tableExists = $stmt->rowCount() > 0;
            
            if (!$tableExists) {
                // La table n'existe pas, retourner un tableau vide
                error_log("La table 'commandes' n'existe pas dans la base de données.");
                return [];
            }
            
            // Vérifier les colonnes de la table commandes
            $stmt = $pdo->query("DESCRIBE commandes");
            $columns = $stmt->fetchAll(\PDO::FETCH_COLUMN);
            
            // Construire la requête SQL en fonction des colonnes disponibles
            $query = "SELECT c.id AS commande_id, c.date_commande, c.montant_total";
            
            // Vérifier si les colonnes optionnelles existent
            if (in_array('mode_paiement', $columns)) {
                $query .= ", c.mode_paiement";
            } else {
                $query .= ", 'Non spécifié' AS mode_paiement";
            }
            
            if (in_array('reduction', $columns)) {
                $query .= ", c.reduction";
            } else {
                $query .= ", 0 AS reduction";
            }
            
            $query .= ",
                        cl.id AS client_id, cl.nom, cl.prenom";
            
            // Vérifier si les colonnes email et telephone existent dans la table clients
            $stmt = $pdo->query("DESCRIBE clients");
            $clientColumns = $stmt->fetchAll(\PDO::FETCH_COLUMN);
            
            if (in_array('email', $clientColumns)) {
                $query .= ", cl.email";
            } else {
                $query .= ", NULL AS email";
            }
            
            if (in_array('telephone', $clientColumns)) {
                $query .= ", cl.telephone";
            } else {
                $query .= ", NULL AS telephone";
            }
            
            $query .= ",
                        COUNT(lc.id) AS nombre_produits";
            
            // Vérifier si la table users existe et si elle a une colonne nom
            $stmt = $pdo->query("SHOW TABLES LIKE 'users'");
            $usersTableExists = $stmt->rowCount() > 0;
            
            if ($usersTableExists) {
                $stmt = $pdo->query("DESCRIBE users");
                $userColumns = $stmt->fetchAll(\PDO::FETCH_COLUMN);
                
                if (in_array('nom', $userColumns) && in_array('user_id', $columns)) {
                    $query .= ",
                        u.nom AS vendeur_nom";
                } else {
                    $query .= ",
                        NULL AS vendeur_nom";
                }
            } else {
                $query .= ",
                        NULL AS vendeur_nom";
            }
            
            $query .= "
                 FROM commandes c
                 LEFT JOIN clients cl ON c.client_id = cl.id
                 LEFT JOIN lignes_commande lc ON c.id = lc.commande_id";
            
            if ($usersTableExists && in_array('user_id', $columns) && in_array('nom', $userColumns)) {
                $query .= "
                 LEFT JOIN users u ON c.user_id = u.id";
            }
            
            $query .= "
                 LEFT JOIN produits p ON lc.produit_id = p.id";
            
            $params = [];
            $whereConditions = [];
            
            // Ajouter la condition de recherche si nécessaire
            if (!empty($search)) {
                $whereConditions[] = "(cl.nom LIKE :search OR cl.prenom LIKE :search OR p.nom LIKE :search)";
                $params[':search'] = '%' . $search . '%';
            }
            
            // Ajouter la condition de pharmacie si nécessaire
            if ($pharmacieId && in_array('pharmacie_id', $columns)) {
                $whereConditions[] = "c.pharmacie_id = :pharmacie_id";
                $params[':pharmacie_id'] = $pharmacieId;
            }
            
            // Ajouter le filtre de date de début si spécifié
            if (!empty($dateDebut)) {
                $whereConditions[] = "DATE(c.date_commande) >= :date_debut";
                $params[':date_debut'] = $dateDebut;
            }
            
            // Ajouter le filtre de date de fin si spécifié
            if (!empty($dateFin)) {
                $whereConditions[] = "DATE(c.date_commande) <= :date_fin";
                $params[':date_fin'] = $dateFin;
            }
            
            // Ajouter le filtre de produit si spécifié
            if ($produitId > 0) {
                $whereConditions[] = "lc.produit_id = :produit_id";
                $params[':produit_id'] = $produitId;
            }
            
            // Ajouter le filtre de client si spécifié
            if ($clientId > 0) {
                $whereConditions[] = "c.client_id = :client_id";
                $params[':client_id'] = $clientId;
            }
            
            // Ajouter les conditions WHERE si nécessaire
            if (!empty($whereConditions)) {
                $query .= " WHERE " . implode(" AND ", $whereConditions);
            }
            
            // Grouper par commande
            $query .= " GROUP BY c.id";
            
            // Déterminer l'ordre de tri
            switch ($sort) {
                case 'date_asc':
                    $query .= " ORDER BY c.date_commande ASC";
                    break;
                case 'montant_desc':
                    $query .= " ORDER BY c.montant_total DESC";
                    break;
                case 'montant_asc':
                    $query .= " ORDER BY c.montant_total ASC";
                    break;
                case 'date_desc':
                default:
                    $query .= " ORDER BY c.date_commande DESC";
                    break;
            }
            
            // Ajouter la pagination
            $query .= " LIMIT :offset, :perPage";
            $params[':offset'] = $offset;
            $params[':perPage'] = $perPage;
            
            // Exécuter la requête
            $stmt = $pdo->prepare($query);
            
            // Binder les paramètres
            foreach ($params as $key => $value) {
                if ($key == ':offset' || $key == ':perPage' || $key == ':produit_id' || $key == ':client_id') {
                    $stmt->bindValue($key, $value, \PDO::PARAM_INT);
                } else {
                    $stmt->bindValue($key, $value);
                }
            }
            
            $stmt->execute();
            
            // Retourner les résultats
            return $stmt->fetchAll(\PDO::FETCH_ASSOC);
        } catch (\Exception $e) {
            // Journaliser l'erreur
            error_log('Erreur lors de la récupération des ventes: ' . $e->getMessage());
            return [];
        }
    }

    /**
     * Récupère le nombre total de ventes avec filtres
     */
    private function getTotalSalesCount($search = '', $pharmacieId = null, $dateDebut = '', $dateFin = '', $produitId = 0, $clientId = 0) {
        // Connexion à la base de données
        $pdo = $this->initPDO();
        
        try {
            // Vérifier si la table commandes existe
            $stmt = $pdo->query("SHOW TABLES LIKE 'commandes'");
            $tableExists = $stmt->rowCount() > 0;
            
            if (!$tableExists) {
                // La table n'existe pas, retourner 0
                error_log("La table 'commandes' n'existe pas dans la base de données.");
                return 0;
            }
            
            // Vérifier les colonnes de la table commandes
            $stmt = $pdo->query("DESCRIBE commandes");
            $columns = $stmt->fetchAll(\PDO::FETCH_COLUMN);
            
            // Construire la requête SQL
            $query = "SELECT COUNT(DISTINCT c.id) AS total
                     FROM commandes c
                     LEFT JOIN clients cl ON c.client_id = cl.id
                     LEFT JOIN lignes_commande lc ON c.id = lc.commande_id
                     LEFT JOIN produits p ON lc.produit_id = p.id";
            
            $params = [];
            $whereConditions = [];
            
            // Ajouter la condition de recherche si nécessaire
            if (!empty($search)) {
                $whereConditions[] = "(cl.nom LIKE :search OR cl.prenom LIKE :search OR p.nom LIKE :search)";
                $params[':search'] = '%' . $search . '%';
            }
            
            // Ajouter la condition de pharmacie si nécessaire
            if ($pharmacieId && in_array('pharmacie_id', $columns)) {
                $whereConditions[] = "c.pharmacie_id = :pharmacie_id";
                $params[':pharmacie_id'] = $pharmacieId;
            }
            
            // Ajouter le filtre de date de début si spécifié
            if (!empty($dateDebut)) {
                $whereConditions[] = "DATE(c.date_commande) >= :date_debut";
                $params[':date_debut'] = $dateDebut;
            }
            
            // Ajouter le filtre de date de fin si spécifié
            if (!empty($dateFin)) {
                $whereConditions[] = "DATE(c.date_commande) <= :date_fin";
                $params[':date_fin'] = $dateFin;
            }
            
            // Ajouter le filtre de produit si spécifié
            if ($produitId > 0) {
                $whereConditions[] = "lc.produit_id = :produit_id";
                $params[':produit_id'] = $produitId;
            }
            
            // Ajouter le filtre de client si spécifié
            if ($clientId > 0) {
                $whereConditions[] = "c.client_id = :client_id";
                $params[':client_id'] = $clientId;
            }
            
            // Ajouter les conditions WHERE si nécessaire
            if (!empty($whereConditions)) {
                $query .= " WHERE " . implode(" AND ", $whereConditions);
            }
            
            // Exécuter la requête
            $stmt = $pdo->prepare($query);
            
            // Binder les paramètres
            foreach ($params as $key => $value) {
                if ($key == ':produit_id' || $key == ':client_id') {
                    $stmt->bindValue($key, $value, \PDO::PARAM_INT);
                } else {
                    $stmt->bindValue($key, $value);
                }
            }
            
            $query .= "
                     FROM commandes c";
            
            if ($clientsTableExists) {
                $query .= "
                     LEFT JOIN clients cl ON c.client_id = cl.id";
            }
            
            $query .= "
                     WHERE c.id = :id";
            
            $stmt = $pdo->prepare($query);
            $stmt->bindValue(':id', $id, \PDO::PARAM_INT);
            $stmt->execute();
            
            $commande = $stmt->fetch(\PDO::FETCH_ASSOC);
            
            if (!$commande) {
                // Récupérer les détails de la vente
                $saleDetails = $this->getSaleDetails($id);
                
                if ($saleDetails === false) {
                    $_SESSION['error'] = "La vente avec l'ID $id n'existe pas.";
                    header('Location: /bigpharma/sales');
                    exit;
                } else {
                    // La commande n'existe pas, mais les détails de la vente existent
                    $commande = [
                        'id' => $id,
                        'client_nom' => '',
                        'client_prenom' => '',
                        'client_email' => '',
                        'client_telephone' => '',
                        'date_commande' => '',
                        'montant_total' => 0,
                        'produits' => $saleDetails
                    ];
                }
            } else {
                // Récupérer les produits de la commande
                $produits = $this->getSaleDetails($id);
                
                if ($produits === false) {
                    $_SESSION['error'] = "La vente avec l'ID $id n'existe pas.";
                    header('Location: /bigpharma/sales');
                    exit;
                }
                
                $commande['produits'] = $produits;
            }
            
            // Rendre la vue
            require_once __DIR__ . '/../../views/sales/show.php';
        } catch (\Exception $e) {
            // Journaliser l'erreur
            error_log('Erreur lors de l\'affichage des détails de la vente: ' . $e->getMessage());
            $_SESSION['error'] = "Une erreur est survenue lors de l'affichage des détails de la vente.";
            header('Location: /bigpharma/sales');
            exit;
        }
    }

    /**
     * Récupère la liste des produits pour le filtre
     */
    private function getProductsList() {
        // Connexion à la base de données
        $pdo = $this->initPDO();
        
        try {
            // Vérifier si les tables nécessaires existent
            $stmt = $pdo->query("SHOW TABLES LIKE 'produits'");
            $produitsExists = $stmt->rowCount() > 0;
            
            $stmt = $pdo->query("SHOW TABLES LIKE 'lignes_commande'");
            $lignesCommandeExists = $stmt->rowCount() > 0;
            
            if (!$produitsExists || !$lignesCommandeExists) {
                // Une des tables n'existe pas, retourner un tableau vide
                error_log("Une des tables nécessaires n'existe pas dans la base de données.");
                return [];
            }
            
            // Requête pour récupérer les produits qui ont été vendus
            $query = "SELECT DISTINCT p.id, p.nom
                     FROM produits p
                     JOIN lignes_commande lc ON p.id = lc.produit_id
                     ORDER BY p.nom ASC";
            
            $stmt = $pdo->prepare($query);
            $stmt->execute();
            
            return $stmt->fetchAll(\PDO::FETCH_ASSOC);
        } catch (\Exception $e) {
            // Journaliser l'erreur
            error_log('Erreur lors de la récupération des produits: ' . $e->getMessage());
            return [];
        }
    }
    
    /**
     * Récupère la liste des clients pour le filtre
     */
    private function getClientsList() {
        // Connexion à la base de données
        $pdo = $this->initPDO();
        
        try {
            // Vérifier si la table clients existe
            $stmt = $pdo->query("SHOW TABLES LIKE 'clients'");
            $clientsExists = $stmt->rowCount() > 0;
            
            if (!$clientsExists) {
                // La table n'existe pas, retourner un tableau vide
                error_log("La table 'clients' n'existe pas dans la base de données.");
                return [];
            }
            
            // Requête pour récupérer les clients qui ont passé des commandes
            $query = "SELECT DISTINCT cl.id, cl.nom, cl.prenom
                     FROM clients cl
                     JOIN commandes c ON cl.id = c.client_id
                     ORDER BY cl.nom ASC, cl.prenom ASC";
            
            $stmt = $pdo->prepare($query);
            $stmt->execute();
            
            return $stmt->fetchAll(\PDO::FETCH_ASSOC);
        } catch (\Exception $e) {
            // Journaliser l'erreur
            error_log('Erreur lors de la récupération des clients: ' . $e->getMessage());
            return [];
        }
    }
    
    /**
     * Récupère les noms des produits pour une vente spécifique
     */
    private function getProductNamesBySaleId($saleId) {
        // Connexion à la base de données
        $pdo = $this->initPDO();
        
        // Requête pour récupérer les noms des produits
        $query = "SELECT p.nom
                 FROM lignes_commande lc
                 JOIN produits p ON lc.produit_id = p.id
                 WHERE lc.commande_id = :commande_id";
        
        $stmt = $pdo->prepare($query);
        $stmt->bindValue(':commande_id', $saleId, \PDO::PARAM_INT);
        $stmt->execute();
        
        $result = $stmt->fetchAll(\PDO::FETCH_ASSOC);
        $productNames = [];
        
        foreach ($result as $row) {
            $productNames[] = $row['nom'];
        }
        
        return $productNames;
    }
    
    /**
     * Récupère une vente par son ID
     */
    private function getSaleById($id) {
        // Connexion à la base de données
        $pdo = $this->initPDO();
        
        try {
            // Vérifier si la table commandes existe
            $stmt = $pdo->query("SHOW TABLES LIKE 'commandes'");
            $tableExists = $stmt->rowCount() > 0;
            
            if (!$tableExists) {
                // La table n'existe pas, retourner false
                error_log("La table 'commandes' n'existe pas dans la base de données.");
                return false;
            }
            
            // Vérifier les colonnes de la table commandes
            $stmt = $pdo->query("DESCRIBE commandes");
            $columns = $stmt->fetchAll(\PDO::FETCH_COLUMN);
            
            // Construire la requête SQL en fonction des colonnes disponibles
            $query = "SELECT c.*";
            
            // Vérifier si la table clients existe
            $stmt = $pdo->query("SHOW TABLES LIKE 'clients'");
            $clientsTableExists = $stmt->rowCount() > 0;
            
            if ($clientsTableExists) {
                // Vérifier les colonnes de la table clients
                $stmt = $pdo->query("DESCRIBE clients");
                $clientColumns = $stmt->fetchAll(\PDO::FETCH_COLUMN);
                
                $query .= ", cl.nom AS client_nom, cl.prenom AS client_prenom";
                
                if (in_array('email', $clientColumns)) {
                    $query .= ", cl.email AS client_email";
                } else {
                    $query .= ", NULL AS client_email";
                }
                
                if (in_array('telephone', $clientColumns)) {
                    $query .= ", cl.telephone AS client_telephone";
                } else {
                    $query .= ", NULL AS client_telephone";
                }
            } else {
                $query .= ", NULL AS client_nom, NULL AS client_prenom, NULL AS client_email, NULL AS client_telephone";
            }
            
            // Vérifier si la table users existe
            $stmt = $pdo->query("SHOW TABLES LIKE 'users'");
            $usersTableExists = $stmt->rowCount() > 0;
            
            if ($usersTableExists && in_array('user_id', $columns)) {
                // Vérifier les colonnes de la table users
                $stmt = $pdo->query("DESCRIBE users");
                $userColumns = $stmt->fetchAll(\PDO::FETCH_COLUMN);
                
                if (in_array('nom', $userColumns)) {
                    $query .= ", u.nom AS vendeur_nom";
                } else {
                    $query .= ", NULL AS vendeur_nom";
                }
            } else {
                $query .= ", NULL AS vendeur_nom";
            }
            
            $query .= "
                     FROM commandes c";
            
            if ($clientsTableExists) {
                $query .= "
                     LEFT JOIN clients cl ON c.client_id = cl.id";
            }
            
            if ($usersTableExists && in_array('user_id', $columns)) {
                $query .= "
                     LEFT JOIN users u ON c.user_id = u.id";
            }
            
            $query .= "
                     WHERE c.id = :id";
            
            $stmt = $pdo->prepare($query);
            $stmt->bindValue(':id', $id, \PDO::PARAM_INT);
            $stmt->execute();
            
            return $stmt->fetch(\PDO::FETCH_ASSOC);
        } catch (\Exception $e) {
            // Journaliser l'erreur
            error_log('Erreur lors de la récupération de la vente: ' . $e->getMessage());
            return false;
        }
    }
    
    /**
     * Récupère les détails d'une vente (produits, etc.)
     */
    private function getSaleDetails($id) {
        // Connexion à la base de données
        $pdo = $this->initPDO();
        
        try {
            // Vérifier si les tables nécessaires existent
            $stmt = $pdo->query("SHOW TABLES LIKE 'lignes_commande'");
            $lignesCommandeExists = $stmt->rowCount() > 0;
            
            $stmt = $pdo->query("SHOW TABLES LIKE 'produits'");
            $produitsExists = $stmt->rowCount() > 0;
            
            if (!$lignesCommandeExists || !$produitsExists) {
                // Une des tables n'existe pas, retourner un tableau vide
                error_log("Une des tables nécessaires n'existe pas dans la base de données.");
                return [];
            }
            
            // Vérifier les colonnes de la table produits
            $stmt = $pdo->query("DESCRIBE produits");
            $produitsColumns = $stmt->fetchAll(\PDO::FETCH_COLUMN);
            
            // Construire la requête SQL en fonction des colonnes disponibles
            $query = "SELECT lc.id, lc.produit_id, lc.quantite, lc.prix_unitaire, 
                            p.nom AS produit_nom";
            
            // Vérifier si les colonnes optionnelles existent
            if (in_array('est_ordonnance', $produitsColumns)) {
                $query .= ", p.est_ordonnance";
            } else {
                $query .= ", 0 AS est_ordonnance";
            }
            
            if (in_array('image', $produitsColumns)) {
                $query .= ", p.image";
            } else {
                $query .= ", '/bigpharma/public/images/products/imgDefault.jpg' AS image";
            }
            
            $query .= "
                     FROM lignes_commande lc
                     JOIN produits p ON lc.produit_id = p.id
                     WHERE lc.commande_id = :commande_id";
            
            $stmt = $pdo->prepare($query);
            $stmt->bindValue(':commande_id', $id, \PDO::PARAM_INT);
            $stmt->execute();
            
            $results = $stmt->fetchAll(\PDO::FETCH_ASSOC);
            
            // Si aucun résultat n'est trouvé, vérifier si la commande existe
            if (empty($results)) {
                $stmt = $pdo->prepare("SELECT id FROM commandes WHERE id = :id");
                $stmt->bindValue(':id', $id, \PDO::PARAM_INT);
                $stmt->execute();
                
                if ($stmt->rowCount() > 0) {
                    // La commande existe mais n'a pas de lignes de commande
                    return [];
                } else {
                    // La commande n'existe pas
                    error_log("La commande avec l'ID $id n'existe pas.");
                    return false;
                }
            }
            
            return $results;
        } catch (\Exception $e) {
            // Journaliser l'erreur
            error_log('Erreur lors de la récupération des détails de la vente: ' . $e->getMessage());
            return [];
        }
    }

    /**
     * Calcule les statistiques des ventes
     */
    private function calculateSalesStats($pharmacieId = null) {
        // Connexion à la base de données
        $pdo = $this->initPDO();
        
        // Statistiques à calculer
        $stats = [
            'total_ventes' => 0,
            'montant_total' => 0,
            'ventes_aujourd_hui' => 0,
            'montant_aujourd_hui' => 0,
            'ventes_ce_mois' => 0,
            'montant_ce_mois' => 0,
            'produits_populaires' => []
        ];
        
        // Condition de pharmacie
        $pharmacieCondition = $pharmacieId ? "WHERE pharmacie_id = :pharmacie_id" : "";
        $params = $pharmacieId ? [':pharmacie_id' => $pharmacieId] : [];
        
        // 1. Nombre total de ventes et montant total
        $query = "SELECT COUNT(*) AS total_ventes, SUM(montant_total) AS montant_total
                 FROM commandes
                 $pharmacieCondition";
        
        $stmt = $pdo->prepare($query);
        $stmt->execute($params);
        $result = $stmt->fetch(\PDO::FETCH_ASSOC);
        
        $stats['total_ventes'] = $result['total_ventes'] ?? 0;
        $stats['montant_total'] = $result['montant_total'] ?? 0;
        
        // 2. Ventes d'aujourd'hui
        $today = date('Y-m-d');
        $query = "SELECT COUNT(*) AS ventes_aujourd_hui, SUM(montant_total) AS montant_aujourd_hui
                 FROM commandes
                 WHERE DATE(date_commande) = :today";
        
        if ($pharmacieId) {
            $query .= " AND pharmacie_id = :pharmacie_id";
            $params[':pharmacie_id'] = $pharmacieId;
        }
        
        $stmt = $pdo->prepare($query);
        $stmt->bindValue(':today', $today);
        if ($pharmacieId) {
            $stmt->bindValue(':pharmacie_id', $pharmacieId);
        }
        $stmt->execute();
        $result = $stmt->fetch(\PDO::FETCH_ASSOC);
        
        $stats['ventes_aujourd_hui'] = $result['ventes_aujourd_hui'] ?? 0;
        $stats['montant_aujourd_hui'] = $result['montant_aujourd_hui'] ?? 0;
        
        // 3. Ventes du mois en cours
        $firstDayOfMonth = date('Y-m-01');
        $lastDayOfMonth = date('Y-m-t');
        
        $query = "SELECT COUNT(*) AS ventes_ce_mois, SUM(montant_total) AS montant_ce_mois
                 FROM commandes
                 WHERE date_commande BETWEEN :first_day AND :last_day";
        
        if ($pharmacieId) {
            $query .= " AND pharmacie_id = :pharmacie_id";
        }
        
        $stmt = $pdo->prepare($query);
        $stmt->bindValue(':first_day', $firstDayOfMonth . ' 00:00:00');
        $stmt->bindValue(':last_day', $lastDayOfMonth . ' 23:59:59');
        if ($pharmacieId) {
            $stmt->bindValue(':pharmacie_id', $pharmacieId);
        }
        $stmt->execute();
        $result = $stmt->fetch(\PDO::FETCH_ASSOC);
        
        $stats['ventes_ce_mois'] = $result['ventes_ce_mois'] ?? 0;
        $stats['montant_ce_mois'] = $result['montant_ce_mois'] ?? 0;
        
        // 4. Produits les plus vendus
        $query = "SELECT p.id, p.nom, SUM(lc.quantite) AS total_vendu, SUM(lc.quantite * lc.prix_unitaire) AS montant_total
                 FROM lignes_commande lc
                 JOIN produits p ON lc.produit_id = p.id
                 JOIN commandes c ON lc.commande_id = c.id";
        
        if ($pharmacieId) {
            $query .= " WHERE c.pharmacie_id = :pharmacie_id";
        }
        
        $query .= " GROUP BY p.id
                   ORDER BY total_vendu DESC
                   LIMIT 5";
        
        $stmt = $pdo->prepare($query);
        if ($pharmacieId) {
            $stmt->bindValue(':pharmacie_id', $pharmacieId);
        }
        $stmt->execute();
        $stats['produits_populaires'] = $stmt->fetchAll(\PDO::FETCH_ASSOC);
        
        return $stats;
    }

    /**
     * Supprime une vente par son ID
     */
    public function delete($id) {
        // Vérifier si l'utilisateur est connecté
        if (!isset($_SESSION['user_id'])) {
            $_SESSION['error'] = "Vous devez être connecté pour accéder à cette page.";
            header('Location: /bigpharma/login');
            exit;
        }
        
        // Connexion à la base de données
        $pdo = $this->initPDO();
        
        try {
            // Vérifier si les tables nécessaires existent
            $stmt = $pdo->query("SHOW TABLES LIKE 'commandes'");
            $commandesExists = $stmt->rowCount() > 0;
            
            $stmt = $pdo->query("SHOW TABLES LIKE 'lignes_commande'");
            $lignesCommandeExists = $stmt->rowCount() > 0;
            
            if (!$commandesExists) {
                $_SESSION['error'] = "Impossible de supprimer la vente. La table 'commandes' n'existe pas.";
                header('Location: /bigpharma/sales');
                exit;
            }
            
            // Démarrer une transaction
            $pdo->beginTransaction();
            
            // Vérifier si la vente existe
            $query = "SELECT id FROM commandes WHERE id = :id";
            $stmt = $pdo->prepare($query);
            $stmt->bindValue(':id', $id, \PDO::PARAM_INT);
            $stmt->execute();
            
            if ($stmt->rowCount() === 0) {
                // La vente n'existe pas
                $_SESSION['error'] = "La vente avec l'ID $id n'existe pas.";
                header('Location: /bigpharma/sales');
                exit;
            }
            
            // Supprimer d'abord les lignes de commande associées si la table existe
            if ($lignesCommandeExists) {
                $query = "DELETE FROM lignes_commande WHERE commande_id = :id";
                $stmt = $pdo->prepare($query);
                $stmt->bindValue(':id', $id, \PDO::PARAM_INT);
                $stmt->execute();
            }
            
            // Ensuite, supprimer la commande elle-même
            $query = "DELETE FROM commandes WHERE id = :id";
            $stmt = $pdo->prepare($query);
            $stmt->bindValue(':id', $id, \PDO::PARAM_INT);
            $stmt->execute();
            
            // Valider la transaction
            $pdo->commit();
            
            // Rediriger avec un message de succès
            $_SESSION['success'] = 'La vente a été supprimée avec succès.';
            header('Location: /bigpharma/sales');
            exit;
            
        } catch (\Exception $e) {
            // En cas d'erreur, annuler la transaction
            if (isset($pdo)) {
                $pdo->rollBack();
            }
            
            // Journaliser l'erreur
            error_log('Erreur lors de la suppression de la vente: ' . $e->getMessage());
            
            // Rediriger avec un message d'erreur
            $_SESSION['error'] = "Une erreur est survenue lors de la suppression de la vente.";
            header('Location: /bigpharma/sales');
            exit;
        }
    }
    
    /**
     * Génère un PDF pour imprimer les détails d'une vente
     */
    public function printSale($id) {
        // Vérifier si l'utilisateur est connecté
        if (!isset($_SESSION['user_id'])) {
            $_SESSION['error'] = "Vous devez être connecté pour accéder à cette page.";
            header('Location: /bigpharma/login');
            exit;
        }
        
        try {
            // Récupérer la vente par son ID
            $sale = $this->getSaleById($id);
            
            if (!$sale) {
                // Vente non trouvée
                $_SESSION['error'] = "La vente avec l'ID $id n'existe pas.";
                header('Location: /bigpharma/sales');
                exit;
            }
            
            // Récupérer les détails de la vente (produits, client, etc.)
            $saleDetails = $this->getSaleDetails($id);
            
            if ($saleDetails === false) {
                $_SESSION['error'] = "Impossible de récupérer les détails de la vente.";
                header('Location: /bigpharma/sales');
                exit;
            }
            
            // Générer le PDF (pour l'instant, nous affichons simplement une page de détails avec un message)
            $_SESSION['success'] = 'La fonctionnalité d\'impression sera bientôt disponible.';
            
            // Rediriger vers la page de détails
            header('Location: /bigpharma/sales/show/' . $id);
            exit;
            
        } catch (\Exception $e) {
            // Journaliser l'erreur
            error_log('Erreur lors de l\'impression de la vente: ' . $e->getMessage());
            
            // Rediriger avec un message d'erreur
            $_SESSION['error'] = "Une erreur est survenue lors de l'impression de la vente.";
            header('Location: /bigpharma/sales');
            exit;
        }
    }
    
    /**
     * Exporte les détails d'une vente au format CSV
     */
    public function exportSale($id) {
        // Vérifier si l'utilisateur est connecté
        if (!isset($_SESSION['user_id'])) {
            $_SESSION['error'] = "Vous devez être connecté pour accéder à cette page.";
            header('Location: /bigpharma/login');
            exit;
        }
        
        try {
            // Récupérer la vente par son ID
            $sale = $this->getSaleById($id);
            
            if (!$sale) {
                // Vente non trouvée
                $_SESSION['error'] = "La vente avec l'ID $id n'existe pas.";
                header('Location: /bigpharma/sales');
                exit;
            }
            
            // Récupérer les détails de la vente (produits, client, etc.)
            $saleDetails = $this->getSaleDetails($id);
            
            if ($saleDetails === false) {
                $_SESSION['error'] = "Impossible de récupérer les détails de la vente.";
                header('Location: /bigpharma/sales');
                exit;
            }
            
            // Pour l'instant, nous affichons simplement un message
            $_SESSION['success'] = 'La fonctionnalité d\'exportation sera bientôt disponible.';
            
            // Rediriger vers la page de détails
            header('Location: /bigpharma/sales/show/' . $id);
            exit;
            
        } catch (\Exception $e) {
            // Journaliser l'erreur
            error_log('Erreur lors de l\'exportation de la vente: ' . $e->getMessage());
            
            // Rediriger avec un message d'erreur
            $_SESSION['error'] = "Une erreur est survenue lors de l'exportation de la vente.";
            header('Location: /bigpharma/sales');
            exit;
        }
    }

    /**
     * Initialise la connexion PDO
     */
    private function initPDO() {
        $dbConfig = require_once __DIR__ . '/../../config/database.php';
        return $GLOBALS['pdo'] ?? null;
    }
}
