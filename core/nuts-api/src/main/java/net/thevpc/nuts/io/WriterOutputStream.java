package net.thevpc.nuts.io;

import net.thevpc.nuts.util.NAssert;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.nio.charset.Charset;

public class WriterOutputStream extends OutputStream {
    private final Writer writer;
    private final Charset charset;

    // Constructor accepting a Writer
    public WriterOutputStream(Writer writer, Charset charset) {
        NAssert.requireNonNull(writer, "writer");
        NAssert.requireNonNull(charset, "charset");
        this.writer = writer;
        this.charset = charset;
    }

    @Override
    public void write(int b) throws IOException {
        writer.write(b);
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        writer.write(new String(b, off, len, charset));
    }

    @Override
    public void close() throws IOException {
        writer.flush();
    }

    @Override
    public void flush() throws IOException {
        writer.flush();
    }
}

