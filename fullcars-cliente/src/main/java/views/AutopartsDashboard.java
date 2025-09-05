package views;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;

import Utils.Icons;
import Utils.NumberFormatArg;
import controller.StatisticsController;
import dtos.SalesData;
import dtos.StatisticsGeneralDTO;
import dtos.TopProductDTO;
import interfaces.Refreshable;
import model.client.entities.CarPart;
import model.client.entities.Purchase;
import model.client.entities.Sale;
import raven.datetime.DatePicker.DateSelectionMode;
import views.components.DatePickerS;

public class AutopartsDashboard extends JPanel implements Refreshable {

    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");

    // Colores
    private final Color PRIMARY_COLOR = new Color(41, 128, 185);
    private final Color SUCCESS_COLOR = new Color(46, 204, 113);
    private final Color WARNING_COLOR = new Color(241, 196, 15);
    private final Color DANGER_COLOR = new Color(231, 76, 60);
    private final Color BACKGROUND_COLOR = new Color(236, 240, 241);
    private final Color CARD_COLOR = Color.WHITE;

    // Componentes dinámicos
    private JTable top5ProductsTable;
    private JTable recentSalesTable;
    private JPanel metricsPanel;
    private JPanel criticalStockPanel;
    private JPanel monthlyBarChartPanel;
    private JPanel notificationsPanel;

    private JLabel ventasValue;
    private JLabel comprasValue;
    private JLabel stockValue;
    private JLabel gananciaValue;
    
    private final StatisticsController controller = new StatisticsController(); 
    private DatePickerS dp = new DatePickerS();
    private JButton searchButton = new JButton("Buscar", Icons.LENS.create(20,20));

    public AutopartsDashboard() {
        setLayout(new BorderLayout(10, 10));
        setBackground(BACKGROUND_COLOR);

        add(createHeaderPanel(), BorderLayout.NORTH);
        add(createContentPanel(), BorderLayout.CENTER);
        add(createFooterPanel(), BorderLayout.SOUTH);
        SwingUtilities.invokeLater(()->refresh());
    }

    /* ------------ HEADER ------------ */
    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(CARD_COLOR);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(5, 25, 5, 10));
        headerPanel.setPreferredSize(new Dimension(0, 45));

        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.setBackground(CARD_COLOR);

        panel.add(new JLabel("Fecha Desde / Hasta: "));
        JFormattedTextField text = new JFormattedTextField();
        text.setColumns(20);

        dp.setDateSelectionMode(DateSelectionMode.BETWEEN_DATE_SELECTED);
        dp.setSelectedDateRange(LocalDate.of(2025, 9, 1), LocalDate.now().plusMonths(2));
        dp.setEditor(text);

        panel.add(text);
        panel.add(searchButton);
        searchButton.addActionListener(e-> refresh());
        headerPanel.add(panel, BorderLayout.CENTER);
        return headerPanel;
    }

    /* ------------ CONTENT ------------ */
    private JPanel createContentPanel() {
        JPanel contentPanel = new JPanel(new BorderLayout(15, 15));
        contentPanel.setOpaque(false);

        contentPanel.add(createMetricsPanel(), BorderLayout.NORTH);

        JPanel centerPanel = new JPanel(new BorderLayout(15, 15));
        centerPanel.setOpaque(false);

        centerPanel.add(createTablesPanel(), BorderLayout.CENTER);
        centerPanel.add(createSidePanel(), BorderLayout.EAST);

        contentPanel.add(centerPanel, BorderLayout.CENTER);
        return contentPanel;
    }

    private JPanel createMetricsPanel() {
    	metricsPanel = new JPanel(new GridLayout(1, 4, 15, 0));
        metricsPanel.setOpaque(false);
        metricsPanel.setPreferredSize(new Dimension(0, 120));

        metricsPanel.add(createMetricCard("Ventas entre Fehcas:", ventasValue = new JLabel("$0.00"), "", SUCCESS_COLOR));

        metricsPanel.add(createMetricCard("Compras entre Fechas:", comprasValue = new JLabel("$0.00"), "", PRIMARY_COLOR));

        metricsPanel.add(createMetricCard("Deuda Total por Cobrar", gananciaValue = new JLabel("$0.00"), "", SUCCESS_COLOR));

        metricsPanel.add(createMetricCard("Items en Stock", stockValue = new JLabel("0"), "", WARNING_COLOR));

        return metricsPanel;
    }

    private JPanel createMetricCard(String title, JLabel valueLabel, String change, Color accentColor) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(CARD_COLOR);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
            BorderFactory.createEmptyBorder(15, 20, 15, 20)
        ));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        titleLabel.setForeground(Color.GRAY);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        valueLabel.setForeground(Color.BLACK);
        valueLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel changeLabel = new JLabel(change);
        changeLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        changeLabel.setForeground(accentColor);
        changeLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        card.add(titleLabel);
        card.add(Box.createVerticalStrut(5));
        card.add(valueLabel);
        card.add(Box.createVerticalStrut(5));
        card.add(changeLabel);

        return card;
    }
    
    private JSplitPane createTablesPanel() {
    	JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, createMonthlyBarChartPanel(), createRecentSalesPanel());
        splitPane.setResizeWeight(1); // El panel de arriba no se estira al redimensionar

        return splitPane;
    }

    private JPanel createSidePanel() {
        JPanel sidePanel = new JPanel();
        sidePanel.setLayout(new BoxLayout(sidePanel, BoxLayout.Y_AXIS));
        sidePanel.setOpaque(false);
        sidePanel.setPreferredSize(new Dimension(400, 0));

        sidePanel.add(createCriticalStockPanel());
        sidePanel.add(Box.createVerticalStrut(15));
        sidePanel.add(createTopProductsPanel());
        sidePanel.add(Box.createVerticalStrut(15));
        sidePanel.add(createNotificationsPanel());

        return sidePanel;
    }
    
    private JPanel createRecentSalesPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setMaximumSize(new Dimension(1920, 180));
        panel.setPreferredSize(new Dimension(1920, 180));
        panel.setBackground(CARD_COLOR);
        panel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
                "Ventas Recientes", TitledBorder.LEFT, TitledBorder.TOP,
                new Font("Segoe UI", Font.BOLD, 14)));

        String[] columns = {"Cliente", "Producto", "Cantidad", "Total", "Fecha"};
        recentSalesTable = new JTable(new DefaultTableModel(columns, 0));
        recentSalesTable.setRowHeight(25);

        panel.add(new JScrollPane(recentSalesTable), BorderLayout.CENTER);
        return panel;
    }

    private JPanel createTopProductsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(CARD_COLOR);
        panel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
                "Productos Más Vendidos", TitledBorder.LEFT, TitledBorder.TOP,
                new Font("Segoe UI", Font.BOLD, 14)));

        String[] columns = {"Producto", "Código", "Vendidos", "Ingresos"};
        top5ProductsTable = new JTable(new DefaultTableModel(columns, 0));
        top5ProductsTable.setRowHeight(25);

        panel.add(new JScrollPane(top5ProductsTable), BorderLayout.CENTER);
        return panel;
    }

    private JPanel createMonthlyBarChartPanel() {
        SalesBarChart salesChart = new SalesBarChart(PRIMARY_COLOR, CARD_COLOR, BACKGROUND_COLOR);
        monthlyBarChartPanel = salesChart.getChartPanel();
        monthlyBarChartPanel.setBackground(CARD_COLOR);
        monthlyBarChartPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
                "Ventas por Período", TitledBorder.LEFT, TitledBorder.TOP,
                new Font("Segoe UI", Font.BOLD, 14)));
        return monthlyBarChartPanel;
    }

    private JPanel createCriticalStockPanel() {
        criticalStockPanel = new JPanel(new BorderLayout());
        criticalStockPanel.setBackground(CARD_COLOR);
        criticalStockPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
                "Stock Crítico (< 5 unidades)", TitledBorder.LEFT, TitledBorder.TOP,
                new Font("Segoe UI", Font.BOLD, 14)));
        return criticalStockPanel;
    }

    private JPanel createNotificationsPanel() {
        notificationsPanel = new JPanel(new BorderLayout());
        notificationsPanel.setBackground(new Color(255, 249, 230));
        notificationsPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(241, 196, 15), 2),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)));
        return notificationsPanel;
    }

    /* ------------ FOOTER ------------ */
    private JPanel createFooterPanel() {
        JPanel footerPanel = new JPanel(new BorderLayout());
        footerPanel.setBackground(new Color(52, 73, 94));
        footerPanel.setBorder(BorderFactory.createEmptyBorder(10, 25, 10, 25));
        footerPanel.setPreferredSize(new Dimension(0, 40));

        JLabel statusLabel = new JLabel("Sistema: Conectado | Base de Datos: OK | Última actualización: "
                + new SimpleDateFormat("HH:mm:ss").format(new Date()));
        statusLabel.setForeground(Color.WHITE);

        JLabel versionLabel = new JLabel("AutoPartes Pro v2.1.0");
        versionLabel.setForeground(Color.LIGHT_GRAY);

        footerPanel.add(statusLabel, BorderLayout.WEST);
        footerPanel.add(versionLabel, BorderLayout.EAST);
        return footerPanel;
    }

    @Override
    public void refresh() {
        dp.setSelectedDateRange(LocalDate.of(2025, 9, 1), LocalDate.now().plusMonths(2));

    	StatisticsGeneralDTO stats = controller.getStatisticsGeneral(dp.getSelectedDateRange()); 
        // ----- CARDS -----
        //List<String> metricValues = stats.getMetricValues(); // ["$15,250.00", "$8,900.00", "1,247", "$45,890.00"]
    	BigDecimal salesTotal = BigDecimal.ZERO;
    	BigDecimal purchaseTotal = BigDecimal.ZERO;
    	for(SalesData s: stats.getSalesData())
        	salesTotal = salesTotal.add(s.getAmount());
    	for(Purchase p: stats.getPurchases())
    		purchaseTotal = purchaseTotal.add(p.getTotal());
    	
		ventasValue.setText(NumberFormatArg.format(salesTotal));
		comprasValue.setText(NumberFormatArg.format(purchaseTotal));
		stockValue.setText(String.valueOf(stats.getItemsRegistered()));
		gananciaValue.setText(NumberFormatArg.format(stats.getTotalToCharge()));
        
        loadRecentSalesTable(stats.getRecentSales());

        loadTop5ProductsTable(stats.getTopProducts());
        
        loadCriticalStockPanel(stats.getCriticalStock());

        //List<String> notifications = stats.getNotifications();
        loadNotificationsPanel(new ArrayList<String>(), stats.getPurchasesNotPayed());

        // ----- GRÁFICO DE VENTAS -----
        SalesBarChart chart = new SalesBarChart(PRIMARY_COLOR, CARD_COLOR, BACKGROUND_COLOR);
        chart.updateChart(dp.getSelectedDateRange()[0], dp.getSelectedDateRange()[1], stats.getSalesData());
        
        monthlyBarChartPanel.removeAll();
        monthlyBarChartPanel.add(chart.getChartPanel());
        monthlyBarChartPanel.revalidate();
        monthlyBarChartPanel.repaint();
    }

    private void loadRecentSalesTable(List<Sale> recentSales) {
        String[] columns = {"Cliente", "Nro. Venta", "Total", "Fecha"};
        Object[][] data = new Object[recentSales.size()][columns.length];

        for (int i = 0; i < recentSales.size(); i++) {
            Sale s = recentSales.get(i);
            data[i][0] = s.getCustomer().getFullName();
            data[i][1] = s.getId();
            data[i][2] = NumberFormatArg.format(s.getTotal());
            data[i][3] = (s.getDate());
        }

        recentSalesTable.setModel(new javax.swing.table.DefaultTableModel(data, columns) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        });
        recentSalesTable.revalidate();
        recentSalesTable.repaint();
    }

    private void loadTop5ProductsTable(List<TopProductDTO> topProducts) {
        String[] columns = {"SKU", "Nombre", "Vendidos", "Ingresos"};
        Object[][] data = new Object[topProducts.size()][columns.length];

        for (int i = 0; i < topProducts.size(); i++) {
        	TopProductDTO p = topProducts.get(i);
        	data[i][0] = p.getSku();
            data[i][1] = p.getNombre();
            data[i][2] = p.getCantidadVendidos();
            data[i][3] = NumberFormatArg.format(p.getIngresosTotales());
        }

        top5ProductsTable.setModel(new javax.swing.table.DefaultTableModel(data, columns) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        });
        top5ProductsTable.revalidate();
        top5ProductsTable.repaint();
    }

    private void loadCriticalStockPanel(List<CarPart> criticalItems) {
        JPanel itemsPanel = new JPanel();
        itemsPanel.setLayout(new BoxLayout(itemsPanel, BoxLayout.Y_AXIS));
        itemsPanel.setBackground(CARD_COLOR);
        itemsPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        for (CarPart item : criticalItems) {
            JLabel label = new JLabel(item.getSku());
            label.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            label.setForeground(DANGER_COLOR);
            label.setBorder(BorderFactory.createEmptyBorder(3, 0, 3, 0));
            itemsPanel.add(label);
        }

        JScrollPane scrollPane = new JScrollPane(itemsPanel);
        scrollPane.setBorder(null);
        scrollPane.setPreferredSize(new Dimension(0, 120));

        criticalStockPanel.removeAll();
        criticalStockPanel.add(scrollPane, BorderLayout.CENTER);
        criticalStockPanel.revalidate();
        criticalStockPanel.repaint();
    }

    private void loadNotificationsPanel(List<String> notifications, List<Long> purchasesNotPayed) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setPreferredSize(new Dimension(WIDTH, 250));

        JLabel titulo = new JLabel(" Notificaciones");
        titulo.setFont(new Font("Segoe UI", Font.BOLD, 14));
        titulo.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));

        JTextArea notificacionesArea = new JTextArea();
        notificacionesArea.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        notificacionesArea.setEditable(false);
        notificacionesArea.setOpaque(false);
        notificacionesArea.setLineWrap(true);
        notificacionesArea.setWrapStyleWord(true);


        // Convertimos la lista en texto
        StringBuilder sb = new StringBuilder();
        sb.append("• Cantidad de Compras no Pagas: ").append(purchasesNotPayed.size()).append("\n");
        for (String n : notifications) {
            sb.append("• ").append(n).append("\n");
        }
        notificacionesArea.setText(sb.toString());

        panel.add(titulo, BorderLayout.NORTH);
        panel.add(new JScrollPane(notificacionesArea), BorderLayout.CENTER);

        notificationsPanel.removeAll();
        notificationsPanel.add(panel, BorderLayout.CENTER);
        notificationsPanel.revalidate();
        notificationsPanel.repaint();
    }

  
}
