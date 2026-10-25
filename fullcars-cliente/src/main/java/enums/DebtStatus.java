package enums;

import java.awt.Color;

/** Estado de deuda de un cliente segun su venta pendiente mas vieja (GET /customers/debt-status). */
public enum DebtStatus {

    SIN_DEUDA(new Color(56, 142, 60), new Color(232, 245, 233)),       // verde: ninguna venta pendiente
    DEUDA_RECIENTE(new Color(26, 115, 232), new Color(232, 240, 254)), // azul: debe ventas de menos de 30 dias
    DEUDA_30_DIAS(new Color(230, 162, 0), new Color(255, 246, 214)),   // amarillo: alguna de 30 dias o mas
    DEUDA_40_DIAS(new Color(211, 47, 47), new Color(253, 234, 234));   // rojo: alguna de 40 dias o mas

    public static final int DIAS_AMARILLO = 30;
    public static final int DIAS_ROJO = 40;

    private final Color color;
    private final Color fondo;

    private DebtStatus(Color color, Color fondo) {
        this.color = color;
        this.fondo = fondo;
    }

    public Color color() {
        return color;
    }

    /** Version clara del color, para fondos. */
    public Color fondo() {
        return fondo;
    }
}
