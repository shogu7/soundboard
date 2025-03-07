package shogu;
import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Soundboard soundboard = new Soundboard(); 
            soundboard.setVisible(true);               
            soundboard.fileManager();                 
        });
    }
}