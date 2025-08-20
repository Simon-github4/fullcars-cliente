package Utils;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.HorizontalAlignment;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.itextpdf.layout.properties.VerticalAlignment;

import model.client.entities.Sale;
import model.client.entities.SaleDetail;

public class PdfUtils {

	//public static final String PDF_DESKTOP_PATH = System.getProperty("user.home") + File.separator + "Desktop" + File.separator + "A";
	public static final String LOGO_PATH = Icons.LOGO.getPath();
	public static final String ADDRESS = "Misiones Nº1869";
	public static final String PHONE = "[02291] 15519359";
	public static final String LOCATION = "Mar del Plata - Bs. As. Argentina";
	
	public static byte[] generateRemitoPdf(Sale sale) {
        try {
        	ByteArrayOutputStream baos = new ByteArrayOutputStream();//in memory file
        	PdfWriter writer = new PdfWriter(baos);
        	
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);
            PdfFont font = PdfFontFactory.createFont(StandardFonts.TIMES_ROMAN); // o Times, Courier...
            document.setFont(font);

            InputStream is = PdfUtils.class.getResourceAsStream(LOGO_PATH);
            Image img = new Image(ImageDataFactory.create(is.readAllBytes()));
            img.setHeight(150).setWidth(225);
            
            Paragraph title = new Paragraph("REMITO ORIGINAL")
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
            document.add(new Paragraph("Fecha:\t\t\t\t" + sale.getDate()));
            document.add(new Paragraph("Cliente:\t\t\t\t" + sale.getCustomer().getFullName()));
            document.add(new Paragraph("Domicilio:\t\t\t" + sale.getCustomer().getAdress()));
            document.add(new Paragraph("Marca/Modelo:\t" + sale.getDetails().get(0).getProduct().getBrand()));
            document.add(new Paragraph("Siniestro:\t\t\t"+ sale.getSaleNumber()));
            document.add(new Paragraph("\n"));

            float[] columnWidths = {50, 280, 90, 50};
            Table detailstable = new Table(columnWidths);
            detailstable.addHeaderCell(new Cell().add(new Paragraph("Cantidad").setBold()));
            detailstable.addHeaderCell(new Cell().add(new Paragraph("Detalle").setBold()));
            detailstable.addHeaderCell(new Cell().add(new Paragraph("Codigo").setBold()));
            detailstable.addHeaderCell(new Cell().add(new Paragraph("")));

            for (SaleDetail detail : sale.getDetails()) {
            	detailstable.addCell(String.valueOf(detail.getQuantity()));
            	detailstable.addCell(detail.getProduct().getName());
            	detailstable.addCell(detail.getProduct().getSku());
            	detailstable.addCell("LEG");
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
	
	public static byte[] generatePresupuestoPdf(Sale sale) {
        try {
        	ByteArrayOutputStream baos = new ByteArrayOutputStream();//in memory file
        	PdfWriter writer = new PdfWriter(baos);
        	
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);
            PdfFont font = PdfFontFactory.createFont(StandardFonts.TIMES_ROMAN); // o Times, Courier...
            document.setFont(font);
            
            InputStream is = PdfUtils.class.getResourceAsStream(LOGO_PATH);
            Image img = new Image(ImageDataFactory.create(is.readAllBytes()));
            img.setHeight(150).setWidth(225);
            
            Paragraph title = new Paragraph("PRESUPUESTO ORIGINAL")
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
            document.add(new Paragraph("Fecha:\t\t\t\t" + sale.getDate()));
            document.add(new Paragraph("Cliente:\t\t\t\t" + sale.getCustomer().getFullName()));
            document.add(new Paragraph("Domicilio:\t\t\t" + sale.getCustomer().getAdress()));
            document.add(new Paragraph("Marca/Modelo:\t" + sale.getDetails().get(0).getProduct().getBrand()));
            document.add(new Paragraph("Siniestro:\t\t\t Particular"));
            document.add(new Paragraph("\n"));

            float[] columnWidths = {50, 280, 70, 100};
            Table detailstable = new Table(columnWidths);
            detailstable.addHeaderCell(new Cell().add(new Paragraph("Cantidad").setBold()));
            detailstable.addHeaderCell(new Cell().add(new Paragraph("Detalle").setBold()));
            detailstable.addHeaderCell(new Cell().add(new Paragraph("Codigo").setBold()));
            detailstable.addHeaderCell(new Cell().add(new Paragraph("Subtotal").setBold()));

            for (SaleDetail detail : sale.getDetails()) {
            	detailstable.addCell(String.valueOf(detail.getQuantity()));
            	detailstable.addCell(detail.getProduct().getName());
            	detailstable.addCell(detail.getProduct().getSku());
            	detailstable.addCell(detail.getSubTotal().toString());
            }

            document.add(detailstable);

            Long total = sale.getTotal();
            		/*sale.getDetails().stream()
                    .map(d -> d.getPrice().multiply(BigDecimal.valueOf(d.getQuantity())))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);*/

            document.add(new Paragraph("\nTotal: $" + total).setBold());

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
	
}
