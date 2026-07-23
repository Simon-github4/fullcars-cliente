package data.service;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import InitClass.Initializr;
import Utils.ServerException;
import model.client.entities.Factura;
import model.client.entities.CreditNote;
import model.client.entities.Sale;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
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
    
    public Factura getFacturaBySaleId(Long saleId) {
		String uri = ADDRESS +"/getBySaleId/"+ URLEncoder.encode(saleId.toString(), StandardCharsets.UTF_8);

	    Request request = new Request.Builder()
	            .url(uri)
	            .get()
	            .build();

	    try (Response response = client.newCall(request).execute()) {
	        if (response.isSuccessful() && response.body() != null) {
	        	String json = response.body().string();
	            return mapper.readValue(json, new TypeReference<Factura>() {});
	        } else {
	            System.err.println("Error HTTP: " + response.code());
	            return null;
	        }
	    } catch (IOException e) {
	        e.printStackTrace();
	        return null;
	    }
	}
    
	public CreditNote getNotaCreditoBySaleId(Long saleId) {
		String uri = ADDRESS + "/nota-credito/by-sale/" + URLEncoder.encode(saleId.toString(), StandardCharsets.UTF_8);

		Request request = new Request.Builder()
				.url(uri)
				.get()
				.build();

		try (Response response = client.newCall(request).execute()) {
			if (response.isSuccessful() && response.body() != null) {
				String json = response.body().string();
				return mapper.readValue(json, new TypeReference<CreditNote>() {});
			} else {
				System.err.println("Error HTTP: " + response.code());
				return null;
			}
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	public Path facturar(Long saleId, Integer tipoComprobante) throws ServerException {
		HttpUrl.Builder urlBuilder = Objects.requireNonNull(HttpUrl.parse(ADDRESS + "/emitir")).newBuilder();
	    urlBuilder.addQueryParameter("idVenta", String.valueOf(saleId));
	    urlBuilder.addQueryParameter("tipoComprobante", tipoComprobante.toString());
	    //urlBuilder.addQueryParameter("idReceptor", String.valueOf(idReceptor));

	    String finalUrl = urlBuilder.build().toString();

	    Request request = new Request.Builder()
	            .url(finalUrl)
	            .get() // Explicito que es un GET
	            .build();
	    
		try (Response response = client.newCall(request).execute()) {
			if (!response.isSuccessful()) {
				System.err.println("Error al descargar archivo: " + response.code() + " - " + response.message());
				throw new ServerException(response.body() != null ? response.body().string() : "No se pudo realizar la Factura");
			}

			String disposition = response.header("Content-Disposition");
			String fileName = "archivo_descargado";
			if (disposition != null && disposition.contains("filename=")) {
				fileName = disposition.split("filename=")[1].replace("\"", "");
			}

			Path tempFile = Files.createTempFile("Factura_", "_" + fileName);
			try (InputStream in = response.body().byteStream(); OutputStream out = Files.newOutputStream(tempFile)) {
				in.transferTo(out);
			}
			
			return tempFile;
			
		} catch (IOException e) {
			e.printStackTrace();
			throw new ServerException(e.getMessage());
		}
	}

	public Path getAndOpenFacturaPdf(Long saleId) throws ServerException {
		Request request = new Request.Builder().url(ADDRESS + "/getFacturaPdfBySaleId/" + saleId.toString()).build();

		try (Response response = client.newCall(request).execute()) {
			if (!response.isSuccessful()) {
				System.err.println("Error al descargar archivo: " + response.code() + " - " + response.message());
				throw new ServerException(response.body() != null ? response.body().string() : "No se pudo obtener el PDF de la Factura");
			}

			String disposition = response.header("Content-Disposition");
			String fileName = "archivo_descargado";
			if (disposition != null && disposition.contains("filename=")) {
				fileName = disposition.split("filename=")[1].replace("\"", "");
			}

			Path tempFile = Files.createTempFile("Factura_", "_" + fileName);
			try (InputStream in = response.body().byteStream(); OutputStream out = Files.newOutputStream(tempFile)) {
				in.transferTo(out);
			}
			
			return tempFile;
			
		} catch (IOException e) {
			e.printStackTrace();
			throw new ServerException("No se pudo obtener el PDF de la Factura");
		}
	}

	public Path getAndOpenNotaCreditoPdf(Long saleId) throws ServerException {
		Request request = new Request.Builder().url(ADDRESS + "/getNotaCreditoPdfBySaleId/" + saleId.toString()).build();

		try (Response response = client.newCall(request).execute()) {
			if (!response.isSuccessful()) {
				System.err.println("Error al descargar archivo: " + response.code() + " - " + response.message());
				throw new ServerException(response.body() != null ? response.body().string() : "No se pudo obtener el PDF de la Nota de Credito");
			}

			String disposition = response.header("Content-Disposition");
			String fileName = "archivo_descargado";
			if (disposition != null && disposition.contains("filename=")) {
				fileName = disposition.split("filename=")[1].replace("\"", "");
			}

			Path tempFile = Files.createTempFile("NC_", "_" + fileName);
			try (InputStream in = response.body().byteStream(); OutputStream out = Files.newOutputStream(tempFile)) {
				in.transferTo(out);
			}

			return tempFile;

		} catch (IOException e) {
			e.printStackTrace();
			throw new ServerException("No se pudo obtener el PDF de la Nota de Credito");
		}
	}

	public Path emitirNotaCredito(Long SaleId, BigDecimal monto, String motivo) throws ServerException {
		HttpUrl.Builder urlBuilder = Objects.requireNonNull(HttpUrl.parse(ADDRESS + "/nota-credito/bySaleId")).newBuilder();
		urlBuilder.addQueryParameter("SaleId", String.valueOf(SaleId));
		urlBuilder.addQueryParameter("monto", monto.toPlainString());
		urlBuilder.addQueryParameter("motivo", motivo);

		String finalUrl = urlBuilder.build().toString();

		Request request = new Request.Builder()
				.url(finalUrl)
				.post(RequestBody.create("", MediaType.parse("application/x-www-form-urlencoded")))
				.build();

		try (Response response = client.newCall(request).execute()) {
			if (!response.isSuccessful()) {
				System.err.println("Error al descargar archivo: " + response.code() + " - " + response.message());
				throw new ServerException(response.body() != null ? response.body().string() : "No se pudo emitir la nota de credito");
			}

			String disposition = response.header("Content-Disposition");
			String fileName = "archivo_descargado";
			if (disposition != null && disposition.contains("filename=")) {
				fileName = disposition.split("filename=")[1].replace("\"", "");
			}

			Path tempFile = Files.createTempFile("NC_", "_" + fileName);
			try (InputStream in = response.body().byteStream(); OutputStream out = Files.newOutputStream(tempFile)) {
				in.transferTo(out);
			}

			return tempFile;

		} catch (IOException e) {
			e.printStackTrace();
			throw new ServerException(e.getMessage());
		}
	}

	public boolean isNotaCreditoEmitida(Long SaleId) throws ServerException {
		HttpUrl.Builder urlBuilder = Objects.requireNonNull(HttpUrl.parse(ADDRESS + "/isNotaCreditoEmitida")).newBuilder();
		urlBuilder.addQueryParameter("SaleId", String.valueOf(SaleId));

		String finalUrl = urlBuilder.build().toString();

		Request request = new Request.Builder()
				.url(finalUrl)
				.get()
				.build();

		try (Response response = client.newCall(request).execute()) {
			if (!response.isSuccessful()) {
				throw new ServerException("Error al consultar nota de credito: Code " + response.code());
			}

			if (response.body() != null) {
				return Boolean.parseBoolean(response.body().string());
			}

			return false;

		} catch (IOException e) {
			e.printStackTrace();
			throw new ServerException("Error de conexion al verificar nota de credito: " + e.getMessage());
		}
	}

	public boolean isSaleFacturada(Long saleId) throws ServerException {
		HttpUrl.Builder urlBuilder = Objects.requireNonNull(HttpUrl.parse(ADDRESS + "/isSaleFacturada")).newBuilder();
		
		urlBuilder.addQueryParameter("idSale", String.valueOf(saleId));

		String finalUrl = urlBuilder.build().toString();

		Request request = new Request.Builder()
				.url(finalUrl)
				.get()
				.build();

		try (Response response = client.newCall(request).execute()) {
			if (!response.isSuccessful()) {
				// Manejo de error no 200 OK
				System.err.println("Error al verificar factura: " + response.code() + " - " + response.message());
				// Dependiendo de tu lógica, podrías retornar false o lanzar excepción.
				// Aquí lanzamos excepción para ser consistentes con el otro método.
				throw new ServerException("Error al consultar estado de facturación:Code " + response.code());
			}

			if (response.body() != null) {
				String responseBody = response.body().string();
				return Boolean.parseBoolean(responseBody);
			}
			
			return false;

		} catch (IOException e) {
			e.printStackTrace();
			throw new ServerException("Error de conexión al verificar factura: " + e.getMessage());
		}
	}
}
