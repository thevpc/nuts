package net.thevpc.nuts.lib.md.docusaurus;


import net.thevpc.nuts.lib.md.MdParser;
import net.thevpc.nuts.lib.md.MdProvider;
import net.thevpc.nuts.lib.md.MdWriter;

import java.io.Reader;
import java.io.Writer;

public class DocusaurusMdProvider implements MdProvider {
    @Override
    public String getMimeType() {
        return "text/markdown-docusaurus";
    }

    @Override
    public MdParser createParser(Reader reader) {
        return new DocusaurusMdParser(reader);
    }

    @Override
    public MdWriter createWriter(Writer writer) {
        return new DocusaurusWriter(writer);
    }
}
