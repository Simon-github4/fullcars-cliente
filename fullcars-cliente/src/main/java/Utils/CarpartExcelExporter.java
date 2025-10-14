package Utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;

import model.client.entities.CarPart;

public class CarpartExcelExporter implements Exporter<CarPart> {

    @Override
    public byte[] export(List<CarPart> parts) throws IOException {
        try (Workbook workbook = new SXSSFWorkbook(); ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("CarParts");

            // Estilo de cabecera
            CellStyle headerStyle = workbook.createCellStyle();
            Font font = workbook.createFont();
            font.setBold(true);
            headerStyle.setFont(font);

            // Crear fila de cabecera
            Row header = sheet.createRow(0);
            String[] headers = {"SKU", "Nombre", "Descripcion", "Stock", "Marca", "Proveedor", "Categoria", "PrecioBase"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = header.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // Rellenar datos
            int rowIdx = 1;
            for (CarPart p : parts) {
                Row row = sheet.createRow(rowIdx++);

                row.createCell(0).setCellValue(safe(p.getSku()));
                row.createCell(1).setCellValue(safe(p.getName()));
                row.createCell(2).setCellValue(safe(p.getDescription()));
                row.createCell(3).setCellValue(p.getStock() != null ? p.getStock() : 0);
                row.createCell(4).setCellValue(p.getBrand().getName() != null ? p.getBrand().getName() : "");
                row.createCell(6).setCellValue(p.getProvider() != null ? p.getProvider().getCompanyName() : "");
                row.createCell(7).setCellValue(p.getCategory() != null ? p.getCategory().getName() : "");
                row.createCell(8).setCellValue(p.getBasePrice() != null ? p.getBasePrice().doubleValue() : 0);
                //row.getCell(8).setCellType(CellType.NUMERIC);
            }

            // Autoajustar columnas
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(baos);
            return baos.toByteArray();
        }
    }

    private static String safe(String s) {
        return s == null ? "" : s.trim();
    }
}
