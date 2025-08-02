package controller;

import java.io.IOException;
import java.util.List;

import data.service.ClienteRestBrand;
import interfaces.IBrandProvider;
import model.client.entities.Brand;
import model.client.entities.Category;

public class BrandController implements IBrandProvider{

	private final ClienteRestBrand brandService = new ClienteRestBrand();
	
	@Override
	public List<Brand> getBrands() {
		return brandService.getBrands();
	}

	public Brand getBrand(Long id) {
		return brandService.getBrand(id);
	}

	public boolean save(Brand m) {
		return brandService.save(m);
	}

	public void delete(Long id) throws IOException {
		try {
			brandService.delete(id);
		} catch (IOException e) {
			e.printStackTrace();
			throw new IOException("No se pudo eliminar la marca");
		}
	}
	
}
