package net.thevpc.nuts.runtime.standalone.io.path.spi.htmlfs;

import net.thevpc.nuts.concurrent.NScorableCallable;

import java.util.List;

public interface HtmlfsParser {
    NScorableCallable<List<String>> parseHtmlTomcat(byte[] bytes);
}
