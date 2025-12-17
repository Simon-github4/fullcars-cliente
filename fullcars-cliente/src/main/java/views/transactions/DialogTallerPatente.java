package views.transactions;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

import model.client.entities.Detail;
import model.client.entities.SaleDetail;

public class DialogTallerPatente extends JDialog {

    private JTextField tallerField;
    private JTextField patenteField;
    private JButton aceptarButton;

    private JTable detallesTable;
    private DefaultTableModel tableModel;

    private String taller;
    private String patente;
    private final List<SaleDetail> detalles;
    private final List<String> calidades; // se llenará con los valores ingresados

    public DialogTallerPatente(Frame owner, List<SaleDetail> detalles) {
        super(owner, "Completar REMITO", true);
        this.detalles = detalles;
        this.calidades = new ArrayList<>();
        initComponents();
        layoutComponents();
        attachListeners();

        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                // bloquea cierre por X o ALT+F4
            }
        });

        setMinimumSize(new Dimension(700, 420));
        pack();
        setLocationRelativeTo(owner);
    }

    private void initComponents() {
        tallerField = new JTextField(20);
        patenteField = new JTextField(20);

        aceptarButton = new JButton("Aceptar");
        aceptarButton.setPreferredSize(new Dimension(150, 30));

        // Configurar tabla
        String[] columnNames = {"Autoparte", "Cantidad", "Precio Unit.", "Subtotal", "Calidad"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                // Solo columna "Calidad" editable
                return column == 4;
            }
        };

        for (Detail d : detalles) {
            Object[] row = {
                d.getCarPart() != null ? d.getCarPart().toString() : "(sin nombre)",
                d.getQuantity(),
                d.getUnitPrice(),
                d.getSubTotal(),
                "" // calidad vacía
            };
            tableModel.addRow(row);
        }

        detallesTable = new JTable(tableModel);
        detallesTable.setRowHeight(25);
        detallesTable.setPreferredScrollableViewportSize(new Dimension(600, 200));
    }

    private void layoutComponents() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Taller:"), gbc);
        gbc.gridx = 1; panel.add(tallerField, gbc);

        gbc.gridx = 0; gbc.gridy++;
        panel.add(new JLabel("Patente:"), gbc);
        gbc.gridx = 1; panel.add(patenteField, gbc);

        // --- Texto de ayuda ---
        gbc.gridx = 0; gbc.gridy++;
        gbc.gridwidth = 2;
        JLabel ayudaLabel = new JLabel(
            "<html><i style='color:gray;'>Complete la calidad de cada autoparte en la columna “Calidad”.<br>" +
            "(haga DOBLE CLICK en la celda para editar).</i></html>"
        );
        panel.add(ayudaLabel, gbc);

        // --- Tabla ---
        gbc.gridx = 0; gbc.gridy++;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        JScrollPane scrollPane = new JScrollPane(detallesTable);
        panel.add(scrollPane, gbc);

        // --- Botón ---
        gbc.gridy++;
        gbc.weighty = 0;
        gbc.anchor = GridBagConstraints.CENTER;
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(aceptarButton);
        panel.add(buttonPanel, gbc);

        getContentPane().add(panel);
    }

    private void attachListeners() {
        aceptarButton.addActionListener(e -> onAceptar());
    }

    private void onAceptar() {
        String t = tallerField.getText().trim();
        String p = patenteField.getText().trim();

        /*if (t.isEmpty() || p.isEmpty()) {
            JOptionPane.showMessageDialog(
                this,
                "Debe ingresar ambos valores: Taller y Patente.",
                "Campos incompletos",
                JOptionPane.WARNING_MESSAGE
            );
            return;
        }*/

        // Validar que todas las calidades estén completas
        calidades.clear();
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            String calidad = tableModel.getValueAt(i, 4).toString().trim();
            if (calidad.isEmpty()) {
                JOptionPane.showMessageDialog(
                    this,
                    "Debe ingresar la calidad para todas las autopartes.",
                    "Campos incompletos",
                    JOptionPane.WARNING_MESSAGE
                );
                return;
            }
            calidades.add(calidad);
        }

        this.taller = t;
        this.patente = p;
        dispose();
    }

    public String getTaller() {
        return taller;
    }

    public String getPatente() {
        return patente;
    }

    public List<String> getCalidades() {
        return calidades;
    }

    public boolean showDialog() {
        setVisible(true);
        return taller != null && patente != null;
    }
}
