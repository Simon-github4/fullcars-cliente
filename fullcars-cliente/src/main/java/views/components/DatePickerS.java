package views.components;

import java.awt.Color;

import javax.swing.JFormattedTextField;

import Utils.Icons;
import raven.datetime.DatePicker;

public class DatePickerS extends DatePicker{
	private static final long serialVersionUID = -7536725853504865001L;

	public DatePickerS() {
		super();
		setBackground(Color.gray);
	}
	
	@Override
	public void setEditor(JFormattedTextField ft) {
		super.setEditor(ft);
		setEditorIcon(Icons.CALENDAR.create());		
	}
}
