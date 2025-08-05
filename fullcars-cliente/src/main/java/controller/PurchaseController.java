package controller;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

import Utils.ServerException;
import data.service.ClienteRestPurchase;
import model.client.entities.Provider;
import model.client.entities.Purchase;

public class PurchaseController {

	private final ClienteRestPurchase servicePurchase = new ClienteRestPurchase();
	
	public Purchase getPurchase(Long id){
		return servicePurchase.getPurchase(id);
	}
	
	public List<Purchase> getPurchases(){
		return servicePurchase.getPurchases();
	}

	public List<Purchase> getPurchases(Provider c, LocalDate[] dates){
		return servicePurchase.getPurchases(dates, c.getId());
	}
	
	public void save(Purchase c) throws ServerException, IOException, Exception {
		servicePurchase.save(c);
	}

	public void delete(Long id) throws ServerException, IOException {
		servicePurchase.delete(id);
	}
}
