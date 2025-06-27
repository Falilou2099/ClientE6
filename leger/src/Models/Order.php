<?php
namespace Models;

use Config\SecurityService;

class Order {
    private $id;
    private $pharmacyId;
    private $userId;
    private $orderItems;
    private $totalAmount;
    private $status;
    private $orderDate;
    private $deliveryDate;
    private $prescriptionRequired;

    // Constructeur
    public function __construct($pharmacyId = null, $userId = null) {
        $this->pharmacyId = $pharmacyId;
        $this->userId = $userId;
        $this->orderItems = [];
        $this->totalAmount = 0;
        $this->status = 'pending';
        $this->orderDate = new \DateTime();
        $this->prescriptionRequired = false;
    }

    // Ajouter un produit à la commande
    public function addProduct(Product $product, $quantity) {
        // Vérifier si le produit nécessite une ordonnance
        if ($product->isPrescrition()) {
            $this->prescriptionRequired = true;
        }

        // Vérifier la disponibilité du stock
        if ($quantity > $product->getStock()) {
            throw new \Exception("Quantité demandée supérieure au stock disponible");
        }

        // Calculer le prix total
        $itemTotal = $product->getPrice() * $quantity;
        $this->totalAmount += $itemTotal;

        // Ajouter le produit aux articles de la commande
        $this->orderItems[] = [
            'product' => $product,
            'quantity' => $quantity,
            'itemTotal' => $itemTotal
        ];
    }

    // Supprimer un produit de la commande
    public function removeProduct(Product $product) {
        foreach ($this->orderItems as $key => $item) {
            if ($item['product']->getId() === $product->getId()) {
                // Soustraire le prix de l'article du total
                $this->totalAmount -= $item['itemTotal'];
                
                // Supprimer l'article
                unset($this->orderItems[$key]);
                
                // Réindexer le tableau
                $this->orderItems = array_values($this->orderItems);
                
                break;
            }
        }
    }

    // Valider la commande
    public function validate() {
        $errors = [];

        // Vérifier que la commande n'est pas vide
        if (empty($this->orderItems)) {
            $errors[] = "La commande est vide";
        }

        // Vérifier le montant total
        if ($this->totalAmount <= 0) {
            $errors[] = "Le montant total de la commande est invalide";
        }

        // Vérifier les informations de la pharmacie et de l'utilisateur
        if (!$this->pharmacyId) {
            $errors[] = "Aucune pharmacie sélectionnée";
        }

        if (!$this->userId) {
            $errors[] = "Aucun utilisateur connecté";
        }

        return $errors;
    }

    // Finaliser la commande
    public function finalize() {
        // Vérifier la validité de la commande
        $validationErrors = $this->validate();
        if (!empty($validationErrors)) {
            throw new \Exception(implode(", ", $validationErrors));
        }

        // Mettre à jour le statut
        $this->status = 'processed';
        $this->deliveryDate = (new \DateTime())->modify('+2 days');

        // Réduire le stock des produits
        foreach ($this->orderItems as $item) {
            $product = $item['product'];
            $quantity = $item['quantity'];
            
            try {
                $product->removeStock($quantity);
            } catch (\Exception $e) {
                // Gérer les erreurs de stock
                throw new \Exception("Impossible de mettre à jour le stock : " . $e->getMessage());
            }
        }

        return true;
    }

    // Annuler la commande
    public function cancel() {
        // Vérifier si la commande peut être annulée
        if ($this->status === 'processed') {
            throw new \Exception("Impossible d'annuler une commande déjà traitée");
        }

        $this->status = 'cancelled';
        
        // Optionnel : restaurer le stock des produits
        foreach ($this->orderItems as $item) {
            $product = $item['product'];
            $quantity = $item['quantity'];
            
            try {
                $product->addStock($quantity);
            } catch (\Exception $e) {
                // Log de l'erreur sans bloquer l'annulation
                error_log("Erreur lors de la restauration du stock : " . $e->getMessage());
            }
        }

        return true;
    }

    // Getters
    public function getId() { return $this->id; }
    public function getPharmacyId() { return $this->pharmacyId; }
    public function getUserId() { return $this->userId; }
    public function getOrderItems() { return $this->orderItems; }
    public function getTotalAmount() { return $this->totalAmount; }
    public function getStatus() { return $this->status; }
    public function getOrderDate() { return $this->orderDate; }
    public function getDeliveryDate() { return $this->deliveryDate; }
    public function isPrescriptionRequired() { return $this->prescriptionRequired; }
}
