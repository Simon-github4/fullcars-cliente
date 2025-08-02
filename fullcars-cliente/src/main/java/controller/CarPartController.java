package controller;

import java.io.IOException;
import java.util.List;

import Utils.ServerException;
import data.service.ClienteRestCarPart;
import model.client.entities.CarPart;

public class CarPartController {

	private final ClienteRestCarPart serviceCarPart = new ClienteRestCarPart();
	
	public CarPart getCarPart(Long id){
		return serviceCarPart.getCarPart(id);
	}

	public CarPart getCarPart(String sku) {
		return serviceCarPart.getCarPart(sku);
	}
	
	public List<CarPart> getCarParts(){
		return serviceCarPart.getCarParts();
	}

	public void save(CarPart c) throws ServerException, IOException, Exception {
		//if(c.getId() != null) 
			//c.setStock(getCarPart(c.getId()).getStock());
			//set actual Stock to ensure that is no modified on the client, or had change
		// This is done on server
		serviceCarPart.save(c);
	}

	public void delete(Long id) throws ServerException, IOException {
		serviceCarPart.delete(id);
	}

	
}
