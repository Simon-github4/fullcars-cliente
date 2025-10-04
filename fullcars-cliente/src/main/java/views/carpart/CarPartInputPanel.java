package views.carpart;

import java.awt.GridLayout;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import controller.AppContext;
import model.client.entities.Brand;
import model.client.entities.CarPart;
import model.client.entities.Category;
import model.client.entities.Model;
import model.client.entities.Provider;
import model.client.entities.ProviderPart;
import views.components.AutocompleteField;
import views.components.BigDecimalField;
import views.components.TypedComboBox;

public class CarPartInputPanel extends JPanel {

    private final JTextField nameTextField = new JTextField("", 29);
    private final JTextField descriptionTextField = new JTextField("", 29);
    private final JTextField skuTextField = new JTextField(29);
    private final JTextField stockTextField = new JTextField("", 29);
    private final BigDecimalField sellPriceTextField = new BigDecimalField(29);
    private final AutocompleteField<Provider> fieldProviders = new AutocompleteField<Provider>();
    private final AutocompleteField<Category> fieldCategory = new AutocompleteField<Category>();
    //private final TypedComboBox<Category> comboBoxCategory = new TypedComboBox<>(c -> c.getName());
	private final AutocompleteField<Model> fieldModels = new AutocompleteField<Model>();
;

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
        columnPanel.add(new JLabel("  Categoría", JLabel.LEFT));
        columnPanel.add(new JLabel("  SKU", JLabel.LEFT));
        rowsPanel.add(columnPanel);

        columnPanel = new JPanel(new GridLayout(1, 0));
        columnPanel.add(fieldCategory);
        columnPanel.add(skuTextField);
        //columnPanel.add(stockTextField);
        rowsPanel.add(columnPanel);

        rowsPanel.add(new JLabel("  Precio venta", JLabel.LEFT));
        rowsPanel.add(sellPriceTextField);

        columnPanel = new JPanel(new GridLayout(1, 0));
        columnPanel.add(new JLabel("  Modelo", JLabel.LEFT));
        rowsPanel.add(columnPanel);

        columnPanel = new JPanel(new GridLayout(1, 0));
        columnPanel.add(fieldModels);
        rowsPanel.add(columnPanel);

        rowsPanel.add(new JLabel("  Proveedor", JLabel.LEFT));
        rowsPanel.add(fieldProviders);

        add(rowsPanel);
    }

    // ---------------- Métodos útiles ----------------

    public void fillCombos(List<Model> models, List<Brand> brands, List<Category> categories, List<Provider> providers) {
    	Model selectedModel = fieldModels.getSelectedItem();
		Category selectedCategory = fieldCategory.getSelectedItem();
		Provider selectedProvider = fieldProviders.getSelectedItem();
		
		
        fieldCategory.setItems(categories);//, new Category(null, "Seleccione una Categoría"));
        fieldProviders.setItems(providers);
        fieldModels.setItems(models);

        if(selectedModel != null)
        	fieldModels.setSelectedItem(selectedModel);
        else
        	fieldModels.clearSelection();
        if(selectedCategory != null)
    		fieldCategory.setSelectedItem(selectedCategory);
        else
        	fieldCategory.clearSelection();
        if(selectedProvider != null)
    	    fieldProviders.setSelectedItem(selectedProvider);
        else
        	fieldProviders.clearSelection();
    }

    public CarPart toCarPart() {
        return CarPart.builder()
                .name(nameTextField.getText())
                .description(descriptionTextField.getText())
                .sku(skuTextField.getText())
                .stock(stockTextField.getText().isBlank() ? 0L : Long.parseLong(stockTextField.getText()))
                .basePrice(sellPriceTextField.getBigDecimal())
                .model(fieldModels.getSelectedItem())
                .category(fieldCategory.getSelectedItem())
                .provider(fieldProviders.getSelectedItem())
                .build();
    }

    public void loadFrom(CarPart part) {
        nameTextField.setText(part.getName());
        descriptionTextField.setText(part.getDescription());
        skuTextField.setText(part.getSku());
        stockTextField.setText(part.getStock() == null ? "" : part.getStock().toString());
        sellPriceTextField.setBigDecimal(part.getBasePrice() == null ? BigDecimal.ZERO : part.getBasePrice());
        fieldModels.setSelectedItem(part.getModel());
        fieldCategory.setSelectedItem(part.getCategory());
        fieldProviders.setSelectedItem(part.getProvider());
    }

    public void clearFields() {
        nameTextField.setText("");
        descriptionTextField.setText("");
        skuTextField.setText("");
        stockTextField.setText("");
        sellPriceTextField.clear();
        fieldModels.clearSelection();
        fieldCategory.clearSelection();
        fieldProviders.clearSelection();
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
            if (sellPriceTextField.getBigDecimal().compareTo(BigDecimal.ZERO) < 0) {
                errorHandler.accept("El precio no puede ser menor a 0");
                return false;
            }
        } catch (NumberFormatException e) {
            errorHandler.accept("Asegúrese de introducir números en Precio venta");
            return false;
        }
        if (fieldModels.getSelectedItem() == null){//) || fieldCategory.getSelectedIndex() == 0) {
            errorHandler.accept("Debe seleccionar Marca y Categoría válidos");
            return false;
        }
        if (fieldProviders.getSelectedItem() == null) {
            errorHandler.accept("Debe seleccionar un Proveedor");
            return false;
        }
        return true;
    }
}

