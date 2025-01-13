package net.thevpc.nuts.lib.doc.processor.impl;

import net.thevpc.nuts.lib.doc.context.NDocContext;
import net.thevpc.nuts.lib.doc.processor.NDocStreamProcessor;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;

public class CopyStreamProcessor implements NDocStreamProcessor {

    @Override
    public void processStream(InputStream source, OutputStream target, NDocContext context) {
        try {
            byte[] buffer = new byte[1024];
            int r;
            while ((r = source.read(buffer)) > 0) {
                target.write(buffer, 0, r);
            }
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    @Override
    public String toString() {
        return "Copy";
    }
    

}
