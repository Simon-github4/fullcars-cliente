package InitClass;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import javax.swing.SwingUtilities;

import views.MainFrame;

public class Initializr {

	//private static final String SERVER_PATH = "http://192.168.0.103:8080";
	private static Properties properties = new Properties();
	
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MainFrame());
        try (InputStream input = Initializr.class.getClassLoader().getResourceAsStream("config.properties")) {
            if (input == null) {
                System.out.println("❌ No se encontró el archivo config.properties");
                return;
            }
            properties.load(input);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public static String getServerUrl() {
    	return properties.getProperty("server.url");
    	//return SERVER_PATH;
    }

    public static String getApiKey() {
        return properties.getProperty("api.key");
    }
}
