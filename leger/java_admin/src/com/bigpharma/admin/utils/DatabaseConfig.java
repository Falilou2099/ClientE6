package com.bigpharma.admin.utils;

/**
 * Configuration de la base de données pour l'application d'administration
 * Cette classe contient les paramètres de connexion à la base de données
 * qui sont partagés entre l'application web PHP et l'application Java
 */
public class DatabaseConfig {
    // Paramètres de connexion à la base de données
    public static final String DB_URL = "jdbc:mysql://localhost:3306/clientlegerlourd";
    public static final String DB_USER = "root";
    public static final String DB_PASSWORD = "";
    
    // Paramètres additionnels pour la connexion
    public static final String DB_DRIVER = "com.mysql.cj.jdbc.Driver";
    public static final String DB_PARAMS = "?useUnicode=true&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC&characterEncoding=UTF-8";
    
    // URL complète de connexion
    public static final String FULL_DB_URL = DB_URL + DB_PARAMS;
}
