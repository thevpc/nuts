package net.thevpc.nuts.runtime.standalone.text.highlighter;

import net.thevpc.nuts.spi.NCodeHighlighter;
import net.thevpc.nuts.reflect.NScore;
import net.thevpc.nuts.reflect.NScorable;
import net.thevpc.nuts.text.NText;
import net.thevpc.nuts.text.NTextStyle;

@NScore(fixed = NScorable.DEFAULT_SCORE)
public class CustomStyleCodeHighlighter implements NCodeHighlighter {

    private NTextStyle style;

    public CustomStyleCodeHighlighter(NTextStyle style) {
        this.style = style;
    }

    @Override
    public String id() {
        return "styled("+style+")";
    }

    @Override
    public NText stringToText(String text) {
        return NText.ofStyled(text, style);
    }

    @Override
    public NText tokenToText(String text, String nodeType) {
        return NText.ofPlain(text);
    }


}
