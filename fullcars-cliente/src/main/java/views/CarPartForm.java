package views;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.RowFilter;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

import com.formdev.flatlaf.FlatClientProperties;

import Utils.Icons;
import Utils.ServerException;
import controller.CarPartController;
import controller.CategoryController;
import controller.ProviderController;
import interfaces.IBrandProvider;
import interfaces.Refreshable;
import model.client.entities.Brand;
import model.client.entities.CarPart;
import model.client.entities.Category;
import model.client.entities.Provider;
import views.components.JPopupMenuModifyDelete;
import views.components.LightTheme;
import views.components.NewModifyButton;
import views.components.TypedComboBox;


public class CarPartForm extends JPanel implements Refreshable{
	
private static final long serialVersionUID = 1L;
	
	private final CarPartController controller;
	private final IBrandProvider brandProvider;
	private final CategoryController categoryProvider;
	private final ProviderController providerController;
	
	private JPanel inputPanel;
	private JPanel tablePanel;
	private static final Object[] COLUMNS = {"Nombre", "Descripcion", "SKU", "Stock", "Precio venta", "Marca", "Categoria", "Proveedor", "id"};
	private JTable table;
	private DefaultTableModel tableModel;
	private JLabel messageLabel;
	private JButton toggleButton = new JButton("Mostrar formulario");
	
	private JTextField nameTextField = new JTextField("", 29);
	private JTextField descriptionTextField = new JTextField("", 29);
	private JTextField skuTextField = new JTextField(29);
	private JTextField stockTextField = new JTextField("", 29);
	private JTextField sellPriceTextField = new JTextField("", 29);
	private TypedComboBox<Provider> comboBoxProviders = new TypedComboBox<Provider>(p -> p.getCompanyName());
	private TypedComboBox<Brand> comboBoxBrands = new TypedComboBox<>(b->b.getName());
	private TypedComboBox<Category> comboBoxCategory = new TypedComboBox<>(c->c.getName());
	private NewModifyButton confirmButton = new NewModifyButton();

	private JButton searchButton = new JButton("Buscar", Icons.LENS.create(18,18));
	private JTextField skuSearchTextField = new JTextField("", 15);
	private JTextField nameSearchTextField = new JTextField("",15);  // Agregado
	private TableRowSorter<DefaultTableModel> sorter;
	private Timer filtroTimer;

		public CarPartForm(CarPartController controller, IBrandProvider brand, CategoryController category, ProviderController pc) {
		    this.controller = controller;
		    this.brandProvider = brand;
		    this.categoryProvider = category;
		    this.providerController = pc;
		    
		    setLayout(new BorderLayout(0, 0));
		    createInputPanel();	
		    createTablePanel();
		    createJPopupMenu();
		    createMessageLabel();
		    initUI();
			
		    comboBoxProviders.fill(providerController.getProviders(), Provider.builder().id(null).companyName("Seleccione una Proveedor").build()); 
			comboBoxBrands.fill(brandProvider.getBrands(), new Brand(null, "Seleccione una Marca")); 
			comboBoxCategory.fill(categoryProvider.getCategories(), new Category(null, "Seleccione una Categoria"));
			sorter = new TableRowSorter<>(tableModel);
	        table.setRowSorter(sorter);
	        setupLiveFilterListeners();
	        
	        loadTable();
		}
		
		private void save() {			  //TO-DO : Personalize
			CarPart m = CarPart.builder()
					.brand(comboBoxBrands.getSelectedItem())
					.category(comboBoxCategory.getSelectedItem())
					.name(nameTextField.getText())
					.description(descriptionTextField.getText())
					.sku(skuTextField.getText())
					.basePrice(Long.parseLong(sellPriceTextField.getText()))
					.provider(comboBoxProviders.getSelectedItem())
					.build();

			if(confirmButton.isInModifyMode()) {
				Long id = confirmButton.getIdToModify();
				m.setId(id);
			}
			
			try{
				controller.save(m); 	
				clearFields();
				loadTable();
			}catch(ServerException se) {
				setMessage(se.getMessage());
			} catch (IOException ioe) {
				setMessage(ioe.getMessage());
			}catch(Exception e) {
				setMessage("No se pudo Guardar");				
			}
		}
		
		private void delete() {			  //TO-DO : Personalize
			String sku = (String)tableModel.getValueAt(table.convertRowIndexToModel(table.getSelectedRow()), table.getColumnModel().getColumnIndex((String)"SKU"));
			try {
				if(JOptionPane.showConfirmDialog(null, "Desea eliminar al producto con el sku: "+sku,  "", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION){
					Long id = (Long)tableModel.getValueAt(table.getSelectedRow(), table.getColumnModel().getColumnIndex((String)"id"));
					controller.delete(id);
					clearFields();
					loadTable();
				}
			}catch(ServerException se) {
				setMessage(se.getMessage());
			} catch (IOException ioe) {
				setMessage(ioe.getMessage());
			}
		}
		
		private void loadTable() {		  //TO-DO : Personalize
	        tableModel.setRowCount(0);
	        sorter.setSortKeys(null);//resets column order
	        
			List<CarPart> carParts = controller.getCarParts();
			for(CarPart c : carParts) {
				Object[] row = {c.getName(), c.getDescription(), c.getSku(), c.getStock(), c.getBasePrice(), c.getBrand(), c.getCategory(), c.getProvider(), c.getId()};
				tableModel.addRow(row);
			}
		}

		private void initUI() {	   		  //TO-DO : Personalize
		    searchButton.addActionListener(e -> loadTable());
		    
		    JPanel north = new JPanel(new BorderLayout());
		    JPanel titulo = LightTheme.createTitle("Gestion de AutoPartes");

			JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
			filterPanel.add(new JLabel("          ", JLabel.RIGHT));
			filterPanel.add(new JLabel("Sku: ", JLabel.RIGHT));
			filterPanel.add(skuSearchTextField);
			filterPanel.add(new JLabel("Nombre: ", JLabel.RIGHT));
			filterPanel.add(nameSearchTextField);
			nameSearchTextField.putClientProperty(FlatClientProperties.TEXT_FIELD_SHOW_CLEAR_BUTTON, true);
			skuSearchTextField.putClientProperty(FlatClientProperties.TEXT_FIELD_SHOW_CLEAR_BUTTON, true);
			filterPanel.add(searchButton);
			filterPanel.add(toggleButton);
			LightTheme.aplicarEstiloPrimario(searchButton);
			LightTheme.aplicarEstiloSecundario(toggleButton);
			filterPanel.setBorder(BorderFactory.createCompoundBorder(
				    BorderFactory.createEmptyBorder(0, 10, 10, 10),  
				    BorderFactory.createTitledBorder(
				        BorderFactory.createEtchedBorder(EtchedBorder.LOWERED),
				        "Filtros",
				        TitledBorder.LEFT,
				        TitledBorder.TOP,
				        new Font("Montserrat-Medium", Font.PLAIN, 14), // TRUETYPE_FONT no es correcto aquí
				        Color.BLACK
				    )
			));
			north.add(titulo, BorderLayout.NORTH);		
			north.add(filterPanel, BorderLayout.SOUTH);
			
			add(north, BorderLayout.NORTH);	
//------------------------------------------------CENTER PANEL-----------------------------(NO se personaliza)
			JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, tablePanel, inputPanel);
	        splitPane.setResizeWeight(1); // El panel de arriba no se estira al redimensionar

	        toggleButton.setPreferredSize(new Dimension(170, 26));
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
	        toggleButton.doClick();
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

			skuTextField.setEnabled(false);
			skuTextField.setToolTipText("Codigo interno generado Automaticamente");
			stockTextField.setToolTipText("Stock disponible (solo lectura)");
			stockTextField.setEnabled(false);
	
			JPanel rowsPanel = new JPanel(new GridLayout(0,1));

			rowsPanel.add(new JLabel("  Nombre", JLabel.LEFT));
			rowsPanel.add(nameTextField);
			rowsPanel.add(new JLabel("  Descripcion", JLabel.LEFT));
			rowsPanel.add(descriptionTextField);
			
			JPanel columnPanel = new JPanel(new GridLayout(1,0));
			columnPanel.add(new JLabel("  Sku", JLabel.LEFT));
			columnPanel.add(new JLabel("  Stock", JLabel.LEFT));
			rowsPanel.add(columnPanel);
			
			columnPanel = new JPanel(new GridLayout(1,0));
			columnPanel.add(skuTextField);		
			columnPanel.add(stockTextField);
			rowsPanel.add(columnPanel);
			
			rowsPanel.add(new JLabel("  Precio venta", JLabel.LEFT));
			rowsPanel.add(sellPriceTextField);

			columnPanel = new JPanel(new GridLayout(1,0));
			columnPanel.add(new JLabel("  Marca", JLabel.LEFT));
			columnPanel.add(new JLabel("  Categoria", JLabel.LEFT));
			rowsPanel.add(columnPanel);
			
			columnPanel = new JPanel(new GridLayout(1,0));
			columnPanel.add(comboBoxBrands);
			columnPanel.add(comboBoxCategory);	
			rowsPanel.add(columnPanel);
			
			rowsPanel.add(new JLabel("  Proveedor", JLabel.LEFT));
			rowsPanel.add(comboBoxProviders);
			//categoryTextField.putClientProperty(FlatClientProperties.STYLE, "showRevealButton:true");
			
			inputPanel.add(rowsPanel);
			
			JPanel buttonsPanel = new JPanel(new BorderLayout());
			buttonsPanel.setMaximumSize(new Dimension(500, 160));
			JPanel firsts = new JPanel(new GridLayout());

			confirmButton.addActionListener(e ->{
				if(validateFields())
					save();
			});
			LightTheme.aplicarEstiloPrimario(confirmButton);
			confirmButton.setPreferredSize(new Dimension(120, 70));
			firsts.add(confirmButton);

			JButton cancel = new JButton("Cancelar");
			cancel.addActionListener(e -> clearFields());
			LightTheme.aplicarEstiloSecundario(cancel);
			cancel.setPreferredSize(new Dimension(250, 70));
			firsts.add(cancel);
			
			buttonsPanel.add(firsts, BorderLayout.CENTER);
			inputPanel.add(buttonsPanel);		
		}

		@SuppressWarnings("serial")
		private void createTablePanel() { //TO-DO : Personalize getColumnClass()
			tableModel = new DefaultTableModel(COLUMNS, 0){
				@Override
	            public boolean isCellEditable(int row, int column) {
	                return false;
	            }
				@Override
	            public Class<?> getColumnClass(int column) {
	                switch (column) {
	                    case 3: return Long.class;
	                    case 4: return Long.class;
	                    case 8: return Long.class;
	                    default:return Object.class;
	                }
	            }
	        };
			table = new JTable(tableModel);
			table.setToolTipText("Click Derecho para Eliminar o Modificar");
			table.setShowGrid(true);
			table.getColumnModel().getColumn(table.getColumnCount()-1).setMaxWidth(0);
			table.getColumnModel().getColumn(table.getColumnCount()-1).setMinWidth(0);
			table.getColumnModel().getColumn(table.getColumnCount()-1).setPreferredWidth(0);

			DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
	        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
	        for (int i = 0; i < table.getColumnCount(); i++) {
	            Class<?> columnClass = tableModel.getColumnClass(i);
	            if (Number.class.isAssignableFrom(columnClass)) 
	                table.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
	        }

			tablePanel = new JPanel();
			tablePanel.setLayout(new BoxLayout(tablePanel, BoxLayout.Y_AXIS));
			tablePanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));
			tablePanel.add(new JScrollPane(table));				
		}

		private void setRowToEdit() {	  //TO-DO : Personalize
           	int modelRow = table.getSelectedRow();
           	if(modelRow != -1){
           	    int row = table.convertRowIndexToModel(modelRow);
				Long id = (Long) tableModel.getValueAt(row, table.getColumnModel().getColumnIndex((String)"id"));
				CarPart part = controller.getCarPart(id);
				
				nameTextField.setText(part.getName());
				descriptionTextField.setText(part.getDescription());
				skuTextField.setText(part.getSku());
				stockTextField.setText(String.valueOf(part.getStock()));
				sellPriceTextField.setText(part.getBasePrice().toString());
				comboBoxCategory.setSelectedItem(part.getCategory());
				comboBoxBrands.setSelectedItem(part.getBrand());
				comboBoxProviders.setSelectedItem(part.getProvider());
					
				if(!inputPanel.isVisible())
					toggleButton.doClick();
				
				confirmButton.toModify(id);
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
			try {
				if(Long.parseLong(sellPriceTextField.getText()) < 0L){
					setMessage("El precio no puede ser menor a 0");
					return false;
				}
			}catch(NumberFormatException e) {
				e.printStackTrace();
				setMessage("Asegurese de introducir numeros en Precio venta");
			}
			if(comboBoxBrands.getSelectedIndex() == 0 || comboBoxCategory.getSelectedIndex() == 0) {
				setMessage("Debe seleccionar Marca y Categoria validos");
				return false;
			}
			if(comboBoxProviders.getSelectedIndex() == 0) {
				setMessage("Debe seleccionar Proveedor valido");
				return false;
			}
			return true;	
		}

		private void clearFields() {	  //TO-DO : Personalize
			table.clearSelection();
			nameTextField.setText("");
			descriptionTextField.setText("");
			skuTextField.setText("");
			stockTextField.setText("");
			sellPriceTextField.setText("");
			comboBoxBrands.setSelectedIndex(0);
			comboBoxCategory.setSelectedIndex(0);
			comboBoxProviders.setSelectedIndex(0);
			
			confirmButton.toNew();
			
			messageLabel.setText("");
	        messageLabel.setOpaque(false);
		}
		
		private void applyCombinedFilters() {//TO-DO : Personalize			
		    String skuText = skuSearchTextField.getText().trim();
		    String nameText = nameSearchTextField.getText();
		    List<RowFilter<Object, Object>> filters = new ArrayList<>();

		    if (!skuText.isEmpty()) 
		        filters.add(RowFilter.regexFilter("(?i)^" + Pattern.quote(skuText), 2)); // Columna SKU

		    if (!nameText.isEmpty()) 
		        filters.add(RowFilter.regexFilter("(?i)" + Pattern.quote(nameText), 0)); // Columna Nombre
		    
		    if (filters.isEmpty()) 
		        sorter.setRowFilter(null); // Sin filtro
		    else 
		        sorter.setRowFilter(RowFilter.andFilter(filters));
		}
		
		private void setupLiveFilterListeners() {
		    DocumentListener debounceListener = new DocumentListener() {
		        @Override public void insertUpdate(DocumentEvent e) { reiniciarTimer(); }
		        @Override public void removeUpdate(DocumentEvent e) { reiniciarTimer(); }
		        @Override public void changedUpdate(DocumentEvent e) { reiniciarTimer(); }

		        private void reiniciarTimer() {
		            if (filtroTimer != null && filtroTimer.isRunning()) 
		                filtroTimer.restart();
		            else {
		                filtroTimer = new Timer(200, evt -> applyCombinedFilters());
		                filtroTimer.setRepeats(false); // Solo una vez
		                filtroTimer.start();
		            }
		        }
		    };
		    skuSearchTextField.getDocument().addDocumentListener(debounceListener);
		    nameSearchTextField.getDocument().addDocumentListener(debounceListener);
		}

		private void createJPopupMenu() {
			new JPopupMenuModifyDelete(table, this::setRowToEdit, this::delete );
		}

		@Override
		public void refresh() {
			//clearFields();
			loadTable();
			Brand selectedBrand = comboBoxBrands.getSelectedItem();
			Category selectedCategory = comboBoxCategory.getSelectedItem();
			Provider selectedProvider = comboBoxProviders.getSelectedItem();
			
			comboBoxBrands.fill(brandProvider.getBrands(), new Brand(null, "Selecione una Marca"));
			comboBoxBrands.setSelectedItem(selectedBrand);
			comboBoxCategory.fill(categoryProvider.getCategories(), new Category(null, "Seleccione una categoria"));
			comboBoxCategory.setSelectedItem(selectedCategory);
		    comboBoxProviders.fill(providerController.getProviders(), Provider.builder().id(null).companyName("Seleccione un Proveedor").build()); 
		    comboBoxProviders.setSelectedItem(selectedProvider);
		}
		
		private void createMessageLabel() {
			messageLabel = LightTheme.createMessageLabel();
	        JPanel horizontalPanel = new JPanel(new GridLayout());
	        horizontalPanel.add(messageLabel);
			add(horizontalPanel, BorderLayout.SOUTH);
		}

		private void setMessage(String message) {
			messageLabel.setText(message);
	        messageLabel.setOpaque(true);
		}
}
