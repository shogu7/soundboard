package shogu;
import java.awt.Graphics;
import java.awt.Image;
import java.util.logging.Logger;

import javax.swing.ImageIcon;
import javax.swing.JPanel;

public class BackgroundPanel extends JPanel {
    private Image backgroundImage;
    private static final Logger LOGGER = Logger.getLogger(BackgroundPanel.class.getName());

    public BackgroundPanel(String imagePath) {
        try {
            backgroundImage = new ImageIcon("C:\\Users\\hugob\\Documents\\Code\\Soundboard_Maven\\soundboard\\src\\main\\java\\resources\\picture4.jpg").getImage();
            if (backgroundImage == null) {
                System.out.println("Erreur de chargement de l'image : " + imagePath);
            } else {
                System.out.println("Background succefuly upload : " + imagePath);
            }
        } catch (Exception e) {
            System.out.println("Erreur in the background");
            LOGGER.log(java.util.logging.Level.SEVERE, "Error in audio file.", e);
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (backgroundImage != null) {
            g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
        } else {
            System.out.println("No background found");
        }
    }
}
