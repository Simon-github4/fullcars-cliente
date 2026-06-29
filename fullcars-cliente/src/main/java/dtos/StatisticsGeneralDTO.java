package dtos;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import model.client.entities.CarPart;
import model.client.entities.Purchase;
import model.client.entities.Sale;

@Getter
public class StatisticsGeneralDTO {
	
	private long itemsRegistered;
	private BigDecimal totalToCharge;
	private List<SalesData> salesData;
	private List<Purchase> purchases;
	private List<RecentSaleDTO> recentSales;
	private List<TopProductDTO> topProducts;
	private List<CarPart> criticalStock;
	
	private List<Long> purchasesNotPayed;
	//private List<String> notifications;
	//private List<String> metricValues; // total ventas en el rango, compras total, ...

	public StatisticsGeneralDTO() {
		// TODO Auto-generated constructor stub
	}

	public StatisticsGeneralDTO(long itemsRegistered, BigDecimal totalToCharge, List<SalesData> salesData,
			List<Purchase> purchases, List<RecentSaleDTO> recentSales, List<TopProductDTO> topProducts, List<CarPart> criticalStock,
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

	@Getter
	public static class RecentSaleDTO {

	    private String customerName;
	    private String id;
	    private BigDecimal total;
	    private LocalDate date;
	}
}