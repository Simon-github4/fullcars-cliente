package views;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.regex.Pattern;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.RowFilter;
import javax.swing.RowSorter;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.Timer;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

import com.formdev.flatlaf.FlatClientProperties;

import Utils.Icons;
import Utils.NumberFormatArg;
import Utils.PdfResumenCuentaUtils;
import Utils.ServerException;
import controller.CustomerController;
import controller.PayController;
import controller.SaleController;
import data.service.ClienteRestPayments;
import dtos.AllocationInfo;
import dtos.CreditInfo;
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
import views.components.PaymentDetailDialog;
import views.transactions.MultiPaymentDialog;

public class CustomerSummaryHistory extends JPanel implements Refreshable {
    private static final long serialVersionUID = 1L;

    private final CustomerController controller;
    private final PayController payController;
    //private final SaleController saleController;
    private final ClienteRestPayments restPayments;
    
    private PendingSalesResponse pendingData;
    private List<MultiPaymentResponse> paymentsList = new ArrayList<>();
    //private Map<Long, BigDecimal> salePaidMap = new HashMap<>();
    
    private JLabel customerInfoLabel = new JLabel("", JLabel.CENTER);
    private JLabel creditBalanceLabel = new JLabel(" - Credito a Usar: $0.00", JLabel.CENTER);
    
    private JPanel paysTablePanel;
    private static final Object[] PAYS_COLUMNS = {"Fecha", "Monto", "Credito Gen.", "Descripcion", "Ver Detalle", "id" };
    private JTable paysTable;
    private DefaultTableModel paysTableModel;
    
    private JPanel saleTablePanel;
    private static final Object[] SALE_COLUMNS = {"Fecha", "Tipo", "Nro siniestro","Total", "Pendiente", "CAE", "Nro. Venta"};
    private static final int SALE_COL_TIPO = 1;
    private static final int SALE_COL_TOTAL = 3;
    private static final int SALE_COL_PENDIENTE = 4;
    private static final String TIPO_VENTA = "Venta";
    private JTable saleTable;
    private DefaultTableModel saleTableModel;
    /** Venta (SalePendingInfo) o credito (CreditInfo) de cada fila del modelo de saleTable, por indice. */
    private final List<Object> saleRowSources = new ArrayList<>();
    private Customer currentCustomer;

    private JTextField idSearchTextField = new JTextField("", 10);
    private JTextField observationSearchTextField = new JTextField("", 13);
    private JFormattedTextField dateSearchTextField = new JFormattedTextField();
    private DatePicker dpFilter = new DatePickerS();
    private JButton refreshButton = new JButton("Actualizar Datos", Icons.REFRESH.create(18, 18));
    private JButton multiPayButton = new JButton("Pago Multiple", Icons.NEW.create(18, 18));
    private JButton exportPdfButton = new JButton("Resumen", Icons.PDF.create(18, 18));
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
        //this.saleController = saleController;
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
        paySortKeys.add(new javax.swing.RowSorter.SortKey(5, javax.swing.SortOrder.DESCENDING)); // mismo dia: el pago mas nuevo (id) primero
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
    
    /** Datos del cliente traidos del servidor en una sola carga. */
    private record LoadedData(PendingSalesResponse pending, List<MultiPaymentResponse> payments, Customer customer) {}

    private void loadDataAsync() {
        loadDataAsync(null, null);
    }

    /**
     * Trae del servidor ventas pendientes, creditos, pagos y datos del cliente, y recien si todo salio bien los
     * aplica a la pantalla y corre {@code onLoaded}. Si algo falla se avisa y la pantalla no queda a medio actualizar.
     * {@code onFinished} corre siempre al terminar (bien o mal). Ambos corren en el EDT.
     */
    private void loadDataAsync(Runnable onLoaded, Runnable onFinished) {
        Long customerId = controller.getCustomerSelectedId();

        new SwingWorker<LoadedData, Void>() {
            @Override
            protected LoadedData doInBackground() throws Exception {
                PendingSalesResponse pending = restPayments.getPendingSales(customerId);
                List<MultiPaymentResponse> payments = restPayments.getPaymentsByCustomer(customerId);
                Customer customer = controller.getCustomer(customerId);
                if (customer == null) throw new IOException("No se pudieron obtener los datos del cliente");
                return new LoadedData(pending, payments, customer);
            }

            @Override
            protected void done() {
                try {
                    LoadedData data = get();
                    pendingData = data.pending();
                    paymentsList = data.payments();
                    loadData();

                    Customer customer = data.customer();
                    currentCustomer = customer;
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

                    if (onLoaded != null) onLoaded.run();
                } catch (Exception e) {
                    Throwable cause = e instanceof ExecutionException && e.getCause() != null ? e.getCause() : e;
                    JOptionPane.showMessageDialog(CustomerSummaryHistory.this,
                            "Error al cargar datos: " + cause.getMessage(),
                            "Error", JOptionPane.ERROR_MESSAGE);
                    e.printStackTrace();
                } finally {
                    if (onFinished != null) onFinished.run();
                }
            }
        }.execute();
    }
    
    private boolean isWithinSelectedRange(LocalDate date) {
        LocalDate[] range = dpFilter.getSelectedDateRange();
        return range == null || date == null || (date.compareTo(range[0]) >= 0 && date.compareTo(range[1]) <= 0);
    }

    private void loadData() {
        saleTableModel.setRowCount(0);
        saleRowSources.clear();
        sorter.setSortKeys(List.of(new javax.swing.RowSorter.SortKey(0, javax.swing.SortOrder.DESCENDING)));

        for (dtos.PendingSalesResponse.SalePendingInfo sale : pendingData.getSales()) {
            if (isWithinSelectedRange(sale.getDate())) {
                //BigDecimal paid = salePaidMap.getOrDefault(sale.getSaleId(), BigDecimal.ZERO);
                //BigDecimal saldo = paid.subtract(sale.getTotal()).setScale(2, RoundingMode.HALF_UP);

                Object[] row = {
                    sale.getDate(),
                    TIPO_VENTA,
                    sale.getSaleNumber()!= null ? sale.getSaleNumber() : "Particular",
                    NumberFormatArg.format(sale.getTotal()),
                    NumberFormatArg.format(sale.getRemainingDue()),
                    sale.getCae() != null? sale.getCae(): "No hay cae, revisar si tiene factura",
                    sale.getSaleId()
                };
                saleTableModel.addRow(row);
                saleRowSources.add(sale);
            }
        }

        // Creditos a favor aun sin usar (devoluciones y saldos por excedente): en negativo, como en un resumen de cuenta.
        // Igual que en las ventas: Total = monto original del credito, Pendiente = lo que queda sin usar.
        List<CreditInfo> credits = pendingData.getCredits() != null ? pendingData.getCredits() : new ArrayList<>();
        for (CreditInfo credit : credits) {
            if (isWithinSelectedRange(credit.getDate())) {
                Object[] row = {
                    credit.getDate(),
                    credit.getLabel(), // "N/C" o "Excedente de Pago#{id}" (lo define el servidor)
                    credit.getSaleNumber() != null ? credit.getSaleNumber() : (credit.getSaleId() != null ? "Particular" : "-"),
                    NumberFormatArg.format(credit.getAmount().negate()),
                    NumberFormatArg.format(credit.getRemaining().negate()),
                    credit.getCae() != null ? credit.getCae()
                            : (CreditInfo.TYPE_DEVOLUCION_INTERNA.equals(credit.getType()) ? "Sin CAE (interna)" : "-"),
                    credit.getSaleId()
                };
                saleTableModel.addRow(row);
                saleRowSources.add(credit);
            }
        }
        loadPays();
    }

    /** Exporta a PDF lo que se ve en "Ventas Pendientes" (con filtros y en el orden de pantalla) y el balance. */
    private void exportarResumenPdf() {
        if (controller.getCustomerSelectedId() == null || currentCustomer == null) {
            JOptionPane.showMessageDialog(this, "Seleccione un Cliente y espere a que carguen sus datos");
            return;
        }
        // Antes de exportar se traen del servidor los datos mas recientes (manteniendo filtros y orden de pantalla).
        exportPdfButton.setEnabled(false);
        List<RowSorter.SortKey> sortKeys = new ArrayList<>(sorter.getSortKeys());
        loadDataAsync(() -> {
            sorter.setSortKeys(sortKeys);
            guardarResumenPdf();
        }, () -> exportPdfButton.setEnabled(true));
    }

    private void guardarResumenPdf() {
        byte[] pdf;
        try {
            pdf = generarResumenPdf();
        } catch (IOException e) {
            setMessage("No se pudo generar el PDF: " + e.getMessage());
            return;
        }

        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Guardar resumen de cuenta");
        File downloads = new File(System.getProperty("user.home"), "Downloads");
        chooser.setCurrentDirectory(downloads);
        String cliente = currentCustomer.getFullName().trim().replaceAll("[^\\p{L}\\p{N}]+", "_");
        String fecha = LocalDate.now().format(DateTimeFormatter.ofPattern("d-M-yyyy"));
        chooser.setSelectedFile(archivoLibre(new File(downloads, "Resumen_" + cliente + "_" + fecha + ".pdf")));
        chooser.setFileFilter(new FileNameExtensionFilter("Archivos PDF", "pdf"));
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;

        File file = chooser.getSelectedFile();
        if (!file.getName().toLowerCase().endsWith(".pdf")) file = new File(file.getAbsolutePath() + ".pdf");
        file = archivoLibre(file); // nunca pisa un resumen anterior
        try {
            Files.write(file.toPath(), pdf);
            if (Desktop.isDesktopSupported()) Desktop.getDesktop().open(file);
        } catch (IOException e) {
            setMessage("No se pudo guardar el PDF: " + e.getMessage());
        }
    }

    /**
     * Si el archivo ya existe devuelve el primero libre agregando " (1)", " (2)"... antes de la extension, como hace
     * Windows con las descargas: "Resumen_Quique_28-9-2026.pdf" -> "Resumen_Quique_28-9-2026 (1).pdf".
     */
    static File archivoLibre(File file) {
        if (!file.exists()) return file;
        String name = file.getName();
        int dot = name.lastIndexOf('.');
        String base = dot > 0 ? name.substring(0, dot) : name;
        String ext = dot > 0 ? name.substring(dot) : "";
        for (int n = 1; ; n++) {
            File candidate = new File(file.getParentFile(), base + " (" + n + ")" + ext);
            if (!candidate.exists()) return candidate;
        }
    }

    private byte[] generarResumenPdf() throws IOException {
        List<PdfResumenCuentaUtils.Linea> lineas = new ArrayList<>();
        for (int r = 0; r < saleTable.getRowCount(); r++) {
            Object src = saleRowSources.get(saleTable.convertRowIndexToModel(r));
            if (src instanceof SalePendingInfo sale) {
                lineas.add(new PdfResumenCuentaUtils.Linea(sale.getDate(), TIPO_VENTA, sale.getSaleId(), sale.getSaleNumber(),
                        sale.getTotal(), sale.getRemainingDue(), false));
            } else if (src instanceof CreditInfo credit) {
                lineas.add(new PdfResumenCuentaUtils.Linea(credit.getDate(), credit.getLabel(), credit.getSaleId(), credit.getSaleNumber(),
                        credit.getAmount().negate(), credit.getRemaining().negate(), true));
            }
        }

        LocalDate[] range = dpFilter.getSelectedDateRange();
        return PdfResumenCuentaUtils.generarResumen(currentCustomer, lineas,
                range != null ? range[0] : null, range != null ? range[1] : null,
                idSearchTextField.getText().trim(), pendingData.getTotalPending(), pendingData.getCreditBalance());
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
        exportPdfButton.addActionListener(e -> exportarResumenPdf());
        exportPdfButton.setToolTipText("Exportar el resumen de cuenta en PDF (actualiza los datos desde el servidor)");
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
        filterPanel.add(exportPdfButton);
        LightTheme.aplicarEstiloPrimario(exportPdfButton);
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
	                 PaymentDetailDialog.show(payment);
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
                case 4: return String.class;
                case 5: return String.class;
                case 6: return Long.class;
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
        // El centerRenderer de arriba se asigna por columna y pisa al default por clase: la fecha lo necesita explicito.
        // El modelo sigue guardando LocalDate, asi el orden por fecha es cronologico y no alfabetico.
        saleTable.getColumnModel().getColumn(0).setCellRenderer(dateRenderer);

        // Montos: los creditos a favor (filas que no son "Venta") van en rojo; el Total de las ventas en verde, como antes.
        DefaultTableCellRenderer amountRenderer = new DefaultTableCellRenderer() {
            @Override
            public java.awt.Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel c = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                c.setHorizontalAlignment(SwingConstants.CENTER);
                Object tipo = table.getModel().getValueAt(table.convertRowIndexToModel(row), SALE_COL_TIPO);
                boolean isCredit = !TIPO_VENTA.equals(tipo);
                if (isSelected)
                    c.setForeground(table.getSelectionForeground());
                else if (isCredit)
                    c.setForeground(Color.RED);
                else if (table.convertColumnIndexToModel(column) == SALE_COL_TOTAL)
                    c.setForeground(new Color(0, 128, 0));
                else
                    c.setForeground(table.getForeground());
                return c;
            }
        };
        saleTable.getColumnModel().getColumn(SALE_COL_TOTAL).setCellRenderer(amountRenderer);
        saleTable.getColumnModel().getColumn(SALE_COL_PENDIENTE).setCellRenderer(amountRenderer);
        
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
        saleTablePanel.add(LightTheme.createSubTitle("Ventas Pendientes y Creditos a Favor"), BorderLayout.NORTH);
        saleTablePanel.add(new JScrollPane(saleTable), BorderLayout.CENTER);
    }

    private void clearFields() {
        saleTable.clearSelection();
        paysTableModel.setRowCount(0);
        saleTableModel.setRowCount(0);
        saleRowSources.clear();
        dpFilter.clearSelectedDate();
        idSearchTextField.setText("");
        observationSearchTextField.setText("");
        customerInfoLabel.setText("");
        currentCustomer = null; // hasta que termine de cargar el cliente nuevo no se puede exportar
        //salePaidMap.clear();
        
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
