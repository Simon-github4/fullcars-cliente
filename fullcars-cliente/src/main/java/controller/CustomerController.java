package controller;

import java.io.IOException;
import java.util.List;

import data.service.ClienteRestCustomer;
import model.client.entities.Customer;

public class CustomerController {

private final ClienteRestCustomer serviceCustomer = new ClienteRestCustomer();
	
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

	public void delete(Long id) throws IOException  {
		try {
			serviceCustomer.delete(id);
		} catch (IOException e) {
			e.printStackTrace();
			throw new IOException("No se pudo eliminar el cliente");
		}	
	}
	
}
