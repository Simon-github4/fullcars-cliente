package model.client.entities;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class CreditNote extends Comprobante {

	private String comprobanteAsociadoToPDF;//comprobanteAsociado;

	public String getComprobanteAsociadoToPDF() {
		return comprobanteAsociadoToPDF;
	}

    //private CustomerCredit customerCredit;
    
/*    public Factura getComprobanteAsociado() {
        return comprobanteAsociado;
    }

    public void setComprobanteAsociado(Factura comprobanteAsociado) {
        this.comprobanteAsociado = comprobanteAsociado;
    }
	
	public CustomerCredit getCustomerCredit() {
        return customerCredit;
    }

    public void setCustomerCredit(CustomerCredit customerCredit) {
        this.customerCredit = customerCredit;
    }*/

}
