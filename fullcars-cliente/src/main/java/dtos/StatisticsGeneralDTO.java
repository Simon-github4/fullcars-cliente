package dtos;

import java.util.List;

import lombok.Getter;
import model.client.entities.CarPart;
import model.client.entities.Purchase;
import model.client.entities.Sale;

@Getter
public class StatisticsGeneralDTO {
	
	private long itemsRegistered;
	private long totalToCharge;
	private List<SalesData> salesData;
	private List<Purchase> purchases;
	private List<Sale> recentSales;
	private List<CarPart> topProducts;
	private List<CarPart> criticalStock;
	
	private List<Long> purchasesNotPayed;
	//private List<String> notifications;
	//private List<String> metricValues; // total ventas en el rango, compras total, ...

	public StatisticsGeneralDTO() {
		// TODO Auto-generated constructor stub
	}

	public StatisticsGeneralDTO(long itemsRegistered, long totalToCharge, List<SalesData> salesData,
			List<Purchase> purchases, List<Sale> recentSales, List<CarPart> topProducts, List<CarPart> criticalStock,
			List<Long> purchasesNotPayed) {
		super();
		this.itemsRegistered = itemsRegistered;
		this.totalToCharge = totalToCharge;
		this.salesData = salesData;
		this.purchases = purchases;
		this.recentSales = recentSales;
		this.topProducts = topProducts;
		this.criticalStock = criticalStock;
		this.purchasesNotPayed = purchasesNotPayed;
	}


}