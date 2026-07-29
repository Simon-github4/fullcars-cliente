package controller;

import java.io.IOException;

import Utils.ServerException;
import data.service.ClienteRestDevolucion;
import dtos.DevolucionRequest;

public class DevolucionController {

	private final ClienteRestDevolucion service = new ClienteRestDevolucion();

	public void devolver(DevolucionRequest request) throws ServerException, IOException {
		service.crearDevolucion(request);
	}
}
