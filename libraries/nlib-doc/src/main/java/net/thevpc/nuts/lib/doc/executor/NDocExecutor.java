package net.thevpc.nuts.lib.doc.executor;

import net.thevpc.nuts.lib.doc.context.NDocContext;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;

public interface NDocExecutor {

    Object eval(InputStream source, NDocContext context);

    void processStream(InputStream source, OutputStream target, NDocContext context);

    void processPath(Path source, String mimeType, NDocContext context);
}
