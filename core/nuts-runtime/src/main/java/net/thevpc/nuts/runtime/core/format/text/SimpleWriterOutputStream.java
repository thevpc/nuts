package net.thevpc.nuts.runtime.core.format.text;

import net.thevpc.nuts.NutsSession;
import net.thevpc.nuts.NutsWorkspace;
import net.thevpc.nuts.runtime.core.terminals.NutsTerminalModeOp;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CoderResult;
import java.nio.charset.CodingErrorAction;

/**
 * fully inspired by org.apache.commons.io.output.WriterOutputStream
 */
public class SimpleWriterOutputStream extends OutputStream implements ExtendedFormatAware{
    private static final int DEFAULT_BUFFER_SIZE = 1024;
    private final Writer writer;
    private final CharsetDecoder decoder;
    private final boolean writeImmediately;

    private final ByteBuffer decoderIn = ByteBuffer.allocate(128);
    private final CharBuffer decoderOut;
    private final NutsWorkspace ws;
    private final NutsSession session;
    public SimpleWriterOutputStream(Writer writer, CharsetDecoder decoder,NutsSession session) {
        this(writer, decoder, DEFAULT_BUFFER_SIZE, false,session);
    }

    public SimpleWriterOutputStream(Writer writer, CharsetDecoder decoder, int bufferSize, boolean writeImmediately,NutsSession session) {
        this.session = session;
        this.ws = session.getWorkspace();
        this.writer = writer;
        this.decoder = decoder;
        this.writeImmediately = writeImmediately;
        decoderOut = CharBuffer.allocate(bufferSize);
    }

    public SimpleWriterOutputStream(Writer writer, Charset charset, int bufferSize, boolean writeImmediately,NutsSession session) {
        this(writer,
                charset.newDecoder()
                        .onMalformedInput(CodingErrorAction.REPLACE)
                        .onUnmappableCharacter(CodingErrorAction.REPLACE)
                        .replaceWith("?"),
                bufferSize,
                writeImmediately,session);
    }

    public SimpleWriterOutputStream(Writer writer, Charset charset,NutsSession session) {
        this(writer, charset, DEFAULT_BUFFER_SIZE, false,session);
    }

    public SimpleWriterOutputStream(Writer writer, String charsetName, int bufferSize, boolean writeImmediately,NutsSession session) {
        this(writer, Charset.forName(charsetName), bufferSize, writeImmediately,session);
    }

    public SimpleWriterOutputStream(Writer writer, String charsetName,NutsSession session) {
        this(writer, charsetName, DEFAULT_BUFFER_SIZE, false,session);
    }

    public SimpleWriterOutputStream(Writer writer,NutsSession session) {
        this(writer, Charset.defaultCharset(), DEFAULT_BUFFER_SIZE, false,session);
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        while (len > 0) {
            int c = Math.min(len, decoderIn.remaining());
            decoderIn.put(b, off, c);
            processInput(false);
            len -= c;
            off += c;
        }
        if (writeImmediately) {
            flushOutput();
        }
    }

    @Override
    public void write(byte[] b) throws IOException {
        write(b, 0, b.length);
    }

    @Override
    public void write(int b) throws IOException {
        write(new byte[] { (byte)b }, 0, 1);
    }

    @Override
    public void flush() throws IOException {
        flushOutput();
        writer.flush();
    }

    @Override
    public void close() throws IOException {
        processInput(true);
        flushOutput();
        writer.close();
    }

    private void processInput(boolean endOfInput) throws IOException {
        decoderIn.flip();
        CoderResult coderResult;
        while (true) {
            coderResult = decoder.decode(decoderIn, decoderOut, endOfInput);
            if (coderResult.isOverflow()) {
                flushOutput();
            } else if (coderResult.isUnderflow()) {
                break;
            } else {
                throw new IOException("Unexpected coder result");
            }
        }
        decoderIn.compact();
    }

    private void flushOutput() throws IOException {
        if (decoderOut.position() > 0) {
            writer.write(decoderOut.array(), 0, decoderOut.position());
            decoderOut.rewind();
        }
    }

    @Override
    public NutsTerminalModeOp getModeOp() {
        if(writer instanceof ExtendedFormatAware){
            return ((ExtendedFormatAware) writer).getModeOp();
        }
        return NutsTerminalModeOp.NOP;
    }

    @Override
    public ExtendedFormatAware convert(NutsTerminalModeOp other) {
        if(other==null || other==getModeOp()){
            return this;
        }
        if(writer instanceof ExtendedFormatAwarePrintWriter){
            return ((ExtendedFormatAwarePrintWriter) writer).convert(other);
        }
        switch (other){
            case NOP:{
                return this;
            }
            case FORMAT:{
                return new FormatOutputStream(this,ws);
            }
            case FILTER:{
                return new FilterFormatOutputStream(this,ws);
            }
            case ESCAPE:{
                return new EscapeOutputStream(this,ws);
            }
            case UNESCAPE:{
                return new EscapeOutputStream(this,ws);
            }
        }
        throw new IllegalArgumentException("Unsupported");
    }
}
