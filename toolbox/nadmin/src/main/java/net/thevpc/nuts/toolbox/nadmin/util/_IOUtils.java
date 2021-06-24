package net.thevpc.nuts.toolbox.nadmin.util;


import java.io.OutputStream;

public class _IOUtils {
    public static PipeThread pipe(String name, final NonBlockingInputStream in, final OutputStream out) {
        PipeThread p = new PipeThread(name, in, out);
        p.start();
        return p;
    }
}
