package net.thevpc.nuts.io;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;

public class WriterOutputStream extends OutputStream {
    private final Writer writer;

    // Constructor accepting a Writer
    public WriterOutputStream(Writer writer) {
        if (writer == null) {
            throw new NullPointerException("Writer cannot be null");
        }
        this.writer = writer;
    }

    @Override
    public void write(int b) throws IOException {
        writer.write(b);
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        writer.write(new String(b, off, len));
    }

    @Override
    public void close() throws IOException {
        writer.close();
    }

    @Override
    public void flush() throws IOException {
        writer.flush();
    }
}

