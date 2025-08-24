package Utils;

import java.awt.Image;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import com.formdev.flatlaf.extras.FlatSVGIcon;

public enum Icons {
	LENS("lupa.png"),
	CONFIRM("confirmar.png"),
	DELETE("eliminar.png"),
	CLEAN("escoba.png"),
	BILL("factura.png"),
	MODIFY("modificar.png"),
	REFRESH("actualizar.png"),
	NEW("nuevo.png"),
	EYE("ojo.png"),
	CAR("auto.png"),
	TOYOTA("toyota.png"),
	PROVIDER("proveedor.png"),
	CUSTOMER("cliente.png"),
	PURCHASE("compra.png"),
	SALE("venta.png"),
	STOCK("stock.png"),
	CATEGORY("barras.png"),
	LOGO("fullcarsLOGO.jpg");
	
	private static final String SRC = "/resources/imgs/";
	private final String fileName;

	public Icon create(int width, int height) {
		if (fileName.toLowerCase().endsWith(".svg")) {
            return new FlatSVGIcon(getPath(), width, height);
        } else {
            ImageIcon icon = new ImageIcon(getClass().getResource(getPath()));
            if (icon.getIconWidth() != width || icon.getIconHeight() != height) {
                Image scaled = icon.getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH);
                return new ImageIcon(scaled);
            }
            return icon;
        }   
	}

    public Icon create() {
        return create(24, 24); 
    }

	Icons(String fileName) {
		this.fileName = fileName;
	}
    public String getPath() {
    	return SRC + fileName;
	}
}	

