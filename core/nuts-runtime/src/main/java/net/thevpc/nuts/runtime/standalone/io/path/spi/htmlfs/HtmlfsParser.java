package net.thevpc.nuts.runtime.standalone.io.path.spi.htmlfs;

import net.thevpc.nuts.NSession;
import net.thevpc.nuts.NSupported;

import java.util.List;

public interface HtmlfsParser {
    NSupported<List<String>> parseHtmlTomcat(byte[] bytes, NSession session);
}
