package net.thevpc.nuts.runtime.standalone.text.highlighter;

import net.thevpc.nuts.*;
import net.thevpc.nuts.spi.NutsSupportLevelContext;
import net.thevpc.nuts.spi.NutsComponent;
import net.thevpc.nuts.text.NutsText;
import net.thevpc.nuts.text.NutsTextStyle;
import net.thevpc.nuts.text.NutsTexts;

public class CustomStyleCodeHighlighter implements NutsCodeHighlighter {

    private NutsSession session;
    private NutsTextStyle style;

    public CustomStyleCodeHighlighter(NutsTextStyle style, NutsSession session) {
        this.session = session;
        this.style = style;
    }

    @Override
    public String getId() {
        return "styled("+style+")";
    }

    @Override
    public NutsText stringToText(String text, NutsTexts txt, NutsSession session) {
        return txt.applyStyles(txt.ofPlain(text), style);
    }

    @Override
    public NutsText tokenToText(String text, String nodeType, NutsTexts txt, NutsSession session) {
        return txt.ofPlain(text);
    }

    @Override
    public int getSupportLevel(NutsSupportLevelContext context) {
        return NutsComponent.DEFAULT_SUPPORT;
    }

}
