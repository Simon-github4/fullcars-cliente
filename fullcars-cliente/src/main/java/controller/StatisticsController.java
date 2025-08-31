package controller;

import java.time.LocalDate;

import data.service.ClienteRestStatistics;
import dtos.StatisticsGeneralDTO;
import model.client.entities.StatisticsGeneral;

public class StatisticsController {
	
	private final ClienteRestStatistics service = new ClienteRestStatistics();
	
	public StatisticsGeneral getStatisticsGeneral(LocalDate[] dates) {
		StatisticsGeneralDTO dto = service.getStatisticsDTO(dates);
		
		return new StatisticsGeneral(
				dto.getItemsRegistered(), 
				dto.getTotalToCharge(),
				dto.getSalesData(),
				dto.getPurchases(),
				dto.getRecentSales(),
				dto.getTopProducts(),
				dto.getCriticalStock(),
				dto.getPurchasesNotPayed()
				);
	}
	

}
