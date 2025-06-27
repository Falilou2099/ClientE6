import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import com.gestionpharma.AjoutProduitDialog;
import com.gestionpharma.models.Produit;

/**
 * Test du support d'image dans l'ajout de produit
 */
public class TestImageProduit {
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }
            
            JFrame frame = new JFrame("Test Support Image Produit");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(400, 200);
            frame.setLocationRelativeTo(null);
            
            JPanel panel = new JPanel(new GridLayout(3, 1, 10, 10));
            panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
            
            JLabel titleLabel = new JLabel("Test du support d'image pour les produits", SwingConstants.CENTER);
            titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
            
            JButton testButton = new JButton("Tester Dialogue Ajout Produit avec Image");
            testButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    testerDialogueAjoutProduit(frame);
                }
            });
            
            JButton quitButton = new JButton("Quitter");
            quitButton.addActionListener(e -> System.exit(0));
            
            panel.add(titleLabel);
            panel.add(testButton);
            panel.add(quitButton);
            
            frame.add(panel);
            frame.setVisible(true);
        });
    }
    
    private static void testerDialogueAjoutProduit(JFrame parent) {
        try {
            AjoutProduitDialog dialog = new AjoutProduitDialog(parent);
            dialog.setVisible(true);
            
            if (dialog.estConfirme()) {
                Produit produit = dialog.getProduit();
                
                // Afficher les informations du produit créé
                StringBuilder info = new StringBuilder();
                info.append("Produit créé avec succès !\n\n");
                info.append("Nom: ").append(produit.getNom()).append("\n");
                info.append("Description: ").append(produit.getDescription()).append("\n");
                info.append("Prix de vente: ").append(produit.getPrixVente()).append(" €\n");
                info.append("Catégorie: ").append(produit.getCategorie()).append("\n");
                info.append("Stock: ").append(produit.getQuantiteStock()).append("\n");
                info.append("Image URL: ").append(produit.getImageUrl()).append("\n");
                
                JOptionPane.showMessageDialog(parent, info.toString(), 
                    "Produit Créé", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(parent, "Ajout de produit annulé.", 
                    "Annulé", JOptionPane.INFORMATION_MESSAGE);
            }
            
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(parent, 
                "Erreur lors du test: " + ex.getMessage(), 
                "Erreur", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }
}
