package data.service;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import Utils.ServerException;
import model.client.entities.Sale;
import model.client.entities.StockMovement;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ClienteRestSale {

	private static final String ADDRESS = "http://localhost:8080/sales";
	private final OkHttpClient client = new OkHttpClient();
    private final ObjectMapper mapper;

    public ClienteRestSale() {
    	this.mapper = new ObjectMapper();
    	this.mapper.registerModule(new JavaTimeModule());
        this.mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }
	public List<Sale> getSales() {
	    String uri = ADDRESS;

	    Request request = new Request.Builder()
	            .url(uri)
	            .get()
	            .build();

	    try (Response response = client.newCall(request).execute()) {
	        if (response.isSuccessful() && response.body() != null) {
	        	String json = response.body().string();
	            return mapper.readValue(json, new TypeReference<List<Sale>>() {});
	        } else {
	            System.err.println("Error HTTP: " + response.code());
	            return new ArrayList<>();
	        }
	    } catch (IOException e) {
	        e.printStackTrace(); // Manejo de error de conexión o de lectura
	        return new ArrayList<>();
	    }
	}
	
	public List<Sale> getSales(LocalDate[] dates, Long idCustomer) {
		HttpUrl.Builder urlBuilder = HttpUrl.parse(ADDRESS + "/filters").newBuilder();

        if (dates != null) {
            urlBuilder.addQueryParameter("start", dates[0].toString());
            urlBuilder.addQueryParameter("end", dates[1].toString());
        }
        if (idCustomer != null) 
            urlBuilder.addQueryParameter("idCustomer", idCustomer.toString());
        
        HttpUrl url = urlBuilder.build();
		Request request = new Request.Builder()
                .url(url)
                .get()
                .build();
        
		try (Response response = client.newCall(request).execute()) {
			if (!response.isSuccessful()) 
				throw new IOException("Unexpected code " + response);
			String jsonResponse = response.body().string();
			return mapper.readValue(jsonResponse, new TypeReference<List<Sale>>() {});
		} catch (IOException e) {
			e.printStackTrace();
			return new ArrayList<>();
		}
	}
	
	public Sale getSale(Long id) {
		String uri = ADDRESS +"/"+ URLEncoder.encode(id.toString(), StandardCharsets.UTF_8);

	    Request request = new Request.Builder()
	            .url(uri)
	            .get()
	            .build();

	    try (Response response = client.newCall(request).execute()) {
	        if (response.isSuccessful() && response.body() != null) {
	        	String json = response.body().string();
	            return mapper.readValue(json, new TypeReference<Sale>() {});
	        } else {
	            System.err.println("Error HTTP: " + response.code());
	            return null;
	        }
	    } catch (IOException e) {
	        e.printStackTrace();
	        return null;
	    }
	}
	
	public void save(Sale m) throws ServerException, IOException, Exception{
		try{
			m.getDetails().forEach(d -> d.setSale(null));

			String json = mapper.writeValueAsString(m);
	
			String url =  ADDRESS +"/"+ URLEncoder.encode(m.getCustomer().getId().toString(), StandardCharsets.UTF_8);
			RequestBody body = RequestBody.create(json, MediaType.parse("application/json"));
			Request request = new Request.Builder().url(url).post(body).build();
	
			try (Response response = client.newCall(request).execute()) {
				if (response.isSuccessful()) {
					System.out.println("Sale posted successfully");
				} else {
					String errorBody = response.body() != null ? response.body().string() : "Error inesperado";
	                System.err.println("Failed to post Sale. Code: " + response.code());
	                System.err.println("Response: " + errorBody);
	                throw new ServerException(errorBody);
				}
			}catch (IOException e) {
				e.printStackTrace();
				throw new IOException("No se pudo guardar la venta, falla en conexion a servidor");
			}
		} catch (JsonProcessingException e) {
			e.printStackTrace();
			throw new Exception("No se pudo guardar");
		}
	}

	public void delete(Long id) throws ServerException, IOException {
		Request request = new Request.Builder()
	            .url(ADDRESS + "/" + id)
	            .delete()
	            .build();

	        try (Response response = client.newCall(request).execute()) {
	            if (response.isSuccessful()) {
	                System.out.println("Sale deleted successfully (ID: " + id + ")");
	            } else {
					String errorBody = response.body() != null ? response.body().string() : "Error inesperado";
					System.err.println("Failed to delete Sale. Code: " + response.code());
	                System.err.println("Response: " + errorBody);
	                throw new ServerException(errorBody);
	            }
	        }catch (IOException e) {
				e.printStackTrace();
				throw new IOException("No se pudo eliminar la venta, falla en conexion a servidor");
			}	
	}
	
}
