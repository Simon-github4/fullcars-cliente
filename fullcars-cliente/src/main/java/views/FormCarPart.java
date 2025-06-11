package views;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.GridLayout;
import java.io.IOException;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.EtchedBorder;
import javax.swing.border.MatteBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;

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
	private JButton searchButton = new JButton("Buscar", Utils.Icons.LENS.create(22,22));
	
	private JTextField nameTextField = new JTextField("", 29);;
	private JTextField stockTextField = new JTextField("", 29);;
	private JComboBox<Brand> comboBoxBrands = new JComboBox<Brand>();
	private JComboBox<Category> comboBoxCategory = new JComboBox<Category>();
	private JTextField descriptionTextField = new JTextField("", 29);;
	private JTextField skuTextField = new JTextField(29);
	private JTextField skuSearchTextField = new JTextField("", 15);;
	

		public FormCarPart(CarPartController controller, IBrandProvider brandProvider) {
			this.brandProvider = brandProvider;
			this.controller = controller;
			
			initUI();
			fillBrands();
			comboBoxCategory.addItem(new Category(null, "selecione cateogria"));
			loadTable();
		}
		
		private void save() {
			//long stock = Long.parseLong( is not changed from the view
			CarPart m = CarPart.builder()
					.brand((Brand)comboBoxBrands.getSelectedItem())
					.category((Category)comboBoxCategory.getSelectedItem())
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
		
		private void delete() {
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
		
		private void loadTable() {
	        tableModel.setRowCount(0);

			List<CarPart> carParts = controller.getCarParts();
			for(CarPart c : carParts) {
				Object[] row = {c.getName(), c.getDescription(), c.getSku(), c.getStock(), c.getBrand(), c.getCategory(), c.getId()};
				tableModel.addRow(row);
			}
		}

		private void fillBrands() {
			comboBoxBrands.removeAllItems();
			comboBoxBrands.addItem(new Brand(null, "Seleccione una Marca"));
			List<Brand> brands = brandProvider.getBrands();
			for(Brand b : brands)
				comboBoxBrands.addItem(b);
		}
		
		private boolean validateFields() {
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

		private void clearFields() {
			table.clearSelection();
			nameTextField.setText("");
			descriptionTextField.setText("");
			skuTextField.setText("");
			comboBoxBrands.setSelectedIndex(0);
			comboBoxCategory.setSelectedIndex(0);
			
			messageLabel.setText("");
	        messageLabel.setOpaque(false);
		}

		private void initUI() {
		    setLayout(new BorderLayout(0, 0));
		    createInputPanel();	
		    createTablePanel();
		    createMessageLabel();
		    searchButton.addActionListener(e -> loadTable());
		    
		    JPanel north = new JPanel(new GridLayout(0,1, 0, 0));
			JLabel titulo = new JLabel("Gestion de Auto Partes", JLabel.CENTER);
			try {
				titulo.setFont(Font.createFont(Font.TRUETYPE_FONT, getClass().getResourceAsStream("/fonts/Montserrat-Bold.ttf")).deriveFont(40f));
			} catch (FontFormatException e1) {
				e1.printStackTrace();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			titulo.setOpaque(true); // Habilita el fondo
	        titulo.setBackground(new Color(150, 150, 150)); 
	        titulo.setBorder(new MatteBorder(0, 0, 0, 0, Color.DARK_GRAY));
	        titulo.setPreferredSize(new Dimension(WIDTH, 85));
	        
			JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
			filterPanel.add(new JLabel("Filtrar por SKU", JLabel.RIGHT));
			filterPanel.setPreferredSize(new Dimension(WIDTH, 60));
			filterPanel.add(skuSearchTextField);
			filterPanel.add(new JLabel("Categoría:", JLabel.RIGHT));
			filterPanel.add(new JTextField(15));
			filterPanel.add(searchButton);
			filterPanel.add(toggleButton);
			filterPanel.setBorder(BorderFactory.createEmptyBorder(0, 15, 0, 0));
			/*filterPanel.setBorder(BorderFactory.createCompoundBorder(
				    BorderFactory.createTitledBorder(
				        BorderFactory.createEtchedBorder(EtchedBorder.LOWERED),
				        "Filtros",
				        TitledBorder.LEFT,
				        TitledBorder.TOP,
				        new Font("Montserrat-Medium", Font.TRUETYPE_FONT, 14),
				        new Color(70,70,70)
				    ),
				    BorderFactory.createEmptyBorder(5, 9, 0, 0)
				));
			*/north.add(titulo);		
			north.add(filterPanel);
			
			add(north, BorderLayout.NORTH);			
//--------------------------------------------------------CENTER PANEL-----------------------------------------------------------
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

		private void createInputPanel() {
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
			buttonsPanel.setMaximumSize(new Dimension(800, 220));

			JPanel firsts = new JPanel(new GridLayout());
			JButton confirm = new JButton("Confirmar");
			confirm.addActionListener(e ->{
				if(validateFields())
					save();
			});
			JButton delete = new JButton("Eliminar");
			delete.addActionListener(e ->{ 
				if(validateFields() && table.getSelectedRow() != -1)
					delete();
			});
			confirm.setPreferredSize(new Dimension(120, 90));
			delete.setPreferredSize(new Dimension(120, 90));
			firsts.add(confirm);
			firsts.add(delete);
			buttonsPanel.add(firsts, BorderLayout.CENTER);

			//inputPanel.add(horizontalPanel);
			horizontalPanel = new JPanel(new GridLayout());
			JButton cancel = new JButton("Cancelar");
			cancel.addActionListener(e -> clearFields());
			cancel.setPreferredSize(new Dimension(250, 65));
			cancel.setBackground(Color.WHITE);
			cancel.setForeground(UIManager.getColor("Button.background"));
			delete.setBackground(Color.red);
			horizontalPanel.add(cancel);		
			
			buttonsPanel.add(horizontalPanel, BorderLayout.SOUTH);
			inputPanel.add(buttonsPanel);		
		}

		private void createTablePanel() {
			tableModel = new DefaultTableModel(columns, 0){
				@Override
	            public boolean isCellEditable(int row, int column) {
	                return false; // Hacer que todas las celdas sean no editables
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
	        
			table = new JTable(tableModel);
			table.getColumnModel().getColumn(table.getColumnCount()-1).setMaxWidth(0);
			table.getColumnModel().getColumn(table.getColumnCount()-1).setMinWidth(0);
			table.getColumnModel().getColumn(table.getColumnCount()-1).setPreferredWidth(0);
			table.setShowGrid(true);
			table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
				@Override
				public void valueChanged(ListSelectionEvent e) {
	                if (!e.getValueIsAdjusting()) {
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
				}
			});
			tablePanel = new JPanel();
			tablePanel.setLayout(new BoxLayout(tablePanel, BoxLayout.Y_AXIS));
			tablePanel.add(new JScrollPane(table));				
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
