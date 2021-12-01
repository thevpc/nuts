package net.thevpc.nuts.runtime.standalone.io.path.spi.htmlfs;

import net.thevpc.nuts.NutsSession;
import net.thevpc.nuts.NutsSupported;

import java.util.List;

public interface HtmlfsParser {
    NutsSupported<List<String>> parseHtmlTomcat(byte[] bytes, NutsSession session);
}
