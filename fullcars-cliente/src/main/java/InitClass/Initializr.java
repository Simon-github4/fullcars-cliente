package InitClass;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import views.MainFrame;
import views.components.LightTheme;

public class Initializr {

	private static final String SERVER_PATH = "http://192.168.0.103:8080";
	private static Properties properties = new Properties();
	
    public static void main(String[] args) {
		SwingUtilities.invokeLater(() -> {
			try {
				UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
				//LightTheme.setup();
			} catch (Exception e) {
				e.printStackTrace();
			}
			new MainFrame();
		});
		try (FileInputStream input = new FileInputStream("config.properties")) {
			properties.load(input);
		} catch (IOException e) {
			System.out.println("❌ No se encontró el archivo config.properties");
			e.printStackTrace();
		}
    }
    
    public static String getServerUrl() {
    	return properties.getProperty("server.url", SERVER_PATH);
    	//return SERVER_PATH;
    }

    public static String getApiKey() {
        return properties.getProperty("api.key");
    }
}
