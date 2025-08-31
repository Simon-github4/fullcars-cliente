package views.carpart;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import model.client.entities.CarPart;
import controller.AppContext;

public class CarPartSearchDialog extends JDialog {
    private JTextField searchField;
    private JTable table;
    private DefaultTableModel tableModel;
    private TableRowSorter<DefaultTableModel> sorter;

    private String selectedCarPart;

    public CarPartSearchDialog(Frame parent) {
        super(parent, "Buscar por SKU o NOMBRE", true);
        setSize(600, 400);
        setLocationRelativeTo(parent);

        searchField = new JTextField();
        tableModel = new DefaultTableModel(new Object[]{"SKU", "Nombre"}, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };

        List<CarPart> carParts = AppContext.carPartController.getCarParts();
        for (CarPart cp : carParts) 
            tableModel.addRow(new Object[]{cp.getSku(), cp.getName()});

        table = new JTable(tableModel);
        sorter = new TableRowSorter<>(tableModel);
        table.setRowSorter(sorter);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // 🔎 Filtro dinámico
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            private void filter() {
                String text = searchField.getText().trim();
                if (text.isEmpty()) {
                    sorter.setRowFilter(null);
                } else {
                    sorter.setRowFilter(RowFilter.regexFilter("(?i)" + text, 0, 1)); // SKU o Nombre
                }
            }
            @Override public void insertUpdate(DocumentEvent e) { filter(); }
            @Override public void removeUpdate(DocumentEvent e) { filter(); }
            @Override public void changedUpdate(DocumentEvent e) { filter(); }
        });

        // ⬇️ Al presionar flecha abajo en el textfield → mover foco a la tabla
        searchField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_DOWN && table.getRowCount() > 0) {
                    table.requestFocus();
                    table.setRowSelectionInterval(0, 0);
                }
            }
        });

        // ⏎ Enter en la tabla → seleccionar CarPart
        table.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    int row = table.getSelectedRow();
                    if (row != -1) {
                        int modelRow = table.convertRowIndexToModel(row);
                        String sku = (String) tableModel.getValueAt(modelRow, 0);
                        String name = (String) tableModel.getValueAt(modelRow, 1);
                        selectedCarPart = sku;
                        dispose();
                    }
                }
            }
        });
        
        setLayout(new BorderLayout(10,10));
        table.setBorder(BorderFactory.createEmptyBorder(5,10,5,10));
        //searchField.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
        add(searchField, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);
    }

    public String getSelectedCarPartSku() {
        return selectedCarPart;
    }

}
