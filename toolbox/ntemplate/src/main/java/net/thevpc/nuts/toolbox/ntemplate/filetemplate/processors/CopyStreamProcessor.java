package net.thevpc.nuts.toolbox.ntemplate.filetemplate.processors;

import net.thevpc.nuts.toolbox.ntemplate.filetemplate.FileTemplater;
import net.thevpc.nuts.toolbox.ntemplate.filetemplate.StreamProcessor;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;

public class CopyStreamProcessor implements StreamProcessor {

    @Override
    public void processStream(InputStream source, OutputStream target, FileTemplater context) {
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
