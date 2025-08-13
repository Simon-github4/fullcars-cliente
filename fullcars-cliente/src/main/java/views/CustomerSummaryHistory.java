package views;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.swing.BorderFactory;
import javax.swing.JButton;
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
import Utils.ServerException;
import controller.CustomerController;
import controller.PayController;
import controller.SaleController;
import dtos.CustomerSummaryDTO;
import interfaces.Refreshable;
import model.client.entities.Customer;
import model.client.entities.Pay;
import model.client.entities.Purchase;
import model.client.entities.PurchaseDetail;
import model.client.entities.Sale;
import raven.datetime.DatePicker;
import raven.datetime.event.DateSelectionEvent;
import raven.datetime.event.DateSelectionListener;
import views.components.JPopupMenuModifyDelete;
import views.components.LightTheme;

public class CustomerSummaryHistory extends JPanel implements Refreshable{
	private static final long serialVersionUID = 1L;

	private final CustomerController controller;
	private final PayController payController;
	private final SaleController saleController;
	private List<Sale> salesList = new ArrayList<>();
	private List<Pay> paysList = new ArrayList<>();
	private JLabel customerInfoLabel = new JLabel("", JLabel.CENTER);
	
	private JPanel paysTablePanel;
	private static final Object[] PAYS_COLUMNS = {"Venta asociada", "Fecha", "Monto", "Metodo de Pago", "id" };
	private JTable paysTable;
	private DefaultTableModel paysTableModel;
	
	private JPanel saleTablePanel;
	private static final Object[] SALE_COLUMNS = {"Fecha", "Total", "Saldo pendiente", "Nro. Venta" };
	private JTable saleTable;
	private DefaultTableModel saleTableModel;

	private JTextField idSearchTextField = new JTextField("", 10);
	private JFormattedTextField dateSearchTextField = new JFormattedTextField();
	private DatePicker dpFilter = new DatePicker();
	private JButton refreshButton = new JButton("Actualizar Datos", Icons.REFRESH.create(18, 18));
	private JButton addPayButton = new JButton("Agregar Nuevo Pago", Icons.NEW.create(18, 18));
	private JButton showAllButton = new JButton("Mostrar Todos los Pagos");

	private TableRowSorter<DefaultTableModel> sorter;
	private Timer filtroTimer;
	private JLabel messageLabel;

	public CustomerSummaryHistory(CustomerController controller, PayController payController, SaleController saleController) {
		this.controller = controller;
		this.payController = payController;
		this.saleController = saleController;
		
		setLayout(new BorderLayout(0, 0));
		
		createPaysTablePanel();
		createSaleTablePanel();
		createJPopupMenu();
		createMessageLabel();
		initUI();

		sorter = new TableRowSorter<>(saleTableModel);
		saleTable.setRowSorter(sorter);
		setupLiveFilterListeners();
		
		//loadPurchaseTable();
	}

	private void deletePayment() {			
		try {
			Long idPay = (Long) paysTableModel.getValueAt(paysTable.convertRowIndexToModel(paysTable.getSelectedRow()), paysTable.getColumnModel().getColumnCount()-1);
			
			if(JOptionPane.showConfirmDialog(null, "Desea eliminar el Pago?",  "", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION){
				payController.delete(idPay);
				refresh();
			}
		}catch(ServerException se) {
			setMessage(se.getMessage());
		} catch (IOException ioe) {
			setMessage(ioe.getMessage());
		}
	}
	
	private void addPayment() {
		int row = saleTable.getSelectedRow();
		if(row == -1)
			JOptionPane.showMessageDialog(null, "Debe seleccionar una Venta asociada al Pago");
		else {
			row = saleTable.convertRowIndexToModel(row);
			PayForm dialog = new PayForm(null, controller.getCustomer(controller.getCustomerSelectedId()), salesList.get(row));
	        Pay pay = dialog.showDialog();
	        if (pay != null) 
	        	try {
					payController.save(pay);
					refresh();
				} catch (Exception e) {
					JOptionPane.showMessageDialog(null, e.getMessage());
				}
	        else 
				JOptionPane.showMessageDialog(null, "Pago cancelado");
	    }	
	}
	
	private void refreshFromDB() {
		if(controller.getCustomerSelectedId() != null) {
			CustomerSummaryDTO sum = controller.getCustomerSummary(controller.getCustomerSelectedId());
			customerInfoLabel.setText(sum.getCustomer().getFullName()+" - Cuit: "+sum.getCustomer().getCuit());
			paysList = sum.getPayments();
			salesList = sum.getSales();
			loadData();
		}else
			JOptionPane.showMessageDialog(this, "Seleccione un Cliente de la pestaña 'Clientes' para ver su balance");	
	}
	
	private void loadData() {
		saleTableModel.setRowCount(0);
		sorter.setSortKeys(null);// resets column order
		
		long saleBalance;
		for(Sale s : salesList) 
			if(dpFilter.getSelectedDateRange() == null || s.getDate().compareTo(dpFilter.getSelectedDateRange()[0]) >= 0 && 
														  s.getDate().compareTo(dpFilter.getSelectedDateRange()[1]) <= 0){
				saleBalance = s.getTotal();
				saleBalance -= paysList.stream()
			           .filter(p -> p.getSale().getId().equals(s.getId()))
			           .mapToDouble(Pay::getAmount)
			           .sum();
				Object[] row = {s.getDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")), s.getTotal(), saleBalance, s.getId() };
				saleTableModel.addRow(row);
			}
		loadPays();
		
	}
	private void loadPays() {
		paysTableModel.setRowCount(0);

		int modelRow = -1;
		int viewRow = saleTable.getSelectedRow();
		if(viewRow != -1)
			modelRow = saleTable.convertRowIndexToModel(saleTable.getSelectedRow());
		for (Pay s : paysList) 
			if(   viewRow == -1 && dpFilter.getSelectedDateRange() == null 
			   || viewRow == -1 && (s.getDate().compareTo(dpFilter.getSelectedDateRange()[0]) >= 0 && s.getDate().compareTo(dpFilter.getSelectedDateRange()[1]) <= 0)
			   || viewRow != -1 && salesList.get(modelRow).getId().equals(s.getSale().getId()) && (dpFilter.getSelectedDateRange() == null || (s.getDate().compareTo(dpFilter.getSelectedDateRange()[0]) >= 0 && s.getDate().compareTo(dpFilter.getSelectedDateRange()[1]) <= 0))) {
				
				//totalSold += s.getAmount();
				Object[] rowT = {s.getSale().getId(), s.getDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")), s.getAmount(), s.getPaymentMethod(), s.getId() };
				paysTableModel.addRow(rowT);
			}
	}
	
	private void initUI() {
		refreshButton.addActionListener(e -> refreshFromDB());
		showAllButton.addActionListener(e -> saleTable.clearSelection());
		addPayButton.addActionListener(e -> addPayment());
		dpFilter.setDateSelectionMode(DatePicker.DateSelectionMode.BETWEEN_DATE_SELECTED);
		dpFilter.setUsePanelOption(true);
		dpFilter.setBackground(Color.GRAY); // Color de fondo oscuro
		dpFilter.setDateFormat("dd/MM/yyyy");
		dpFilter.addDateSelectionListener(new DateSelectionListener() {
			@Override
			public void dateSelected(DateSelectionEvent dateSelectionEvent) {
				loadData();
			}
		});
		dpFilter.setEditor(dateSearchTextField);
		
		JPanel north = new JPanel(new BorderLayout());
		JPanel titulo = LightTheme.createTitle("Balance Cliente");
		titulo.add(customerInfoLabel, BorderLayout.SOUTH);
		customerInfoLabel.setFont(LightTheme.getSubTitleFont().deriveFont(19f));
		
		JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		filterPanel.add(new JLabel("Nro Venta: ", JLabel.RIGHT));
		filterPanel.add(idSearchTextField);
		filterPanel.add(new JLabel("Desde/Hasta ", JLabel.RIGHT));
		filterPanel.add(dateSearchTextField);
		dateSearchTextField.putClientProperty(FlatClientProperties.TEXT_FIELD_SHOW_CLEAR_BUTTON, true);
		idSearchTextField.putClientProperty(FlatClientProperties.TEXT_FIELD_SHOW_CLEAR_BUTTON, true);
		filterPanel.add(refreshButton);
		LightTheme.aplicarEstiloPrimario(refreshButton);
		filterPanel.add(addPayButton);
		LightTheme.aplicarEstiloPrimario(addPayButton);
		filterPanel.add(showAllButton);
		LightTheme.aplicarEstiloSecundario(showAllButton);
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
		JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, saleTablePanel, paysTablePanel);
		splitPane.setResizeWeight(1); // El panel de arriba no se estira al redimensionar
		splitPane.setDividerSize(8);
		splitPane.setDividerLocation(splitPane.getWidth() - paysTablePanel.getPreferredSize().width);
		add(splitPane, BorderLayout.CENTER);
//--------------------------------------------------------CENTER PANEL------------------------------------------------------------
	}

	private void createPaysTablePanel() {
		paysTableModel = new DefaultTableModel(PAYS_COLUMNS, 0) {
			private static final long serialVersionUID = 2888488979870981328L;
			@Override
			public boolean isCellEditable(int row, int column) {
				return false;
			}
			@Override
			public Class<?> getColumnClass(int column) {
				switch (column) {
				case 0:
					return Long.class;
				case 2:
					return Long.class;
				case 4:
					return Long.class;
				default:
					return Object.class;
				}
			}
		};
		paysTable = new JTable(paysTableModel);
		paysTable.setToolTipText("Click Derecho para Eliminar");
		paysTable.setShowGrid(true);
		paysTable.getColumnModel().getColumn(paysTable.getColumnCount() - 1).setMaxWidth(0);
		paysTable.getColumnModel().getColumn(paysTable.getColumnCount() - 1).setMinWidth(0);
		paysTable.getColumnModel().getColumn(paysTable.getColumnCount() - 1).setPreferredWidth(0);

		DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
		centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
		for (int i = 0; i < paysTable.getColumnCount(); i++) {
			Class<?> columnClass = paysTableModel.getColumnClass(i);
			if (Number.class.isAssignableFrom(columnClass))
				paysTable.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
		}

		paysTablePanel = new JPanel(new BorderLayout());
		paysTablePanel.add(new JScrollPane(paysTable), BorderLayout.CENTER);
		paysTablePanel.add(LightTheme.createSubTitle("Pagos"), BorderLayout.NORTH);
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
				default:
					return Object.class;
				}
			}
		};
		saleTable = new JTable(saleTableModel);
		saleTable.setToolTipText("Selecciona una venta(click izquierdo) para ver sus pagos asociados");
		saleTable.setShowGrid(true);
		saleTable.getColumnModel().getColumn(saleTable.getColumnCount() - 1).setMaxWidth(90);
		saleTable.getColumnModel().getColumn(saleTable.getColumnCount() - 1).setMinWidth(90);
		saleTable.getColumnModel().getColumn(saleTable.getColumnCount() - 1).setPreferredWidth(90);
		saleTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {
                	loadPays();
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
	}

	
	private void clearFields() {
		saleTable.clearSelection();
		paysTableModel.setRowCount(0);
		saleTableModel.setRowCount(0);
		dpFilter.clearSelectedDate();
		idSearchTextField.setText("");
		customerInfoLabel.setText("");
		
		messageLabel.setText("");
		messageLabel.setOpaque(false);
	}

	private void applyCombinedFilters() {
		String saleNumberText = idSearchTextField.getText().trim();
		List<RowFilter<Object, Object>> filters = new ArrayList<>();

		if (!saleNumberText.isEmpty())
			filters.add(RowFilter.regexFilter("(?i)^" + Pattern.quote(saleNumberText), 3)); // Columna Nro Compra

		if (filters.isEmpty())
			sorter.setRowFilter(null); // Sin filtro
		else
			sorter.setRowFilter(RowFilter.andFilter(filters));
	}
	
	private void setupLiveFilterListeners() {
		DocumentListener debounceListener = new DocumentListener() {
			@Override
			public void insertUpdate(DocumentEvent e) {
				reiniciarTimer();
			}
			@Override
			public void removeUpdate(DocumentEvent e) {
				reiniciarTimer();
			}
			@Override
			public void changedUpdate(DocumentEvent e) {
				reiniciarTimer();
			}
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
		idSearchTextField.getDocument().addDocumentListener(debounceListener);
	}

	private void createJPopupMenu() {
		new JPopupMenuModifyDelete(paysTable, this::deletePayment, "Eliminar Pago");
	}

	@Override
	public void refresh() {
		clearFields();
		refreshFromDB();
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