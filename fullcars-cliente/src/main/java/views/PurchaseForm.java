package views;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.Timer;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

import Utils.Icons;
import Utils.ServerException;
import controller.CarPartController;
import controller.ProviderController;
import controller.PurchaseController;
import interfaces.Refreshable;
import model.client.entities.CarPart;
import model.client.entities.Provider;
import model.client.entities.Purchase;
import model.client.entities.PurchaseDetail;
import raven.datetime.DatePicker;
import views.components.JPopupMenuModifyDelete;
import views.components.LightTheme;
import views.components.NewModifyButton;
import views.components.TypedComboBox;

public class PurchaseForm extends JPanel implements Refreshable{
private static final long serialVersionUID = 1L;
	
	private final PurchaseController controller;
	private final CarPartController carpartController;
	private final ProviderController providerController;
	
	private JPanel mainPanel;
	private JPanel tablePanel;
	private static final Object[] COLUMNS = {"Autoparte", "Cantidad", "Precio unitario", "SubTotal"};
	private JTable table;
	private DefaultTableModel tableModel;
	private List<PurchaseDetail> detailsList = new ArrayList<>();
	private CarPart detailCarpart = null;
	private JLabel messageLabel;
	
	private JTextField carpartTextField = new JTextField(29);
	private TypedComboBox<Provider> providerComboBox = new TypedComboBox<>(c -> c.getCompanyName());
	private DatePicker dpInput = new DatePicker();

	private JTextField quantityTextField = new JTextField(29);
	private NewModifyButton confirmButton = new NewModifyButton();

	private JLabel carpartNameLabel = new JLabel("", JLabel.LEFT);
	
	public PurchaseForm(PurchaseController controller, CarPartController cpController, ProviderController custController) {
		this.controller = controller;
		this.carpartController = cpController;
		this.providerController = custController;
		
		setLayout(new BorderLayout(0, 0));
		createTablePanel();
		createInputPanel();
		createJPopupMenu();
		createMessageLabel();

		//carpartComboBox.fill(carpartController.getCarParts(), CarPart.builder().id(null).sku("Seleccione una autoparte").build());
		//providerComboBox.fill(providerController.getProviders(), Provider.builder().companyName("Seleccione un Proveedor").build());
	}

	private void save() {
		Purchase purchase = Purchase.builder()
				.id(null)
			    .date(dpInput.getSelectedDate())
			    .provider(providerComboBox.getSelectedItem())
			    .taxes(new BigDecimal(0))
			    .build();
		
		detailsList.forEach(d -> {
		    d.setPurchase(purchase);
		});
		purchase.setDetails(detailsList);
			
		try {
			controller.save(purchase);
			clearFields();
		} catch (ServerException se) {
			setMessage(se.getMessage());
		} catch (IOException ioe) {
			setMessage(ioe.getMessage());
		} catch (Exception e) {
			setMessage("No se pudo Guardar");
		}
	}

	private void addDetail() {
        if(validateDetailFields()) {
			Integer quantity = Integer.parseInt(quantityTextField.getText().trim());
			PurchaseDetail purchaseDetail = new PurchaseDetail(quantity, detailCarpart.getBasePrice(), detailCarpart);
			
			detailsList.add(purchaseDetail);
	        tableModel.addRow(new Object[]{ detailCarpart.getSku(), quantity, purchaseDetail.getUnitPrice(), purchaseDetail.getSubTotal() });

	        quantityTextField.setText("");
	        carpartTextField.setText("");
	        carpartTextField.requestFocus();
        }
    }
	
	private void deleteDetail() {
		int row = table.getSelectedRow();
		detailsList.remove(row);
		tableModel.removeRow(row);
	}

	private void createInputPanel() {
		carpartNameLabel.setForeground(LightTheme.COLOR_AZUL_FIRME);

		add(LightTheme.createTitle("Nueva Compra"), BorderLayout.NORTH);
		mainPanel = new JPanel();
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
		Dimension size = new Dimension(700, Integer.MAX_VALUE);

		JPanel fieldsPanel = new JPanel(new GridLayout(0, 1));
		fieldsPanel.setMaximumSize(size);

		fieldsPanel.add(new JLabel("Proveedor", JLabel.LEFT));
		fieldsPanel.add(providerComboBox);
		fieldsPanel.add(new JLabel("Fecha", JLabel.LEFT));
		JFormattedTextField dateInputTextField = new JFormattedTextField();
		dpInput.setEditor(dateInputTextField);
		dpInput.setUsePanelOption(true);
		dpInput.setBackground(Color.GRAY); 
		dpInput.setDateFormat("dd/MM/yyyy");
		fieldsPanel.add(dateInputTextField);
		
		mainPanel.add(fieldsPanel);
		mainPanel.add(tablePanel);
		
		JPanel buttonsPanel = new JPanel(new GridLayout(1,0));
		buttonsPanel.setMaximumSize(new Dimension(500, 160));

		confirmButton.setText("Confirmar Compra");
		confirmButton.addActionListener(e -> {
			if (validateSaleFields())
				save();
		});
		LightTheme.aplicarEstiloPrimario(confirmButton);
		confirmButton.setPreferredSize(new Dimension(120, 70));
		buttonsPanel.add(confirmButton);
		JButton cancel = new JButton("Cancelar", Icons.CLEAN.create());
		cancel.addActionListener(e -> clearFields());
		LightTheme.aplicarEstiloSecundario(cancel);
		cancel.setPreferredSize(new Dimension(250, 70));
		buttonsPanel.add(cancel);

		mainPanel.add(buttonsPanel);
		
		add(mainPanel, BorderLayout.CENTER);
	}

	@SuppressWarnings("serial")
	private void createTablePanel() {		
		tableModel = new DefaultTableModel(COLUMNS, 0) {
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
		table = new JTable(tableModel);
		table.setToolTipText("Click Derecho para Eliminar");
		table.setShowGrid(true);
		table.getColumnModel().getColumn(table.getColumnCount() - 1).setMaxWidth(90);
		table.getColumnModel().getColumn(table.getColumnCount() - 1).setMinWidth(90);
		table.getColumnModel().getColumn(table.getColumnCount() - 1).setPreferredWidth(90);

		DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
		centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
		for (int i = 0; i < table.getColumnCount(); i++) {
			Class<?> columnClass = tableModel.getColumnClass(i);
			if (Number.class.isAssignableFrom(columnClass))
				table.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
		}

		tablePanel = new JPanel();
		tablePanel.setLayout(new BoxLayout(tablePanel, BoxLayout.Y_AXIS));
		tablePanel.setBorder(BorderFactory.createCompoundBorder(
			    BorderFactory.createEmptyBorder(),  
			    BorderFactory.createTitledBorder(
			        BorderFactory.createEtchedBorder(),
			        "Detalles",
			        TitledBorder.CENTER,
			        TitledBorder.TOP,
			        LightTheme.SUBTITLE_FONT,
			        Color.BLACK
			    )
		));
		
		JPanel fieldsDetailsRow = new JPanel(new GridLayout(1,2));
		fieldsDetailsRow.setMaximumSize(new Dimension(750, Integer.MAX_VALUE));
		fieldsDetailsRow.add(new JLabel("  AutoParte:", JLabel.CENTER));
		fieldsDetailsRow.add(carpartNameLabel);
		fieldsDetailsRow.add(new JLabel("  Cantidad", JLabel.LEFT));
		tablePanel.add(fieldsDetailsRow);
		
		fieldsDetailsRow = new JPanel(new GridLayout(0,2));
		fieldsDetailsRow.setMaximumSize(new Dimension(750, Integer.MAX_VALUE));
		fieldsDetailsRow.add(carpartTextField);
		carpartTextField.putClientProperty("JTextField.placeholderText", "ENTER para buscar autoparte");
		carpartTextField.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void insertUpdate(DocumentEvent e) {
				detailCarpart = null;
				carpartNameLabel.setText("");
			}
			@Override
			public void removeUpdate(DocumentEvent e) {
				detailCarpart = null;
				carpartNameLabel.setText("");
			}
			@Override
			public void changedUpdate(DocumentEvent e) {
				detailCarpart = null;
				carpartNameLabel.setText("");
			}
		});
		carpartTextField.addActionListener(e-> {
			detailCarpart = carpartController.getCarPart(carpartTextField.getText());
			if(detailCarpart == null)
				setMessage("No se escontro autoparte con ese sku");
			else {
				carpartNameLabel.setText(detailCarpart.getName());
				quantityTextField.requestFocus();
			}			
		});		fieldsDetailsRow.add(quantityTextField);
		quantityTextField.addActionListener(e -> addDetail());
		quantityTextField.putClientProperty("JTextField.placeholderText", "ENTER para agregar detalle");
		tablePanel.add(fieldsDetailsRow);
		
		fieldsDetailsRow = new JPanel(new GridLayout(1,0,0,8));
		fieldsDetailsRow.setMaximumSize(new Dimension(750, Integer.MAX_VALUE));
		fieldsDetailsRow.setBorder(BorderFactory.createEmptyBorder(8,0,7,0));
		fieldsDetailsRow.add(new JScrollPane(table), BorderLayout.CENTER);
		tablePanel.add(fieldsDetailsRow);
	}

	private boolean validateDetailFields() {
		if (detailCarpart == null) {
			setMessage("Debe seleccionar una autoparte");
			return false;
		}
		try {
			if (Integer.parseInt(quantityTextField.getText()) <= 0) {
				setMessage("La cantidad debe ser mayor a cero");
				return false;
			}
		} catch (NumberFormatException e) {
			setMessage("Debe indicar una cantidad Valida (unicamente numeros)");
			return false;
		}
		return true;
	}
	private boolean validateSaleFields() {
		if (dpInput.getSelectedDate() == null) {
			setMessage("Debe seleccionar una fecha");
			return false;
		}
		if (providerComboBox.getSelectedIndex() == 0) {
			setMessage("Debe seleccionar un proveedor");
			return false;
		}

		if(detailsList.size() == 0) {
			setMessage("Debe agregar al menos un detalle");
			return false;
		}
		return true;
	}

	private void clearFields() {
		tableModel.setRowCount(0);;
		providerComboBox.setSelectedIndex(0);
		dpInput.setSelectedDate(LocalDate.now());

		detailsList = new ArrayList<>();
		carpartTextField.setText("");
		quantityTextField.setText("");

		messageLabel.setText("");
		messageLabel.setOpaque(false);
	}
	
	private void createJPopupMenu() {
		new JPopupMenuModifyDelete(table, this::deleteDetail, "Eliminar detalle");
	}

	@Override
	public void refresh() {
		providerComboBox.fill(providerController.getProviders(), Provider.builder().companyName("Seleccione un Proveedor").build());
		clearFields();
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
