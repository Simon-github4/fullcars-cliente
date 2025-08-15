package Utils;

import java.text.NumberFormat;
import java.util.Locale;

public class NumberFormatArg {

	public static String format(Object obj) {
		Locale argentina = new Locale("es", "AR");
	    NumberFormat numberFormat = NumberFormat.getNumberInstance(argentina);
	    return numberFormat.format(obj);
	}
}
