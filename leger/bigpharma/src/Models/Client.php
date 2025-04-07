<?php
namespace App\Models;

class Client {
    private $db;

    public function __construct() {
        global $pdo;
        $this->db = $pdo;
    }

    public function getClientsByPharmacie($pharmacie_id) {
        $sql = "SELECT * FROM clients WHERE pharmacie_id = ? ORDER BY nom, prenom";
        $stmt = $this->db->prepare($sql);
        $stmt->execute([$pharmacie_id]);
        return $stmt->fetchAll();
    }

    public function getClientById($id) {
        $sql = "SELECT * FROM clients WHERE id = ?";
        $stmt = $this->db->prepare($sql);
        $stmt->execute([$id]);
        return $stmt->fetch();
    }

    public function creerClient($data) {
        $sql = "INSERT INTO clients (pharmacie_id, nom, prenom, email, telephone) 
                VALUES (?, ?, ?, ?, ?)";
        $stmt = $this->db->prepare($sql);
        return $stmt->execute([
            $data['pharmacie_id'],
            $data['nom'],
            $data['prenom'],
            $data['email'],
            $data['telephone']
        ]);
    }

    public function modifierClient($data) {
        $sql = "UPDATE clients 
                SET nom = ?, prenom = ?, email = ?, telephone = ? 
                WHERE id = ?";
        $stmt = $this->db->prepare($sql);
        return $stmt->execute([
            $data['nom'],
            $data['prenom'],
            $data['email'],
            $data['telephone'],
            $data['id']
        ]);
    }

    public function supprimerClient($id) {
        $sql = "DELETE FROM clients WHERE id = ?";
        $stmt = $this->db->prepare($sql);
        return $stmt->execute([$id]);
    }

    public function getHistoriqueAchats($client_id) {
        $sql = "SELECT v.id as vente_id, v.date_vente, v.total,
                       dv.quantite, dv.prix_unitaire,
                       p.nom as nom_produit
                FROM ventes v
                JOIN details_ventes dv ON v.id = dv.vente_id
                JOIN produits p ON dv.produit_id = p.id
                WHERE v.client_id = ?
                ORDER BY v.date_vente DESC";
        
        $stmt = $this->db->prepare($sql);
        $stmt->execute([$client_id]);
        return $stmt->fetchAll();
    }

    public function getStatistiquesClient($client_id) {
        $sql = "SELECT 
                    COUNT(DISTINCT v.id) as nombre_achats,
                    SUM(v.total) as total_achats,
                    AVG(v.total) as moyenne_achats,
                    MAX(v.date_vente) as dernier_achat
                FROM ventes v
                WHERE v.client_id = ?";
        
        $stmt = $this->db->prepare($sql);
        $stmt->execute([$client_id]);
        return $stmt->fetch();
    }
}
