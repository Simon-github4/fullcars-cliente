package controller;

import java.io.IOException;
import java.util.List;

import Utils.ServerException;
import data.service.ClienteRestPayments;
import dtos.MultiPaymentRequest;
import dtos.MultiPaymentResponse;
import dtos.PendingSalesResponse;
import model.client.entities.Pay;

public class PayController {

	private final ClienteRestPayments payService = new ClienteRestPayments();
	
	public void delete(Long idPay) throws ServerException, IOException {
		payService.delete(idPay);
	}

	public PendingSalesResponse getPendingSales(Long customerId) throws ServerException, IOException {
		return payService.getPendingSales(customerId);
	}
	
	public MultiPaymentResponse createMultiPayment(MultiPaymentRequest request) throws ServerException, IOException {
		return payService.createMultiPayment(request);
	}

}
