<?php
namespace App\Controllers;

use App\Models\Vente;
use App\Models\Client;
use App\Models\Produit;
use App\Models\Stock;

class VenteController {
    private $venteModel;
    private $clientModel;
    private $produitModel;
    private $stockModel;

    public function __construct() {
        $this->venteModel = new Vente();
        $this->clientModel = new Client();
        $this->produitModel = new Produit();
        $this->stockModel = new Stock();
    }

    public function index() {
        // Vérifier si l'utilisateur est connecté
        if (!isset($_SESSION['pharmacie_id'])) {
            header('Location: /bigpharma/login');
            exit;
        }

        $pharmacie_id = $_SESSION['pharmacie_id'];
        $ventes = $this->venteModel->getVentesByPharmacie($pharmacie_id);
        
        require_once SRC_PATH . '/Views/ventes/index.php';
    }

    public function nouvelle() {
        if (!isset($_SESSION['pharmacie_id'])) {
            header('Location: /bigpharma/login');
            exit;
        }

        $pharmacie_id = $_SESSION['pharmacie_id'];
        $clients = $this->clientModel->getClientsByPharmacie($pharmacie_id);
        $produits = $this->stockModel->getProduitsEnStock($pharmacie_id);
        
        require_once SRC_PATH . '/Views/ventes/nouvelle.php';
    }

    public function enregistrer() {
        if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
            header('Location: /bigpharma/ventes/nouvelle');
            exit;
        }

        $pharmacie_id = $_SESSION['pharmacie_id'];
        $client_id = $_POST['client_id'];
        $produits = $_POST['produits']; // Array of product IDs
        $quantites = $_POST['quantites']; // Array of quantities
        $total = 0;

        try {
            // Début de la transaction
            $this->venteModel->beginTransaction();

            // 1. Créer la vente
            $vente_id = $this->venteModel->creerVente([
                'pharmacie_id' => $pharmacie_id,
                'client_id' => $client_id,
                'total' => 0 // Sera mis à jour après
            ]);

            // 2. Ajouter les produits à la vente
            foreach ($produits as $index => $produit_id) {
                $quantite = $quantites[$index];
                $produit = $this->produitModel->getProduitById($produit_id);
                
                // Vérifier le stock
                $stock = $this->stockModel->getStock($pharmacie_id, $produit_id);
                if ($stock['quantite'] < $quantite) {
                    throw new \Exception("Stock insuffisant pour " . $produit['nom']);
                }

                // Ajouter le détail de la vente
                $this->venteModel->ajouterDetailVente([
                    'vente_id' => $vente_id,
                    'produit_id' => $produit_id,
                    'quantite' => $quantite,
                    'prix_unitaire' => $produit['prix_unitaire']
                ]);

                // Mettre à jour le stock
                $this->stockModel->diminuerStock($pharmacie_id, $produit_id, $quantite);

                // Calculer le total
                $total += $quantite * $produit['prix_unitaire'];
            }

            // 3. Mettre à jour le total de la vente
            $this->venteModel->mettreAJourTotal($vente_id, $total);

            // Valider la transaction
            $this->venteModel->commit();

            $_SESSION['success'] = "Vente enregistrée avec succès";
            header('Location: /bigpharma/ventes');
            exit;

        } catch (\Exception $e) {
            $this->venteModel->rollback();
            $_SESSION['error'] = $e->getMessage();
            header('Location: /bigpharma/ventes/nouvelle');
            exit;
        }
    }

    public function details($id) {
        if (!isset($_SESSION['pharmacie_id'])) {
            header('Location: /bigpharma/login');
            exit;
        }

        $vente = $this->venteModel->getVenteById($id);
        if (!$vente || $vente['pharmacie_id'] !== $_SESSION['pharmacie_id']) {
            header('Location: /bigpharma/ventes');
            exit;
        }

        $details = $this->venteModel->getDetailsVente($id);
        $client = $this->clientModel->getClientById($vente['client_id']);
        
        require_once SRC_PATH . '/Views/ventes/details.php';
    }
}
