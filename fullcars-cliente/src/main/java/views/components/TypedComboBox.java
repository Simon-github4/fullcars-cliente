package views.components;

import java.util.List;

import javax.swing.JComboBox;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.util.ArrayList;
import java.util.function.Function;

public class TypedComboBox<T> extends JComboBox<T> {
    
    private static final long serialVersionUID = 4343156235974283391L;

    private List<T> allItems = new ArrayList<>();
    private Function<T, String> toStringFunction;

	private JTextField editor;

    public TypedComboBox(Function<T, String> toStringFunction) {
        super();
        this.toStringFunction = toStringFunction;
        //setEditable(true);
        //setupFiltering();
    }

    public void fill(List<T> items, T placeholder) {
        removeAllItems();
        addItem(placeholder);
        allItems.clear();
        allItems.addAll(items);
        items.forEach(this::addItem);
    }

    @SuppressWarnings("unchecked")
    @Override
    public T getSelectedItem() {
        return (T) super.getSelectedItem();
    }

    /*private boolean isAdjusting = false;

    private void setupFiltering() {
        editor = (JTextField) getEditor().getEditorComponent();
        editor.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { scheduleFilter(); }
            public void removeUpdate(DocumentEvent e) { scheduleFilter(); }
            public void changedUpdate(DocumentEvent e) {}

            private void scheduleFilter() {
                if (!isAdjusting) {
                    SwingUtilities.invokeLater(() -> applyFilter());
                }
            }
        });
    }

    private void applyFilter() {
        if (isAdjusting) return;

        isAdjusting = true;
        try {
            String text = editor.getText().trim().toLowerCase();
            List<T> filtered = new ArrayList<>();

            for (T item : allItems) {
                String itemText = toStringFunction.apply(item).toLowerCase();
                if (itemText.contains(text)) {
                    filtered.add(item);
                }
            }

            boolean noMatch = filtered.isEmpty() || text.isEmpty();
            Object selected = getEditor().getItem();

            DefaultComboBoxModel<T> model = new DefaultComboBoxModel<>();
            for (T item : filtered) {
                model.addElement(item);
            }

            setModel(model);
            getEditor().setItem(selected);

            if (!noMatch && isDisplayable() && isShowing()) {
                setPopupVisible(true);
            } else {
                setPopupVisible(false);
            }

        } finally {
            isAdjusting = false;
        }
    }
	*/
}


