package data.service;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import Utils.ServerException;
import model.client.entities.CarPart;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ClienteRestCarPart {

	private static final String ADDRESS = "http://localhost:8080/carparts";
	private final OkHttpClient client = new OkHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();

	public List<CarPart> getCarParts() {
	    String uri = ADDRESS;// + "getMarcas?filterName=" + URLEncoder.encode(filterName, StandardCharsets.UTF_8);

	    Request request = new Request.Builder()
	            .url(uri)
	            .get()
	            .build();

	    try (Response response = client.newCall(request).execute()) {
	        if (response.isSuccessful() && response.body() != null) {
	        	String json = response.body().string();
	            return mapper.readValue(json, new TypeReference<List<CarPart>>() {});
	        } else {
	            System.err.println("Error HTTP: " + response.code());
	            return new ArrayList<>();
	        }
	    } catch (IOException e) {
	        e.printStackTrace(); // Manejo de error de conexión o de lectura
	        return new ArrayList<>();
	    }
	}

	public void save(CarPart carPart) throws ServerException, IOException, Exception {
		try{
			String json = mapper.writeValueAsString(carPart);
	
			RequestBody body = RequestBody.create(json, MediaType.parse("application/json"));
			Request request = new Request.Builder().url(ADDRESS).post(body).build();
	
			try (Response response = client.newCall(request).execute()) {
				if (response.isSuccessful()) {
					System.out.println("CarPart posted successfully");
				} else {
					String errorBody = response.body() != null ? response.body().string() : "Error inesperado";
					System.err.println("Failed to post CarPart. Code: " + response.code());
					System.err.println("Response: " + errorBody);
					throw new ServerException(errorBody);
				}
			} catch (IOException e) {
				e.printStackTrace();
				throw new IOException("No se pudo guardar, hubo una falla en conectar al servidor");
			}
		} catch (JsonProcessingException e) {
			e.printStackTrace();
			throw new Exception("No se pudo guardar");
		}
    }

	public void delete(Long id) throws ServerException, IOException{        
        Request request = new Request.Builder()
            .url(ADDRESS + "/" + id)
            .delete()
            .build();

        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful()) {
                System.out.println("CarPart deleted successfully (ID: " + id + ")");
            } else {
				String errorBody = response.body() != null ? response.body().string() : "Error inesperado";
				System.err.println("Failed to delete CarPart. Code: " + response.code());
                System.err.println("Response: " + errorBody);
                throw new ServerException(errorBody);
            }
        }catch (IOException e) {
			e.printStackTrace();
			throw new IOException("No se pudo eliminar el movimiento de stock, falla en conexion a servidor");
		}	
    }

	public CarPart getCarPart(Long id) {
		String uri = ADDRESS + "/"+ id;

	    Request request = new Request.Builder()
	            .url(uri)
	            .get()
	            .build();

	    try (Response response = client.newCall(request).execute()) {
	        if (response.isSuccessful() && response.body() != null) {
	        	String json = response.body().string();
	            return mapper.readValue(json, new TypeReference<CarPart>() {});
	        } else {
	            System.err.println("Error HTTP: " + response.code());
	            return new CarPart();
	        }
	    } catch (IOException e) {
	        e.printStackTrace(); // Manejo de error de conexión o de lectura
	        return new CarPart();
	    }
	}

	public CarPart getCarPart(String sku) {
		String uri = ADDRESS + "?sku=" + URLEncoder.encode(sku, StandardCharsets.UTF_8) ;

	    Request request = new Request.Builder()
	            .url(uri)
	            .get()
	            .build();

	    try (Response response = client.newCall(request).execute()) {
	        if (response.isSuccessful() && response.body() != null) {
	        	String json = response.body().string();
	            return mapper.readValue(json, new TypeReference<CarPart>() {});
	        } else {
	            System.err.println("Error HTTP: " + response.code());
	            return null;
	        }
	    } catch (IOException e) {
	        e.printStackTrace(); // Manejo de error de conexión o de lectura
            return null;
	    }	
	}

}
