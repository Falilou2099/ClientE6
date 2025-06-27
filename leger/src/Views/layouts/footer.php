    <!-- Pied de page -->
    <footer class="bg-light py-4 mt-5">
        <div class="container">
            <div class="row">
                <div class="col-md-6">
                    <h5><i class="bi bi-capsule me-2"></i>BigPharma</h5>
                    <p class="text-muted">
                        Gestion de produits pharmaceutiques pour les pharmacies.
                    </p>
                </div>
                <div class="col-md-3">
                    <h5>Liens rapides</h5>
                    <ul class="list-unstyled">
                        <li><a href="/bigpharma" class="text-decoration-none">Accueil</a></li>
                        <li><a href="/bigpharma/products" class="text-decoration-none">Produits</a></li>
                        <li><a href="/bigpharma/clients" class="text-decoration-none">Clients</a></li>
                        <li><a href="/bigpharma/orders" class="text-decoration-none">Ventes</a></li>
                    </ul>
                </div>
                <div class="col-md-3">
                    <h5>Support</h5>
                    <ul class="list-unstyled">
                        <li><a href="#" class="text-decoration-none">Aide</a></li>
                        <li><a href="#" class="text-decoration-none">Contact</a></li>
                        <li><a href="#" class="text-decoration-none">Mentions légales</a></li>
                    </ul>
                </div>
            </div>
            <hr>
            <div class="text-center">
                <p class="mb-0">&copy; <?= date('Y') ?> BigPharma. Tous droits réservés.</p>
            </div>
        </div>
    </footer>

    <!-- Bootstrap JS -->
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0-alpha1/dist/js/bootstrap.bundle.min.js"></script>
    
    <!-- Script pour la gestion des clients -->
    <?php if (isset($_GET['url']) && strpos($_GET['url'], 'clients') === 0): ?>
    <script src="/bigpharma/public/js/clients.js"></script>
    <?php endif; ?>
</body>
</html>
