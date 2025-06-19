package Utils;

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLightLaf;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.io.InputStream;

public class LightTheme extends FlatLightLaf {

    public static final String NAME = "MiTemaOscuro";
    private static final Color COLOR_PRIMARIO = new Color(26, 115, 232); // Azul Google
    private static final Color COLOR_TEXTO = Color.WHITE;

    private static final Color COLOR_SECUNDARIO = new Color(224, 224, 224); 
    private static final Color COLOR_TEXTO_SEC = new Color(26, 115, 232);
	private static final Color COLOR_HEADER_TABLE = new Color(64, 152, 215);     

    public static boolean setup() {
        return setup(new LightTheme());
    }

    public static void aplicarEstiloPrimario(JButton boton) {
        boton.setBackground(COLOR_PRIMARIO);
        boton.setForeground(COLOR_TEXTO);
        boton.setFocusPainted(false);
        //boton.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));
        boton.setFont(boton.getFont().deriveFont(Font.BOLD));
        boton.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    public static void aplicarEstiloSecundario(JButton boton) {
        boton.setBackground(Color.white);
        boton.setForeground(COLOR_TEXTO_SEC);
        boton.setFocusPainted(false);
        //boton.setBorder(BorderFactory.createLineBorder(new Color(180, 180, 180)));
        boton.setFont(boton.getFont().deriveFont(Font.PLAIN));
        boton.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }
    
    @Override
    public String getName() {
        return NAME;
    }

    public LightTheme() {
        super();
        InputStream is = getClass().getResourceAsStream("/fonts/Roboto-Medium.ttf");
        Font roboto = null;
		try {
			roboto = Font.createFont(Font.TRUETYPE_FONT, is).deriveFont(14f);
		} catch (FontFormatException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
        GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(roboto);
        UIManager.put("defaultFont", roboto);
       
        UIManager.put("Component.arrowType", "chevron");      
        UIManager.put("TableHeader.background", COLOR_HEADER_TABLE);
        UIManager.put("TableHeader.foreground", Color.WHITE);
		UIManager.put("Button.arc", 15);		
		UIManager.put("Button.foreground", Color.WHITE);		
		UIManager.put("Button.background", COLOR_PRIMARIO);	//new Color(157, 201, 255)	
		//UIManager.put("Button.borderColor", COLOR_HEADER_TABLE);	//new Color(157, 201, 255)	
		UIManager.put("TextComponent.arc", 10);		
		UIManager.put("Component.arc", 10);		
		//UIManager.put("Component.innerFocusWidth", 0.001f);		
		UIManager.put("Table.alternateRowColor", new Color(225,225,225));
        UIManager.put("Panel.background", new Color(225,225,225));
        //UIManager.put("SplitPane.dividerColor", new Color(52, 58, 64)); // Color del divisor

    }
}
