package net.thevpc.nuts.runtime.standalone.text.highlighter;

import net.thevpc.nuts.NCodeHighlighter;
import net.thevpc.nuts.NSession;
import net.thevpc.nuts.text.NText;
import net.thevpc.nuts.text.NTexts;
import net.thevpc.nuts.spi.NComponent;
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
            return DEFAULT_SUPPORT;
        }
        switch (s){
            case "ntf":
            case "nuts-text-format":
            case "text/x-nuts-text-format":
            {
                return NComponent.DEFAULT_SUPPORT;
            }
        }
        return NComponent.NO_SUPPORT;
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
