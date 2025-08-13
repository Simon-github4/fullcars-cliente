package data.service;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import Utils.ServerException;
import dtos.CustomerSummaryDTO;
import model.client.entities.Customer;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import views.CustomerSummaryHistory;

public class ClienteRestCustomer {
	
	private static final String ADDRESS = "http://localhost:8080/customers";
	private final OkHttpClient client = new OkHttpClient();
    private final ObjectMapper mapper;

    public ClienteRestCustomer() {
    	this.mapper = new ObjectMapper();
    	this.mapper.registerModule(new JavaTimeModule());
        this.mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }
    
	public List<Customer> getCustomers() {
	    String uri = ADDRESS;

	    Request request = new Request.Builder()
	            .url(uri)
	            .get()
	            .build();

	    try (Response response = client.newCall(request).execute()) {
	        if (response.isSuccessful() && response.body() != null) {
	        	String json = response.body().string();
	            return mapper.readValue(json, new TypeReference<List<Customer>>() {});
	        } else {
	            System.err.println("Error HTTP: " + response.code());
	            return new ArrayList<>();
	        }
	    } catch (IOException e) {
	        e.printStackTrace(); // Manejo de error de conexión o de lectura
	        return new ArrayList<>();
	    }
	}
	
	public Customer getCustomer(Long id) {
		String uri = ADDRESS +"/"+ URLEncoder.encode(id.toString(), StandardCharsets.UTF_8);

	    Request request = new Request.Builder()
	            .url(uri)
	            .get()
	            .build();

	    try (Response response = client.newCall(request).execute()) {
	        if (response.isSuccessful() && response.body() != null) {
	        	String json = response.body().string();
	            return mapper.readValue(json, new TypeReference<Customer>() {});
	        } else {
	            System.err.println("Error HTTP: " + response.code());
	            return null;
	        }
	    } catch (IOException e) {
	        e.printStackTrace();
	        return null;
	    }
	}

	public Customer getCustomer(String dni) {
		String uri = ADDRESS +"?dni=" + URLEncoder.encode(dni, StandardCharsets.UTF_8);

	    Request request = new Request.Builder()
	            .url(uri)
	            .get()
	            .build();

	    try (Response response = client.newCall(request).execute()) {
	        if (response.isSuccessful() && response.body() != null) {
	        	String json = response.body().string();
	            return mapper.readValue(json, new TypeReference<Customer>() {});
	        } else {
	            System.err.println("Error HTTP: " + response.code());
	            return null;
	        }
	    } catch (IOException e) {
	        e.printStackTrace();
	        return null;
	    }
	}
	
	public boolean save(Customer m) {
		try{
			String json = mapper.writeValueAsString(m);
	
			RequestBody body = RequestBody.create(json, MediaType.parse("application/json"));
			Request request = new Request.Builder().url(ADDRESS).post(body).build();
	
			try (Response response = client.newCall(request).execute()) {
				if (response.isSuccessful()) {
					System.out.println("Customer posted successfully");
					return true;
				} else {
					System.err.println("Failed to post Customer. Code: " + response.code());
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

	public void delete(Long id) throws IOException, ServerException {
		Request request = new Request.Builder()
	            .url(ADDRESS + "/" + id)
	            .delete()
	            .build();
		
		try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful()) {
                System.out.println("Customer deleted successfully (ID: " + id + ")");
            } else {
				String errorBody = response.body() != null ? response.body().string() : "Error inesperado";
				System.err.println("Failed to delete Customer. Code: " + response.code());
                System.err.println("Response: " + errorBody);
                throw new ServerException(errorBody);
            }
        }catch (IOException e) {
			e.printStackTrace();
			throw new IOException("No se pudo eliminar el cliente, falla en conexion a servidor");
		}	
	}

	
	public CustomerSummaryDTO getCustomerSummary(Long id) {
		String uri = ADDRESS +"/"+ URLEncoder.encode(id.toString(), StandardCharsets.UTF_8)+"/summary";

	    Request request = new Request.Builder()
	            .url(uri)
	            .get()
	            .build();

	    try (Response response = client.newCall(request).execute()) {
	        if (response.isSuccessful() && response.body() != null) {
	        	String json = response.body().string();
	            return mapper.readValue(json, new TypeReference<CustomerSummaryDTO>() {});
	        } else {
	            System.err.println("Error HTTP: " + response.code());
	            return null;
	        }
	    } catch (IOException e) {
	        e.printStackTrace();
	        return null;
	    }
	}
	
}
