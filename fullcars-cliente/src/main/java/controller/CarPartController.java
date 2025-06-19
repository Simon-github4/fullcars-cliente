package controller;

import java.util.List;

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

	public boolean save(CarPart c) {
		if(c.getId() != null) 
			c.setStock(getCarPart(c.getId()).getStock());
			//set actual Stock to ensure that is no modified on the client, or had change
		c.setSku("sku"+c.getName()+c.getDescription());
		return serviceCarPart.save(c);
	}

	public void delete(Long id) {
		serviceCarPart.delete(id);
	}

	
}
