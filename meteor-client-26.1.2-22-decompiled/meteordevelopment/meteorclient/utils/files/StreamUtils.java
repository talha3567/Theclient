package meteordevelopment.meteorclient.utils.files;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import meteordevelopment.meteorclient.MeteorClient;
import org.apache.commons.io.IOUtils;

public class StreamUtils {
    private StreamUtils() {
    }

    public static void copy(File from, File to) {
        try (FileInputStream in = new FileInputStream(from);
             FileOutputStream out = new FileOutputStream(to);){
            ((InputStream)in).transferTo(out);
        }
        catch (IOException e) {
            MeteorClient.LOG.error("Error copying from file '{}' to file '{}'.", from.getName(), to.getName(), e);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void copy(InputStream in, File to) {
        try (FileOutputStream out = new FileOutputStream(to);){
            in.transferTo(out);
        }
        catch (IOException iOException) {
            MeteorClient.LOG.error("Error writing to file '{}'.", (Object)to.getName());
        }
        finally {
            IOUtils.closeQuietly((InputStream)in);
        }
    }
}
