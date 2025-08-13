package views;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.RowFilter;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

import com.formdev.flatlaf.FlatClientProperties;

import Utils.Icons;
import controller.CustomerController;
import controller.ProviderController;
import interfaces.Refreshable;
import model.client.entities.Customer;
import model.client.entities.Provider;
import views.components.JPopupMenuModifyDelete;
import views.components.LightTheme;
import views.components.NewModifyButton;

public class ProviderForm extends JPanel implements Refreshable{
private static final long serialVersionUID = 1L;
	
	private final ProviderController controller;
	
	private JPanel inputPanel;
	private JPanel tablePanel;
	private static final Object[] COLUMNS = {"Nombre", "CUIT", "Direccion", "Telefono", "Mail", "id"};
	private JTable table;
	private DefaultTableModel tableModel;
	private JLabel messageLabel;
	private JButton toggleButton = new JButton("Mostrar formulario");
	
	private JTextField nameTextField = new JTextField("", 29);
	private JTextField cuitTextField = new JTextField(29);
	private JTextField adressTextField = new JTextField("", 29);
	private JTextField phoneTextField = new JTextField("", 29);
	private JTextField mailTextField = new JTextField("", 29);
	private NewModifyButton confirmButton = new NewModifyButton();
	
	private JButton searchButton = new JButton("Buscar", Icons.LENS.create(18,18));
	private JTextField cuitSearchTextField = new JTextField("", 15);
	private JTextField nameSearchTextField = new JTextField("",15); 
	private TableRowSorter<DefaultTableModel> sorter;
	private Timer filtroTimer;

		public ProviderForm(ProviderController controller) {
		    this.controller = controller;

		    setLayout(new BorderLayout(0, 0));
		    createInputPanel();	
		    createTablePanel();
		    createJPopupMenu();
		    createMessageLabel();
		    initUI();
			
			sorter = new TableRowSorter<>(tableModel);
	        table.setRowSorter(sorter);
	        setupLiveFilterListeners();
	        
	        //loadTable();
		}
		
		private void save() {			  
			Provider c = Provider.builder()
					.companyName(nameTextField.getText())
					.adress(adressTextField.getText())
					.cuit(cuitTextField.getText())
					.email(mailTextField.getText())
					.phone(phoneTextField.getText())
					.build();

			if(confirmButton.isInModifyMode()) {
				Long id = confirmButton.getIdToModify();
				c.setId(id);
			}
			
			if(controller.save(c)) {	
				clearFields();
				loadTable();
			}else
				setMessage("No se pudo Guardar");
		}
		
		private void delete() {			
			String name = (String)tableModel.getValueAt(table.convertRowIndexToModel(table.getSelectedRow()), table.getColumnModel().getColumnIndex((String)"Nombre"));
			try {
				if(JOptionPane.showConfirmDialog(null, "Desea eliminar al proveedor "+ name,  "", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION){
					Long id = (Long)tableModel.getValueAt(table.getSelectedRow(), table.getColumnModel().getColumnIndex((String)"id"));
					controller.delete(id);
					clearFields();
					loadTable();
				}
			}catch(Exception e) {
				setMessage(e.getCause().getMessage());
			}
		}
		
		private void loadTable() {		  
	        tableModel.setRowCount(0);
	        sorter.setSortKeys(null);//resets column order
	        
			List<Provider> providers = controller.getProviders();
			for(Provider c : providers) {
				Object[] row = {c.getCompanyName(), c.getCuit(), c.getAdress(), c.getPhone(), c.getEmail(), c.getId()};
				tableModel.addRow(row);
			}
		}

		private void initUI() {	   		 
		    searchButton.addActionListener(e -> loadTable());
		    
		    JPanel north = new JPanel(new BorderLayout());
		    JPanel titulo = LightTheme.createTitle("Proveedores");

			JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
			filterPanel.add(new JLabel("          ", JLabel.RIGHT));
			filterPanel.add(new JLabel("CUIT: ", JLabel.RIGHT));
			filterPanel.add(cuitSearchTextField);
			filterPanel.add(new JLabel("Nombre: ", JLabel.RIGHT));
			filterPanel.add(nameSearchTextField);
			nameSearchTextField.putClientProperty(FlatClientProperties.TEXT_FIELD_SHOW_CLEAR_BUTTON, true);
			cuitSearchTextField.putClientProperty(FlatClientProperties.TEXT_FIELD_SHOW_CLEAR_BUTTON, true);
			filterPanel.add(searchButton);
			filterPanel.add(toggleButton);
			LightTheme.aplicarEstiloPrimario(searchButton);
			LightTheme.aplicarEstiloSecundario(toggleButton);
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
			JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, tablePanel, inputPanel);
	        splitPane.setResizeWeight(1); // El panel de arriba no se estira al redimensionar

	        toggleButton.setPreferredSize(new Dimension(170, 26));
	        toggleButton.addActionListener(e -> {
	            if (inputPanel.isVisible()) {
	                inputPanel.setVisible(false);
	                toggleButton.setText("Mostrar formulario");
	                splitPane.setDividerSize(0);
	            } else {
	                inputPanel.setVisible(true);
	                toggleButton.setText("Ocultar formulario");
	                splitPane.setDividerSize(8);
	                SwingUtilities.invokeLater(() -> {
	                	splitPane.setDividerLocation(splitPane.getWidth() - inputPanel.getPreferredSize().width);
	                });
	            }
	            splitPane.revalidate();
	        });
	        add(splitPane, BorderLayout.CENTER);
//--------------------------------------------------------CENTER PANEL------------------------------------------------------------
		}

		private void createInputPanel() { 
			inputPanel = new JPanel();
			inputPanel.setLayout(new BoxLayout(inputPanel, BoxLayout.Y_AXIS));
			inputPanel.setVisible(false);
			Dimension size = new Dimension(450, HEIGHT);
			inputPanel.setMinimumSize(new Dimension(380, HEIGHT));
			inputPanel.setPreferredSize(size);

			JPanel rowsPanel = new JPanel(new GridLayout(0,1));

			rowsPanel.add(new JLabel("  Nombre", JLabel.LEFT));
			rowsPanel.add(nameTextField);
			rowsPanel.add(new JLabel("  CUIT / CUIL", JLabel.LEFT));
			rowsPanel.add(cuitTextField);
			rowsPanel.add(new JLabel("  Direccion", JLabel.LEFT));
			rowsPanel.add(adressTextField);			
			rowsPanel.add(new JLabel("  Mail", JLabel.LEFT));
			rowsPanel.add(mailTextField);		
			rowsPanel.add(new JLabel("  Telefono", JLabel.LEFT));
			rowsPanel.add(phoneTextField);
			
			inputPanel.add(rowsPanel);
			
			JPanel buttonsPanel = new JPanel(new BorderLayout());
			buttonsPanel.setMaximumSize(new Dimension(500, 160));
			JPanel firsts = new JPanel(new GridLayout());

			confirmButton.addActionListener(e ->{
				if(validateFields())
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
		}

		@SuppressWarnings("serial")
		private void createTablePanel() { 
			tableModel = new DefaultTableModel(COLUMNS, 0){
				@Override
	            public boolean isCellEditable(int row, int column) {
	                return false;
	            }
				@Override
	            public Class<?> getColumnClass(int column) {
	                switch (column) {
	                    case 3: return String.class;
	                    case 5: return Long.class;
	                    default:return Object.class;
	                }
	            }
	        };
			table = new JTable(tableModel);
			table.setToolTipText("Click Derecho para Eliminar o Modificar");
			table.setShowGrid(true);
			table.getColumnModel().getColumn(table.getColumnCount()-1).setMaxWidth(0);
			table.getColumnModel().getColumn(table.getColumnCount()-1).setMinWidth(0);
			table.getColumnModel().getColumn(table.getColumnCount()-1).setPreferredWidth(0);

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
	           	if(modelRow != -1){
	           	    int row = table.convertRowIndexToModel(modelRow);
					Long id = (Long) tableModel.getValueAt(row, table.getColumnModel().getColumnIndex((String)"id"));
					Provider c = controller.getProvider(id);
					
					nameTextField.setText(c.getCompanyName());
					cuitTextField.setText(c.getCuit());
					adressTextField.setText(c.getAdress());
					phoneTextField.setText(c.getPhone());
					mailTextField.setText(c.getEmail());
						
					if(!inputPanel.isVisible())
						toggleButton.doClick();
					
					confirmButton.toModify(id);
	            }
           		
		}
		
		private boolean validateFields() {
			if(nameTextField.getText().isBlank()) {
				setMessage("El nombre no puede estar vacio");
				return false;
			}
			if(cuitTextField.getText().isBlank()) {
				setMessage("El CUIT no puede estar vacio");
				return false;
			}
			return true;	
		}

		private void clearFields() {	
			table.clearSelection();
			nameTextField.setText("");
			cuitTextField.setText("");
			adressTextField.setText("");
			phoneTextField.setText("");
			mailTextField.setText("");
			confirmButton.toNew();
			
			messageLabel.setText("");
	        messageLabel.setOpaque(false);
		}
		
		private void applyCombinedFilters() {		
		    String cuitText = cuitSearchTextField.getText().trim();
		    String nameText = nameSearchTextField.getText();
		    List<RowFilter<Object, Object>> filters = new ArrayList<>();

		    if (!cuitText.isEmpty()) 
		        filters.add(RowFilter.regexFilter("(?i)^" + Pattern.quote(cuitText), 1)); // Columna cuit

		    if (!nameText.isEmpty()) 
		        filters.add(RowFilter.regexFilter("(?i)" + Pattern.quote(nameText), 0)); // Columna Nombre
		    
		    if (filters.isEmpty()) 
		        sorter.setRowFilter(null); // Sin filtro
		    else 
		        sorter.setRowFilter(RowFilter.andFilter(filters));
		}
		
		private void setupLiveFilterListeners() {
		    DocumentListener debounceListener = new DocumentListener() {
		        @Override public void insertUpdate(DocumentEvent e) { reiniciarTimer(); }
		        @Override public void removeUpdate(DocumentEvent e) { reiniciarTimer(); }
		        @Override public void changedUpdate(DocumentEvent e) { reiniciarTimer(); }

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
		    cuitSearchTextField.getDocument().addDocumentListener(debounceListener);
		    nameSearchTextField.getDocument().addDocumentListener(debounceListener);
		}

		private void createJPopupMenu() {
			new JPopupMenuModifyDelete(table, this::setRowToEdit, this::delete );
		}

		@Override
		public void refresh() {
			clearFields();
			loadTable();
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
