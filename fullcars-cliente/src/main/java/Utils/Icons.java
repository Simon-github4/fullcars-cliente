package Utils;

import java.awt.Image;
import java.net.URL;

import javax.swing.ImageIcon;

public enum Icons {
	LENS("lupa.png"),
	CONFIRM("confirmar.png"),
	DELETE("eliminar.png"),
	CLEAN("escoba.png"),
	BILL("factura.png"),
	MODIFY("modificar.png");
	
	private static final String SRC = "/resources/imgs/";
	private final String fileName;

	public ImageIcon create(int width, int height) {
		URL url = getClass().getResource(this.getPath());
        if (url != null) {
            ImageIcon icon = new ImageIcon(url);
            Image image = icon.getImage();
            Image resizedImage = image.getScaledInstance(width, height, Image.SCALE_SMOOTH);
            ImageIcon resizedIcon = new ImageIcon(resizedImage);
            return resizedIcon;
        }else
        	return new ImageIcon();//default
	}
	public ImageIcon create() {
		URL url = getClass().getResource(this.getPath());
        if (url != null) 
            return new ImageIcon(url);
        else
        	return new ImageIcon();
	}
	Icons(String fileName) {
		this.fileName = fileName;
	}
    public String getPath() {
    	return SRC + fileName;
	}
}	

