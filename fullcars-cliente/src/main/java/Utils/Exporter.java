package Utils;

import java.io.IOException;
import java.util.List;

public interface Exporter<T> {
    byte[] export(List<T> items) throws IOException;
}

