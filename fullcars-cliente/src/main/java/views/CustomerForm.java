package views;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.regex.Pattern;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.RowFilter;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.Timer;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

import com.formdev.flatlaf.FlatClientProperties;

import Utils.Icons;
import Utils.NumberFormatArg;
import controller.CustomerController;
import dtos.CustomerDebtStatusResponse;
import dtos.CustomerDebtStatusResponse.CustomerDebtInfo;
import enums.DebtStatus;
import interfaces.Refreshable;
import model.client.entities.Customer;
import views.components.DotIcon;
import views.components.JPopupMenuModifyDelete;
import views.components.LightTheme;
import views.components.NewModifyButton;

public class CustomerForm extends JPanel implements Refreshable{
	
private static final long serialVersionUID = 1L;
	
	private final CustomerController controller;
	
	private JPanel inputPanel;
	private JPanel tablePanel;
	private static final Object[] COLUMNS = {"Deuda", "Nombre/Razon Social", "D.N.I", "CUIT / CUIL", "Compañía", "Email", "Telefono", "Direccion", "id"};
	private static final int COL_DEUDA = 0;
	private static final int COL_NOMBRE = 1;
	private static final int COL_DNI = 2;
	private static final int COL_COMPANIA = 4;
	private static final int COL_ID = 8;
	/** Avisos de deuda, del mas grave al sin deuda. */
	private static final DebtStatus[] ESTADOS_AVISO = {DebtStatus.DEUDA_40_DIAS, DebtStatus.DEUDA_30_DIAS, DebtStatus.DEUDA_RECIENTE, DebtStatus.SIN_DEUDA};
	private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
	private final Map<DebtStatus, JToggleButton> avisosDeuda = new EnumMap<>(DebtStatus.class);
	private final JLabel avisoDeudaError = new JLabel("No se pudo consultar la deuda de los clientes");
	/** Estado de deuda por el que se filtra la tabla (click en un aviso); null = todos. */
	private DebtStatus filtroDeuda;
	/** Clientes con un cambio de "Compañía" en curso (su checkbox queda bloqueado hasta que responda el servidor). */
	private final Set<Long> guardandoEsCompania = new HashSet<>();
	private boolean revirtiendo = false;
	private JTable table;
	private DefaultTableModel tableModel;
	private JLabel messageLabel;
	private JButton toggleButton = new JButton("Mostrar formulario", Icons.EYE.create());
	
	private JTextField nameTextField = new JTextField("", 29);
	private JTextField dniTextField = new JTextField(29);
	private JTextField cuitTextField = new JTextField("", 29);
	private JTextField adressTextField = new JTextField("", 29);
	private JTextField phoneTextField = new JTextField("", 29);
	private JTextField mailTextField = new JTextField("", 29);
	private JCheckBox esCompaniaCheck = new JCheckBox("Es Compañía");
	private NewModifyButton confirmButton = new NewModifyButton();
	
	private JButton searchButton = new JButton("Buscar", Icons.LENS.create(18,18));
	private JTextField dniSearchTextField = new JTextField("", 15);
	private JTextField nameSearchTextField = new JTextField("",15);  // Agregado
	private TableRowSorter<DefaultTableModel> sorter;
	private Timer filtroTimer;

		public CustomerForm(CustomerController controller) {
		    this.controller = controller;

		    setLayout(new BorderLayout(0, 0));
		    createInputPanel();	
		    createTablePanel();
		    createJPopupMenu();
		    createMessageLabel();
		    initUI();
			
			sorter = new TableRowSorter<>(tableModel);
			// ordena por los dias de la venta pendiente mas vieja (los sin deuda primero)
			sorter.setComparator(COL_DEUDA, Comparator.comparing(
					(CustomerDebtInfo d) -> d.getOldestPendingDays(), Comparator.nullsFirst(Comparator.naturalOrder())));
	        table.setRowSorter(sorter);
	        setupLiveFilterListeners();
	        
	        //loadTable();
		}
		
		private void save() {			  
			Customer c = Customer.builder()
					.fullName(nameTextField.getText())
					.dni(dniTextField.getText())
					.adress(adressTextField.getText())
					.cuit(cuitTextField.getText())
					.email(mailTextField.getText())
					.phone(phoneTextField.getText())
					.esCompania(esCompaniaCheck.isSelected())
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
		
		/** Guarda en el servidor el checkbox "Compañía" de la tabla; si falla vuelve al valor anterior y avisa. */
		private void guardarEsCompania(Long id, boolean value) {
			guardandoEsCompania.add(id);
			new SwingWorker<Void, Void>() {
				@Override
				protected Void doInBackground() throws Exception {
					controller.setEsCompania(id, value);
					return null;
				}
				@Override
				protected void done() {
					guardandoEsCompania.remove(id);
					try {
						get();
						// si el formulario esta modificando a este cliente, que no vuelva a guardar el valor viejo
						if (confirmButton.isInModifyMode() && id.equals(confirmButton.getIdToModify()))
							esCompaniaCheck.setSelected(value);
					} catch (Exception e) {
						Throwable cause = e instanceof ExecutionException && e.getCause() != null ? e.getCause() : e;
						int row = modelRowOf(id);
						if (row != -1) {
							revirtiendo = true;
							tableModel.setValueAt(!value, row, COL_COMPANIA);
							revirtiendo = false;
						}
						setMessage("No se pudo actualizar 'Compañía': " + cause.getMessage());
					}
				}
			}.execute();
		}

		private int modelRowOf(Long id) {
			for (int r = 0; r < tableModel.getRowCount(); r++)
				if (id.equals(tableModel.getValueAt(r, COL_ID))) return r;
			return -1;
		}

		private void delete() {
			String name = (String)tableModel.getValueAt(table.convertRowIndexToModel(table.getSelectedRow()), table.getColumnModel().getColumnIndex((String)"Nombre completo"));
			try {
				if(JOptionPane.showConfirmDialog(null, "Desea eliminar al cliente "+ name,  "", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION){
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
	        
			List<Customer> customers = controller.getCustomers();
			CustomerDebtStatusResponse deudas = controller.getDebtStatus();
			Map<Long, CustomerDebtInfo> deudaPorCliente = new HashMap<>();
			if (deudas != null)
				for (CustomerDebtInfo d : deudas.getCustomers())
					deudaPorCliente.put(d.getCustomerId(), d);

			for(Customer c : customers) {
				Object[] row = {deudaPorCliente.get(c.getId()), c.getFullName(), c.getDni(), c.getCuit(), c.isEsCompania(), c.getEmail(), c.getPhone(), c.getAdress(), c.getId()};
				tableModel.addRow(row);
			}
			actualizarAvisosDeuda(deudas);
		}

		/** Cuantos clientes hay en cada estado de deuda; si no se pudo consultar, lo avisa. */
		private void actualizarAvisosDeuda(CustomerDebtStatusResponse deudas) {
			avisoDeudaError.setVisible(deudas == null);
			for (DebtStatus estado : ESTADOS_AVISO) {
				JToggleButton aviso = avisosDeuda.get(estado);
				aviso.setVisible(deudas != null);
				if (deudas != null)
					aviso.setText(textoAviso(estado, deudas.getCounts().getOrDefault(estado, 0L)));
			}
			if (deudas == null && filtroDeuda != null)
				filtrarPorDeuda(filtroDeuda); // sin datos de deuda no se puede filtrar: muestra todos
		}

		private static String textoAviso(DebtStatus estado, long cantidad) {
			String clientes = "<html><b>" + cantidad + "</b> " + (cantidad == 1 ? "cliente" : "clientes");
			switch (estado) {
				case DEUDA_40_DIAS: return clientes + " con deuda de " + DebtStatus.DIAS_ROJO + " días o más</html>";
				case DEUDA_30_DIAS: return clientes + " con deuda de " + DebtStatus.DIAS_AMARILLO + " a " + (DebtStatus.DIAS_ROJO - 1) + " días</html>";
				case DEUDA_RECIENTE: return clientes + " con deuda de menos de " + DebtStatus.DIAS_AMARILLO + " días</html>";
				default: return clientes + " sin deuda</html>";
			}
		}

		/** Click en un aviso: muestra solo los clientes de ese estado; otro click en el mismo vuelve a mostrar todos. */
		private void filtrarPorDeuda(DebtStatus estado) {
			filtroDeuda = estado == filtroDeuda ? null : estado;
			avisosDeuda.forEach((e, aviso) -> {
				boolean activo = e == filtroDeuda;
				aviso.setSelected(activo);
				aviso.setBackground(activo ? e.color() : e.fondo());
				aviso.setForeground(activo ? Color.WHITE : Color.DARK_GRAY);
				aviso.setIcon(activo ? new DotIcon(Color.WHITE, 12) : new DotIcon(e.color(), 12));
			});
			applyCombinedFilters();
		}

		private JPanel createAvisoDeudaPanel() {
			JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 2));
			for (DebtStatus estado : ESTADOS_AVISO) {
				JToggleButton aviso = new JToggleButton("", new DotIcon(estado.color(), 12));
				aviso.setBackground(estado.fondo());
				aviso.setForeground(Color.DARK_GRAY);
				aviso.setFocusPainted(false);
				aviso.setCursor(new Cursor(Cursor.HAND_CURSOR));
				aviso.setToolTipText("Mostrar solo estos clientes (otro click: todos)");
				aviso.addActionListener(e -> filtrarPorDeuda(estado));
				avisosDeuda.put(estado, aviso);
				panel.add(aviso);
			}
			avisoDeudaError.setForeground(Color.GRAY);
			avisoDeudaError.setVisible(false);
			panel.add(avisoDeudaError);
			panel.setBorder(createSectionBorder("Deuda de clientes (venta pendiente más vieja)"));
			return panel;
		}

		private static String tooltipDeuda(CustomerDebtInfo d) {
			StringBuilder sb = new StringBuilder("<html>");
			if (d.getStatus() == DebtStatus.SIN_DEUDA)
				sb.append("Sin ventas pendientes");
			else
				sb.append("Debe ").append(d.getPendingSales()).append(d.getPendingSales() == 1 ? " venta" : " ventas")
				  .append(" por ").append(NumberFormatArg.format(d.getTotalPending()))
				  .append("<br>La más vieja es del ").append(d.getOldestPendingDate().format(FECHA))
				  .append(" (hace ").append(textoDias(d.getOldestPendingDays())).append(")");
			if (d.getCreditBalance() != null && d.getCreditBalance().signum() > 0)
				sb.append("<br>Saldo a favor: ").append(NumberFormatArg.format(d.getCreditBalance()));
			return sb.append("</html>").toString();
		}

		private static String textoDias(long dias) {
			return dias == 1 ? "1 día" : dias + " días";
		}

		private static Border createSectionBorder(String titulo) {
			return BorderFactory.createCompoundBorder(
				    BorderFactory.createEmptyBorder(0, 10, 10, 10),
				    BorderFactory.createTitledBorder(
				        BorderFactory.createEtchedBorder(EtchedBorder.LOWERED),
				        titulo,
				        TitledBorder.LEFT,
				        TitledBorder.TOP,
				        new Font("Montserrat-Medium", Font.PLAIN, 14), // TRUETYPE_FONT no es correcto aquí
				        Color.BLACK
				    )
			);
		}

		private void initUI() {	   		 
		    searchButton.addActionListener(e -> loadTable());
		    
		    JPanel north = new JPanel(new BorderLayout());
		    JPanel titulo = LightTheme.createTitle("Clientes");

			JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
			filterPanel.add(new JLabel("          ", JLabel.RIGHT));
			filterPanel.add(new JLabel("Dni: ", JLabel.RIGHT));
			filterPanel.add(dniSearchTextField);
			filterPanel.add(new JLabel("Nombre/R.S : ", JLabel.RIGHT));
			filterPanel.add(nameSearchTextField);
			nameSearchTextField.putClientProperty(FlatClientProperties.TEXT_FIELD_SHOW_CLEAR_BUTTON, true);
			dniSearchTextField.putClientProperty(FlatClientProperties.TEXT_FIELD_SHOW_CLEAR_BUTTON, true);
			filterPanel.add(searchButton);
			filterPanel.add(toggleButton);
			LightTheme.aplicarEstiloPrimario(searchButton);
			LightTheme.aplicarEstiloSecundario(toggleButton);
			filterPanel.setBorder(createSectionBorder("Filtros"));
			north.add(titulo, BorderLayout.NORTH);
			north.add(filterPanel, BorderLayout.CENTER);
			north.add(createAvisoDeudaPanel(), BorderLayout.SOUTH);
			
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

			rowsPanel.add(new JLabel("  Nombre/Razon Social", JLabel.LEFT));
			rowsPanel.add(nameTextField);
			rowsPanel.add(new JLabel("  Documento (DNI)", JLabel.LEFT));
			rowsPanel.add(dniTextField);
			rowsPanel.add(new JLabel("  CUIT / CUIL", JLabel.LEFT));
			rowsPanel.add(cuitTextField);
			rowsPanel.add(new JLabel("  Direccion", JLabel.LEFT));
			rowsPanel.add(adressTextField);
			
			JPanel columnPanel = new JPanel(new GridLayout(1,0));
			columnPanel.add(new JLabel("  Mail", JLabel.LEFT));
			columnPanel.add(new JLabel("  Telefono", JLabel.LEFT));
			rowsPanel.add(columnPanel);
			
			columnPanel = new JPanel(new GridLayout(1,0));
			columnPanel.add(mailTextField);
			columnPanel.add(phoneTextField);
			rowsPanel.add(columnPanel);

			esCompaniaCheck.setToolTipText("Compañía de seguros: en su resumen de cuenta se muestra el número de siniestro");
			rowsPanel.add(esCompaniaCheck);

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

			JButton cancel = new JButton("Cancelar", Icons.CLEAN.create());
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
	                // solo "Compañía", y no mientras se esta guardando un cambio anterior de ese cliente
	                return column == COL_COMPANIA && !guardandoEsCompania.contains(getValueAt(row, COL_ID));
	            }
				@Override
	            public Class<?> getColumnClass(int column) {
	                switch (column) {
	                    case COL_DEUDA: return CustomerDebtInfo.class;
	                    case COL_COMPANIA: return Boolean.class; // se dibuja como checkbox
	                    case 5: return String.class;
	                    case COL_ID: return Long.class;
	                    default:return Object.class;
	                }
	            }
				@Override
				public void setValueAt(Object value, int row, int column) {
					Object anterior = getValueAt(row, column);
					super.setValueAt(value, row, column);
					if (column == COL_COMPANIA && !revirtiendo && !value.equals(anterior))
						guardarEsCompania((Long) getValueAt(row, COL_ID), (Boolean) value);
				}
	        };
			table = new JTable(tableModel);
			table.setToolTipText("Click Derecho para Eliminar o Modificar, selecciona un cliente(click izquierdo) para ver su saldo");
			table.setShowGrid(true);
			table.getColumnModel().getColumn(table.getColumnCount()-1).setMaxWidth(0);
			table.getColumnModel().getColumn(table.getColumnCount()-1).setMinWidth(0);
			table.getColumnModel().getColumn(table.getColumnCount()-1).setPreferredWidth(0);
			table.getColumnModel().getColumn(COL_COMPANIA).setMaxWidth(90);
			table.getColumnModel().getColumn(COL_COMPANIA).setPreferredWidth(80);
			table.getColumnModel().getColumn(COL_DEUDA).setMaxWidth(110);
			table.getColumnModel().getColumn(COL_DEUDA).setPreferredWidth(90);
			table.getColumnModel().getColumn(COL_DEUDA).setCellRenderer(new DefaultTableCellRenderer() {
				@Override
				public Component getTableCellRendererComponent(JTable t, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
					super.getTableCellRendererComponent(t, "", isSelected, hasFocus, row, column);
					CustomerDebtInfo deuda = value instanceof CustomerDebtInfo d ? d : null;
					setIcon(deuda == null ? null : new DotIcon(deuda.getStatus().color(), 12));
					setText(deuda == null || deuda.getStatus() == DebtStatus.SIN_DEUDA ? "" : textoDias(deuda.getOldestPendingDays()));
					setToolTipText(deuda == null ? null : tooltipDeuda(deuda));
					return this;
				}
			});
			table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
				@Override
				public void valueChanged(ListSelectionEvent e) {
					int row = table.getSelectedRow();
					if(row != -1)
						controller.setCustomerSelectedId((Long) tableModel.getValueAt(table.convertRowIndexToModel(row),
															table.getColumnModel().getColumnIndex((String)"id")));
				}});
			
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
					Customer c = controller.getCustomer(id);
					
					nameTextField.setText(c.getFullName());
					cuitTextField.setText(c.getCuit());
					dniTextField.setText(c.getDni());
					adressTextField.setText(c.getAdress());
					phoneTextField.setText(c.getPhone());
					mailTextField.setText(c.getEmail());
					esCompaniaCheck.setSelected(c.isEsCompania());

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
			if(dniTextField.getText().isBlank()) {
				setMessage("El DNI no puede estar vacio");
				return false;
			}
			return true;	
		}

		private void clearFields() {	
			table.clearSelection();
			controller.setCustomerSelectedId(null);
			nameTextField.setText("");
			cuitTextField.setText("");
			dniTextField.setText("");
			adressTextField.setText("");
			phoneTextField.setText("");
			mailTextField.setText("");
			esCompaniaCheck.setSelected(false);
			confirmButton.toNew();
			
			messageLabel.setText("");
	        messageLabel.setOpaque(false);
		}
		
		private void applyCombinedFilters() {		
		    String dniText = dniSearchTextField.getText().trim();
		    String nameText = nameSearchTextField.getText();
		    List<RowFilter<Object, Object>> filters = new ArrayList<>();

		    if (!dniText.isEmpty())
		        filters.add(RowFilter.regexFilter("(?i)^" + Pattern.quote(dniText), COL_DNI));

		    if (!nameText.isEmpty())
		        filters.add(RowFilter.regexFilter("(?i)" + Pattern.quote(nameText), COL_NOMBRE));

		    if (filtroDeuda != null)
		        filters.add(new RowFilter<Object, Object>() {
		            @Override
		            public boolean include(Entry<?, ?> entry) {
		                return entry.getValue(COL_DEUDA) instanceof CustomerDebtInfo d && d.getStatus() == filtroDeuda;
		            }
		        });

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
		    dniSearchTextField.getDocument().addDocumentListener(debounceListener);
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
