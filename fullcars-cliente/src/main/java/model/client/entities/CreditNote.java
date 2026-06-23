package model.client.entities;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class CreditNote extends Comprobante {

	private String comprobanteAsociadoToPDF;//comprobanteAsociado;

	private String motivo;

	public String getComprobanteAsociadoToPDF() {
		return comprobanteAsociadoToPDF;
	}

	public String getMotivo() {
		return motivo;
	}

	public void setMotivo(String motivo) {
		this.motivo = motivo;
	}

}
