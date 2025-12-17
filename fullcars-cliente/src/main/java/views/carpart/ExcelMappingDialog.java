package views.carpart;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.datatransfer.DataFlavor;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.event.ActionEvent;
import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTextField;
import javax.swing.SwingWorker;

import controller.AppContext;
import controller.ProviderController;
import dtos.TaskStatusInfo;
import model.client.entities.ProviderMapping;

public class ExcelMappingDialog extends JDialog {

	private final ProviderController providerController = AppContext.providerController;
    private Long idProvider;
    private JTextField txtColumnaNombre;
    private JTextField txtColumnaMarca;
    private JTextField txtColumnaPrecio;
    private JTextField txtColumnaProvCod;
    private JTextField txtColumnaCategory;
    private JTextField txtUltimaActualizacion;
    private JTextField txtRutaArchivo;

    private JButton btnSeleccionarArchivo;
    private JButton btnGuardar;
    private JButton btnCancelar;

    private File archivoSeleccionado;

    public ExcelMappingDialog(Frame parent, Long idProvider) {
        super(parent, "Carga y Mapeo de Excel", true);
        setSize(700, 450); // más ancho que el default
        setLocationRelativeTo(parent);
        habilitarDragAndDrop();
        this.idProvider = idProvider;
        
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 10); // padding general con espacio extra a la derecha
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0; gbc.anchor = GridBagConstraints.EAST;
        panel.add(new JLabel("Archivo Excel:"), gbc);

        txtRutaArchivo = new JTextField(40);
        txtRutaArchivo.setEditable(false);
        gbc.gridx = 1; gbc.gridy = 0; gbc.gridwidth = 2; gbc.weightx = 1.0; gbc.anchor = GridBagConstraints.CENTER;
        panel.add(txtRutaArchivo, gbc);

        btnSeleccionarArchivo = new JButton("Seleccionar...");
        gbc.gridx = 3; gbc.gridy = 0; gbc.gridwidth = 1; gbc.weightx = 0;
        panel.add(btnSeleccionarArchivo, gbc);

        JLabel lblDragInfo = new JLabel("También podés arrastrar el archivo aquí");
        lblDragInfo.setFont(lblDragInfo.getFont().deriveFont(Font.ITALIC));
        lblDragInfo.setForeground(Color.GRAY);
        gbc.gridx = 1; gbc.gridy = 1; gbc.gridwidth = 3; gbc.weightx = 1.0;
        panel.add(lblDragInfo, gbc);

        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 1; gbc.weightx = 0; gbc.anchor = GridBagConstraints.EAST;
        panel.add(new JLabel("Columna Nombre:"), gbc);

        txtColumnaNombre = new JTextField(40);
        gbc.gridx = 1; gbc.gridy = 2; gbc.gridwidth = 3; gbc.weightx = 1.0; gbc.anchor = GridBagConstraints.CENTER;
        panel.add(txtColumnaNombre, gbc);

        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 1; gbc.weightx = 0; gbc.anchor = GridBagConstraints.EAST;
        panel.add(new JLabel("Columna Marca:"), gbc);

        txtColumnaMarca = new JTextField(40);
        gbc.gridx = 1; gbc.gridy = 3; gbc.gridwidth = 3; gbc.weightx = 1.0; gbc.anchor = GridBagConstraints.CENTER;
        panel.add(txtColumnaMarca, gbc);

        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 1; gbc.weightx = 0; gbc.anchor = GridBagConstraints.EAST;
        panel.add(new JLabel("Columna Precio:"), gbc);

        txtColumnaPrecio = new JTextField(40);
        gbc.gridx = 1; gbc.gridy = 4; gbc.gridwidth = 3; gbc.weightx = 1.0; gbc.anchor = GridBagConstraints.CENTER;
        panel.add(txtColumnaPrecio, gbc);

        gbc.gridx = 0; gbc.gridy = 5; gbc.gridwidth = 1; gbc.weightx = 0; gbc.anchor = GridBagConstraints.EAST;
        panel.add(new JLabel("Columna Cod.Proveedor:"), gbc);

        txtColumnaProvCod = new JTextField(40);
        gbc.gridx = 1; gbc.gridy = 5; gbc.gridwidth = 3; gbc.weightx = 1.0; gbc.anchor = GridBagConstraints.CENTER;
        panel.add(txtColumnaProvCod, gbc);
        
        gbc.gridx = 0; gbc.gridy = 6; gbc.gridwidth = 1; gbc.weightx = 0; gbc.anchor = GridBagConstraints.EAST;
        panel.add(new JLabel("Columna Categoria:"), gbc);

        txtColumnaCategory = new JTextField(40);
        gbc.gridx = 1; gbc.gridy = 6; gbc.gridwidth = 3; gbc.weightx = 1.0; gbc.anchor = GridBagConstraints.CENTER;
        panel.add(txtColumnaCategory, gbc);
        
        gbc.gridx = 0; gbc.gridy = 7; gbc.gridwidth = 1; gbc.weightx = 0; gbc.anchor = GridBagConstraints.EAST;
        panel.add(new JLabel("Última actualización:"), gbc);

        txtUltimaActualizacion = new JTextField(40);
        txtUltimaActualizacion.setEditable(false);
        gbc.gridx = 1; gbc.gridy = 7; gbc.gridwidth = 3; gbc.weightx = 1.0; gbc.anchor = GridBagConstraints.CENTER;
        panel.add(txtUltimaActualizacion, gbc);

        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnGuardar = new JButton("Guardar");
        btnCancelar = new JButton("Cancelar");
        panelBotones.add(btnGuardar);
        panelBotones.add(btnCancelar);

        gbc.gridx = 0; gbc.gridy = 8; gbc.gridwidth = 4; gbc.weightx = 0; gbc.anchor = GridBagConstraints.CENTER;
        panel.add(panelBotones, gbc);

        add(panel);

        ProviderMapping mappingExistente = providerController.getProviderMapping(idProvider);
        // Inicializar con datos previos si existen
        if (mappingExistente != null) {
            txtColumnaNombre.setText(mappingExistente.getNameColumn());
            txtColumnaMarca.setText(mappingExistente.getBrandColumn());
            txtColumnaPrecio.setText(mappingExistente.getPriceColumn());
            txtColumnaCategory.setText(mappingExistente.getCategoryColumn());
            txtColumnaProvCod.setText(mappingExistente.getProvCodColumn());
            txtUltimaActualizacion.setText(mappingExistente.getLastUpdate()
                    .format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
        } else {
            txtUltimaActualizacion.setText("Sin registros previos");
        }

        btnSeleccionarArchivo.addActionListener(this::seleccionarArchivo);
        btnCancelar.addActionListener(e -> dispose());
        btnGuardar.addActionListener(e -> guardarMapping());
    }

    private void seleccionarArchivo(ActionEvent e) {
        JFileChooser fileChooser = new JFileChooser();
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            archivoSeleccionado = fileChooser.getSelectedFile();
            txtRutaArchivo.setText(archivoSeleccionado.getAbsolutePath());
        }
    }

    private void guardarMapping() {
        if (archivoSeleccionado == null) {
            JOptionPane.showMessageDialog(this, "Debe seleccionar un archivo Excel.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        ProviderMapping nuevoMapping = new ProviderMapping(
        		idProvider,
                txtColumnaNombre.getText(),
                txtColumnaMarca.getText(),
                txtColumnaPrecio.getText(),
                txtColumnaProvCod.getText(),
                //"",
                txtColumnaCategory.getText(),
                LocalDateTime.now()
        );
        System.out.println(nuevoMapping);
        try {
            String taskId = providerController.saveProviderMapping(nuevoMapping, archivoSeleccionado);

            JDialog progressDialog = new JDialog(this, "Procesando. Esto puede tardar unos segundos...", true);
            progressDialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
            JLabel message = new JLabel("   Actualizando Base de Datos...");
            progressDialog.add(message, BorderLayout.NORTH);
            JProgressBar progressBar = new JProgressBar();
            progressBar.setIndeterminate(true);
            progressDialog.add(progressBar, BorderLayout.CENTER);
            progressDialog.setSize(400, 140);
            progressDialog.setLocationRelativeTo(this);

            SwingWorker<Void, String> worker = new SwingWorker<>() {
                @Override
                protected Void doInBackground() throws Exception {
                    TaskStatusInfo status;
                    do {
                        Thread.sleep(500); // consultar cada 1s
                        status = providerController.getTaskStatus(taskId); // llamada al servidor
                        //publish(status.getStatus().toString() + "\nActualizando Base de Datos...");//status.getError() != null?status.getError():""); 
                    } while (TaskStatusInfo.TaskStatus.PENDIENTE.equals(status.getStatus()) || TaskStatusInfo.TaskStatus.CARGANDO.equals(status.getStatus()));

                    return null;
                }
                @Override
                protected void process(java.util.List<String> chunks) {
                    String latestStatus = chunks.get(chunks.size() - 1);
                    message.setText(latestStatus);
                }
                @Override
                protected void done() {
                    progressDialog.dispose();
                    try {
                    	TaskStatusInfo finalStatus = providerController.getTaskStatus(taskId);
                        if (TaskStatusInfo.TaskStatus.TERMINADO.equals(finalStatus.getStatus())) {
                            JOptionPane.showMessageDialog(null, "Datos guardados correctamente.");
                            dispose();
                        } else {
                            JOptionPane.showMessageDialog(null, "Error en la tarea."+finalStatus.getError(), "Error", JOptionPane.ERROR_MESSAGE);
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        JOptionPane.showMessageDialog(null, "Error al consultar el estado.", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            };
            worker.execute();
            progressDialog.setVisible(true);

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, e.getLocalizedMessage() , "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }

    }
    
    
    private void habilitarDragAndDrop() {
        new DropTarget(this, new DropTargetListener() {
            @Override
            public void dragEnter(DropTargetDragEvent dtde) {}

            @Override
            public void dragOver(DropTargetDragEvent dtde) {}

            @Override
            public void dropActionChanged(DropTargetDragEvent dtde) {}

            @Override
            public void dragExit(DropTargetEvent dte) {}

            @Override
            public void drop(DropTargetDropEvent dtde) {
                try {
                    dtde.acceptDrop(DnDConstants.ACTION_COPY);
                    Object transferData = dtde.getTransferable()
                            .getTransferData(DataFlavor.javaFileListFlavor);

                    if (transferData instanceof List) {
                        List<File> files = (List<File>) transferData;
                        if (!files.isEmpty()) {
                            archivoSeleccionado = files.get(0);
                            txtRutaArchivo.setText(archivoSeleccionado.getAbsolutePath());
                            JOptionPane.showMessageDialog(ExcelMappingDialog.this,
                                    "Archivo cargado: " + archivoSeleccionado.getName());
                        }
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(ExcelMappingDialog.this,
                            "Error al arrastrar archivo: " + ex.getMessage(),
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
    }
    
    @Override
    public void dispose() {
    	this.removeAll();
    	archivoSeleccionado = null;
    	//providerController = null;
    	txtColumnaMarca = null;
    	txtColumnaNombre=null;
    	super.dispose();
    }
}
