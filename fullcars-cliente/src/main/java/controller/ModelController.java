package controller;

import java.io.IOException;
import java.util.List;

import Utils.ServerException;
import data.service.ClienteRestModel;
import model.client.entities.Model;

public class ModelController {

private final ClienteRestModel modelService = new ClienteRestModel();
	
	public List<Model> getModels() {
		return modelService.getModels();
	}

	public Model getModel(Long id) {
		return modelService.getModel(id);
	}

	public Model save(Model m) throws ServerException, IOException, Exception {
		return modelService.save(m);
	}

	public void delete(Long id) throws IOException, ServerException {
		try {
			modelService.delete(id);
		} catch (IOException e) {
			e.printStackTrace();
			throw new IOException("No se pudo eliminar el Modelo");
		}
	}
}
