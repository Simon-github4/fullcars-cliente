package views.components;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.GraphicsEnvironment;
import java.awt.Insets;
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

	private static final long serialVersionUID = 150295267894992027L;
	
	public static final String NAME = "MiTemaClaro";
    public static final Color COLOR_PRIMARIO = new Color(26, 115, 232); // Azul Google
    public static final Color COLOR_SECUNDARIO = new Color(224, 224, 224); //gris fondo
    public static final Color COLOR_TEXTO = Color.WHITE;
    public static final Color COLOR_HEADER_TABLE = new Color(64, 152, 215);//celestito  
    public static final Color COLOR_GREEN_MONEY = new Color(85, 187, 101);     
    public static final Color COLOR_AZUL_CLARITO = new Color(84, 173, 253 );//alsinaApp botones     
    public static final Color COLOR_AZUL_FIRME = new Color(12,81,169);//boceto benja     

	public static final Font TITLE_FONT = getTitleFont();
	public static final Font SUBTITLE_FONT = getSubTitleFont();
    
	public static boolean setup() {
        return setup(new LightTheme());
    }

    public static void aplicarEstiloPrimario(JButton boton) {
        boton.setOpaque(true);
        boton.setFocusPainted(false);
        boton.setBackground(COLOR_PRIMARIO);
        boton.setForeground(COLOR_TEXTO);
        //boton.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));
        boton.setFont(boton.getFont().deriveFont(Font.BOLD));
        boton.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    public static void aplicarEstiloSecundario(JButton boton) {
        boton.setBackground(Color.white);
        boton.setForeground(Color.DARK_GRAY);
        boton.setOpaque(true);
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
        try (InputStream fontStream = LightTheme.class.getResourceAsStream("/resources/imgs/fonts/Montserrat-Bold.ttf")) {
            
            if (fontStream == null) {
                System.err.println("No se encontró el archivo de fuente: /fonts/Montserrat-Bold.ttf");
                return UIManager.getFont("Label.font").deriveFont(Font.BOLD, 40f);
            }

            Font customFont = Font.createFont(Font.TRUETYPE_FONT, fontStream).deriveFont(40f);
            GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(customFont);
            return customFont;

        } catch (FontFormatException | IOException e) {
            e.printStackTrace();
            return UIManager.getFont("Label.font").deriveFont(Font.BOLD, 40f);
        }
    }

    public static Font getSubTitleFont() {
    	Font font=null;
    	try {
			 font = Font.createFont(Font.TRUETYPE_FONT, LightTheme.class.getResourceAsStream("/resources/imgs/fonts/Montserrat-Bold.ttf")).deriveFont(25f);
		} catch (FontFormatException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
			return UIManager.getFont("Label.font").deriveFont(Font.BOLD, 40f);
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
    	
    	//panel.add(logo, BorderLayout.EAST);
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
        InputStream is = getClass().getResourceAsStream("/resources/imgs/fonts/Roboto-Medium.ttf");
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
		UIManager.put("Button.background", COLOR_AZUL_FIRME);	//new Color(157, 201, 255)	
		//UIManager.put("Button.borderColor", COLOR_HEADER_TABLE);	//new Color(157, 201, 255)	
		UIManager.put("TextComponent.arc", 10);		
		UIManager.put("Component.arc", 10);		
		//UIManager.put("Component.innerFocusWidth", 0.001f);		
		UIManager.put("Table.alternateRowColor", COLOR_SECUNDARIO);
        UIManager.put("Panel.background", COLOR_SECUNDARIO);
        //UIManager.put("SplitPane.dividerColor", new Color(52, 58, 64)); // Color del divisor
        
        UIManager.put("TabbedPane.tabType", "card");
        UIManager.put("TabbedPane.tabsOverlapBorder", true);
        UIManager.put("TabbedPane.hasFullBorder", true);
        UIManager.put("TabbedPane.selectedBackground", Color.WHITE);
        UIManager.put("TabbedPane.selectedForeground", Color.BLACK);
        UIManager.put("TabbedPane.foreground", Color.DARK_GRAY);
        UIManager.put("TabbedPane.hoverColor", new Color(240, 240, 240));
        UIManager.put("TabbedPane.tabInsets", new Insets(10, 20, 10, 20)); // padding
        UIManager.put("TabbedPane.showTabSeparators", true);
        UIManager.put("TabbedPane.tabSeparatorsFullHeight", true);
        //UIManager.put("TabbedPane.tabAreaAlignment", "fill"); fill para que ocupe todo el ancho
        UIManager.put("TabbedPane.tabAlignment", "center");
        UIManager.put("TabbedPane.tabWidthMode", "equal");
        
        UIManager.put("MenuBar.background", COLOR_AZUL_FIRME);
        UIManager.put("MenuBar.foreground", Color.white);
        UIManager.put("MenuItem.background", COLOR_AZUL_FIRME);
        UIManager.put("MenuItem.foreground", Color.white);
        UIManager.put("PopupMenu.background", COLOR_AZUL_FIRME);
        UIManager.put("PopupMenu.foreground", Color.white);
        //UIManager.put("MenuItem.selectionBackground", java.awt.Color.decode("#eeeeee"));
        //UIManager.put("MenuItem.selectionForeground", java.awt.Color.decode("#000000"));

    }

}
