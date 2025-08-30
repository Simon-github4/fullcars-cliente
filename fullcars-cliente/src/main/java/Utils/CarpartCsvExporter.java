package Utils;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;

import model.client.entities.CarPart;

public class CarpartCsvExporter implements CsvExporter<CarPart> {

    @Override
    public byte[] export(List<CarPart> parts) throws IOException {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             OutputStreamWriter osw = new OutputStreamWriter(baos, StandardCharsets.UTF_8);
             BufferedWriter writer = new BufferedWriter(osw)) {

            writer.write("SKU,Nombre,Descripción,Stock,Marca,Proveedor,Categoría,PrecioBase");
            writer.newLine();

            for (CarPart p : parts) {
                writer.write(String.format("%s,%s,%s,%d,%s,%s,%s,%d",
                		safe(p.getSku()),
                        safe(p.getName()),
                        safe(p.getDescription()),
                        p.getStock() != null ? p.getStock() : 0,
                        safe(p.getBrand() != null ? p.getBrand().getName() : ""),
                        safe(p.getProvider() != null ? p.getProvider().getCompanyName() : ""),
                        safe(p.getCategory() != null ? p.getCategory().getName() : ""),
                        p.getBasePrice() != null ? p.getBasePrice() : 0
                ));
                writer.newLine();
            }

            writer.flush();
            return baos.toByteArray();
        }
    }

    private static String safe(String s) {
        if (s == null) return "";

        String cleaned = s.replace("\"", "\"\"");

        // Forzar texto en Excel si parece fecha o tiene guiones
        if (cleaned.matches(".*[-/].*") || cleaned.matches("\\d{4,}")) 
            return "'" + cleaned; // apóstrofe al inicio

        // Si contiene espacios o comas, envolver en comillas
        if (cleaned.contains(" ") || cleaned.contains(",")) 
            return "\"" + cleaned + "\"";

        return cleaned;
    }

}

