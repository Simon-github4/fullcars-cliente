package controller;

import java.awt.Desktop;
import java.io.IOException;
import java.nio.file.Path;

import Utils.ServerException;
import data.service.ClienteRestDevolucion;
import dtos.DevolucionRequest;

public class DevolucionController {

	private final ClienteRestDevolucion service = new ClienteRestDevolucion();

	public Long devolver(DevolucionRequest request) throws ServerException, IOException {
		Long devolucionId = service.crearDevolucion(request).get("id").asLong();

		if (Boolean.TRUE.equals(request.getEmitirCAE()) && devolucionId != null) {
			Path pdfPath = service.getDevolucionPdf(devolucionId);
			if (Desktop.isDesktopSupported()) {
				Desktop.getDesktop().open(pdfPath.toFile());
			}
		}

		return devolucionId;
	}
}
