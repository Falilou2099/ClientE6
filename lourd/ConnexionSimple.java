import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.*;
import java.time.LocalDateTime;

/**
 * Dialogue de connexion simplifié avec gestion des tentatives et mot de passe oublié
 */
public class ConnexionSimple extends JDialog {
    private JTextField emailField;
    private JPasswordField passwordField;
    private JButton connexionButton;
    private JButton annulerButton;
    private JButton motDePasseOublieButton;
    
    private boolean connexionReussie = false;
    private int userId = -1;
    private int pharmacieId = -1;
    private String nomUtilisateur = "";
    
    public ConnexionSimple(Frame parent) {
        super(parent, "Connexion - BigPharma", true);
        initComponents();
        setupLayout();
        setupEventListeners();
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(450, 300);
        setLocationRelativeTo(parent);
    }
    
    private void initComponents() {
        emailField = new JTextField(20);
        passwordField = new JPasswordField(20);
        connexionButton = new JButton("Se connecter");
        annulerButton = new JButton("Annuler");
        motDePasseOublieButton = new JButton("Mot de passe oublié");
        
        connexionButton.setEnabled(false);
        motDePasseOublieButton.setFont(new Font("Arial", Font.PLAIN, 11));
    }
    
    private void setupLayout() {
        setLayout(new BorderLayout());
        
        // Panel principal
        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(30, 30, 20, 30));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        
        // Titre
        JLabel titleLabel = new JLabel("Connexion BigPharma");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titleLabel.setForeground(new Color(0, 102, 204));
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(5, 5, 20, 5);
        mainPanel.add(titleLabel, gbc);
        
        // Email
        gbc.gridwidth = 1;
        gbc.insets = new Insets(8, 8, 8, 8);
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
        gbc.insets = new Insets(5, 8, 15, 8);
        mainPanel.add(motDePasseOublieButton, gbc);
        
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
        
        motDePasseOublieButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ouvrirMotDePasseOublie();
            }
        });
        
        // Validation en temps réel
        java.awt.event.KeyAdapter keyListener = new java.awt.event.KeyAdapter() {
            @Override
            public void keyReleased(java.awt.event.KeyEvent e) {
                validerChamps();
            }
        };
        
        emailField.addKeyListener(keyListener);
        passwordField.addKeyListener(keyListener);
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
        
        // Vérifier si le compte est suspendu
        if (estCompteSuspendu(email)) {
            JOptionPane.showMessageDialog(this, 
                "Votre compte est temporairement suspendu suite à trop de tentatives de connexion échouées.\n" +
                "Veuillez réessayer dans 30 minutes ou utiliser la fonction 'Mot de passe oublié'.", 
                "Compte suspendu", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // Hasher le mot de passe
        String hashedPassword = hashPassword(password);
        
        // Vérifier les identifiants
        if (verifierIdentifiants(email, hashedPassword)) {
            connexionReussie = true;
            enregistrerTentativeReussie(email);
            dispose();
        } else {
            enregistrerTentativeEchouee(email);
            int tentativesRestantes = getTentativesRestantes(email);
            
            if (tentativesRestantes <= 0) {
                JOptionPane.showMessageDialog(this, 
                    "Trop de tentatives de connexion échouées.\n" +
                    "Votre compte est suspendu pour 30 minutes.\n" +
                    "Utilisez la fonction 'Mot de passe oublié' si nécessaire.", 
                    "Compte suspendu", JOptionPane.ERROR_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, 
                    "Email ou mot de passe incorrect.\n" +
                    "Tentatives restantes: " + tentativesRestantes, 
                    "Erreur de connexion", JOptionPane.ERROR_MESSAGE);
            }
            
            passwordField.setText("");
            passwordField.requestFocus();
        }
    }
    
    private boolean verifierIdentifiants(String email, String hashedPassword) {
        String query = "SELECT id, pharmacie_id, nom FROM utilisateurs WHERE email = ? AND mot_de_passe = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setString(1, email);
            pstmt.setString(2, hashedPassword);
            
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                userId = rs.getInt("id");
                pharmacieId = rs.getInt("pharmacie_id");
                nomUtilisateur = rs.getString("nom");
                return true;
            }
            
        } catch (SQLException e) {
            System.err.println("Erreur lors de la vérification des identifiants: " + e.getMessage());
            JOptionPane.showMessageDialog(this, 
                "Erreur de connexion à la base de données.\n" +
                "Vérifiez que MySQL est démarré et que la base 'bigpharma' existe.", 
                "Erreur de base de données", JOptionPane.ERROR_MESSAGE);
        }
        
        return false;
    }
    
    private boolean estCompteSuspendu(String email) {
        creerTableLoginAttempts();
        
        String query = """
            SELECT COUNT(*) FROM login_attempts 
            WHERE email = ? AND success = FALSE 
            AND attempt_time > DATE_SUB(NOW(), INTERVAL 30 MINUTE)
        """;
        
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setString(1, email);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt(1) >= 5;
            }
            
        } catch (SQLException e) {
            System.err.println("Erreur lors de la vérification de suspension: " + e.getMessage());
        }
        
        return false;
    }
    
    private int getTentativesRestantes(String email) {
        creerTableLoginAttempts();
        
        String query = """
            SELECT COUNT(*) FROM login_attempts 
            WHERE email = ? AND success = FALSE 
            AND attempt_time > DATE_SUB(NOW(), INTERVAL 30 MINUTE)
        """;
        
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setString(1, email);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                int tentativesEchouees = rs.getInt(1);
                return Math.max(0, 5 - tentativesEchouees);
            }
            
        } catch (SQLException e) {
            System.err.println("Erreur lors du calcul des tentatives restantes: " + e.getMessage());
        }
        
        return 5;
    }
    
    private void enregistrerTentativeEchouee(String email) {
        creerTableLoginAttempts();
        
        String query = "INSERT INTO login_attempts (email, success, attempt_time) VALUES (?, FALSE, NOW())";
        
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setString(1, email);
            pstmt.executeUpdate();
            
        } catch (SQLException e) {
            System.err.println("Erreur lors de l'enregistrement de la tentative échouée: " + e.getMessage());
        }
    }
    
    private void enregistrerTentativeReussie(String email) {
        creerTableLoginAttempts();
        
        String query = "INSERT INTO login_attempts (email, success, attempt_time) VALUES (?, TRUE, NOW())";
        
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setString(1, email);
            pstmt.executeUpdate();
            
        } catch (SQLException e) {
            System.err.println("Erreur lors de l'enregistrement de la tentative réussie: " + e.getMessage());
        }
    }
    
    private void creerTableLoginAttempts() {
        String createTableQuery = """
            CREATE TABLE IF NOT EXISTS login_attempts (
                id INT AUTO_INCREMENT PRIMARY KEY,
                email VARCHAR(255) NOT NULL,
                success BOOLEAN NOT NULL,
                attempt_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                INDEX idx_email_time (email, attempt_time)
            )
        """;
        
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            
            stmt.executeUpdate(createTableQuery);
            
        } catch (SQLException e) {
            System.err.println("Erreur lors de la création de la table login_attempts: " + e.getMessage());
        }
    }
    
    private void ouvrirMotDePasseOublie() {
        MotDePasseOublieSimple dialog = new MotDePasseOublieSimple((Frame) this.getOwner());
        dialog.setVisible(true);
        
        if (dialog.estEmailEnvoye()) {
            JOptionPane.showMessageDialog(this, 
                "Un email de réinitialisation a été envoyé.\n" +
                "Vérifiez votre boîte de réception.", 
                "Email envoyé", JOptionPane.INFORMATION_MESSAGE);
        }
    }
    
    private String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hashedBytes = md.digest(password.getBytes());
            
            StringBuilder sb = new StringBuilder();
            for (byte b : hashedBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
            
        } catch (NoSuchAlgorithmException e) {
            System.err.println("Erreur lors du hashage du mot de passe: " + e.getMessage());
            return password; // Fallback (non sécurisé)
        }
    }
    
    private Connection getConnection() throws SQLException {
        String url = "jdbc:mysql://localhost:3306/bigpharma";
        String username = "root";
        String password = "";
        
        return DriverManager.getConnection(url, username, password);
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
