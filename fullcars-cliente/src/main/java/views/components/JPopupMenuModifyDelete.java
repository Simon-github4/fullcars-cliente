package views.components;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTable;

public class JPopupMenuModifyDelete extends JPopupMenu {

    private static final long serialVersionUID = 1L;

    public JPopupMenuModifyDelete(JTable table, Runnable onEdit, Runnable onDelete) {
        addMenuItem("Modificar fila", onEdit);
        addMenuItem("Eliminar fila", onDelete);
        attachToTable(table);
    }

    public JPopupMenuModifyDelete(JTable table, Runnable toDo, String text) {
        addMenuItem(text, toDo);
        attachToTable(table);
    }

    public JPopupMenuModifyDelete addMenuItem(String text, Runnable action) {
        JMenuItem item = new JMenuItem(text);
        item.addActionListener(e -> action.run());
        add(item);
		return this;
    }

    private void attachToTable(JTable table) {
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (e.isPopupTrigger()) 
                    showIfPopup(e);
            }
            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.isPopupTrigger()) 
                    showIfPopup(e);
            }
            private void showIfPopup(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    int row = table.rowAtPoint(e.getPoint());
                    if (row >= 0 && row < table.getRowCount()) {
                        table.setRowSelectionInterval(row, row);
                        show(e.getComponent(), e.getX(), e.getY());
                    }
                }
            }
        });
    }
}


