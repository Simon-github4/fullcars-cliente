package views.transactions;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.swing.AbstractCellEditor;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableColumnModel;

import Utils.NumberFormatArg;

import javax.swing.SwingConstants;

import dtos.DevolucionRequest;
import dtos.DevolucionRequest.DevolucionDetailRequest;
import model.client.entities.Sale;
import model.client.entities.SaleDetail;

public class DialogDevolucion extends JDialog {

	private static final long serialVersionUID = 1L;
	private static final NumberFormat CURRENCY_FORMAT = NumberFormat.getCurrencyInstance(new Locale("es", "AR"));

	private final Sale sale;
	private DevolucionRequest result = null;

	private CardLayout cardLayout;
	private JPanel cardPanel;
	private boolean modoArticulos = true;
	private ArticulosTableModel articulosModel;
	private JTable articulosTable;
	private JTextField montoDirectoField;
	private JTextField motivoField;
	private JCheckBox emitirCAECheckBox;

	private BigDecimal disponible;

	public DialogDevolucion(JFrame frame, Sale sale) {
		super(frame, "Devolución - Venta #" + sale.getId(), true);
		this.sale = sale;
		this.disponible = sale.getTotal().subtract(
			sale.getTotalReturned() != null ? sale.getTotalReturned() : BigDecimal.ZERO
		);
		initComponents();
	}

	private void initComponents() {
		setLayout(new BorderLayout(10, 10));
		setPreferredSize(new Dimension(750, 600));

		// Panel resumen
		JPanel resumenPanel = new JPanel();
		resumenPanel.setLayout(new BoxLayout(resumenPanel, BoxLayout.Y_AXIS));
		resumenPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 5, 20));

		JLabel lblTitulo = new JLabel("Devolución de Venta");
		lblTitulo.setFont(new Font("Arial", Font.BOLD, 14));
		lblTitulo.setAlignmentX(Component.LEFT_ALIGNMENT);
		resumenPanel.add(lblTitulo);
		resumenPanel.add(Box.createVerticalStrut(8));

		JLabel lblTotal = new JLabel("Total de la venta: " + CURRENCY_FORMAT.format(sale.getTotal()));
		lblTotal.setAlignmentX(Component.LEFT_ALIGNMENT);
		resumenPanel.add(lblTotal);

		BigDecimal yaDevuelto = sale.getTotalReturned() != null ? sale.getTotalReturned() : BigDecimal.ZERO;
		JLabel lblDevuelto = new JLabel("Ya devuelto: " + CURRENCY_FORMAT.format(yaDevuelto));
		lblDevuelto.setAlignmentX(Component.LEFT_ALIGNMENT);
		resumenPanel.add(lblDevuelto);

		JLabel lblDisponible = new JLabel("Disponible para devolver: " + CURRENCY_FORMAT.format(disponible));
		lblDisponible.setFont(new Font("Arial", Font.BOLD, 12));
		lblDisponible.setAlignmentX(Component.LEFT_ALIGNMENT);
		resumenPanel.add(lblDisponible);

		add(resumenPanel, BorderLayout.NORTH);

		// Panel central con toggle y cards
		JPanel centerContainer = new JPanel(new BorderLayout());

		// Panel modo toggle
		JPanel togglePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		JRadioButton rbArticulos = new JRadioButton("Seleccionar artículos", true);
		JRadioButton rbMonto = new JRadioButton("Monto directo");
		ButtonGroup group = new ButtonGroup();
		group.add(rbArticulos);
		group.add(rbMonto);
		togglePanel.add(rbArticulos);
		togglePanel.add(rbMonto);
		centerContainer.add(togglePanel, BorderLayout.NORTH);

		// CardLayout para los dos modos
		cardLayout = new CardLayout();
		cardPanel = new JPanel(cardLayout);

		// Modo artículos
		articulosModel = new ArticulosTableModel();
		articulosModel.setDetails(sale.getDetails());
		articulosTable = new JTable(articulosModel);
		articulosTable.setRowHeight(30);
		articulosTable.getColumnModel().getColumn(0).setPreferredWidth(200);
		articulosTable.getColumnModel().getColumn(1).setPreferredWidth(70);
		articulosTable.getColumnModel().getColumn(2).setPreferredWidth(60);
		articulosTable.getColumnModel().getColumn(3).setPreferredWidth(70);
		articulosTable.getColumnModel().getColumn(4).setPreferredWidth(70);
		articulosTable.getColumnModel().getColumn(5).setPreferredWidth(80);

		DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
		centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
		articulosTable.getColumnModel().getColumn(1).setCellRenderer(centerRenderer);
		articulosTable.getColumnModel().getColumn(2).setCellRenderer(centerRenderer);
		articulosTable.getColumnModel().getColumn(3).setCellRenderer(centerRenderer);
		articulosTable.getColumnModel().getColumn(4).setCellRenderer(centerRenderer);

		TableColumnModel tcm = articulosTable.getColumnModel();
		tcm.getColumn(5).setCellEditor(new SpinnerCellEditor());
		tcm.getColumn(5).setCellRenderer(new SpinnerCellRenderer());

		JScrollPane scrollArticulos = new JScrollPane(articulosTable);
		JPanel panelArticulos = new JPanel(new BorderLayout());
		panelArticulos.add(scrollArticulos, BorderLayout.CENTER);
		cardPanel.add(panelArticulos, "articulos");

		// Modo monto directo
		JPanel panelMonto = new JPanel();
		panelMonto.setLayout(new BoxLayout(panelMonto, BoxLayout.Y_AXIS));
		panelMonto.setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 40));

		JLabel lblMonto = new JLabel("Monto a devolver:");
		lblMonto.setAlignmentX(Component.LEFT_ALIGNMENT);
		panelMonto.add(lblMonto);

		montoDirectoField = new JTextField(15);
		montoDirectoField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
		montoDirectoField.setAlignmentX(Component.LEFT_ALIGNMENT);
		panelMonto.add(montoDirectoField);
		panelMonto.add(Box.createVerticalStrut(10));

		JLabel lblHint = new JLabel("Máximo: " + CURRENCY_FORMAT.format(disponible));
		lblHint.setAlignmentX(Component.LEFT_ALIGNMENT);
		panelMonto.add(lblHint);

		cardPanel.add(panelMonto, "monto");

		cardLayout.show(cardPanel, "articulos");
		centerContainer.add(cardPanel, BorderLayout.CENTER);

		rbArticulos.addActionListener(e -> {
			cardLayout.show(cardPanel, "articulos");
			modoArticulos = true;
		});
		rbMonto.addActionListener(e -> {
			cardLayout.show(cardPanel, "monto");
			modoArticulos = false;
		});

		add(centerContainer, BorderLayout.CENTER);

		// Panel motivo + botones
		JPanel bottomPanel = new JPanel();
		bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.Y_AXIS));

		JPanel motivoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		motivoPanel.add(new JLabel("Motivo: "));
		motivoField = new JTextField(30);
		motivoPanel.add(motivoField);
		bottomPanel.add(motivoPanel);

		JPanel caePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		emitirCAECheckBox = new JCheckBox("Emitir Nota de Crédito electrónica (CAE)");
		caePanel.add(emitirCAECheckBox);
		bottomPanel.add(caePanel);

		JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		JButton btnConfirmar = new JButton("Confirmar");
		JButton btnCancelar = new JButton("Cancelar");
		btnConfirmar.addActionListener(e -> confirmar());
		btnCancelar.addActionListener(e -> cancelar());
		btnPanel.add(btnConfirmar);
		btnPanel.add(btnCancelar);
		bottomPanel.add(btnPanel);

		add(bottomPanel, BorderLayout.SOUTH);

		pack();
		setMinimumSize(new Dimension(500, 450));
		setLocationRelativeTo(getParent());
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
	}

	private void confirmar() {
		if (articulosTable.getCellEditor() != null)
			articulosTable.getCellEditor().stopCellEditing();

		String motivo = motivoField.getText().trim();
		if (motivo.isEmpty()) {
			JOptionPane.showMessageDialog(this, "Debe ingresar un motivo.", "Error", JOptionPane.ERROR_MESSAGE);
			return;
		}

		if (modoArticulos) {
			List<DevolucionDetailRequest> detalles = new ArrayList<>();
			BigDecimal montoTotal = BigDecimal.ZERO;

			for (int i = 0; i < articulosModel.getRowCount(); i++) {
				int cantDevolver = articulosModel.getCantidadDevolver(i);
				if (cantDevolver > 0) {
					SaleDetail detail = articulosModel.getDetailAt(i);
					detalles.add(DevolucionDetailRequest.builder()
						.saleDetailId(detail.getId())
						.cantidadDevuelta(cantDevolver)
						.build());
					montoTotal = montoTotal.add(detail.getUnitPrice().multiply(BigDecimal.valueOf(cantDevolver)));
				}
			}

			if (detalles.isEmpty()) {
				JOptionPane.showMessageDialog(this, "Debe seleccionar al menos un artículo para devolver.", "Error", JOptionPane.ERROR_MESSAGE);
				return;
			}

			result = DevolucionRequest.builder()
				.saleId(sale.getId())
				.motivo(motivo)
				.emitirCAE(emitirCAECheckBox.isSelected() ? true : null)
				.detalles(detalles)
				.build();
		} else {
			try {
				BigDecimal monto = new BigDecimal(montoDirectoField.getText().trim());
				if (monto.compareTo(BigDecimal.ZERO) <= 0) {
					JOptionPane.showMessageDialog(this, "El monto debe ser mayor a 0.", "Error", JOptionPane.ERROR_MESSAGE);
					return;
				}
				if (monto.compareTo(disponible) > 0) {
					JOptionPane.showMessageDialog(this, "El monto no puede exceder " + CURRENCY_FORMAT.format(disponible), "Error", JOptionPane.ERROR_MESSAGE);
					return;
				}
			result = DevolucionRequest.builder()
				.saleId(sale.getId())
				.motivo(motivo)
				.emitirCAE(emitirCAECheckBox.isSelected() ? true : null)
				.monto(monto)
				.build();
			} catch (NumberFormatException e) {
				JOptionPane.showMessageDialog(this, "Ingrese un monto válido.", "Error", JOptionPane.ERROR_MESSAGE);
				return;
			}
		}

		dispose();
	}

	private void cancelar() {
		result = null;
		dispose();
	}

	public DevolucionRequest getResult() {
		return result;
	}

	// --- Tabla de artículos ---

	private static class ArticulosTableModel extends AbstractTableModel {

		private static final String[] COLUMNS = {"Autoparte", "Precio U.", "Cantidad", "Devuelto", "Disponible", "Cant. a Devolver"};
		private final List<SaleDetail> details = new ArrayList<>();
		private final List<Integer> cantidades = new ArrayList<>();

		public void setDetails(List<SaleDetail> details) {
			this.details.clear();
			this.cantidades.clear();
			for (SaleDetail d : details) {
				int disponible = d.getQuantity() - d.getQuantityReturned();
				if (disponible > 0) {
					this.details.add(d);
					this.cantidades.add(0);
				}
			}
			fireTableDataChanged();
		}

		public SaleDetail getDetailAt(int row) {
			return details.get(row);
		}

		public int getCantidadDevolver(int row) {
			return cantidades.get(row);
		}

		@Override
		public int getRowCount() {
			return details.size();
		}

		@Override
		public int getColumnCount() {
			return COLUMNS.length;
		}

		@Override
		public String getColumnName(int column) {
			return COLUMNS[column];
		}

		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			SaleDetail d = details.get(rowIndex);
			int disponible = d.getQuantity() - d.getQuantityReturned();
			switch (columnIndex) {
				case 0: return d.getCarPart().getName();
				case 1: return NumberFormatArg.format(d.getUnitPrice());
				case 2: return d.getQuantity();
				case 3: return d.getQuantityReturned();
				case 4: return disponible;
				case 5: return cantidades.get(rowIndex);
				default: return null;
			}
		}

		@Override
		public boolean isCellEditable(int rowIndex, int columnIndex) {
			return columnIndex == 5;
		}

		@Override
		public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
			if (columnIndex == 5 && aValue instanceof Integer) {
				setCantidadDevolver(rowIndex, (Integer) aValue);
			}
		}

		public void setCantidadDevolver(int row, int value) {
			int disponible = details.get(row).getQuantity() - details.get(row).getQuantityReturned();
			if (value < 0) value = 0;
			if (value > disponible) value = disponible;
			cantidades.set(row, value);
			fireTableRowsUpdated(row, row);
		}
	}

	// --- Spinner Cell Editor ---

	private class SpinnerCellEditor extends AbstractCellEditor implements TableCellEditor {
		private static final long serialVersionUID = 1L;
		private final javax.swing.JSpinner spinner;

		public SpinnerCellEditor() {
			spinner = new javax.swing.JSpinner(new SpinnerNumberModel(0, 0, 1000, 1));
		}

		@Override
		public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
			int disponible = articulosModel.getDetailAt(row).getQuantity() - articulosModel.getDetailAt(row).getQuantityReturned();
			spinner.setModel(new SpinnerNumberModel(0, 0, disponible, 1));
			spinner.setValue(value != null ? value : 0);
			return spinner;
		}

		@Override
		public Object getCellEditorValue() {
			return spinner.getValue();
		}

		@Override
		public boolean stopCellEditing() {
			int row = articulosTable.getEditingRow();
			if (row >= 0) {
				articulosModel.setCantidadDevolver(row, (Integer) spinner.getValue());
			}
			return super.stopCellEditing();
		}
	}

	// --- Spinner Cell Renderer ---

	private static class SpinnerCellRenderer extends DefaultTableCellRenderer {
		private static final long serialVersionUID = 1L;

		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
			javax.swing.JSpinner spinner = new javax.swing.JSpinner(new SpinnerNumberModel(0, 0, 1000, 1));
			spinner.setValue(value != null ? value : 0);
			spinner.setBorder(null);
			return spinner;
		}
	}
}
