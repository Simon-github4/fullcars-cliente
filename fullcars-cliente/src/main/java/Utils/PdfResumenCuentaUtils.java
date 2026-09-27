package Utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.colors.Color;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.HorizontalAlignment;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.itextpdf.layout.properties.VerticalAlignment;

import model.client.entities.Customer;

/** Resumen de cuenta del cliente en PDF: ventas pendientes, creditos a favor y balance (saldo, credito y neto). */
public class PdfResumenCuentaUtils {

    /** Una fila del resumen. En los creditos, total y pendiente vienen en negativo (como en pantalla). */
    public record Linea(LocalDate fecha, String tipo, Long nroVenta, String siniestro, String cae,
                        BigDecimal total, BigDecimal pendiente, boolean credito) {}

    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final Color AZUL = new DeviceRgb(12, 81, 169);
    private static final Color ROJO = new DeviceRgb(198, 40, 40);
    private static final Color VERDE = new DeviceRgb(46, 125, 50);
    private static final Color GRIS = new DeviceRgb(108, 117, 125);
    private static final Color GRIS_CLARO = new DeviceRgb(244, 246, 249);
    private static final Color BORDE = new DeviceRgb(222, 226, 230);

    /**
     * @param desde,hasta     rango del filtro de fechas de la pantalla (null si no hay)
     * @param filtroNroVenta  texto del filtro "Nro Venta" (null o vacio si no hay)
     * @param saldoCuenta     deuda total de la cuenta (todas las ventas pendientes)
     * @param creditoCuenta   saldo a favor total de la cuenta
     */
    public static byte[] generarResumen(Customer customer, List<Linea> lineas, LocalDate desde, LocalDate hasta,
                                        String filtroNroVenta, BigDecimal saldoCuenta, BigDecimal creditoCuenta) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfDocument pdf = new PdfDocument(new PdfWriter(baos));
        Document document = new Document(pdf, PageSize.A4, false); // false: permite numerar paginas al final
        document.setMargins(36, 36, 48, 36);

        PdfFont regular = PdfFontFactory.createFont(StandardFonts.HELVETICA);
        PdfFont bold = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);
        document.setFont(regular).setFontSize(9);

        encabezado(document, bold);
        datosCliente(document, bold, customer, desde, hasta, filtroNroVenta);
        tablaMovimientos(document, bold, lineas);

        BigDecimal saldoListado = BigDecimal.ZERO;
        BigDecimal creditoListado = BigDecimal.ZERO;
        for (Linea l : lineas) {
            if (l.credito()) creditoListado = creditoListado.add(nz(l.pendiente()).negate());
            else saldoListado = saldoListado.add(nz(l.pendiente()));
        }

        boolean filtrado = desde != null || (filtroNroVenta != null && !filtroNroVenta.isBlank());
        if (filtrado) {
            balance(document, bold, "TOTALES DE LO LISTADO", saldoListado, creditoListado);
            balance(document, bold, "SALDO TOTAL DE LA CUENTA", nz(saldoCuenta), nz(creditoCuenta));
        } else {
            balance(document, bold, "BALANCE DE LA CUENTA", saldoListado, creditoListado);
        }

        piePaginas(document, pdf);
        document.close();
        return baos.toByteArray();
    }

    // ------------------------------------------------------------------------------------------ secciones

    private static void encabezado(Document document, PdfFont bold) throws IOException {
        Table header = new Table(UnitValue.createPercentArray(new float[] { 45, 55 })).useAllAvailableWidth();

        Cell logoCell = new Cell().setBorder(Border.NO_BORDER).setVerticalAlignment(VerticalAlignment.MIDDLE);
        try (InputStream is = PdfResumenCuentaUtils.class.getResourceAsStream(PdfRemitosUtils.LOGO_PATH)) {
            if (is != null) logoCell.add(new Image(ImageDataFactory.create(is.readAllBytes())).scaleToFit(120, 85));
        }
        header.addCell(logoCell);

        Cell right = new Cell().setBorder(Border.NO_BORDER).setTextAlignment(TextAlignment.RIGHT)
                .setVerticalAlignment(VerticalAlignment.MIDDLE);
        right.add(new Paragraph("RESUMEN DE CUENTA").setFont(bold).setFontSize(18).setFontColor(AZUL));
        right.add(new Paragraph(PdfRemitosUtils.ADDRESS + " - " + PdfRemitosUtils.PHONE + "\n" + PdfRemitosUtils.LOCATION)
                .setFontSize(9).setFontColor(GRIS).setMarginTop(2));
        right.add(new Paragraph("Fecha de emisión: " + LocalDate.now().format(DATE)).setFontSize(9).setMarginTop(6));
        header.addCell(right);

        document.add(header);
    }

    private static void datosCliente(Document document, PdfFont bold, Customer c, LocalDate desde, LocalDate hasta,
                                     String filtroNroVenta) {
        Table box = new Table(UnitValue.createPercentArray(new float[] { 18, 82 })).useAllAvailableWidth()
                .setMarginTop(12).setBackgroundColor(GRIS_CLARO).setBorder(new SolidBorder(BORDE, 0.8f));
        fila(box, bold, "Cliente", c != null ? c.getFullName() : "-");
        if (c != null && notBlank(c.getCuit())) fila(box, bold, "CUIT", c.getCuit());
        else if (c != null && notBlank(c.getDni())) fila(box, bold, "DNI", c.getDni());
        if (c != null && notBlank(c.getAdress())) fila(box, bold, "Domicilio", c.getAdress());

        String periodo = desde != null
                ? "Del " + desde.format(DATE) + " al " + (hasta != null ? hasta : desde).format(DATE)
                : "Todas las ventas pendientes y créditos a favor";
        if (filtroNroVenta != null && !filtroNroVenta.isBlank()) periodo += "  (filtrado por Nro Venta: " + filtroNroVenta + ")";
        fila(box, bold, "Período", periodo);
        document.add(box);
    }

    private static void fila(Table t, PdfFont bold, String label, String value) {
        t.addCell(new Cell().setBorder(Border.NO_BORDER).setPaddingLeft(8).add(new Paragraph(label + ":").setFont(bold).setFontSize(10)));
        t.addCell(new Cell().setBorder(Border.NO_BORDER).add(new Paragraph(value).setFontSize(10)));
    }

    private static void tablaMovimientos(Document document, PdfFont bold, List<Linea> lineas) {
        String[] headers = { "Fecha", "Tipo", "Venta", "Siniestro", "CAE", "Total", "Pendiente" };
        // Anchos pensados para siniestros tipo "56/02/108397/00", CAE de 14 digitos y montos de millones sin cortar linea.
        Table t = new Table(UnitValue.createPercentArray(new float[] { 10.5f, 20.5f, 8, 15, 15, 15, 16 })).useAllAvailableWidth()
                .setMarginTop(14);
        for (int i = 0; i < headers.length; i++) {
            t.addHeaderCell(new Cell().setBackgroundColor(AZUL).setBorder(new SolidBorder(AZUL, 0.5f))
                    .setTextAlignment(i >= 5 ? TextAlignment.RIGHT : TextAlignment.LEFT)
                    .add(new Paragraph(headers[i]).setFont(bold).setFontColor(DeviceRgb.WHITE).setFontSize(8.5f)));
        }

        if (lineas.isEmpty()) {
            t.addCell(new Cell(1, headers.length).setBorder(new SolidBorder(BORDE, 0.5f)).setTextAlignment(TextAlignment.CENTER)
                    .add(new Paragraph("Sin ventas pendientes ni créditos a favor").setFontColor(GRIS)));
        }
        boolean zebra = false;
        for (Linea l : lineas) {
            Color bg = zebra ? GRIS_CLARO : DeviceRgb.WHITE;
            zebra = !zebra;
            Color montoColor = l.credito() ? ROJO : DeviceRgb.BLACK;
            t.addCell(celda(l.fecha() != null ? l.fecha().format(DATE) : "-", bg, TextAlignment.LEFT, null, null));
            t.addCell(celda(nvl(l.tipo()), bg, TextAlignment.LEFT, l.credito() ? ROJO : null, l.credito() ? bold : null));
            t.addCell(celda(l.nroVenta() != null ? String.valueOf(l.nroVenta()) : "-", bg, TextAlignment.LEFT, null, null));
            // sin siniestro: "Particular" si hay venta asociada, "-" si no (p.ej. excedente de pago)
            String siniestro = notBlank(l.siniestro()) ? l.siniestro() : (l.nroVenta() != null ? "Particular" : "-");
            t.addCell(celda(siniestro, bg, TextAlignment.LEFT, null, null));
            t.addCell(celda(notBlank(l.cae()) ? l.cae() : "-", bg, TextAlignment.LEFT, null, null));
            t.addCell(celda(money(l.total()), bg, TextAlignment.RIGHT, montoColor, null));
            t.addCell(celda(money(l.pendiente()), bg, TextAlignment.RIGHT, montoColor, bold));
        }
        document.add(t);
    }

    private static Cell celda(String text, Color bg, TextAlignment align, Color color, PdfFont font) {
        Paragraph p = new Paragraph(text).setFontSize(8);
        if (color != null) p.setFontColor(color);
        if (font != null) p.setFont(font);
        return new Cell().setBackgroundColor(bg).setBorder(Border.NO_BORDER)
                .setBorderBottom(new SolidBorder(BORDE, 0.5f)).setTextAlignment(align).add(p);
    }

    /** Saldo (deuda de ventas), credito a favor y neto (saldo - credito). */
    private static void balance(Document document, PdfFont bold, String titulo, BigDecimal saldo, BigDecimal credito) {
        BigDecimal neto = saldo.subtract(credito);
        String netoLabel = neto.signum() > 0 ? "NETO A PAGAR" : neto.signum() < 0 ? "SALDO A FAVOR DEL CLIENTE" : "NETO";

        Table b = new Table(UnitValue.createPercentArray(new float[] { 62, 38 })).setWidth(UnitValue.createPercentValue(50))
                .setHorizontalAlignment(HorizontalAlignment.RIGHT).setMarginTop(14).setBorder(new SolidBorder(BORDE, 0.8f));
        b.setKeepTogether(true); // el balance nunca queda partido entre dos paginas
        b.addCell(new Cell(1, 2).setBorder(Border.NO_BORDER).setBackgroundColor(GRIS_CLARO)
                .add(new Paragraph(titulo).setFont(bold).setFontSize(9).setFontColor(GRIS)));
        balanceFila(b, "Saldo (ventas pendientes)", money(saldo), DeviceRgb.BLACK, null, 10);
        balanceFila(b, "Crédito a favor", money(credito.negate()), ROJO, null, 10);
        Cell sepL = new Cell().setBorder(Border.NO_BORDER).setBorderTop(new SolidBorder(DeviceRgb.BLACK, 1f));
        Cell sepR = new Cell().setBorder(Border.NO_BORDER).setBorderTop(new SolidBorder(DeviceRgb.BLACK, 1f));
        b.addCell(sepL.add(new Paragraph(netoLabel).setFont(bold).setFontSize(11)));
        b.addCell(sepR.setTextAlignment(TextAlignment.RIGHT)
                .add(new Paragraph(money(neto.abs())).setFont(bold).setFontSize(12).setFontColor(neto.signum() < 0 ? VERDE : AZUL)));
        document.add(b);
    }

    private static void balanceFila(Table b, String label, String value, Color color, PdfFont font, float size) {
        b.addCell(new Cell().setBorder(Border.NO_BORDER).add(new Paragraph(label).setFontSize(size)));
        Paragraph p = new Paragraph(value).setFontSize(size).setFontColor(color);
        if (font != null) p.setFont(font);
        b.addCell(new Cell().setBorder(Border.NO_BORDER).setTextAlignment(TextAlignment.RIGHT).add(p));
    }

    private static void piePaginas(Document document, PdfDocument pdf) {
        int n = pdf.getNumberOfPages();
        String generado = "Generado el " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
        for (int i = 1; i <= n; i++) {
            float y = 24;
            document.showTextAligned(new Paragraph(generado).setFontSize(8).setFontColor(GRIS),
                    36, y, i, TextAlignment.LEFT, VerticalAlignment.BOTTOM, 0);
            document.showTextAligned(new Paragraph("Página " + i + " de " + n).setFontSize(8).setFontColor(GRIS),
                    pdf.getPage(i).getPageSize().getWidth() - 36, y, i, TextAlignment.RIGHT, VerticalAlignment.BOTTOM, 0);
        }
    }

    // ------------------------------------------------------------------------------------------ helpers

    /** "$ 1.234,50" / "-$ 1.234,50", siempre con 2 decimales. */
    static String money(BigDecimal v) {
        NumberFormat f = NumberFormat.getNumberInstance(Locale.forLanguageTag("es-AR"));
        f.setMinimumFractionDigits(2);
        f.setMaximumFractionDigits(2);
        BigDecimal x = nz(v).setScale(2, RoundingMode.HALF_UP);
        return (x.signum() < 0 ? "-$ " : "$ ") + f.format(x.abs());
    }

    private static BigDecimal nz(BigDecimal v) { return v != null ? v : BigDecimal.ZERO; }
    /** Tiene al menos una letra o numero (descarta vacios y rellenos como "." o "-" cargados en los datos del cliente). */
    private static boolean notBlank(String s) { return s != null && s.codePoints().anyMatch(Character::isLetterOrDigit); }
    private static String nvl(String s) { return s != null ? s : "-"; }
}
