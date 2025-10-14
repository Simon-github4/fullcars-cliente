package controller;

import java.io.File;
import java.io.IOException;
import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;

import Utils.ServerException;
import data.service.ClienteRestProvider;
import dtos.TaskStatusInfo;
import model.client.entities.CarPart;
import model.client.entities.Provider;
import model.client.entities.ProviderMapping;
import model.client.entities.ProviderPart;

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

	public List<ProviderPart> getProviderParts(){
		return serviceProvider.getProviderParts();
	}
	public ProviderMapping getProviderMapping(Long idProvider) {
		return serviceProvider.getProviderMapping(idProvider);
	}

	public String saveProviderMapping(ProviderMapping nuevoMapping, File archivoSeleccionado) throws IOException {
		return serviceProvider.saveProviderMapping(nuevoMapping, archivoSeleccionado);
	}

	public TaskStatusInfo getTaskStatus(String taskId) {
		return serviceProvider.getTaskStatus(taskId);
	}

	public CarPart findOrCreateCarPartFromProviderPart(ProviderPart provPart)  {
		try {
			return serviceProvider.findOrCreateCarPartFromProviderPart(provPart);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
			//throw new Exception("No se pudo obtener Ni Crear la Autoparte");
		}
	}
	
}
