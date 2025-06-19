package views;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import Utils.LightTheme;
import controller.BrandController;
import controller.CarPartController;

public class MainFrame extends JFrame{

	private CardLayout cardLayout;
    private JPanel cardPanels;
    private static final CarPartController CAR_PART_CONTROLLER = new CarPartController();
    private static final BrandController BRAND_CONTROLLER = new BrandController();
    
    
    public MainFrame() {
    	setStyling();
        //setTitle("Full Cars");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
        int width = gd.getDisplayMode().getWidth();
        int height = gd.getDisplayMode().getHeight();
        setSize(width - 450, height - 400);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        
        setLocationRelativeTo(null);
        setBackground(new Color(220, 220, 220));

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
    	LightTheme.setup();
	}
}
