package views;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

import Utils.ServerException;
import controller.CarPartController;
import controller.CustomerController;
import controller.SaleController;
import interfaces.Refreshable;
import model.client.entities.CarPart;
import model.client.entities.Customer;
import model.client.entities.Sale;
import model.client.entities.SaleDetail;
import raven.datetime.DatePicker;
import views.components.JPopupMenuModifyDelete;
import views.components.LightTheme;
import views.components.NewModifyButton;
import views.components.TypedComboBox;

public class SaleForm extends JPanel implements Refreshable{
private static final long serialVersionUID = 1L;
	
	private final SaleController controller;
	private final CarPartController carpartController;
	private final CustomerController customerController;
	
	private JPanel inputPanel;
	private JPanel tablePanel;
	private static final Object[] COLUMNS = {"Autoparte", "Cantidad", "Precio unitario", "SubTotal"};
	private JTable table;
	private DefaultTableModel tableModel;
	private List<SaleDetail> detailsList = new ArrayList<>();
	private JLabel messageLabel;
	
	private TypedComboBox<CarPart> carpartComboBox = new TypedComboBox<>(c -> c.getSku());
	private TypedComboBox<Customer> customerComboBox = new TypedComboBox<>(c -> c.getFullName());
	private DatePicker dpInput = new DatePicker();

	private JTextField quantityTextField = new JTextField(29);
	private NewModifyButton confirmButton = new NewModifyButton();
	

	public SaleForm(SaleController controller, CarPartController cpController, CustomerController custController) {
		this.controller = controller;
		this.carpartController = cpController;
		this.customerController = custController;
		
		setLayout(new BorderLayout(0, 0));
		createTablePanel();
		createInputPanel();
		createJPopupMenu();
		createMessageLabel();

		carpartComboBox.fill(carpartController.getCarParts(), CarPart.builder().id(null).sku("Seleccione una autoparte").build());
		customerComboBox.fill(customerController.getCustomers(), Customer.builder().fullName("Seleccione un Cliente").build());
	}

	private void save() {
		Sale sale = Sale.builder()
				.id(null)
			    .date(dpInput.getSelectedDate())
			    .customer(customerComboBox.getSelectedItem())
			    .saleNumber("")
			    .taxes(new BigDecimal(0))
			    .build();
		
		detailsList.forEach(d -> {
		    d.setSale(sale);
		});
		sale.setDetails(detailsList);
			
		try {
			controller.save(sale);
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
			CarPart cp = carpartComboBox.getSelectedItem();
			SaleDetail sd = new SaleDetail(quantity, cp.getBasePrice(), cp);
			
			detailsList.add(sd);
	        tableModel.addRow(new Object[]{ cp.getSku(), quantity, sd.getUnitPrice(), sd.getSubTotal() });

	        quantityTextField.setText("");
	        carpartComboBox.setSelectedIndex(0);
	        carpartComboBox.requestFocus();
        }
    }
	
	private void deleteDetail() {
		int row = table.getSelectedRow();
		detailsList.remove(row);
		tableModel.removeRow(row);
	}

	private void createInputPanel() {
		add(LightTheme.createTitle("Nueva Venta"), BorderLayout.NORTH);
		inputPanel = new JPanel();
		inputPanel.setLayout(new BoxLayout(inputPanel, BoxLayout.Y_AXIS));
		Dimension size = new Dimension(600, 9999);
		//inputPanel.setMaximumSize(size);
		//inputPanel.setPreferredSize(size);

		confirmButton.setText("Confirmar Venta");

		JPanel rowsPanel = new JPanel();//new GridLayout(0, 1));
		rowsPanel.setLayout(new BoxLayout(rowsPanel, BoxLayout.Y_AXIS));
		//rowsPanel.setBorder(BorderFactory.createEmptyBorder(0, 400, 0, 400)); // arriba, izquierda, abajo, derecha

		rowsPanel.add(new JLabel("Cliente", JLabel.LEFT));
		rowsPanel.add(customerComboBox);
		rowsPanel.add(new JLabel("  Fecha", JLabel.LEFT));
		JFormattedTextField dateInputTextField = new JFormattedTextField();
		dpInput.setEditor(dateInputTextField);
		dpInput.setUsePanelOption(true);
		dpInput.setBackground(Color.GRAY); // Color de fondo oscuro
		dpInput.setDateFormat("dd/MM/yyyy");
		rowsPanel.add(dateInputTextField);
		
		inputPanel.add(rowsPanel);
		inputPanel.add(tablePanel);
		
		JPanel buttonsPanel = new JPanel(new BorderLayout());
		buttonsPanel.setMaximumSize(new Dimension(500, 160));
		JPanel firsts = new JPanel(new GridLayout());

		confirmButton.addActionListener(e -> {
			if (validateSaleFields())
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
		
		JPanel contenedorCentral = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 20));
		contenedorCentral.add(inputPanel);
		contenedorCentral.setMaximumSize(size);
		contenedorCentral.setPreferredSize(size);
		add(contenedorCentral, BorderLayout.CENTER);
		
		//add(inputPanel, BorderLayout.CENTER);
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

		tablePanel = new JPanel(new BorderLayout(0,8));
		tablePanel.add(new JScrollPane(table), BorderLayout.CENTER);

		JPanel north = new JPanel(new BorderLayout());
		//north.add(LightTheme.createSubTitle("Detalles"), BorderLayout.NORTH);
		
		JPanel fields = new JPanel(new GridLayout(0,2));
		fields.setBorder(new EmptyBorder(0,400,0,400));
		fields.add(new JLabel("  AutoParte", JLabel.LEFT));
		fields.add(new JLabel("  Cantidad", JLabel.LEFT));
		fields.add(carpartComboBox);
		carpartComboBox.addActionListener(e-> quantityTextField.requestFocus());
		fields.add(quantityTextField);
		quantityTextField.addActionListener(e -> addDetail());
		quantityTextField.putClientProperty("JTextField.placeholderText", "ENTER para agregar detalle");

		north.add(fields, BorderLayout.CENTER);
		
		tablePanel.add(north, BorderLayout.NORTH);
		tablePanel.setBorder(BorderFactory.createCompoundBorder(
			    BorderFactory.createEmptyBorder(0, 10, 5, 10),  
			    BorderFactory.createTitledBorder(
			        BorderFactory.createEtchedBorder(EtchedBorder.LOWERED),
			        "Detalles",
			        TitledBorder.LEFT,
			        TitledBorder.TOP,
			        LightTheme.SUBTITLE_FONT,
			        Color.BLACK
			    )
		));
	}

	private boolean validateDetailFields() {
		if (carpartComboBox.getSelectedIndex() == 0) {
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
		if(detailsList.size() == 0) {
			setMessage("Debe agregar al menos un detalle");
			return false;
		}
		return true;
	}
	private boolean validateSaleFields() {
		if (dpInput.getSelectedDate() == null) {
			setMessage("Debe seleccionar una fecha");
			return false;
		}
		if (customerComboBox.getSelectedIndex() == 0) {
			setMessage("Debe seleccionar un cliente");
			return false;
		}
		return true;
	}

	private void clearFields() {
		tableModel.setRowCount(0);;
		customerComboBox.setSelectedIndex(0);
		dpInput.setSelectedDate(LocalDate.now());

		detailsList = new ArrayList<>();
		carpartComboBox.setSelectedIndex(0);
		quantityTextField.setText("");

		messageLabel.setText("");
		messageLabel.setOpaque(false);
	}
	
	private void createJPopupMenu() {
		new JPopupMenuModifyDelete(table, this::deleteDetail, "Eliminar detalle");
	}

	@Override
	public void refresh() {
		clearFields();
		carpartComboBox.fill(carpartController.getCarParts(), CarPart.builder().id(null).sku("Seleccione una autoparte").build());
		customerComboBox.fill(customerController.getCustomers(), Customer.builder().fullName("Seleccione un Cliente").build());
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
