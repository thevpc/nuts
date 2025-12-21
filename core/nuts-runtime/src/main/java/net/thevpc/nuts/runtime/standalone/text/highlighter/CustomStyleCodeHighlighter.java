package net.thevpc.nuts.runtime.standalone.text.highlighter;

import net.thevpc.nuts.spi.NCodeHighlighter;
import net.thevpc.nuts.util.NScore;
import net.thevpc.nuts.util.NScorable;
import net.thevpc.nuts.text.NText;
import net.thevpc.nuts.text.NTextStyle;
import net.thevpc.nuts.text.NTexts;

@NScore(fixed = NScorable.DEFAULT_SCORE)
public class CustomStyleCodeHighlighter implements NCodeHighlighter {

    private NTextStyle style;

    public CustomStyleCodeHighlighter(NTextStyle style) {
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


}
