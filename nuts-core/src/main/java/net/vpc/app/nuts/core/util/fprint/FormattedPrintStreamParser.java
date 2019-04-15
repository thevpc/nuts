package net.vpc.app.nuts.core.util.fprint;

import net.vpc.app.nuts.core.util.fprint.parser.TextNode;

public interface FormattedPrintStreamParser {
//    TextNode parse(String text);
    String escapeText(String text);

    void take(String str);

    TextNode consumeNode();

    void forceEnding();
}
