package net.thevpc.nuts.io;

import net.thevpc.nuts.text.NText;
import net.thevpc.nuts.util.NBlankable;
import net.thevpc.nuts.util.NStringUtils;

/**
 * NTerminalFormatterFromHighlighter class.
 *
 * @author thevpc
 * @since 0.8.0
 */
public class NTerminalFormatterFromHighlighter implements NTerminalFormatter {
    private static final NTerminalFormatter SYSTEM_HIGHLIGHTER = new NTerminalFormatterFromHighlighter("system");
    private final String highlighter;

    /**
     * N terminal formatter from highlighter.
     *
     * @param highlighter highlighter
     * @return n terminal formatter from highlighter result
     */
    protected NTerminalFormatterFromHighlighter(String highlighter) {
        this.highlighter = highlighter;
    }

    /**
     * Creates a new instance of of.
     *
     * @param highlighter highlighter
     * @return of result
     */
    public static NTerminalFormatter of(String highlighter) {
        String ct = highlighter;
        if (NBlankable.isBlank(ct)) {
            ct = "system";
        }
        switch (NStringUtils.strip(ct).toLowerCase()) {
            case "system":
                return SYSTEM_HIGHLIGHTER;
        }
        return new NTerminalFormatterFromHighlighter(ct);
    }

    @Override
    public NText format(Context context) {
        String ct = highlighter;
        if (NBlankable.isBlank(ct)) {
            ct = "system";
        }
        try {
            return NText.ofCode(ct, context.buffer()).highlight();
        }catch (Exception ex){
            return NText.ofPlain(context.buffer());
        }
    }
}
