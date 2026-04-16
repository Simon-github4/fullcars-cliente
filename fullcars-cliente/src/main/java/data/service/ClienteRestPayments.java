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
import dtos.MultiPaymentRequest;
import dtos.MultiPaymentResponse;
import dtos.PendingSalesResponse;
import model.client.entities.Pay;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ClienteRestPayments {

	private static final String ADDRESS = Initializr.getServerUrl()+"/payments";
	private final OkHttpClient client = new OkHttpClient();
    private final ObjectMapper mapper;

    public ClienteRestPayments() {
    	this.mapper = new ObjectMapper();
    	this.mapper.registerModule(new JavaTimeModule());
        this.mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
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

	public PendingSalesResponse getPendingSales(Long customerId) throws ServerException, IOException {
		Request request = new Request.Builder()
	            .url(ADDRESS + "/customers/" + customerId + "/pending")
	            .get()
	            .build();

	    try (Response response = client.newCall(request).execute()) {
	        if (response.isSuccessful() && response.body() != null) {
	        	String json = response.body().string();
	            return mapper.readValue(json, PendingSalesResponse.class);
	        } else {
				String errorBody = response.body() != null ? response.body().string() : "Error inesperado";
				System.err.println("Failed to get pending sales. Code: " + response.code());
	            System.err.println("Response: " + errorBody);
	            throw new ServerException(errorBody);
	        }
	    } catch (IOException e) {
			e.printStackTrace();
			throw new IOException("No se pudo obtener las ventas pendientes, falla en conexion a servidor");
		}
	}
	
	public MultiPaymentResponse createMultiPayment(MultiPaymentRequest request) throws ServerException, IOException {
		try {
			String json = mapper.writeValueAsString(request);
	
			RequestBody body = RequestBody.create(json, MediaType.parse("application/json"));
			Request httpRequest = new Request.Builder()
					.url(ADDRESS + "/multi")
					.post(body)
					.build();
	
			try (Response response = client.newCall(httpRequest).execute()) {
				if (response.isSuccessful() && response.body() != null) {
					String responseJson = response.body().string();
					return mapper.readValue(responseJson, MultiPaymentResponse.class);
				} else {
					String errorBody = response.body() != null ? response.body().string() : "Error inesperado";
	                System.err.println("Failed to create multi payment. Code: " + response.code());
	                System.err.println("Response: " + errorBody);
	                throw new ServerException(errorBody);
				}
			}
		} catch (JsonProcessingException e) {
			e.printStackTrace();
			throw new IOException("No se pudo procesar la respuesta del servidor");
		}
	}
	
	public List<MultiPaymentResponse> getPaymentsByCustomer(Long customerId) throws ServerException, IOException {
		Request request = new Request.Builder()
	            .url(ADDRESS + "/customer/" + customerId)
	            .get()
	            .build();

	    try (Response response = client.newCall(request).execute()) {
	        if (response.isSuccessful() && response.body() != null) {
	        	String json = response.body().string();
	            return mapper.readValue(json, new TypeReference<List<MultiPaymentResponse>>() {});
	        } else {
				String errorBody = response.body() != null ? response.body().string() : "Error inesperado";
				System.err.println("Failed to get payments by customer. Code: " + response.code());
	            System.err.println("Response: " + errorBody);
	            throw new ServerException(errorBody);
	        }
	    } catch (IOException e) {
			e.printStackTrace();
			throw new IOException("No se pudo obtener los pagos del cliente, falla en conexion a servidor");
		}
	}
	
	public MultiPaymentResponse getPaymentDetail(Long payId) throws ServerException, IOException {
		Request request = new Request.Builder()
	            .url(ADDRESS + "/" + payId + "/detail")
	            .get()
	            .build();

	    try (Response response = client.newCall(request).execute()) {
	        if (response.isSuccessful() && response.body() != null) {
	        	String json = response.body().string();
	            return mapper.readValue(json, MultiPaymentResponse.class);
	        } else {
				String errorBody = response.body() != null ? response.body().string() : "Error inesperado";
				System.err.println("Failed to get payment detail. Code: " + response.code());
	            System.err.println("Response: " + errorBody);
	            throw new ServerException(errorBody);
	        }
	    } catch (IOException e) {
			e.printStackTrace();
			throw new IOException("No se pudo obtener el detalle del pago, falla en conexion a servidor");
		}
	}
	
}
