package net.thevpc.nuts.runtime.standalone.io.path.spi.htmlfs;

import net.thevpc.nuts.concurrent.NScoredCallable;
import net.thevpc.nuts.io.NPathInfo;

import java.util.List;

public interface HtmlfsParser {
    NScoredCallable<List<String>> parseHtmlTomcat(byte[] bytes);
}
