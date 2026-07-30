package data.service;

import java.awt.Desktop;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import InitClass.Initializr;
import Utils.ServerException;
import dtos.DevolucionRequest;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ClienteRestDevolucion {

	private static final String ADDRESS = Initializr.getServerUrl() + "/devoluciones";
	private final OkHttpClient client = new OkHttpClient();
	private final ObjectMapper mapper;

	public ClienteRestDevolucion() {
		this.mapper = new ObjectMapper();
		this.mapper.registerModule(new JavaTimeModule());
		this.mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
	}

	public JsonNode crearDevolucion(DevolucionRequest request) throws ServerException, IOException {
		String json = mapper.writeValueAsString(request);
		RequestBody body = RequestBody.create(json, MediaType.parse("application/json"));
		Request httpRequest = new Request.Builder().url(ADDRESS).post(body).build();

		try (Response response = client.newCall(httpRequest).execute()) {
			if (response.isSuccessful() && response.body() != null) {
				String responseJson = response.body().string();
				return mapper.readTree(responseJson);
			} else {
				String errorBody = response.body() != null ? response.body().string() : "Error inesperado";
				throw new ServerException(errorBody);
			}
		}
	}

	public Path getDevolucionPdf(Long devolucionId) throws ServerException, IOException {
		String url = ADDRESS + "/" + devolucionId + "/pdf";
		Request request = new Request.Builder().url(url).get().build();

		try (Response response = client.newCall(request).execute()) {
			if (!response.isSuccessful()) {
				throw new ServerException(response.body() != null ? response.body().string() : "No se pudo obtener el PDF");
			}

			String disposition = response.header("Content-Disposition");
			String fileName = "devolucion_" + devolucionId + ".pdf";
			if (disposition != null && disposition.contains("filename=")) {
				fileName = disposition.split("filename=")[1].replace("\"", "");
			}

			Path tempFile = Files.createTempFile("nc_", "_" + fileName);
			try (InputStream in = response.body().byteStream(); OutputStream out = Files.newOutputStream(tempFile)) {
				in.transferTo(out);
			}
			return tempFile;
		}
	}

	public List<JsonNode> getDevolucionesBySale(Long saleId) throws ServerException, IOException {
		String url = ADDRESS + "/by-sale/" + saleId;
		Request request = new Request.Builder().url(url).get().build();

		try (Response response = client.newCall(request).execute()) {
			if (response.isSuccessful() && response.body() != null) {
				String responseJson = response.body().string();
				JsonNode rootNode = mapper.readTree(responseJson);
				List<JsonNode> devoluciones = new ArrayList<>();
				if (rootNode.isArray()) {
					for (JsonNode node : rootNode) {
						devoluciones.add(node);
					}
				}
				return devoluciones;
			} else {
				String errorBody = response.body() != null ? response.body().string() : "Error inesperado";
				throw new ServerException(errorBody);
			}
		}
	}
}
