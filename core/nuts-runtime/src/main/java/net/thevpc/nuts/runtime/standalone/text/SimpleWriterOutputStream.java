package net.thevpc.nuts.runtime.standalone.text;

import net.thevpc.nuts.NUnsupportedEnumException;
import net.thevpc.nuts.NWorkspace;
import net.thevpc.nuts.runtime.standalone.io.terminal.NTerminalModeOp;
import net.thevpc.nuts.spi.NSystemTerminalBase;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CoderResult;
import java.nio.charset.CodingErrorAction;

/**
 * fully inspired by org.apache.commons.io.output.WriterOutputStream
 */
public class SimpleWriterOutputStream extends OutputStream implements ExtendedFormatAware {
    private static final int DEFAULT_BUFFER_SIZE = 1024;
    private final Writer writer;
    private final CharsetDecoder decoder;
    private final boolean writeImmediately;

    private final ByteBuffer decoderIn = ByteBuffer.allocate(128);
    private final CharBuffer decoderOut;
    private final NWorkspace workspace;
    private final NSystemTerminalBase term;
    public SimpleWriterOutputStream(Writer writer, CharsetDecoder decoder, NSystemTerminalBase term, NWorkspace workspace) {
        this(writer, decoder, DEFAULT_BUFFER_SIZE, false,term, workspace);
    }

    public SimpleWriterOutputStream(Writer writer, CharsetDecoder decoder, int bufferSize, boolean writeImmediately, NSystemTerminalBase term, NWorkspace workspace) {
        this.workspace = workspace;
        this.writer = writer;
        this.decoder = decoder;
        this.term = term;
        this.writeImmediately = writeImmediately;
        decoderOut = CharBuffer.allocate(bufferSize);
    }

    public SimpleWriterOutputStream(Writer writer, Charset charset, int bufferSize, boolean writeImmediately, NSystemTerminalBase term, NWorkspace workspace) {
        this(writer,
                charset.newDecoder()
                        .onMalformedInput(CodingErrorAction.REPLACE)
                        .onUnmappableCharacter(CodingErrorAction.REPLACE)
                        .replaceWith("?"),
                bufferSize,
                writeImmediately,term,workspace);
    }

    public SimpleWriterOutputStream(Writer writer, Charset charset, NSystemTerminalBase term, NWorkspace workspace) {
        this(writer, charset, DEFAULT_BUFFER_SIZE, false,term,workspace);
    }

    public SimpleWriterOutputStream(Writer writer, String charsetName, int bufferSize, boolean writeImmediately, NSystemTerminalBase term, NWorkspace workspace) {
        this(writer, Charset.forName(charsetName), bufferSize, writeImmediately,term,workspace);
    }

    public SimpleWriterOutputStream(Writer writer, String charsetName, NSystemTerminalBase term, NWorkspace workspace) {
        this(writer, charsetName, DEFAULT_BUFFER_SIZE, false,term,workspace);
    }

    public SimpleWriterOutputStream(Writer writer, NSystemTerminalBase term, NWorkspace workspace) {
        this(writer, Charset.defaultCharset(), DEFAULT_BUFFER_SIZE, false,term,workspace);
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
        ((Buffer) decoderIn).flip();
        CoderResult coderResult;
        while (true) {
            coderResult = decoder.decode(decoderIn, decoderOut, endOfInput);
            if (coderResult.isOverflow()) {
                flushOutput();
            } else if (coderResult.isUnderflow()) {
                break;
            } else {
                throw new IOException("unexpected coder result");
            }
        }
        decoderIn.compact();
    }

    private void flushOutput() throws IOException {
        if (decoderOut.position() > 0) {
            writer.write(decoderOut.array(), 0, decoderOut.position());
            ((Buffer) decoderOut).rewind();
        }
    }

    @Override
    public NTerminalModeOp getModeOp() {
        if(writer instanceof ExtendedFormatAware){
            return ((ExtendedFormatAware) writer).getModeOp();
        }
        return NTerminalModeOp.NOP;
    }

    @Override
    public ExtendedFormatAware convert(NTerminalModeOp other) {
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
                return new FormatOutputStream(this,term);
            }
            case FILTER:{
                return new FilterFormatOutputStream(this,term);
            }
            case ESCAPE:{
                return new EscapeOutputStream(this,term);
            }
            case UNESCAPE:{
                return new EscapeOutputStream(this,term);
            }
        }
        throw new NUnsupportedEnumException(other);
    }
}
