package controller;

import java.io.IOException;
import java.util.List;

import Utils.ServerException;
import data.service.ClienteRestCustomer;
import dtos.CustomerSummaryDTO;
import lombok.Getter;
import lombok.Setter;
import model.client.entities.Customer;
import views.CustomerSummaryHistory;

public class CustomerController {

private final ClienteRestCustomer serviceCustomer = new ClienteRestCustomer();
	@Getter
	@Setter
	private Long customerSelectedId;
	
	public Customer getCustomer(Long id){
		return serviceCustomer.getCustomer(id);
	}

	public Customer getCustomer(String dni) {
		return serviceCustomer.getCustomer(dni);
	}
	
	public List<Customer> getCustomers(){
		return serviceCustomer.getCustomers();
	}

	public boolean save(Customer c) {
		return serviceCustomer.save(c);
	}

	public void delete(Long id) throws IOException, ServerException {
		serviceCustomer.delete(id);
	}
	
	public CustomerSummaryDTO getCustomerSummary(Long id){
		return serviceCustomer.getCustomerSummary(id);
	}

}
