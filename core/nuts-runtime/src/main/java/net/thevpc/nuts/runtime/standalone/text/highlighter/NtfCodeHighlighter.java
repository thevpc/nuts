package net.thevpc.nuts.runtime.standalone.text.highlighter;

import net.thevpc.nuts.NCodeHighlighter;
import net.thevpc.nuts.NSession;
import net.thevpc.nuts.NCallableSupport;
import net.thevpc.nuts.text.NText;
import net.thevpc.nuts.text.NTexts;
import net.thevpc.nuts.spi.NSupportLevelContext;

public class NtfCodeHighlighter implements NCodeHighlighter {
    public NtfCodeHighlighter(NSession session) {
    }

    @Override
    public String getId() {
        return "ntf";
    }

    @Override
    public int getSupportLevel(NSupportLevelContext context) {
        String s = context.getConstraints();
        if(s==null){
            return NCallableSupport.DEFAULT_SUPPORT;
        }
        switch (s){
            case "ntf":
            case "nuts-text-format":
            case "text/x-nuts-text-format":
            case "text/x-ntf":
            case "text/nuts-text-format":
            case "text/ntf":
            case "application/x-nuts-text-format":
            case "application/x-ntf":
            case "application/nuts-text-format":
            case "application/ntf":
            {
                return NCallableSupport.DEFAULT_SUPPORT;
            }
        }
        return NCallableSupport.NO_SUPPORT;
    }

    @Override
    public NText stringToText(String text, NTexts txt, NSession session) {
        return txt.parse(text);
    }

    @Override
    public NText tokenToText(String text, String tokenType, NTexts txt, NSession session) {
        return txt.parse(text);
    }
}
