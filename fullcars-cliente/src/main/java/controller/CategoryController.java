package controller;

import java.io.IOException;
import java.util.List;

import data.service.ClienteRestCategory;
import model.client.entities.Category;

public class CategoryController {

	private final ClienteRestCategory categoryService = new ClienteRestCategory();
	
	//@Override
	public List<Category> getCategories() {
		return categoryService.getCategories();
	}

	public Category getCategory(Long id) {
		return categoryService.getCategory(id);
	}

	public boolean save(Category m) {
		return categoryService.save(m);
	}

	public void delete(Long id) throws IOException {
		try {
			categoryService.delete(id);
		} catch (IOException e) {
			e.printStackTrace();
			throw new IOException("No se pudo eliminar la categoria");
		}
	}

	
}
