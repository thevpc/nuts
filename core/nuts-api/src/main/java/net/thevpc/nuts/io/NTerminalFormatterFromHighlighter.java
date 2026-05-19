package net.thevpc.nuts.io;

import net.thevpc.nuts.text.NText;
import net.thevpc.nuts.util.NBlankable;

public class NTerminalFormatterFromHighlighter implements NTerminalFormatter {
    private static final NTerminalFormatter SYSTEM_HIGHLIGHTER = new NTerminalFormatterFromHighlighter("system");
    private final String highlighter;

    protected NTerminalFormatterFromHighlighter(String highlighter) {
        this.highlighter = highlighter;
    }

    public static NTerminalFormatter of(String highlighter) {
        String ct = highlighter;
        if (NBlankable.isBlank(ct)) {
            ct = "system";
        }
        switch (ct.trim().toLowerCase()) {
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
