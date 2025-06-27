package com.bigpharma.admin.models;

import java.math.BigDecimal;
import java.util.Date;

/**
 * Modèle représentant un produit pharmaceutique
 * Cette classe correspond à la table 'produits' dans la base de données
 */
public class Product {
    private Integer id;
    private String nom;
    private String description;
    private BigDecimal prix;
    private Integer quantiteStock;
    private String categorie;
    private Integer categorieId;
    private Integer fournisseurId;
    private Boolean estOrdonnance;
    private String image;
    private Date dateAjout;
    private Date dateModification;
    private Integer pharmacyId;
    
    // Constructeur par défaut
    public Product() {
    }
    
    // Constructeur complet
    public Product(Integer id, String nom, String description, BigDecimal prix, Integer quantiteStock,
                  String categorie, Integer categorieId, Integer fournisseurId, Boolean estOrdonnance,
                  String image, Date dateAjout, Date dateModification, Integer pharmacyId) {
        this.id = id;
        this.nom = nom;
        this.description = description;
        this.prix = prix;
        this.quantiteStock = quantiteStock;
        this.categorie = categorie;
        this.categorieId = categorieId;
        this.fournisseurId = fournisseurId;
        this.estOrdonnance = estOrdonnance;
        this.image = image;
        this.dateAjout = dateAjout;
        this.dateModification = dateModification;
        this.pharmacyId = pharmacyId;
    }
    
    // Getters et Setters
    public Integer getId() {
        return id;
    }
    
    public void setId(Integer id) {
        this.id = id;
    }
    
    public String getNom() {
        return nom;
    }
    
    public void setNom(String nom) {
        this.nom = nom;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public BigDecimal getPrix() {
        return prix;
    }
    
    public void setPrix(BigDecimal prix) {
        this.prix = prix;
    }
    
    public Integer getQuantiteStock() {
        return quantiteStock;
    }
    
    public void setQuantiteStock(Integer quantiteStock) {
        this.quantiteStock = quantiteStock;
    }
    
    public String getCategorie() {
        return categorie;
    }
    
    public void setCategorie(String categorie) {
        this.categorie = categorie;
    }
    
    public Integer getCategorieId() {
        return categorieId;
    }
    
    public void setCategorieId(Integer categorieId) {
        this.categorieId = categorieId;
    }
    
    public Integer getFournisseurId() {
        return fournisseurId;
    }
    
    public void setFournisseurId(Integer fournisseurId) {
        this.fournisseurId = fournisseurId;
    }
    
    public Boolean getEstOrdonnance() {
        return estOrdonnance;
    }
    
    public void setEstOrdonnance(Boolean estOrdonnance) {
        this.estOrdonnance = estOrdonnance;
    }
    
    public String getImage() {
        return image;
    }
    
    public void setImage(String image) {
        this.image = image;
    }
    
    public Date getDateAjout() {
        return dateAjout;
    }
    
    public void setDateAjout(Date dateAjout) {
        this.dateAjout = dateAjout;
    }
    
    public Date getDateModification() {
        return dateModification;
    }
    
    public void setDateModification(Date dateModification) {
        this.dateModification = dateModification;
    }
    
    public Integer getPharmacyId() {
        return pharmacyId;
    }
    
    public void setPharmacyId(Integer pharmacyId) {
        this.pharmacyId = pharmacyId;
    }
    
    /**
     * Obtient l'URL de l'image du produit
     * @return L'URL complète de l'image ou l'URL de l'image par défaut si aucune image n'est définie
     */
    public String getImageUrl() {
        if (image == null || image.isEmpty()) {
            return "/bigpharma/public/images/products/imgDefault.jpg";
        }
        
        // Vérifier si l'image est une URL complète
        if (image.startsWith("http://") || image.startsWith("https://")) {
            return image;
        }
        
        // Sinon, c'est un fichier local
        return "/bigpharma/public/images/products/" + image;
    }
    
    @Override
    public String toString() {
        return "Product{" +
                "id=" + id +
                ", nom='" + nom + '\'' +
                ", prix=" + prix +
                ", quantiteStock=" + quantiteStock +
                ", categorie='" + categorie + '\'' +
                ", estOrdonnance=" + estOrdonnance +
                '}';
    }
}
