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

import controller.BrandController;
import controller.ModelController;
import interfaces.Refreshable;
import model.client.entities.Brand;
import model.client.entities.Model;
import views.components.JPopupMenuModifyDelete;
import views.components.LightTheme;
import views.components.NewModifyButton;
import views.components.TypedComboBox;

public class ModelForm extends JPanel implements Refreshable {
    private static final long serialVersionUID = 1L;

    private final ModelController controller;
    private final BrandController brandController;

    private JPanel inputPanel;
    private JPanel tablePanel;
    private static final Object[] COLUMNS = { "Nombre", "Marca", "id" };
    private JTable table;
    private DefaultTableModel tableModel;
    private JLabel messageLabel;

    private NewModifyButton confirmButton = new NewModifyButton();
    private JTextField nameTextField = new JTextField("", 24);
    private TypedComboBox<Brand> brandComboBox = new TypedComboBox<>(b-> b.getName());
    private JTextField idTextField = new JTextField("", 10);
    private JButton searchButton = new JButton("Actualizar");

    public ModelForm(ModelController controller, BrandController brandController) {
        this.controller = controller;
        this.brandController = brandController;

        setLayout(new BorderLayout(0, 0));
        createInputPanel();
        createTablePanel();
        createJPopupMenu();
        createMessageLabel();
        initUI();
    }

    private void save() {
        Brand selectedBrand = (Brand) brandComboBox.getSelectedItem();
        if (selectedBrand == null) {
            setMessage("Debe seleccionar una marca");
            return;
        }

        Model m = new Model(null, nameTextField.getText(), selectedBrand);

        if (confirmButton.isInModifyMode()) {
            Long id = confirmButton.getIdToModify();
            m.setId(id);
        }
        try {
        	controller.save(m);
            clearFields();
            loadTable();
        }catch(Exception e) { 
            setMessage("Error: "+e.getMessage());
        }
    }

    private void delete() {
        try {
            Long id = (Long) tableModel.getValueAt(table.getSelectedRow(),
                    table.getColumnModel().getColumnIndex("id"));
            if (JOptionPane.showConfirmDialog(null, "Desea eliminar el modelo: " + id, "",
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

        List<Model> models = controller.getModels();
        for (Model m : models) {
            Object[] row = { m.getName(), m.getBrand() != null ? m.getBrand().getName() : "", m.getId() };
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

        JPanel titlePanel = new JPanel(new GridLayout(1, 1));
        titlePanel.add(LightTheme.createTitle("MODELOS"));
        inputPanel.add(titlePanel);

        idTextField.setToolTipText("Identificador del modelo (no se modifica)");
        idTextField.setEnabled(false);

        JPanel horizontalPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        horizontalPanel.setPreferredSize(new Dimension(400, 30));
        horizontalPanel.add(new JLabel("Nombre", JLabel.RIGHT));
		nameTextField.addActionListener(e-> save());
		horizontalPanel.add(nameTextField);
        horizontalPanel.add(new JLabel("Marca", JLabel.RIGHT));
        horizontalPanel.add(brandComboBox);
        horizontalPanel.add(new JLabel("Id", JLabel.RIGHT));
        horizontalPanel.add(idTextField);

        inputPanel.add(horizontalPanel);

        JPanel buttonsPanel = new JPanel(new FlowLayout());

        confirmButton.addActionListener(e -> {
            if (validateFields())
                save();
        });
        LightTheme.aplicarEstiloPrimario(confirmButton);
        confirmButton.setPreferredSize(new Dimension(250, 40));
        buttonsPanel.add(confirmButton);

        JButton cancel = new JButton("Cancelar");
        cancel.addActionListener(e -> clearFields());
        LightTheme.aplicarEstiloSecundario(cancel);
        cancel.setPreferredSize(new Dimension(250, 40));
        buttonsPanel.add(cancel);

        searchButton.setPreferredSize(new Dimension(250, 40));
        buttonsPanel.add(searchButton);

        inputPanel.add(buttonsPanel);

        loadBrands();
    }

    private void loadBrands() {
        brandComboBox.removeAllItems();
        for (Brand b : brandController.getBrands()) {
            brandComboBox.addItem(b);
        }
    }

    @SuppressWarnings("serial")
    private void createTablePanel() {
        tableModel = new DefaultTableModel(COLUMNS, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }

            @Override
            public Class<?> getColumnClass(int column) {
                switch (column) {
                    case 2: return Long.class;
                    default: return Object.class;
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
            Long id = (Long) tableModel.getValueAt(row, table.getColumnModel().getColumnIndex("id"));
            String name = (String) tableModel.getValueAt(row, table.getColumnModel().getColumnIndex("Nombre"));
            String brandName = (String) tableModel.getValueAt(row, table.getColumnModel().getColumnIndex("Marca"));

            nameTextField.setText(name);
            idTextField.setText(id.toString());

            // Seleccionar la marca correspondiente
            for (int i = 0; i < brandComboBox.getItemCount(); i++) {
                if (brandComboBox.getItemAt(i).getName().equals(brandName)) {
                    brandComboBox.setSelectedIndex(i);
                    break;
                }
            }

            confirmButton.toModify(id);
        }
    }

    private boolean validateFields() {
        if (nameTextField.getText().isBlank()) {
            setMessage("El nombre no puede estar vacío");
            return false;
        }
        if (brandComboBox.getSelectedItem() == null) {
            setMessage("Debe seleccionar una marca");
            return false;
        }
        return true;
    }

    private void clearFields() {
        table.clearSelection();
        nameTextField.setText("");
        idTextField.setText("");
        confirmButton.toNew();
        //brandComboBox.setSelectedIndex(-1);

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
        loadBrands();
    }
}

