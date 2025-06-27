package com.gestionpharma;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;
import java.util.UUID;
import com.gestionpharma.config.DatabaseConfigSimple;

/**
 * Dialogue pour la réinitialisation du mot de passe oublié
 */
public class MotDePasseOublieDialog extends JDialog {
    private JTextField emailField;
    private JButton envoyerButton;
    private JButton annulerButton;
    private boolean emailEnvoye = false;
    
    public MotDePasseOublieDialog(Frame parent) {
        super(parent, "Mot de passe oublié", true);
        initComponents();
        setupLayout();
        setupEventListeners();
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(400, 200);
        setLocationRelativeTo(parent);
    }
    
    private void initComponents() {
        emailField = new JTextField(20);
        envoyerButton = new JButton("Envoyer le lien de réinitialisation");
        annulerButton = new JButton("Annuler");
    }
    
    private void setupLayout() {
        setLayout(new BorderLayout());
        
        // Panel principal
        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        
        // Titre
        JLabel titleLabel = new JLabel("Réinitialisation du mot de passe");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        mainPanel.add(titleLabel, gbc);
        
        // Instructions
        JLabel instructionLabel = new JLabel("<html>Entrez votre adresse email pour recevoir<br>un lien de réinitialisation de mot de passe.</html>");
        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(10, 5, 15, 5);
        mainPanel.add(instructionLabel, gbc);
        
        // Email
        gbc.gridwidth = 1;
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.EAST;
        gbc.gridx = 0; gbc.gridy = 2;
        mainPanel.add(new JLabel("Email:"), gbc);
        
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 1; gbc.gridy = 2;
        mainPanel.add(emailField, gbc);
        
        add(mainPanel, BorderLayout.CENTER);
        
        // Panel des boutons
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(envoyerButton);
        buttonPanel.add(annulerButton);
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    private void setupEventListeners() {
        envoyerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                envoyerLienReinitialisation();
            }
        });
        
        annulerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });
        
        // Validation en temps réel
        emailField.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyReleased(java.awt.event.KeyEvent e) {
                validerEmail();
            }
        });
    }
    
    private void validerEmail() {
        String email = emailField.getText().trim();
        boolean emailValide = email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
        envoyerButton.setEnabled(emailValide);
    }
    
    private void envoyerLienReinitialisation() {
        String email = emailField.getText().trim();
        
        if (email.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Veuillez saisir votre adresse email.", 
                "Erreur", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // Vérifier si l'email existe dans la base de données
        if (!verifierEmailExiste(email)) {
            JOptionPane.showMessageDialog(this, 
                "Aucun compte n'est associé à cette adresse email.", 
                "Email non trouvé", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // Générer un token de réinitialisation
        String token = genererToken();
        
        // Sauvegarder le token en base
        if (sauvegarderToken(email, token)) {
            // Envoyer l'email (simulation)
            if (envoyerEmail(email, token)) {
                JOptionPane.showMessageDialog(this, 
                    "Un lien de réinitialisation a été envoyé à votre adresse email.\n" +
                    "Veuillez vérifier votre boîte de réception.", 
                    "Email envoyé", JOptionPane.INFORMATION_MESSAGE);
                emailEnvoye = true;
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, 
                    "Erreur lors de l'envoi de l'email. Veuillez réessayer plus tard.", 
                    "Erreur", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(this, 
                "Erreur lors de la génération du lien. Veuillez réessayer.", 
                "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private boolean verifierEmailExiste(String email) {
        String query = "SELECT COUNT(*) FROM utilisateurs WHERE email = ?";
        
        try (Connection conn = DatabaseConfigSimple.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setString(1, email);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
            
        } catch (SQLException e) {
            System.err.println("Erreur lors de la vérification de l'email: " + e.getMessage());
        }
        
        return false;
    }
    
    private String genererToken() {
        return UUID.randomUUID().toString();
    }
    
    private boolean sauvegarderToken(String email, String token) {
        // Créer la table password_resets si elle n'existe pas
        creerTablePasswordResets();
        
        // Supprimer les anciens tokens pour cet email
        String deleteQuery = "DELETE FROM password_resets WHERE email = ?";
        
        // Insérer le nouveau token
        String insertQuery = "INSERT INTO password_resets (email, token, created_at) VALUES (?, ?, NOW())";
        
        try (Connection conn = DatabaseConfigSimple.getConnection()) {
            // Supprimer les anciens tokens
            try (PreparedStatement deleteStmt = conn.prepareStatement(deleteQuery)) {
                deleteStmt.setString(1, email);
                deleteStmt.executeUpdate();
            }
            
            // Insérer le nouveau token
            try (PreparedStatement insertStmt = conn.prepareStatement(insertQuery)) {
                insertStmt.setString(1, email);
                insertStmt.setString(2, token);
                return insertStmt.executeUpdate() > 0;
            }
            
        } catch (SQLException e) {
            System.err.println("Erreur lors de la sauvegarde du token: " + e.getMessage());
            return false;
        }
    }
    
    private void creerTablePasswordResets() {
        String createTableQuery = """
            CREATE TABLE IF NOT EXISTS password_resets (
                id INT AUTO_INCREMENT PRIMARY KEY,
                email VARCHAR(255) NOT NULL,
                token VARCHAR(255) NOT NULL,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                INDEX idx_email (email),
                INDEX idx_token (token)
            )
        """;
        
        try (Connection conn = DatabaseConfigSimple.getConnection();
             Statement stmt = conn.createStatement()) {
            
            stmt.executeUpdate(createTableQuery);
            
        } catch (SQLException e) {
            System.err.println("Erreur lors de la création de la table password_resets: " + e.getMessage());
        }
    }
    
    private boolean envoyerEmail(String email, String token) {
        // Pour cette démonstration, on simule l'envoi d'email
        // Dans un vrai projet, vous utiliseriez JavaMail API
        
        System.out.println("=== SIMULATION ENVOI EMAIL ===");
        System.out.println("À: " + email);
        System.out.println("Sujet: Réinitialisation de votre mot de passe");
        System.out.println("Corps:");
        System.out.println("Bonjour,");
        System.out.println("");
        System.out.println("Vous avez demandé la réinitialisation de votre mot de passe.");
        System.out.println("Cliquez sur le lien suivant pour créer un nouveau mot de passe:");
        System.out.println("");
        System.out.println("http://localhost/bigpharma/reset-password?token=" + token);
        System.out.println("");
        System.out.println("Ce lien expirera dans 1 heure.");
        System.out.println("");
        System.out.println("Si vous n'avez pas demandé cette réinitialisation, ignorez ce message.");
        System.out.println("");
        System.out.println("Cordialement,");
        System.out.println("L'équipe BigPharma");
        System.out.println("===============================");
        
        // Simulation réussie
        return true;
    }
    
    public boolean estEmailEnvoye() {
        return emailEnvoye;
    }
}
