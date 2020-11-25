package net.thevpc.nuts.runtime.util.fprint;

import net.thevpc.nuts.runtime.util.fprint.parser.DefaultTextNodeParser;

import java.io.IOException;
import java.io.OutputStream;

public class FormatOutputStreamSupport {
    private TextNodeWriter nodeWriter;
    private TextNodeParser parser = new DefaultTextNodeParser();
    private boolean formatEnabled = true;
    private TextNodeVisitor textNodeVisitor=node -> {
        if (isFormatEnabled()) {
            nodeWriter.writeNode(node, new TextNodeWriterContext().setFiltered(false));
        } else {
            nodeWriter.writeNode(node,new TextNodeWriterContext().setFiltered(true));
        }
    };

    public FormatOutputStreamSupport() {

    }

    public FormatOutputStreamSupport(OutputStream rawOutput,FormattedPrintStreamRenderer renderer) {
        this.nodeWriter = new TextNodeWriterRenderer(rawOutput,renderer);
    }

    public TextNodeParser getParser() {
        return parser;
    }

    public FormatOutputStreamSupport setParser(TextNodeParser parser) {
        this.parser = parser == null ? new DefaultTextNodeParser() : parser;
        return this;
    }

    public boolean isFormatEnabled() {
        return formatEnabled;
    }

    public FormatOutputStreamSupport setFormatEnabled(boolean formatEnabled) {
        this.formatEnabled = formatEnabled;
        return this;
    }

    public void processByte(int oneByte) throws IOException {
        processBytes(new byte[]{(byte) oneByte}, 0, 1);
    }

    public void processBytes(byte[] buf, int off, int len) {
        if (!isFormatEnabled()) {
            nodeWriter.writeRaw(buf, off, len);
        } else {
            parser.parseIncremental(buf, off, len, textNodeVisitor);
        }
    }

    public void reset() {
        flush();
    }

    public void flush() {
        nodeWriter.flush();
        parser.parseRemaining(textNodeVisitor);
//        if(!some) {
//            flushLater();
//        }
        nodeWriter.flush();
    }

    public boolean isIncomplete() {
        return parser.isIncomplete();
    }

    @Override
    public String toString() {
        return "FormatOutputStreamSupport(" + parser.toString() + ";" + this.nodeWriter + ")";
    }

}
