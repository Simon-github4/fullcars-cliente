package data.service;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import model.client.entities.Customer;
import model.client.entities.Provider;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ClienteRestProvider {
	
	private static final String ADDRESS = "http://localhost:8080/providers";
	private final OkHttpClient client = new OkHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();

	public List<Provider> getProviders() {
	    String uri = ADDRESS;

	    Request request = new Request.Builder()
	            .url(uri)
	            .get()
	            .build();

	    try (Response response = client.newCall(request).execute()) {
	        if (response.isSuccessful() && response.body() != null) {
	        	String json = response.body().string();
	            return mapper.readValue(json, new TypeReference<List<Provider>>() {});
	        } else {
	            System.err.println("Error HTTP: " + response.code());
	            return new ArrayList<>();
	        }
	    } catch (IOException e) {
	        e.printStackTrace(); // Manejo de error de conexión o de lectura
	        return new ArrayList<>();
	    }
	}
	
	public Provider getProvider(Long id) {
		String uri = ADDRESS +"/"+ URLEncoder.encode(id.toString(), StandardCharsets.UTF_8);

	    Request request = new Request.Builder()
	            .url(uri)
	            .get()
	            .build();

	    try (Response response = client.newCall(request).execute()) {
	        if (response.isSuccessful() && response.body() != null) {
	        	String json = response.body().string();
	            return mapper.readValue(json, new TypeReference<Provider>() {});
	        } else {
	            System.err.println("Error HTTP: " + response.code());
	            return null;
	        }
	    } catch (IOException e) {
	        e.printStackTrace();
	        return null;
	    }
	}
	
	public boolean save(Provider m) {
		try{
			String json = mapper.writeValueAsString(m);
	
			RequestBody body = RequestBody.create(json, MediaType.parse("application/json"));
			Request request = new Request.Builder().url(ADDRESS).post(body).build();
	
			try (Response response = client.newCall(request).execute()) {
				if (response.isSuccessful()) {
					System.out.println("Provider posted successfully");
					return true;
				} else {
					System.err.println("Failed to post Provider. Code: " + response.code());
					System.err.println("Response: " + response.body().string());
					return false;
				}
			} catch (IOException e) {
				e.printStackTrace();
				return false;
			}
		} catch (JsonProcessingException e) {
			e.printStackTrace();
			return false;
		}
	}

	public void delete(Long id) throws IOException {
		Request request = new Request.Builder()
	            .url(ADDRESS + "/" + id)
	            .delete()
	            .build();

	        try (Response response = client.newCall(request).execute()) {
	            if (response.isSuccessful()) {
	                System.out.println("Provider deleted successfully (ID: " + id + ")");
	            } else {
	                System.err.println("Failed to delete Provider. Code: " + response.code());
	                System.err.println("Response: " + response.body().string());
	            }
	        } 
	}
	
}
