package Utils;

import java.io.IOException;
import java.util.List;

public interface CsvExporter<T> {
    byte[] export(List<T> items) throws IOException;
}

