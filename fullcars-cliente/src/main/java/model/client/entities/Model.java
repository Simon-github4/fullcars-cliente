package model.client.entities;

import lombok.Data;

@Data
public class Model {

	private Long id;
	private String name;
	private Brand brand;
	
	public Model(Long id, String name, Brand brand) {
		this.id = id;
		this.name = name;
		this.brand = brand;
	}
	public Model(Long id, String name) {
		this.id = id;
		this.name = name;
	}
	public Model() {
		this.id = null;
	}
	@Override
	public String toString() {
		return ((brand != null)?brand.getName()+"-" : "") + name;
	}
}
