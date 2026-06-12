package views.transactions;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FlowLayout;
import java.awt.Frame;

public class DialogNotaCredito extends JDialog {
    private static final long serialVersionUID = 1L;
    private BigDecimal monto = null;
    private JTextField montoField;
    private BigDecimal totalFactura;

    private static final NumberFormat CURRENCY_FORMAT = NumberFormat.getCurrencyInstance(new Locale("es", "AR"));

    public DialogNotaCredito(Frame parent, BigDecimal totalFactura) {
        super(parent, "Nota de Crédito", true);
        this.totalFactura = totalFactura;
        initComponents();
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));

        JPanel panelOpciones = new JPanel();
        panelOpciones.setLayout(new BoxLayout(panelOpciones, BoxLayout.Y_AXIS));
        panelOpciones.setBorder(BorderFactory.createEmptyBorder(20, 20, 10, 20));

        JLabel lblTitulo = new JLabel("Emitir Nota de Crédito");
        lblTitulo.setFont(new Font("Arial", Font.BOLD, 14));
        lblTitulo.setAlignmentX(Component.LEFT_ALIGNMENT);
        panelOpciones.add(lblTitulo);
        panelOpciones.add(Box.createVerticalStrut(15));

        JLabel lblTotal = new JLabel("Total facturado: " + CURRENCY_FORMAT.format(totalFactura));
        lblTotal.setFont(new Font("Arial", Font.PLAIN, 13));
        lblTotal.setAlignmentX(Component.LEFT_ALIGNMENT);
        panelOpciones.add(lblTotal);
        panelOpciones.add(Box.createVerticalStrut(10));

        JLabel lblMonto = new JLabel("Monto de la nota de crédito:");
        lblMonto.setFont(new Font("Arial", Font.PLAIN, 12));
        lblMonto.setAlignmentX(Component.LEFT_ALIGNMENT);
        panelOpciones.add(lblMonto);

        montoField = new JTextField(totalFactura.toPlainString(), 15);
        montoField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        montoField.setAlignmentX(Component.LEFT_ALIGNMENT);
        panelOpciones.add(montoField);

        add(panelOpciones, BorderLayout.CENTER);

        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        panelBotones.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));

        JButton btnConfirmar = new JButton("Confirmar");
        JButton btnCancelar = new JButton("Cancelar");

        btnConfirmar.addActionListener(e -> confirmar());
        btnCancelar.addActionListener(e -> cancelar());

        panelBotones.add(btnConfirmar);
        panelBotones.add(btnCancelar);

        add(panelBotones, BorderLayout.SOUTH);

        pack();
        setMinimumSize(new Dimension(350, 200));
        setLocationRelativeTo(getParent());
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        montoField.requestFocus();
    }

    private void confirmar() {
        try {
            BigDecimal montoIngresado = new BigDecimal(montoField.getText().trim());
            if (montoIngresado.compareTo(BigDecimal.ZERO) <= 0) {
                JOptionPane.showMessageDialog(this,
                        "El monto debe ser mayor a 0.",
                        "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (montoIngresado.compareTo(totalFactura) > 0) {
                JOptionPane.showMessageDialog(this,
                        "El monto no puede exceder el total facturado de " + CURRENCY_FORMAT.format(totalFactura),
                        "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            this.monto = montoIngresado;
            dispose();
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this,
                    "Ingrese un monto válido.",
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void cancelar() {
        monto = null;
        dispose();
    }

    public BigDecimal getMonto() {
        return monto;
    }
}
