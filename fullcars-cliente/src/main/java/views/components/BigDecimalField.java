package views.components;

import java.awt.event.ActionEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;

import javax.swing.AbstractAction;
import javax.swing.JFormattedTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.text.NumberFormatter;

public class BigDecimalField extends JFormattedTextField {

    public BigDecimalField(int cols) {
        this(BigDecimal.ZERO);
        setColumns(cols);
    }

    public BigDecimalField(BigDecimal initialValue) {
        super(createFormatter());
        setBigDecimal(initialValue);
        setColumns(10);
        addSelectOnFocusForZero();
        addEnterKeyBinding(); // 👈 lo agregamos acá

    }

    private static NumberFormatter createFormatter() {
        DecimalFormat df = new DecimalFormat("#0.00"); 
        df.setParseBigDecimal(true); // 👈 muy importante

        NumberFormatter formatter = new NumberFormatter(df);
        formatter.setValueClass(BigDecimal.class);
        formatter.setAllowsInvalid(false);
        formatter.setCommitsOnValidEdit(true);

        return formatter;
    }

    public BigDecimal getBigDecimal() {
        Object value = getValue();
        if (value instanceof BigDecimal) {
            return ((BigDecimal) value).setScale(2, RoundingMode.HALF_UP);
        } else if (value instanceof Number) {
            return BigDecimal.valueOf(((Number) value).doubleValue()).setScale(2, RoundingMode.HALF_UP);
        } else {
            return BigDecimal.ZERO;
        }
    }

    public void clear() {
    	setValue(BigDecimal.ZERO);
    }
    public void setBigDecimal(BigDecimal value) {
        setValue(value != null ? value.setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO);
    }
    
    private void addSelectOnFocusForZero() {
        addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                SwingUtilities.invokeLater(() -> {
                    BigDecimal currentValue = getBigDecimal();
                    if (currentValue.compareTo(BigDecimal.ZERO) == 0) {
                        selectAll();
                    }
                });
            }
        });
    }
    
    private void addEnterKeyBinding() {
        getInputMap().put(KeyStroke.getKeyStroke("ENTER"), "commitAndFire");
        getActionMap().put("commitAndFire", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // dispara el ActionListener como si hubiese cambio
                postActionEvent();
            }
        });
    }
    
}

