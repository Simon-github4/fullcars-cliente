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
import model.client.entities.StockMovement;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ClienteRestStockMovement {

	private static final String ADDRESS = "http://localhost:8080/stockmovements";
	private final OkHttpClient client = new OkHttpClient();
    private final ObjectMapper mapper;

    public ClienteRestStockMovement() {
    	this.mapper = new ObjectMapper();
    	this.mapper.registerModule(new JavaTimeModule());
        this.mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }
	public List<StockMovement> getStockMovements() {
	    String uri = ADDRESS;

	    Request request = new Request.Builder()
	            .url(uri)
	            .get()
	            .build();

	    try (Response response = client.newCall(request).execute()) {
	        if (response.isSuccessful() && response.body() != null) {
	        	String json = response.body().string();
	            return mapper.readValue(json, new TypeReference<List<StockMovement>>() {});
	        } else {
	            System.err.println("Error HTTP: " + response.code());
	            return new ArrayList<>();
	        }
	    } catch (IOException e) {
	        e.printStackTrace(); // Manejo de error de conexión o de lectura
	        return new ArrayList<>();
	    }
	}
	
	public List<StockMovement> getStockMovements(LocalDate start, LocalDate end) {
		mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
		HttpUrl url = HttpUrl.parse(ADDRESS).newBuilder()
                .addQueryParameter("start", start.toString())
                .addQueryParameter("end", end.toString())	//Both dd/MM/yyyy
                .build();

        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();
        
		try (Response response = client.newCall(request).execute()) {
			if (!response.isSuccessful()) 
				throw new IOException("Unexpected code " + response);
			String jsonResponse = response.body().string();
			return mapper.readValue(jsonResponse, new TypeReference<List<StockMovement>>() {
			});
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public StockMovement getStockMovement(Long id) {
		String uri = ADDRESS +"/"+ URLEncoder.encode(id.toString(), StandardCharsets.UTF_8);

	    Request request = new Request.Builder()
	            .url(uri)
	            .get()
	            .build();

	    try (Response response = client.newCall(request).execute()) {
	        if (response.isSuccessful() && response.body() != null) {
	        	String json = response.body().string();
	            return mapper.readValue(json, new TypeReference<StockMovement>() {});
	        } else {
	            System.err.println("Error HTTP: " + response.code());
	            return null;
	        }
	    } catch (IOException e) {
	        e.printStackTrace();
	        return null;
	    }
	}
	
	public void save(StockMovement m) throws ServerException, IOException, Exception{
		try{
			String json = mapper.writeValueAsString(m);
	
			RequestBody body = RequestBody.create(json, MediaType.parse("application/json"));
			Request request = new Request.Builder().url(ADDRESS).post(body).build();
	
			try (Response response = client.newCall(request).execute()) {
				if (response.isSuccessful()) {
					System.out.println("StockMovement posted successfully");
				} else {
					String errorBody = response.body() != null ? response.body().string() : "Error inesperado";
	                System.err.println("Failed to post StockMovement. Code: " + response.code());
	                System.err.println("Response: " + errorBody);
	                throw new ServerException(errorBody);
				}
			}catch (IOException e) {
				e.printStackTrace();
				throw new IOException("No se pudo guardar el movimiento de stock, falla en conexion a servidor");
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
	                System.out.println("StockMovement deleted successfully (ID: " + id + ")");
	            } else {
					String errorBody = response.body() != null ? response.body().string() : "Error inesperado";
					System.err.println("Failed to delete Provider. Code: " + response.code());
	                System.err.println("Response: " + errorBody);
	                throw new ServerException(errorBody);
	            }
	        }catch (IOException e) {
				e.printStackTrace();
				throw new IOException("No se pudo eliminar el movimiento de stock, falla en conexion a servidor");
			}	
	}
	
}
