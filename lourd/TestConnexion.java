import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import com.gestionpharma.ConnexionDialog;

/**
 * Test du système de connexion avec gestion des tentatives et mot de passe oublié
 */
public class TestConnexion {
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }
            
            JFrame frame = new JFrame("Test Système de Connexion");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(500, 300);
            frame.setLocationRelativeTo(null);
            
            JPanel panel = new JPanel(new GridLayout(4, 1, 10, 10));
            panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
            
            JLabel titleLabel = new JLabel("Test du système de connexion BigPharma", SwingConstants.CENTER);
            titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
            
            JButton connexionButton = new JButton("Tester Connexion");
            connexionButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    testerConnexion(frame);
                }
            });
            
            JLabel infoLabel = new JLabel("<html><center>Fonctionnalités testées:<br>" +
                "• Connexion avec email/mot de passe<br>" +
                "• Gestion des tentatives échouées (max 5)<br>" +
                "• Suspension temporaire (30 minutes)<br>" +
                "• Lien 'Mot de passe oublié'<br>" +
                "• Génération de token de réinitialisation</center></html>");
            infoLabel.setHorizontalAlignment(SwingConstants.CENTER);
            
            JButton quitButton = new JButton("Quitter");
            quitButton.addActionListener(e -> System.exit(0));
            
            panel.add(titleLabel);
            panel.add(connexionButton);
            panel.add(infoLabel);
            panel.add(quitButton);
            
            frame.add(panel);
            frame.setVisible(true);
        });
    }
    
    private static void testerConnexion(JFrame parent) {
        try {
            ConnexionDialog dialog = new ConnexionDialog(parent);
            dialog.setVisible(true);
            
            if (dialog.estConnexionReussie()) {
                StringBuilder info = new StringBuilder();
                info.append("Connexion réussie !\n\n");
                info.append("ID Utilisateur: ").append(dialog.getUserId()).append("\n");
                info.append("ID Pharmacie: ").append(dialog.getPharmacieId()).append("\n");
                info.append("Nom: ").append(dialog.getNomUtilisateur()).append("\n");
                
                JOptionPane.showMessageDialog(parent, info.toString(), 
                    "Connexion Réussie", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(parent, "Connexion annulée ou échouée.", 
                    "Connexion", JOptionPane.INFORMATION_MESSAGE);
            }
            
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(parent, 
                "Erreur lors du test: " + ex.getMessage(), 
                "Erreur", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }
}
