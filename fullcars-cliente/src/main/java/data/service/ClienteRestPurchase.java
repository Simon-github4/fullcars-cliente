package data.service;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import Utils.ServerException;
import dtos.ConfirmPurchasePayDTO;
import model.client.entities.Purchase;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ClienteRestPurchase {

	private static final String ADDRESS = "http://localhost:8080/purchases";
	private final OkHttpClient client = new OkHttpClient();
    private final ObjectMapper mapper;

    public ClienteRestPurchase() {
    	this.mapper = new ObjectMapper();
    	this.mapper.registerModule(new JavaTimeModule());
        this.mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }
	public List<Purchase> getPurchases() {
	    String uri = ADDRESS;

	    Request request = new Request.Builder()
	            .url(uri)
	            .get()
	            .build();

	    try (Response response = client.newCall(request).execute()) {
	        if (response.isSuccessful() && response.body() != null) {
	        	String json = response.body().string();
	            return mapper.readValue(json, new TypeReference<List<Purchase>>() {});
	        } else {
	            System.err.println("Error HTTP: " + response.code());
	            return new ArrayList<>();
	        }
	    } catch (IOException e) {
	        e.printStackTrace(); 
	        return new ArrayList<>();
	    }
	}
	
	public List<Purchase> getPurchases(LocalDate[] dates, Long idProvider) {
		HttpUrl.Builder urlBuilder = HttpUrl.parse(ADDRESS + "/filters").newBuilder();

        if (dates != null) {
            urlBuilder.addQueryParameter("start", dates[0].toString());
            urlBuilder.addQueryParameter("end", dates[1].toString());
        }
        if (idProvider != null) 
            urlBuilder.addQueryParameter("idProvider", idProvider.toString());
        
        HttpUrl url = urlBuilder.build();
		Request request = new Request.Builder()
                .url(url)
                .get()
                .build();
        
		try (Response response = client.newCall(request).execute()) {
			if (!response.isSuccessful()) 
				throw new IOException("Unexpected code " + response);
			String jsonResponse = response.body().string();
			return mapper.readValue(jsonResponse, new TypeReference<List<Purchase>>() {});
		} catch (IOException e) {
			e.printStackTrace();
			return new ArrayList<>();
		}
	}
	
	public Purchase getPurchase(Long id) {
		String uri = ADDRESS +"/"+ URLEncoder.encode(id.toString(), StandardCharsets.UTF_8);

	    Request request = new Request.Builder()
	            .url(uri)
	            .get()
	            .build();

	    try (Response response = client.newCall(request).execute()) {
	        if (response.isSuccessful() && response.body() != null) {
	        	String json = response.body().string();
	            return mapper.readValue(json, new TypeReference<Purchase>() {});
	        } else {
	            System.err.println("Error HTTP: " + response.code());
	            return null;
	        }
	    } catch (IOException e) {
	        e.printStackTrace();
	        return null;
	    }
	}
	
	public void save(Purchase m) throws ServerException, IOException, Exception{
		try{
			m.getDetails().forEach(d -> d.setPurchase(null));// Avoid loop json

			String json = mapper.writeValueAsString(m);
	
			String url =  ADDRESS +"/"+ URLEncoder.encode(m.getProvider().getId().toString(), StandardCharsets.UTF_8);
			RequestBody body = RequestBody.create(json, MediaType.parse("application/json"));
			Request request = new Request.Builder().url(url).post(body).build();
	
			try (Response response = client.newCall(request).execute()) {
				if (response.isSuccessful()) {
					System.out.println("Purchase posted successfully");
				} else {
					String errorBody = response.body() != null ? response.body().string() : "Error inesperado";
	                System.err.println("Failed to post Purchase. Code: " + response.code());
	                System.err.println("Response: " + errorBody);
	                throw new ServerException(errorBody);
				}
			}catch (IOException e) {
				e.printStackTrace();
				throw new IOException("No se pudo guardar la compra, falla en conexion a servidor");
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
	                System.out.println("Purchase deleted successfully (ID: " + id + ")");
	            } else {
					String errorBody = response.body() != null ? response.body().string() : "Error inesperado";
					System.err.println("Failed to delete Purchase. Code: " + response.code());
	                System.err.println("Response: " + errorBody);
	                throw new ServerException(errorBody);
	            }
	        }catch (IOException e) {
				e.printStackTrace();
				throw new IOException("No se pudo eliminar la compra, falla en conexion a servidor");
			}	
	}
	
	public void uploadFile(Long purchaseId, File file) throws IOException, ServerException {
        RequestBody fileBody = RequestBody.create(file, MediaType.parse("application/octet-stream"));
        MultipartBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", file.getName(), fileBody)
                .build();

        Request request = new Request.Builder()
                .url(ADDRESS +"/"+ purchaseId + "/uploadBill")
                .post(requestBody)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful()) {
                System.out.println("Archivo enviado con éxito: " + response.body().string());
            } else {
            	System.err.println("Error en la subida: " + response.code() + " - " + response.message());
            	throw new ServerException("No se pudo guardar el Archivo en el servidor");
            }
        }
	}

	public void downloadAndOpenFile(Long purchaseId) {
		Request request = new Request.Builder()
	        .url(ADDRESS+"/"+purchaseId.toString()+"/getBill")
	        .build();

		try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                System.err.println("Error al descargar archivo: " + response.code() + " - " + response.message());
                return;
            }

            String disposition = response.header("Content-Disposition");
            String fileName = "archivo_descargado";
            if (disposition != null && disposition.contains("filename=")) {
                fileName = disposition.split("filename=")[1].replace("\"", "");
            }

            Path tempFile = Files.createTempFile("bill_", "_" + fileName);
            try (InputStream in = response.body().byteStream();
                 OutputStream out = Files.newOutputStream(tempFile)) {
                in.transferTo(out);
            }

            if (Desktop.isDesktopSupported()) 
                Desktop.getDesktop().open(tempFile.toFile());
            else 
                System.out.println("Archivo descargado en: " + tempFile.toAbsolutePath());
            
        } catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void confirmPay(Long purchaseId, ConfirmPurchasePayDTO confirmPurchasePayDTO) throws Exception {
		try {
			String uri = ADDRESS +"/"+ URLEncoder.encode(purchaseId.toString(), StandardCharsets.UTF_8)+"/confirmPay";
			String json = mapper.writeValueAsString(confirmPurchasePayDTO);
			RequestBody body = RequestBody.create(json, MediaType.parse("application/json"));
	
		    Request request = new Request.Builder()
		            .url(uri)
		            .patch(body)
		            .build();
	
		    try (Response response = client.newCall(request).execute()) {
		        if (response.isSuccessful() && response.body() != null) {
		        	 System.out.println(response.body().string());
		        } else {
		            System.err.println("Error HTTP: " + response.code());
		            throw new Exception("No se pudo confirmar");
		        }
		    } catch (IOException e) {
		        e.printStackTrace();
		    }	
		} catch (JsonProcessingException e) {
			e.printStackTrace();
			throw new Exception("No se pudo confirmar");
		}
	}
}
