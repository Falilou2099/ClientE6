/**
 * Script spécifique pour corriger le menu déroulant utilisateur sur toutes les pages
 * Ce script garantit que le menu fonctionne correctement, que ce soit sur la page d'accueil ou ailleurs
 */
document.addEventListener('DOMContentLoaded', function() {
    console.log('Menu fix script loaded');
    
    // Fonction pour initialiser le menu déroulant
    function initializeDropdowns() {
        // Trouver le bouton du menu utilisateur
        var userDropdownButton = document.getElementById('userDropdown');
        var userDropdownMenu = document.getElementById('userDropdownMenu');
        
        if (!userDropdownButton || !userDropdownMenu) {
            console.log('Menu utilisateur non trouvé sur cette page');
            return;
        }
        
        console.log('Menu utilisateur trouvé, initialisation...');
        
        // Supprimer les gestionnaires d'événements existants pour éviter les conflits
        userDropdownButton.removeEventListener('click', toggleUserMenu);
        
        // Ajouter notre gestionnaire d'événements personnalisé
        userDropdownButton.addEventListener('click', function(e) {
            e.preventDefault();
            e.stopPropagation();
            toggleUserMenu();
        });
        
        // Fermer le menu lors d'un clic à l'extérieur
        document.addEventListener('click', function(e) {
            if (userDropdownMenu.classList.contains('show') && 
                !userDropdownButton.contains(e.target) && 
                !userDropdownMenu.contains(e.target)) {
                userDropdownMenu.classList.remove('show');
            }
        });
        
        console.log('Menu utilisateur initialisé avec succès');
    }
    
    // Fonction pour basculer l'affichage du menu
    window.toggleUserMenu = function() {
        var menu = document.getElementById('userDropdownMenu');
        if (!menu) return;
        
        if (menu.classList.contains('show')) {
            menu.classList.remove('show');
        } else {
            menu.classList.add('show');
        }
    };
    
    // Initialiser le menu après un court délai pour s'assurer que tout est chargé
    setTimeout(initializeDropdowns, 100);
    
    // Réinitialiser le menu si le DOM change (pour les applications SPA)
    var observer = new MutationObserver(function(mutations) {
        for (var mutation of mutations) {
            if (mutation.type === 'childList') {
                initializeDropdowns();
                break;
            }
        }
    });
    
    // Observer les changements dans le body
    observer.observe(document.body, { childList: true, subtree: true });
});
