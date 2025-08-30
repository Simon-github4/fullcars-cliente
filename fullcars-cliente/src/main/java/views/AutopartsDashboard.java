package views;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.border.TitledBorder;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.data.category.DefaultCategoryDataset;

import Utils.Icons;
import interfaces.Refreshable;
import raven.datetime.DatePicker;
import raven.datetime.DatePicker.DateSelectionMode;
import views.components.DatePickerS;


public class AutopartsDashboard extends JPanel implements Refreshable{
    
    private DecimalFormat currencyFormat = new DecimalFormat("$###,##0.00");
    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");
    
    // Colores del tema
    private final Color PRIMARY_COLOR = new Color(41, 128, 185);
    private final Color SUCCESS_COLOR = new Color(46, 204, 113);
    private final Color WARNING_COLOR = new Color(241, 196, 15);
    private final Color DANGER_COLOR = new Color(231, 76, 60);
    private final Color BACKGROUND_COLOR = new Color(236, 240, 241);
    private final Color CARD_COLOR = Color.WHITE;
    private DatePickerS dp = new DatePickerS();
	private JButton searchButton = new JButton("Buscar", Icons.LENS.create(20,20));
	private JTable top5ProductsTable;
	private JTable recentSalesTable;
	private JPanel metricsPanel;
	private JPanel notificationsPanel;
	private JPanel criticalStockPanel;
	private JPanel monthlyBarChartPanel;

    public AutopartsDashboard() {
        setLayout(new BorderLayout(10, 10));
        setBackground(BACKGROUND_COLOR);
        
        add(createHeaderPanel(), BorderLayout.NORTH);
        
        add(createContentPanel(), BorderLayout.CENTER);
        
        add(createFooterPanel(), BorderLayout.SOUTH);        
    }
    
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
        dp.setBackground(Color.GRAY);
        dp.setEditor(text);
        panel.add(text);
        panel.add(searchButton);
        
        headerPanel.add(panel, BorderLayout.CENTER);
        return headerPanel;
    }
    
    private JPanel createContentPanel() {
        JPanel contentPanel = new JPanel(new BorderLayout(15, 15));
        contentPanel.setOpaque(false);
        
        // Panel superior - Métricas
        contentPanel.add(createMetricsPanel(), BorderLayout.NORTH);
        
        // Panel central
        JPanel centerPanel = new JPanel(new BorderLayout(15, 15));
        centerPanel.setOpaque(false);
        
        // Panel izquierdo - Tablas informativas
        centerPanel.add(createTablesPanel(), BorderLayout.CENTER);
        
        // Panel derecho - Accesos rápidos y gráficos
        centerPanel.add(createSidePanel(), BorderLayout.EAST);
        
        contentPanel.add(centerPanel, BorderLayout.CENTER);
        
        return contentPanel;
    }
    
    private JPanel createMetricsPanel() {
        metricsPanel = new JPanel(new GridLayout(1, 4, 15, 0));
        metricsPanel.setOpaque(false);
        metricsPanel.setPreferredSize(new Dimension(0, 120));
        
        // Ventas del día
        metricsPanel.add(createMetricCard("Ventas Hoy", "$15,250.00", "+12%", SUCCESS_COLOR));
        
        // Compras realizadas
        metricsPanel.add(createMetricCard("Compras Realizadas", "$8,900.00", "+5%", PRIMARY_COLOR));
        
        // Items en Stock
        metricsPanel.add(createMetricCard("Items en Stock", "1,247", "-3 críticos", WARNING_COLOR));
        
        // Ganancia del mes
        metricsPanel.add(createMetricCard("Ganancia Mes", "$45,890.00", "+18%", SUCCESS_COLOR));
        
        return metricsPanel;
    }
    
    private JPanel createMetricCard(String title, String value, String change, Color accentColor) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(CARD_COLOR);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
            BorderFactory.createEmptyBorder(15, 20, 15, 20)
        ));
        
        // Título
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        titleLabel.setForeground(Color.GRAY);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        // Valor principal
        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        valueLabel.setForeground(Color.BLACK);
        valueLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        // Cambio
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
    
    private JPanel createTablesPanel() {
        JPanel tablesPanel = new JPanel();
        tablesPanel.setLayout(new BoxLayout(tablesPanel, BoxLayout.Y_AXIS));
        tablesPanel.setOpaque(false);
        
        // Productos más vendidos
        tablesPanel.add(createMonthlyBarChartPanel());
        
        tablesPanel.add(Box.createVerticalStrut(5));
        // Ventas recientes
        tablesPanel.add(createRecentSalesPanel());
        
        return tablesPanel;
    }
    
    private JPanel createTopProductsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(CARD_COLOR);
        panel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
            "Top 5 Productos Más Vendidos",
            TitledBorder.LEFT, TitledBorder.TOP,
            new Font("Segoe UI", Font.BOLD, 14)
        ));
        
        String[] columns = {"Producto", "Código", "Vendidos", "Ingresos"};
        Object[][] data = {
            {"Filtro de Aceite Toyota", "FO-001", "45", "$2,250.00"}
        };
        
        top5ProductsTable = new JTable(data, columns);
        top5ProductsTable.setRowHeight(25);
        top5ProductsTable.setShowGrid(false);
        top5ProductsTable.setIntercellSpacing(new Dimension(0, 0));
        top5ProductsTable.setSelectionBackground(new Color(232, 245, 255));
        
        JScrollPane scrollPane = new JScrollPane(top5ProductsTable);
        scrollPane.setBorder(null);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createRecentSalesPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setMaximumSize(new Dimension(1500, 100));
        panel.setBackground(CARD_COLOR);
        panel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
            "Ventas Recientes",
            TitledBorder.LEFT, TitledBorder.TOP,
            new Font("Segoe UI", Font.BOLD, 14)
        ));
        
        String[] columns = {"Cliente", "Producto", "Cantidad", "Total", "Fecha"};
        Object[][] data = {
            {"Juan Pérez", "Filtro de Aceite", "2", "$100.00", "27/08/2025 14:30"}
        };
        
        recentSalesTable = new JTable(data, columns);
        recentSalesTable.setRowHeight(25);
        recentSalesTable.setShowGrid(false);
        recentSalesTable.setIntercellSpacing(new Dimension(0, 0));
        recentSalesTable.setSelectionBackground(new Color(232, 245, 255));
        
        JScrollPane scrollPane = new JScrollPane(recentSalesTable);
        scrollPane.setBorder(null);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createSidePanel() {
        JPanel sidePanel = new JPanel();
        sidePanel.setLayout(new BoxLayout(sidePanel, BoxLayout.Y_AXIS));
        sidePanel.setOpaque(false);
        sidePanel.setPreferredSize(new Dimension(400, 0));
        
        // Stock crítico
        sidePanel.add(createCriticalStockPanel());
        sidePanel.add(Box.createVerticalStrut(15));
        
        // Gráfico de ventas mensuales (6 meses)
        sidePanel.add(createTopProductsPanel());
        sidePanel.add(Box.createVerticalStrut(15));
        
        // Gráfico de ventas diarias
        sidePanel.add(crearPanelNotificaciones());
        
        return sidePanel;
    }
    
    public enum ChartTimeFrame {
        DAILY, WEEKLY, MONTHLY, QUARTERLY, YEARLY
    }

    private JPanel createMonthlyBarChartPanel() {
        monthlyBarChartPanel = new JPanel(new BorderLayout());
        monthlyBarChartPanel.setBackground(CARD_COLOR);
        monthlyBarChartPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
            "Ventas por Período",
            TitledBorder.LEFT, TitledBorder.TOP,
            new Font("Segoe UI", Font.BOLD, 14)
        ));

        // Crear dataset - aquí pondrías tus datos reales del backend
        // Por defecto mostrar mensual, pero puede cambiar según el rango de fechas
        DefaultCategoryDataset dataset = createSampleDataset();
        ChartTimeFrame timeFrame = determineOptimalTimeFrame(dataset);
        
        // Crear gráfico de barras
        JFreeChart chart = ChartFactory.createBarChart(
            "", // título
            getAxisLabel(timeFrame), // etiqueta eje X dinámica
            "Ventas ($)", // etiqueta eje Y
            dataset // datos
        );
        
        // Personalizar el gráfico
        customizeChart(chart, dataset.getColumnCount(), timeFrame);
        
        // Crear panel del gráfico con scroll si es necesario
        ChartPanel chartPanel = new ChartPanel(chart);
        
        // Ajustar el tamaño según la cantidad de datos y timeframe
        int columnCount = dataset.getColumnCount();
        int barWidth = getOptimalBarWidth(timeFrame, columnCount);
        int minWidth = Math.max(400, columnCount * barWidth);
        chartPanel.setBackground(CARD_COLOR);
        
        // Determinar si necesita scroll
        boolean needsScroll = needsHorizontalScroll(timeFrame, columnCount);
        
        if (needsScroll) {
            JScrollPane scrollPane = new JScrollPane(chartPanel);
            scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
            scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
            scrollPane.setBorder(null);
            monthlyBarChartPanel.add(scrollPane, BorderLayout.CENTER);
        } else {
            monthlyBarChartPanel.add(chartPanel, BorderLayout.CENTER);
        }
        
        return monthlyBarChartPanel;
    }

    private void customizeChart(JFreeChart chart, int columnCount, ChartTimeFrame timeFrame) {
        CategoryPlot plot = chart.getCategoryPlot();
        
        // Colores y fondo
        plot.setBackgroundPaint(Color.WHITE);
        plot.setRangeGridlinePaint(Color.LIGHT_GRAY);
        plot.getRenderer().setSeriesPaint(0, PRIMARY_COLOR);
        
        // Configurar el eje X (categorías)
        CategoryAxis xAxis = plot.getDomainAxis();
        
        // Ajustar según el timeframe y cantidad de datos
        if (timeFrame == ChartTimeFrame.YEARLY && columnCount > 1) {
            // Muchos años: etiquetas horizontales, fuente más pequeña
            xAxis.setCategoryLabelPositions(CategoryLabelPositions.STANDARD);
            xAxis.setTickLabelFont(new Font("Segoe UI", Font.PLAIN, 11));
        } else if (timeFrame == ChartTimeFrame.QUARTERLY && columnCount > 8) {
            // Muchos trimestres: etiquetas inclinadas
            xAxis.setCategoryLabelPositions(CategoryLabelPositions.UP_45);
            xAxis.setTickLabelFont(new Font("Segoe UI", Font.PLAIN, 9));
        } else if (columnCount <= 6) {
            // Pocas columnas: etiquetas horizontales
            xAxis.setCategoryLabelPositions(CategoryLabelPositions.STANDARD);
            xAxis.setTickLabelFont(new Font("Segoe UI", Font.PLAIN, 10));
        } else if (columnCount <= 12) {
            // Columnas medianas: etiquetas inclinadas
            xAxis.setCategoryLabelPositions(CategoryLabelPositions.UP_45);
            xAxis.setTickLabelFont(new Font("Segoe UI", Font.PLAIN, 10));
        } else if (columnCount <= 24) {
            // Muchas columnas: etiquetas verticales
            xAxis.setCategoryLabelPositions(CategoryLabelPositions.UP_90);
            xAxis.setTickLabelFont(new Font("Segoe UI", Font.PLAIN, 9));
        } else {
            // Demasiadas columnas: etiquetas muy pequeñas y verticales
            xAxis.setCategoryLabelPositions(CategoryLabelPositions.UP_90);
            xAxis.setTickLabelFont(new Font("Segoe UI", Font.PLAIN, 8));
            // Mostrar solo cada N etiquetas para evitar solapamiento
            xAxis.setTickLabelFont(new Font("Segoe UI", Font.PLAIN, 8));
            xAxis.setCategoryMargin(0.02);
        }
        
        // Ajustar márgenes según la cantidad
        if (columnCount > 20) {
            xAxis.setCategoryMargin(0.02); // Muy poco espacio entre barras
            xAxis.setLowerMargin(0.005);
            xAxis.setUpperMargin(0.005);
        } else if (columnCount > 15) {
            xAxis.setCategoryMargin(0.05); // Menos espacio entre barras
            xAxis.setLowerMargin(0.01);
            xAxis.setUpperMargin(0.01);
        }
    }

    // Determina el mejor timeframe según la cantidad de datos
    private ChartTimeFrame determineOptimalTimeFrame(DefaultCategoryDataset dataset) {
        int columnCount = dataset.getColumnCount();
        
        if (columnCount <= 31) {
            return ChartTimeFrame.DAILY;
        } else if (columnCount <= 52) {
            return ChartTimeFrame.WEEKLY; 
        } else if (columnCount <= 24) {
            return ChartTimeFrame.MONTHLY;
        } else if (columnCount <= 20) {
            return ChartTimeFrame.QUARTERLY;
        } else {
            return ChartTimeFrame.YEARLY;
        }
    }

    // Determina si necesita scroll horizontal
    private boolean needsHorizontalScroll(ChartTimeFrame timeFrame, int columnCount) {
        switch (timeFrame) {
            case DAILY:
                return columnCount > 15;
            case WEEKLY:
                return columnCount > 12;
            case MONTHLY:
                return columnCount > 8;
            case QUARTERLY:
                return columnCount > 6;
            case YEARLY:
                return columnCount > 5;
            default:
                return columnCount > 8;
        }
    }

    // Calcula el ancho óptimo de las barras
    private int getOptimalBarWidth(ChartTimeFrame timeFrame, int columnCount) {
        switch (timeFrame) {
            case DAILY:
                return columnCount > 30 ? 25 : 40;
            case WEEKLY:
                return columnCount > 20 ? 35 : 50;
            case MONTHLY:
                return 60;
            case QUARTERLY:
                return 80;
            case YEARLY:
                return 100;
            default:
                return 60;
        }
    }

    // Etiqueta dinámica para el eje X
    private String getAxisLabel(ChartTimeFrame timeFrame) {
        switch (timeFrame) {
            case DAILY: return "Día";
            case WEEKLY: return "Semana";
            case MONTHLY: return "Mes";
            case QUARTERLY: return "Trimestre";
            case YEARLY: return "Año";
            default: return "Período";
        }
    }

    // Método principal para actualizar el gráfico desde tu backend
    /*public void updateChartWithDateRange(Date startDate, Date endDate, java.util.List<SalesData> salesData) {
        SwingUtilities.invokeLater(() -> {
            // Determinar el mejor timeframe según el rango de fechas
            ChartTimeFrame optimalTimeFrame = determineTimeFrameByDateRange(startDate, endDate, salesData.size());
            
            // Agrupar datos según el timeframe
            DefaultCategoryDataset dataset = groupDataByTimeFrame(salesData, optimalTimeFrame);
            
            // Actualizar el título del panel
            updatePanelTitle(optimalTimeFrame, startDate, endDate);
            
            // Recrear el gráfico
            recreateChart(dataset, optimalTimeFrame);
        });
    }*/

    // Determina el timeframe óptimo basado en el rango de fechas
    private ChartTimeFrame determineTimeFrameByDateRange(Date startDate, Date endDate, int dataPoints) {
        long daysDifference = (endDate.getTime() - startDate.getTime()) / (1000 * 60 * 60 * 24);
        
        if (daysDifference <= 31) {
            return ChartTimeFrame.DAILY;
        } else if (daysDifference <= 90) {
            return ChartTimeFrame.WEEKLY;
        } else if (daysDifference <= 730) { // 2 años
            return ChartTimeFrame.MONTHLY;
        } else if (daysDifference <= 1460) { // 4 años
            return ChartTimeFrame.QUARTERLY;
        } else {
            return ChartTimeFrame.YEARLY;
        }
    }

    // Agrupa los datos según el timeframe especificado
    /*private DefaultCategoryDataset groupDataByTimeFrame(java.util.List<SalesData> salesData, ChartTimeFrame timeFrame) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        
        // Aquí implementarías la lógica de agrupación según tu estructura de datos
        // Este es un ejemplo simplificado
        java.util.Map<String, Double> groupedData = new java.util.LinkedHashMap<>();
        
        for (SalesData data : salesData) {
            String key = formatDateForTimeFrame(data.getDate(), timeFrame);
            groupedData.merge(key, data.getAmount(), Double::sum);
        }
        
        // Agregar al dataset
        for (java.util.Map.Entry<String, Double> entry : groupedData.entrySet()) {
            dataset.addValue(entry.getValue(), "Ventas", entry.getKey());
        }
        
        return dataset;
    }*/

    // Formatea la fecha según el timeframe
    private String formatDateForTimeFrame(Date date, ChartTimeFrame timeFrame) {
        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.setTime(date);
        
        switch (timeFrame) {
            case DAILY:
                return String.format("%02d/%02d", cal.get(java.util.Calendar.DAY_OF_MONTH), 
                                                cal.get(java.util.Calendar.MONTH) + 1);
            case WEEKLY:
                int week = cal.get(java.util.Calendar.WEEK_OF_YEAR);
                return String.format("S%02d/%d", week, cal.get(java.util.Calendar.YEAR));
            case MONTHLY:
                String[] months = {"Ene", "Feb", "Mar", "Abr", "May", "Jun",
                                 "Jul", "Ago", "Sep", "Oct", "Nov", "Dic"};
                return months[cal.get(java.util.Calendar.MONTH)] + "/" + cal.get(java.util.Calendar.YEAR);
            case QUARTERLY:
                int quarter = (cal.get(java.util.Calendar.MONTH) / 3) + 1;
                return String.format("Q%d/%d", quarter, cal.get(java.util.Calendar.YEAR));
            case YEARLY:
                return String.valueOf(cal.get(java.util.Calendar.YEAR));
            default:
                return date.toString();
        }
    }

    // Actualiza el título del panel según el timeframe
    private void updatePanelTitle(ChartTimeFrame timeFrame, Date startDate, Date endDate) {
        String title = "Ventas por " + getAxisLabel(timeFrame);
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy");
        title += String.format(" (%s - %s)", sdf.format(startDate), sdf.format(endDate));
        
        javax.swing.border.TitledBorder border = (javax.swing.border.TitledBorder) monthlyBarChartPanel.getBorder();
        border.setTitle(title);
    }

    // Recrea el gráfico completo
    private void recreateChart(DefaultCategoryDataset dataset, ChartTimeFrame timeFrame) {
        // Limpiar el panel actual
        monthlyBarChartPanel.removeAll();
        
        // Crear nuevo gráfico
        JFreeChart chart = ChartFactory.createBarChart(
            "", getAxisLabel(timeFrame), "Ventas ($)", dataset
        );
        
        customizeChart(chart, dataset.getColumnCount(), timeFrame);
        
        ChartPanel chartPanel = new ChartPanel(chart);
        int columnCount = dataset.getColumnCount();
        int barWidth = getOptimalBarWidth(timeFrame, columnCount);
        int minWidth = Math.max(400, columnCount * barWidth);
        chartPanel.setPreferredSize(new Dimension(minWidth, 250));
        chartPanel.setBackground(CARD_COLOR);
        
        boolean needsScroll = needsHorizontalScroll(timeFrame, columnCount);
        
        if (needsScroll) {
            JScrollPane scrollPane = new JScrollPane(chartPanel);
            scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
            scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
            scrollPane.setBorder(null);
            monthlyBarChartPanel.add(scrollPane, BorderLayout.CENTER);
        } else {
            monthlyBarChartPanel.add(chartPanel, BorderLayout.CENTER);
        }
        
        // Refrescar la UI
        monthlyBarChartPanel.revalidate();
        monthlyBarChartPanel.repaint();
    }

    private DefaultCategoryDataset createSampleDataset() {
        // Este método lo reemplazarías con datos reales de tu backend
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        
        // Ejemplo con muchos meses para probar el scroll
        String[] months = {"Ene", "Feb", "Mar", "Abr", "May", "Jun", 
                          "Jul", "Ago", "Sep", "Oct", "Nov", "Dic"};
        double[] sales = {35000, 42000, 38000, 47000, 52000, 45000,
                         48000, 51000, 44000, 49000, 53000, 47000};
        
        for (int i = 0; i < months.length; i++) {
            dataset.addValue(sales[i], "Ventas", months[i]);
        }
        
        return dataset;
    }

    /*public DefaultCategoryDataset createDatasetFromBackend(java.util.List<SalesData> salesData) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        
        for (SalesData data : salesData) {
            dataset.addValue(data.getAmount(), "Ventas", data.getPeriod());
        }
        
        return dataset;
    }*/

    
    private JPanel createCriticalStockPanel() {
        criticalStockPanel = new JPanel(new BorderLayout());
        criticalStockPanel.setBackground(CARD_COLOR);
        criticalStockPanel.setPreferredSize(new Dimension(WIDTH, 250));
        criticalStockPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
            "Stock Crítico (< 5 unidades)",
            TitledBorder.LEFT, TitledBorder.TOP,
            new Font("Segoe UI", Font.BOLD, 14)
        ));
        
        JPanel itemsPanel = new JPanel();
        itemsPanel.setLayout(new BoxLayout(itemsPanel, BoxLayout.Y_AXIS));
        itemsPanel.setBackground(CARD_COLOR);
        itemsPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        String[] criticalItems = {
            "Filtro de Aire Honda - 3 unidades",
            "Correa de Distribución - 5 unidades", 
            "Sensor de Oxígeno - 2 unidades",
            "Radiador Nissan - 4 unidades"
        };
        
        for (String item : criticalItems) {
            JLabel label = new JLabel("⚠ " + item);
            label.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            label.setForeground(DANGER_COLOR);
            label.setBorder(BorderFactory.createEmptyBorder(3, 0, 3, 0));
            itemsPanel.add(label);
        }
        
        JScrollPane scrollPane = new JScrollPane(itemsPanel);
        scrollPane.setBorder(null);
        scrollPane.setPreferredSize(new Dimension(0, 120));
        criticalStockPanel.add(scrollPane, BorderLayout.CENTER);
        
        return criticalStockPanel;
    }
    
    private JPanel createSalesChartPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(CARD_COLOR);
        panel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
            "Ventas Últimos 7 Días",
            TitledBorder.LEFT, TitledBorder.TOP,
            new Font("Segoe UI", Font.BOLD, 14)
        ));
        
        // Gráfico simple simulado
        JPanel chartPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                int width = getWidth() - 40;
                int height = getHeight() - 40;
                int[] values = {120, 180, 150, 220, 190, 250, 200};
                
                g2d.setColor(Color.LIGHT_GRAY);
                for (int i = 1; i <= 5; i++) {
                    int y = 20 + (height * i / 5);
                    g2d.drawLine(20, y, width + 20, y);
                }
                
                g2d.setColor(PRIMARY_COLOR);
                g2d.setStroke(new BasicStroke(2));
                
                for (int i = 0; i < values.length - 1; i++) {
                    int x1 = 20 + (width * i / (values.length - 1));
                    int y1 = height + 20 - (values[i] * height / 300);
                    int x2 = 20 + (width * (i + 1) / (values.length - 1));
                    int y2 = height + 20 - (values[i + 1] * height / 300);
                    
                    g2d.drawLine(x1, y1, x2, y2);
                    g2d.fillOval(x1 - 3, y1 - 3, 6, 6);
                }
                
                g2d.fillOval(20 + width - 3, height + 20 - (values[values.length-1] * height / 300) - 3, 6, 6);
            }
        };
        
        chartPanel.setPreferredSize(new Dimension(0, 180));
        chartPanel.setBackground(CARD_COLOR);
        panel.add(chartPanel, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel crearPanelNotificaciones() {
        notificationsPanel = new JPanel(new BorderLayout());
        notificationsPanel.setBackground(new Color(255, 249, 230));
        notificationsPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(241, 196, 15), 2),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        notificationsPanel.setPreferredSize(new Dimension(WIDTH, 250));
        
        JLabel titulo = new JLabel(" Notificaciones");
        titulo.setFont(new Font("Segoe UI", Font.BOLD, 14));
        titulo.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        
        JTextArea notificaciones = new JTextArea();
        notificaciones.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        notificaciones.setEditable(false);
        notificaciones.setOpaque(false);
        notificaciones.setLineWrap(true);
        notificaciones.setWrapStyleWord(true);
        notificaciones.setText(
            "• 5 productos con stock bajo\n" +
            "• 3 pedidos pendientes de entrega\n" +
            "• 2 facturas por cobrar vencen hoy\n" +
            "• Backup programado: 18:00\n"
        );
        
        notificationsPanel.add(titulo, BorderLayout.NORTH);
        notificationsPanel.add(new JScrollPane(notificaciones), BorderLayout.CENTER);
        
        return notificationsPanel;
    }
    
    private JPanel createFooterPanel() {
        JPanel footerPanel = new JPanel(new BorderLayout());
        footerPanel.setBackground(new Color(52, 73, 94));
        footerPanel.setBorder(BorderFactory.createEmptyBorder(10, 25, 10, 25));
        footerPanel.setPreferredSize(new Dimension(0, 40));
        
        JLabel statusLabel = new JLabel("Sistema: Conectado | Base de Datos: OK | Última actualización: " + 
                                      new SimpleDateFormat("HH:mm:ss").format(new Date()));
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        statusLabel.setForeground(Color.WHITE);
        
        JLabel versionLabel = new JLabel("AutoPartes Pro v2.1.0");
        versionLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        versionLabel.setForeground(Color.LIGHT_GRAY);
        
        footerPanel.add(statusLabel, BorderLayout.WEST);
        footerPanel.add(versionLabel, BorderLayout.EAST);
        
        return footerPanel;
    }

	@Override
	public void refresh() {
		createMetricsPanel();
		createMonthlyBarChartPanel();
		createCriticalStockPanel();
		loadRecentSalesTable();
		loadTop5ProductsTable();
		crearPanelNotificaciones();
	}

	private void loadRecentSalesTable(){
		
	}
	
	private void loadTop5ProductsTable(){
		
	}
	
}