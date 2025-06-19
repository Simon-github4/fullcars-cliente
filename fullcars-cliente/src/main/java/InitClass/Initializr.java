package InitClass;

import javax.swing.SwingUtilities;

import com.formdev.flatlaf.FlatLaf;

import Utils.LightTheme;
import views.MainFrame;


public class Initializr {


    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MainFrame().setVisible(true));
    }
    
}
