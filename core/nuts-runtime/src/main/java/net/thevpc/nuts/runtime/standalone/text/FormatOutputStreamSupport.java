package net.thevpc.nuts.runtime.standalone.text;

import net.thevpc.nuts.*;
import net.thevpc.nuts.io.NPrintStream;
import net.thevpc.nuts.runtime.standalone.io.outputstream.OutputHelper;
import net.thevpc.nuts.runtime.standalone.text.parser.AbstractNTextNodeParserDefaults;
import net.thevpc.nuts.spi.NSystemTerminalBase;
import net.thevpc.nuts.text.*;

public class FormatOutputStreamSupport {
    private NTextNodeWriter nodeWriter;
    private NTextParser parser;
    private boolean formatEnabled = true;
    private NSession session;
    private NWorkspace ws;
    private NTextTransformConfig writeConfiguration = new NTextTransformConfig();
    private NTextVisitor nutsTextNodeVisitor = node -> {
        nodeWriter.writeNode(node);
    };

    public FormatOutputStreamSupport() {
    }

    public FormatOutputStreamSupport(NPrintStream rawOutput, NSession session, NSystemTerminalBase term, boolean filtered) {
        this.session = session;
        this.ws = session.getWorkspace();
        this.parser = AbstractNTextNodeParserDefaults.createDefault(session);
        this.nodeWriter = new NTextNodeWriterRenderer(rawOutput, session, term)
                .setWriteConfiguration(writeConfiguration.setFiltered(false));
        this.writeConfiguration.setFiltered(filtered);
    }

    public NTextParser getParser() {
        return parser;
    }

    public FormatOutputStreamSupport setParser(NTextParser parser) {
        this.parser = parser == null ? NTexts.of(session).parser() : parser;
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

    public void processByte(int oneByte) {
        processBytes(new byte[]{(byte) oneByte}, 0, 1);
    }

    public void writeRaw(byte[] buf, int off, int len) {
        nodeWriter.writeRaw(buf, off, len);
    }

    public void processBytes(byte[] buf, int off, int len) {
        if (!isFormatEnabled()) {
            nodeWriter.writeRaw(buf, off, len);
        } else {
            parser.parseIncremental(buf, off, len, new NTextVisitor() {
                @Override
                public void visit(NText node) {
//                    JOptionPane.showMessageDialog(null,node.getType()+":"+node);
                    nutsTextNodeVisitor.visit(node);
                }
            });
        }
    }

    public void pushNode(NText node) {
        flush();
        nutsTextNodeVisitor.visit(node);
    }

    public void processChars(char[] buf, int off, int len) {
        if (!isFormatEnabled()) {
            nodeWriter.writeRaw(buf, off, len);
        } else {
            parser.parseIncremental(buf, off, len, new NTextVisitor() {
                @Override
                public void visit(NText node) {
//                    JOptionPane.showMessageDialog(null,node.getType()+":"+node);
                    nutsTextNodeVisitor.visit(node);
                }
            });
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
