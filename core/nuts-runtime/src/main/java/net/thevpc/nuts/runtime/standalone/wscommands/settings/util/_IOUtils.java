package net.thevpc.nuts.runtime.standalone.wscommands.settings.util;


import net.thevpc.nuts.NutsSession;
import net.thevpc.nuts.runtime.bundles.io.NonBlockingInputStream;
import net.thevpc.nuts.runtime.bundles.io.PipeThread;

import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public class _IOUtils {
    public static PipeThread pipe(String name, final NonBlockingInputStream in, final OutputStream out, NutsSession session) {
        PipeThread p = new PipeThread(name, in, out,session);
        p.start();
        return p;
    }

    public static String loadFileContentLenientString(Path out) {
        return new String(loadFileContentLenient(out));
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
