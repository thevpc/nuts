package net.thevpc.nuts.runtime.core.format.text;

import net.thevpc.nuts.NutsTextWriteConfiguration;
import net.thevpc.nuts.NutsWorkspace;
import net.thevpc.nuts.runtime.core.format.text.parser.DefaultNutsTextNodeParser;

import java.io.IOException;
import java.io.OutputStream;
import net.thevpc.nuts.NutsSession;
import net.thevpc.nuts.NutsTextParser;
import net.thevpc.nuts.NutsTextVisitor;

public class FormatOutputStreamSupport {
    private NutsTextNodeWriter nodeWriter;
    private NutsTextParser parser;
    private boolean formatEnabled = true;
    private NutsSession session;
    private NutsWorkspace ws;
    private NutsTextWriteConfiguration writeConfiguration=new NutsTextWriteConfiguration();
    private NutsTextVisitor nutsTextNodeVisitor = node -> {
        nodeWriter.writeNode(node);
    };

    public FormatOutputStreamSupport() {

    }

    public FormatOutputStreamSupport(OutputStream rawOutput, FormattedPrintStreamRenderer renderer, NutsSession session) {
        this.session=session;
        this.ws=session.getWorkspace();
        this.parser = new DefaultNutsTextNodeParser(session);
        this.nodeWriter = new NutsTextNodeWriterRenderer(rawOutput,renderer,session)
        .setWriteConfiguration(writeConfiguration.setFiltered(false));
    }

    public NutsTextParser getParser() {
        return parser;
    }

    public FormatOutputStreamSupport setParser(NutsTextParser parser) {
        this.parser = parser == null ? new DefaultNutsTextNodeParser(session) : parser;
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
