package views;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.temporal.WeekFields;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.border.TitledBorder;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.data.category.DefaultCategoryDataset;

import dtos.SalesData;

public class SalesBarChart {

    // Enum para diferentes tipos de agrupación temporal
    public enum ChartTimeFrame {
        DAILY, WEEKLY, MONTHLY, QUARTERLY, YEARLY
    }

    // Configuración de colores
    private final Color primaryColor;
    private final Color cardColor;
    private final Color backgroundColor;

    // Componentes UI
    private JPanel chartPanel;
    private JFreeChart chart;
    private DefaultCategoryDataset dataset;
    private ChartTimeFrame currentTimeFrame;

    // Constructor con colores personalizados
    public SalesBarChart(Color primaryColor, Color cardColor, Color backgroundColor) {
        this.primaryColor = primaryColor;
        this.cardColor = cardColor;
        this.backgroundColor = backgroundColor;
        this.currentTimeFrame = ChartTimeFrame.MONTHLY;
        initializeChart();
    }

    // Constructor con colores por defecto
    public SalesBarChart() {
        this(new Color(41, 128, 185), Color.WHITE, new Color(236, 240, 241));
    }

    /**
     * Inicializa el gráfico con datos de ejemplo
     */
    private void initializeChart() {
        createChartPanel();
        //loadSampleData();
    }

    /**
     * Crea el panel principal del gráfico
     */
    private void createChartPanel() {
        chartPanel = new JPanel(new BorderLayout());
        chartPanel.setBackground(cardColor);
        chartPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
            "Ventas por Período",
            TitledBorder.LEFT, TitledBorder.TOP,
            new Font("Segoe UI", Font.BOLD, 14)
        ));
    }

    /**
     * Método principal para actualizar el gráfico con un rango de fechas
     */
    public void updateChart(LocalDate startDate, LocalDate endDate, List<SalesData> salesData) {
        SwingUtilities.invokeLater(() -> {
            currentTimeFrame = determineTimeFrameByDateRange(startDate, endDate);

            dataset = groupDataByTimeFrame(salesData, currentTimeFrame, startDate, endDate);

            updatePanelTitle(currentTimeFrame, startDate, endDate);
            recreateChart();
        });
    }

    /**
     * Carga datos de ejemplo para testing
     */
    public void loadSampleData() {
        dataset = createSampleDataset();
        currentTimeFrame = ChartTimeFrame.MONTHLY;
        recreateChart();
    }

    /**
     * Obtiene el panel del gráfico para agregar a la UI
     */
    public JPanel getChartPanel() {
        return chartPanel;
    }

    /**
     * Determina el timeframe óptimo basado en el rango de fechas
     */
    private ChartTimeFrame determineTimeFrameByDateRange(LocalDate startDate, LocalDate endDate) {
        long daysDifference = ChronoUnit.DAYS.between(startDate, endDate);

        if (daysDifference <= 31) {
            return ChartTimeFrame.DAILY;
        } else if (daysDifference <= 90) {
            return ChartTimeFrame.WEEKLY;
        } else if (daysDifference <= 730) {
            return ChartTimeFrame.MONTHLY;
        } else if (daysDifference <= 1460) {
            return ChartTimeFrame.QUARTERLY;
        } else {
            return ChartTimeFrame.YEARLY;
        }
    }

    /**
     * Agrupa los datos según el timeframe especificado
     */
    private DefaultCategoryDataset groupDataByTimeFrame(
            List<SalesData> salesData,
            ChartTimeFrame timeFrame,
            LocalDate startDate,
            LocalDate endDate) {

        DefaultCategoryDataset groupedDataset = new DefaultCategoryDataset();
        Map<String, Long> groupedData = new LinkedHashMap<>();

        // 1. Inicializar todas las categorías en el rango con 0
        LocalDate cursor = startDate;
        while (!cursor.isAfter(endDate)) {
            String key = formatDateForTimeFrame(cursor, timeFrame);
            groupedData.putIfAbsent(key, 0L);

            switch (timeFrame) {
                case DAILY: cursor = cursor.plusDays(1); break;
                case WEEKLY: cursor = cursor.plusWeeks(1); break;
                case MONTHLY: cursor = cursor.plusMonths(1); break;
                case QUARTERLY: cursor = cursor.plusMonths(3); break;
                case YEARLY: cursor = cursor.plusYears(1); break;
            }
        }

        // 2. Sumar las ventas recibidas
        for (SalesData data : salesData) {
            String key = formatDateForTimeFrame(data.getDate(), timeFrame);
            groupedData.merge(key, data.getAmount(), Long::sum);
        }

        // 3. Pasar al dataset
        for (Map.Entry<String, Long> entry : groupedData.entrySet()) {
            groupedDataset.addValue(entry.getValue(), "Ventas", entry.getKey());
        }

        return groupedDataset;
    }

    /**
     * Formatea la fecha según el timeframe
     */
    private String formatDateForTimeFrame(LocalDate date, ChartTimeFrame timeFrame) {
        switch (timeFrame) {
            case DAILY:
                return String.format("%02d/%02d", date.getDayOfMonth(), date.getMonthValue());
            case WEEKLY:
                int week = date.get(WeekFields.ISO.weekOfYear());
                return String.format("S%02d/%d", week, date.getYear());
            case MONTHLY:
                String[] months = {"Ene", "Feb", "Mar", "Abr", "May", "Jun",
                                   "Jul", "Ago", "Sep", "Oct", "Nov", "Dic"};
                return months[date.getMonthValue() - 1] + "/" + date.getYear();
            case QUARTERLY:
                int quarter = ((date.getMonthValue() - 1) / 3) + 1;
                return String.format("Q%d/%d", quarter, date.getYear());
            case YEARLY:
                return String.valueOf(date.getYear());
            default:
                return date.toString();
        }
    }

    /**
     * Recrea el gráfico completo
     */
    private void recreateChart() {
        chartPanel.removeAll();

        chart = ChartFactory.createBarChart(
            "",
            getAxisLabel(currentTimeFrame),
            "Ventas ($)",
            dataset
        );

        customizeChart(currentTimeFrame);

        ChartPanel jfreeChartPanel = new ChartPanel(chart);
        configureChartPanelSize(jfreeChartPanel, currentTimeFrame);

        chartPanel.revalidate();
        chartPanel.repaint();
    }

    /**
     * Personaliza la apariencia del gráfico
     */
    private void customizeChart(ChartTimeFrame timeFrame) {
        CategoryPlot plot = chart.getCategoryPlot();
        int columnCount = dataset.getColumnCount();

        plot.setBackgroundPaint(Color.WHITE);
        plot.setRangeGridlinePaint(Color.LIGHT_GRAY);
        plot.getRenderer().setSeriesPaint(0, primaryColor);

        CategoryAxis xAxis = plot.getDomainAxis();
        configureXAxis(xAxis, timeFrame, columnCount);
    }

    /**
     * Configura el eje X según el timeframe y cantidad de datos
     */
    private void configureXAxis(CategoryAxis xAxis, ChartTimeFrame timeFrame, int columnCount) {
        if (timeFrame == ChartTimeFrame.YEARLY && columnCount > 1) {
            xAxis.setCategoryLabelPositions(CategoryLabelPositions.STANDARD);
            xAxis.setTickLabelFont(new Font("Segoe UI", Font.PLAIN, 11));
        } else if (timeFrame == ChartTimeFrame.QUARTERLY && columnCount > 8) {
            xAxis.setCategoryLabelPositions(CategoryLabelPositions.UP_45);
            xAxis.setTickLabelFont(new Font("Segoe UI", Font.PLAIN, 9));
        } else if (columnCount <= 6) {
            xAxis.setCategoryLabelPositions(CategoryLabelPositions.STANDARD);
            xAxis.setTickLabelFont(new Font("Segoe UI", Font.PLAIN, 10));
        } else if (columnCount <= 12) {
            xAxis.setCategoryLabelPositions(CategoryLabelPositions.UP_45);
            xAxis.setTickLabelFont(new Font("Segoe UI", Font.PLAIN, 10));
        } else if (columnCount <= 24) {
            xAxis.setCategoryLabelPositions(CategoryLabelPositions.UP_90);
            xAxis.setTickLabelFont(new Font("Segoe UI", Font.PLAIN, 9));
        } else {
            xAxis.setCategoryLabelPositions(CategoryLabelPositions.UP_90);
            xAxis.setTickLabelFont(new Font("Segoe UI", Font.PLAIN, 8));
            xAxis.setCategoryMargin(0.02);
        }

        if (columnCount > 20) {
            xAxis.setCategoryMargin(0.02);
            xAxis.setLowerMargin(0.005);
            xAxis.setUpperMargin(0.005);
        } else if (columnCount > 15) {
            xAxis.setCategoryMargin(0.05);
            xAxis.setLowerMargin(0.01);
            xAxis.setUpperMargin(0.01);
        }
    }

    /**
     * Configura el tamaño del ChartPanel y agrega scroll si es necesario
     */
    private void configureChartPanelSize(ChartPanel jfreeChartPanel, ChartTimeFrame timeFrame) {
        int columnCount = dataset.getColumnCount();
        int barWidth = getOptimalBarWidth(timeFrame, columnCount);
        int minWidth = Math.max(400, columnCount * barWidth);

        jfreeChartPanel.setPreferredSize(new Dimension(minWidth, 250));
        jfreeChartPanel.setBackground(cardColor);

        boolean needsScroll = needsHorizontalScroll(timeFrame, columnCount);

        if (needsScroll) {
            JScrollPane scrollPane = new JScrollPane(jfreeChartPanel);
            scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
            scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
            scrollPane.setBorder(null);
            chartPanel.add(scrollPane, BorderLayout.CENTER);
        } else {
            chartPanel.add(jfreeChartPanel, BorderLayout.CENTER);
        }
    }

    private boolean needsHorizontalScroll(ChartTimeFrame timeFrame, int columnCount) {
        switch (timeFrame) {
            case DAILY: return columnCount > 15;
            case WEEKLY: return columnCount > 12;
            case MONTHLY: return columnCount > 8;
            case QUARTERLY: return columnCount > 6;
            case YEARLY: return columnCount > 5;
            default: return columnCount > 8;
        }
    }

    private int getOptimalBarWidth(ChartTimeFrame timeFrame, int columnCount) {
        switch (timeFrame) {
            case DAILY: return columnCount > 30 ? 25 : 40;
            case WEEKLY: return columnCount > 20 ? 35 : 50;
            case MONTHLY: return 60;
            case QUARTERLY: return 80;
            case YEARLY: return 100;
            default: return 60;
        }
    }

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

    private void updatePanelTitle(ChartTimeFrame timeFrame, LocalDate startDate, LocalDate endDate) {
        String title = "Ventas por " + getAxisLabel(timeFrame);
        if (startDate != null && endDate != null) {
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            title += String.format(" (%s - %s)", fmt.format(startDate), fmt.format(endDate));
        }

        TitledBorder border = (TitledBorder) chartPanel.getBorder();
        border.setTitle(title);
    }

    private DefaultCategoryDataset createSampleDataset() {
        DefaultCategoryDataset sampleDataset = new DefaultCategoryDataset();

        String[] months = {"Ene", "Feb", "Mar", "Abr", "May", "Jun",
                           "Jul", "Ago", "Sep", "Oct", "Nov", "Dic"};
        long[] sales = {35000, 42000, 38000, 47000, 52000, 45000,
                        48000, 51000, 44000, 49000, 53000, 47000};

        for (int i = 0; i < months.length; i++) {
            sampleDataset.addValue(sales[i], "Ventas", months[i]);
        }

        return sampleDataset;
    }

    public ChartTimeFrame getCurrentTimeFrame() {
        return currentTimeFrame;
    }

    public DefaultCategoryDataset getCurrentDataset() {
        return dataset;
    }

    public void updateTitle(String newTitle) {
        TitledBorder border = (TitledBorder) chartPanel.getBorder();
        border.setTitle(newTitle);
        chartPanel.repaint();
    }
}
