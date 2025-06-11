package controller;

import java.util.List;

import data.service.ClienteRestBrand;
import interfaces.IBrandProvider;
import model.client.entities.Brand;

public class BrandController implements IBrandProvider{

	private final ClienteRestBrand brandService = new ClienteRestBrand();
	
	@Override
	public List<Brand> getBrands() {
		return brandService.getBrands();
	}

	
}
