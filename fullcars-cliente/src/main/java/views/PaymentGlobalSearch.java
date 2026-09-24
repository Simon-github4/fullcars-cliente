package views;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.Timer;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

import com.formdev.flatlaf.FlatClientProperties;

import Utils.NumberFormatArg;
import Utils.ServerException;
import controller.PayController;
import dtos.MultiPaymentResponse;
import views.components.LightTheme;
import views.components.PaymentDetailDialog;

public class PaymentGlobalSearch extends JPanel {
    private static final long serialVersionUID = 1L;

    private static final int COL_CLIENTE = 1;
    private static final int COL_VER_DETALLE = 4;
    private static final int COL_ID = 5;
    private static final int MIN_SEARCH_LENGTH = 2;

    private static final Object[] COLUMNS = {"Fecha", "Cliente", "Monto", "Descripcion", "Ver Detalle", "id"};

    private final PayController payController;
    private final Consumer<Long> onGoToCustomer;

    private List<MultiPaymentResponse> resultsList = new ArrayList<>();
    private final AtomicInteger searchSeq = new AtomicInteger(0);

    private JTextField searchTextField = new JTextField("", 25);
    private JTable resultsTable;
    private DefaultTableModel resultsTableModel;
    private JLabel messageLabel = LightTheme.createMessageLabel();
    private Timer debounceTimer;

    public PaymentGlobalSearch(PayController payController, Consumer<Long> onGoToCustomer) {
        this.payController = payController;
        this.onGoToCustomer = onGoToCustomer;

        setLayout(new BorderLayout(0, 0));
        createResultsTable();
        initUI();
        setupLiveSearchListener();
    }

    private void initUI() {
        JPanel north = new JPanel(new BorderLayout());
        north.add(LightTheme.createTitle("Buscar Pagos - Todos los Clientes"), BorderLayout.NORTH);

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.add(new JLabel("Descripcion / Referencia: ", JLabel.RIGHT));
        searchTextField.putClientProperty(FlatClientProperties.TEXT_FIELD_SHOW_CLEAR_BUTTON, true);
        searchPanel.add(searchTextField);
        searchPanel.setBorder(BorderFactory.createCompoundBorder(
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
        north.add(searchPanel, BorderLayout.SOUTH);

        add(north, BorderLayout.NORTH);
        add(new JScrollPane(resultsTable), BorderLayout.CENTER);

        JPanel messagePanel = new JPanel(new GridLayout());
        messagePanel.add(messageLabel);
        add(messagePanel, BorderLayout.SOUTH);
    }

    private void createResultsTable() {
        resultsTableModel = new DefaultTableModel(COLUMNS, 0) {
            private static final long serialVersionUID = 1L;
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
        resultsTable = new JTable(resultsTableModel);
        resultsTable.setToolTipText("Click en el Cliente para ir a su resumen, o en 'Ver >' para el detalle del pago");
        resultsTable.setShowGrid(true);
        resultsTable.getColumnModel().getColumn(COL_ID).setMaxWidth(0);
        resultsTable.getColumnModel().getColumn(COL_ID).setMinWidth(0);
        resultsTable.getColumnModel().getColumn(COL_ID).setPreferredWidth(0);

        DefaultTableCellRenderer dateRenderer = new DefaultTableCellRenderer() {
            @Override
            protected void setValue(Object value) {
                if (value instanceof LocalDate)
                    value = ((LocalDate) value).format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                super.setValue(value);
            }
        };
        dateRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        resultsTable.setDefaultRenderer(LocalDate.class, dateRenderer);

        setupClickableColumns();
    }

    private void setupClickableColumns() {
        DefaultTableCellRenderer linkRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

                String text = value == null ? "" : value.toString();
                int hoverRow = resultsTable.rowAtPoint(
                        resultsTable.getMousePosition() != null ? resultsTable.getMousePosition() : new Point(-1, -1));
                boolean isHover = (row == hoverRow);

                if (isSelected) {
                    label.setForeground(table.getSelectionForeground());
                } else if (isHover) {
                    label.setForeground(new Color(0, 70, 160));
                } else {
                    label.setForeground(new Color(0, 102, 204));
                }
                label.setText("<html><u>" + text + "</u></html>");
                label.setHorizontalAlignment(SwingConstants.CENTER);
                return label;
            }
        };
        resultsTable.getColumnModel().getColumn(COL_CLIENTE).setCellRenderer(linkRenderer);
        resultsTable.getColumnModel().getColumn(COL_VER_DETALLE).setCellRenderer(linkRenderer);

        resultsTable.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                int col = resultsTable.columnAtPoint(e.getPoint());
                if (col == COL_CLIENTE || col == COL_VER_DETALLE) {
                    resultsTable.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                } else {
                    resultsTable.setCursor(Cursor.getDefaultCursor());
                }
                resultsTable.repaint();
            }
        });

        resultsTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int viewRow = resultsTable.rowAtPoint(e.getPoint());
                int viewCol = resultsTable.columnAtPoint(e.getPoint());
                if (viewRow == -1) return;

                int modelRow = resultsTable.convertRowIndexToModel(viewRow);
                Long paymentId = (Long) resultsTableModel.getValueAt(modelRow, COL_ID);

                MultiPaymentResponse payment = resultsList.stream()
                        .filter(p -> p.getPaymentId().equals(paymentId))
                        .findFirst()
                        .orElse(null);
                if (payment == null) return;

                if (viewCol == COL_CLIENTE) {
                    onGoToCustomer.accept(payment.getCustomerId());
                } else if (viewCol == COL_VER_DETALLE) {
                    PaymentDetailDialog.show(payment);
                }
            }
        });
    }

    private void setupLiveSearchListener() {
        DocumentListener debounceListener = new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) { reiniciarTimer(); }
            @Override
            public void removeUpdate(DocumentEvent e) { reiniciarTimer(); }
            @Override
            public void changedUpdate(DocumentEvent e) { reiniciarTimer(); }
            private void reiniciarTimer() {
                if (debounceTimer != null && debounceTimer.isRunning())
                    debounceTimer.restart();
                else {
                    debounceTimer = new Timer(200, evt -> doSearch());
                    debounceTimer.setRepeats(false);
                    debounceTimer.start();
                }
            }
        };
        searchTextField.getDocument().addDocumentListener(debounceListener);
    }

    private void doSearch() {
        String text = searchTextField.getText().trim();
        int mySeq = searchSeq.incrementAndGet();

        if (text.length() < MIN_SEARCH_LENGTH) {
            resultsList = new ArrayList<>();
            resultsTableModel.setRowCount(0);
            setMessage(text.isEmpty() ? "" : "Ingrese al menos " + MIN_SEARCH_LENGTH + " caracteres");
            return;
        }

        new SwingWorker<List<MultiPaymentResponse>, Void>() {
            @Override
            protected List<MultiPaymentResponse> doInBackground() throws Exception {
                return payController.searchPayments(text);
            }

            @Override
            protected void done() {
                if (mySeq != searchSeq.get()) return; // una busqueda mas reciente ya esta en curso

                try {
                    List<MultiPaymentResponse> results = get();
                    resultsList = results;
                    loadResults();
                    setMessage(results.isEmpty() ? "No se encontraron pagos para: " + text : "");
                } catch (Exception e) {
                    Throwable cause = e.getCause() != null ? e.getCause() : e;
                    String msg = cause instanceof ServerException || cause instanceof IOException
                            ? cause.getMessage() : "Error al buscar pagos: " + cause.getMessage();
                    setMessage(msg);
                }
            }
        }.execute();
    }

    private void loadResults() {
        resultsTableModel.setRowCount(0);
        for (MultiPaymentResponse payment : resultsList) {
            Object[] row = {
                payment.getDate(),
                payment.getCustomerName() != null ? payment.getCustomerName() : "Cliente #" + payment.getCustomerId(),
                NumberFormatArg.format(payment.getTotalAmount()),
                payment.getDescription() != null && !payment.getDescription().isEmpty() ? payment.getDescription() : "-",
                "Ver >",
                payment.getPaymentId()
            };
            resultsTableModel.addRow(row);
        }
    }

    private void setMessage(String message) {
        messageLabel.setText(message);
        messageLabel.setOpaque(!message.isEmpty());
    }
}
