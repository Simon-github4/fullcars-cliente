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
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import com.formdev.flatlaf.FlatDarkLaf;

import Utils.FormFactory;
import Utils.LightTheme;

public class MainFrame extends JFrame{

	private CardLayout cardLayout;
    private JPanel cardPanels;
   
    public MainFrame() {
    	setStyling();
        setTitle("Full Cars");
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

        JMenu menuProductos = new JMenu("PRODUCTOS");//en vex de eomji icono
        JMenu menuCompras   = new JMenu("COMPRAS");
        JMenu menuVentas    = new JMenu("VENTAS");
        JMenu menuClientes  = new JMenu("CLIENTES");

        menuBar.add(menuProductos);
        menuBar.add(menuCompras);
        menuBar.add(menuVentas);
        menuBar.add(menuClientes);

        menuProductos.addMouseListener(new ClickAdapter("PRODUCTOS"));
        menuCompras.addMouseListener(new ClickAdapter("COMPRAS"));
        menuVentas.addMouseListener(new ClickAdapter("VENTAS"));
        menuClientes.addMouseListener(new ClickAdapter("CLIENTES"));
        
        cardLayout = new CardLayout();
        
        cardPanels = new JPanel(cardLayout);
        add(cardPanels, BorderLayout.CENTER);
        
        cardPanels.add(FormFactory.createFormCarPart(), "PRODUCTOS");
        cardPanels.add(crearPanel("🛒 Panel de Compras", new Color(232, 245, 233)), "COMPRAS");
        cardPanels.add(crearPanel("💵 Panel de Ventas", new Color(255, 243, 224)), "VENTAS");
        cardPanels.add(crearPanel("👥 Panel de Clientes", new Color(255, 236, 239)), "CLIENTES");

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
