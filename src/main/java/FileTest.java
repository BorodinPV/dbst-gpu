import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

/**
 * Created by Pavel Borodin on 07.09.2020
 */
public class FileTest {
    String nameFile;

    public FileTest(String file) {
        this.nameFile = file;
    }

    public File getFile() {
        ClassLoader classLoader = getClass().getClassLoader();
        return new File(classLoader.getResource(nameFile).getFile());
    }

    public byte[] getFileByte()  {
        try {
            return Files.readAllBytes(getFile().toPath());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    byte[] encodeUTF8(String string) {
        return string.getBytes(StandardCharsets.US_ASCII);
    }

    String decodeUTF8(byte[] bytes) {
        return new String(bytes, StandardCharsets.US_ASCII);
    }
}
