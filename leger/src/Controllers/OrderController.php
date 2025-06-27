<?php
namespace Controllers;

use Models\Order;
use Models\Product;
use Config\SecurityService;

class OrderController extends BaseController {
    private $order;

    public function __construct($pharmacyId = null, $userId = null) {
        parent::__construct();
        $this->order = new Order($pharmacyId, $userId);
    }

    // Ajouter un produit à la commande
    public function addProductToOrder(Product $product, $quantity) {
        $data = $this->addPharmacyIdToData($data);
        try {
            $this->order->addProduct($product, $quantity);
            return [
                'success' => true,
                'message' => 'Produit ajouté à la commande',
                'order' => $this->order
            ];
        } catch (\Exception $e) {
            return [
                'success' => false,
                'error' => $e->getMessage()
            ];
        }
    }

    // Supprimer un produit de la commande
    public function removeProductFromOrder(Product $product) {
        $this->order->removeProduct($product);
        return [
            'success' => true,
            'message' => 'Produit retiré de la commande',
            'order' => $this->order
        ];
    }

    // Valider la commande
    public function validateOrder() {
        $errors = $this->order->validate();
        
        if (empty($errors)) {
            return [
                'success' => true,
                'message' => 'Commande valide',
                'order' => $this->order
            ];
        }

        return [
            'success' => false,
            'errors' => $errors
        ];
    }

    // Finaliser la commande
    public function processOrder() {
        try {
            // Vérifier si une ordonnance est requise
            if ($this->order->isPrescriptionRequired()) {
                return [
                    'success' => false,
                    'error' => 'Une ordonnance est requise pour certains produits'
                ];
            }

            // Finaliser la commande
            $this->order->finalize();

            return [
                'success' => true,
                'message' => 'Commande traitée avec succès',
                'order' => $this->order
            ];
        } catch (\Exception $e) {
            return [
                'success' => false,
                'error' => $e->getMessage()
            ];
        }
    }

    // Annuler la commande
    public function cancelOrder() {
        try {
            $this->order->cancel();
            return [
                'success' => true,
                'message' => 'Commande annulée avec succès'
            ];
        } catch (\Exception $e) {
            return [
                'success' => false,
                'error' => $e->getMessage()
            ];
        }
    }

    // Calculer le montant total de la commande
    public function calculateTotalAmount() {
        return $this->order->getTotalAmount();
    }

    // Vérifier si une ordonnance est requise
    public function checkPrescriptionRequirement() {
        return $this->order->isPrescriptionRequired();
    }

    // Obtenir les détails de la commande
    public function getOrderDetails() {
        return [
            'id' => $this->order->getId(),
            'pharmacyId' => $this->order->getPharmacyId(),
            'userId' => $this->order->getUserId(),
            'orderItems' => $this->order->getOrderItems(),
            'totalAmount' => $this->order->getTotalAmount(),
            'status' => $this->order->getStatus(),
            'orderDate' => $this->order->getOrderDate(),
            'deliveryDate' => $this->order->getDeliveryDate(),
            'prescriptionRequired' => $this->order->isPrescriptionRequired()
        ];
    }
}
