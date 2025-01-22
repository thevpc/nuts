package net.thevpc.nuts.lib.doc.executor;

import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.lib.doc.context.NDocContext;

import java.io.InputStream;
import java.io.OutputStream;

class IgnoreNDocExecutor implements NDocExecutor {
    @Override
    public Object eval(InputStream source, NDocContext context) {
        return null;
    }

    @Override
    public void processStream(InputStream source, OutputStream target, NDocContext context) {

    }

    @Override
    public void processPath(NPath source, String mimeType, NDocContext context) {

    }
}
