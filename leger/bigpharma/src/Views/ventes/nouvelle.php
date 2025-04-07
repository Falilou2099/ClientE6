<?php require_once TEMPLATES_PATH . '/header.php'; ?>

<div class="container mt-4">
    <div class="d-flex justify-content-between align-items-center mb-4">
        <h1>Nouvelle vente</h1>
        <a href="/bigpharma/ventes" class="btn btn-secondary">
            <i class="fas fa-arrow-left"></i> Retour aux ventes
        </a>
    </div>

    <?php if (isset($_SESSION['error'])): ?>
        <div class="alert alert-danger">
            <?= htmlspecialchars($_SESSION['error']) ?>
            <?php unset($_SESSION['error']); ?>
        </div>
    <?php endif; ?>

    <div class="card">
        <div class="card-body">
            <form action="/bigpharma/ventes/enregistrer" method="POST" id="venteForm">
                <div class="row mb-4">
                    <div class="col-md-6">
                        <label for="client_id" class="form-label">Client</label>
                        <select class="form-select" name="client_id" id="client_id" required>
                            <option value="">Sélectionner un client</option>
                            <?php foreach ($clients as $client): ?>
                                <option value="<?= $client['id'] ?>">
                                    <?= htmlspecialchars($client['nom'] . ' ' . $client['prenom']) ?>
                                </option>
                            <?php endforeach; ?>
                        </select>
                    </div>
                </div>

                <div id="produits-container">
                    <h4 class="mb-3">Produits</h4>
                    <div class="produit-ligne mb-3">
                        <div class="row">
                            <div class="col-md-6">
                                <label class="form-label">Produit</label>
                                <select class="form-select produit-select" name="produits[]" required>
                                    <option value="">Sélectionner un produit</option>
                                    <?php foreach ($produits as $produit): ?>
                                        <option value="<?= $produit['id'] ?>" 
                                                data-prix="<?= $produit['prix_unitaire'] ?>"
                                                data-stock="<?= $produit['quantite'] ?>">
                                            <?= htmlspecialchars($produit['nom']) ?> 
                                            (Stock: <?= $produit['quantite'] ?>) - 
                                            <?= number_format($produit['prix_unitaire'], 2, ',', ' ') ?> €
                                        </option>
                                    <?php endforeach; ?>
                                </select>
                            </div>
                            <div class="col-md-4">
                                <label class="form-label">Quantité</label>
                                <input type="number" class="form-control quantite-input" 
                                       name="quantites[]" min="1" required>
                            </div>
                            <div class="col-md-2 d-flex align-items-end">
                                <button type="button" class="btn btn-danger remove-produit mb-3" style="display: none;">
                                    <i class="fas fa-trash"></i>
                                </button>
                            </div>
                        </div>
                    </div>
                </div>

                <button type="button" id="ajouter-produit" class="btn btn-secondary mb-4">
                    <i class="fas fa-plus"></i> Ajouter un produit
                </button>

                <div class="row mb-4">
                    <div class="col-md-6">
                        <div class="card">
                            <div class="card-body">
                                <h5 class="card-title">Total</h5>
                                <h3 id="total-vente">0,00 €</h3>
                            </div>
                        </div>
                    </div>
                </div>

                <button type="submit" class="btn btn-primary">
                    <i class="fas fa-save"></i> Enregistrer la vente
                </button>
            </form>
        </div>
    </div>
</div>

<script>
document.addEventListener('DOMContentLoaded', function() {
    const produitsContainer = document.getElementById('produits-container');
    const ajouterProduitBtn = document.getElementById('ajouter-produit');
    const premiereLigne = produitsContainer.querySelector('.produit-ligne');
    
    // Fonction pour mettre à jour le total
    function updateTotal() {
        let total = 0;
        const lignes = document.querySelectorAll('.produit-ligne');
        
        lignes.forEach(ligne => {
            const select = ligne.querySelector('.produit-select');
            const quantite = ligne.querySelector('.quantite-input');
            
            if (select.value && quantite.value) {
                const prix = parseFloat(select.options[select.selectedIndex].dataset.prix);
                total += prix * parseInt(quantite.value);
            }
        });
        
        document.getElementById('total-vente').textContent = total.toFixed(2).replace('.', ',') + ' €';
    }

    // Fonction pour vérifier le stock
    function checkStock(ligne) {
        const select = ligne.querySelector('.produit-select');
        const quantite = ligne.querySelector('.quantite-input');
        
        if (select.value && quantite.value) {
            const stock = parseInt(select.options[select.selectedIndex].dataset.stock);
            const qte = parseInt(quantite.value);
            
            if (qte > stock) {
                alert('Stock insuffisant !');
                quantite.value = stock;
            }
        }
    }

    // Ajouter les écouteurs d'événements à la première ligne
    premiereLigne.querySelector('.produit-select').addEventListener('change', () => updateTotal());
    premiereLigne.querySelector('.quantite-input').addEventListener('change', () => {
        checkStock(premiereLigne);
        updateTotal();
    });

    // Fonction pour ajouter une nouvelle ligne de produit
    ajouterProduitBtn.addEventListener('click', function() {
        const nouvelleLigne = premiereLigne.cloneNode(true);
        
        // Réinitialiser les valeurs
        nouvelleLigne.querySelector('.produit-select').value = '';
        nouvelleLigne.querySelector('.quantite-input').value = '';
        
        // Afficher le bouton de suppression
        nouvelleLigne.querySelector('.remove-produit').style.display = 'block';
        
        // Ajouter les écouteurs d'événements
        nouvelleLigne.querySelector('.produit-select').addEventListener('change', () => updateTotal());
        nouvelleLigne.querySelector('.quantite-input').addEventListener('change', () => {
            checkStock(nouvelleLigne);
            updateTotal();
        });
        nouvelleLigne.querySelector('.remove-produit').addEventListener('click', function() {
            nouvelleLigne.remove();
            updateTotal();
        });
        
        produitsContainer.appendChild(nouvelleLigne);
    });

    // Validation du formulaire
    document.getElementById('venteForm').addEventListener('submit', function(e) {
        const lignes = document.querySelectorAll('.produit-ligne');
        let valid = false;
        
        lignes.forEach(ligne => {
            const select = ligne.querySelector('.produit-select');
            const quantite = ligne.querySelector('.quantite-input');
            
            if (select.value && quantite.value) {
                valid = true;
            }
        });
        
        if (!valid) {
            e.preventDefault();
            alert('Veuillez ajouter au moins un produit à la vente');
        }
    });
});
</script>

<?php require_once TEMPLATES_PATH . '/footer.php'; ?>
