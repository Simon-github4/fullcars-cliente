package controller;

import java.io.IOException;
import java.util.List;

import data.service.ClienteRestProvider;
import model.client.entities.Provider;

public class ProviderController {
	
	private final ClienteRestProvider serviceProvider = new ClienteRestProvider();
	
	public Provider getProvider(Long id){
		return serviceProvider.getProvider(id);
	}
	
	public List<Provider> getProviders(){
		return serviceProvider.getProviders();
	}

	public boolean save(Provider c) {
		return serviceProvider.save(c);
	}

	public void delete(Long id) throws IOException  {
		try {
			serviceProvider.delete(id);
		} catch (IOException e) {
			e.printStackTrace();
			throw new IOException("No se pudo eliminar el proveedor");
		}	
	}
	
}
