package net.thevpc.nuts.runtime.standalone.io.path.spi.htmlfs;

import net.thevpc.nuts.concurrent.NCallableSupport;

import java.util.List;

public interface HtmlfsParser {
    NCallableSupport<List<String>> parseHtmlTomcat(byte[] bytes);
}
