package controller;

import java.awt.Desktop;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

import Utils.PdfUtils;
import Utils.ServerException;
import data.service.ClienteRestSale;
import model.client.entities.Customer;
import model.client.entities.Sale;

public class SaleController {

	private final ClienteRestSale serviceSale = new ClienteRestSale();
	
	public Sale getSale(Long id){
		return serviceSale.getSale(id);
	}
	
	public List<Sale> getSales(){
		return serviceSale.getSales();
	}

	public List<Sale> getSales(Customer c, LocalDate[] dates){
		return serviceSale.getSales(dates, c.getId());
	}
	
	public void save(Sale c) throws ServerException, IOException, Exception {
		Sale savedSale = serviceSale.save(c);
		
		Thread t = new Thread(() ->{ 
			byte[] file = PdfUtils.generateSalePdf(c);
			
			if(file != null && savedSale != null) {
	            File tempFile = null;
				try {
					tempFile = File.createTempFile("sale-", ".pdf");
				} catch (IOException e) {
					e.printStackTrace();
				}
	            try (FileOutputStream fos = new FileOutputStream(tempFile)) {
	                fos.write(file);
	                Desktop.getDesktop().open(tempFile);
	                serviceSale.uploadRemito(file, savedSale.getId());
	            } catch (IOException | ServerException e) {
					e.printStackTrace();
				}
			}else
				System.err.println("Archivo devuelto o savedSale == null");
		});
		t.start();
	}

	public void delete(Long id) throws ServerException, IOException {
		serviceSale.delete(id);
	}

	
	public void getAndOpenRemito(Long saleId) throws ServerException, IOException {
		Path tempFile = serviceSale.getAndOpenRemito(saleId);
		
		if (Desktop.isDesktopSupported()) 
            Desktop.getDesktop().open(tempFile.toFile());
        else 
            System.out.println("Archivo descargado en: " + tempFile.toAbsolutePath());
	}
}
