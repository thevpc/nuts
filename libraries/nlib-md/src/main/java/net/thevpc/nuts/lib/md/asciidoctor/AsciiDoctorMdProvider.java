package net.thevpc.nuts.lib.md.asciidoctor;


import net.thevpc.nuts.lib.md.MdParser;
import net.thevpc.nuts.lib.md.MdProvider;
import net.thevpc.nuts.lib.md.MdWriter;

import java.io.Reader;
import java.io.Writer;

public class AsciiDoctorMdProvider implements MdProvider {
    @Override
    public String getMimeType() {
        return "text/markdown-asciidoctor";
    }

    @Override
    public MdParser createParser(Reader reader) {
        return new AsciiDoctorMdParser(reader);
    }

    @Override
    public MdWriter createWriter(Writer writer) {
        return new AsciiDoctorWriter(writer);
    }
}
