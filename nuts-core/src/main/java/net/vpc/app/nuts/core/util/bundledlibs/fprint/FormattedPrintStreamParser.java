package net.vpc.app.nuts.core.util.bundledlibs.fprint;

import net.vpc.app.nuts.core.util.bundledlibs.fprint.parser.TextNode;

public interface FormattedPrintStreamParser {
//    TextNode parse(String text);
    String escapeText(String text);

    void take(String str);

    TextNode consumeNode();

    void forceEnding();
}
