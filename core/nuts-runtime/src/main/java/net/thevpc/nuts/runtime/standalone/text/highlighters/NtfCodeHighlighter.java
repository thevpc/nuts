package net.thevpc.nuts.runtime.standalone.text.highlighters;

import net.thevpc.nuts.NutsCodeHighlighter;
import net.thevpc.nuts.NutsSession;
import net.thevpc.nuts.NutsText;
import net.thevpc.nuts.NutsTexts;
import net.thevpc.nuts.spi.NutsComponent;
import net.thevpc.nuts.spi.NutsSupportLevelContext;

public class NtfCodeHighlighter implements NutsCodeHighlighter {
    public NtfCodeHighlighter(NutsSession session) {
    }

    @Override
    public String getId() {
        return "ntf";
    }

    @Override
    public int getSupportLevel(NutsSupportLevelContext context) {
        String s = context.getConstraints();
        if(s==null){
            return DEFAULT_SUPPORT;
        }
        switch (s){
            case "ntf":
            case "nuts-text-format":
            case "text/x-nuts-text-format":
            {
                return NutsComponent.DEFAULT_SUPPORT;
            }
        }
        return NutsComponent.NO_SUPPORT;
    }

    @Override
    public NutsText stringToText(String text, NutsTexts txt, NutsSession session) {
        return txt.parse(text);
    }

    @Override
    public NutsText tokenToText(String text, String tokenType, NutsTexts txt, NutsSession session) {
        return txt.parse(text);
    }
}
