package views.transactions;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.swing.AbstractCellEditor;
import javax.swing.BorderFactory;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingWorker;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;

import controller.PayController;
import dtos.MultiPaymentRequest;
import dtos.MultiPaymentRequest.PaymentSplitRequest;
import dtos.MultiPaymentResponse;
import dtos.MultiPaymentResponse.PaymentSplitResponse;
import dtos.PendingSalesResponse;
import dtos.PendingSalesResponse.SalePendingInfo;
import model.client.entities.Customer;
import raven.datetime.DatePicker;
import raven.datetime.DatePicker.DateSelectionMode;
import views.components.BigDecimalField;

public class MultiPaymentDialog extends JDialog {

    private Customer customer;
    private PayController controller;

    private JTable salesTable;
    private SalesTableModel salesTableModel;
    private JTable splitsTable;
    private SplitsTableModel splitsTableModel;

    private JLabel creditBalanceLabel;
    private JCheckBox useCreditCheck;
    private JCheckBox selectAllCheck;
    private JLabel totalSelectedLabel;
    private JLabel totalSplitsLabel;
    private JLabel diffLabel;

    private DatePicker datePicker;
    private JTextField descriptionField;

    private JButton addSplitButton;
    private JButton removeSplitButton;
    private JButton confirmButton;
    private JButton cancelButton;

    private PendingSalesResponse pendingData;
    private MultiPaymentResponse result;

    private NumberFormat currencyFormat;

    public MultiPaymentDialog(Frame owner, Customer customer) {
        super(owner, "Pago Multiple", true);
        this.customer = customer;
        this.controller = new PayController();
        this.currencyFormat = NumberFormat.getNumberInstance(Locale.forLanguageTag("es-AR"));
        this.currencyFormat.setMinimumFractionDigits(2);
        this.currencyFormat.setMaximumFractionDigits(2);

        initComponents();
        layoutComponents();
        attachListeners();
        loadData();

        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        setSize(screen.width - 100, screen.height - 100);
        setLocationRelativeTo(owner);
    }

    private void initComponents() {
        creditBalanceLabel = new JLabel("$0,00", JLabel.RIGHT);
        creditBalanceLabel.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 0));

        useCreditCheck = new JCheckBox("Usar credito a favor");
        selectAllCheck = new JCheckBox("Seleccionar todas");

        totalSelectedLabel = new JLabel("Total ventas: $0,00");
        totalSplitsLabel = new JLabel("Total pagos: $0,00");
        diffLabel = new JLabel("Diferencia: $0,00");
        diffLabel.setForeground(Color.RED);

        datePicker = new DatePicker();
        datePicker.setDateSelectionMode(DateSelectionMode.SINGLE_DATE_SELECTED);
        datePicker.setSelectedDate(LocalDate.now());
        descriptionField = new JTextField(25);

        salesTableModel = new SalesTableModel();
        salesTable = new JTable(salesTableModel);
        salesTable.setRowHeight(25);
        salesTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        splitsTableModel = new SplitsTableModel();
        splitsTable = new JTable(splitsTableModel);
        splitsTable.setRowHeight(28);

        JComboBox<String> methodCombo = new JComboBox<>(new String[]{
            "Efectivo", "Tarjeta", "Transferencia", "Cheque", "ECheq", "Deposito"
        });
        splitsTable.getColumnModel().getColumn(0).setCellEditor(new DefaultCellEditor(methodCombo));

        BigDecimalFieldRenderer amountRenderer = new BigDecimalFieldRenderer();
        splitsTable.getColumnModel().getColumn(1).setCellEditor(new BigDecimalCellEditor());
        splitsTable.getColumnModel().getColumn(1).setCellRenderer(amountRenderer);

        addSplitButton = new JButton("+ Agregar metodo");
        removeSplitButton = new JButton("- Quitar");
        confirmButton = new JButton("Confirmar Pago");
        confirmButton.setBackground(new Color(40, 167, 69));
        confirmButton.setForeground(Color.WHITE);
        cancelButton = new JButton("Cancelar");
    }

    private void layoutComponents() {
        JPanel main = new JPanel(new BorderLayout(10, 10));
        main.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel top = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        
        gbc.gridx = 0; gbc.gridy = 0;
        top.add(new JLabel("Cliente: " + customer.getFullName()), gbc);
        
        gbc.gridx = 1;
        top.add(new JLabel("Credito disponible:"), gbc);
        
        gbc.gridx = 2;
        gbc.anchor = GridBagConstraints.EAST;
        top.add(creditBalanceLabel, gbc);
        
        main.add(top, BorderLayout.NORTH);

        JPanel center = new JPanel(new GridLayout(1, 2, 10, 0));
        
        JPanel salesPanel = new JPanel(new BorderLayout());
        salesPanel.setBorder(BorderFactory.createTitledBorder("Ventas Pendientes"));
        salesPanel.add(selectAllCheck, BorderLayout.NORTH);
        salesPanel.add(new JScrollPane(salesTable), BorderLayout.CENTER);
        salesPanel.add(totalSelectedLabel, BorderLayout.SOUTH);
        center.add(salesPanel);

        JPanel splitsPanel = new JPanel(new BorderLayout());
        splitsPanel.setBorder(BorderFactory.createTitledBorder("Metodos de Pago"));
        JPanel splitBtns = new JPanel(new FlowLayout(FlowLayout.LEFT));
        splitBtns.add(addSplitButton);
        splitBtns.add(removeSplitButton);
        splitsPanel.add(splitBtns, BorderLayout.NORTH);
        splitsPanel.add(new JScrollPane(splitsTable), BorderLayout.CENTER);
        
        JPanel splitsBottom = new JPanel(new GridLayout(3, 1));
        splitsBottom.add(totalSplitsLabel);
        splitsBottom.add(diffLabel);
        splitsBottom.add(useCreditCheck);
        splitsPanel.add(splitsBottom, BorderLayout.SOUTH);
        center.add(splitsPanel);
        
        main.add(center, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new GridBagLayout());
        GridBagConstraints gbc2 = new GridBagConstraints();
        gbc2.insets = new Insets(5, 5, 5, 5);
        gbc2.anchor = GridBagConstraints.WEST;
        gbc2.fill = GridBagConstraints.HORIZONTAL;

        int y = 0;
        gbc2.gridx = 0; gbc2.gridy = y;
        bottom.add(new JLabel("Fecha:"), gbc2);
        gbc2.gridx = 1; gbc2.gridwidth = 2;
        JFormattedTextField dateField = new JFormattedTextField();
        datePicker.setEditor(dateField);
        bottom.add(dateField, gbc2);
        gbc2.gridwidth = 1;

        y++;
        gbc2.gridx = 0; gbc2.gridy = y;
        bottom.add(new JLabel("Notas:"), gbc2);
        gbc2.gridx = 1; gbc2.gridwidth = 2;
        bottom.add(descriptionField, gbc2);
        gbc2.gridwidth = 1;

        y++;
        gbc2.gridx = 0; gbc2.gridy = y;
        gbc2.gridwidth = 3;
        gbc2.fill = GridBagConstraints.NONE;
        gbc2.anchor = GridBagConstraints.CENTER;
        JPanel btnPanel = new JPanel();
        btnPanel.add(confirmButton);
        btnPanel.add(cancelButton);
        bottom.add(btnPanel, gbc2);

        main.add(bottom, BorderLayout.SOUTH);

        add(main);
    }

    private void attachListeners() {
        selectAllCheck.addActionListener(e -> {
            boolean sel = selectAllCheck.isSelected();
            salesTableModel.selectAll(sel);
            updateTotals();
        });

        salesTableModel.addTableModelListener(e -> updateTotals());
        splitsTableModel.addTableModelListener(e -> updateTotals());
        useCreditCheck.addActionListener(e -> updateTotals());

        addSplitButton.addActionListener(e -> {
            splitsTableModel.addRow();
            splitsTable.setRowSelectionInterval(splitsTableModel.getRowCount() - 1, splitsTableModel.getRowCount() - 1);
        });

        removeSplitButton.addActionListener(e -> {
            int row = splitsTable.getSelectedRow();
            if (row >= 0) {
                splitsTableModel.removeRow(row);
            } else {
                JOptionPane.showMessageDialog(this, "Selecciona una fila para quitar");
            }
        });

        confirmButton.addActionListener(e -> confirmPayment());
        cancelButton.addActionListener(e -> dispose());
    }

    private void loadData() {
        new SwingWorker<PendingSalesResponse, Void>() {
            protected PendingSalesResponse doInBackground() throws Exception {
                return controller.getPendingSales(customer.getId());
            }
            protected void done() {
                try {
                    pendingData = get();
                    if (pendingData != null && pendingData.getSales() != null) {
                        salesTableModel.setData(pendingData.getSales());
                        creditBalanceLabel.setText(format(pendingData.getCreditBalance()));
                        useCreditCheck.setEnabled(pendingData.getCreditBalance().compareTo(BigDecimal.ZERO) > 0);
                        if (pendingData.getCreditBalance().compareTo(BigDecimal.ZERO) > 0) {
                            useCreditCheck.setSelected(true);
                        }
                        splitsTableModel.addRow();
                        updateTotals();
                    }
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(MultiPaymentDialog.this, "Error cargando datos: " + e.getMessage());
                }
            }
        }.execute();
    }

    private void updateTotals() {
        BigDecimal totalSales = salesTableModel.getTotalSelected();
        BigDecimal totalSplits = splitsTableModel.getTotalAmount();

        BigDecimal creditToUse = BigDecimal.ZERO;
        if (useCreditCheck.isSelected() && pendingData != null) {
            BigDecimal falta = totalSales.subtract(totalSplits);
            if (falta.compareTo(BigDecimal.ZERO) > 0) {
                creditToUse = pendingData.getCreditBalance().min(falta);
            }
        }

        BigDecimal totalConCredito = totalSplits.add(creditToUse);
        BigDecimal diff = totalSales.subtract(totalConCredito);

        totalSelectedLabel.setText("Total ventas: " + format(totalSales));
        totalSplitsLabel.setText("Total pagos: " + format(totalSplits));
        
        if (creditToUse.compareTo(BigDecimal.ZERO) > 0) {
            diffLabel.setText("Credito a usar: " + format(creditToUse) + " | Resta: " + format(diff));
        } else {
            diffLabel.setText("Resta: " + format(diff));
        }

        if (diff.compareTo(BigDecimal.ZERO) > 0) {
            diffLabel.setForeground(Color.RED);
        } else if (diff.compareTo(BigDecimal.ZERO) == 0) {
            diffLabel.setForeground(new Color(40, 167, 69));
        } else {
            diffLabel.setForeground(Color.BLUE);
        }
    }

    private void confirmPayment() {
        List<Long> selectedIds = salesTableModel.getSelectedIds();

        if (selectedIds.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Selecciona al menos una venta", "Atencion", JOptionPane.WARNING_MESSAGE);
            return;
        }

        List<PaymentSplitRequest> splits = splitsTableModel.getValidSplits();
        if (!useCreditCheck.isSelected() && splits.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Agrega al menos un metodo de pago con monto mayor a 0", "Atencion", JOptionPane.WARNING_MESSAGE);
            return;
        }

        BigDecimal totalSales = salesTableModel.getTotalSelected();
        BigDecimal totalSplits = splitsTableModel.getTotalAmount();

        if (!useCreditCheck.isSelected() && totalSplits.compareTo(totalSales) < 0) {
            int opcion = JOptionPane.showConfirmDialog(this, 
                "El total de pagos (" + format(totalSplits) + ") es menor que la deuda (" + format(totalSales) + ").\nSe aplicara como pago parcial. ¿Continuar?",
                "Pago parcial", JOptionPane.YES_NO_OPTION);
            if (opcion != JOptionPane.YES_OPTION) return;
        }

        MultiPaymentRequest req = MultiPaymentRequest.builder()
                .customerId(customer.getId())
                .saleIds(selectedIds)
                .date(datePicker.getSelectedDate())
                .notes(descriptionField.getText())
                .useCredit(useCreditCheck.isSelected())
                .splits(splits)
                .build();

        confirmButton.setEnabled(false);
        new SwingWorker<MultiPaymentResponse, Void>() {
            protected MultiPaymentResponse doInBackground() throws Exception {
                return controller.createMultiPayment(req);
            }
            protected void done() {
                confirmButton.setEnabled(true);
                try {
                    result = get();
                    showResult(result);
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(MultiPaymentDialog.this, "Error procesando pago: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    private void showResult(MultiPaymentResponse resp) {
        new PaymentResultDialog(this, resp).setVisible(true);
    }

    public MultiPaymentResponse showDialog() {
        setVisible(true);
        return result;
    }

    private String format(BigDecimal v) {
        if (v == null) return "$0,00";
        return "$" + currencyFormat.format(v.setScale(2, BigDecimal.ROUND_HALF_UP));
    }

    class SalesTableModel extends AbstractTableModel {
        private final String[] cols = {"Sel", "Nro", "Fecha", "Total", "Pendiente"};
        private List<SalePendingInfo> sales = new ArrayList<>();
        private boolean[] selected = new boolean[0];

        public void setData(List<SalePendingInfo> data) {
            this.sales = data;
            this.selected = new boolean[data.size()];
            fireTableDataChanged();
        }

        public void selectAll(boolean value) {
            for (int i = 0; i < selected.length; i++) selected[i] = value;
            fireTableDataChanged();
        }

        public List<Long> getSelectedIds() {
            List<Long> ids = new ArrayList<>();
            for (int i = 0; i < sales.size(); i++) {
                if (selected[i]) ids.add(sales.get(i).getSaleId());
            }
            return ids;
        }

        public int getSelectedCount() {
            int count = 0;
            for (boolean b : selected) if (b) count++;
            return count;
        }

        public BigDecimal getTotalSelected() {
            BigDecimal total = BigDecimal.ZERO;
            for (int i = 0; i < sales.size(); i++) {
                if (selected[i]) {
                    total = total.add(sales.get(i).getRemainingDue());
                }
            }
            return total;
        }

        @Override public int getRowCount() { return sales.size(); }
        @Override public int getColumnCount() { return cols.length; }
        @Override public String getColumnName(int c) { return cols[c]; }

        @Override public Object getValueAt(int r, int c) {
            SalePendingInfo s = sales.get(r);
            return switch (c) {
                case 0 -> selected[r];
                case 1 -> s.getSaleNumber() != null ? s.getSaleNumber() : "Particular";
                case 2 -> s.getDate();
                case 3 -> format(s.getTotal());
                //case 4 -> format(s.getTotalPaid());
                case 4 -> format(s.getRemainingDue());
                default -> null;
            };
        }

        @Override public Class<?> getColumnClass(int c) { return c == 0 ? Boolean.class : Object.class; }
        @Override public boolean isCellEditable(int r, int c) { return c == 0; }

        @Override public void setValueAt(Object v, int r, int c) {
            if (c == 0) selected[r] = (Boolean) v;
            fireTableDataChanged();
        }
    }

    class SplitsTableModel extends AbstractTableModel {
        private final String[] cols = {"Metodo", "Monto", "Notas"};
        private List<PaymentSplitRequest> splits = new ArrayList<>();

        public void addRow() {
            splits.add(PaymentSplitRequest.builder()
                    .paymentMethod("Efectivo")
                    .amount(BigDecimal.ZERO)
                    .reference("")
                    .build());
            fireTableDataChanged();
        }

        public void removeRow(int r) {
            if (r >= 0 && r < splits.size()) {
                splits.remove(r);
                fireTableDataChanged();
            }
        }

        public List<PaymentSplitRequest> getValidSplits() {
            List<PaymentSplitRequest> valid = new ArrayList<>();
            for (PaymentSplitRequest s : splits) {
                if (s.getAmount() != null && s.getAmount().compareTo(BigDecimal.ZERO) > 0) {
                    valid.add(s);
                }
            }
            return valid;
        }

        public BigDecimal getTotalAmount() {
            return splits.stream()
                    .map(PaymentSplitRequest::getAmount)
                    .filter(a -> a != null)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
        }

        @Override public int getRowCount() { return splits.size(); }
        @Override public int getColumnCount() { return cols.length; }
        @Override public String getColumnName(int c) { return cols[c]; }

        @Override public Object getValueAt(int r, int c) {
            PaymentSplitRequest s = splits.get(r);
            return switch (c) {
                case 0 -> s.getPaymentMethod();
                case 1 -> s.getAmount();
                case 2 -> s.getReference() != null ? s.getReference() : "";
                default -> null;
            };
        }

        @Override public Class<?> getColumnClass(int c) {
            return switch (c) {
                case 1 -> BigDecimal.class;
                default -> String.class;
            };
        }

        @Override public boolean isCellEditable(int r, int c) { return true; }

        @Override public void setValueAt(Object v, int r, int c) {
            PaymentSplitRequest s = splits.get(r);
            switch (c) {
                case 0 -> s.setPaymentMethod((String) v);
                case 1 -> s.setAmount((BigDecimal) v);
                case 2 -> s.setReference((String) v);
            }
            fireTableCellUpdated(r, c);
        }
    }

    class BigDecimalFieldRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
            if (value instanceof BigDecimal) {
                setText(format((BigDecimal) value));
            } else {
                setText(value != null ? value.toString() : "");
            }
            setHorizontalAlignment(JTextField.RIGHT);
            if (isSelected) {
                setBackground(table.getSelectionBackground());
                setForeground(table.getSelectionForeground());
            } else {
                setBackground(table.getBackground());
                setForeground(table.getForeground());
            }
            return this;
        }
    }

    class BigDecimalCellEditor extends AbstractCellEditor implements javax.swing.table.TableCellEditor {
        private final BigDecimalField field = new BigDecimalField(BigDecimal.ZERO);

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int col) {
            if (value instanceof BigDecimal) {
                field.setBigDecimal((BigDecimal) value);
            }
            return field;
        }

        @Override
        public Object getCellEditorValue() {
            return field.getBigDecimal();
        }
    }

    class PaymentResultDialog extends JDialog {

        PaymentResultDialog(JDialog parent, MultiPaymentResponse resp) {
            super(parent, "Pago Exitoso", true);

            JTextArea textArea = new JTextArea();
            textArea.setEditable(false);
            textArea.setFont(new java.awt.Font(java.awt.Font.MONOSPACED, java.awt.Font.PLAIN, 13));
            textArea.setText(buildResultText(resp));
            textArea.setCaretPosition(0);
            textArea.setMargin(new Insets(10, 10, 10, 10));

            JButton okButton = new JButton("Aceptar");
            okButton.addActionListener(e -> {
                parent.dispose();
                dispose();
            });

            JPanel main = new JPanel(new BorderLayout(10, 10));
            main.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
            main.add(new JScrollPane(textArea), BorderLayout.CENTER);

            JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
            btnPanel.add(okButton);
            main.add(btnPanel, BorderLayout.SOUTH);

            setContentPane(main);
            setSize(700, 550);
            setLocationRelativeTo(parent);
        }

        private String buildResultText(MultiPaymentResponse resp) {
            StringBuilder sb = new StringBuilder();
            sb.append("Pago #").append(resp.getPaymentId())
              .append(" - ").append(format(resp.getTotalAmount())).append("\n\n");
            sb.append("Detalle por metodo:\n");
            for (PaymentSplitResponse split : resp.getSplits()) {
                sb.append("  ").append(split.getPaymentMethod())
                  .append(": ").append(format(split.getAmount()));
                if (split.getReference() != null && !split.getReference().isEmpty()) {
                    sb.append(" (Ref: ").append(split.getReference()).append(")");
                }
                if (split.getSalesCovered() != null && !split.getSalesCovered().isEmpty()) {
                    sb.append(" -> Ventas: ").append(String.join(", ", split.getSalesCovered()));
                }
                sb.append("\n");
            }
            if (resp.getCreditUsed().compareTo(BigDecimal.ZERO) > 0) {
                sb.append("\nCredito usado: ").append(format(resp.getCreditUsed())).append("\n");
            }
            if (resp.getCreditGenerated().compareTo(BigDecimal.ZERO) > 0) {
                sb.append("\nCredito generado: ").append(format(resp.getCreditGenerated())).append("\n");
            }
            sb.append("\nNuevo saldo credito: ").append(format(resp.getCustomerCreditBalance()));
            sb.append("\n\n").append(resp.getSummary());
            return sb.toString();
        }
    }
}
