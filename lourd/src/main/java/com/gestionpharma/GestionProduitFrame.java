package com.gestionpharma;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.Vector;

public class GestionProduitFrame extends JFrame {
    private JTextField txtNom, txtDescription, txtPrix, txtQuantite;
    private JComboBox<String> cbCategorie;
    private JTable tableProduits;
    private DefaultTableModel modelProduits;
    private ProduitDAO produitDAO;
    private JButton btnAjouter, btnModifier, btnSupprimer;
    private int produitSelectionneId = -1;

    public GestionProduitFrame() {
        produitDAO = new ProduitDAO();
        initComponents();
        chargerProduits();
        chargerCategories();
    }

    private void initComponents() {
        setTitle("Gestion des Produits Pharmaceutiques");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Panneau de saisie
        JPanel panelSaisie = new JPanel(new GridLayout(6, 2));
        panelSaisie.add(new JLabel("Nom :"));
        txtNom = new JTextField();
        panelSaisie.add(txtNom);

        panelSaisie.add(new JLabel("Description :"));
        txtDescription = new JTextField();
        panelSaisie.add(txtDescription);

        panelSaisie.add(new JLabel("Prix :"));
        txtPrix = new JTextField();
        panelSaisie.add(txtPrix);

        panelSaisie.add(new JLabel("Quantité :"));
        txtQuantite = new JTextField();
        panelSaisie.add(txtQuantite);

        panelSaisie.add(new JLabel("Catégorie :"));
        cbCategorie = new JComboBox<>();
        panelSaisie.add(cbCategorie);

        add(panelSaisie, BorderLayout.NORTH);

        // Boutons
        JPanel panelBoutons = new JPanel();
        btnAjouter = new JButton("Ajouter");
        btnModifier = new JButton("Modifier");
        btnSupprimer = new JButton("Supprimer");

        panelBoutons.add(btnAjouter);
        panelBoutons.add(btnModifier);
        panelBoutons.add(btnSupprimer);
        add(panelBoutons, BorderLayout.CENTER);

        // Tableau des produits
        String[] colonnes = {"ID", "Nom", "Description", "Prix", "Quantité", "Catégorie"};
        modelProduits = new DefaultTableModel(colonnes, 0);
        tableProduits = new JTable(modelProduits);
        JScrollPane scrollPane = new JScrollPane(tableProduits);
        add(scrollPane, BorderLayout.SOUTH);

        // Écouteurs d'événements
        btnAjouter.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ajouterProduit();
            }
        });

        btnModifier.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                modifierProduit();
            }
        });

        btnSupprimer.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                supprimerProduit();
            }
        });

        // Sélection dans le tableau
        tableProduits.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int ligneSelectionnee = tableProduits.getSelectedRow();
                if (ligneSelectionnee != -1) {
                    produitSelectionneId = (int) tableProduits.getValueAt(ligneSelectionnee, 0);
                    txtNom.setText((String) tableProduits.getValueAt(ligneSelectionnee, 1));
                    txtDescription.setText((String) tableProduits.getValueAt(ligneSelectionnee, 2));
                    txtPrix.setText(String.valueOf(tableProduits.getValueAt(ligneSelectionnee, 3)));
                    txtQuantite.setText(String.valueOf(tableProduits.getValueAt(ligneSelectionnee, 4)));
                    cbCategorie.setSelectedItem(tableProduits.getValueAt(ligneSelectionnee, 5));
                }
            }
        });
    }

    private void chargerCategories() {
        List<String> categories = produitDAO.obtenirCategories();
        cbCategorie.removeAllItems();
        for (String categorie : categories) {
            cbCategorie.addItem(categorie);
        }
    }

    private void chargerProduits() {
        modelProduits.setRowCount(0);
        List<Produit> produits = produitDAO.obtenirTousProduits();
        for (Produit produit : produits) {
            modelProduits.addRow(new Object[]{
                produit.getId(),
                produit.getNom(),
                produit.getDescription(),
                produit.getPrix(),
                produit.getQuantiteStock(),
                produit.getCategorie()
            });
        }
    }

    private void ajouterProduit() {
        try {
            String nom = txtNom.getText();
            String description = txtDescription.getText();
            double prix = Double.parseDouble(txtPrix.getText());
            int quantite = Integer.parseInt(txtQuantite.getText());
            String categorie = (String) cbCategorie.getSelectedItem();

            Produit nouveauProduit = new Produit(nom, description, prix, quantite, categorie);
            produitDAO.creerProduit(nouveauProduit);
            chargerProduits();
            reinitialiserFormulaire();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Veuillez saisir des valeurs numériques valides pour le prix et la quantité.", "Erreur de saisie", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void modifierProduit() {
        if (produitSelectionneId != -1) {
            try {
                String nom = txtNom.getText();
                String description = txtDescription.getText();
                double prix = Double.parseDouble(txtPrix.getText());
                int quantite = Integer.parseInt(txtQuantite.getText());
                String categorie = (String) cbCategorie.getSelectedItem();

                Produit produitModifie = new Produit(nom, description, prix, quantite, categorie);
                produitModifie.setId(produitSelectionneId);
                produitDAO.mettreAJourProduit(produitModifie);
                chargerProduits();
                reinitialiserFormulaire();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Veuillez saisir des valeurs numériques valides pour le prix et la quantité.", "Erreur de saisie", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void supprimerProduit() {
        if (produitSelectionneId != -1) {
            int confirmation = JOptionPane.showConfirmDialog(this, 
                "Êtes-vous sûr de vouloir supprimer ce produit ?", 
                "Confirmation de suppression", 
                JOptionPane.YES_NO_OPTION);
            
            if (confirmation == JOptionPane.YES_OPTION) {
                produitDAO.supprimerProduit(produitSelectionneId);
                chargerProduits();
                reinitialiserFormulaire();
            }
        }
    }

    private void reinitialiserFormulaire() {
        txtNom.setText("");
        txtDescription.setText("");
        txtPrix.setText("");
        txtQuantite.setText("");
        cbCategorie.setSelectedIndex(0);
        produitSelectionneId = -1;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new GestionProduitFrame().setVisible(true);
        });
    }
}
