package net.thevpc.nuts.lib.md.base;

import net.thevpc.nuts.lib.md.MdParser;
import net.thevpc.nuts.lib.md.MdProvider;
import net.thevpc.nuts.lib.md.MdWriter;

import java.io.Reader;
import java.io.Writer;

public class DefaultMdProvider implements MdProvider {
    @Override
    public String getMimeType() {
        return "text/markdown";
    }

    @Override
    public MdParser createParser(Reader reader) {
        return new BaseMdParser(reader);
    }

    @Override
    public MdWriter createWriter(Writer writer) {
        return new DefaultMdWriter(writer);
    }
}
