package views;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.RowFilter;
import javax.swing.SwingConstants;
import javax.swing.Timer;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

import com.formdev.flatlaf.FlatClientProperties;

import Utils.Icons;
import Utils.NumberFormatArg;
import Utils.ServerException;
import controller.CustomerController;
import controller.SaleController;
import interfaces.Refreshable;
import model.client.entities.Customer;
import model.client.entities.Purchase;
import model.client.entities.Sale;
import model.client.entities.SaleDetail;
import raven.datetime.DatePicker;
import raven.datetime.event.DateSelectionEvent;
import raven.datetime.event.DateSelectionListener;
import views.components.JPopupMenuModifyDelete;
import views.components.LightTheme;
import views.components.TypedComboBox;

public class SalesHistory extends JPanel implements Refreshable{
private static final long serialVersionUID = 1L;

	private final SaleController controller;
	private final CustomerController customerController;
	private List<Sale> salesList = new ArrayList<>();

	private JPanel detailsTablePanel;
	private static final Object[] DETAILS_COLUMNS = {"Autoparte", "Cantidad", "Precio unitario", "SubTotal", "id" };
	private JTable detailsTable;
	private DefaultTableModel detailsTableModel;
	
	private JPanel saleTablePanel;
	private static final Object[] SALE_COLUMNS = {"Fecha", "Cliente", "Nro. Siniestro", "Total", "Nro. Venta" };
	private JTable saleTable;
	private DefaultTableModel saleTableModel;
	private JTextField totalTextField = new JTextField("", 10);

	private TypedComboBox<Customer> customerComboBox = new TypedComboBox<>(customer -> customer.getFullName());
	private JTextField idSearchTextField = new JTextField("", 10);
	private JFormattedTextField dateSearchTextField = new JFormattedTextField();
	private DatePicker dpFilter = new DatePicker();
	private JButton searchButton = new JButton("Buscar", Icons.LENS.create(18, 18));
	private JCheckBox hidenCheckBox = new JCheckBox("Mostrar Todo");//, Icons.CONFIRM.create(18, 18));

	private TableRowSorter<DefaultTableModel> sorter;
	private Timer filtroTimer;
	private JLabel messageLabel;

	public SalesHistory(SaleController controller, CustomerController customerController) {
		this.controller = controller;
		this.customerController = customerController;

		setLayout(new BorderLayout(0, 0));
		//customerComboBox.fill(customerController.getCustomers(), Customer.builder().id(null).fullName("Seleccione un cliente").build());
		
		createDetailsTablePanel();
		createSaleTablePanel();
		createJPopupMenu();
		createMessageLabel();
		initUI();

		sorter = new TableRowSorter<>(saleTableModel);
		saleTable.setRowSorter(sorter);
		setupDocumentListeners();
		
		getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
        .put(KeyStroke.getKeyStroke("control A"), "mostrarCheckbox");

		getActionMap().put("mostrarCheckbox", new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				hidenCheckBox.setVisible(!hidenCheckBox.isVisible());
				hidenCheckBox.setSelected(hidenCheckBox.isVisible());
				revalidate();
				repaint();
				loadSaleTable();
			}
		});
		//loadSaleTable();
	}

	private void delete() {			
		try {
			Long idSale = (Long) saleTableModel.getValueAt(saleTable.convertRowIndexToModel(saleTable.getSelectedRow()), saleTable.getColumnModel().getColumnCount()-1);
			
			if(JOptionPane.showConfirmDialog(null, "Desea eliminar la venta Numero: "+ idSale.toString(),  "", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION){
				controller.delete(idSale);
				clearFields();
				loadSaleTable();
			}
		}catch(ServerException se) {
			setMessage(se.getMessage());
		} catch (IOException ioe) {
			setMessage(ioe.getMessage());
		}
	}
	
	private void loadSaleTable() {
		saleTableModel.setRowCount(0);
		detailsTableModel.setRowCount(0);
		sorter.setSortKeys(null);// resets column order
		
		salesList = new ArrayList<>();//controller.getSales(customerComboBox.getSelectedItem(), dpFilter.getSelectedDateRange(), hidenCheckBox.isSelected());
		long totalSold = 0;
		
		String nroCompraText = idSearchTextField.getText().trim();

		if (!nroCompraText.isEmpty()) 
			try {
				Sale s = controller.getSale(Long.parseLong(nroCompraText));
				if(s == null)
					throw new NullPointerException();
				if(!hidenCheckBox.isSelected() && s.getFactura() == null)
					throw new NullPointerException();					
				salesList.add(s);
			} catch (NumberFormatException ex) {
				setMessage("El número de venta no es válido");
			}catch (NullPointerException ex) {
				setMessage("No se encontro la venta: "+ nroCompraText);
			}
		 else 
			 salesList = controller.getSales(customerComboBox.getSelectedItem(), dpFilter.getSelectedDateRange(), hidenCheckBox.isSelected());
		
		for (Sale s : salesList) {
			totalSold += s.getTotal();
			Object[] row = {s.getDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")), s.getCustomer(), 
						   (s.getSaleNumber()==null || s.getSaleNumber().isBlank())?"Particular":s.getSaleNumber()
							, s.getTotal(), s.getId() };
			saleTableModel.addRow(row);
		}
		totalTextField.setText("$"+ NumberFormatArg.format(totalSold));
	}
	private void loadDetailsTable(Long idSale) {
		int i = 0;
		detailsTableModel.setRowCount(0);
		Sale sale = salesList.get(i);
		while(sale.getId() != idSale) 
			sale = salesList.get(++i);
    	for(SaleDetail d : sale.getDetails()) {
    		Object[] row = {d.getProduct().getSku(), d.getQuantity(), d.getUnitPrice(), d.getSubTotal(), d.getId()};
    		detailsTableModel.addRow(row);
    	}
	}
	
	private void initUI() {
		searchButton.addActionListener(e -> loadSaleTable());
		dpFilter.setDateSelectionMode(DatePicker.DateSelectionMode.BETWEEN_DATE_SELECTED);
		dpFilter.setUsePanelOption(true);
		dpFilter.setBackground(Color.GRAY); // Color de fondo oscuro
		dpFilter.setDateFormat("dd/MM/yyyy");
		dpFilter.addDateSelectionListener(new DateSelectionListener() {
			@Override
			public void dateSelected(DateSelectionEvent dateSelectionEvent) {
				loadSaleTable();
			}
		});
		dpFilter.setEditor(dateSearchTextField);
		customerComboBox.addActionListener(e -> loadSaleTable());
		totalTextField.setForeground(LightTheme.COLOR_GREEN_MONEY);
		totalTextField.setEditable(false);
		hidenCheckBox.setVisible(false);
		hidenCheckBox.setSelected(false);
		hidenCheckBox.addActionListener(e-> loadSaleTable());
		
		JPanel north = new JPanel(new BorderLayout());
		JPanel titulo = LightTheme.createTitle("Ventas");

		JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		filterPanel.add(new JLabel("          ", JLabel.RIGHT));
		filterPanel.add(new JLabel("Nro Venta: ", JLabel.RIGHT));
		filterPanel.add(idSearchTextField);
		filterPanel.add(new JLabel("Cliente ", JLabel.RIGHT));
		filterPanel.add(customerComboBox);
		filterPanel.add(new JLabel("Desde/Hasta ", JLabel.RIGHT));
		filterPanel.add(dateSearchTextField);
		filterPanel.add(new JLabel("  Total Vendido", JLabel.RIGHT));
		filterPanel.add(totalTextField);		
		dateSearchTextField.putClientProperty(FlatClientProperties.TEXT_FIELD_SHOW_CLEAR_BUTTON, true);
		idSearchTextField.putClientProperty(FlatClientProperties.TEXT_FIELD_SHOW_CLEAR_BUTTON, true);
		filterPanel.add(searchButton);
		filterPanel.add(hidenCheckBox);
		LightTheme.aplicarEstiloPrimario(searchButton);
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
		JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, saleTablePanel, detailsTablePanel);
		splitPane.setResizeWeight(1); // El panel de arriba no se estira al redimensionar
		splitPane.setDividerSize(8);
		splitPane.setDividerLocation(splitPane.getWidth() - detailsTablePanel.getPreferredSize().width);
		add(splitPane, BorderLayout.CENTER);
//--------------------------------------------------------CENTER PANEL------------------------------------------------------------
	}

	private void createDetailsTablePanel() {
		detailsTableModel = new DefaultTableModel(DETAILS_COLUMNS, 0) {
			@Override
			public boolean isCellEditable(int row, int column) {
				return false;
			}
			@Override
			public Class<?> getColumnClass(int column) {
				switch (column) {
				case 1:
					return Long.class;
				case 2:
					return Long.class;
				case 3:
					return Long.class;
				default:
					return Object.class;
				}
			}
		};
		detailsTable = new JTable(detailsTableModel);
		//detailsTable.setToolTipText("Click Derecho para Eliminar");
		detailsTable.setShowGrid(true);
		detailsTable.getColumnModel().getColumn(detailsTable.getColumnCount() - 1).setMaxWidth(90);
		detailsTable.getColumnModel().getColumn(detailsTable.getColumnCount() - 1).setMinWidth(90);
		detailsTable.getColumnModel().getColumn(detailsTable.getColumnCount() - 1).setPreferredWidth(90);

		DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
		centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
		for (int i = 0; i < detailsTable.getColumnCount(); i++) {
			Class<?> columnClass = detailsTableModel.getColumnClass(i);
			if (Number.class.isAssignableFrom(columnClass))
				detailsTable.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
		}

		detailsTablePanel = new JPanel(new BorderLayout());
		detailsTablePanel.add(new JScrollPane(detailsTable), BorderLayout.CENTER);
		detailsTablePanel.add(LightTheme.createSubTitle("Detalles"), BorderLayout.NORTH);
	}

	@SuppressWarnings("serial")
	private void createSaleTablePanel() {
		saleTableModel = new DefaultTableModel(SALE_COLUMNS, 0) {
			@Override
			public boolean isCellEditable(int row, int column) {
				return false;
			}
			@Override
			public Class<?> getColumnClass(int column) {
				switch (column) {
				case 2:
					return Long.class;
				case 3:
					return Long.class;
				case 4:
					return Long.class;	
				default:
					return Object.class;
				}
			}
		};
		saleTable = new JTable(saleTableModel);
		saleTable.setToolTipText("Click Derecho para Eliminar ; Click izquierdo para ver detalles");
		saleTable.setShowGrid(true);
		saleTable.getColumnModel().getColumn(saleTable.getColumnCount() - 1).setMaxWidth(90);
		saleTable.getColumnModel().getColumn(saleTable.getColumnCount() - 1).setMinWidth(90);
		saleTable.getColumnModel().getColumn(saleTable.getColumnCount() - 1).setPreferredWidth(90);
		saleTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {
                	int row = saleTable.getSelectedRow();
                	if(row != -1)
                		loadDetailsTable((Long)saleTableModel.getValueAt(saleTable.convertRowIndexToModel(row), saleTable.getColumnCount()-1));
                }
			}
		});
		DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
		centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
		for (int i = 0; i < saleTable.getColumnCount(); i++) {
			Class<?> columnClass = saleTableModel.getColumnClass(i);
			if (Number.class.isAssignableFrom(columnClass))
				saleTable.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
		}

		saleTablePanel = new JPanel(new BorderLayout());
		saleTablePanel.add(LightTheme.createSubTitle("Historial Ventas"), BorderLayout.NORTH);
		saleTablePanel.add(new JScrollPane(saleTable), BorderLayout.CENTER);
		saleTablePanel.add(totalTextField, BorderLayout.SOUTH);
	}

	private void clearFields() {
		saleTable.clearSelection();
		detailsTableModel.setRowCount(0);
		customerComboBox.setSelectedIndex(0);
		dpFilter.clearSelectedDate();
		idSearchTextField.setText("");
		totalTextField.setText("");
		
		messageLabel.setText("");
		messageLabel.setOpaque(false);
	}

	private void openRemito() {
		try {
			controller.getAndOpenRemito((Long) saleTableModel.getValueAt(saleTable.convertRowIndexToModel(saleTable.getSelectedRow()), saleTable.getColumnModel().getColumnCount()-1));
		} catch (ServerException e) {
			e.printStackTrace();
			setMessage(e.getMessage());
		} catch (IOException e) {
			e.printStackTrace();
			setMessage("Hubo un error al obtener el remito "+e.getMessage());
		}
	}

	private void setupDocumentListeners() {
		DocumentListener debounceListener = new DocumentListener() {
			@Override
			public void insertUpdate(DocumentEvent e) {
				setFilters();
			}
			@Override
			public void removeUpdate(DocumentEvent e) {
				setFilters();
			}
			@Override
			public void changedUpdate(DocumentEvent e) {
				setFilters();
			}
			private void setFilters() {
				if (filtroTimer != null && filtroTimer.isRunning())
					filtroTimer.restart();
				else {
					filtroTimer = new Timer(300, evt ->{
						dpFilter.clearSelectedDate();
						customerComboBox.setSelectedIndex(0);
						dateSearchTextField.setEnabled(idSearchTextField.getText().isBlank());
						customerComboBox.setEnabled(idSearchTextField.getText().isBlank());
						loadSaleTable();
					});
					filtroTimer.setRepeats(false); 
					filtroTimer.start();
				}
			}
		};
		idSearchTextField.getDocument().addDocumentListener(debounceListener);
	}

	private void createJPopupMenu() {
		new JPopupMenuModifyDelete(saleTable, this::delete, "Eliminar Venta").addMenuItem("Abrir Remito de Venta", this::openRemito);
	}

	@Override
	public void refresh() {
		ActionListener[] listeners = customerComboBox.getActionListeners();
		for (ActionListener l : listeners) 
		    customerComboBox.removeActionListener(l);
		
		Customer c = customerComboBox.getSelectedItem();
		customerComboBox.fill(customerController.getCustomers(), Customer.builder().id(null).fullName("Seleccione un cliente").build());
		customerComboBox.setSelectedItem(c);

		for (ActionListener l : listeners) 
		    customerComboBox.addActionListener(l);
		
		clearFields();
		loadSaleTable();
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
