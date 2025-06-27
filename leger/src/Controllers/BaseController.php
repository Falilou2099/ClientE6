<?php
namespace Controllers;

class BaseController {
    protected $currentUser;
    protected $pharmacyId;
    
    public function __construct() {
        // Initialiser les informations de l'utilisateur connecté
        $this->initCurrentUser();
    }
    
    protected function initCurrentUser() {
        if (session_status() === PHP_SESSION_NONE) {
            session_start();
        }
        
        $this->currentUser = isset($_SESSION['user_id']) ? $_SESSION['user_id'] : null;
        $this->pharmacyId = isset($_SESSION['pharmacy_id']) ? $_SESSION['pharmacy_id'] : null;
    }
    
    /**
     * Filtre les données en fonction de la pharmacie de l'utilisateur connecté
     * @param array $data Tableau de données à filtrer
     * @param string $pharmacyIdField Nom du champ contenant l'ID de la pharmacie
     * @return array Données filtrées
     */
    protected function filterDataByPharmacy($data, $pharmacyIdField = 'pharmacy_id') {
        // Si l'utilisateur n'est pas connecté ou n'a pas de pharmacie associée, retourner un tableau vide
        if (!$this->currentUser || !$this->pharmacyId) {
            return [];
        }
        
        // Si l'utilisateur a le rôle 'admin', ne pas filtrer les données
        if (isset($_SESSION['user_role']) && $_SESSION['user_role'] === 'admin') {
            return $data;
        }
        
        // Filtrer les données pour ne garder que celles de la pharmacie de l'utilisateur
        return array_filter($data, function($item) use ($pharmacyIdField) {
            // Si l'élément est un objet
            if (is_object($item)) {
                $method = 'get' . ucfirst($pharmacyIdField);
                if (method_exists($item, $method)) {
                    return $item->$method() == $this->pharmacyId;
                }
                return isset($item->$pharmacyIdField) && $item->$pharmacyIdField == $this->pharmacyId;
            }
            
            // Si l'élément est un tableau
            if (is_array($item)) {
                return isset($item[$pharmacyIdField]) && $item[$pharmacyIdField] == $this->pharmacyId;
            }
            
            return false;
        });
    }
    
    /**
     * Vérifie si l'utilisateur a accès à une ressource spécifique
     * @param int $resourcePharmacyId ID de la pharmacie associée à la ressource
     * @return bool True si l'utilisateur a accès, false sinon
     */
    protected function hasAccessToResource($resourcePharmacyId) {
        // Si l'utilisateur n'est pas connecté, il n'a pas accès
        if (!$this->currentUser) {
            return false;
        }
        
        // Si l'utilisateur est admin, il a accès à toutes les ressources
        if (isset($_SESSION['user_role']) && $_SESSION['user_role'] === 'admin') {
            return true;
        }
        
        // Sinon, vérifier si la ressource appartient à la pharmacie de l'utilisateur
        return $this->pharmacyId && $resourcePharmacyId == $this->pharmacyId;
    }
    
    /**
     * Ajoute l'ID de la pharmacie aux données avant de les sauvegarder
     * @param array|object $data Données à modifier
     * @return array|object Données avec l'ID de la pharmacie ajouté
     */
    protected function addPharmacyIdToData($data) {
        if (!$this->pharmacyId) {
            return $data;
        }
        
        if (is_object($data)) {
            $method = 'setPharmacyId';
            if (method_exists($data, $method)) {
                $data->$method($this->pharmacyId);
            } else {
                $data->pharmacy_id = $this->pharmacyId;
            }
        } elseif (is_array($data)) {
            $data['pharmacy_id'] = $this->pharmacyId;
        }
        
        return $data;
    }
}