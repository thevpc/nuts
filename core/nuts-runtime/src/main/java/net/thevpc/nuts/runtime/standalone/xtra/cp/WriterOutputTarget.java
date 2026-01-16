package net.thevpc.nuts.runtime.standalone.xtra.cp;

import net.thevpc.nuts.core.NWorkspace;
import net.thevpc.nuts.io.DefaultNContentMetadata;
import net.thevpc.nuts.runtime.standalone.io.printstream.OutputTargetExt;
import net.thevpc.nuts.io.WriterOutputStream;

import java.io.Writer;
import java.nio.charset.StandardCharsets;

class WriterOutputTarget extends OutputTargetExt {
    Writer writer;

    public WriterOutputTarget(Writer writer) {
        super(new WriterOutputStream(writer, StandardCharsets.UTF_8), new DefaultNContentMetadata());
        this.writer = writer;
    }

    public Writer getWriter() {
        return writer;
    }
}
