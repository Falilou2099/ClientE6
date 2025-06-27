<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <title>Panier - BigPharma</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.2.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <style>
        body { background-color: #f4f6f9; }
        .cart-container {
            background-color: white;
            border-radius: 10px;
            box-shadow: 0 4px 6px rgba(0,0,0,0.1);
            padding: 30px;
        }
        .cart-item {
            border-bottom: 1px solid #e9ecef;
            padding: 15px 0;
        }
        .cart-item:last-child {
            border-bottom: none;
        }
    </style>
</head>
<body>
    <?php require_once 'C:\xampp\htdocs\bigpharma/templates/header.php'; ?>

    <div class="container mt-4">
        <div class="cart-container">
            <h1 class="mb-4">Votre Panier</h1>

            <?php if(empty($cartItems)): ?>
                <div class="alert alert-info text-center" role="alert">
                    Votre panier est vide. <a href="/products" class="alert-link">Commencez vos achats</a>
                </div>
            <?php else: ?>
                <div class="row">
                    <div class="col-md-8">
                        <?php foreach($cartItems as $item): ?>
                            <div class="cart-item d-flex justify-content-between align-items-center">
                                <div class="d-flex align-items-center">
                                    <img 
                                        src="<?= htmlspecialchars($item->getProduct()->getImageUrl() ?? '/assets/img/default-product.png') ?>" 
                                        alt="<?= htmlspecialchars($item->getProduct()->getName()) ?>" 
                                        style="width: 80px; height: 80px; object-fit: cover; margin-right: 15px;"
                                    >
                                    <div>
                                        <h5 class="mb-1"><?= htmlspecialchars($item->getProduct()->getName()) ?></h5>
                                        <p class="text-muted mb-0">
                                            <?= htmlspecialchars($item->getProduct()->getDosage()) ?> 
                                            - <?= htmlspecialchars($item->getProduct()->getManufacturer()) ?>
                                        </p>
                                    </div>
                                </div>
                                <div class="d-flex align-items-center">
                                    <div class="input-group" style="width: 130px;">
                                        <button 
                                            class="btn btn-outline-secondary decrease-quantity" 
                                            type="button" 
                                            data-product-id="<?= $item->getProduct()->getId() ?>"
                                        >-</button>
                                        <input 
                                            type="text" 
                                            class="form-control text-center quantity-input" 
                                            value="<?= $item->getQuantity() ?>" 
                                            readonly
                                        >
                                        <button 
                                            class="btn btn-outline-secondary increase-quantity" 
                                            type="button" 
                                            data-product-id="<?= $item->getProduct()->getId() ?>"
                                        >+</button>
                                    </div>
                                    <div class="ms-3">
                                        <strong><?= number_format($item->getProduct()->getPrice() * $item->getQuantity(), 2) ?> €</strong>
                                    </div>
                                    <button 
                                        class="btn btn-danger ms-3 remove-item" 
                                        data-product-id="<?= $item->getProduct()->getId() ?>"
                                    >
                                        <i class="bi bi-trash"></i>
                                    </button>
                                </div>
                            </div>
                        <?php endforeach; ?>
                    </div>
                    <div class="col-md-4">
                        <div class="card">
                            <div class="card-body">
                                <h4 class="card-title">Résumé de la Commande</h4>
                                <div class="d-flex justify-content-between mb-2">
                                    <span>Sous-total</span>
                                    <strong><?= number_format($subtotal, 2) ?> €</strong>
                                </div>
                                <div class="d-flex justify-content-between mb-2">
                                    <span>TVA (20%)</span>
                                    <strong><?= number_format($tax, 2) ?> €</strong>
                                </div>
                                <hr>
                                <div class="d-flex justify-content-between mb-3">
                                    <h5>Total</h5>
                                    <h5><?= number_format($total, 2) ?> €</h5>
                                </div>

                                <?php if($hasPrescriptionItems): ?>
                                    <div class="alert alert-warning" role="alert">
                                        <i class="bi bi-prescription"></i> 
                                        Certains produits nécessitent une ordonnance
                                    </div>
                                <?php endif; ?>

                                <button 
                                    class="btn btn-primary w-100 proceed-to-checkout" 
                                    <?= $hasPrescriptionItems ? 'disabled' : '' ?>
                                >
                                    <?= $hasPrescriptionItems 
                                        ? 'Ordonnance requise' 
                                        : 'Procéder au paiement' 
                                    ?>
                                </button>
                            </div>
                        </div>
                    </div>
                </div>
            <?php endif; ?>
        </div>
    </div>

    <?php require_once 'C:\xampp\htdocs\bigpharma/templates/footer.php'; ?>

    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.2.3/dist/js/bootstrap.bundle.min.js"></script>
    <script>
        document.addEventListener('DOMContentLoaded', function() {
            // Gestion de la quantité
            document.querySelectorAll('.increase-quantity, .decrease-quantity').forEach(button => {
                button.addEventListener('click', function() {
                    const productId = this.getAttribute('data-product-id');
                    const quantityInput = this.closest('.input-group').querySelector('.quantity-input');
                    let currentQuantity = parseInt(quantityInput.value);

                    if (this.classList.contains('increase-quantity')) {
                        currentQuantity++;
                    } else {
                        currentQuantity = Math.max(1, currentQuantity - 1);
                    }

                    quantityInput.value = currentQuantity;

                    // TODO: Mettre à jour la quantité côté serveur
                    updateCartQuantity(productId, currentQuantity);
                });
            });

            // Suppression d'un article
            document.querySelectorAll('.remove-item').forEach(button => {
                button.addEventListener('click', function() {
                    const productId = this.getAttribute('data-product-id');
                    const cartItem = this.closest('.cart-item');

                    // TODO: Supprimer l'article du panier côté serveur
                    removeFromCart(productId);
                    cartItem.remove();
                });
            });

            // Paiement
            document.querySelector('.proceed-to-checkout')?.addEventListener('click', function() {
                // TODO: Rediriger vers la page de paiement
                window.location.href = '/checkout';
            });

            // Fonctions utilitaires (à implémenter côté serveur)
            function updateCartQuantity(productId, quantity) {
                console.log(`Mise à jour quantité pour le produit ${productId} : ${quantity}`);
                // Appel AJAX pour mettre à jour la quantité
            }

            function removeFromCart(productId) {
                console.log(`Suppression du produit ${productId} du panier`);
                // Appel AJAX pour supprimer l'article
            }
        });
    </script>
</body>
</html>
