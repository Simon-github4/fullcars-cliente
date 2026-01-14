package enums;

public enum CondicionIva {
    RESPONSABLE_INSCRIPTO(30L, "IVA Responsable Inscripto"),
    MONOTRIBUTO(20L, "Responsable Monotributo"),
    IVA_EXENTO(32L, "IVA Sujeto Exento"),
    CONSUMIDOR_FINAL(98L, "Consumidor Final (DNI/Sin CUIT)"),
    NO_ENCONTRADO(99L, "Sujeto No Categorizado / Error");//para el codigo

    private final Long codigo;
    private final String descripcion;

    CondicionIva(Long codigo, String descripcion) {
        this.codigo = codigo;
        this.descripcion = descripcion;
    }

    public Long getCodigo() {
        return codigo;
    }

    public String getDescripcion() {
        return descripcion;
    }
    
    // Método estático para buscar la condición por código (útil para el parser)
    public static CondicionIva fromCodigo(Long codigo) {
        for (CondicionIva c : CondicionIva.values()) {
            if (c.codigo.equals(codigo)) {
                return c;
            }
        }
        return NO_ENCONTRADO;
    }
    
    public static CondicionIva fromCodigo(String codigoStr) {
		try {
			Long codigo = Long.parseLong(codigoStr);
			return fromCodigo(codigo);
		} catch (NumberFormatException e) {
			return NO_ENCONTRADO;
		}
        
    }
}