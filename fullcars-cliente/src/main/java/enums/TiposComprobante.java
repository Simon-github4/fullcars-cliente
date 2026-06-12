package enums;

public enum TiposComprobante {
    FACTURA_A(1),
    NOTA_CREDITO_A(3),
    FACTURA_B(6),
    NOTA_CREDITO_B(8),
    FACTURA_C(11),
    NOTA_CREDITO_C(13);

    private final int codigo;

    private TiposComprobante(int codigo) {
        this.codigo = codigo;
    }

    public int getCodigo() {
        return codigo;
    }

    public char getTipo() {
        switch (this) {
            case FACTURA_A:
            case NOTA_CREDITO_A:
                return 'A';
            case FACTURA_B:
            case NOTA_CREDITO_B:
                return 'B';
            case FACTURA_C:
            case NOTA_CREDITO_C:
                return 'C';
            default:
                throw new IllegalArgumentException("Tipo de Comprobante invalido: " + this);
        }
    }

    public boolean isFactura() {
        return this == FACTURA_A || this == FACTURA_B || this == FACTURA_C;
    }

    public boolean isNotaCredito() {
        return this == NOTA_CREDITO_A || this == NOTA_CREDITO_B || this == NOTA_CREDITO_C;
    }

    public static TiposComprobante notaCreditoPara(TiposComprobante comprobanteOriginal) {
        switch (comprobanteOriginal) {
            case FACTURA_A:
                return NOTA_CREDITO_A;
            case FACTURA_B:
                return NOTA_CREDITO_B;
            case FACTURA_C:
                return NOTA_CREDITO_C;
            default:
                throw new IllegalArgumentException("No se puede generar nota de credito para: " + comprobanteOriginal);
        }
    }

    public static TiposComprobante fromCodigo(int codigo) {
        for (TiposComprobante c : TiposComprobante.values()) {
            if (c.codigo == codigo) {
                return c;
            }
        }

        throw new IllegalArgumentException("Codigo de Tipo de Comprobante invalido: " + codigo);
    }
}
