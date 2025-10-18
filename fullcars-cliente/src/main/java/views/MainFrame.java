package views;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

import Utils.Icons;
import controller.AppContext;
import data.service.LoginService.Role;
import data.service.LoginService.User;
import interfaces.Refreshable;
import views.components.FormFactory;
import views.components.LightTheme;

public class MainFrame extends JFrame{

	private static final long serialVersionUID = 1L;
	private CardLayout cardLayout;
    private JPanel cardPanels;
    private User user;
   
    public MainFrame() {
    	this.user = AppContext.getUser();
    	setIconImage(new ImageIcon(getClass().getResource(Icons.LOGO.getPath())).getImage());
    	setStyling();
        setTitle("Full Cars");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
        int width = gd.getDisplayMode().getWidth();
        int height = gd.getDisplayMode().getHeight();
        setSize(width - 450, height - 400);
        //setLocationRelativeTo(null);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        
        setBackground(new Color(220, 220, 220));
        setVisible(true);

        JMenuBar menuBar = new JMenuBar();
        //menuBar.setForeground(Color.WHITE);
        menuBar.setOpaque(true);
        menuBar.setPreferredSize(new Dimension(WIDTH, 50));
        menuBar.setLayout(null);
        setJMenuBar(menuBar);
 
        JButton full = new JButton(Icons.LOGO_TEXTO_BLANCO.create(120,28));
        full.addMouseListener(new ClickAdapter("DASHBOARD"));
        full.setFocusPainted(false);
        full.setBorderPainted(false); 
        full.setOpaque(true);
        full.setBackground(new Color(0,0,0,0));
        full.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        full.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                full.setBackground(new Color(0, 50, 100)); // gris hover tipo JMenu
            }
            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                full.setBackground(new Color(0,0,0,0)); // vuelve transparente
            }
        });
        
        JMenu menuProductos = new JMenu("AUTOPARTES");
        menuProductos.setIcon(Icons.CAR.create());
        menuProductos.addMouseListener(new ClickAdapter("AUTOPARTES"));

        JMenu menuClientes  = new JMenu("CLIENTES");
        menuClientes.setIcon(Icons.CUSTOMER.create());
        menuClientes.addMouseListener(new ClickAdapter("CLIENTES"));
        
        JMenu menuProveedores = new JMenu("PROVEEDORES");
        menuProveedores.setIcon(Icons.PROVIDER.create());
        menuProveedores.addMouseListener(new ClickAdapter("PROVEEDORES"));
        
        JMenu menuMovimientosStock = new JMenu("MOV. STOCK");
        menuMovimientosStock.setIcon(Icons.STOCK.create());
        menuMovimientosStock.addMouseListener(new ClickAdapter("MOV. STOCK"));
        
        JMenu menuVentas = new JMenu("VENTAS");
        menuVentas.setIcon(Icons.SALE.create());
        
        JMenuItem historySales = new JMenuItem("HISTORIAL VENTAS");
        historySales.addMouseListener(new ClickAdapter("HISTORIAL VENTAS"));
        JMenuItem formSales = new JMenuItem("NUEVA VENTA");
        formSales.addMouseListener(new ClickAdapter("NUEVA VENTA"));
        menuVentas.add(formSales);
        
        JMenu menuPurchases = new JMenu("COMPRAS");
        menuPurchases.setIcon(Icons.PURCHASE.create());

        JMenuItem historyPurchases = new JMenuItem("HISTORIAL COMPRAS");
        historyPurchases.addMouseListener(new ClickAdapter("HISTORIAL COMPRAS"));
        JMenuItem formPurchases = new JMenuItem("NUEVA COMPRA");
        formPurchases.addMouseListener(new ClickAdapter("NUEVA COMPRA"));
        menuPurchases.add(formPurchases);
        
        JMenu menuInfo = new JMenu("INFO");
        menuInfo.setIcon(Icons.CATEGORY.create());

        JMenuItem menuCategorias = new JMenuItem("CATEGORIAS");
        menuCategorias.setIcon(Icons.CATEGORY.create());
        menuCategorias.addMouseListener(new ClickAdapter("CATEGORIAS"));
        JMenuItem menuMarcas  = new JMenuItem("MARCAS");
        menuMarcas.setIcon(Icons.BRAND.create());
        menuMarcas.addMouseListener(new ClickAdapter("MARCAS"));
        
        menuInfo.add(menuCategorias);
        menuInfo.add(menuMarcas);
        
        if(user.getRole() == Role.ADMIN) {
        	menuBar.add(full);
            menuVentas.add(historySales);
            menuPurchases.add(historyPurchases);
            menuBar.add(menuMovimientosStock);
            
        }
        menuBar.add(menuProductos);
        menuBar.add(menuClientes);
        menuBar.add(menuVentas);
        menuBar.add(menuProveedores);
        menuBar.add(menuPurchases);
        menuBar.add(menuInfo);
        
        createForms();
    }

    private void createForms() {
    	cardLayout = new CardLayout();
        
        cardPanels = new JPanel(cardLayout);
        add(cardPanels, BorderLayout.CENTER);

        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                // precarga sin bloquear la UI
                if(user.getRole() == Role.ADMIN) {
                	cardPanels.add(FormFactory.createDashboard(), "DASHBOARD");
	            	cardPanels.add(FormFactory.createPurchaseHistory(), "HISTORIAL COMPRAS");
	            	cardPanels.add(FormFactory.createSalesHistory(), "HISTORIAL VENTAS");
	            	cardPanels.add(FormFactory.createStockMovementForm(), "MOV. STOCK");
                }
                cardPanels.add(FormFactory.createFormCarPart(), "AUTOPARTES");
     	        cardPanels.add(FormFactory.createCustomerForm(), "CLIENTES");
     	        cardPanels.add(FormFactory.createProviderForm(), "PROVEEDORES");
     	        cardPanels.add(FormFactory.createSalesForm(), "NUEVA VENTA");
     	        cardPanels.add(FormFactory.createPurchaseForm(), "NUEVA COMPRA");
     	        cardPanels.add(FormFactory.createBrandsForm(), "MARCAS");
     	        cardPanels.add(FormFactory.createCategoriesForm(), "CATEGORIAS");
     	        //cardPanels.add(FormFactory.createModelsForm(), "MODELOS");
                return null;
            }
        }.execute();

        
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
	}
}
