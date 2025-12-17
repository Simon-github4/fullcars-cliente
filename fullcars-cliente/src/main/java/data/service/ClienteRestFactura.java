package data.service;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import InitClass.Initializr;
import Utils.ServerException;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class ClienteRestFactura {

	private static final String ADDRESS = Initializr.getServerUrl()+"/facturas";
	private final OkHttpClient client = new OkHttpClient();
    private final ObjectMapper mapper;

    public ClienteRestFactura() {
    	this.mapper = new ObjectMapper();
    	this.mapper.registerModule(new JavaTimeModule());
        this.mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }
    
	
	public Path facturar(Long saleId, Integer tipoComprobante, long idReceptor) throws ServerException {
		HttpUrl.Builder urlBuilder = Objects.requireNonNull(HttpUrl.parse(ADDRESS + "/emitir")).newBuilder();
	    urlBuilder.addQueryParameter("idVenta", String.valueOf(saleId));
	    urlBuilder.addQueryParameter("tipoComprobante", tipoComprobante.toString());
	    urlBuilder.addQueryParameter("idReceptor", String.valueOf(idReceptor));

	    String finalUrl = urlBuilder.build().toString();

	    Request request = new Request.Builder()
	            .url(finalUrl)
	            .get() // Explicito que es un GET
	            .build();
	    
		try (Response response = client.newCall(request).execute()) {
			if (!response.isSuccessful()) {
				System.err.println("Error al descargar archivo: " + response.code() + " - " + response.message());
				throw new ServerException(response.body() != null ? response.body().string() : "No se pudo obtener el Remito");
			}

			String disposition = response.header("Content-Disposition");
			String fileName = "archivo_descargado";
			if (disposition != null && disposition.contains("filename=")) {
				fileName = disposition.split("filename=")[1].replace("\"", "");
			}

			Path tempFile = Files.createTempFile("bill_", "_" + fileName);
			try (InputStream in = response.body().byteStream(); OutputStream out = Files.newOutputStream(tempFile)) {
				in.transferTo(out);
			}
			
			return tempFile;
			
		} catch (IOException e) {
			e.printStackTrace();
			throw new ServerException("No se pudo obtener el Remito");
		}
	}
}
