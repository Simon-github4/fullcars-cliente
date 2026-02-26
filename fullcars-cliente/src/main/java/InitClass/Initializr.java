package InitClass;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import controller.AppContext;
import data.service.LoginService.Role;
import data.service.LoginService.User;

import views.LoginView;
import views.MainFrame;

public class Initializr {

	private static final String SERVER_PATH = "http://192.168.0.10:8080";
	private static Properties properties = new Properties();
	
    public static void main(String[] args) {
		SwingUtilities.invokeLater(() -> {
			try {
				UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
				//LightTheme.setup();
				new LoginView();
				//Initializr.launch(new User("walterlucas", "fullcontra", Role.ADMIN));
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
		loadProperties();		
    }
    
    private static void loadProperties() {
		try {
	//try (FileInputStream input = new FileInputStream("config.properties"))  si el archivo esta en misma carpeta, NO con jpackage
		    // Obtiene el path del .exe o .jar
		    String path = new File(Initializr.class.getProtectionDomain()
		            .getCodeSource().getLocation()
		            .toURI()).getParent();
		
		    File configFile = new File(path, "config.properties");
		
		    try (FileInputStream input = new FileInputStream(configFile)) {
		        properties.load(input);
		        System.out.println("✅ Config cargado desde: " + configFile.getAbsolutePath());
		    }
		} catch (Exception e) {
		    System.out.println("❌ No se encontró el archivo config.properties");
		    e.printStackTrace();
		}		
	}
    
	public static void launch(User u) {
		AppContext.setUser(u);
    	new MainFrame();
    }
    public static String getServerUrl() {
    	return properties.getProperty("server.url", SERVER_PATH);
    	//return SERVER_PATH;
    }

    public static String getApiKey() {
        return properties.getProperty("api.key");
    }
}
