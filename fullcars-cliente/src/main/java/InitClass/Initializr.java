package InitClass;

import javax.swing.SwingUtilities;
import views.MainFrame;

public class Initializr {


    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MainFrame().setVisible(true));
    }
    
}
