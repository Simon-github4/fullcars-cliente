package views.components;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTable;

public class JPopupMenuModifyDelete extends JPopupMenu {

	private static final long serialVersionUID = 1L;

	public JPopupMenuModifyDelete(JTable table, Runnable onEdit, Runnable onDelete) {
        JMenuItem editItem = new JMenuItem("Modificar fila");
        JMenuItem deleteItem = new JMenuItem("Eliminar fila");
        
        add(editItem);
        add(deleteItem);

        editItem.addActionListener(e -> onEdit.run());
        deleteItem.addActionListener(e -> onDelete.run());

        table.addMouseListener(new MouseAdapter() {
            @Override 
            public void mousePressed(MouseEvent e) {
            	if (e.isPopupTrigger())
            		showIfPopup(e); 
            }
            @Override
            public void mouseReleased(MouseEvent e){
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
	
	public JPopupMenuModifyDelete(JTable table, Runnable toDo, String text) {
        JMenuItem editItem = new JMenuItem(text);
        
        add(editItem);

        editItem.addActionListener(e -> toDo.run());

        table.addMouseListener(new MouseAdapter() {
            @Override 
            public void mousePressed(MouseEvent e) {
            	if (e.isPopupTrigger())
            		showIfPopup(e); 
            }
            @Override
            public void mouseReleased(MouseEvent e){
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

