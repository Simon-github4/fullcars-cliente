package views.transactions;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.nio.file.Path;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingWorker;
import javax.swing.UIManager;

import Utils.ServerException;
import data.service.ClienteRestFactura;
import enums.TiposComprobante;
import model.client.entities.CreditNote;

public class DialogNotaCreditoShowData extends JDialog {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final NumberFormat CURRENCY_FORMAT = NumberFormat.getCurrencyInstance(new Locale("es", "AR"));

    private CreditNote creditNote;
    private ClienteRestFactura clienteRestFactura;
    private Long saleId;

    public DialogNotaCreditoShowData(Frame parent, CreditNote factura, ClienteRestFactura clienteRestFactura, Long saleId) {
        super(parent, "Detalle de Nota de Credito", true);
        this.creditNote = factura;
        this.clienteRestFactura = clienteRestFactura;
        this.saleId = saleId;
        initComponents();
        setVisible(true);
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        setSize(600, 700);
        setLocationRelativeTo(getParent());

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JLabel titleLabel = new JLabel(getTipoComprobanteStr());
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(titleLabel);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 20)));

        String motivo = creditNote.getMotivo();
        if (motivo == null || motivo.isEmpty()) {
            motivo = creditNote.getObservaciones();
        }

        mainPanel.add(createSectionPanel("Datos del Comprobante",
            "Número:", formatNumeroComprobante(),
            "Fecha Emisión:", formatDate(creditNote.getFechaEmision()),
            "CAE:", creditNote.getCae() != null ? creditNote.getCae() : "N/A",
            "Vto. CAE:", formatDate(creditNote.getVtoCae()),
            "Comprobante Asociado:", creditNote.getComprobanteAsociadoToPDF() != null ?
            		creditNote.getComprobanteAsociadoToPDF() : "N/A",
            "Motivo:", motivo != null ? motivo : "N/A"
        ));

        mainPanel.add(Box.createRigidArea(new Dimension(0, 15)));

        mainPanel.add(createSectionPanel("Datos del Cliente",
            "Razon Social:", creditNote.getRazonSocialCliente(),
            "CUIT:", formatCuit(creditNote.getCuitCliente()),
            "Domicilio:", creditNote.getDomicilioCliente(),
            "Condicion IVA:", creditNote.getCondicionIvaCliente() != null ?
                creditNote.getCondicionIvaCliente().toString() : "N/A"
        ));

        mainPanel.add(Box.createRigidArea(new Dimension(0, 15)));

        mainPanel.add(createSectionPanel("Importes",
            "Neto Gravado:", formatCurrency(creditNote.getImpNeto()),
            "IVA:", formatCurrency(creditNote.getImpIva()),
            "Tributos:", formatCurrency(creditNote.getImpTributos()),
            "TOTAL:", formatCurrency(creditNote.getImpTotal())
        ));

        mainPanel.add(Box.createRigidArea(new Dimension(0, 15)));

        if (creditNote.getObservaciones() != null && !creditNote.getObservaciones().isEmpty()) {
            JPanel obsPanel = new JPanel(new BorderLayout(5, 5));
            obsPanel.setBorder(BorderFactory.createTitledBorder("Observaciones"));
            JTextArea obsArea = new JTextArea(creditNote.getObservaciones());
            obsArea.setEditable(false);
            obsArea.setWrapStyleWord(true);
            obsArea.setLineWrap(true);
            obsArea.setRows(3);
            obsPanel.add(new JScrollPane(obsArea), BorderLayout.CENTER);
            mainPanel.add(obsPanel);
        }

        JScrollPane scrollPane = new JScrollPane(mainPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        add(scrollPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));

        JButton downloadButton = new JButton("Ver Nota de Credito");
        downloadButton.setIcon(UIManager.getIcon("FileView.fileIcon"));
        downloadButton.addActionListener(e -> descargarNotaCredito());

        JButton closeButton = new JButton("Cerrar");
        closeButton.addActionListener(e -> dispose());

        buttonPanel.add(downloadButton);
        buttonPanel.add(closeButton);

        add(buttonPanel, BorderLayout.SOUTH);
    }

    private JPanel createSectionPanel(String title, String... labelValuePairs) {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder(title));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 10, 5, 10);
        gbc.anchor = GridBagConstraints.WEST;

        for (int i = 0; i < labelValuePairs.length; i += 2) {
            gbc.gridx = 0;
            gbc.gridy = i / 2;
            gbc.weightx = 0.3;
            JLabel label = new JLabel(labelValuePairs[i]);
            label.setFont(new Font("Arial", Font.BOLD, 12));
            panel.add(label, gbc);

            gbc.gridx = 1;
            gbc.weightx = 0.7;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            JLabel value = new JLabel(labelValuePairs[i + 1]);
            value.setFont(new Font("Arial", Font.PLAIN, 12));
            panel.add(value, gbc);
        }

        return panel;
    }

    private String getTipoComprobanteStr() {
    	return TiposComprobante.fromCodigo(creditNote.getTipoComprobante()).toString();
    }
    
    private String formatNumeroComprobante() {
        if (creditNote.getPuntoVenta() == null || creditNote.getNumeroComprobante() == null) {
            return "N/A";
        }
        return String.format("%04d-%08d", creditNote.getPuntoVenta(), creditNote.getNumeroComprobante());
    }

    private String formatCuit(Long cuit) {
        if (cuit == null) return "N/A";
        String cuitStr = String.valueOf(cuit);
        if (cuitStr.length() == 11) {
            return cuitStr.substring(0, 2) + "-" + cuitStr.substring(2, 10) + "-" + cuitStr.substring(10);
        }
        return cuitStr;
    }

    private String formatDate(java.time.LocalDate date) {
        return date != null ? date.format(DATE_FORMATTER) : "N/A";
    }

    private String formatCurrency(java.math.BigDecimal amount) {
        return amount != null ? CURRENCY_FORMAT.format(amount) : "$0,00";
    }

    private void descargarNotaCredito() {
        SwingWorker<Path, Void> worker = new SwingWorker<Path, Void>() {
            @Override
            protected Path doInBackground() throws Exception {
                return clienteRestFactura.getAndOpenNotaCreditoPdf(saleId);
            }

            @Override
            protected void done() {
                try {
                    Path tempFile = get();
                    Desktop.getDesktop().open(tempFile.toFile());
                } catch (Exception ex) {
                    String mensaje = ex.getCause() instanceof ServerException
                        ? ex.getCause().getMessage()
                        : "Error al abrir el archivo: " + ex.getMessage();

                    JOptionPane.showMessageDialog(DialogNotaCreditoShowData.this,
                        mensaje,
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                    ex.printStackTrace();
                }
            }
        };

        worker.execute();
    }
}
