package data.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import InitClass.Initializr;
import Utils.ServerException;
import model.client.entities.Pay;
import model.client.entities.StockMovement;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ClienteRestPayments {

	private static final String ADDRESS = "http://"+Initializr.getIpAndPort()+"/payments";
	private final OkHttpClient client = new OkHttpClient();
    private final ObjectMapper mapper;

    public ClienteRestPayments() {
    	this.mapper = new ObjectMapper();
    	this.mapper.registerModule(new JavaTimeModule());
        this.mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }
    
	public List<Pay> getPays() {
	    String uri = ADDRESS;

	    Request request = new Request.Builder()
	            .url(uri)
	            .get()
	            .build();

	    try (Response response = client.newCall(request).execute()) {
	        if (response.isSuccessful() && response.body() != null) {
	        	String json = response.body().string();
	            return mapper.readValue(json, new TypeReference<List<Pay>>() {});
	        } else {
	            System.err.println("Error HTTP: " + response.code());
	            return new ArrayList<>();
	        }
	    } catch (IOException e) {
	        e.printStackTrace(); // Manejo de error de conexión o de lectura
	        return new ArrayList<>();
	    }
	}

	public void delete(Long idPay)  throws ServerException, IOException {
		Request request = new Request.Builder()
	            .url(ADDRESS + "/" + idPay)
	            .delete()
	            .build();

	        try (Response response = client.newCall(request).execute()) {
	            if (response.isSuccessful()) {
	                System.out.println("Pay deleted successfully (ID: " + idPay + ")");
	            } else {
					String errorBody = response.body() != null ? response.body().string() : "Error inesperado";
					System.err.println("Failed to delete Pay. Code: " + response.code());
	                System.err.println("Response: " + errorBody);
	                throw new ServerException(errorBody);
	            }
	        }catch (IOException e) {
				e.printStackTrace();
				throw new IOException("No se pudo eliminar el pago, falla en conexion a servidor");
			}	
	}

	
	public void save(Pay pay) throws ServerException, IOException, Exception{
		try{
			String json = mapper.writeValueAsString(pay);
	
			RequestBody body = RequestBody.create(json, MediaType.parse("application/json"));
			Request request = new Request.Builder().url(ADDRESS).post(body).build();
	
			try (Response response = client.newCall(request).execute()) {
				if (response.isSuccessful()) {
					System.out.println("Pay posted successfully");
				} else {
					String errorBody = response.body() != null ? response.body().string() : "Error inesperado";
	                System.err.println("Failed to post Pay. Code: " + response.code());
	                System.err.println("Response: " + errorBody);
	                throw new ServerException(errorBody);
				}
			}catch (IOException e) {
				e.printStackTrace();
				throw new IOException("No se pudo guardar el Pago, falla en conexion a servidor");
			}
		} catch (JsonProcessingException e) {
			e.printStackTrace();
			throw new Exception("No se pudo guardar");
		}
	}
	
}
