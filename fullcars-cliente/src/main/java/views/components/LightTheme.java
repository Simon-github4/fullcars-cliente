package views.components;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.GraphicsEnvironment;
import java.io.IOException;
import java.io.InputStream;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.UIManager;

import com.formdev.flatlaf.FlatLightLaf;

import Utils.Icons;

public class LightTheme extends FlatLightLaf {

    public static final String NAME = "MiTemaClaro";
    public static final Color COLOR_PRIMARIO = new Color(26, 115, 232); // Azul Google
    public static final Color COLOR_SECUNDARIO = new Color(224, 224, 224); 
    public static final Color COLOR_TEXTO = Color.WHITE;
    public static final Color COLOR_TEXTO_SEC = new Color(26, 115, 232);
    public static final Color COLOR_HEADER_TABLE = new Color(64, 152, 215);     
    public static final Color COLOR_GREEN_MONEY = new Color(85, 187, 101);     

	public static final Font TITLE_FONT = getTitleFont();
	public static final Font SUBTITLE_FONT = getSubTitleFont();
    
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

    private static Font getTitleFont() {
    	Font font=null;
    	try {
			 font = Font.createFont(Font.TRUETYPE_FONT, LightTheme.class.getResourceAsStream("/fonts/Montserrat-Bold.ttf")).deriveFont(40f);
		} catch (FontFormatException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return font;
    }
    private static Font getSubTitleFont() {
    	Font font=null;
    	try {
			 font = Font.createFont(Font.TRUETYPE_FONT, LightTheme.class.getResourceAsStream("/fonts/Montserrat-Bold.ttf")).deriveFont(25f);
		} catch (FontFormatException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return font;
    }
    
    public static JPanel createTitle(String text) {
    	JPanel panel = new JPanel(new BorderLayout());
    	JLabel title = new JLabel(text, JLabel.CENTER);
    	title.setFont(TITLE_FONT);
    	title.setPreferredSize(new Dimension(Integer.MAX_VALUE, 96));
    	JLabel logo = new JLabel(Icons.LOGO.create(96, 96));
    	logo.setPreferredSize(new Dimension(96, 96));
    	
    	panel.add(logo, BorderLayout.WEST);
    	panel.add(title, BorderLayout.CENTER);
    	return panel;
    }

	public static JLabel createSubTitle(String text) {
		JLabel title = new JLabel(text, JLabel.CENTER);
    	title.setFont(SUBTITLE_FONT);
    	title.setPreferredSize(new Dimension(1, 40));
    	
    	return title;
	}
    
    public static JLabel createMessageLabel() {
    	JLabel messageLabel = new JLabel("", SwingConstants.CENTER);
        messageLabel.setForeground(Color.WHITE);
        messageLabel.setBackground(Color.RED);
        messageLabel.setFont(new Font("Montserrat", Font.BOLD, 16));
        messageLabel.setPreferredSize(new Dimension(1920,55));
        messageLabel.setMaximumSize(new Dimension(1920,100));
        return messageLabel;
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
