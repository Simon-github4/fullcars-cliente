package enums;

public enum Conceptos {

    PRODUCTOS(1), SERVICIOS(2), PRODUCTOS_Y_SERVICIOS(3);

    private final int codigo;

    private Conceptos(int codigo) {
        this.codigo = codigo;
    }

    public int codigo() {
        return codigo;
    }
}
