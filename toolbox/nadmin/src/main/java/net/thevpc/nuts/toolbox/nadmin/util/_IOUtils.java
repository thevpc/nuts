package net.thevpc.nuts.toolbox.nadmin.util;


import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public class _IOUtils {
    public static PipeThread pipe(String name, final NonBlockingInputStream in, final OutputStream out) {
        PipeThread p = new PipeThread(name, in, out);
        p.start();
        return p;
    }

    public static byte[] loadFileContentLenient(Path out) {
        if (Files.isRegularFile(out)) {
            try {
                return Files.readAllBytes(out);
            } catch (Exception ex) {
                //ignore
            }
        }
        return new byte[0];
    }
}
