package net.thevpc.nuts.runtime.util.fprint;

import net.thevpc.nuts.runtime.util.fprint.parser.TextNode;

public interface TextNodeWriter {
    void writeNode(TextNode node, TextNodeWriterContext ctx);

    void writeRaw(byte[] buf, int off, int len);

    boolean flush();

}
