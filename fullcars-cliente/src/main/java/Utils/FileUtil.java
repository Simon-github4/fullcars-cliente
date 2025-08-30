package Utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class FileUtil {
	
    public static void saveBytes(File file, byte[] data) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(data);
        } catch (IOException e) { //TAmbien estaba FileNotFoundExcepiton
			e.printStackTrace();
			throw new IOException("No se pudo guardar el Archivo");
		}
    }
}

