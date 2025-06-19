package controller;

import java.util.List;

import data.service.ClienteRestCategory;
import model.client.entities.Category;

public class CategoryController {

	private final ClienteRestCategory categoryService = new ClienteRestCategory();
	
	//@Override
	public List<Category> getCategories() {
		return categoryService.getCategories();
	}

	
}
