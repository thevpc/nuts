package net.thevpc.nuts.lib.doc.executor;

import net.thevpc.nuts.lib.doc.context.NDocContext;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;

class IgnoreNDocExecutor implements NDocExecutor {
    @Override
    public Object eval(InputStream source, NDocContext context) {
        return null;
    }

    @Override
    public void processStream(InputStream source, OutputStream target, NDocContext context) {

    }

    @Override
    public void processPath(Path source, String mimeType, NDocContext context) {

    }
}
