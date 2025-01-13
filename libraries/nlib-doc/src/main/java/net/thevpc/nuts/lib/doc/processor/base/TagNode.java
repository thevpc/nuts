package net.thevpc.nuts.lib.doc.processor.base;

import java.io.IOException;

abstract class TagNode {
    public abstract void run(ProcessStreamContext ctx) throws IOException;
}
