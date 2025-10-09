package net.thevpc.nuts.runtime.standalone.io.path.spi.htmlfs;

import net.thevpc.nuts.concurrent.NScoredCallable;

import java.util.List;

public interface HtmlfsParser {
    NScoredCallable<List<String>> parseHtmlTomcat(byte[] bytes);
}
