package views;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

import Utils.Icons;
import controller.BrandController;
import interfaces.Refreshable;
import model.client.entities.Brand;
import views.components.JPopupMenuModifyDelete;
import views.components.LightTheme;
import views.components.NewModifyButton;

public class BrandForm extends JPanel implements Refreshable{
private static final long serialVersionUID = 1L;
	
	private final BrandController controller;
	
	private JPanel inputPanel;
	private JPanel tablePanel;
	private static final Object[] COLUMNS = { "Nombre", "id" };
	private JTable table;
	private DefaultTableModel tableModel;
	private JLabel messageLabel;
	
	private NewModifyButton confirmButton = new NewModifyButton();
	private JTextField nameTextField = new JTextField("", 29);
	private JTextField idTextField = new JTextField("", 10);
	private JButton searchButton = new JButton("Actualizar", Icons.REFRESH.create(24,24));
		
	public BrandForm(BrandController controller) {
		this.controller = controller;
	
		setLayout(new BorderLayout(0, 0));
		createInputPanel();
		createTablePanel();
		createJPopupMenu();
		createMessageLabel();
		initUI();
		
		//loadTable();
	}
	
	private void save() { 
		Brand m = new Brand(null, nameTextField.getText());
	
		if(confirmButton.isInModifyMode()) {
			Long id = confirmButton.getIdToModify();
			m.setId(id);
		}
		if (controller.save(m)) {
			clearFields();
			loadTable();
		} else
			setMessage("No se pudo Guardar");
	}
	
	private void delete() { 
		try {
			Long id = (Long) tableModel.getValueAt(table.getSelectedRow(),
					table.getColumnModel().getColumnIndex((String) "id"));
			if (JOptionPane.showConfirmDialog(null, "Desea eliminar la marca: " + id.toString(), "",
					JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
				controller.delete(id);
				clearFields();
				loadTable();
			}
		} catch (Exception e) {
			setMessage(e.getCause().getLocalizedMessage());
		}
	}
	
	private void loadTable() { 
		tableModel.setRowCount(0);
	
		List<Brand> brands = controller.getBrands();
		for (Brand c : brands) {
			Object[] row = { c.getName(), c.getId() };
			tableModel.addRow(row);
		}
	}
	
	private void initUI() {
		searchButton.addActionListener(e -> loadTable());
		LightTheme.aplicarEstiloPrimario(searchButton);
		
		add(tablePanel, BorderLayout.CENTER);
		add(inputPanel, BorderLayout.NORTH);
	}
	
	private void createInputPanel() { 
		inputPanel = new JPanel();
		inputPanel.setLayout(new BoxLayout(inputPanel, BoxLayout.Y_AXIS));
		
		JPanel titlePanel = new JPanel(new GridLayout(1,1));
		titlePanel.add(LightTheme.createTitle("MARCAS"));
		inputPanel.add(titlePanel);
	
		idTextField.setToolTipText("Identificador de la marca (no se modifica)");
		idTextField.setEnabled(false);
	
		JPanel horizontalPanel = new JPanel(new FlowLayout(FlowLayout.CENTER,10,5));
		horizontalPanel.setPreferredSize(new Dimension(300, 30));
		horizontalPanel.add(new JLabel("Nombre", JLabel.RIGHT));
		horizontalPanel.add(nameTextField);
		horizontalPanel.add(new JLabel("       Id", JLabel.RIGHT));
		horizontalPanel.add(idTextField);
		horizontalPanel.add(new JLabel("", JLabel.LEFT));
	
		inputPanel.add(horizontalPanel);
	
		JPanel space = new JPanel(new FlowLayout());
		space.setPreferredSize(new Dimension(1, 10));
		inputPanel.add(space);
		
		JPanel buttonsPanel = new JPanel(new FlowLayout());
	
		confirmButton.addActionListener(e -> {
			if (validateFields())
				save();
		});
		LightTheme.aplicarEstiloPrimario(confirmButton);
		confirmButton.setPreferredSize(new Dimension(250, 40));
		buttonsPanel.add(confirmButton);
	
		JButton cancel = new JButton("Cancelar", Icons.CLEAN.create());
		cancel.addActionListener(e -> clearFields());
		LightTheme.aplicarEstiloSecundario(cancel);
		cancel.setPreferredSize(new Dimension(250, 40));
		buttonsPanel.add(cancel);
	
		searchButton.setPreferredSize(new Dimension(250, 40));
		buttonsPanel.add(searchButton);
		
		inputPanel.add(buttonsPanel);
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
					return String.class;
				case 2:
					return Long.class;
				default:
					return Object.class;
				}
			}
		};
		table = new JTable(tableModel);
		table.setToolTipText("Click Derecho para Eliminar o Modificar");
		table.setShowGrid(true);
		table.getColumnModel().getColumn(table.getColumnCount() - 1).setMaxWidth(400);
		table.getColumnModel().getColumn(table.getColumnCount() - 1).setMinWidth(0);
		table.getColumnModel().getColumn(table.getColumnCount() - 1).setPreferredWidth(400);
	
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
		if (modelRow != -1) {
			int row = table.convertRowIndexToModel(modelRow);
			Long id = (Long) tableModel.getValueAt(row, table.getColumnModel().getColumnIndex((String) "id"));
			String name = (String) tableModel.getValueAt(row, table.getColumnModel().getColumnIndex((String) "Nombre"));
	
			nameTextField.setText(name);
			idTextField.setText(id.toString());
			confirmButton.toModify(id);
		}
	}
	
	private boolean validateFields() {
		if (nameTextField.getText().isBlank()) {
			setMessage("El nombre no puede estar vacio");
			return false;
		}
		return true;
	}
	
	private void clearFields() { 
		table.clearSelection();
		nameTextField.setText("");
		idTextField.setText("");
		confirmButton.toNew();
		
		messageLabel.setText("");
		messageLabel.setOpaque(false);
	}
	
	private void createJPopupMenu() {
		new JPopupMenuModifyDelete(table, this::setRowToEdit, this::delete);
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

	@Override
	public void refresh() {
		clearFields();
		loadTable();
	}
}
