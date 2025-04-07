package com.gestionpharma;

/**
 * Classe Launcher qui sert de point d'entrée pour l'application JavaFX
 * lorsqu'elle est empaquetée dans un JAR exécutable.
 * Cette classe est nécessaire car JavaFX a besoin d'une configuration spéciale
 * lorsqu'il est empaqueté dans un JAR.
 */
public class Launcher {
    
    /**
     * Point d'entrée principal de l'application
     * @param args arguments de ligne de commande
     */
    public static void main(String[] args) {
        Main.main(args);
    }
}
