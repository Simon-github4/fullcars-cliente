package views.carpart;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
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
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

import com.formdev.flatlaf.FlatClientProperties;

import Utils.Icons;
import Utils.NumberFormatArg;
import Utils.ServerException;
import controller.CarPartController;
import controller.CategoryController;
import controller.ProviderController;
import interfaces.IBrandProvider;
import interfaces.Refreshable;
import model.client.entities.CarPart;
import views.components.JPopupMenuModifyDelete;
import views.components.LightTheme;
import views.components.NewModifyButton;


public class CarPartForm extends JPanel implements Refreshable{
	
private static final long serialVersionUID = 1L;
	
	private final CarPartController controller;
	private final IBrandProvider brandProvider;
	private final CategoryController categoryProvider;
	private final ProviderController providerController;
	
	private JPanel tablePanel;
	private static final Object[] COLUMNS = {"Nombre", "Descripcion", "SKU", "Cod Prov", "Stock", "Precio venta", "Marca", "Categoria", "Proveedor", "id"};
	private JTable table;
	private DefaultTableModel tableModel;
	private JLabel messageLabel;
	private JButton toggleButton = new JButton("Mostrar formulario", Icons.EYE.create());
	private JButton csvExportButton = new JButton(Icons.EXCEL.create());
	
	private CarPartInputPanel inputPanel;
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
			
			sorter = new TableRowSorter<>(tableModel);
	        table.setRowSorter(sorter);
	        setupLiveFilterListeners();
	        
	        refresh();
	        loadTable();
		}
		
		private void save() {			  //TO-DO : Personalize
			CarPart m = inputPanel.toCarPart();

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
				Object[] row = {c.getName(), c.getDescription(), c.getSku(), c.getProviderSku(), c.getStock(), NumberFormatArg.format(c.getBasePrice()),
								c.getBrand(), c.getCategory(), c.getProvider(), c.getId()};
				tableModel.addRow(row);
			}
		}

		private void initUI() {	   		  //TO-DO : Personalize
		    searchButton.addActionListener(e -> loadTable());
		    
		    JPanel north = new JPanel(new BorderLayout());
		    JPanel titulo = LightTheme.createTitle("Gestion de AutoPartes");

			JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
			filterPanel.add(new JLabel("          ", JLabel.RIGHT));
			filterPanel.add(new JLabel("SKU: ", JLabel.RIGHT));
			filterPanel.add(skuSearchTextField);
			filterPanel.add(new JLabel("Nombre: ", JLabel.RIGHT));
			filterPanel.add(nameSearchTextField);
			nameSearchTextField.putClientProperty(FlatClientProperties.TEXT_FIELD_SHOW_CLEAR_BUTTON, true);
			skuSearchTextField.putClientProperty(FlatClientProperties.TEXT_FIELD_SHOW_CLEAR_BUTTON, true);
			filterPanel.add(searchButton);
			filterPanel.add(toggleButton);
			LightTheme.aplicarEstiloPrimario(searchButton);
			LightTheme.aplicarEstiloSecundario(toggleButton);
			filterPanel.add(csvExportButton);
			csvExportButton.addActionListener(e->exportCsv());
			LightTheme.aplicarEstiloSecundario(csvExportButton);
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
			inputPanel = new CarPartInputPanel();
			inputPanel.setVisible(false);
			inputPanel.setPreferredSize(new Dimension(450, HEIGHT));

			JPanel buttonsPanel = new JPanel(new BorderLayout());
			buttonsPanel.setMaximumSize(new Dimension(500, 160));
			JPanel firsts = new JPanel(new GridLayout());

			confirmButton.addActionListener(e -> {
				if (inputPanel.validateFields(this::setMessage)) 
					save();
			});
			LightTheme.aplicarEstiloPrimario(confirmButton);
			confirmButton.setPreferredSize(new Dimension(120, 70));
			firsts.add(confirmButton);

			JButton cancel = new JButton("Cancelar", Icons.CLEAN.create());
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
			table.getColumnModel().getColumn(table.getColumnCount()-2).setPreferredWidth(50);
			table.getColumnModel().getColumn(0).setMinWidth(130);

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

				inputPanel.loadFrom(part);
				
				if(!inputPanel.isVisible())
					toggleButton.doClick();
				
				confirmButton.toModify(id);
            }
		}

		private void exportCsv() {
			JFileChooser fileChooser = new JFileChooser();
			fileChooser.setDialogTitle("Guardar reporte de autopartes");

			File downloadsDir = new File(System.getProperty("user.home"), "Downloads");
			fileChooser.setCurrentDirectory(downloadsDir);
			fileChooser.setSelectedFile(new File(downloadsDir, "reporte_autopartes_"+LocalDate.now()+".xlsx"));
			fileChooser.setFileFilter(new FileNameExtensionFilter("Archivos XLSX", "xlsx"));

			
		    if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
		        File file = fileChooser.getSelectedFile();
		        if (!file.getName().toLowerCase().endsWith(".xlsx")) {
		            file = new File(file.getAbsolutePath() + ".xlsx");
		        }

		        try {
		            controller.exportCarPartsToCsv(file);

		            JOptionPane.showMessageDialog(this,
		                    "Reporte guardado en:\n" + file.getAbsolutePath(),
		                    "Éxito", JOptionPane.INFORMATION_MESSAGE);
		        } catch (IOException ex) {
		            setMessage("Error al guardar el archivo:\n" + ex.getMessage());
		        }
		    }
		}

		private void clearFields() {	  //TO-DO : Personalize
			table.clearSelection();
			confirmButton.toNew();
			messageLabel.setText("");
			messageLabel.setOpaque(false);

			inputPanel.clearFields();
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
			
		    inputPanel.fillCombos(
		    	    brandProvider.getBrands(),
		    	    categoryProvider.getCategories(),
		    	    providerController.getProviders()
		    	);
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
