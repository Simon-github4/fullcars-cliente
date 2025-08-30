package views;

import java.awt.GridLayout;
import java.util.List;
import java.util.function.Consumer;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import model.client.entities.Brand;
import model.client.entities.CarPart;
import model.client.entities.Category;
import model.client.entities.Provider;
import views.components.TypedComboBox;

public class CarPartInputPanel extends JPanel {

    private final JTextField nameTextField = new JTextField("", 29);
    private final JTextField descriptionTextField = new JTextField("", 29);
    private final JTextField skuTextField = new JTextField(29);
    private final JTextField stockTextField = new JTextField("", 29);
    private final JTextField sellPriceTextField = new JTextField("", 29);
    private final TypedComboBox<Provider> comboBoxProviders = new TypedComboBox<>(p -> p.getCompanyName());
    private final TypedComboBox<Brand> comboBoxBrands = new TypedComboBox<>(b -> b.getName());
    private final TypedComboBox<Category> comboBoxCategory = new TypedComboBox<>(c -> c.getName());

    public CarPartInputPanel() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        buildUI();
    }

    private void buildUI() {
        skuTextField.setEnabled(false);
        skuTextField.setToolTipText("Código interno generado automáticamente");
        stockTextField.setEnabled(false);
        stockTextField.setToolTipText("Stock disponible (solo lectura)");

        JPanel rowsPanel = new JPanel(new GridLayout(0, 1));

        rowsPanel.add(new JLabel("  Nombre", JLabel.LEFT));
        rowsPanel.add(nameTextField);
        rowsPanel.add(new JLabel("  Descripción", JLabel.LEFT));
        rowsPanel.add(descriptionTextField);

        JPanel columnPanel = new JPanel(new GridLayout(1, 0));
        columnPanel.add(new JLabel("  SKU", JLabel.LEFT));
        columnPanel.add(new JLabel("  Stock", JLabel.LEFT));
        rowsPanel.add(columnPanel);

        columnPanel = new JPanel(new GridLayout(1, 0));
        columnPanel.add(skuTextField);
        columnPanel.add(stockTextField);
        rowsPanel.add(columnPanel);

        rowsPanel.add(new JLabel("  Precio venta", JLabel.LEFT));
        rowsPanel.add(sellPriceTextField);

        columnPanel = new JPanel(new GridLayout(1, 0));
        columnPanel.add(new JLabel("  Marca", JLabel.LEFT));
        columnPanel.add(new JLabel("  Categoría", JLabel.LEFT));
        rowsPanel.add(columnPanel);

        columnPanel = new JPanel(new GridLayout(1, 0));
        columnPanel.add(comboBoxBrands);
        columnPanel.add(comboBoxCategory);
        rowsPanel.add(columnPanel);

        rowsPanel.add(new JLabel("  Proveedor", JLabel.LEFT));
        rowsPanel.add(comboBoxProviders);

        add(rowsPanel);
    }

    // ---------------- Métodos útiles ----------------

    public void fillCombos(List<Brand> brands, List<Category> categories, List<Provider> providers) {
        comboBoxBrands.fill(brands, new Brand(null, "Seleccione una Marca"));
        comboBoxCategory.fill(categories, new Category(null, "Seleccione una Categoría"));
        comboBoxProviders.fill(providers, Provider.builder().id(null).companyName("Seleccione un Proveedor").build());
    }

    public CarPart toCarPart() {
        return CarPart.builder()
                .name(nameTextField.getText())
                .description(descriptionTextField.getText())
                .sku(skuTextField.getText())
                .stock(stockTextField.getText().isBlank() ? 0L : Long.parseLong(stockTextField.getText()))
                .basePrice(Long.parseLong(sellPriceTextField.getText()))
                .brand(comboBoxBrands.getSelectedItem())
                .category(comboBoxCategory.getSelectedItem())
                .provider(comboBoxProviders.getSelectedItem())
                .build();
    }

    public void loadFrom(CarPart part) {
        nameTextField.setText(part.getName());
        descriptionTextField.setText(part.getDescription());
        skuTextField.setText(part.getSku());
        stockTextField.setText(part.getStock() == null ? "" : part.getStock().toString());
        sellPriceTextField.setText(part.getBasePrice() == null ? "" : part.getBasePrice().toString());
        comboBoxBrands.setSelectedItem(part.getBrand());
        comboBoxCategory.setSelectedItem(part.getCategory());
        comboBoxProviders.setSelectedItem(part.getProvider());
    }

    public void clearFields() {
        nameTextField.setText("");
        descriptionTextField.setText("");
        skuTextField.setText("");
        stockTextField.setText("");
        sellPriceTextField.setText("");
        comboBoxBrands.setSelectedIndex(0);
        comboBoxCategory.setSelectedIndex(0);
        comboBoxProviders.setSelectedIndex(0);
    }

    public boolean validateFields(Consumer<String> errorHandler) {
        if (nameTextField.getText().isBlank()) {
            errorHandler.accept("El nombre no puede estar vacío");
            return false;
        }
        if (nameTextField.getText().length() > 45) {
            errorHandler.accept("El nombre no puede superar los 45 caracteres");
            return false;
        }
        try {
            if (Long.parseLong(sellPriceTextField.getText()) < 0L) {
                errorHandler.accept("El precio no puede ser menor a 0");
                return false;
            }
        } catch (NumberFormatException e) {
            errorHandler.accept("Asegúrese de introducir números en Precio venta");
            return false;
        }
        if (comboBoxBrands.getSelectedIndex() == 0 || comboBoxCategory.getSelectedIndex() == 0) {
            errorHandler.accept("Debe seleccionar Marca y Categoría válidos");
            return false;
        }
        if (comboBoxProviders.getSelectedIndex() == 0) {
            errorHandler.accept("Debe seleccionar un Proveedor válido");
            return false;
        }
        return true;
    }
}

