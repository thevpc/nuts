package net.thevpc.nuts.runtime.standalone.text.highlighter;

import net.thevpc.nuts.spi.NCodeHighlighter;
import net.thevpc.nuts.core.NConstants;
import net.thevpc.nuts.core.NWorkspace;
import net.thevpc.nuts.text.NText;
import net.thevpc.nuts.text.NTexts;
import net.thevpc.nuts.spi.NScorableContext;

public class NtfCodeHighlighter implements NCodeHighlighter {
    NWorkspace workspace;
    public NtfCodeHighlighter(NWorkspace workspace) {
        this.workspace=workspace;
    }

    @Override
    public String getId() {
        return "ntf";
    }

    @Override
    public int getScore(NScorableContext context) {
        String s = context.getCriteria();
        if(s==null){
            return DEFAULT_SCORE;
        }
        if(NConstants.Ntf.MIME_TYPES.contains(s)){
            return DEFAULT_SCORE;
        }
        if(NConstants.Ntf.NAMES.contains(s)){
            return DEFAULT_SCORE;
        }
        return UNSUPPORTED_SCORE;
    }

    @Override
    public NText stringToText(String text, NTexts txt) {
        return txt.of(text);
    }

    @Override
    public NText tokenToText(String text, String tokenType, NTexts txt) {
        return txt.of(text);
    }
}
