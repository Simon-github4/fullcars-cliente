package views;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.GraphicsEnvironment;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.io.InputStream;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.UIManager;

import com.formdev.flatlaf.FlatLightLaf;

import controller.BrandController;
import controller.CarPartController;

public class MainFrame extends JFrame{

	private CardLayout cardLayout;
    private JPanel cardPanels;
    private static final CarPartController CAR_PART_CONTROLLER = new CarPartController();
    private static final BrandController BRAND_CONTROLLER = new BrandController();
    
    
    public MainFrame() {
        setTitle("Full Cars");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setSize(1820, 1000);
        setLocationRelativeTo(null);
        setBackground(new Color(220, 220, 220));
        setStyling();

        JMenuBar menuBar = new JMenuBar();
        menuBar.setBackground(Color.white);
        menuBar.setOpaque(true);
        menuBar.setPreferredSize(new Dimension(WIDTH, 50));
        setJMenuBar(menuBar);

        JMenu menuProductos = crearMenu("PRODUCTOS", "📦");//en vex de eomji icono
        JMenu menuCompras   = crearMenu("COMPRAS", "🛒");
        JMenu menuVentas    = crearMenu("VENTAS", "💵");
        JMenu menuClientes  = crearMenu("CLIENTES", "👥");

        menuBar.add(menuProductos);
        menuBar.add(menuCompras);
        menuBar.add(menuVentas);
        menuBar.add(menuClientes);

        cardLayout = new CardLayout();
        
        cardPanels = new JPanel(cardLayout);
        add(cardPanels, BorderLayout.CENTER);
        //cardPanels.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        
        cardPanels.add(new FormCarPart(CAR_PART_CONTROLLER, BRAND_CONTROLLER), "PRODUCTOS");
        cardPanels.add(crearPanel("🛒 Panel de Compras", new Color(232, 245, 233)), "COMPRAS");
        cardPanels.add(crearPanel("💵 Panel de Ventas", new Color(255, 243, 224)), "VENTAS");
        cardPanels.add(crearPanel("👥 Panel de Clientes", new Color(255, 236, 239)), "CLIENTES");

        menuProductos.addMouseListener(new ClickAdapter("PRODUCTOS"));
        menuCompras.addMouseListener(new ClickAdapter("COMPRAS"));
        menuVentas.addMouseListener(new ClickAdapter("VENTAS"));
        menuClientes.addMouseListener(new ClickAdapter("CLIENTES"));
    }

    private JMenu crearMenu(String texto, String emoji) {
        JMenu menu = new JMenu(emoji + " " + texto);
        return menu;
    }

    private JPanel crearPanel(String texto, Color colorFondo) {
        JPanel panel = new JPanel(new BorderLayout(0,0));
        panel.setBackground(colorFondo);
        panel.setBorder(BorderFactory.createLineBorder(new Color(230, 230, 230)));
        
        JLabel label = new JLabel(texto, SwingConstants.CENTER);
        label.setFont(new Font("Montserrat", Font.BOLD, 24));
        panel.add(label, BorderLayout.CENTER);

        return panel;
    }

    private class ClickAdapter extends MouseAdapter {
        private final String panelNombre;

        public ClickAdapter(String panelNombre) {
            this.panelNombre = panelNombre;
        }

        @Override
        public void mouseClicked(MouseEvent e) {
            cardLayout.show(cardPanels, panelNombre);
        }
    }
    
    private void setStyling() {
    	FlatLightLaf.setup();	
    	
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
       
        UIManager.put("TableHeader.background", new Color(52, 58, 64));
        UIManager.put("TableHeader.foreground", Color.WHITE);


		UIManager.put("Button.arc", 15);		
		//UIManager.put("Button.font", new Font("Montserrat", Font.PLAIN, 16));		
		UIManager.put("Button.foreground", Color.WHITE);		
		UIManager.put("Button.background", new Color(84, 173, 253 ));	//new Color(157, 201, 255)	
		//UIManager.put("Button.borderColor", new Color(84, 173, 253));				

		UIManager.put("TextComponent.arc", 10);		
		UIManager.put("Component.arc", 10);		
		//UIManager.put("Component.innerFocusWidth", 0.001f);		
		UIManager.put("Table.alternateRowColor", new Color(225,225,225));
	    
        UIManager.put("Panel.background", new Color(225,225,225));

		//((JPanel) this.getContentPane()).setBorder(BorderFactory.createLineBorder(Color.GRAY, 3, true)); // Soft shadow
	}
}
