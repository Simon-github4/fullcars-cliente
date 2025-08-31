package data.service;

import java.io.IOException;
import java.time.LocalDate;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import InitClass.Initializr;
import dtos.StatisticsGeneralDTO;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class ClienteRestStatistics {

	private static final String ADDRESS = Initializr.getServerUrl()+"/statistics";
	private final OkHttpClient client = new OkHttpClient();
    private final ObjectMapper mapper;

    public ClienteRestStatistics() {
    	this.mapper = new ObjectMapper();
    	this.mapper.registerModule(new JavaTimeModule());
        this.mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }
    
	public StatisticsGeneralDTO getStatisticsDTO(LocalDate[] dates) {
		HttpUrl.Builder urlBuilder = HttpUrl.parse(ADDRESS).newBuilder();

        if (dates != null) {
            urlBuilder.addQueryParameter("start", dates[0].toString());
            urlBuilder.addQueryParameter("end", dates[1].toString());
        }
        
        HttpUrl url = urlBuilder.build();
		Request request = new Request.Builder()
                .url(url)
                .get()
                .build();
        
		try (Response response = client.newCall(request).execute()) {
			if (!response.isSuccessful()) 
				throw new IOException("Unexpected code " + response);
			String jsonResponse = response.body().string();
			return mapper.readValue(jsonResponse, new TypeReference<StatisticsGeneralDTO>() {});
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
	
}
