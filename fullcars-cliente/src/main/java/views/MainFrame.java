package views;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;

import Utils.Icons;
import interfaces.Refreshable;
import views.components.FormFactory;
import views.components.LightTheme;

public class MainFrame extends JFrame{

	private static final long serialVersionUID = 1L;
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
        menuProductos.addMouseListener(new ClickAdapter("PRODUCTOS"));

        JMenu menuCategorias = new JMenu("CATEGORIAS");
        menuCategorias.addMouseListener(new ClickAdapter("CATEGORIAS"));

        JMenu menuMarcas  = new JMenu("MARCAS");
        menuMarcas.addMouseListener(new ClickAdapter("MARCAS"));

        JMenu menuClientes  = new JMenu("CLIENTES");
        menuClientes.addMouseListener(new ClickAdapter("CLIENTES"));
        
        JMenu menuProveedores = new JMenu("PROVEEDORES");
        menuProveedores.addMouseListener(new ClickAdapter("PROVEEDORES"));
        
        JMenu menuMovimientosStock = new JMenu("MOV. STOCK");
        menuMovimientosStock.addMouseListener(new ClickAdapter("MOV. STOCK"));
        
        JMenu menuVentas = new JMenu("VENTAS");
        
        JMenuItem historySales = new JMenuItem("HISTORIAL VENTAS");
        historySales.addMouseListener(new ClickAdapter("HISTORIAL VENTAS"));
        menuVentas.add(historySales);
        JMenuItem formSales = new JMenuItem("NUEVA VENTA");
        formSales.addMouseListener(new ClickAdapter("NUEVA VENTA"));
        menuVentas.add(formSales);
        
        JMenu menuPurchases = new JMenu("COMPRAS");
        
        JMenuItem historyPurchases = new JMenuItem("HISTORIAL COMPRAS");
        historyPurchases.addMouseListener(new ClickAdapter("HISTORIAL COMPRAS"));
        menuPurchases.add(historyPurchases);
        JMenuItem formPurchases = new JMenuItem("NUEVA COMPRA");
        formPurchases.addMouseListener(new ClickAdapter("NUEVA COMPRA"));
        menuPurchases.add(formPurchases);
        
        menuBar.add(menuProductos);
        menuBar.add(menuClientes);
        menuBar.add(menuProveedores);
        menuBar.add(menuMovimientosStock);
        menuBar.add(menuCategorias);
        menuBar.add(menuMarcas);
        menuBar.add(menuVentas);
        menuBar.add(menuPurchases);
        
        cardLayout = new CardLayout();
        
        cardPanels = new JPanel(cardLayout);
        add(cardPanels, BorderLayout.CENTER);
        
        cardPanels.add(FormFactory.createFormCarPart(), "PRODUCTOS");
        cardPanels.add(FormFactory.createCategoriesForm(), "CATEGORIAS");
        cardPanels.add(FormFactory.createBrandsForm(), "MARCAS");
        cardPanels.add(FormFactory.createCustomerForm(), "CLIENTES");
        cardPanels.add(FormFactory.createProviderForm(), "PROVEEDORES");
        cardPanels.add(FormFactory.createStockMovementForm(), "MOV. STOCK");
        cardPanels.add(FormFactory.createSalesHistory(), "HISTORIAL VENTAS");
        cardPanels.add(FormFactory.createSalesForm(), "NUEVA VENTA");
        cardPanels.add(FormFactory.createPurchaseHistory(), "HISTORIAL COMPRAS");
        cardPanels.add(FormFactory.createPurchaseForm(), "NUEVA COMPRA");
        
    }

    private class ClickAdapter extends MouseAdapter {
        private final String panelNombre;

        public ClickAdapter(String panelNombre) {
            this.panelNombre = panelNombre;
        }

        @Override
        public void mousePressed(MouseEvent e) {
        	cardLayout.show(cardPanels, panelNombre);

            for (Component comp : cardPanels.getComponents()) 
                if (comp.isVisible() && comp instanceof Refreshable )
                	((Refreshable)comp).refresh();
        }
    }
    
    private void setStyling() {
    	LightTheme.setup();
    	setIconImage(new ImageIcon(getClass().getResource(Icons.LOGO.getPath())).getImage());
	}
}
