package views.carpart;

import java.awt.BorderLayout;
import java.awt.FlowLayout;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import controller.AppContext;
import model.client.entities.CarPart;
import model.client.entities.ProviderPart;
import views.components.LightTheme;

public class CarPartDialog extends JDialog {

    private CarPartInputPanel inputPanel = new CarPartInputPanel();
    private Long id;
    private JButton saveButton = new JButton("Guardar");
    private JButton cancelButton = new JButton("Cancelar");

    private CarPart createdPart;
	private JPanel title; 

    public CarPartDialog(CarPart nuevo) {
		this();
		inputPanel.loadFrom(nuevo);
		LightTheme.replaceTitle(title, "Completar Descripcion");
		id = nuevo.getId();
	}
    
    public CarPartDialog() {
        super(null, "Autoparte", ModalityType.APPLICATION_MODAL);
        id = null;
        
        setLayout(new BorderLayout(5,5));
        inputPanel.setBorder(BorderFactory.createEmptyBorder(7, 7, 0, 7));
        add(inputPanel, BorderLayout.CENTER);

        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonsPanel.add(saveButton);
        buttonsPanel.add(cancelButton);
        add(buttonsPanel, BorderLayout.SOUTH);
        title = LightTheme.createTitle("Nueva AutoParte");
        add(title, BorderLayout.NORTH);
        
        inputPanel.fillCombos(
            AppContext.brandProvider.getBrands(),
            AppContext.categoryController.getCategories(),
            AppContext.providerController.getProviders()
        );

        saveButton.addActionListener(e -> {
            if (inputPanel.validateFields(msg -> JOptionPane.showMessageDialog(this, msg, "Error", JOptionPane.ERROR_MESSAGE))) 
                try {
                	CarPart part = inputPanel.toCarPart();
                	if(id != null)
                		part.setId(id);
                    createdPart = AppContext.carPartController.save(part); 
                    dispose(); 
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Error al guardar: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
        });

        cancelButton.addActionListener(e -> dispose());

        setSize(750, 600);
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

    public void setDescriptionFocus() {
		inputPanel.setDescriptionFocus();
	}
}

