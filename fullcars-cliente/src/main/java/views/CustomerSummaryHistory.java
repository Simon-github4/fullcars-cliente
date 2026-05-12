package views;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.RowFilter;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
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
import Utils.NumberFormatArg;
import Utils.ServerException;
import controller.CustomerController;
import controller.PayController;
import controller.SaleController;
import data.service.ClienteRestPayments;
import dtos.AllocationInfo;
import dtos.MultiPaymentResponse;
import dtos.PendingSalesResponse;
import dtos.PendingSalesResponse.SalePendingInfo;
import interfaces.Refreshable;
import model.client.entities.Customer;
import raven.datetime.DatePicker;
import raven.datetime.event.DateSelectionEvent;
import raven.datetime.event.DateSelectionListener;
import views.components.DatePickerS;
import views.components.JPopupMenuModifyDelete;
import views.components.LightTheme;
import views.transactions.MultiPaymentDialog;

public class CustomerSummaryHistory extends JPanel implements Refreshable {
    private static final long serialVersionUID = 1L;

    private final CustomerController controller;
    private final PayController payController;
    private final SaleController saleController;
    private final ClienteRestPayments restPayments;
    
    private PendingSalesResponse pendingData;
    private List<MultiPaymentResponse> paymentsList = new ArrayList<>();
    private Map<Long, BigDecimal> salePaidMap = new HashMap<>();
    
    private JLabel customerInfoLabel = new JLabel("", JLabel.CENTER);
    private JLabel creditBalanceLabel = new JLabel(" - Credito a Usar: $0.00", JLabel.CENTER);
    
    private JPanel paysTablePanel;
    private static final Object[] PAYS_COLUMNS = {"Fecha", "Monto", "Credito Gen.", "Descripcion", "Ver Detalle", "id" };
    private JTable paysTable;
    private DefaultTableModel paysTableModel;
    
    private JPanel saleTablePanel;
    private static final Object[] SALE_COLUMNS = {"Fecha", "Nro siniestro", "Pendiente", "CAE", "Nro. Venta"};
    private JTable saleTable;
    private DefaultTableModel saleTableModel;

    private JTextField idSearchTextField = new JTextField("", 10);
    private JTextField observationSearchTextField = new JTextField("", 13);
    private JFormattedTextField dateSearchTextField = new JFormattedTextField();
    private DatePicker dpFilter = new DatePickerS();
    private JButton refreshButton = new JButton("Actualizar Datos", Icons.REFRESH.create(18, 18));
    private JButton multiPayButton = new JButton("Pago Multiple", Icons.NEW.create(18, 18));
    //private JButton showAllButton = new JButton("Mostrar Todos", Icons.EYE.create());

    private TableRowSorter<DefaultTableModel> sorter;
    private TableRowSorter<DefaultTableModel> paysSorter;
    private Timer filtroTimer;
    private JLabel messageLabel;

    private JLabel totalSalesLabel = new JLabel("Total Ventas: 0", JLabel.CENTER);
    private JLabel totalPaysLabel = new JLabel("Total Pagos: 0", JLabel.CENTER);
    private JLabel totalBalanceLabel = new JLabel(" - Saldo Total: 0", JLabel.CENTER);

    public CustomerSummaryHistory(CustomerController controller, PayController payController, SaleController saleController) {
        this.controller = controller;
        this.payController = payController;
        this.saleController = saleController;
        this.restPayments = new ClienteRestPayments();
        
        setLayout(new BorderLayout(0, 0));
        
        createPaysTablePanel();
        createSaleTablePanel();
        createJPopupMenu();
        createMessageLabel();
        initUI();

        sorter = new TableRowSorter<>(saleTableModel);
        saleTable.setRowSorter(sorter);
        
        paysSorter = new TableRowSorter<>(paysTableModel);
        paysTable.setRowSorter(paysSorter);
        
        List<javax.swing.RowSorter.SortKey> paySortKeys = new ArrayList<>();
        paySortKeys.add(new javax.swing.RowSorter.SortKey(0, javax.swing.SortOrder.DESCENDING));
        paysSorter.setSortKeys(paySortKeys);
        
        setupLiveFilterListeners();
    }

    private void deletePayment() {            
        try {
            Long idPay = (Long) paysTableModel.getValueAt(paysTable.convertRowIndexToModel(paysTable.getSelectedRow()), 5);
            
            if(JOptionPane.showConfirmDialog(null, "Desea eliminar el Pago?",  "", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION){
                payController.delete(idPay);
                refresh();
            }
        } catch(ServerException se) {
            setMessage(se.getMessage());
        } catch (IOException ioe) {
            setMessage(ioe.getMessage());
        }
    }
    
    private void showPaymentDetails(MultiPaymentResponse payment) {
        JDialog dialog = new JDialog((java.awt.Frame) null, "Detalle Pago #" + payment.getPaymentId(), true);
        dialog.setSize(650, 500);
        dialog.setLocationRelativeTo(null);
        
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        JPanel infoPanel = new JPanel(new GridLayout(0, 2, 5, 5));
        infoPanel.add(new JLabel("Pago #:")); infoPanel.add(new JLabel(String.valueOf(payment.getPaymentId())));
        infoPanel.add(new JLabel("Fecha:")); infoPanel.add(new JLabel(payment.getDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))));
        infoPanel.add(new JLabel("Monto Total:")); infoPanel.add(new JLabel(NumberFormatArg.format(payment.getTotalAmount())));
        
        BigDecimal creditUsed = payment.getCreditUsed() != null ? payment.getCreditUsed() : BigDecimal.ZERO;
        BigDecimal creditGen = payment.getCreditGenerated() != null ? payment.getCreditGenerated() : BigDecimal.ZERO;
        
        infoPanel.add(new JLabel("Credito Usado:")); infoPanel.add(new JLabel(NumberFormatArg.format(creditUsed)));
        infoPanel.add(new JLabel("Credito Generado:")); infoPanel.add(new JLabel(NumberFormatArg.format(creditGen)));
        
        panel.add(infoPanel, BorderLayout.NORTH);
        
        JPanel splitsPanel = new JPanel(new BorderLayout());
        splitsPanel.add(new JLabel("Metodos de Pago:"), BorderLayout.NORTH);
        
        Object[] splitColumns = {"Metodo", "Monto", "Referencia", "Ventas Cubiertas"};
        DefaultTableModel splitModel = new DefaultTableModel(splitColumns, 0);
        JTable splitTable = new JTable(splitModel);
        
        BigDecimal totalSplits = BigDecimal.ZERO;
        if (payment.getSplits() != null) {
            for (MultiPaymentResponse.PaymentSplitResponse split : payment.getSplits()) {
                System.out.println(split.getSalesCovered());
            	String ventasCubiertas = split.getSalesCovered() != null 
                        ? String.join(", ", split.getSalesCovered()) : "-";
                splitModel.addRow(new Object[]{
                    split.getPaymentMethod(),
                    NumberFormatArg.format(split.getAmount()),
                    split.getReference() != null ? split.getReference() : "-",
                    ventasCubiertas
                });
                totalSplits = totalSplits.add(split.getAmount());
            }
        }
        
        JPanel totalPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        totalPanel.add(new JLabel("Total: " + NumberFormatArg.format(totalSplits)));
        
        splitsPanel.add(new JScrollPane(splitTable), BorderLayout.CENTER);
        splitsPanel.add(totalPanel, BorderLayout.SOUTH);
        splitsPanel.setPreferredSize(new Dimension(450, 150));
        
        panel.add(splitsPanel, BorderLayout.CENTER);
        
        JButton closeButton = new JButton("Cerrar");
        closeButton.addActionListener(e -> dialog.dispose());
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(closeButton);
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        dialog.add(panel);
        dialog.setVisible(true);
    }
    
    private void openMultiPaymentDialog() {
        Customer customer = controller.getCustomer(controller.getCustomerSelectedId());
        if (customer == null) {
            JOptionPane.showMessageDialog(null, "Debe seleccionar un Cliente");
            return;
        }
        
        MultiPaymentDialog dialog = new MultiPaymentDialog(null, customer);
        dialog.setVisible(true);
        refresh();
    }
    
    private void refreshFromDB() {
        if(controller.getCustomerSelectedId() != null) {
            loadDataAsync();
        } else
            JOptionPane.showMessageDialog(this, "Seleccione un Cliente de la pestana 'Clientes' para ver su balance");    
    }
    
    private void loadDataAsync() {
        Long customerId = controller.getCustomerSelectedId();
        
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                pendingData = restPayments.getPendingSales(customerId);
                paymentsList = restPayments.getPaymentsByCustomer(customerId);
                
                salePaidMap.clear();
                for (SalePendingInfo sale : pendingData.getSales()) {
                    salePaidMap.put(sale.getSaleId(), sale.getTotalPaid());
                }
                return null;
            }
            
            @Override
            protected void done() {
                try {
                    loadData();
                    
                    Customer customer = controller.getCustomer(customerId);
                    customerInfoLabel.setText(customer.getFullName() + " - Cuit: " + customer.getCuit());
                    
                    BigDecimal creditBalance = pendingData.getCreditBalance();
                    if (creditBalance == null) creditBalance = BigDecimal.ZERO;
                    creditBalanceLabel.setText(" - Credito a Usar: " + NumberFormatArg.format(creditBalance));
                    creditBalanceLabel.setForeground(LightTheme.COLOR_GREEN_MONEY);
                    
                    BigDecimal totalBalance = pendingData.getTotalPending().subtract(creditBalance).negate();// Saldo NEGATIVO si debe
                    
                    totalBalanceLabel.setText(" - Saldo Total: " + NumberFormatArg.format(totalBalance));
                    if(totalBalance.compareTo(BigDecimal.ZERO) < 0) 
                        totalBalanceLabel.setForeground(new Color(255, 0, 0));
                    else 
                        totalBalanceLabel.setForeground(LightTheme.COLOR_GREEN_MONEY);
                        
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(CustomerSummaryHistory.this, 
                            "Error al cargar datos: " + e.getMessage(),
                            "Error", JOptionPane.ERROR_MESSAGE);
                    e.printStackTrace();
                }
            }
        }.execute();
    }
    
    private void loadData() {
        saleTableModel.setRowCount(0);
        sorter.setSortKeys(null);
        
        for (dtos.PendingSalesResponse.SalePendingInfo sale : pendingData.getSales()) {
            if(dpFilter.getSelectedDateRange() == null || 
               sale.getDate().compareTo(dpFilter.getSelectedDateRange()[0]) >= 0 && 
               sale.getDate().compareTo(dpFilter.getSelectedDateRange()[1]) <= 0) {
                
                BigDecimal paid = salePaidMap.getOrDefault(sale.getSaleId(), BigDecimal.ZERO);
                BigDecimal saldo = paid.subtract(sale.getTotal()).setScale(2, RoundingMode.HALF_UP);
                
                Object[] row = { 
                    sale.getDate(),
                    sale.getSaleNumber()!= null ? sale.getSaleNumber() : "Particular",
                    NumberFormatArg.format(sale.getRemainingDue()), 
                    sale.getCae() != null? sale.getCae(): "No hay cae, revisar si tiene factura",
                    sale.getSaleId() 
                };
                saleTableModel.addRow(row);
            }
        }
        loadPays();
    }
    
    private void loadPays() {
        paysTableModel.setRowCount(0);
        
        for (MultiPaymentResponse pay : paymentsList) {
            LocalDate[] dateRange = dpFilter.getSelectedDateRange();
            boolean withinDate = dateRange == null || 
                (pay.getDate().compareTo(dateRange[0]) >= 0 && pay.getDate().compareTo(dateRange[1]) <= 0);
            
            if (withinDate) {
                BigDecimal creditGen = pay.getCreditGenerated() != null ? pay.getCreditGenerated() : BigDecimal.ZERO;
                
                Object[] row = {
                    pay.getDate(),
                    NumberFormatArg.format(pay.getTotalAmount()),
                    NumberFormatArg.format(creditGen),
                    pay.getDescription() != null && !pay.getDescription().isEmpty() ? pay.getDescription() : "-",
                    "Ver >",
                    pay.getPaymentId()
                };
                paysTableModel.addRow(row);
            }
        }
    }
    
    private void initUI() {
        refreshButton.addActionListener(e -> refreshFromDB());
        //showAllButton.addActionListener(e -> saleTable.clearSelection());
        multiPayButton.addActionListener(e -> openMultiPaymentDialog());
        dpFilter.setDateSelectionMode(DatePicker.DateSelectionMode.BETWEEN_DATE_SELECTED);
        dpFilter.setUsePanelOption(true);
        dpFilter.setBackground(Color.GRAY);
        dpFilter.setDateFormat("dd/MM/yyyy");
        dpFilter.addDateSelectionListener(new DateSelectionListener() {
            @Override
            public void dateSelected(DateSelectionEvent dateSelectionEvent) {
                loadData();
            }
        });
        dpFilter.setEditor(dateSearchTextField);
        
        JPanel north = new JPanel(new BorderLayout());
        JPanel titulo = LightTheme.createTitle("Balance Cliente");
        JPanel customerInfo = new JPanel(new FlowLayout(FlowLayout.CENTER));
        customerInfo.add(customerInfoLabel);
        customerInfo.add(totalBalanceLabel);
        customerInfo.add(creditBalanceLabel);
        customerInfoLabel.setFont(LightTheme.getSubTitleFont().deriveFont(19f));
        creditBalanceLabel.setFont(LightTheme.getSubTitleFont().deriveFont(16f));
        titulo.add(customerInfo, BorderLayout.SOUTH);
        
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filterPanel.add(new JLabel("Nro Venta: ", JLabel.RIGHT));
        filterPanel.add(idSearchTextField);
        filterPanel.add(new JLabel("Desde/Hasta ", JLabel.RIGHT));
        filterPanel.add(dateSearchTextField);
        
        dateSearchTextField.putClientProperty(FlatClientProperties.TEXT_FIELD_SHOW_CLEAR_BUTTON, true);
        idSearchTextField.putClientProperty(FlatClientProperties.TEXT_FIELD_SHOW_CLEAR_BUTTON, true);
        observationSearchTextField.putClientProperty(FlatClientProperties.TEXT_FIELD_SHOW_CLEAR_BUTTON, true);
        
        filterPanel.add(refreshButton);
        LightTheme.aplicarEstiloPrimario(refreshButton);
        //filterPanel.add(showAllButton);
        //LightTheme.aplicarEstiloSecundario(showAllButton);
        filterPanel.add(multiPayButton);
        LightTheme.aplicarEstiloPrimario(multiPayButton);
        filterPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createEmptyBorder(0, 10, 10, 10),  
            BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(EtchedBorder.LOWERED),
                "Filtros",
                TitledBorder.LEFT,
                TitledBorder.TOP,
                new Font("Montserrat-Medium", Font.PLAIN, 14),
                Color.BLACK
            )
        ));
        
        north.add(titulo, BorderLayout.NORTH);
        north.add(filterPanel, BorderLayout.SOUTH);

        add(north, BorderLayout.NORTH);
        
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, saleTablePanel, paysTablePanel);
        splitPane.setResizeWeight(1);
        splitPane.setDividerSize(8);
        splitPane.setDividerLocation(splitPane.getWidth() - paysTablePanel.getPreferredSize().width);
        add(splitPane, BorderLayout.CENTER);
    }

    private void createPaysTablePanel() {
        paysTableModel = new DefaultTableModel(PAYS_COLUMNS, 0) {
            private static final long serialVersionUID = 2888488979870981328L;
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
            @Override
            public Class<?> getColumnClass(int column) {
                return switch (column) {
                    case 0 -> LocalDate.class;
                    case 5 -> Long.class;
                    default -> String.class;
                };
            }
        };
        paysTable = new JTable(paysTableModel);
        paysTable.setToolTipText("Click en 'Ver >' para ver detalle de asignaciones");
        paysTable.setShowGrid(true);
        paysTable.getColumnModel().getColumn(5).setMaxWidth(0);
        paysTable.getColumnModel().getColumn(5).setMinWidth(0);
        paysTable.getColumnModel().getColumn(5).setPreferredWidth(0);
        setupClickableColumn();
        
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);

        DefaultTableCellRenderer dateRenderer = new DefaultTableCellRenderer() {
            @Override
            protected void setValue(Object value) {
                if (value instanceof LocalDate) 
                    value = ((LocalDate) value).format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                super.setValue(value);
            }
        };
        dateRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        paysTable.setDefaultRenderer(LocalDate.class, dateRenderer);

        paysTablePanel = new JPanel(new BorderLayout());
        paysTablePanel.add(new JScrollPane(paysTable), BorderLayout.CENTER);
        
        JPanel northPayPanel = new JPanel(new BorderLayout());
        JPanel descriptionPanel = new JPanel();
        descriptionPanel.add(new JLabel("Descripcion: ", JLabel.RIGHT));
        descriptionPanel.add(observationSearchTextField);
        
        northPayPanel.add(LightTheme.createSubTitle("Pagos"), BorderLayout.NORTH);
        northPayPanel.add(descriptionPanel, BorderLayout.SOUTH);
        paysTablePanel.add(northPayPanel, BorderLayout.NORTH);
    }
    private void setupClickableColumn() {
    	
	 int linkColumnViewIndex = 4; 
	
	     paysTable.getColumnModel().getColumn(linkColumnViewIndex).setCellRenderer(new DefaultTableCellRenderer() {
	         @Override
	         public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,boolean hasFocus, int row, int column) {
	             JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

	             String text = value == null ? "" : value.toString();

	             int hoverRow = paysTable.rowAtPoint(
	                     paysTable.getMousePosition() != null ? paysTable.getMousePosition() : new java.awt.Point(-1, -1));

	             boolean isHover = (row == hoverRow);

	             if (isSelected) {
	                 label.setForeground(table.getSelectionForeground());
	             } else if (isHover) {
	                 label.setForeground(new Color(0, 70, 160)); // más oscuro al hover
	             } else {
	                 label.setForeground(new Color(0, 102, 204)); // azul normal
	             }
	             label.setText("<html><u>" + text + "</u></html>");
	             label.setHorizontalAlignment(SwingConstants.CENTER);
	             return label;
	         }
	     });

	     paysTable.addMouseMotionListener(new MouseMotionAdapter() {
	         @Override
	         public void mouseMoved(MouseEvent e) {
	             int col = paysTable.columnAtPoint(e.getPoint());
	             if (col == linkColumnViewIndex) {
	                 paysTable.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
	             } else {
	                 paysTable.setCursor(Cursor.getDefaultCursor());
	             }
	             paysTable.repaint(); 
	         }
	     });
	
	     paysTable.addMouseListener(new MouseAdapter() {
	         @Override
	         public void mouseClicked(MouseEvent e) {
	             int viewRow = paysTable.rowAtPoint(e.getPoint());
	             int viewCol = paysTable.columnAtPoint(e.getPoint());
	
	             if (viewRow == -1 || viewCol != linkColumnViewIndex) return;
	             int modelRow = paysTable.convertRowIndexToModel(viewRow);
	
	             Long payId = (Long) paysTableModel.getValueAt(modelRow, 5);
	
	             MultiPaymentResponse payment = paymentsList.stream()
	                     .filter(p -> p.getPaymentId().equals(payId))
	                     .findFirst()
	                     .orElse(null);
	
	             if (payment != null) {
	                 showPaymentDetails(payment);
	             }
	         }
	     });
	}
    
    private void createSaleTablePanel() {
        saleTableModel = new DefaultTableModel(SALE_COLUMNS, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
            @Override
            public Class<?> getColumnClass(int column) {
                switch (column) {
                case 0: return LocalDate.class;
                case 1: return String.class;
                case 2: return String.class;
                case 3: return String.class;
                case 4: return Long.class;   
                default: return Object.class;
                }
            }
        };
        saleTable = new JTable(saleTableModel);
        //saleTable.setToolTipText("Click derecho para Pago Multiple");
        saleTable.setShowGrid(true);
        saleTable.getColumnModel().getColumn(saleTable.getColumnCount() - 1).setMaxWidth(90);
        saleTable.getColumnModel().getColumn(saleTable.getColumnCount() - 1).setMinWidth(90);
        saleTable.getColumnModel().getColumn(saleTable.getColumnCount() - 1).setPreferredWidth(90);
        
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        for (int i = 0; i < saleTable.getColumnCount(); i++) {
            saleTable.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }
        
        DefaultTableCellRenderer dateRenderer = new DefaultTableCellRenderer() {
            @Override
            protected void setValue(Object value) {
                if (value instanceof LocalDate) 
                    value = ((LocalDate) value).format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                super.setValue(value);
            }
        };
        dateRenderer.setHorizontalAlignment(SwingConstants.CENTER); 
        saleTable.setDefaultRenderer(LocalDate.class, dateRenderer);
        
        saleTable.getColumnModel().getColumn(2).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public java.awt.Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel c = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                c.setHorizontalAlignment(SwingConstants.CENTER);
                c.setForeground(new Color(0, 128, 0));
                return c;
            }
        });
        
        /*saleTable.getColumnModel().getColumn(3).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public java.awt.Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel c = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                c.setHorizontalAlignment(SwingConstants.CENTER);
                if (value != null) {
                    String texto = value.toString().replace("$", "").replace(",", ".").trim();
                    try {
                        BigDecimal val = new BigDecimal(texto);
                        if (val.compareTo(BigDecimal.ZERO) < 0) 
                            c.setForeground(Color.RED);
                        else 
                            c.setForeground(LightTheme.COLOR_GREEN_MONEY);
                    } catch (Exception e) {}
                }
                if (isSelected) {
                    c.setForeground(table.getSelectionForeground());
                }
                return c;
            }
        });*/
        
        saleTablePanel = new JPanel(new BorderLayout());
        saleTablePanel.add(LightTheme.createSubTitle("Ventas Pendientes"), BorderLayout.NORTH);
        saleTablePanel.add(new JScrollPane(saleTable), BorderLayout.CENTER);
    }

    private void clearFields() {
        saleTable.clearSelection();
        paysTableModel.setRowCount(0);
        saleTableModel.setRowCount(0);
        dpFilter.clearSelectedDate();
        idSearchTextField.setText("");
        observationSearchTextField.setText("");
        customerInfoLabel.setText("");
        salePaidMap.clear();
        
        messageLabel.setText("");
        messageLabel.setOpaque(false);
    }

    private void applyCombinedFilters() {
        String saleNumberText = idSearchTextField.getText().trim();
        List<RowFilter<Object, Object>> filters = new ArrayList<>();

        if (!saleNumberText.isEmpty())
            filters.add(RowFilter.regexFilter("(?i)" + Pattern.quote(saleNumberText), saleTable.getColumnCount()-1));

        if (filters.isEmpty())
            sorter.setRowFilter(null);
        else
            sorter.setRowFilter(RowFilter.andFilter(filters));
    }
    
    private void applyPaysFilter() {
        String obsText = observationSearchTextField.getText().trim();
        if (obsText.isEmpty()) {
            paysSorter.setRowFilter(null);
        } else {
            final String searchText = obsText.toLowerCase();
            paysSorter.setRowFilter(new RowFilter<DefaultTableModel, Integer>() {
                @Override
                public boolean include(RowFilter.Entry<? extends DefaultTableModel, ? extends Integer> entry) {
                    int modelRow = entry.getIdentifier();
                    Long payId = (Long) paysTableModel.getValueAt(modelRow, 5);
                    
                    MultiPaymentResponse pay = paymentsList.stream()
                        .filter(p -> p.getPaymentId().equals(payId))
                        .findFirst().orElse(null);
                    
                    if (pay == null) return false;
                    
                    if (pay.getDescription() != null && pay.getDescription().toLowerCase().contains(searchText)) return true;
                    
                    if (pay.getSplits() != null) {
                        for (MultiPaymentResponse.PaymentSplitResponse split : pay.getSplits()) {
                            if (split.getReference() != null && split.getReference().toLowerCase().contains(searchText)) return true;
                        }
                    }
                    return false;
                }
            });
        }
    }
    
    private void setupLiveFilterListeners() {
        DocumentListener debounceListener = new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) { reiniciarTimer(); }
            @Override
            public void removeUpdate(DocumentEvent e) { reiniciarTimer(); }
            @Override
            public void changedUpdate(DocumentEvent e) { reiniciarTimer(); }
            private void reiniciarTimer() {
                if (filtroTimer != null && filtroTimer.isRunning())
                    filtroTimer.restart();
                else {
                    filtroTimer = new Timer(200, evt -> {
                        applyCombinedFilters();
                        applyPaysFilter();
                    });
                    filtroTimer.setRepeats(false);
                    filtroTimer.start();
                }
            }
        };
        idSearchTextField.getDocument().addDocumentListener(debounceListener);
        observationSearchTextField.getDocument().addDocumentListener(debounceListener);
    }

    private void createJPopupMenu() {
        new JPopupMenuModifyDelete(paysTable, this::deletePayment, "Eliminar Pago");
        /*new JPopupMenuModifyDelete(saleTable, this::openMultiPaymentDialog, "Pago Multiple")*/;
    }

    @Override
    public void refresh() {
        clearFields();
        refreshFromDB();
    }
    
    private void createMessageLabel() {
        messageLabel = LightTheme.createMessageLabel();
        JPanel horizontalPanel = new JPanel(new GridLayout());
        horizontalPanel.add(messageLabel);
        add(horizontalPanel, BorderLayout.SOUTH);
        
        JPanel totalsPanel = new JPanel(new GridLayout(1, 3, 10, 0));
        totalsPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        totalSalesLabel.setFont(new Font("Montserrat", Font.BOLD, 20));
        totalPaysLabel.setFont(new Font("Montserrat", Font.BOLD, 20));
        totalBalanceLabel.setFont(new Font("Montserrat", Font.BOLD, 20));
        totalSalesLabel.setForeground(new Color(0, 128, 0));
        totalPaysLabel.setForeground(new Color(0, 0, 255));   

        totalsPanel.add(totalSalesLabel);
        totalsPanel.add(totalBalanceLabel);
        totalsPanel.add(totalPaysLabel);
    }

    private void setMessage(String message) {
        messageLabel.setText(message);
        messageLabel.setOpaque(true);
    }
}
