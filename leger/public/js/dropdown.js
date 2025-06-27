/**
 * Script personnalisé pour garantir que les menus déroulants Bootstrap fonctionnent correctement
 */
document.addEventListener('DOMContentLoaded', function() {
    // Initialiser tous les dropdowns Bootstrap
    var dropdownElementList = [].slice.call(document.querySelectorAll('.dropdown-toggle'));
    var dropdownList = dropdownElementList.map(function(dropdownToggleEl) {
        return new bootstrap.Dropdown(dropdownToggleEl);
    });
    
    // Ajouter un gestionnaire d'événements pour les clics sur les boutons dropdown
    document.querySelectorAll('.dropdown-toggle').forEach(function(element) {
        element.addEventListener('click', function(e) {
            e.stopPropagation();
            var dropdown = bootstrap.Dropdown.getInstance(element);
            if (!dropdown) {
                dropdown = new bootstrap.Dropdown(element);
            }
            dropdown.toggle();
        });
    });
    
    console.log('Dropdowns initialized successfully');
});