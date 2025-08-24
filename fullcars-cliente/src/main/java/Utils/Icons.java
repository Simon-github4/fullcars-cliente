package Utils;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import com.formdev.flatlaf.extras.FlatSVGIcon;

public enum Icons {
	LENS("lupa.png"),
	CONFIRM("confirmar2.png"),
	DELETE("eliminar.png"),
	CLEAN("cancelar.png"),
	BILL("factura.png"),
	MODIFY("modificar.png"),
	REFRESH("actualizar.png"),
	NEW("nuevo.png"),
	EYE("ojo.png"),
	CAR("auto.png"),
	BRAND("marcasfrente.png"),
	PROVIDER("proveedor.png"),
	CUSTOMER("cliente.png"),
	PURCHASE("compra.png"),
	SALE("venta.png"),
	STOCK("stock.png"),
	CATEGORY("barras.png"),
	LOGO_ST("logoS-T.png"),
	LOGO("fullcarsLOGO.jpg");
	
	private static final String SRC = "/resources/imgs/";
	private final String fileName;

	public Icon create(int width, int height) {
		if (fileName.toLowerCase().endsWith(".svg")) {
            return new FlatSVGIcon(getPath(), width, height);
        } else {
            ImageIcon icon = new ImageIcon(getClass().getResource(getPath()));
            /*Image img = icon.getImage();

	         // Crear buffer con transparencia
	         BufferedImage buffered = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
	         Graphics2D g2 = buffered.createGraphics();
	
	         // Dibujar imagen original
	         g2.drawImage(img, 0, 0, width, height, null);
	
	         // Aplicar un "tinte" blanco
	         g2.setComposite(AlphaComposite.SrcAtop);
	         g2.setColor(Color.WHITE);
	         g2.fillRect(0, 0, width, height);
	
	         g2.dispose();
	         icon = new ImageIcon(buffered);
             */
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

