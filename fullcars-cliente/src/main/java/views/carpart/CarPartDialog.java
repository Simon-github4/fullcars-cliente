package views.carpart;

import java.awt.BorderLayout;
import java.awt.Dialog.ModalityType;
import java.awt.FlowLayout;
import java.awt.Window;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import controller.AppContext;
import controller.CarPartController;
import controller.CategoryController;
import controller.ProviderController;
import interfaces.IBrandProvider;
import model.client.entities.CarPart;
import model.client.entities.ProviderPart;
import views.components.LightTheme;

public class CarPartDialog extends JDialog {

    private CarPartInputPanel inputPanel = new CarPartInputPanel();
    private JButton saveButton = new JButton("Guardar");
    private JButton cancelButton = new JButton("Cancelar");

    private CarPart createdPart; 

    public CarPartDialog(ProviderPart providerPart) {
    	this();
    	inputPanel.loadFrom(CarPart.builder()
    			.name(providerPart.getNombre())
    			.basePrice(providerPart.getPrecio())
    			.provider(AppContext.providerController.getProvider(providerPart.getProviderId()))
    			//.model(providerPart)
    			.build());
	}
    
    public CarPartDialog() {
        super(null, "Nueva Autoparte", ModalityType.APPLICATION_MODAL);
        
        setLayout(new BorderLayout(5,5));
        inputPanel.setBorder(BorderFactory.createEmptyBorder(7, 7, 0, 7));
        add(inputPanel, BorderLayout.CENTER);

        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonsPanel.add(saveButton);
        buttonsPanel.add(cancelButton);
        add(buttonsPanel, BorderLayout.SOUTH);
        add(LightTheme.createTitle("Nueva AutoParte"), BorderLayout.NORTH);
        
        inputPanel.fillCombos(
       		AppContext.modelController.getModels(),
            AppContext.brandProvider.getBrands(),
            AppContext.categoryController.getCategories(),
            AppContext.providerController.getProviders()
        );

        saveButton.addActionListener(e -> {
            if (inputPanel.validateFields(msg -> JOptionPane.showMessageDialog(this, msg, "Error", JOptionPane.ERROR_MESSAGE))) 
                try {
                	CarPart part = inputPanel.toCarPart();
                    createdPart = AppContext.carPartController.save(part); 
                    dispose(); 
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Error al guardar: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
        });

        cancelButton.addActionListener(e -> dispose());

        setSize(500, 500);
        setLocationRelativeTo(null);
    }

	//Devuelve la CarPart creada, o null si el usuario canceló.
    public CarPart getCreatedPart() {
        return createdPart;
    }
    
    @Override
    public void dispose() {
    	super.dispose();
    	this.removeAll();
    	this.inputPanel = null;
    	this.cancelButton = null;
    	this.saveButton = null;
    }
}

