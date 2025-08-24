package InitClass;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import javax.swing.SwingUtilities;

import views.MainFrame;

public class Initializr {

	private static final String SERVER_PATH = "http://192.168.0.178:8080";
	private static Properties properties = new Properties();
	
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MainFrame());
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
