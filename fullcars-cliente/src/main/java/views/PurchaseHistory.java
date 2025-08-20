package views;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
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
import controller.ProviderController;
import controller.PurchaseController;
import interfaces.Refreshable;
import model.client.entities.Provider;
import model.client.entities.Purchase;
import model.client.entities.PurchaseDetail;
import raven.datetime.DatePicker;
import raven.datetime.event.DateSelectionEvent;
import raven.datetime.event.DateSelectionListener;
import views.components.JPopupMenuModifyDelete;
import views.components.LightTheme;
import views.components.TypedComboBox;

public class PurchaseHistory extends JPanel implements Refreshable{
private static final long serialVersionUID = 1L;

	private final PurchaseController controller;
	private final ProviderController customerController;
	private List<Purchase> purchasesList = new ArrayList<>();

	private JPanel detailsTablePanel;
	private static final Object[] DETAILS_COLUMNS = {"Autoparte", "Cantidad", "Precio unitario", "SubTotal", "id" };
	private JTable detailsTable;
	private DefaultTableModel detailsTableModel;
	
	private JPanel purchaseeTablePanel;
	private static final Object[] PURCHASE_COLUMNS = {"Fecha", "Proveedor", "Total", "Esta Pago", "Nro. Compra" };
	private JTable purchaseTable;
	private DefaultTableModel purchaseTableModel;
	private JTextField totalTextField = new JTextField("", 10);

	private TypedComboBox<Provider> providerComboBox = new TypedComboBox<>(provider -> provider.getCompanyName());
	private JTextField idSearchTextField = new JTextField("", 10);
	private JFormattedTextField dateSearchTextField = new JFormattedTextField();
	private DatePicker dpFilter = new DatePicker();
	private JButton searchButton = new JButton("Buscar", Icons.LENS.create(18, 18));

	private TableRowSorter<DefaultTableModel> sorter;
	private Timer filtroTimer;
	private JLabel messageLabel;

	public PurchaseHistory(PurchaseController controller, ProviderController providerController) {
		this.controller = controller;
		this.customerController = providerController;

		setLayout(new BorderLayout(0, 0));
		//providerComboBox.fill(providerController.getProviders(), Provider.builder().id(null).companyName("Seleccione un proveedor").build());
		
		createDetailsTablePanel();
		createPurchaseTablePanel();
		createJPopupMenu();
		createMessageLabel();
		initUI();

		sorter = new TableRowSorter<>(purchaseTableModel);
		purchaseTable.setRowSorter(sorter);
		setupDocumentListeners();
		
		//loadPurchaseTable();
	}

	private void delete() {			
		try {
			Long idSale = (Long) purchaseTableModel.getValueAt(purchaseTable.convertRowIndexToModel(purchaseTable.getSelectedRow()), purchaseTable.getColumnModel().getColumnCount()-1);
			
			if(JOptionPane.showConfirmDialog(null, "Desea eliminar la compra Numero: "+ idSale.toString(),  "", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION){
				controller.delete(idSale);
				clearFields();
				loadPurchaseTable();
			}
		}catch(ServerException se) {
			setMessage(se.getMessage());
		} catch (IOException ioe) {
			setMessage(ioe.getMessage());
		}
	}
	
	private void loadPurchaseTable() {
		purchaseTableModel.setRowCount(0);
		detailsTableModel.setRowCount(0);
		sorter.setSortKeys(null); // resets column order

		purchasesList = new ArrayList<Purchase>();
		long totalBuys = 0;

		String nroCompraText = idSearchTextField.getText().trim();

		if (!nroCompraText.isEmpty()) 
			try {
				Purchase p = controller.getPurchase(Long.parseLong(nroCompraText));
				if(p == null)
					throw new NullPointerException();
				purchasesList.add(p);
			} catch (NumberFormatException ex) {
				setMessage("El número de compra no es válido");
			}catch (NullPointerException ex) {
				setMessage("No se encontro la compra: "+ nroCompraText);
			}
		 else 
			purchasesList = controller.getPurchases(providerComboBox.getSelectedItem(), dpFilter.getSelectedDateRange());

		for (Purchase s : purchasesList) {
			totalBuys += s.getTotal();
			Object[] row = { s.getDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")), s.getProvider(),
					s.getTotal(), (s.isPayed()) ? "Si" : "No", s.getId() };
			purchaseTableModel.addRow(row);
		}
		totalTextField.setText("$" + NumberFormatArg.format(totalBuys));

	}

	private void loadDetailsTable(int idPurch) {
		Purchase purchase = purchasesList.get(idPurch);
		detailsTableModel.setRowCount(0);
    	for(PurchaseDetail d : purchase.getDetails()) {
    		Object[] row = {d.getProduct().getSku(), d.getQuantity(), d.getUnitPrice(), d.getSubTotal(), d.getId()};
    		detailsTableModel.addRow(row);
    	}
	}
	
	private void initUI() {
		searchButton.addActionListener(e -> loadPurchaseTable());
		dpFilter.setDateSelectionMode(DatePicker.DateSelectionMode.BETWEEN_DATE_SELECTED);
		dpFilter.setUsePanelOption(true);
		dpFilter.setBackground(Color.GRAY); // Color de fondo oscuro
		dpFilter.setDateFormat("dd/MM/yyyy");
		dpFilter.addDateSelectionListener(new DateSelectionListener() {
			@Override
			public void dateSelected(DateSelectionEvent dateSelectionEvent) {
				loadPurchaseTable();
			}
		});
		dpFilter.setEditor(dateSearchTextField);
		providerComboBox.addActionListener(e -> loadPurchaseTable());
		totalTextField.setForeground(LightTheme.COLOR_GREEN_MONEY);
		totalTextField.setEditable(false);
		
		JPanel north = new JPanel(new BorderLayout());
		JPanel titulo = LightTheme.createTitle("Compras");

		JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		filterPanel.add(new JLabel("          ", JLabel.RIGHT));
		filterPanel.add(new JLabel("Nro Compra: ", JLabel.RIGHT));
		filterPanel.add(idSearchTextField);
		filterPanel.add(new JLabel("Proveedor ", JLabel.RIGHT));
		filterPanel.add(providerComboBox);
		filterPanel.add(new JLabel("Desde/Hasta ", JLabel.RIGHT));
		filterPanel.add(dateSearchTextField);
		filterPanel.add(new JLabel("  Total Gastado", JLabel.RIGHT));
		filterPanel.add(totalTextField);		
		dateSearchTextField.putClientProperty(FlatClientProperties.TEXT_FIELD_SHOW_CLEAR_BUTTON, true);
		idSearchTextField.putClientProperty(FlatClientProperties.TEXT_FIELD_SHOW_CLEAR_BUTTON, true);
		filterPanel.add(searchButton);
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
		JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, purchaseeTablePanel, detailsTablePanel);
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
	private void createPurchaseTablePanel() {
		purchaseTableModel = new DefaultTableModel(PURCHASE_COLUMNS, 0) {
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
		purchaseTable = new JTable(purchaseTableModel);
		purchaseTable.setToolTipText("Click Derecho para Eliminar ; Click izquierdo para ver detalles");
		purchaseTable.setShowGrid(true);
		purchaseTable.getColumnModel().getColumn(purchaseTable.getColumnCount() - 1).setMaxWidth(90);
		purchaseTable.getColumnModel().getColumn(purchaseTable.getColumnCount() - 1).setMinWidth(90);
		purchaseTable.getColumnModel().getColumn(purchaseTable.getColumnCount() - 1).setPreferredWidth(90);
		purchaseTable.getColumnModel().getColumn(purchaseTable.getColumnCount() - 2).setPreferredWidth(90);
		purchaseTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {
                	int idSale = purchaseTable.getSelectedRow();
                	if(idSale != -1)
                		loadDetailsTable(purchaseTable.convertRowIndexToModel(idSale));
                }
			}
		});
		DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
		centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
		for (int i = 0; i < purchaseTable.getColumnCount(); i++) {
			Class<?> columnClass = purchaseTableModel.getColumnClass(i);
			if (Number.class.isAssignableFrom(columnClass))
				purchaseTable.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
		}

		purchaseeTablePanel = new JPanel(new BorderLayout());
		purchaseeTablePanel.add(LightTheme.createSubTitle("Historial Compras"), BorderLayout.NORTH);
		purchaseeTablePanel.add(new JScrollPane(purchaseTable), BorderLayout.CENTER);
		purchaseeTablePanel.add(totalTextField, BorderLayout.SOUTH);
	}

	private void clearFields() {
		purchaseTable.clearSelection();
		detailsTableModel.setRowCount(0);
		providerComboBox.setSelectedIndex(0);
		dpFilter.clearSelectedDate();
		idSearchTextField.setText("");
		totalTextField.setText("");
		
		messageLabel.setText("");
		messageLabel.setOpaque(false);
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
						providerComboBox.setSelectedIndex(0);
						dateSearchTextField.setEnabled(idSearchTextField.getText().isBlank());
						providerComboBox.setEnabled(idSearchTextField.getText().isBlank());
						loadPurchaseTable();
					});
					filtroTimer.setRepeats(false); 
					filtroTimer.start();
				}
			}
		};
		idSearchTextField.getDocument().addDocumentListener(debounceListener);
	}

	private void createJPopupMenu() {
		new JPopupMenuModifyDelete(purchaseTable, this::delete, "Eliminar Compra").addMenuItem("Agregar Archivo", this::uploadFile)
		.addMenuItem("Abrir Archivo", this::downloadFile).addMenuItem("Confirmar Pago",	this::confirmPay);
	}
	private void confirmPay() {
        try {
			controller.confirmPay((Long) purchaseTableModel.getValueAt(purchaseTable.convertRowIndexToModel(purchaseTable.getSelectedRow()), purchaseTable.getColumnModel().getColumnCount()-1));
			loadPurchaseTable();
        } catch (Exception e) {
			e.printStackTrace();
			setMessage(e.getMessage());
		}
	}
	private void uploadFile() {
		JFileChooser fileChooser = new JFileChooser();
        if (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            try {
                controller.uploadBill((Long) purchaseTableModel.getValueAt(purchaseTable.convertRowIndexToModel(purchaseTable.getSelectedRow()), purchaseTable.getColumnModel().getColumnCount()-1), file);
                JOptionPane.showMessageDialog(null, "Archivo subido correctamente");
            } catch (IOException ex) {
                ex.printStackTrace();
                setMessage("Error al subir archivo: " + ex.getMessage());
            } catch (ServerException e) {
				e.printStackTrace();
                setMessage("Error al subir archivo: " + e.getMessage());
			}
        }
	}
	private void downloadFile() {
		controller.downloadAndOpenFile((Long) purchaseTableModel.getValueAt(purchaseTable.convertRowIndexToModel(purchaseTable.getSelectedRow()), purchaseTable.getColumnModel().getColumnCount()-1));
	}

	@Override
	public void refresh() {
		ActionListener[] listeners = providerComboBox.getActionListeners();
		for (ActionListener l : listeners) 
		    providerComboBox.removeActionListener(l);
		
		Provider c = providerComboBox.getSelectedItem();
		providerComboBox.fill(customerController.getProviders(), Provider.builder().id(null).companyName("Seleccione un proveedor").build());
		providerComboBox.setSelectedItem(c);

		for (ActionListener l : listeners) 
		    providerComboBox.addActionListener(l);
		
		clearFields();
		loadPurchaseTable();
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