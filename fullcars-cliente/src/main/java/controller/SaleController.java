package controller;

import java.awt.Desktop;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

import Utils.PdfRemitosUtils;
import Utils.ServerException;
import data.service.ClienteRestSale;
import model.client.entities.Customer;
import model.client.entities.Sale;
import views.transactions.DialogTallerPatente;

public class SaleController {

	private final ClienteRestSale serviceSale = new ClienteRestSale();
	private final FacturaController serviceFactura = new FacturaController();
	
	public Sale getSale(Long id){
		return serviceSale.getSale(id);
	}

	public List<Sale> getSales(Customer c, LocalDate[] dates, boolean showAll){
		List<Sale> sales = serviceSale.getSales(dates, (c == null)?null: c.getId());
		if(!showAll)
			sales = sales.stream().filter(s-> s.getFactura() != null).toList();
		return sales;
	}
	
	public Sale save(Sale c) throws ServerException, IOException, Exception {
		Sale savedSale = serviceSale.save(c);
		
		DialogTallerPatente dialogo = new DialogTallerPatente(null, c.getDetails());
		dialogo.showDialog();
		String taller = dialogo.getTaller();
        String patente = dialogo.getPatente();
        List<String> calidades = dialogo.getCalidades();
        
		Thread t = new Thread(() ->{ 
			byte[] file;
			if(savedSale.getSaleNumber() == null)
				file = PdfRemitosUtils.generatePresupuestoPdf(savedSale, patente, taller, calidades);
			else
				file = PdfRemitosUtils.generateRemitoPdf(savedSale, patente, taller, calidades);
			
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
		return savedSale;
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
	
	public void facturar(Long saleId) throws Exception {
		serviceFactura.facturar(saleId);
	}

	public void notaCredito(Long idSale) throws Exception {
		serviceFactura.emitirNotaCredito(idSale);
	}

	public void showFacturaData(Long saleId) throws Exception {
		serviceFactura.showFacturaData(saleId);
	}
}
