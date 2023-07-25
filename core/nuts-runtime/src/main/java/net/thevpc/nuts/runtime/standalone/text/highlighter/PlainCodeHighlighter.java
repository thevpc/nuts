package net.thevpc.nuts.runtime.standalone.text.highlighter;

import net.thevpc.nuts.*;
import net.thevpc.nuts.spi.NSupportLevelContext;
import net.thevpc.nuts.text.NText;
import net.thevpc.nuts.text.NTexts;

public class PlainCodeHighlighter implements NCodeHighlighter {

    NWorkspace ws;

    public PlainCodeHighlighter(NWorkspace ws) {
        this.ws = ws;
    }

    @Override
    public String getId() {
        return "plain";
    }

    @Override
    public NText tokenToText(String text, String nodeType, NTexts txt, NSession session) {
        return txt.ofPlain(text);
    }

    @Override
    public NText stringToText(String text, NTexts txt, NSession session) {
        return txt.ofPlain(text);
    }

    @Override
    public int getSupportLevel(NSupportLevelContext context) {
        String s = context.getConstraints();
        if(s==null){
            return NCallableSupport.DEFAULT_SUPPORT;
        }
        switch (s){
            case "plain":
            case "text":
            case "text/plain":
            {
                return NCallableSupport.DEFAULT_SUPPORT;
            }
        }
        return NCallableSupport.NO_SUPPORT;
    }

}
