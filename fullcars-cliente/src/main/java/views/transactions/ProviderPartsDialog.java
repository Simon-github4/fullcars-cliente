package views.transactions;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridLayout;
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
import javax.swing.RowFilter;
import javax.swing.SwingConstants;
import javax.swing.event.DocumentListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableRowSorter;

import model.client.entities.ProviderPart;

public class ProviderPartsDialog extends JDialog {

    private JTable table;
    private ProviderPartTableModel tableModel;
    private TableRowSorter<ProviderPartTableModel> sorter;

    private JTextField txtNombre;
    private JTextField txtMarca;
    private JTextField txtProveedor;

    private ProviderPart selectedPart;

    public ProviderPartsDialog(Frame parent, List<ProviderPart> partes) {
        super(parent, "Seleccionar Parte de Proveedor", true);

        tableModel = new ProviderPartTableModel(partes);
        table = new JTable(tableModel);

        sorter = new TableRowSorter<>(tableModel);
        table.setRowSorter(sorter);
        sorter.setComparator(3, (o1, o2) -> {
            if (o1 == o2) return 0;
            if (o1 == null) return 1;   // nulls al final
            if (o2 == null) return -1;
            return ((java.math.BigDecimal) o1).compareTo((java.math.BigDecimal) o2);
        });

        JScrollPane scrollPane = new JScrollPane(table);

        JPanel filterPanel = new JPanel(new GridLayout(2, 3, 5, 5));
        txtNombre = new JTextField();
        txtMarca = new JTextField();
        txtProveedor = new JTextField();

        filterPanel.add(new JLabel("Nombre:"));
        filterPanel.add(new JLabel("Marca:"));
        filterPanel.add(new JLabel("Proveedor:"));
        filterPanel.add(txtNombre);
        filterPanel.add(txtMarca);
        filterPanel.add(txtProveedor);

        DocumentListener listener = new DocumentListener() {
            @Override
            public void insertUpdate(javax.swing.event.DocumentEvent e) { filtrar(); }
            @Override
            public void removeUpdate(javax.swing.event.DocumentEvent e) { filtrar(); }
            @Override
            public void changedUpdate(javax.swing.event.DocumentEvent e) { filtrar(); }
        };

        txtNombre.getDocument().addDocumentListener(listener);
        txtMarca.getDocument().addDocumentListener(listener);
        txtProveedor.getDocument().addDocumentListener(listener);

        JButton btnAceptar = new JButton("Aceptar");
        JButton btnCancelar = new JButton("Cancelar");

        btnAceptar.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row >= 0) {
                int modelRow = table.convertRowIndexToModel(row);
                selectedPart = tableModel.getPartAt(modelRow);
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Seleccione una parte primero.");
            }
        });

        btnCancelar.addActionListener(e -> {
            selectedPart = null;
            dispose();
        });

        DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
        rightRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        table.getColumnModel().getColumn(3).setCellRenderer(rightRenderer);
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(btnAceptar);
        buttonPanel.add(btnCancelar);

        getContentPane().setLayout(new BorderLayout(10, 10));
        getContentPane().add(filterPanel, BorderLayout.NORTH);
        getContentPane().add(scrollPane, BorderLayout.CENTER);
        getContentPane().add(buttonPanel, BorderLayout.SOUTH);

        setSize(800, 500);
        setLocationRelativeTo(parent);
    }

    private void filtrar() {
        List<RowFilter<Object,Object>> filtros = new ArrayList<>();

        if (!txtNombre.getText().trim().isEmpty()) {
            filtros.add(RowFilter.regexFilter("(?i)" + txtNombre.getText().trim(), 0)); // columna nombre
        }
        if (!txtMarca.getText().trim().isEmpty()) {
            filtros.add(RowFilter.regexFilter("(?i)" + txtMarca.getText().trim(), 1)); // columna marca
        }
        if (!txtProveedor.getText().trim().isEmpty()) {
            filtros.add(RowFilter.regexFilter("(?i)" + txtProveedor.getText().trim(), 2)); // columna proveedor
        }

        RowFilter<ProviderPartTableModel,Object> rf = RowFilter.andFilter(filtros);
        sorter.setRowFilter(rf);
    }

    public ProviderPart getSelectedPart() {
        return selectedPart;
    }

    // ==========================
    // Table Model interno
    // ==========================
    private static class ProviderPartTableModel extends AbstractTableModel {
        private final String[] columnNames = {"Nombre", "Marca", "Proveedor", "Precio"};
        private final List<ProviderPart> partes;

        public ProviderPartTableModel(List<ProviderPart> partes) {
            this.partes = partes != null ? partes : new ArrayList<>();
        }
        @Override
        public int getRowCount() {
            return partes.size();
        }
        @Override
        public int getColumnCount() {
            return columnNames.length;
        }
        @Override
        public String getColumnName(int column) {
            return columnNames[column];
        }
        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            ProviderPart p = partes.get(rowIndex);
            return switch (columnIndex) {
                case 0 -> p.getNombre();
                case 1 -> p.getMarca();
                case 2 -> p.getProviderMapping() != null ? p.getProviderMapping().getProviderId() : "-";
                case 3 -> p.getPrecio();
                default -> "";
            };
        }
        public ProviderPart getPartAt(int rowIndex) {
            return partes.get(rowIndex);
        }
        @Override
        public Class<?> getColumnClass(int columnIndex) {
            return switch (columnIndex) {
                case 0 -> String.class;          // Nombre
                case 1 -> String.class;          // Marca
                case 2 -> String.class;          // Proveedor (si es texto)
                case 3 -> java.math.BigDecimal.class; // Precio
                default -> Object.class;
            };
        }

    }
}
