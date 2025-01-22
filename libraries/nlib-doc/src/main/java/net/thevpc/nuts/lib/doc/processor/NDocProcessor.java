package net.thevpc.nuts.lib.doc.processor;

import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.lib.doc.context.NDocContext;

import java.io.InputStream;
import java.io.OutputStream;

public interface NDocProcessor {

    void processStream(InputStream source, OutputStream target, NDocContext context);
    void processPath(NPath source, String mimeType, NDocContext context);
}
