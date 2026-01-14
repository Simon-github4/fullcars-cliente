package views.transactions;

import javax.swing.*;
import java.awt.*;

public class DialogFactura extends JDialog {
	private static final long serialVersionUID = 1L;
	private Integer tipoFacturaSeleccionado = null;//1 = A, 6 = B
    private JRadioButton rbFacturaA;
    private JRadioButton rbFacturaB;
    
    public DialogFactura(Frame parent) {
        super(parent, "Seleccionar Tipo de Factura", true);
        initComponents();
    }
    
    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        
        JPanel panelOpciones = new JPanel();
        panelOpciones.setLayout(new BoxLayout(panelOpciones, BoxLayout.Y_AXIS));
        panelOpciones.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        JLabel lblTitulo = new JLabel("Seleccione el tipo de factura:");
        lblTitulo.setFont(new Font("Arial", Font.BOLD, 14));
        lblTitulo.setAlignmentX(Component.LEFT_ALIGNMENT);
        panelOpciones.add(lblTitulo);
        panelOpciones.add(Box.createVerticalStrut(15));
        
        rbFacturaA = new JRadioButton("Factura tipo A");
        rbFacturaB = new JRadioButton("Factura tipo B");
        
        rbFacturaA.setAlignmentX(Component.LEFT_ALIGNMENT);
        rbFacturaB.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        ButtonGroup grupo = new ButtonGroup();
        grupo.add(rbFacturaA);
        grupo.add(rbFacturaB);
        
        panelOpciones.add(rbFacturaA);
        panelOpciones.add(Box.createVerticalStrut(10));
        panelOpciones.add(rbFacturaB);
        
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
        setMinimumSize(new Dimension(350, 180));
        setLocationRelativeTo(getParent());
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        
        rbFacturaA.requestFocus();
    }
    
    private void confirmar() {
        if (rbFacturaA.isSelected()) {
            tipoFacturaSeleccionado = 1;
        } else if (rbFacturaB.isSelected()) {
            tipoFacturaSeleccionado = 6;
        } else {
            JOptionPane.showMessageDialog(this, 
                "Por favor, seleccione un tipo de factura", 
                "Advertencia", 
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        dispose();
    }
    
    private void cancelar() {
        tipoFacturaSeleccionado = null;
        dispose();
    }
    
    public Integer getTipoFacturaSeleccionado() {
        return tipoFacturaSeleccionado;
    }
    
    // Método main para probar el diálogo
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            DialogFactura dialogo = new DialogFactura(null);
            dialogo.setVisible(true);
            
            Integer resultado = dialogo.getTipoFacturaSeleccionado();
            if (resultado != null) {
                System.out.println("Tipo de factura seleccionado: " + resultado);
            } else {
                System.out.println("Operación cancelada");
            }
        });
    }
}