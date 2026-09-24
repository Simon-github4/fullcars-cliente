package views.components;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import Utils.NumberFormatArg;
import dtos.MultiPaymentResponse;

public class PaymentDetailDialog {

    private PaymentDetailDialog() {}

    public static void show(MultiPaymentResponse payment) {
        JDialog dialog = new JDialog((java.awt.Frame) null, "Detalle Pago #" + payment.getPaymentId(), true);
        dialog.setSize(650, 500);
        dialog.setLocationRelativeTo(null);

        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JPanel infoPanel = new JPanel(new GridLayout(0, 2, 5, 5));
        infoPanel.add(new JLabel("Pago #:")); infoPanel.add(new JLabel(String.valueOf(payment.getPaymentId())));
        if (payment.getCustomerName() != null) {
            infoPanel.add(new JLabel("Cliente:")); infoPanel.add(new JLabel(payment.getCustomerName()));
        }
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
}
