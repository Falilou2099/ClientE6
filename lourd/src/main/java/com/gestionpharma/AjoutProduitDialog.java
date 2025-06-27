package com.gestionpharma;

import javax.swing.*;
import javax.swing.DefaultComboBoxModel;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import com.gestionpharma.models.Produit;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;

/**
 * Fenêtre de dialogue pour l'ajout et la modification de produits pharmaceutiques
 */
public class AjoutProduitDialog extends JDialog {
    private JTextField txtNom;
    private JTextField txtDescription;
    private JTextField txtPrixVente;
    private JTextField txtPrixAchat;
    private JComboBox<String> cbCategorie;
    private JSpinner dateExpiration;
    private JTextField txtImagePath;
    private JLabel lblImagePreview;
    private JButton btnSelectImage;
    private File selectedImageFile = null;
    private static final String IMAGES_DIR = "images";
    private JButton btnEnregistrer;
    private JButton btnAnnuler;
    
    private boolean estConfirme = false;
    private Produit produit;
    private static final String DEFAULT_IMAGE_PATH = "images/default_product.png";
    
    /**
     * Constructeur pour créer un nouveau produit
     * @param parent Fenêtre parente
     */
    public AjoutProduitDialog(Frame parent) {
        super(parent, "Ajouter un produit", true);
        this.produit = new Produit();
        initialiserComposants();
    }
    
    /**
     * Constructeur pour modifier un produit existant
     * @param parent Fenêtre parente
     * @param produit Produit à modifier
     */
    public AjoutProduitDialog(Frame parent, Produit produit) {
        super(parent, "Modifier un produit", true);
        this.produit = produit;
        initialiserComposants();
        remplirFormulaire();
    }
    
    /**
     * Initialise les composants de l'interface
     */
    private void initialiserComposants() {
        setSize(600, 500);
        setLocationRelativeTo(getParent());
        setResizable(false);
        setLayout(new BorderLayout());
        
        // Panel principal avec GridBagLayout pour un meilleur contrôle du placement
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 10, 5, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // Titre
        JLabel lblTitre = new JLabel("Saisissez les informations du nouveau produit");
        lblTitre.setFont(new Font("Arial", Font.BOLD, 14));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 3;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(10, 10, 20, 10);
        panel.add(lblTitre, gbc);
        
        // Réinitialiser les insets pour les champs
        gbc.insets = new Insets(5, 10, 5, 10);
        gbc.anchor = GridBagConstraints.WEST;
        
        // Nom
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        panel.add(new JLabel("Nom:"), gbc);
        
        txtNom = new JTextField(20);
        gbc.gridx = 1;
        gbc.gridwidth = 2;
        panel.add(txtNom, gbc);
        
        // Description
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 1;
        panel.add(new JLabel("Description:"), gbc);
        
        txtDescription = new JTextField(20);
        gbc.gridx = 1;
        gbc.gridwidth = 2;
        panel.add(txtDescription, gbc);
        
        // Prix de vente
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 1;
        panel.add(new JLabel("Prix de vente:"), gbc);
        
        txtPrixVente = new JTextField(20);
        gbc.gridx = 1;
        gbc.gridwidth = 2;
        panel.add(txtPrixVente, gbc);
        
        // Prix d'achat
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 1;
        panel.add(new JLabel("Prix d'achat:"), gbc);
        
        txtPrixAchat = new JTextField(20);
        gbc.gridx = 1;
        gbc.gridwidth = 2;
        panel.add(txtPrixAchat, gbc);
        
        // Catégorie
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.gridwidth = 1;
        panel.add(new JLabel("Catégorie:"), gbc);
        
        // Initialiser le JComboBox avec un modèle par défaut explicite
        DefaultComboBoxModel<String> categorieModel = new DefaultComboBoxModel<>();
        cbCategorie = new JComboBox<>(categorieModel);
        cbCategorie.setEditable(false);
        cbCategorie.setMaximumRowCount(10); // Afficher jusqu'à 10 éléments à la fois
        gbc.gridx = 1;
        gbc.gridwidth = 2;
        panel.add(cbCategorie, gbc);
        
        // Date d'expiration
        gbc.gridx = 0;
        gbc.gridy = 6;
        gbc.gridwidth = 1;
        panel.add(new JLabel("Date d'expiration:"), gbc);
        
        dateExpiration = new JSpinner(new SpinnerDateModel());
        JSpinner.DateEditor editor = new JSpinner.DateEditor(dateExpiration, "dd/MM/yyyy");
        dateExpiration.setEditor(editor);
        gbc.gridx = 1;
        gbc.gridwidth = 2;
        panel.add(dateExpiration, gbc);
        
        // Sélection de l'image
        gbc.gridx = 0;
        gbc.gridy = 7;
        gbc.gridwidth = 1;
        panel.add(new JLabel("Image du produit:"), gbc);
        
        txtImagePath = new JTextField(20);
        txtImagePath.setEditable(false);
        txtImagePath.setToolTipText("Chemin de l'image sélectionnée");
        gbc.gridx = 1;
        gbc.gridwidth = 1;
        panel.add(txtImagePath, gbc);
        
        btnSelectImage = new JButton("Sélectionner");
        btnSelectImage.addActionListener(e -> selectImage());
        gbc.gridx = 2;
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.NONE;
        panel.add(btnSelectImage, gbc);
        
        // Créer le répertoire d'images s'il n'existe pas
        createImagesDirectory();
        
        // Prévisualisation de l'image
        gbc.gridx = 0;
        gbc.gridy = 8;
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(new JLabel("Prévisualisation:"), gbc);
        
        lblImagePreview = new JLabel();
        lblImagePreview.setPreferredSize(new Dimension(150, 150));
        lblImagePreview.setBorder(BorderFactory.createLoweredBevelBorder());
        lblImagePreview.setHorizontalAlignment(JLabel.CENTER);
        lblImagePreview.setText("Aucune image");
        gbc.gridx = 1;
        gbc.gridwidth = 2;
        panel.add(lblImagePreview, gbc);
        
        // Panel des boutons
        JPanel panelBoutons = new JPanel(new FlowLayout());
        
        btnEnregistrer = new JButton("Enregistrer");
        btnEnregistrer.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (validerFormulaire()) {
                    estConfirme = true;
                    dispose();
                }
            }
        });
        
        btnAnnuler = new JButton("Annuler");
        btnAnnuler.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });
        
        panelBoutons.add(btnEnregistrer);
        panelBoutons.add(btnAnnuler);
        
        // Ajout des panels à la fenêtre
        add(panel, BorderLayout.CENTER);
        add(panelBoutons, BorderLayout.SOUTH);
        
        // Charger les catégories après la création du JComboBox
        chargerCategories();
    }
    
    /**
     * Crée le répertoire d'images s'il n'existe pas
     */
    private void createImagesDirectory() {
        try {
            Path imagesDir = Paths.get(IMAGES_DIR);
            if (!Files.exists(imagesDir)) {
                Files.createDirectories(imagesDir);
                System.out.println("Répertoire d'images créé: " + imagesDir.toAbsolutePath());
                
                // Créer une image par défaut si elle n'existe pas
                Path defaultImage = imagesDir.resolve("default_product.png");
                if (!Files.exists(defaultImage)) {
                    // Créer une image par défaut simple
                    BufferedImage img = new BufferedImage(150, 150, BufferedImage.TYPE_INT_RGB);
                    Graphics2D g2d = img.createGraphics();
                    g2d.setColor(Color.WHITE);
                    g2d.fillRect(0, 0, 150, 150);
                    g2d.setColor(Color.BLACK);
                    g2d.drawRect(0, 0, 149, 149);
                    g2d.setFont(new Font("Arial", Font.BOLD, 14));
                    g2d.drawString("Produit", 50, 75);
                    g2d.dispose();
                    ImageIO.write(img, "png", defaultImage.toFile());
                    System.out.println("Image par défaut créée: " + defaultImage.toAbsolutePath());
                }
            }
        } catch (IOException e) {
            System.err.println("Erreur lors de la création du répertoire d'images: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Ouvre un sélecteur de fichier pour choisir une image
     */
    private void selectImage() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Sélectionner une image");
        fileChooser.setFileFilter(new FileNameExtensionFilter("Images", "jpg", "jpeg", "png", "gif", "bmp"));
        
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            selectedImageFile = fileChooser.getSelectedFile();
            txtImagePath.setText(selectedImageFile.getName());
            previewSelectedImage();
        }
    }
    
    /**
     * Prévisualise l'image sélectionnée
     */
    private void previewSelectedImage() {
        if (selectedImageFile == null || !selectedImageFile.exists()) {
            useDefaultImage();
            return;
        }
        
        try {
            BufferedImage image = ImageIO.read(selectedImageFile);
            
            if (image != null) {
                // Redimensionner l'image pour la prévisualisation
                Image scaledImage = image.getScaledInstance(150, 150, Image.SCALE_SMOOTH);
                ImageIcon icon = new ImageIcon(scaledImage);
                lblImagePreview.setIcon(icon);
                lblImagePreview.setText("");
            } else {
                useDefaultImage();
            }
        } catch (Exception e) {
            useDefaultImage();
            JOptionPane.showMessageDialog(this, 
                "Erreur lors du chargement de l'image : " + e.getMessage(), 
                "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * Utilise l'image par défaut
     */
    private void useDefaultImage() {
        try {
            File defaultImageFile = new File(DEFAULT_IMAGE_PATH);
            if (defaultImageFile.exists()) {
                BufferedImage image = ImageIO.read(defaultImageFile);
                Image scaledImage = image.getScaledInstance(150, 150, Image.SCALE_SMOOTH);
                ImageIcon icon = new ImageIcon(scaledImage);
                lblImagePreview.setIcon(icon);
                lblImagePreview.setText("");
                selectedImageFile = defaultImageFile;
                txtImagePath.setText("Image par défaut");
            } else {
                lblImagePreview.setIcon(null);
                lblImagePreview.setText("Image par défaut non disponible");
                selectedImageFile = null;
            }
        } catch (Exception e) {
            lblImagePreview.setIcon(null);
            lblImagePreview.setText("Image non disponible");
            System.err.println("Erreur lors du chargement de l'image par défaut: " + e.getMessage());
        }
    }

    /**
     * Charge les catégories dans la liste déroulante
     */
    private void chargerCategories() {
        try {
            System.out.println("Début du chargement des catégories...");
            
            // Vérifier que le composant cbCategorie existe
            if (cbCategorie == null) {
                System.err.println("Erreur: cbCategorie est null!");
                return;
            }
            
            // Créer un nouveau modèle pour être sûr
            DefaultComboBoxModel<String> model = new DefaultComboBoxModel<>();
            
            // Liste statique des catégories
            String[] categories = {
                "Analgésiques", 
                "Anti-inflammatoires", 
                "Antibiotiques", 
                "Antihistaminiques",
                "Gastro-entérologie", 
                "Dermatologie", 
                "Cardiologie", 
                "Vitamines",
                "Compléments alimentaires", 
                "Homéopathie", 
                "Hygiène", 
                "Premiers soins",
                "Ophtalmologie", 
                "ORL", 
                "Contraception", 
                "Nutrition", 
                "Autres"
            };
            
            System.out.println("Ajout de " + categories.length + " catégories...");
            
            // Ajouter les catégories au modèle
            for (String categorie : categories) {
                model.addElement(categorie);
                System.out.println("Catégorie ajoutée: " + categorie);
            }
            
            // Appliquer le modèle au JComboBox
            cbCategorie.setModel(model);
            
            // Sélectionner la première catégorie par défaut
            if (model.getSize() > 0) {
                cbCategorie.setSelectedIndex(0);
                System.out.println("Première catégorie sélectionnée: " + model.getElementAt(0));
            }
            
            // Forcer la mise à jour du composant
            cbCategorie.revalidate();
            cbCategorie.repaint();
            
            System.out.println("Chargement des catégories terminé. Nombre d'éléments: " + model.getSize());
            
        } catch (Exception e) {
            System.err.println("Erreur lors du chargement des catégories: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Remplit le formulaire avec les données du produit à modifier
     */
    private void remplirFormulaire() {
        if (produit != null) {
            txtNom.setText(produit.getNom());
            txtDescription.setText(produit.getDescription());
            txtPrixVente.setText(String.valueOf(produit.getPrixVente()));
            txtPrixAchat.setText(String.valueOf(produit.getPrixAchat()));
            
            // Sélectionner la catégorie du produit
            String categorie = produit.getCategorie();
            if (categorie != null && !categorie.isEmpty()) {
                cbCategorie.setSelectedItem(categorie);
            }
            
            // Définir la date d'expiration
            if (produit.getDateExpiration() != null) {
                dateExpiration.setValue(Date.from(produit.getDateExpiration().atStartOfDay(ZoneId.systemDefault()).toInstant()));
            }
            
            // Définir l'image du produit
            String imagePath = produit.getImageUrl();
            if (imagePath != null && !imagePath.isEmpty()) {
                File imageFile = new File(imagePath);
                if (imageFile.exists()) {
                    selectedImageFile = imageFile;
                    txtImagePath.setText(imageFile.getName());
                    previewSelectedImage();
                } else {
                    useDefaultImage();
                }
            } else {
                useDefaultImage();
            }
        }
    }
    
    /**
     * Valide les données du formulaire
     * @return true si les données sont valides, false sinon
     */
    private boolean validerFormulaire() {
        // Vérifier que les champs obligatoires sont remplis
        if (txtNom.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Veuillez saisir un nom pour le produit.", "Erreur de saisie", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        
        // Vérifier que les prix sont des nombres valides
        try {
            double prixVente = Double.parseDouble(txtPrixVente.getText().trim());
            if (prixVente < 0) {
                JOptionPane.showMessageDialog(this, "Le prix de vente ne peut pas être négatif.", "Erreur de saisie", JOptionPane.ERROR_MESSAGE);
                return false;
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Veuillez saisir un prix de vente valide.", "Erreur de saisie", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        
        try {
            double prixAchat = Double.parseDouble(txtPrixAchat.getText().trim());
            if (prixAchat < 0) {
                JOptionPane.showMessageDialog(this, "Le prix d'achat ne peut pas être négatif.", "Erreur de saisie", JOptionPane.ERROR_MESSAGE);
                return false;
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Veuillez saisir un prix d'achat valide.", "Erreur de saisie", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        
        // Mettre à jour l'objet produit avec les données du formulaire
        produit.setNom(txtNom.getText().trim());
        produit.setDescription(txtDescription.getText().trim());
        produit.setPrixVente(Double.parseDouble(txtPrixVente.getText().trim()));
        produit.setPrixAchat(Double.parseDouble(txtPrixAchat.getText().trim()));
        produit.setCategorie((String) cbCategorie.getSelectedItem());
        
        // Conversion de Date vers LocalDate pour la date d'expiration
        Date dateExp = (Date) dateExpiration.getValue();
        if (dateExp != null) {
            LocalDate localDateExp = dateExp.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            produit.setDateExpiration(localDateExp);
        }
        
        // Copier l'image sélectionnée dans le répertoire d'images et définir le chemin
        String imagePath = DEFAULT_IMAGE_PATH;
        
        if (selectedImageFile != null && selectedImageFile.exists()) {
            try {
                // Créer un nom de fichier unique basé sur le nom du produit et timestamp
                String fileName = produit.getNom().replaceAll("\\s+", "_").toLowerCase() + "_" + 
                                 System.currentTimeMillis() + "." + 
                                 getFileExtension(selectedImageFile.getName());
                
                // Chemin de destination dans le répertoire d'images
                Path destination = Paths.get(IMAGES_DIR, fileName);
                
                // Si ce n'est pas déjà l'image par défaut, la copier
                if (!selectedImageFile.getPath().equals(DEFAULT_IMAGE_PATH)) {
                    Files.copy(selectedImageFile.toPath(), destination, StandardCopyOption.REPLACE_EXISTING);
                    imagePath = destination.toString();
                    System.out.println("Image copiée vers: " + imagePath);
                }
            } catch (IOException e) {
                System.err.println("Erreur lors de la copie de l'image: " + e.getMessage());
                e.printStackTrace();
                imagePath = DEFAULT_IMAGE_PATH;
            }
        }
        
        produit.setImageUrl(imagePath);
        
        return true;
    }
    
    /**
     * Vérifie si l'utilisateur a confirmé l'ajout/modification du produit
     * @return true si l'utilisateur a confirmé, false sinon
     */
    public boolean estConfirme() {
        return estConfirme;
    }
    
    /**
     * Récupère le produit créé ou modifié
     * @return Le produit
     */
    public Produit getProduit() {
        return produit;
    }
    
    /**
     * Récupère l'extension d'un fichier à partir de son nom
     * @param fileName Nom du fichier
     * @return Extension du fichier (sans le point) ou "png" par défaut
     */
    private String getFileExtension(String fileName) {
        if (fileName == null || fileName.isEmpty() || !fileName.contains(".")) {
            return "png"; // Extension par défaut
        }
        
        String extension = fileName.substring(fileName.lastIndexOf(".") + 1);
        return extension.toLowerCase();
    }
}
