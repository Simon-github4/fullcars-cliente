package controller;

import java.io.IOException;
import java.util.List;

import Utils.ServerException;
import data.service.ClienteRestPayments;
import model.client.entities.Pay;

public class PayController {

	private final ClienteRestPayments payService = new ClienteRestPayments();
	
	public List<Pay> getPays() {
		return payService.getPays();
	}

	public void delete(Long idPay) throws ServerException, IOException {
		payService.delete(idPay);
	}

	
	public void save(Pay pay) throws ServerException, IOException, Exception{
		payService.save(pay);
	}

}
