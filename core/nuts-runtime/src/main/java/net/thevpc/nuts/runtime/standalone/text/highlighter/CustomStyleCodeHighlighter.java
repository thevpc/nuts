package net.thevpc.nuts.runtime.standalone.text.highlighter;

import net.thevpc.nuts.*;
import net.thevpc.nuts.NConstants;
import net.thevpc.nuts.spi.NSupportLevelContext;
import net.thevpc.nuts.text.NText;
import net.thevpc.nuts.text.NTextStyle;
import net.thevpc.nuts.text.NTexts;

public class CustomStyleCodeHighlighter implements NCodeHighlighter {

    private NWorkspace workspace;
    private NTextStyle style;

    public CustomStyleCodeHighlighter(NTextStyle style, NWorkspace workspace) {
        this.workspace = workspace;
        this.style = style;
    }

    @Override
    public String getId() {
        return "styled("+style+")";
    }

    @Override
    public NText stringToText(String text, NTexts txt) {
        return txt.ofStyled(text, style);
    }

    @Override
    public NText tokenToText(String text, String nodeType, NTexts txt) {
        return txt.ofPlain(text);
    }

    @Override
    public int getSupportLevel(NSupportLevelContext context) {
        return NConstants.Support.DEFAULT_SUPPORT;
    }

}
