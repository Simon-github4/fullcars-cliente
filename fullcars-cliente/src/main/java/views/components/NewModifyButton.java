package views.components;

import javax.swing.JButton;

import Utils.Icons;

public class NewModifyButton extends JButton{

	private static final long serialVersionUID = 1L;
	private Long idToModify;
	
	public NewModifyButton() {
		super();
		toNew();
	}
	
	public void toModify(Long id) {
		setText("Modificar");
		setIcon(Icons.MODIFY.create());
		idToModify = id;
	}
	
	public void toNew() {
		setText("Agregar");
		setIcon(Icons.NEW.create());
		idToModify = null;
	}

	public boolean isInModifyMode() {
	    return idToModify != null;
	}

	public Long getIdToModify() {
		return idToModify;
	}

}
