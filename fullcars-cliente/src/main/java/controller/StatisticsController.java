package controller;

import java.time.LocalDate;

import data.service.ClienteRestStatistics;
import dtos.StatisticsGeneralDTO;

public class StatisticsController {
	
	private final ClienteRestStatistics service = new ClienteRestStatistics();
	
	public StatisticsGeneralDTO getStatisticsGeneral(LocalDate[] dates) {
		StatisticsGeneralDTO dto = service.getStatisticsDTO(dates);
		
		return dto;
	}
	

}
