package com.gestionpharma;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import com.gestionpharma.config.DatabaseConfigSimple;

/**
 * Dialogue de connexion avec gestion des tentatives échouées et mot de passe oublié
 */
public class ConnexionDialog extends JDialog {
    private JTextField emailField;
    private JPasswordField passwordField;
    private JButton connexionButton;
    private JButton annulerButton;
    private JLabel motDePasseOublieLabel;
    private boolean connexionReussie = false;
    private int userId = -1;
    private int pharmacieId = -1;
    private String nomUtilisateur = "";
    
    // Constantes pour la gestion des tentatives
    private static final int MAX_TENTATIVES = 5;
    private static final int DUREE_SUSPENSION_MINUTES = 30;
    
    public ConnexionDialog(Frame parent) {
        super(parent, "Connexion", true);
        initComponents();
        setupLayout();
        setupEventListeners();
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(400, 300);
        setLocationRelativeTo(parent);
    }
    
    private void initComponents() {
        emailField = new JTextField(20);
        passwordField = new JPasswordField(20);
        connexionButton = new JButton("Se connecter");
        annulerButton = new JButton("Annuler");
        motDePasseOublieLabel = new JLabel("<html><u>Mot de passe oublié ?</u></html>");
        motDePasseOublieLabel.setForeground(Color.BLUE);
        motDePasseOublieLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }
    
    private void setupLayout() {
        setLayout(new BorderLayout());
        
        // Panel principal
        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        
        // Titre
        JLabel titleLabel = new JLabel("Connexion à BigPharma");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(5, 5, 20, 5);
        mainPanel.add(titleLabel, gbc);
        
        // Email
        gbc.gridwidth = 1;
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.EAST;
        gbc.gridx = 0; gbc.gridy = 1;
        mainPanel.add(new JLabel("Email:"), gbc);
        
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 1; gbc.gridy = 1;
        mainPanel.add(emailField, gbc);
        
        // Mot de passe
        gbc.anchor = GridBagConstraints.EAST;
        gbc.fill = GridBagConstraints.NONE;
        gbc.gridx = 0; gbc.gridy = 2;
        mainPanel.add(new JLabel("Mot de passe:"), gbc);
        
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 1; gbc.gridy = 2;
        mainPanel.add(passwordField, gbc);
        
        // Lien mot de passe oublié
        gbc.gridx = 1; gbc.gridy = 3;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.NONE;
        gbc.insets = new Insets(5, 5, 15, 5);
        mainPanel.add(motDePasseOublieLabel, gbc);
        
        add(mainPanel, BorderLayout.CENTER);
        
        // Panel des boutons
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(connexionButton);
        buttonPanel.add(annulerButton);
        add(buttonPanel, BorderLayout.SOUTH);
        
        // Définir le bouton par défaut
        getRootPane().setDefaultButton(connexionButton);
    }
    
    private void setupEventListeners() {
        connexionButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                tenterConnexion();
            }
        });
        
        annulerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });
        
        motDePasseOublieLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                ouvrirMotDePasseOublie();
            }
        });
        
        // Validation en temps réel
        emailField.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyReleased(java.awt.event.KeyEvent e) {
                validerChamps();
            }
        });
        
        passwordField.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyReleased(java.awt.event.KeyEvent e) {
                validerChamps();
            }
        });
    }
    
    private void validerChamps() {
        String email = emailField.getText().trim();
        String password = new String(passwordField.getPassword());
        
        boolean champsValides = !email.isEmpty() && !password.isEmpty() && 
                               email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
        
        connexionButton.setEnabled(champsValides);
    }
    
    private void tenterConnexion() {
        String email = emailField.getText().trim();
        String password = new String(passwordField.getPassword());
        
        if (email.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Veuillez remplir tous les champs.", 
                "Erreur", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // Vérifier si le compte est suspendu
        if (estCompteSuspendu(email)) {
            JOptionPane.showMessageDialog(this, 
                "Votre compte est temporairement suspendu suite à trop de tentatives de connexion échouées.\n" +
                "Veuillez réessayer dans " + DUREE_SUSPENSION_MINUTES + " minutes ou utiliser 'Mot de passe oublié'.", 
                "Compte suspendu", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // Tenter la connexion
        if (verifierConnexion(email, password)) {
            // Connexion réussie - réinitialiser les tentatives échouées
            reinitialiserTentativesEchouees(email);
            connexionReussie = true;
            dispose();
        } else {
            // Connexion échouée - enregistrer la tentative
            enregistrerTentativeEchouee(email);
            
            int tentativesRestantes = getTentativesRestantes(email);
            if (tentativesRestantes <= 0) {
                suspendreCompte(email);
                JOptionPane.showMessageDialog(this, 
                    "Trop de tentatives de connexion échouées.\n" +
                    "Votre compte est suspendu pour " + DUREE_SUSPENSION_MINUTES + " minutes.", 
                    "Compte suspendu", JOptionPane.ERROR_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, 
                    "Email ou mot de passe incorrect.\n" +
                    "Tentatives restantes: " + tentativesRestantes, 
                    "Erreur de connexion", JOptionPane.ERROR_MESSAGE);
            }
        }
        
        // Effacer le mot de passe pour la sécurité
        passwordField.setText("");
    }
    
    private boolean verifierConnexion(String email, String password) {
        String query = "SELECT id, nom, prenom, mot_de_passe, pharmacie_id FROM utilisateurs WHERE email = ?";
        
        try (Connection conn = DatabaseConfigSimple.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setString(1, email);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                String motDePasseHash = rs.getString("mot_de_passe");
                
                // Vérifier le mot de passe (supposons qu'il soit hashé avec SHA-256)
                if (verifierMotDePasse(password, motDePasseHash)) {
                    userId = rs.getInt("id");
                    pharmacieId = rs.getInt("pharmacie_id");
                    nomUtilisateur = rs.getString("prenom") + " " + rs.getString("nom");
                    return true;
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Erreur lors de la vérification de connexion: " + e.getMessage());
        }
        
        return false;
    }
    
    private boolean verifierMotDePasse(String motDePasse, String hash) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hashedPassword = md.digest(motDePasse.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : hashedPassword) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString().equals(hash);
        } catch (NoSuchAlgorithmException e) {
            System.err.println("Erreur de hashage: " + e.getMessage());
            return false;
        }
    }
    
    private boolean estCompteSuspendu(String email) {
        String query = "SELECT COUNT(*) FROM login_attempts WHERE email = ? AND " +
                      "attempt_time > DATE_SUB(NOW(), INTERVAL ? MINUTE) AND " +
                      "success = FALSE";
        
        try (Connection conn = DatabaseConfigSimple.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setString(1, email);
            pstmt.setInt(2, DUREE_SUSPENSION_MINUTES);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt(1) >= MAX_TENTATIVES;
            }
            
        } catch (SQLException e) {
            System.err.println("Erreur lors de la vérification de suspension: " + e.getMessage());
        }
        
        return false;
    }
    
    private void enregistrerTentativeEchouee(String email) {
        creerTableLoginAttempts();
        
        String query = "INSERT INTO login_attempts (email, attempt_time, success, ip_address) VALUES (?, NOW(), FALSE, ?)";
        
        try (Connection conn = DatabaseConfigSimple.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setString(1, email);
            pstmt.setString(2, "127.0.0.1"); // IP locale pour l'application Java
            pstmt.executeUpdate();
            
        } catch (SQLException e) {
            System.err.println("Erreur lors de l'enregistrement de la tentative: " + e.getMessage());
        }
    }
    
    private void reinitialiserTentativesEchouees(String email) {
        String query = "INSERT INTO login_attempts (email, attempt_time, success, ip_address) VALUES (?, NOW(), TRUE, ?)";
        
        try (Connection conn = DatabaseConfigSimple.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setString(1, email);
            pstmt.setString(2, "127.0.0.1");
            pstmt.executeUpdate();
            
        } catch (SQLException e) {
            System.err.println("Erreur lors de la réinitialisation des tentatives: " + e.getMessage());
        }
    }
    
    private int getTentativesRestantes(String email) {
        String query = "SELECT COUNT(*) FROM login_attempts WHERE email = ? AND " +
                      "attempt_time > DATE_SUB(NOW(), INTERVAL ? MINUTE) AND " +
                      "success = FALSE";
        
        try (Connection conn = DatabaseConfigSimple.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setString(1, email);
            pstmt.setInt(2, DUREE_SUSPENSION_MINUTES);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                int tentativesEchouees = rs.getInt(1);
                return Math.max(0, MAX_TENTATIVES - tentativesEchouees);
            }
            
        } catch (SQLException e) {
            System.err.println("Erreur lors du calcul des tentatives restantes: " + e.getMessage());
        }
        
        return MAX_TENTATIVES;
    }
    
    private void suspendreCompte(String email) {
        // La suspension est gérée automatiquement par la vérification des tentatives récentes
        System.out.println("Compte suspendu pour " + email + " pendant " + DUREE_SUSPENSION_MINUTES + " minutes");
    }
    
    private void creerTableLoginAttempts() {
        String createTableQuery = """
            CREATE TABLE IF NOT EXISTS login_attempts (
                id INT AUTO_INCREMENT PRIMARY KEY,
                email VARCHAR(255) NOT NULL,
                attempt_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                success BOOLEAN NOT NULL DEFAULT FALSE,
                ip_address VARCHAR(45),
                INDEX idx_email_time (email, attempt_time),
                INDEX idx_success (success)
            )
        """;
        
        try (Connection conn = DatabaseConfigSimple.getConnection();
             Statement stmt = conn.createStatement()) {
            
            stmt.executeUpdate(createTableQuery);
            
        } catch (SQLException e) {
            System.err.println("Erreur lors de la création de la table login_attempts: " + e.getMessage());
        }
    }
    
    private void ouvrirMotDePasseOublie() {
        MotDePasseOublieDialog dialog = new MotDePasseOublieDialog((Frame) getOwner());
        dialog.setVisible(true);
        
        if (dialog.estEmailEnvoye()) {
            JOptionPane.showMessageDialog(this, 
                "Un email de réinitialisation a été envoyé.\n" +
                "Veuillez vérifier votre boîte de réception.", 
                "Email envoyé", JOptionPane.INFORMATION_MESSAGE);
        }
    }
    
    // Getters
    public boolean estConnexionReussie() {
        return connexionReussie;
    }
    
    public int getUserId() {
        return userId;
    }
    
    public int getPharmacieId() {
        return pharmacieId;
    }
    
    public String getNomUtilisateur() {
        return nomUtilisateur;
    }
}
