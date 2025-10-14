package data.service;

import java.io.File;
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

import InitClass.Initializr;
import dtos.TaskStatusInfo;
import model.client.entities.CarPart;
import model.client.entities.Provider;
import model.client.entities.ProviderMapping;
import model.client.entities.ProviderPart;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ClienteRestProvider {
	
	private static final String ADDRESS = Initializr.getServerUrl()+"/providers";
	private final OkHttpClient client = new OkHttpClient();
    private final ObjectMapper mapper;

    public ClienteRestProvider() {
    	this.mapper = new ObjectMapper();
    	this.mapper.registerModule(new JavaTimeModule());
        this.mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }
    
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
	            System.err.println("Error HTTP: " + response.code() + response.body().toString());
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

	public List<ProviderPart> getProviderParts() {
	    String uri = ADDRESS + "/parts";

	    Request request = new Request.Builder()
	            .url(uri)
	            .get()
	            .build();

	    try (Response response = client.newCall(request).execute()) {
	        if (response.isSuccessful() && response.body() != null) {
	        	String json = response.body().string();
	            return mapper.readValue(json, new TypeReference<List<ProviderPart>>() {});
	        } else {
	            System.err.println("Error HTTP: " + response.code()+ response.body().toString());
	            return new ArrayList<>();
	        }
	    } catch (IOException e) {
	        e.printStackTrace(); // Manejo de error de conexión o de lectura
	        return new ArrayList<>();
	    }
	}
	
	public ProviderMapping getProviderMapping(Long idProvider) {
		String uri = ADDRESS +"/"+ idProvider.toString() + "/mapping";

	    Request request = new Request.Builder()
	            .url(uri)
	            .get()
	            .build();

	    try (Response response = client.newCall(request).execute()) {
	        if (response.isSuccessful() && response.body() != null) {
	        	String json = response.body().string();
	            return mapper.readValue(json, new TypeReference<ProviderMapping>() {});
	        } else {
	            System.err.println("Error HTTP: " + response.code());
	            return null;
	        }
	    } catch (IOException e) {
	        e.printStackTrace();
	        return null;
	    }	   
	}

	public String saveProviderMapping(ProviderMapping mapping, File excelFile) throws IOException {
		String mappingJson = mapper.writeValueAsString(mapping);

		MultipartBody.Builder multipartBuilder = new MultipartBody.Builder().setType(MultipartBody.FORM)
				.addFormDataPart("mapping", "mapping.json",
						RequestBody.create(mappingJson, MediaType.parse("application/json")));

		if (excelFile != null && excelFile.exists()) {
			multipartBuilder.addFormDataPart("archivoExcel", excelFile.getName(), RequestBody.create(excelFile,
					MediaType.parse("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")));
		}

		RequestBody requestBody = multipartBuilder.build();

		Request request = new Request.Builder().url(ADDRESS + "/" + mapping.getProviderId() + "/mapping")
				.post(requestBody).build();

		try (Response response = client.newCall(request).execute()) {
			if (!response.isSuccessful()) {
				throw new IOException("Error API: " + response.code() + " - " + response.message());
			}
			System.out.println("Mapping y archivo enviados correctamente");
			return response.body().string();
		}
	}

	public TaskStatusInfo getTaskStatus(String taskId) {
		String uri = ADDRESS +"/tasks/"+ taskId;

	    Request request = new Request.Builder()
	            .url(uri)
	            .get()
	            .build();

	    try (Response response = client.newCall(request).execute()) {
	        if (response.isSuccessful() && response.body() != null) {
	        	String json = response.body().string();
	            return mapper.readValue(json, new TypeReference<TaskStatusInfo>() {});
	        } else {
	            System.err.println("Error HTTP: " + response.code());
	            return null;
	        }
	    } catch (IOException e) {
	        e.printStackTrace();
	        return null;
	    }	  
	}

	public CarPart findOrCreateCarPartFromProviderPart(ProviderPart provPart)  {
		String uri = ADDRESS +"/from-provider";
		String json = null;
		try {
			json = mapper.writeValueAsString(provPart);
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		RequestBody body = RequestBody.create(json, MediaType.parse("application/json"));
		Request request = new Request.Builder().url(uri).post(body).build();

	    try (Response response = client.newCall(request).execute()) {
	        if (response.isSuccessful() && response.body() != null) {
	        	String jsonResponse = response.body().string();
	            return mapper.readValue(jsonResponse, new TypeReference<CarPart>() {});
	        } else {
	            System.err.println("Error HTTP: " + response.code()+ response.body().string());
	            return null;
	        }
	    } catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

}
