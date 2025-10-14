package model.client.entities;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ProviderMapping {
	
	private Long providerId;
	private String nameColumn;
	private String brandColumn;
	private String priceColumn;

	private String provCodColumn;
	private String qualityColumn;
	private String categoryColumn;
	
	private LocalDateTime lastUpdate;

}
