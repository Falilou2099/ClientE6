package com.gestionpharma;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.Vector;

public class GestionFournisseursFrame extends JFrame {
    private JTable tableFournisseurs;
    private DefaultTableModel modelFournisseurs;
    private FournisseurDAO fournisseurDAO;

    // Champs de saisie pour les fournisseurs
    private JTextField txtId;
    private JTextField txtNom;
    private JTextField txtContact;
    private JTextField txtTelephone;
    private JTextField txtEmail;
    private JComboBox<String> cbTypesMedicaments;

    // Boutons d'action
    private JButton btnAjouter;
    private JButton btnModifier;
    private JButton btnSupprimer;
    private JButton btnReinitialiser;

    public GestionFournisseursFrame() {
        fournisseurDAO = new FournisseurDAO();
        
        initComponents();
        chargerFournisseurs();
        initialiserTypesMedicaments();
    }

    private void initComponents() {
        setTitle("Gestion des Fournisseurs Pharmaceutiques");
        setSize(1000, 600);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        // Panneau de saisie des informations
        JPanel panelSaisie = new JPanel(new GridLayout(7, 2, 5, 5));
        panelSaisie.setBorder(BorderFactory.createTitledBorder("Informations du Fournisseur"));

        // Champs de saisie
        panelSaisie.add(new JLabel("ID (auto-généré) :"));
        txtId = new JTextField();
        txtId.setEditable(false);
        panelSaisie.add(txtId);

        panelSaisie.add(new JLabel("Nom du Fournisseur :"));
        txtNom = new JTextField();
        panelSaisie.add(txtNom);

        panelSaisie.add(new JLabel("Personne Contact :"));
        txtContact = new JTextField();
        panelSaisie.add(txtContact);

        panelSaisie.add(new JLabel("Téléphone :"));
        txtTelephone = new JTextField();
        panelSaisie.add(txtTelephone);

        panelSaisie.add(new JLabel("Email :"));
        txtEmail = new JTextField();
        panelSaisie.add(txtEmail);

        panelSaisie.add(new JLabel("Type de Médicaments :"));
        cbTypesMedicaments = new JComboBox<>();
        panelSaisie.add(cbTypesMedicaments);

        // Panneau des boutons
        JPanel panelBoutons = new JPanel(new FlowLayout());
        btnAjouter = new JButton("Ajouter");
        btnModifier = new JButton("Modifier");
        btnSupprimer = new JButton("Supprimer");
        btnReinitialiser = new JButton("Réinitialiser");

        panelBoutons.add(btnAjouter);
        panelBoutons.add(btnModifier);
        panelBoutons.add(btnSupprimer);
        panelBoutons.add(btnReinitialiser);

        // Tableau des fournisseurs
        String[] colonnes = {"ID", "Nom", "Contact", "Téléphone", "Email", "Types de Médicaments"};
        modelFournisseurs = new DefaultTableModel(colonnes, 0);
        tableFournisseurs = new JTable(modelFournisseurs);
        JScrollPane scrollFournisseurs = new JScrollPane(tableFournisseurs);

        // Disposition des composants
        add(panelSaisie, BorderLayout.NORTH);
        add(scrollFournisseurs, BorderLayout.CENTER);
        add(panelBoutons, BorderLayout.SOUTH);

        // Écouteurs d'événements
        btnAjouter.addActionListener(e -> ajouterFournisseur());
        btnModifier.addActionListener(e -> modifierFournisseur());
        btnSupprimer.addActionListener(e -> supprimerFournisseur());
        btnReinitialiser.addActionListener(e -> reinitialiserFormulaire());

        // Sélection dans le tableau
        tableFournisseurs.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int ligneSelectionnee = tableFournisseurs.getSelectedRow();
                if (ligneSelectionnee != -1) {
                    remplirFormulaire(ligneSelectionnee);
                }
            }
        });
    }

    private void initialiserTypesMedicaments() {
        // Types de médicaments prédéfinis
        String[] typesMedicaments = {
            "Antibiotiques", 
            "Antidépresseurs", 
            "Antihypertenseurs", 
            "Antiviraux", 
            "Vaccins", 
            "Médicaments génériques", 
            "Médicaments de prescription", 
            "Médicaments en vente libre"
        };

        cbTypesMedicaments.removeAllItems();
        for (String type : typesMedicaments) {
            cbTypesMedicaments.addItem(type);
        }
    }

    private void ajouterFournisseur() {
        try {
            // Validation des champs
            if (txtNom.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Le nom du fournisseur est obligatoire.", "Erreur", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Créer un objet Fournisseur
            Fournisseur fournisseur = new Fournisseur();
            fournisseur.setNom(txtNom.getText().trim());
            fournisseur.setContact(txtContact.getText().trim());
            fournisseur.setTelephone(txtTelephone.getText().trim());
            fournisseur.setEmail(txtEmail.getText().trim());
            fournisseur.setTypesMedicaments((String) cbTypesMedicaments.getSelectedItem());

            // Enregistrer le fournisseur
            fournisseurDAO.creerFournisseur(fournisseur);

            // Actualiser l'affichage
            chargerFournisseurs();

            // Réinitialiser le formulaire
            reinitialiserFormulaire();

            JOptionPane.showMessageDialog(this, "Fournisseur ajouté avec succès !", "Succès", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Erreur lors de l'ajout du fournisseur : " + ex.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void modifierFournisseur() {
        try {
            // Vérifier qu'un fournisseur est sélectionné
            if (txtId.getText().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Veuillez sélectionner un fournisseur à modifier.", "Erreur", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Validation des champs
            if (txtNom.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Le nom du fournisseur est obligatoire.", "Erreur", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Créer un objet Fournisseur
            Fournisseur fournisseur = new Fournisseur();
            fournisseur.setId(Integer.parseInt(txtId.getText()));
            fournisseur.setNom(txtNom.getText().trim());
            fournisseur.setContact(txtContact.getText().trim());
            fournisseur.setTelephone(txtTelephone.getText().trim());
            fournisseur.setEmail(txtEmail.getText().trim());
            fournisseur.setTypesMedicaments((String) cbTypesMedicaments.getSelectedItem());

            // Mettre à jour le fournisseur
            fournisseurDAO.modifierFournisseur(fournisseur);

            // Actualiser l'affichage
            chargerFournisseurs();

            // Réinitialiser le formulaire
            reinitialiserFormulaire();

            JOptionPane.showMessageDialog(this, "Fournisseur modifié avec succès !", "Succès", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Erreur lors de la modification du fournisseur : " + ex.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void supprimerFournisseur() {
        try {
            // Vérifier qu'un fournisseur est sélectionné
            if (txtId.getText().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Veuillez sélectionner un fournisseur à supprimer.", "Erreur", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Confirmation de suppression
            int confirmation = JOptionPane.showConfirmDialog(
                this, 
                "Êtes-vous sûr de vouloir supprimer ce fournisseur ?", 
                "Confirmation de suppression", 
                JOptionPane.YES_NO_OPTION
            );

            if (confirmation == JOptionPane.YES_OPTION) {
                int fournisseurId = Integer.parseInt(txtId.getText());
                
                // Supprimer le fournisseur
                fournisseurDAO.supprimerFournisseur(fournisseurId);

                // Actualiser l'affichage
                chargerFournisseurs();

                // Réinitialiser le formulaire
                reinitialiserFormulaire();

                JOptionPane.showMessageDialog(this, "Fournisseur supprimé avec succès !", "Succès", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Erreur lors de la suppression du fournisseur : " + ex.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void reinitialiserFormulaire() {
        txtId.setText("");
        txtNom.setText("");
        txtContact.setText("");
        txtTelephone.setText("");
        txtEmail.setText("");
        cbTypesMedicaments.setSelectedIndex(0);
        
        // Désélectionner la ligne du tableau
        tableFournisseurs.clearSelection();
    }

    private void remplirFormulaire(int ligneSelectionnee) {
        txtId.setText(modelFournisseurs.getValueAt(ligneSelectionnee, 0).toString());
        txtNom.setText(modelFournisseurs.getValueAt(ligneSelectionnee, 1).toString());
        txtContact.setText(modelFournisseurs.getValueAt(ligneSelectionnee, 2).toString());
        txtTelephone.setText(modelFournisseurs.getValueAt(ligneSelectionnee, 3).toString());
        txtEmail.setText(modelFournisseurs.getValueAt(ligneSelectionnee, 4).toString());
        
        // Sélectionner le type de médicaments
        String typeMedicament = modelFournisseurs.getValueAt(ligneSelectionnee, 5).toString();
        cbTypesMedicaments.setSelectedItem(typeMedicament);
    }

    private void chargerFournisseurs() {
        // Vider le modèle existant
        modelFournisseurs.setRowCount(0);

        // Charger les fournisseurs depuis la base de données
        List<Fournisseur> fournisseurs = fournisseurDAO.obtenirTousFournisseurs();
        
        for (Fournisseur fournisseur : fournisseurs) {
            modelFournisseurs.addRow(new Object[]{
                fournisseur.getId(),
                fournisseur.getNom(),
                fournisseur.getContact(),
                fournisseur.getTelephone(),
                fournisseur.getEmail(),
                fournisseur.getTypesMedicaments()
            });
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new GestionFournisseursFrame().setVisible(true);
        });
    }
}
