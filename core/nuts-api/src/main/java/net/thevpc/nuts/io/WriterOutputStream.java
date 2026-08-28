package net.thevpc.nuts.io;

import net.thevpc.nuts.util.NAssert;
import net.thevpc.nuts.util.NJdkExtension;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.nio.charset.Charset;

/**
 * WriterOutputStream class.
 *
 * @author thevpc
 * @since 0.8.0
 */
@NJdkExtension
public class WriterOutputStream extends OutputStream {
    private final Writer writer;
    private final Charset charset;

    // Constructor accepting a Writer
    /**
     * Writer output stream.
     *
     * @param writer writer
     * @param charset charset
     * @return writer output stream result
     */
    public WriterOutputStream(Writer writer, Charset charset) {
        NAssert.requireNamedNonNull(writer, "writer");
        NAssert.requireNamedNonNull(charset, "charset");
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

