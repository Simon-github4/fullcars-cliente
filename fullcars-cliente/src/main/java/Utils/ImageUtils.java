package Utils;

import java.awt.Image;
import java.net.URL;

import javax.swing.AbstractButton;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;

public class ImageUtils {

	public static void setIconToButton(JButton button, String iconFile, int width, int height) {
        URL url = ImageUtils.class.getResource(iconFile);
        if (url != null) {
            ImageIcon icon = new ImageIcon(url);
            Image image = icon.getImage();
            Image resizedImage = image.getScaledInstance(width, height, Image.SCALE_SMOOTH);
            ImageIcon resizedIcon = new ImageIcon(resizedImage);
            button.setIcon(resizedIcon);
        } else {
            System.err.println("No se pudo encontrar la imagen en la ruta especificada");
        }
        button.setHorizontalTextPosition(AbstractButton.LEFT);
        //button.setHorizontalAlignment(AbstractButton.CENTER);
    }
    
    public static void setIconToLabel(JLabel label, String iconFile, int width, int height) {
        URL url = ImageUtils.class.getResource(iconFile);
        if (url != null) {
            ImageIcon icon = new ImageIcon(url);
            Image image = icon.getImage();
            Image resizedImage = image.getScaledInstance(width, height, Image.SCALE_SMOOTH);
            ImageIcon resizedIcon = new ImageIcon(resizedImage);
            label.setIcon(resizedIcon);
        } else {
            System.err.println("No se pudo encontrar la imagen en la ruta especificada");
        }
        //label.setHorizontalTextPosition(AbstractButton.LEFT);
        label.setHorizontalAlignment(AbstractButton.CENTER);
    }
}
