package controller;

import java.io.File;
import java.io.IOException;
import java.util.List;

import Utils.CarpartExcelExporter;
import Utils.Exporter;
import Utils.FileUtil;
import Utils.ServerException;
import data.service.ClienteRestCarPart;
import model.client.entities.CarPart;

public class CarPartController {

	private final ClienteRestCarPart serviceCarPart = new ClienteRestCarPart();
	private final Exporter<CarPart> CarpartCsvcarpartExporter = new CarpartExcelExporter();
	
	public CarPart getCarPart(Long id){
		return serviceCarPart.getCarPart(id);
	}

	public CarPart getCarPart(String sku) {
		return serviceCarPart.getCarPart(sku);
	}
	
	public List<CarPart> getCarParts(){
		return serviceCarPart.getCarParts();
	}

	public CarPart save(CarPart c) throws ServerException, IOException, Exception {
		return serviceCarPart.save(c);
	}

	public void delete(Long id) throws ServerException, IOException {
		serviceCarPart.delete(id);
	}

	public void exportCarPartsToCsv(File file) throws IOException {
		try {
			FileUtil.saveBytes(file, CarpartCsvcarpartExporter.export(getCarParts()));
		} catch (IOException e) {
			e.printStackTrace();
			throw new IOException("Hubo un error creando el archivo");
		}
	}

	
}
