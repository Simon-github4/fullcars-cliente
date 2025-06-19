package views;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;

import Utils.LightTheme;
import Utils.TypedComboBox;
import controller.CarPartController;
import interfaces.IBrandProvider;
import model.client.entities.Brand;
import model.client.entities.CarPart;
import model.client.entities.Category;


public class FormCarPart extends JPanel{
	
private static final long serialVersionUID = 1L;
	
	private final CarPartController controller;
	private final IBrandProvider brandProvider;
	
	private JPanel inputPanel;
	private JPanel tablePanel;
	private Object[] columns = {"Nombre", "Descripcion", "SKU", "Stock", "Marca", "Categoria", "id"};
	private JTable table;
	private DefaultTableModel tableModel;
	private JLabel messageLabel;
	private JButton toggleButton = new JButton("Mostrar formulario");
	private JButton searchButton = new JButton("Buscar", Utils.Icons.LENS.create(18,18));
	
	private JTextField nameTextField = new JTextField("", 29);;
	private JTextField stockTextField = new JTextField("", 29);;
	private TypedComboBox<Brand> comboBoxBrands = new TypedComboBox<Brand>();
	private TypedComboBox<Category> comboBoxCategory = new TypedComboBox<Category>();
	private JTextField descriptionTextField = new JTextField("", 29);;
	private JTextField skuTextField = new JTextField(29);
	private JTextField skuSearchTextField = new JTextField("", 15);;
	

		public FormCarPart(CarPartController controller, IBrandProvider brandProvider) {
			this.brandProvider = brandProvider;
			this.controller = controller;

		    setLayout(new BorderLayout(0, 0));
		    createInputPanel();	
		    createTablePanel();
		    createMessageLabel();
		    createJPopupMenu();
			initUI();
			
			fillBrands();
			comboBoxCategory.addItem(new Category((long) 0, "selecione cateogria"));
			Object[] row= {"ss", "ss", "ss", "ss", "ss", "ss", "ss"}; 
			tableModel.addRow(row);
			//loadTable();
			
		}
		
		private void save() {			  //TO-DO : Personalize
			//long stock = Long.parseLong( is not changed from the view
			CarPart m = CarPart.builder()
					.brand(comboBoxBrands.getSelectedItem())
					.category(comboBoxCategory.getSelectedItem())
					.name(nameTextField.getText())
					.description(descriptionTextField.getText())
					.sku(skuTextField.getText())
					.build();

			if(table.getSelectedRow() != -1) {
				long id = (Long)tableModel.getValueAt(table.getSelectedRow(), table.getColumnModel().getColumnIndex((String)"id"));
				m.setId(id);
			}//PODRIA DIFERNCIAR EL INSERT DEL UPDATE LLAMANDO A PUT EN VEZ DE POST
			if(controller.save(m)) {	
				clearFields();
				loadTable();
			}else
				setMessage("No se pudo Guardar");
		}
		
		private void delete() {			  //TO-DO : Personalize
			String sku = (String)tableModel.getValueAt(table.getSelectedRow(), table.getColumnModel().getColumnIndex((String)"SKU"));
			try {
				if(JOptionPane.showConfirmDialog(null, "Desea eliminar al producto con el sku: "+sku,  "", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION){
					Long id = (Long)tableModel.getValueAt(table.getSelectedRow(), table.getColumnModel().getColumnIndex((String)"id"));
					controller.delete(id);
					clearFields();
					loadTable();
				}
			}catch(Exception e) {
				setMessage(e.getCause().getMessage());
			}
		}
		
		private void loadTable() {		  //TO-DO : Personalize
	        tableModel.setRowCount(0);

			List<CarPart> carParts = controller.getCarParts();
			for(CarPart c : carParts) {
				Object[] row = {c.getName(), c.getDescription(), c.getSku(), c.getStock(), c.getBrand(), c.getCategory(), c.getId()};
				tableModel.addRow(row);
			}
		}

		private void initUI() {	   		  //TO-DO : Personalize
		    searchButton.addActionListener(e -> loadTable());
		    
		    JPanel north = new JPanel(new BorderLayout());
			JLabel titulo = new JLabel("Gestion de Auto Partes", JLabel.CENTER);
			try {
				titulo.setFont(Font.createFont(Font.TRUETYPE_FONT, getClass().getResourceAsStream("/fonts/Montserrat-Bold.ttf")).deriveFont(40f));
				//titulo.setOpaque(true); // Habilita el fondo
				//titulo.setForeground(new Color(64, 152, 215)); 
				//titulo.setForeground(Color.white);
				titulo.setPreferredSize(new Dimension(WIDTH, 70));
				//titulo.setBorder(BorderFactory.createEtchedBorder());
			} catch (FontFormatException e1) {
				e1.printStackTrace();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			
			JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
			filterPanel.add(new JLabel("Filtrar por SKU", JLabel.RIGHT));
			//filterPanel.setMaximumSize(new Dimension(WIDTH, 60));
			//filterPanel.setPreferredSize(new Dimension(WIDTH, 50));
			filterPanel.add(skuSearchTextField);
			filterPanel.add(new JLabel("Categoría:", JLabel.RIGHT));
			filterPanel.add(new JTextField(15));
			filterPanel.add(searchButton);
			filterPanel.add(toggleButton);
			LightTheme.aplicarEstiloPrimario(searchButton);
			LightTheme.aplicarEstiloSecundario(toggleButton);
			filterPanel.setBorder(BorderFactory.createCompoundBorder(
				    BorderFactory.createEmptyBorder(0, 10, 10, 10),  // 👉 Margen EXTERNO al borde con título
				    BorderFactory.createTitledBorder(
				        BorderFactory.createEtchedBorder(EtchedBorder.LOWERED),
				        "Filtros",
				        TitledBorder.LEFT,
				        TitledBorder.TOP,
				        new Font("Montserrat-Medium", Font.PLAIN, 14), // Nota: TRUETYPE_FONT no es correcto aquí
				        Color.BLACK
				    )
				));
			
			north.add(titulo, BorderLayout.NORTH);		
			north.add(filterPanel, BorderLayout.SOUTH);
			
			add(north, BorderLayout.NORTH);	
			
//--------------------------------------------------------CENTER PANEL-------------------------------------------------(NO se personaliza)
			JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, tablePanel, inputPanel);
	        splitPane.setResizeWeight(1); // El panel de arriba no se estira al redimensionar

	        toggleButton.addActionListener(e -> {
	            if (inputPanel.isVisible()) {
	                inputPanel.setVisible(false);
	                toggleButton.setText("Mostrar formulario");
	                splitPane.setDividerSize(0);
	            } else {
	                inputPanel.setVisible(true);
	                toggleButton.setText("Ocultar formulario");
	                splitPane.setDividerSize(8);
	                SwingUtilities.invokeLater(() -> {
	                	splitPane.setDividerLocation(splitPane.getWidth() - inputPanel.getPreferredSize().width);
	                });
	            }
	            splitPane.revalidate();
	        });
	        //splitPane.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
	        add(splitPane, BorderLayout.CENTER);
//--------------------------------------------------------CENTER PANEL------------------------------------------------------------
		}

		private void createInputPanel() { //TO-DO : Personalize
			inputPanel = new JPanel();
			inputPanel.setLayout(new BoxLayout(inputPanel, BoxLayout.Y_AXIS));
			inputPanel.setVisible(false);
			Dimension size = new Dimension(450, HEIGHT);
			inputPanel.setMinimumSize(new Dimension(380, HEIGHT));
			inputPanel.setPreferredSize(size);
			//add(inputPanel, BorderLayout.EAST);		

			JPanel horizontalPanel = new JPanel(new GridLayout(0,1));

			//horizontalPanel = new JPanel(new GridLayout());
			horizontalPanel.add(new JLabel("  Nombre", JLabel.LEFT));
			horizontalPanel.add(nameTextField);
			//inputPanel.add(horizontalPanel);		
			
			//horizontalPanel = new JPanel(new FlowLayout());
			horizontalPanel.add(new JLabel("  Descripcion", JLabel.LEFT));
			horizontalPanel.add(descriptionTextField);
			//inputPanel.add(horizontalPanel);		
			
			//horizontalPanel = new JPanel(new FlowLayout());
			JPanel grid = new JPanel(new GridLayout(1,0));
			grid.add(new JLabel("  Sku", JLabel.LEFT));
			grid.add(new JLabel("  Marca", JLabel.LEFT));
			horizontalPanel.add(grid);
			//inputPanel.add(horizontalPanel);		
			
			//horizontalPanel = new JPanel(new FlowLayout());
			grid = new JPanel(new GridLayout(1,0));
			grid.add(skuTextField);		
			grid.add(comboBoxBrands);
			horizontalPanel.add(grid);
			//inputPanel.add(horizontalPanel);		
			
			//horizontalPanel = new JPanel(new FlowLayout());
			horizontalPanel.add(new JLabel("  Stock", JLabel.LEFT));
			stockTextField.setEditable(false);
			horizontalPanel.add(stockTextField);
			//inputPanel.add(horizontalPanel);		
			
			//horizontalPanel = new JPanel(new FlowLayout());
			horizontalPanel.add(new JLabel("  Categoria", JLabel.LEFT));
			//comboBoxCategory.setShowClearButton(true);
			//categoryTextField.putClientProperty(FlatClientProperties.STYLE, "showRevealButton:true");
			horizontalPanel.add(comboBoxCategory);		
			
			inputPanel.add(horizontalPanel);
			
			JPanel buttonsPanel = new JPanel(new BorderLayout());
			buttonsPanel.setMaximumSize(new Dimension(500, 160));

			JPanel firsts = new JPanel(new GridLayout());
			JButton confirm = new JButton("Confirmar");
			confirm.addActionListener(e ->{
				if(validateFields())
					save();
			});
			LightTheme.aplicarEstiloPrimario(confirm);
			JButton delete = new JButton("Eliminar");
			delete.addActionListener(e ->{ 
				if(validateFields() && table.getSelectedRow() != -1)
					delete();
			});
			LightTheme.aplicarEstiloPrimario(delete);
			delete.setBackground(Color.red);

			delete.setPreferredSize(new Dimension(120, 70));
			confirm.setPreferredSize(new Dimension(120, 70));
			firsts.add(confirm);
			firsts.add(delete);
			buttonsPanel.add(firsts, BorderLayout.NORTH);

			//inputPanel.add(horizontalPanel);
			horizontalPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
			JButton cancel = new JButton("Cancelar");
			cancel.addActionListener(e -> clearFields());
			cancel.setPreferredSize(new Dimension(250, 70));
			LightTheme.aplicarEstiloSecundario(cancel);
			horizontalPanel.add(cancel);		
			
			buttonsPanel.add(horizontalPanel, BorderLayout.SOUTH);
			inputPanel.add(buttonsPanel);		
		}

		private void createTablePanel() { //TO-DO : Personalize
			tableModel = new DefaultTableModel(columns, 0){
				@Override
	            public boolean isCellEditable(int row, int column) {
	                return false;
	            }
				@Override
	            public Class<?> getColumnClass(int column) {
	                switch (column) {
	                    case 0: return String.class;
	                    case 1: return String.class;
	                    case 2: return String.class;
	                    case 3: return String.class;
	                    case 4: return Long.class;
	                    case 5: return Brand.class;
	                    case 6: return Category.class;
	                    case 7: return Long.class;
	                    default:return Object.class;
	                }
	            }
	        };
	        tableModel.setColumnIdentifiers(columns);
	        
			table = new JTable(tableModel) {
				@Override
				public String getToolTipText(MouseEvent e) {
				       return "Click Derecho para Eliminar o Modificar";
				}
			};
			table.getColumnModel().getColumn(table.getColumnCount()-1).setMaxWidth(0);
			table.getColumnModel().getColumn(table.getColumnCount()-1).setMinWidth(0);
			table.getColumnModel().getColumn(table.getColumnCount()-1).setPreferredWidth(0);
			table.setShowGrid(true);
			tablePanel = new JPanel();
			tablePanel.setLayout(new BoxLayout(tablePanel, BoxLayout.Y_AXIS));
			tablePanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 5));
			tablePanel.add(new JScrollPane(table));				
		}

		private void setRowToEdit() {	  //TO-DO : Personalize
           	int row = table.getSelectedRow();
           	if(row != -1){
				String name = (String) tableModel.getValueAt(row, table.getColumnModel().getColumnIndex((String)"Nombre"));
				String description = (String) tableModel.getValueAt(row, 1);
				String sku = (String) tableModel.getValueAt(row, 2);
				Long stock = (Long) tableModel.getValueAt(row, 3);
				Brand brand = (Brand) tableModel.getValueAt(row, 4);
				Category category = (Category) tableModel.getValueAt(row, 5);
					
				nameTextField.setText(name);
				descriptionTextField.setText(description);
				skuTextField.setText(sku);
				stockTextField.setText(stock.toString());
				comboBoxCategory.setSelectedItem(category);
				comboBoxBrands.setSelectedItem(brand);
					
				if(!inputPanel.isVisible())
					toggleButton.doClick();
            }
		}
		
		private boolean validateFields() {//TO-DO : Personalize
			if(nameTextField.getText().isBlank()) {
				setMessage("El nombre no puede estar vacio");
				return false;
			}
			if(nameTextField.getText().length() > 45) {
				setMessage("El nombre no puede superar los 45 caracteres");
				return false;
			}
			return true;	
		}

		private void clearFields() {	  //TO-DO : Personalize
			table.clearSelection();
			nameTextField.setText("");
			descriptionTextField.setText("");
			skuTextField.setText("");
			comboBoxBrands.setSelectedIndex(0);
			comboBoxCategory.setSelectedIndex(0);
			
			messageLabel.setText("");
	        messageLabel.setOpaque(false);
		}

		private void fillBrands() {// metodo en obj de I,  fillBrands(JComboBox c){..}
			comboBoxBrands.removeAllItems();
			comboBoxBrands.addItem(new Brand(null, "Seleccione una Marca"));
			List<Brand> brands = brandProvider.getBrands();
			for(Brand b : brands)
				comboBoxBrands.addItem(b);
		}
		
		private void createJPopupMenu() {
			JPopupMenu popupMenu = new JPopupMenu();
			JMenuItem editarItem = new JMenuItem("Modificar fila");
			JMenuItem eliminarItem = new JMenuItem("Eliminar fila");
			popupMenu.add(editarItem);
			popupMenu.add(eliminarItem);
			//table.setComponentPopupMenu(popupMenu);

			table.addMouseListener(new MouseAdapter() {
				@Override
				public void mousePressed(MouseEvent e) {
					if (e.isPopupTrigger())
						showPopup(e);
				}
				@Override
				public void mouseReleased(MouseEvent e) {
					if (e.isPopupTrigger())
						showPopup(e);
				}
				private void showPopup(MouseEvent e) {
					int row = table.rowAtPoint(e.getPoint());
					if (row >= 0 && row < table.getRowCount()) {
						table.setRowSelectionInterval(row, row); // selecciona la fila
						popupMenu.show(e.getComponent(), e.getX(), e.getY());
					}
				}
			});
			eliminarItem.addActionListener(e -> delete() );
			editarItem.addActionListener(e -> setRowToEdit() );
		}
		
		private void createMessageLabel() {
			messageLabel = new JLabel("", SwingConstants.CENTER);
	        messageLabel.setForeground(Color.WHITE);
	        messageLabel.setBackground(Color.RED);
	        messageLabel.setFont(new Font("Montserrat", Font.BOLD, 16));
	        JPanel horizontalPanel = new JPanel(new GridLayout());
	        horizontalPanel.setPreferredSize(new Dimension(1920,55));
	        horizontalPanel.setMaximumSize(new Dimension(1920,100));
	        horizontalPanel.add(messageLabel);
			//tablePanel.add(horizontalPanel);
			add(horizontalPanel, BorderLayout.SOUTH);
		}

		private void setMessage(String message) {
			messageLabel.setText(message);
	        messageLabel.setOpaque(true);
		}
}
