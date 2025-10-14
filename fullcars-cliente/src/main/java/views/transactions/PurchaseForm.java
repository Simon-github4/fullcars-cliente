package views.transactions;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.Timer;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

import Utils.Icons;
import Utils.NumberFormatArg;
import Utils.ServerException;
import controller.CarPartController;
import controller.ProviderController;
import controller.PurchaseController;
import interfaces.Refreshable;
import model.client.entities.CarPart;
import model.client.entities.Provider;
import model.client.entities.ProviderMapping;
import model.client.entities.ProviderPart;
import model.client.entities.Purchase;
import model.client.entities.PurchaseDetail;
import raven.datetime.DatePicker;
import views.carpart.CarPartDialog;
import views.carpart.CarPartSearchDialog;
import views.carpart.ProviderPartsDialog;
import views.components.AutocompleteField;
import views.components.BigDecimalField;
import views.components.DatePickerS;
import views.components.JPopupMenuModifyDelete;
import views.components.LightTheme;
import views.components.NewModifyButton;

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
    private final AutocompleteField<Provider> fieldProvider = new AutocompleteField<Provider>();
	private DatePicker dpInput = new DatePickerS();
	private JTextField facturaNumberTextField = new JTextField(29);

	private JTextField quantityTextField = new JTextField(29);
	private BigDecimalField unitPriceTextField = new BigDecimalField(20);
	private NewModifyButton confirmButton = new NewModifyButton();

	private JLabel carpartNameLabel = new JLabel("", JLabel.LEFT);
	
	public PurchaseForm(PurchaseController controller, CarPartController cpController, ProviderController custController) {
		this.controller = controller;
		this.carpartController = cpController;
		this.providerController = custController;
		
		setLayout(new BorderLayout(0, 0));
		createTablePanel();
		createInputPanel();
		createHelperPanel();
		createJPopupMenu();
		createMessageLabel();

		//carpartComboBox.fill(carpartController.getCarParts(), CarPart.builder().id(null).sku("Seleccione una autoparte").build());
		//providerComboBox.fill(providerController.getProviders(), Provider.builder().companyName("Seleccione un Proveedor").build());
	}

	private void save() {
		Purchase purchase = Purchase.builder()
				.id(null)
				.facturaNumber(facturaNumberTextField.getText())
			    .date(dpInput.getSelectedDate())
			    .provider(fieldProvider.getSelectedItem())
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
			PurchaseDetail purchaseDetail = new PurchaseDetail(quantity, unitPriceTextField.getBigDecimal(), detailCarpart);
			
			detailsList.add(purchaseDetail);
	        tableModel.addRow(new Object[]{ detailCarpart.getSku(), quantity,
	        								NumberFormatArg.format(purchaseDetail.getUnitPrice()),
	        								 NumberFormatArg.format(purchaseDetail.getSubTotal()) });

	        quantityTextField.setText("");
	        carpartTextField.setText("");
			unitPriceTextField.clear();
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

		JPanel fieldsPanel = new JPanel(new GridLayout(0, 2));
		fieldsPanel.setMaximumSize(size);

		fieldsPanel.add(new JLabel("Proveedor", JLabel.LEFT));
		fieldsPanel.add(new JLabel("Fecha", JLabel.LEFT));
		fieldsPanel.add(fieldProvider);
		JFormattedTextField dateInputTextField = new JFormattedTextField();
		dpInput.setEditor(dateInputTextField);
		dpInput.setUsePanelOption(true);
		dpInput.setBackground(Color.GRAY); 
		dpInput.setDateFormat("dd/MM/yyyy");
		fieldsPanel.add(dateInputTextField);
		mainPanel.add(fieldsPanel);
		
		fieldsPanel = new JPanel(new GridLayout(0,1));
		fieldsPanel.setMaximumSize(size);
		fieldsPanel.add(new JLabel("Numero de Factura", JLabel.LEFT));
		fieldsPanel.add(facturaNumberTextField);
		mainPanel.add(fieldsPanel);
		
		mainPanel.add(tablePanel);
		
		JPanel buttonsPanel = new JPanel(new GridLayout(1,0));
		buttonsPanel.setMaximumSize(new Dimension(500, 160));

		confirmButton.setText("Confirmar Compra");
		confirmButton.addActionListener(e -> {
			if (validatePurchaseFields())
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
		table.getColumnModel().getColumn(1).setMaxWidth(90);
		table.getColumnModel().getColumn(1).setMinWidth(90);
		table.getColumnModel().getColumn(1).setPreferredWidth(90);

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
		
		JPanel fieldsDetailsRow = new JPanel(new GridLayout(1,3));
		fieldsDetailsRow.setMaximumSize(new Dimension(750, Integer.MAX_VALUE));
		fieldsDetailsRow.add(new JLabel("SKU", JLabel.CENTER));
		fieldsDetailsRow.add(new JLabel("Cantidad", JLabel.CENTER));
		fieldsDetailsRow.add(new JLabel("Precio Unitario", JLabel.CENTER));
		tablePanel.add(fieldsDetailsRow);
		
		fieldsDetailsRow = new JPanel(new GridLayout(0,3));
		fieldsDetailsRow.setMaximumSize(new Dimension(750, Integer.MAX_VALUE));
		fieldsDetailsRow.add(carpartTextField);
		carpartTextField.putClientProperty("JTextField.placeholderText", "ENTER para buscar autoparte");
		carpartTextField.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void insertUpdate(DocumentEvent e) {
				detailCarpart = null;
				carpartNameLabel.setText("");
				unitPriceTextField.clear();
			}
			@Override
			public void removeUpdate(DocumentEvent e) {
				detailCarpart = null;
				carpartNameLabel.setText("");
				unitPriceTextField.clear();
			}
			@Override
			public void changedUpdate(DocumentEvent e) {
				//detailCarpart = null;
				//carpartNameLabel.setText("");
				//unitPriceTextField.clear();
			}
		});
		carpartTextField.addActionListener(e-> setDetailCarPart());		
		fieldsDetailsRow.add(quantityTextField);
		quantityTextField.addActionListener(e -> unitPriceTextField.requestFocus());
		quantityTextField.putClientProperty("JTextField.placeholderText", "ENTER para agregar detalle");
		fieldsDetailsRow.add(unitPriceTextField);
		unitPriceTextField.addActionListener(e-> addDetail());		
		tablePanel.add(fieldsDetailsRow);

		fieldsDetailsRow = new JPanel(new GridLayout());
		fieldsDetailsRow.setMaximumSize(new Dimension(750, Integer.MAX_VALUE));
		fieldsDetailsRow.add(carpartNameLabel);
		tablePanel.add(fieldsDetailsRow);

		fieldsDetailsRow = new JPanel(new GridLayout(1,0,0,8));
		fieldsDetailsRow.setMaximumSize(new Dimension(750, Integer.MAX_VALUE));
		fieldsDetailsRow.setBorder(BorderFactory.createEmptyBorder(8,0,7,0));
		fieldsDetailsRow.add(new JScrollPane(table), BorderLayout.CENTER);
		tablePanel.add(fieldsDetailsRow);
	}

	private void setDetailCarPart() {
		detailCarpart = carpartController.getCarPart(carpartTextField.getText());
		if(detailCarpart == null) {
			setMessage("No se escontro autoparte con ese SKU");
		}else {
			carpartNameLabel.setText(detailCarpart.getName());
			unitPriceTextField.setBigDecimal(detailCarpart.getBasePrice());  //precioventa NO?
			quantityTextField.requestFocus();
		}	
	}
	private void setDetailCarPart(String carpartSku) {
		if(carpartSku == null) {
			setMessage("No se escontro autoparte con ese SKU");
		}else {
			carpartTextField.setText(carpartSku);
			for (ActionListener al : carpartTextField.getActionListeners()) 
			    al.actionPerformed(new ActionEvent(carpartTextField, ActionEvent.ACTION_PERFORMED, null));
			//Fire event and set namelabel and detailCarpart.
		}	
	}
	
	private void createHelperPanel() {
		InputMap inputMap = getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap actionMap = getActionMap();
        
        // Definir atajo ALT B (buscar)
        KeyStroke ctrlS = KeyStroke.getKeyStroke(KeyEvent.VK_B, InputEvent.ALT_DOWN_MASK);
        inputMap.put(ctrlS, "buscarCarpartAction");
        actionMap.put("buscarCarpartAction", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
            	CarPartSearchDialog dialog = new CarPartSearchDialog(null); // ventana padre
        	    dialog.setVisible(true);
        	    String sku = dialog.getSelectedCarPartSku();
        	    if(sku != null) {
        	    	setDetailCarPart(sku);
        	    }
            }
        });
        // Atajo ALT A (Agregar)
        KeyStroke f1 = KeyStroke.getKeyStroke(KeyEvent.VK_A, InputEvent.ALT_DOWN_MASK);
        inputMap.put(f1, "newCarpartAction");
        actionMap.put("newCarpartAction", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
            	CarPartDialog dialog = new CarPartDialog(); 
        	    dialog.setVisible(true);

        	    CarPart nuevo = dialog.getCreatedPart();
        	    if (nuevo != null) {
        	        setDetailCarPart(nuevo.getSku());
        	    }
            }
        });
        // Atajo ALT P (buscar Precios)
        KeyStroke altP = KeyStroke.getKeyStroke(KeyEvent.VK_P, InputEvent.ALT_DOWN_MASK);
        inputMap.put(altP, "searchPrices");
        actionMap.put("searchPrices", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
            	ProviderPartsDialog dialog = new ProviderPartsDialog(null, providerController.getProviderParts(), providerController.getProviders());
            	dialog.setVisible(true);

				ProviderPart seleccionada = dialog.getSelectedPart();
				CarPart nuevo = providerController.findOrCreateCarPartFromProviderPart(seleccionada);
				
				if (nuevo != null) 
					if (fieldProvider.getSelectedItem() != null && 
							fieldProvider.getSelectedItem().getId() != nuevo.getProvider().getId())
						setMessage("El proveedor debe coincidir con el seleccionado");
					else {
						//CarPartDialog newPartDialog = new CarPartDialog(seleccionada);
						//newPartDialog.setVisible(true);

						//nuevo = newPartDialog.getCreatedPart();
						//if (nuevo != null) {
							fieldProvider.setSelectedItem(providerController.getProvider(seleccionada.getProviderId()));
							setDetailCarPart(nuevo.getSku());
						//}
					}
				else
					setMessage("No se pudo obtener Ni Crear la Autoparte");

				dialog = null;
            	System.gc();
            }
        });
        
        JPanel panelAyuda = new JPanel();
        panelAyuda.setLayout(new BoxLayout(panelAyuda, BoxLayout.Y_AXIS));
        panelAyuda.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panelAyuda.setBackground(new Color(240, 240, 240));
        panelAyuda.setPreferredSize(new Dimension(180,0));

        JLabel titulo = new JLabel("Atajos de Teclado", JLabel.LEFT);
        titulo.setFont(new Font("Arial", Font.BOLD, 14));
        panelAyuda.add(titulo);
        panelAyuda.add(Box.createRigidArea(new Dimension(0, 10)));
        panelAyuda.add(new JLabel("ALT + A",  JLabel.CENTER));
        panelAyuda.add(new JLabel("Registrar AUTOPARTE", JLabel.LEFT));
        panelAyuda.add(Box.createRigidArea(new Dimension(0, 10)));
        panelAyuda.add(new JLabel("ALT + B", JLabel.CENTER));
        panelAyuda.add(new JLabel("Buscar AUTOPARTE", JLabel.LEFT));
        panelAyuda.add(Box.createRigidArea(new Dimension(0, 10)));
        panelAyuda.add(new JLabel("ALT + P", JLabel.CENTER));
        panelAyuda.add(new JLabel("Buscar x Cod Proveedor", JLabel.LEFT));
        
        JPanel west = new JPanel();
        west.setPreferredSize(panelAyuda.getPreferredSize());

        add(panelAyuda, BorderLayout.EAST);
        add(west, BorderLayout.WEST);
	}
	
	private boolean validateDetailFields() {
		if (detailCarpart == null) {
			setMessage("Debe seleccionar una autoparte");
			return false;
		}
		if(unitPriceTextField.getBigDecimal().compareTo(BigDecimal.ZERO) <= 0) {
			setMessage("Ingrece un precio unitario valido");
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
	private boolean validatePurchaseFields() {
		if (dpInput.getSelectedDate() == null) {
			setMessage("Debe seleccionar una fecha");
			return false;
		}
		if (fieldProvider.getSelectedItem() == null) {
			setMessage("Debe seleccionar un proveedor");
			return false;
		}
		if (facturaNumberTextField.getText().isBlank()) {
			setMessage("Debe introducir un Numero de Factura");
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
		fieldProvider.clearSelection();
		dpInput.setSelectedDate(LocalDate.now());

		detailsList = new ArrayList<>();
		carpartTextField.setText("");
		quantityTextField.setText("");
		unitPriceTextField.clear();
		facturaNumberTextField.setText("");
		
		messageLabel.setText("");
		messageLabel.setOpaque(false);
	}
	
	private void createJPopupMenu() {
		new JPopupMenuModifyDelete(table, this::deleteDetail, "Eliminar detalle");
	}

	@Override
	public void refresh() {
		fieldProvider.setItems(providerController.getProviders());
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
