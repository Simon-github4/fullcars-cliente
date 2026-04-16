package views.transactions;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
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
import javax.swing.SwingWorker;
import javax.swing.table.AbstractTableModel;

import controller.PayController;
import data.service.ClienteRestPayments;
import dtos.MultiPaymentRequest;
import dtos.MultiPaymentResponse;
import dtos.PendingSalesResponse;
import dtos.PendingSalesResponse.SalePendingInfo;
import model.client.entities.Customer;
import raven.datetime.DatePicker;
import raven.datetime.DatePicker.DateSelectionMode;
import views.components.BigDecimalField;

public class MultiPaymentDialog extends JDialog {

    private Customer customer;
    private PayController paymentsController;

    private JTable table;
    private SalesTableModel tableModel;

    private JLabel creditBalanceLabel;
    private JCheckBox useCreditCheck;
    private JCheckBox selectAllCheck;
    private JLabel totalSelectedLabel;
    private JTextArea summaryArea;

    private BigDecimalField amountField;
    private JComboBox<String> payMethodCombo;
    private DatePicker datePicker;

    private JButton confirmButton;
    private JButton cancelButton;

    private PendingSalesResponse pendingData;
    private MultiPaymentResponse result;

    public MultiPaymentDialog(Frame owner, Customer customer) {
        super(owner, "Pago Múltiple", true);
        this.customer = customer;
        this.paymentsController = new PayController();

        initComponents();
        layoutComponents();
        attachListeners();
        loadData();

        setSize(800, 600);
        setLocationRelativeTo(owner);
    }

    private void initComponents() {
        creditBalanceLabel = new JLabel("$0,00", JLabel.LEFT);

        useCreditCheck = new JCheckBox("Usar Credito a favor");
        selectAllCheck = new JCheckBox("Seleccionar todas");

        totalSelectedLabel = new JLabel("Total seleccionado: $0,00");

        summaryArea = new JTextArea(5, 30);
        summaryArea.setEditable(false);
        summaryArea.setBorder(BorderFactory.createLineBorder(Color.GRAY));

        amountField = new BigDecimalField(BigDecimal.ZERO);

        payMethodCombo = new JComboBox<>(new String[]{
        		"Efectivo", "Tarjeta", "Transferencia", "Cheque"
        });

        datePicker = new DatePicker();
        datePicker.setDateSelectionMode(DateSelectionMode.SINGLE_DATE_SELECTED);
        datePicker.setSelectedDate(LocalDate.now());

        confirmButton = new JButton("Confirmar Pago");
        cancelButton = new JButton("Cancelar");

        tableModel = new SalesTableModel();
        table = new JTable(tableModel);
        table.setRowHeight(25);
    }

    private void layoutComponents() {
        JPanel main = new JPanel(new BorderLayout(10, 10));
        main.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // TOP
        JPanel top = new JPanel(new GridLayout(2, 2, 10, 5));
        top.add(new JLabel("Cliente: " + customer.getFullName()));
        top.add(new JLabel());
        top.add(new JLabel("Credito a favor:"));
        top.add(creditBalanceLabel);

        // CENTER
        JPanel center = new JPanel(new BorderLayout(5, 5));
        center.add(selectAllCheck, BorderLayout.NORTH);
        center.add(new JScrollPane(table), BorderLayout.CENTER);
        center.add(totalSelectedLabel, BorderLayout.SOUTH);

        // BOTTOM
        JPanel bottom = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        int y = 0;

        gbc.gridx = 0; gbc.gridy = y;
        bottom.add(useCreditCheck, gbc);

        y++;
        gbc.gridx = 0; gbc.gridy = y;
        bottom.add(new JLabel("Monto:"), gbc);
        gbc.gridx = 1;
        bottom.add(amountField, gbc);

        y++;
        gbc.gridx = 0; gbc.gridy = y;
        bottom.add(new JLabel("Fecha:"), gbc);
        gbc.gridx = 1;
        JFormattedTextField tf = new JFormattedTextField();
        datePicker.setEditor(tf);
        bottom.add(tf, gbc);

        y++;
        gbc.gridx = 0; gbc.gridy = y;
        bottom.add(new JLabel("Método:"), gbc);
        gbc.gridx = 1;
        bottom.add(payMethodCombo, gbc);

        y++;
        gbc.gridx = 0; gbc.gridy = y;
        gbc.gridwidth = 2;
        bottom.add(new JLabel("Resumen:"), gbc);

        y++;
        gbc.gridx = 0; gbc.gridy = y;
        bottom.add(new JScrollPane(summaryArea), gbc);

        y++;
        gbc.gridx = 0; gbc.gridy = y;
        JPanel buttons = new JPanel();
        buttons.add(confirmButton);
        buttons.add(cancelButton);
        bottom.add(buttons, gbc);

        main.add(top, BorderLayout.NORTH);
        main.add(center, BorderLayout.CENTER);
        main.add(bottom, BorderLayout.SOUTH);

        setContentPane(main);
    }

    private void attachListeners() {
        selectAllCheck.addActionListener(e -> {
            boolean selected = selectAllCheck.isSelected();
            tableModel.selectAll(selected);
            updateSummary();
        });

        useCreditCheck.addActionListener(e -> updateSummary());
        
        amountField.addActionListener(e -> updateSummary());

        confirmButton.addActionListener(e -> onConfirm());
        cancelButton.addActionListener(e -> dispose());
    }

    private void loadData() {
        new SwingWorker<PendingSalesResponse, Void>() {
            protected PendingSalesResponse doInBackground() throws Exception {
                return paymentsController.getPendingSales(customer.getId());
            }

            protected void done() {
                try {
                    pendingData = get();
                    tableModel.setSales(pendingData.getSales());
                    creditBalanceLabel.setText(format(pendingData.getCreditBalance()));
                    useCreditCheck.setEnabled(
                            pendingData.getCreditBalance().compareTo(BigDecimal.ZERO) > 0
                    );
                    updateSummary();
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(MultiPaymentDialog.this,
                            "Error cargando datos",
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    private void updateSummary() {
        BigDecimal total = tableModel.getTotalSelected();
        BigDecimal userPayment = amountField.getBigDecimal();
        
        BigDecimal creditToUse = BigDecimal.ZERO;
        if (useCreditCheck.isSelected()) {
            BigDecimal falta = total.subtract(userPayment);
            if (falta.compareTo(BigDecimal.ZERO) > 0) {
                creditToUse = pendingData.getCreditBalance().min(falta);
            }
        }

        BigDecimal efectivoNecesario = total.subtract(creditToUse);
        BigDecimal faltaCubrir = efectivoNecesario.subtract(userPayment);

        totalSelectedLabel.setText("Total seleccionado: " + format(total));
        
        if (faltaCubrir.compareTo(BigDecimal.ZERO) > 0) {
            amountField.setBackground(new Color(255, 200, 200));
        } else {
            amountField.setBackground(Color.WHITE);
        }

        summaryArea.setText(
                "Ventas: " + tableModel.getSelectedCount() + "\n" +
                "Total: " + format(total) + "\n" +
                "Credito a usar: " + format(creditToUse) + "\n" +
                "Pago: " + format(efectivoNecesario) + "\n" +
                "Falta: " + format(faltaCubrir)
        );
    }

    private void onConfirm() {
        List<Long> ids = tableModel.getSelectedIds();

        if (ids.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Seleccioná al menos una venta");
            return;
        }

        MultiPaymentRequest req = MultiPaymentRequest.builder()
                .customerId(customer.getId())
                .paymentAmount(amountField.getBigDecimal())
                .paymentMethod((String) payMethodCombo.getSelectedItem())
                .date(datePicker.getSelectedDate())
                .saleIds(ids)
                .useCredit(useCreditCheck.isSelected())
                .build();

        new SwingWorker<MultiPaymentResponse, Void>() {
            protected MultiPaymentResponse doInBackground() throws Exception {
                return paymentsController.createMultiPayment(req);
            }

            protected void done() {
                try {
                    result = get();
                    JOptionPane.showMessageDialog(MultiPaymentDialog.this,
                            result.getSummary());
                    dispose();
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(MultiPaymentDialog.this,
                            "Error procesando pago:"+e.getMessage());
                }
            }
        }.execute();
    }

    public MultiPaymentResponse showDialog() {
        setVisible(true);
        return result;
    }

    private String format(BigDecimal v) {
        if (v == null) return "$0,00";
        return "$" + v.setScale(2, BigDecimal.ROUND_HALF_UP).toString().replace(".", ",");
    }

    // =========================
    // TABLE MODEL
    // =========================
    private class SalesTableModel extends AbstractTableModel {

        private final String[] cols = {
                "Sel", "ID", "Fecha", "Total", "Pagado", "Pendiente", "Nro siniestro"
        };

        private List<SalePendingInfo> sales = new ArrayList<>();
        private List<Boolean> selected = new ArrayList<>();

        public void setSales(List<SalePendingInfo> data) {
            sales = data;
            selected = new ArrayList<>();
            for (int i = 0; i < sales.size(); i++) selected.add(false);
            fireTableDataChanged();
        }

        public void selectAll(boolean value) {
            for (int i = 0; i < selected.size(); i++) selected.set(i, value);
            fireTableDataChanged();
        }

        public BigDecimal getTotalSelected() {
            BigDecimal total = BigDecimal.ZERO;
            for (int i = 0; i < sales.size(); i++) {
                if (selected.get(i)) {
                    total = total.add(sales.get(i).getRemainingDue());
                }
            }
            return total;
        }

        public int getSelectedCount() {
            return (int) selected.stream().filter(b -> b).count();
        }

        public List<Long> getSelectedIds() {
            List<Long> ids = new ArrayList<>();
            for (int i = 0; i < sales.size(); i++) {
                if (selected.get(i)) {
                    ids.add(sales.get(i).getSaleId());
                }
            }
            return ids;
        }

        @Override
        public int getRowCount() { return sales.size(); }

        @Override
        public int getColumnCount() { return cols.length; }

        @Override
        public String getColumnName(int col) { return cols[col]; }

        @Override
        public Object getValueAt(int row, int col) {
            SalePendingInfo s = sales.get(row);
            switch (col) {
                case 0: return selected.get(row);
                case 1: return s.getSaleId();
                case 2: return s.getDate();
                case 3: return format(s.getTotal());
                case 4: return format(s.getTotalPaid());
                case 5: return format(s.getRemainingDue());
                case 6: return s.getSaleNumber() != null ? s.getSaleNumber() : "Particular";
            }
            return null;
        }

        @Override
        public boolean isCellEditable(int row, int col) {
            return col == 0;
        }

        @Override
        public void setValueAt(Object val, int row, int col) {
            if (col == 0) {
                selected.set(row, (Boolean) val);
                updateSummary();
            }
        }

        @Override
        public Class<?> getColumnClass(int col) {
            return col == 0 ? Boolean.class : String.class;
        }
    }
}