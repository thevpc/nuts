package net.thevpc.nuts.io;

import net.thevpc.nuts.text.NText;

public interface NTerminalFormatter {
    static NTerminalFormatter ofSystemHighlighter() {
        return NTerminalFormatterFromHighlighter.of("system");
    }

    static NTerminalFormatter ofHighlighter(String highlighter) {
        return NTerminalFormatterFromHighlighter.of(highlighter);
    }

    NText format(Context context);

    interface Context{
        String buffer();
        // wrapper context in case i need to add extra params
    }

}
