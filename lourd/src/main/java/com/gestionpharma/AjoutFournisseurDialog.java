package com.gestionpharma;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import com.gestionpharma.models.Fournisseur;

/**
 * Fenêtre de dialogue pour l'ajout et la modification de fournisseurs
 */
public class AjoutFournisseurDialog extends JDialog {
    private JTextField txtNom;
    private JTextField txtAdresse;
    private JTextField txtTelephone;
    private JTextField txtEmail;
    private JTextField txtSiret;
    private JButton btnEnregistrer;
    private JButton btnAnnuler;
    
    private boolean estConfirme = false;
    private Fournisseur fournisseur;
    
    /**
     * Constructeur pour créer un nouveau fournisseur
     * @param parent Fenêtre parente
     */
    public AjoutFournisseurDialog(Frame parent) {
        super(parent, "Ajouter un fournisseur", true);
        this.fournisseur = new Fournisseur();
        initialiserComposants();
    }
    
    /**
     * Constructeur pour modifier un fournisseur existant
     * @param parent Fenêtre parente
     * @param fournisseur Fournisseur à modifier
     */
    public AjoutFournisseurDialog(Frame parent, Fournisseur fournisseur) {
        super(parent, "Modifier un fournisseur", true);
        this.fournisseur = fournisseur;
        initialiserComposants();
        remplirFormulaire();
    }
    
    /**
     * Initialise les composants de l'interface
     */
    private void initialiserComposants() {
        setSize(500, 350);
        setLocationRelativeTo(getParent());
        setResizable(false);
        setLayout(new BorderLayout());
        
        // Panel principal avec GridBagLayout pour un meilleur contrôle du placement
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 10, 5, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // Titre
        JLabel lblTitre = new JLabel("Saisissez les informations du fournisseur");
        lblTitre.setFont(new Font("Arial", Font.BOLD, 16));
        lblTitre.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        panel.add(lblTitre, gbc);
        
        // Réinitialiser gridwidth
        gbc.gridwidth = 1;
        
        // Nom
        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(new JLabel("Nom *:"), gbc);
        txtNom = new JTextField(20);
        gbc.gridx = 1;
        panel.add(txtNom, gbc);
        
        // Adresse
        gbc.gridx = 0; gbc.gridy = 2;
        panel.add(new JLabel("Adresse:"), gbc);
        txtAdresse = new JTextField(20);
        gbc.gridx = 1;
        panel.add(txtAdresse, gbc);
        
        // Téléphone
        gbc.gridx = 0; gbc.gridy = 3;
        panel.add(new JLabel("Téléphone *:"), gbc);
        txtTelephone = new JTextField(20);
        gbc.gridx = 1;
        panel.add(txtTelephone, gbc);
        
        // Email
        gbc.gridx = 0; gbc.gridy = 4;
        panel.add(new JLabel("Email *:"), gbc);
        txtEmail = new JTextField(20);
        gbc.gridx = 1;
        panel.add(txtEmail, gbc);
        
        // SIRET
        gbc.gridx = 0; gbc.gridy = 5;
        panel.add(new JLabel("SIRET:"), gbc);
        txtSiret = new JTextField(20);
        gbc.gridx = 1;
        panel.add(txtSiret, gbc);
        
        // Panel pour les boutons
        JPanel panelBoutons = new JPanel(new FlowLayout());
        btnEnregistrer = new JButton("Enregistrer");
        btnAnnuler = new JButton("Annuler");
        
        btnEnregistrer.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (validerFormulaire()) {
                    estConfirme = true;
                    dispose();
                }
            }
        });
        
        btnAnnuler.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                estConfirme = false;
                dispose();
            }
        });
        
        panelBoutons.add(btnEnregistrer);
        panelBoutons.add(btnAnnuler);
        
        // Ajouter les panels au dialog
        add(panel, BorderLayout.CENTER);
        add(panelBoutons, BorderLayout.SOUTH);
    }
    
    /**
     * Remplit le formulaire avec les données du fournisseur à modifier
     */
    private void remplirFormulaire() {
        if (fournisseur != null) {
            txtNom.setText(fournisseur.getNom());
            txtAdresse.setText(fournisseur.getAdresse());
            txtTelephone.setText(fournisseur.getTelephone());
            txtEmail.setText(fournisseur.getEmail());
            txtSiret.setText(fournisseur.getSiret());
        }
    }
    
    /**
     * Valide les données du formulaire
     * @return true si les données sont valides, false sinon
     */
    private boolean validerFormulaire() {
        // Vérifier que les champs obligatoires sont remplis
        if (txtNom.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Veuillez saisir un nom pour le fournisseur.", 
                    "Erreur de saisie", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        
        if (txtTelephone.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Veuillez saisir un numéro de téléphone.", 
                    "Erreur de saisie", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        
        if (txtEmail.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Veuillez saisir une adresse email.", 
                    "Erreur de saisie", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        
        // Validation basique de l'email
        String email = txtEmail.getText().trim();
        if (!email.contains("@") || !email.contains(".")) {
            JOptionPane.showMessageDialog(this, "Veuillez saisir une adresse email valide.", 
                    "Erreur de saisie", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        
        // Mettre à jour l'objet fournisseur avec les données du formulaire
        fournisseur.setNom(txtNom.getText().trim());
        fournisseur.setAdresse(txtAdresse.getText().trim());
        fournisseur.setTelephone(txtTelephone.getText().trim());
        fournisseur.setEmail(txtEmail.getText().trim());
        fournisseur.setSiret(txtSiret.getText().trim());
        
        return true;
    }
    
    /**
     * Vérifie si l'utilisateur a confirmé l'ajout/modification du fournisseur
     * @return true si l'utilisateur a confirmé, false sinon
     */
    public boolean estConfirme() {
        return estConfirme;
    }
    
    /**
     * Récupère le fournisseur créé ou modifié
     * @return Le fournisseur
     */
    public Fournisseur getFournisseur() {
        return fournisseur;
    }
}
