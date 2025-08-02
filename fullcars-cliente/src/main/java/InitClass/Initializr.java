package InitClass;

import javax.swing.SwingUtilities;

import com.formdev.flatlaf.FlatLaf;

import views.MainFrame;
import views.components.LightTheme;


public class Initializr {


    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MainFrame().setVisible(true));
    }
    
}
