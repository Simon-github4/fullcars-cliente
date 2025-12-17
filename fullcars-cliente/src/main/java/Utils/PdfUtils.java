package Utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.List;

import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.HorizontalAlignment;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.VerticalAlignment;

import model.client.entities.Sale;
import model.client.entities.SaleDetail;

public class PdfUtils {

	//public static final String PDF_DESKTOP_PATH = System.getProperty("user.home") + File.separator + "Desktop" + File.separator + "A";
	public static final String LOGO_PATH = Icons.LOGO.getPath();
	public static final String ADDRESS = "Misiones Nº1869";
	public static final String PHONE = "[02291] 15519359";
	public static final String LOCATION = "Mar del Plata - Bs. As. Argentina";
	
	public static byte[] generateRemitoPdf(Sale sale, String plate, String taller, List<String> calidades) {
        try {
        	ByteArrayOutputStream baos = new ByteArrayOutputStream();//in memory file
        	PdfWriter writer = new PdfWriter(baos);
        	
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);
            createHeader(document, "REMITO ORIGINAL", sale, plate, taller);

            float[] columnWidths = {50, 370, 50};
            Table detailstable = new Table(columnWidths);
            detailstable.addHeaderCell(new Cell().add(new Paragraph("Cantidad").setBold()));
            detailstable.addHeaderCell(new Cell().add(new Paragraph("Detalle").setBold()));
            detailstable.addHeaderCell(new Cell().add(new Paragraph("")));

            List<SaleDetail> details = sale.getDetails();
            for (int i = 0; i < details.size(); i++) {
                SaleDetail detail = details.get(i);
                detailstable.addCell(String.valueOf(detail.getQuantity()));
                detailstable.addCell(detail.getCarPart().getName() + "  " +
                    ((detail.getCarPart().getDescription() != null) ? detail.getCarPart().getDescription() : ""));
                detailstable.addCell(calidades.get(i));
            }

            
            document.add(detailstable);
            document.add(new Paragraph("\nLa conformidad de este remito declara el perfecto estado de la mercadería recibida. No se aceptan reclamos pasadas 72hs.")
                    .setFontSize(9)
                    .setFontColor(ColorConstants.GRAY));
            
            document.add(new Paragraph("\n\n\nFirma / Aclaración ______________________________")
                    .setTextAlignment(TextAlignment.CENTER));
            
            document.close();
            System.out.println("PDF generado correctamente: " );
            
            return baos.toByteArray();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
	
	public static byte[] generatePresupuestoPdf(Sale sale, String plate, String taller, List<String> calidades) {
        try {
        	ByteArrayOutputStream baos = new ByteArrayOutputStream();//in memory file
        	PdfWriter writer = new PdfWriter(baos);
        	
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);
            createHeader(document, "PRESUPUESTO ORIGINAL", sale, plate, taller);
            
            float[] columnWidths = {50, 280, 90, 50};
            Table detailstable = new Table(columnWidths);
            detailstable.addHeaderCell(new Cell().add(new Paragraph("Cantidad").setBold()));
            detailstable.addHeaderCell(new Cell().add(new Paragraph("Detalle").setBold()));
            detailstable.addHeaderCell(new Cell().add(new Paragraph("Subtotal").setBold()));
            detailstable.addHeaderCell(new Cell().add(new Paragraph("")));

            List<SaleDetail> details = sale.getDetails();
            for (int i = 0; i < details.size(); i++) {
                SaleDetail detail = details.get(i);
                detailstable.addCell(String.valueOf(detail.getQuantity()));
            	detailstable.addCell(detail.getCarPart().getName() + "  " +
            		    ((detail.getCarPart().getDescription() != null) ? detail.getCarPart().getDescription() : ""));
            	detailstable.addCell(NumberFormatArg.format(detail.getSubTotal()));
                detailstable.addCell(calidades.get(i));
            }

            document.add(detailstable);

            BigDecimal total = sale.getTotal();

            document.add(new Paragraph("\nTotal: " + NumberFormatArg.format(total)).setBold());

            document.add(new Paragraph("\nLa conformidad de este remito declara el perfecto estado de la mercadería recibida. No se aceptan reclamos pasadas 72hs.")
                    .setFontSize(9)
                    .setFontColor(ColorConstants.GRAY));

            document.add(new Paragraph("\n\n\nFirma / Aclaración ______________________________")
                    .setTextAlignment(TextAlignment.CENTER));
            
            document.close();
            System.out.println("PDF generado correctamente: " );
            
            return baos.toByteArray();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
	
	private static void createHeader(Document document, String titleText, Sale sale, String plate, String taller) throws IOException {
		PdfFont font = null;
		try {
			font = PdfFontFactory.createFont(StandardFonts.TIMES_ROMAN);
		} catch (IOException e) {
			font = document.getPdfDocument().getDefaultFont();
			e.printStackTrace();
		} // o Times, Courier...
        document.setFont(font);
        
        InputStream is = PdfUtils.class.getResourceAsStream(LOGO_PATH);
        Image img = new Image(ImageDataFactory.create(is.readAllBytes()));
        img.setHeight(150).setWidth(225);
        
        Paragraph title = new Paragraph(titleText)
                .setBold()
                .setFontSize(16);
        Paragraph companyAddress = new Paragraph(ADDRESS+" - "+ PHONE+"\n"+LOCATION)
                .setFontSize(10)         
                .setFontColor(ColorConstants.DARK_GRAY)
                .setMarginTop(5);         

        Cell rightCell = new Cell()
                .add(title)
                .add(companyAddress)
                .setBorder(null)
                .setVerticalAlignment(VerticalAlignment.MIDDLE)
                .setHorizontalAlignment(HorizontalAlignment.RIGHT);

        Table table = new Table(2);
        table.addCell(new Cell().add(img).setBorder(null));
        table.addCell(rightCell);

        document.add(table);
		document.setMargins(40,55,55,40);
        document.add(new Paragraph("Fecha:                   " + sale.getDate()));
        document.add(new Paragraph("Cliente:                 " + sale.getCustomer().getFullName()));
        if(taller != null && !taller.isBlank())
        	document.add(new Paragraph("Taller:                   " + taller));
        if(plate != null && !plate.isBlank())
        	document.add(new Paragraph("Patente:                 " + plate));
        String siniestroText = (sale.getSaleNumber() == null || sale.getSaleNumber().isBlank())
        	        ? "Particular"
        	        : sale.getSaleNumber();
        document.add(new Paragraph("Siniestro:               "+ siniestroText));
        document.add(new Paragraph("\n"));
		
	}

	
}
