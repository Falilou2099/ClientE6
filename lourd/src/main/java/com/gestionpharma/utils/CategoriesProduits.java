package com.gestionpharma.utils;

import java.util.Arrays;
import java.util.List;

/**
 * Classe utilitaire qui fournit une liste complète de catégories de produits pharmaceutiques
 * Cette classe est utilisée à la fois par l'application Java et PHP pour assurer la cohérence
 * des catégories entre les deux applications.
 */
public class CategoriesProduits {
    
    /**
     * Liste complète des catégories de produits pharmaceutiques
     * @return Une liste de chaînes contenant toutes les catégories
     */
    public static List<String> obtenirCategories() {
        return Arrays.asList(
            // Médicaments
            "Analgésiques et antipyrétiques",
            "Anti-inflammatoires non stéroïdiens (AINS)",
            "Antibiotiques",
            "Antihistaminiques",
            "Antidépresseurs",
            "Antipsychotiques",
            "Anxiolytiques",
            "Anticoagulants",
            "Antihypertenseurs",
            "Antidiabétiques",
            "Antiviraux",
            "Antifongiques",
            "Antiparasitaires",
            "Bronchodilatateurs",
            "Corticostéroïdes",
            "Diurétiques",
            "Hypnotiques",
            "Immunosuppresseurs",
            "Laxatifs",
            "Myorelaxants",
            "Antiémétiques",
            "Antiacides et antiulcéreux",
            "Anticonvulsivants",
            "Hypolipidémiants",
            "Hormones thyroïdiennes",
            "Contraceptifs hormonaux",
            "Médicaments cardiovasculaires",
            "Médicaments dermatologiques",
            "Médicaments ophtalmiques",
            "Médicaments ORL",
            
            // Produits de santé et bien-être
            "Vitamines et minéraux",
            "Compléments alimentaires",
            "Produits homéopathiques",
            "Phytothérapie",
            "Produits d'aromathérapie",
            "Produits de nutrition sportive",
            "Produits minceur",
            "Produits de sevrage tabagique",
            
            // Hygiène et soins
            "Produits d'hygiène corporelle",
            "Produits d'hygiène bucco-dentaire",
            "Produits d'hygiène intime",
            "Soins capillaires",
            "Soins de la peau",
            "Soins des pieds",
            "Produits pour bébés",
            "Produits pour femmes enceintes",
            "Produits pour personnes âgées",
            
            // Matériel médical et orthopédique
            "Pansements et bandages",
            "Matériel d'injection",
            "Matériel de diagnostic",
            "Matériel orthopédique",
            "Aides à la mobilité",
            "Matériel d'incontinence",
            "Matériel de premiers secours",
            
            // Autres
            "Produits vétérinaires",
            "Produits saisonniers",
            "Produits naturels",
            "Dispositifs médicaux",
            "Autres"
        );
    }
    
    /**
     * Obtient un tableau de chaînes contenant toutes les catégories
     * @return Un tableau de chaînes contenant toutes les catégories
     */
    public static String[] obtenirCategoriesTableau() {
        return obtenirCategories().toArray(new String[0]);
    }
}
