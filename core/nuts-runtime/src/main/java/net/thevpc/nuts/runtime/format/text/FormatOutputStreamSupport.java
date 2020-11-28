package net.thevpc.nuts.runtime.format.text;

import net.thevpc.nuts.NutsTextNodeParser;
import net.thevpc.nuts.NutsTextNodeVisitor;
import net.thevpc.nuts.NutsTextNodeWriteConfiguration;
import net.thevpc.nuts.NutsWorkspace;
import net.thevpc.nuts.runtime.format.text.parser.DefaultNutsTextNodeParser;

import java.io.IOException;
import java.io.OutputStream;

public class FormatOutputStreamSupport {
    private NutsTextNodeWriter nodeWriter;
    private NutsTextNodeParser parser;
    private boolean formatEnabled = true;
    private NutsWorkspace ws;
    private NutsTextNodeWriteConfiguration writeConfiguration=new NutsTextNodeWriteConfiguration();
    private NutsTextNodeVisitor nutsTextNodeVisitor = node -> {
        nodeWriter.writeNode(node);
    };

    public FormatOutputStreamSupport() {

    }

    public FormatOutputStreamSupport(OutputStream rawOutput, FormattedPrintStreamRenderer renderer, NutsWorkspace ws) {
        this.ws=ws;
        this.parser = new DefaultNutsTextNodeParser(ws);
        this.nodeWriter = new NutsTextNodeWriterRenderer(rawOutput,renderer,ws)
        .setWriteConfiguration(writeConfiguration.setFiltered(false));
    }

    public NutsTextNodeParser getParser() {
        return parser;
    }

    public FormatOutputStreamSupport setParser(NutsTextNodeParser parser) {
        this.parser = parser == null ? new DefaultNutsTextNodeParser(ws) : parser;
        return this;
    }

    public boolean isFormatEnabled() {
        return formatEnabled;
    }

    public FormatOutputStreamSupport setFormatEnabled(boolean formatEnabled) {
        this.formatEnabled = formatEnabled;
        writeConfiguration.setFiltered(!formatEnabled);
        return this;
    }

    public void processByte(int oneByte) throws IOException {
        processBytes(new byte[]{(byte) oneByte}, 0, 1);
    }

    public void processBytes(byte[] buf, int off, int len) {
        if (!isFormatEnabled()) {
            nodeWriter.writeRaw(buf, off, len);
        } else {
            parser.parseIncremental(buf, off, len, nutsTextNodeVisitor);
        }
    }

    public void reset() {
        flush();
    }

    public void flush() {
        nodeWriter.flush();
        parser.parseRemaining(nutsTextNodeVisitor);
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
