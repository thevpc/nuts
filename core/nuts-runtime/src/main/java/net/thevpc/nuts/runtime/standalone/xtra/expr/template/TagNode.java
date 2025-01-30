package net.thevpc.nuts.runtime.standalone.xtra.expr.template;

import java.io.IOException;

abstract class TagNode {
    public abstract void run(ProcessStreamContext ctx) throws IOException;
}
