package controller;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

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
		serviceSale.save(c);
	}

	public void delete(Long id) throws ServerException, IOException {
		serviceSale.delete(id);
	}
}
