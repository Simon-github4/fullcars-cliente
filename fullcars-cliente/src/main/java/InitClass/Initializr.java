package InitClass;

import javax.swing.SwingUtilities;
import views.MainFrame;

public class Initializr {

	private static final String IP_PORT = "192.168.0.178:8080";
	
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MainFrame().setVisible(true));
    }
    
    public static String getIpAndPort() {
    	return IP_PORT;
    }
}
