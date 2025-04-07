<?php
namespace App\Models;

class Vente {
    private $db;

    public function __construct() {
        global $pdo;
        $this->db = $pdo;
    }

    public function beginTransaction() {
        return $this->db->beginTransaction();
    }

    public function commit() {
        return $this->db->commit();
    }

    public function rollback() {
        return $this->db->rollBack();
    }

    public function getVentesByPharmacie($pharmacie_id) {
        $sql = "SELECT v.*, c.nom as nom_client, c.prenom as prenom_client 
                FROM ventes v 
                JOIN clients c ON v.client_id = c.id 
                WHERE v.pharmacie_id = ? 
                ORDER BY v.date_vente DESC";
        
        $stmt = $this->db->prepare($sql);
        $stmt->execute([$pharmacie_id]);
        return $stmt->fetchAll();
    }

    public function getVenteById($id) {
        $sql = "SELECT * FROM ventes WHERE id = ?";
        $stmt = $this->db->prepare($sql);
        $stmt->execute([$id]);
        return $stmt->fetch();
    }

    public function creerVente($data) {
        $sql = "INSERT INTO ventes (pharmacie_id, client_id, total) VALUES (?, ?, ?)";
        $stmt = $this->db->prepare($sql);
        $stmt->execute([
            $data['pharmacie_id'],
            $data['client_id'],
            $data['total']
        ]);
        return $this->db->lastInsertId();
    }

    public function ajouterDetailVente($data) {
        $sql = "INSERT INTO details_ventes (vente_id, produit_id, quantite, prix_unitaire) 
                VALUES (?, ?, ?, ?)";
        $stmt = $this->db->prepare($sql);
        return $stmt->execute([
            $data['vente_id'],
            $data['produit_id'],
            $data['quantite'],
            $data['prix_unitaire']
        ]);
    }

    public function mettreAJourTotal($vente_id, $total) {
        $sql = "UPDATE ventes SET total = ? WHERE id = ?";
        $stmt = $this->db->prepare($sql);
        return $stmt->execute([$total, $vente_id]);
    }

    public function getDetailsVente($vente_id) {
        $sql = "SELECT dv.*, p.nom as nom_produit 
                FROM details_ventes dv 
                JOIN produits p ON dv.produit_id = p.id 
                WHERE dv.vente_id = ?";
        $stmt = $this->db->prepare($sql);
        $stmt->execute([$vente_id]);
        return $stmt->fetchAll();
    }

    public function getVentesStats($pharmacie_id) {
        $sql = "SELECT 
                    COUNT(*) as nombre_ventes,
                    SUM(total) as total_ventes,
                    AVG(total) as moyenne_ventes
                FROM ventes 
                WHERE pharmacie_id = ? 
                AND date_vente >= DATE_SUB(NOW(), INTERVAL 30 DAY)";
        
        $stmt = $this->db->prepare($sql);
        $stmt->execute([$pharmacie_id]);
        return $stmt->fetch();
    }
}
