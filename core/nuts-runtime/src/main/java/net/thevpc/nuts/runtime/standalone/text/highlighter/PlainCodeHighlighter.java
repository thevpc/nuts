package net.thevpc.nuts.runtime.standalone.text.highlighter;

import net.thevpc.nuts.*;
import net.thevpc.nuts.spi.NutsSupportLevelContext;
import net.thevpc.nuts.spi.NutsComponent;
import net.thevpc.nuts.NutsTexts;

public class PlainCodeHighlighter implements NutsCodeHighlighter {

    NutsWorkspace ws;

    public PlainCodeHighlighter(NutsWorkspace ws) {
        this.ws = ws;
    }

    @Override
    public String getId() {
        return "plain";
    }

    @Override
    public NutsText tokenToText(String text, String nodeType, NutsTexts txt, NutsSession session) {
        return txt.ofPlain(text);
    }

    @Override
    public NutsText stringToText(String text, NutsTexts txt, NutsSession session) {
        return txt.ofPlain(text);
    }

    @Override
    public int getSupportLevel(NutsSupportLevelContext context) {
        String s = context.getConstraints();
        if(s==null){
            return DEFAULT_SUPPORT;
        }
        switch (s){
            case "plain":
            case "text":
            case "text/plain":
            {
                return NutsComponent.DEFAULT_SUPPORT;
            }
        }
        return NutsComponent.NO_SUPPORT;
    }

}
