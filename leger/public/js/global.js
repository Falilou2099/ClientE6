/**
 * Script global pour l'application BigPharma
 * Ce script initialise les fonctionnalités Bootstrap et gère les menus déroulants
 */
document.addEventListener('DOMContentLoaded', function() {
    console.log('BigPharma global script loaded');
    
    // Initialiser tous les tooltips
    var tooltipTriggerList = [].slice.call(document.querySelectorAll('[data-bs-toggle="tooltip"]'));
    tooltipTriggerList.map(function(tooltipTriggerEl) {
        return new bootstrap.Tooltip(tooltipTriggerEl);
    });
    
    // Initialiser tous les popovers
    var popoverTriggerList = [].slice.call(document.querySelectorAll('[data-bs-toggle="popover"]'));
    popoverTriggerList.map(function(popoverTriggerEl) {
        return new bootstrap.Popover(popoverTriggerEl);
    });
    
    // Gestion de la popup du profil utilisateur
    // Vérifier si nous sommes sur la page de profil
    if (window.location.pathname.includes('/profile')) {
        // Attendre que tous les éléments soient chargés
        setTimeout(function() {
            // Vérifier si la modal du profil existe
            const profileModal = document.getElementById('profileModal');
            if (profileModal) {
                // Créer et afficher la modal
                const modal = new bootstrap.Modal(profileModal);
                modal.show();
                
                // Ajouter un événement au bouton d'affichage de la modal
                const showProfileModalBtn = document.getElementById('showProfileModal');
                if (showProfileModalBtn) {
                    showProfileModalBtn.addEventListener('click', function() {
                        modal.show();
                    });
                }
            }
        }, 500); // Délai court pour s'assurer que la page est complètement chargée
    }
    
    // Initialiser tous les dropdowns manuellement
    var dropdownElementList = document.querySelectorAll('.dropdown-toggle');
    console.log('Found ' + dropdownElementList.length + ' dropdown elements');
    
    dropdownElementList.forEach(function(element) {
        // Créer une nouvelle instance de dropdown pour chaque élément
        var dropdown = new bootstrap.Dropdown(element);
        
        // Ajouter un gestionnaire d'événements pour le clic
        element.addEventListener('click', function(e) {
            e.preventDefault();
            e.stopPropagation();
            console.log('Dropdown clicked:', element.id);
            dropdown.toggle();
        });
    });
    
    // Ajouter un gestionnaire d'événements global pour les clics sur les éléments dropdown
    document.addEventListener('click', function(e) {
        if (e.target.classList.contains('dropdown-toggle') || e.target.closest('.dropdown-toggle')) {
            console.log('Dropdown toggle clicked');
            var toggleElement = e.target.classList.contains('dropdown-toggle') ? e.target : e.target.closest('.dropdown-toggle');
            var dropdownInstance = bootstrap.Dropdown.getInstance(toggleElement);
            
            if (!dropdownInstance) {
                console.log('Creating new dropdown instance');
                dropdownInstance = new bootstrap.Dropdown(toggleElement);
            }
            
            dropdownInstance.toggle();
            e.stopPropagation();
        }
    });
    
    console.log('Bootstrap components initialized');
});
