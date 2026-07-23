package controller;

import java.awt.BorderLayout;
import java.awt.Desktop;
import java.awt.Frame;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.util.concurrent.ExecutionException;

import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;

import Utils.ServerException;
import data.service.ClienteRestFactura;
import model.client.entities.CreditNote;
import model.client.entities.Factura;
import views.transactions.DialogFactura;
import views.transactions.DialogFacturaShowData;
import views.transactions.DialogNotaCredito;
import views.transactions.DialogNotaCreditoShowData;

public class FacturaController {

	private final ClienteRestFactura service = new ClienteRestFactura();
	
	public void facturar(Long saleId) throws ServerException, Exception {
		if (service.isSaleFacturada(saleId)) 
			throw new Exception("La venta ya fue facturada.");

		DialogFactura dialogo = new DialogFactura(null);
		dialogo.setVisible(true);

		Integer tipoFactura = dialogo.getTipoFacturaSeleccionado();

		if (tipoFactura == null)
			return;

		JDialog loadingDialog = crearDialogoCarga("Facturando", "Generando comprobante en AFIP...");

		SwingWorker<Path, Void> worker = new SwingWorker<>() {
			@Override
			protected Path doInBackground() throws Exception {
				return service.facturar(saleId, tipoFactura);
			}

			@Override
			protected void done() {
				loadingDialog.dispose();

				try {
					Path tempFile = get();

					if (Desktop.isDesktopSupported()) 
						try {
							Desktop.getDesktop().open(tempFile.toFile());
						} catch (IOException e) {
							JOptionPane.showMessageDialog(null, "No se pudo abrir el archivo.", "Error", JOptionPane.ERROR_MESSAGE);
							e.printStackTrace();
						}
					 else 
						JOptionPane.showMessageDialog(null, "Archivo guardado en: " + tempFile.toAbsolutePath());
					

				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
				} catch (ExecutionException e) {
					// Aquí capturamos las excepciones que lanzaste en el service (ServerException,etc)
					Throwable causa = e.getCause();
					JOptionPane.showMessageDialog(null, "Error al facturar: " + causa.getMessage(), "Error",
							JOptionPane.ERROR_MESSAGE);
					causa.printStackTrace();
				}
			}
		};

		worker.execute();
		loadingDialog.setVisible(true); // Esto bloquea la interacción hasta que se cierre en done()
	}

	public void emitirNotaCredito(Long saleId) throws Exception {
		if (service.isNotaCreditoEmitida(saleId)) {
			CreditNote nc = service.getNotaCreditoBySaleId(saleId);
			if (nc != null) {
				new DialogNotaCreditoShowData(null, nc, service, saleId);
			} else {
				JOptionPane.showMessageDialog(null, "No se encontraron los datos de la nota de credito.",
						"Error", JOptionPane.ERROR_MESSAGE);
			}
			return;
		}

		Factura factura = service.getFacturaBySaleId(saleId);
		if (factura == null)
			throw new Exception("No se encontro la factura para esta venta.");

		DialogNotaCredito dialogo = new DialogNotaCredito(null, factura.getImpTotal());
		dialogo.setVisible(true);
		BigDecimal monto = dialogo.getMonto();
		if (monto == null)
			return;
		String motivo = dialogo.getMotivo();

		JDialog loadingDialog = crearDialogoCarga("Emitiendo Nota de Credito", "Generando comprobante en ARCA...");

		SwingWorker<Path, Void> worker = new SwingWorker<>() {
			@Override
			protected Path doInBackground() throws Exception {
				return service.emitirNotaCredito(saleId, monto, motivo);
			}

			@Override
			protected void done() {
				loadingDialog.dispose();
				try {
					Path tempFile = get();
					if (Desktop.isDesktopSupported()) {
						Desktop.getDesktop().open(tempFile.toFile());
					} else {
						JOptionPane.showMessageDialog(null, "Archivo guardado en: " + tempFile.toAbsolutePath());
					}
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
				} catch (ExecutionException e) {
					Throwable causa = e.getCause();
					JOptionPane.showMessageDialog(null, "Error al emitir nota de credito: " + causa.getMessage(),
							"Error", JOptionPane.ERROR_MESSAGE);
					causa.printStackTrace();
				} catch (IOException e) {
					JOptionPane.showMessageDialog(null, "No se pudo abrir el archivo.", "Error", JOptionPane.ERROR_MESSAGE);
					e.printStackTrace();
				}
			}
		};

		worker.execute();
		loadingDialog.setVisible(true);
	}

	public void showFacturaData(Long saleId) throws Exception {
		
		new DialogFacturaShowData(null, service, saleId);
		
	}

	private JDialog crearDialogoCarga(String titulo, String mensaje) {
		JDialog dialog = new JDialog((Frame) null, titulo, true); // true = Modal
		dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE); // Evitar que lo cierren con la X

		JPanel panel = new JPanel(new BorderLayout(15, 15));
		panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

		JLabel lblMensaje = new JLabel(mensaje);
		lblMensaje.setHorizontalAlignment(SwingConstants.CENTER);

		JProgressBar progressBar = new JProgressBar();
		progressBar.setIndeterminate(true); // La barrita que se mueve de lado a lado

		panel.add(lblMensaje, BorderLayout.NORTH);
		panel.add(progressBar, BorderLayout.CENTER);

		dialog.add(panel);
		dialog.pack();
		dialog.setLocationRelativeTo(null);

		return dialog;
	}


}
