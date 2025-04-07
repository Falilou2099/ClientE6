<?php
namespace App\Controllers;

use App\Models\Client;

class ClientController {
    private $clientModel;

    public function __construct() {
        $this->clientModel = new Client();
    }

    public function index() {
        if (!isset($_SESSION['pharmacie_id'])) {
            header('Location: /bigpharma/login');
            exit;
        }

        $pharmacie_id = $_SESSION['pharmacie_id'];
        $clients = $this->clientModel->getClientsByPharmacie($pharmacie_id);
        
        require_once SRC_PATH . '/Views/clients/index.php';
    }

    public function ajouter() {
        if (!isset($_SESSION['pharmacie_id'])) {
            header('Location: /bigpharma/login');
            exit;
        }

        if ($_SERVER['REQUEST_METHOD'] === 'POST') {
            $data = [
                'pharmacie_id' => $_SESSION['pharmacie_id'],
                'nom' => $_POST['nom'],
                'prenom' => $_POST['prenom'],
                'email' => $_POST['email'],
                'telephone' => $_POST['telephone']
            ];

            try {
                $this->clientModel->creerClient($data);
                $_SESSION['success'] = "Client ajouté avec succès";
                header('Location: /bigpharma/clients');
                exit;
            } catch (\PDOException $e) {
                if ($e->getCode() == 23000) { // Code d'erreur pour duplicate entry
                    $_SESSION['error'] = "Un client avec cet email existe déjà";
                } else {
                    $_SESSION['error'] = "Erreur lors de l'ajout du client";
                }
            }
        }

        require_once SRC_PATH . '/Views/clients/ajouter.php';
    }

    public function modifier($id) {
        if (!isset($_SESSION['pharmacie_id'])) {
            header('Location: /bigpharma/login');
            exit;
        }

        $client = $this->clientModel->getClientById($id);
        if (!$client || $client['pharmacie_id'] !== $_SESSION['pharmacie_id']) {
            header('Location: /bigpharma/clients');
            exit;
        }

        if ($_SERVER['REQUEST_METHOD'] === 'POST') {
            $data = [
                'id' => $id,
                'nom' => $_POST['nom'],
                'prenom' => $_POST['prenom'],
                'email' => $_POST['email'],
                'telephone' => $_POST['telephone']
            ];

            try {
                $this->clientModel->modifierClient($data);
                $_SESSION['success'] = "Client modifié avec succès";
                header('Location: /bigpharma/clients');
                exit;
            } catch (\PDOException $e) {
                if ($e->getCode() == 23000) {
                    $_SESSION['error'] = "Un client avec cet email existe déjà";
                } else {
                    $_SESSION['error'] = "Erreur lors de la modification du client";
                }
            }
        }

        require_once SRC_PATH . '/Views/clients/modifier.php';
    }

    public function supprimer($id) {
        if (!isset($_SESSION['pharmacie_id'])) {
            header('Location: /bigpharma/login');
            exit;
        }

        $client = $this->clientModel->getClientById($id);
        if (!$client || $client['pharmacie_id'] !== $_SESSION['pharmacie_id']) {
            header('Location: /bigpharma/clients');
            exit;
        }

        try {
            $this->clientModel->supprimerClient($id);
            $_SESSION['success'] = "Client supprimé avec succès";
        } catch (\PDOException $e) {
            $_SESSION['error'] = "Impossible de supprimer ce client car il a des ventes associées";
        }

        header('Location: /bigpharma/clients');
        exit;
    }

    public function details($id) {
        if (!isset($_SESSION['pharmacie_id'])) {
            header('Location: /bigpharma/login');
            exit;
        }

        $client = $this->clientModel->getClientById($id);
        if (!$client || $client['pharmacie_id'] !== $_SESSION['pharmacie_id']) {
            header('Location: /bigpharma/clients');
            exit;
        }

        $historique_achats = $this->clientModel->getHistoriqueAchats($id);
        
        require_once SRC_PATH . '/Views/clients/details.php';
    }
}
