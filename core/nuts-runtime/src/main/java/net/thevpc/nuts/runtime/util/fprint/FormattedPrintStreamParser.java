package net.thevpc.nuts.runtime.util.fprint;

import net.thevpc.nuts.runtime.util.fprint.parser.TextNode;

public interface FormattedPrintStreamParser {
//    TextNode parse(String text);

    String escapeText(String text);

    String filterText(String text);

    void take(String str);

    TextNode consumeNode();

    boolean forceEnding();

    boolean isIncomplete();
}
