package net.thevpc.nuts.runtime.standalone.text.highlighter;

import net.thevpc.nuts.reflect.NScore;
import net.thevpc.nuts.reflect.NScorable;
import net.thevpc.nuts.reflect.NScorableContext;

/**
 * Syntax highlighter for ntexup {@code .ntx} documents.
 * <p>
 * ntexup documents are TSON documents whose nodes are page/slide elements.
 * The tokenizer and the generic TSON styling are inherited from
 * {@link NTexupCodeHighlighter}; only the identifier/extension aliases differ.
 */
public class NtxCodeHighlighter extends NTexupCodeHighlighter {

    @Override
    public String id() {
        return "ntx";
    }

    @NScore
    public static int getScore(NScorableContext context) {
        String s = context.criteria();
        if (s == null) {
            return NScorable.DEFAULT_SCORE;
        }
        switch (s) {
            case "ntx":
            case "text/ntx":
            case "application/ntx": {
                return NScorable.DEFAULT_SCORE;
            }
        }
        return NScorable.UNSUPPORTED_SCORE;
    }
}
