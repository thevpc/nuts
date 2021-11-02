package net.thevpc.nuts.runtime.core.format.text.highlighters;

import net.thevpc.nuts.NutsCodeHighlighter;
import net.thevpc.nuts.NutsSession;
import net.thevpc.nuts.NutsText;
import net.thevpc.nuts.NutsTexts;
import net.thevpc.nuts.spi.NutsSupportLevelContext;

public class NtfCodeHighlighter implements NutsCodeHighlighter {
    @Override
    public String getId() {
        return "ntf";
    }

    @Override
    public int getSupportLevel(NutsSupportLevelContext<String> context) {
        return DEFAULT_SUPPORT;
    }

    @Override
    public NutsText stringToText(String text, NutsSession session) {
        return NutsTexts.of(session).parse(text);
    }

    @Override
    public NutsText tokenToText(String text, String tokenType, NutsSession session) {
        return NutsTexts.of(session).parse(text);
    }
}
