package net.thevpc.nuts.io;

import net.thevpc.nuts.text.NText;

/**
 * NTerminalFormatter interface.
 *
 * @author thevpc
 * @since 0.8.0
 */
public interface NTerminalFormatter {
    /**
     * Creates a new instance of of system highlighter.
     *
     * @return of system highlighter result
     */
    static NTerminalFormatter ofSystemHighlighter() {
        return NTerminalFormatterFromHighlighter.of("system");
    }

    /**
     * Creates a new instance of of highlighter.
     *
     * @param highlighter highlighter
     * @return of highlighter result
     */
    static NTerminalFormatter ofHighlighter(String highlighter) {
        return NTerminalFormatterFromHighlighter.of(highlighter);
    }

    /**
     * Format.
     *
     * @param context context
     * @return format result
     */
    NText format(Context context);

    interface Context{
        /**
         * Buffer.
         *
         * @return buffer result
         */
        String buffer();
        // wrapper context in case i need to add extra params
    }

}
