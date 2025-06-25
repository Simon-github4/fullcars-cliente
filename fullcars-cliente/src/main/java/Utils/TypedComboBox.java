package Utils;

import java.util.List;

import javax.swing.JComboBox;

public class TypedComboBox<T> extends JComboBox<T> {
    
	private static final long serialVersionUID = 4343156235974283391L;

	//@SuppressWarnings("unchecked")
	@Override
	public T getSelectedItem() {
        return (T) super.getSelectedItem();
    }

	public void fill(List<T> items, T placeholder) {
	    removeAllItems();
	    addItem(placeholder);
	    items.forEach(this::addItem); // << this is correct
	}

}

