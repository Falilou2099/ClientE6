package com.bigpharma.admin.utils;

import javafx.scene.image.Image;

import java.io.InputStream;
import java.net.URL;

/**
 * Classe utilitaire pour gérer les ressources de l'application
 */
public class ResourceUtils {
    
    /**
     * Charge une image depuis les ressources
     * @param path Le chemin de l'image dans les ressources
     * @return L'image chargée ou null si l'image n'a pas pu être chargée
     */
    public static Image loadImage(String path) {
        try {
            InputStream is = ResourceUtils.class.getResourceAsStream(path);
            if (is != null) {
                return new Image(is);
            }
            return null;
        } catch (Exception e) {
            System.err.println("Impossible de charger l'image: " + path);
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Charge une image depuis une URL ou un chemin local
     * @param urlOrPath L'URL ou le chemin local de l'image
     * @return L'image chargée ou null si l'image n'a pas pu être chargée
     */
    public static Image loadImageFromUrlOrPath(String urlOrPath) {
        try {
            // Vérifier si c'est une URL ou un chemin local
            if (urlOrPath == null || urlOrPath.isEmpty()) {
                return loadDefaultProductImage();
            }
            
            if (urlOrPath.startsWith("http://") || urlOrPath.startsWith("https://")) {
                // URL distante
                return new Image(urlOrPath, true);
            } else {
                // Chemin local
                try {
                    return new Image("file:" + urlOrPath);
                } catch (Exception e) {
                    // Essayer comme ressource
                    return loadImage(urlOrPath);
                }
            }
        } catch (Exception e) {
            System.err.println("Impossible de charger l'image: " + urlOrPath);
            e.printStackTrace();
            return loadDefaultProductImage();
        }
    }
    
    /**
     * Charge l'image par défaut pour les produits
     * @return L'image par défaut
     */
    public static Image loadDefaultProductImage() {
        return loadImage("/resources/images/product_default.png");
    }
    
    /**
     * Obtient l'URL d'une ressource
     * @param path Le chemin de la ressource
     * @return L'URL de la ressource ou null si la ressource n'a pas pu être trouvée
     */
    public static URL getResourceUrl(String path) {
        return ResourceUtils.class.getResource(path);
    }
    
    /**
     * Obtient le chemin externe d'une ressource CSS
     * @param path Le chemin de la ressource CSS
     * @return Le chemin externe de la ressource CSS
     */
    public static String getCssExternalForm(String path) {
        URL url = getResourceUrl(path);
        if (url != null) {
            return url.toExternalForm();
        }
        return null;
    }
}
