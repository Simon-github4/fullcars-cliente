package views.transactions;

import model.client.entities.Factura;
import data.service.ClienteRestFactura;
import Utils.ServerException;
import javax.swing.*;
import java.awt.*;
import java.awt.Desktop;
import java.nio.file.Path;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class DialogFacturaShowData extends JDialog {
    
    private ClienteRestFactura clienteRestFactura;
    
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final NumberFormat CURRENCY_FORMAT = NumberFormat.getCurrencyInstance(new Locale("es", "AR"));
     
    private Factura factura;
    private Long saleId;
    
    public DialogFacturaShowData(Frame parent, ClienteRestFactura clienteRestFactura, Long saleId) throws Exception {
        super(parent, "Detalle de Factura", true);
        this.clienteRestFactura = clienteRestFactura;
        this.saleId = saleId;
        
        this.factura = this.clienteRestFactura.getFacturaBySaleId(this.saleId);
        if(factura == null)
			throw new Exception("Factura no Encontrada");
		
        initComponents();
        setVisible(true);
    }
    
    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        setSize(500, 600);
        setLocationRelativeTo(getParent());
        
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        JLabel titleLabel = new JLabel("FACTURA " + getTipoComprobanteStr());
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(titleLabel);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        
        mainPanel.add(createSectionPanel("Datos del Comprobante",
            "Número:", formatNumeroComprobante(),
            "Fecha Emisión:", formatDate(factura.getFechaEmision()),
            "CAE:", factura.getCae() != null ? factura.getCae() : "N/A",
            "Vto. CAE:", formatDate(factura.getVtoCae())
        ));
        
        mainPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        
        mainPanel.add(createSectionPanel("Datos del Cliente",
            "Razón Social:", factura.getRazonSocialCliente(),
            "CUIT:", formatCuit(factura.getCuitCliente()),
            "Domicilio:", factura.getDomicilioCliente(),
            "Condición IVA:", factura.getCondicionIvaCliente() != null ? 
                factura.getCondicionIvaCliente().toString() : "N/A"
        ));
        
        mainPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        
        mainPanel.add(createSectionPanel("Importes",
            "Neto Gravado:", formatCurrency(factura.getImpNeto()),
            "IVA:", formatCurrency(factura.getImpIva()),
            "Tributos:", formatCurrency(factura.getImpTributos()),
            "TOTAL:", formatCurrency(factura.getImpTotal())
        ));
        
        mainPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        
        if (factura.getFechaVencimientoPago() != null) {
            mainPanel.add(createSectionPanel("Información Adicional",
                "Vencimiento Pago:", formatDate(factura.getFechaVencimientoPago())
            ));
            mainPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        }
        
        if (factura.getObservaciones() != null && !factura.getObservaciones().isEmpty()) {
            JPanel obsPanel = new JPanel(new BorderLayout(5, 5));
            obsPanel.setBorder(BorderFactory.createTitledBorder("Observaciones"));
            JTextArea obsArea = new JTextArea(factura.getObservaciones());
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
        
        JButton downloadButton = new JButton("Ver Factura");
        downloadButton.setIcon(UIManager.getIcon("FileView.fileIcon"));
        downloadButton.addActionListener(e -> descargarFactura());
        
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
        if (factura.getTipoComprobante() == null) return "";
        
        switch (factura.getTipoComprobante()) {
            case 1: return "A";
            case 6: return "B";
            case 11: return "C";
            default: return String.valueOf(factura.getTipoComprobante());
        }
    }
    
    private String formatNumeroComprobante() {
        if (factura.getPuntoVenta() == null || factura.getNumeroComprobante() == null) {
            return "N/A";
        }
        return String.format("%04d-%08d", factura.getPuntoVenta(), factura.getNumeroComprobante());
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
    
    private void descargarFactura() {
        SwingWorker<Path, Void> worker = new SwingWorker<Path, Void>() {
            @Override
            protected Path doInBackground() throws Exception {
                return descargarDesdeAPI();
            }
            
            @Override
            protected void done() {
                try {
                    Path tempFile = get(); // Obtener el archivo temporal
                    
                    Desktop.getDesktop().open(tempFile.toFile());
                    
                } catch (Exception ex) {
                    String mensaje = ex.getCause() instanceof ServerException 
                        ? ex.getCause().getMessage() 
                        : "Error al abrir el archivo: " + ex.getMessage();
                    
                    JOptionPane.showMessageDialog(DialogFacturaShowData.this,
                        mensaje,
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                    ex.printStackTrace();
                }
            }
        };
        
        worker.execute();
    }
    
    private Path descargarDesdeAPI() throws Exception {
        try {
            // Llamar al servicio para obtener el PDF temporal
            return clienteRestFactura.getAndOpenFacturaPdf(saleId);
            
        } catch (ServerException e) {
            throw new Exception("Error del servidor al descargar la factura", e);
        }
    }
    
}