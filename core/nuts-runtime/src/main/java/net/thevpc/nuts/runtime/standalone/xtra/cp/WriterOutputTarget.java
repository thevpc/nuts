package net.thevpc.nuts.runtime.standalone.xtra.cp;

import net.thevpc.nuts.NWorkspace;
import net.thevpc.nuts.io.DefaultNContentMetadata;
import net.thevpc.nuts.runtime.standalone.io.printstream.OutputTargetExt;
import net.thevpc.nuts.util.WriterOutputStream;

import java.io.OutputStream;
import java.io.Writer;

class WriterOutputTarget extends OutputTargetExt {
    Writer writer;

    public WriterOutputTarget(NWorkspace workspace,Writer writer) {
        super(new WriterOutputStream(writer), new DefaultNContentMetadata(), workspace);
        this.writer = writer;
    }

    public Writer getWriter() {
        return writer;
    }
}
