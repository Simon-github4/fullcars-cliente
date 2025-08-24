package views;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
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
import Utils.MovementType;
import Utils.ServerException;
import controller.CarPartController;
import controller.StockMovementController;
import interfaces.Refreshable;
import model.client.entities.CarPart;
import model.client.entities.StockMovement;
import raven.datetime.DatePicker;
import raven.datetime.event.DateSelectionEvent;
import raven.datetime.event.DateSelectionListener;
import views.components.JPopupMenuModifyDelete;
import views.components.LightTheme;
import views.components.NewModifyButton;
import views.components.TypedComboBox;

public class StockMovementForm extends JPanel implements Refreshable{
private static final long serialVersionUID = 1L;
	
	private final StockMovementController controller;
	private final CarPartController carpartController;
	
	private JPanel inputPanel;
	private JPanel tablePanel;
	private static final Object[] COLUMNS = {"AutoParte(SKU)", "Cantidad", "Referencia", "Fecha", "Tipo", "id"};
	private JTable table;
	private DefaultTableModel tableModel;
	private JLabel messageLabel;
	private JButton toggleButton = new JButton("Mostrar formulario", Icons.EYE.create());
	
	private TypedComboBox<CarPart> carpartComboBox = new TypedComboBox<>(c -> c.getSku());
	private JTextField quantityTextField = new JTextField(29);
	private JComboBox<MovementType> movementComboBox = new JComboBox<>();
	private JTextField observationsTextField = new JTextField("", 29);
	private DatePicker dpInput = new DatePicker();
	private NewModifyButton confirmButton = new NewModifyButton();
	
	private JButton searchButton = new JButton("Buscar", Icons.LENS.create(18,18));
	private JTextField skuSearchTextField = new JTextField("", 15);
	private JFormattedTextField dateSearchTextField = new JFormattedTextField(); 
	private DatePicker dpFilter = new DatePicker();
	private JCheckBox exits = new JCheckBox("Salidas  ");
	private JCheckBox entries = new JCheckBox("Entradas  ");

	private TableRowSorter<DefaultTableModel> sorter;
	private Timer filtroTimer;

		public StockMovementForm(StockMovementController controller, CarPartController cpController) {
		    this.controller = controller;
		    this.carpartController = cpController;
		    
		    setLayout(new BorderLayout(0, 0));
		    createInputPanel();	
		    createTablePanel();
		    createJPopupMenu();
		    createMessageLabel();
		    initUI();
			
		    //carpartComboBox.fill(carpartController.getCarParts(), CarPart.builder().id(null).sku( "Seleccione una autoparte").build());
		    movementComboBox.addItem(MovementType.ENTRADA_AJUSTE);
		    movementComboBox.addItem(MovementType.SALIDA_AJUSTE);
			sorter = new TableRowSorter<>(tableModel);
	        table.setRowSorter(sorter);
	        setupLiveFilterListeners();
	        
	        //loadTable();
		}
		
		private void save() {			  
			StockMovement c = StockMovement.builder()
					.carPart(carpartComboBox.getSelectedItem())
					.date(dpInput.getSelectedDate())
					.reference(observationsTextField.getText())
					.quantity(Integer.parseInt(quantityTextField.getText()))
					.type((MovementType) movementComboBox.getSelectedItem())
					.purchaseDetail(null)
					.saleDetail(null)
					.build();

			if(confirmButton.isInModifyMode()) {
				Long id = confirmButton.getIdToModify();
				c.setId(id);
			}
			try{
				controller.save(c);	
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
		
		private void delete() {			
			try {
				String type = ((MovementType)tableModel.getValueAt(table.convertRowIndexToModel(table.getSelectedRow()), table.getColumnModel().getColumnIndex("Tipo"))).toString();
				if(type.equalsIgnoreCase("ENTRADA_COMPRA") || type.equalsIgnoreCase("SALIDA_VENTA"))
					throw new ServerException("No se pueden eliminar movimientos de compras o ventas, unicamente de Ajustes");//throw new Exception("No se pueden eliminar movimientos de compras o ventas, unicamente de Ajustes");

				String reference = (String)tableModel.getValueAt(table.convertRowIndexToModel(table.getSelectedRow()), table.getColumnModel().getColumnIndex((String)"Referencia"));
				if(JOptionPane.showConfirmDialog(null, "Desea eliminar el movimiento "+ reference,  "", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION){
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
		
		private void loadTable() {		  
	        if(!entries.isSelected() && !exits.isSelected())
	        	setMessage("para buscar debe seleccionar minimo un Tipo (entrada o salida)");
	        else{
	        	sorter.setSortKeys(null);//resets column order
	        	tableModel.setRowCount(0);
	        	List<StockMovement> stockMovements = controller.getStockMovements(dpFilter.getSelectedDateRange(), entries.isSelected(), exits.isSelected());
				for(StockMovement sm : stockMovements) {
					Object[] row = {sm.getCarPart().getSku(), sm.getQuantity(), sm.getReference(), sm.getDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")), sm.getType(), sm.getId()};
					tableModel.addRow(row);
				}
	        }
		}

		private void initUI() {	   		 
		    searchButton.addActionListener(e -> loadTable());
		    dpFilter.setDateSelectionMode(DatePicker.DateSelectionMode.BETWEEN_DATE_SELECTED);
			dpFilter.setUsePanelOption(true);  
			dpFilter.setBackground(Color.GRAY); // Color de fondo oscuro
			dpFilter.setDateFormat("dd/MM/yyyy");
			dpFilter.addDateSelectionListener(new DateSelectionListener(){
				@Override
				public void dateSelected(DateSelectionEvent dateSelectionEvent) {
					loadTable();
				}
			});
			dpFilter.setEditor(dateSearchTextField);
			entries.addActionListener(e -> loadTable());
			exits.addActionListener(e -> loadTable());
		    
		    JPanel north = new JPanel(new BorderLayout());
		    JPanel titulo = LightTheme.createTitle("Movimientos de Stock");

			JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
			filterPanel.add(new JLabel("          ", JLabel.RIGHT));
			filterPanel.add(new JLabel("SKU: ", JLabel.RIGHT));
			filterPanel.add(skuSearchTextField);
			filterPanel.add(new JLabel("Desde/Hasta ", JLabel.RIGHT));
			filterPanel.add(dateSearchTextField);
			dateSearchTextField.putClientProperty(FlatClientProperties.TEXT_FIELD_SHOW_CLEAR_BUTTON, true);
			skuSearchTextField.putClientProperty(FlatClientProperties.TEXT_FIELD_SHOW_CLEAR_BUTTON, true);
			entries.setSelected(true);
			exits.setSelected(true);
			filterPanel.add(entries);
			filterPanel.add(exits);			
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

			JPanel rowsPanel = new JPanel(new GridLayout(0,1));

			rowsPanel.add(new JLabel("  AutoParte", JLabel.LEFT));
			rowsPanel.add(carpartComboBox);
			rowsPanel.add(new JLabel("  Cantidad", JLabel.LEFT));
			rowsPanel.add(quantityTextField);
			rowsPanel.add(new JLabel("  Tipo Movimiento", JLabel.LEFT));
			rowsPanel.add(movementComboBox);			
			rowsPanel.add(new JLabel("  Fecha", JLabel.LEFT));
			JFormattedTextField dateInputTextField = new JFormattedTextField();
			dpInput.setEditor(dateInputTextField);
			dpInput.setUsePanelOption(true);  
			dpInput.setBackground(Color.GRAY); // Color de fondo oscuro
			dpInput.setDateFormat("dd/MM/yyyy");
			rowsPanel.add(dateInputTextField);		
			rowsPanel.add(new JLabel("  Referencia / Observaciones", JLabel.LEFT));
			rowsPanel.add(observationsTextField);
			
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

			JButton cancel = new JButton("Cancelar", Icons.CLEAN.create());
			cancel.addActionListener(e -> clearFields());
			LightTheme.aplicarEstiloSecundario(cancel);
			cancel.setPreferredSize(new Dimension(250, 70));
			firsts.add(cancel);
			
			buttonsPanel.add(firsts, BorderLayout.CENTER);
			inputPanel.add(buttonsPanel);		
		}

		@SuppressWarnings("serial")
		private void createTablePanel() { 
			tableModel = new DefaultTableModel(COLUMNS, 0){
				@Override
	            public boolean isCellEditable(int row, int column) {
	                return false;
	            }
				@Override
	            public Class<?> getColumnClass(int column) {
	                switch (column) {
	                    case 1: return Long.class;
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

		private void setRowToEdit() {	  
				int modelRow = table.getSelectedRow();
	           	if(modelRow != -1){
	           	    int row = table.convertRowIndexToModel(modelRow);
					Long id = (Long) tableModel.getValueAt(row, table.getColumnModel().getColumnIndex((String)"id"));
					StockMovement sm = controller.getStockMovement(id);
					
					if(sm.getType() == MovementType.ENTRADA_COMPRA || sm.getType() == MovementType.SALIDA_VENTA)
						setMessage("No se puede modificar este movimiento, unicamente se pueden modificar los Ajustes");
					else{
						carpartComboBox.setSelectedItem(sm.getCarPart());
						quantityTextField.setText(sm.getQuantity().toString());
						movementComboBox.setSelectedItem(sm.getType());
						observationsTextField.setText(sm.getReference());
						dpInput.setSelectedDate(sm.getDate());
						if(!inputPanel.isVisible())
							toggleButton.doClick();					
						confirmButton.toModify(id);
					}
	            }
		}
		
		private boolean validateFields() {
			if(carpartComboBox.getSelectedIndex() == 0) {
				setMessage("Debe seleccionar una autoparte");
				return false;
			}
			try {
				if(Integer.parseInt(quantityTextField.getText()) < 0) {
					setMessage("La cantidad no puede ser menor a cero");
					return false;
				}
			}catch(NumberFormatException e) {
				setMessage("Debe indicar una cantidad Valida (unicamente numeros)");
				return false;
			}
			if(dpInput.getSelectedDate() == null) {
				setMessage("Debe seleccionar una fecha");
				return false;
			}
			return true;	
		}

		private void clearFields() {	
			table.clearSelection();
			carpartComboBox.setSelectedIndex(0);
			movementComboBox.setSelectedIndex(0);
			quantityTextField.setText("");
			observationsTextField.setText("");
			confirmButton.toNew();
			entries.setSelected(true);
			exits.setSelected(true);
			dpFilter.clearSelectedDate();
			dpInput.setSelectedDate(LocalDate.now());
			
			messageLabel.setText("");
	        messageLabel.setOpaque(false);
		}
		
		private void applyCombinedFilters() {		
		    String skuText = skuSearchTextField.getText().trim();
		    List<RowFilter<Object, Object>> filters = new ArrayList<>();

		    if (!skuText.isEmpty()) 
		        filters.add(RowFilter.regexFilter("(?i)^" + Pattern.quote(skuText), 0)); // Columna SKU

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
		}

		private void createJPopupMenu() {
			new JPopupMenuModifyDelete(table, this::setRowToEdit, this::delete );
		}

		@Override
		public void refresh() {
			CarPart c = carpartComboBox.getSelectedItem();
		    carpartComboBox.fill(carpartController.getCarParts(), CarPart.builder().id(null).sku( "Seleccione una autoparte").build());
		    carpartComboBox.setSelectedItem(c);
		    
			clearFields();
			loadTable();
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

		    Timer timer = new Timer(3500, e -> {
		        messageLabel.setText("");
		        messageLabel.setOpaque(false); 
		    });
		    timer.setRepeats(false); 
		    timer.start();
		}

		
}

