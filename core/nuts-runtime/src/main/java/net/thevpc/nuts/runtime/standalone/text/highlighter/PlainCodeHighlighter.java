package net.thevpc.nuts.runtime.standalone.text.highlighter;

import net.thevpc.nuts.core.NWorkspace;
import net.thevpc.nuts.spi.NCodeHighlighter;
import net.thevpc.nuts.util.NScorableContext;
import net.thevpc.nuts.text.NText;
import net.thevpc.nuts.text.NTexts;

public class PlainCodeHighlighter implements NCodeHighlighter {

    NWorkspace workspace;

    public PlainCodeHighlighter(NWorkspace workspace) {
        this.workspace = workspace;
    }

    @Override
    public String getId() {
        return "plain";
    }

    @Override
    public NText tokenToText(String text, String nodeType, NTexts txt) {
        return txt.ofPlain(text);
    }

    @Override
    public NText stringToText(String text, NTexts txt) {
        return txt.ofPlain(text);
    }

    @Override
    public int getScore(NScorableContext context) {
        String s = context.getCriteria();
        if(s==null){
            return DEFAULT_SCORE;
        }
        switch (s){
            case "plain":
            case "text":
            case "text/plain":
            {
                return DEFAULT_SCORE;
            }
        }
        return UNSUPPORTED_SCORE;
    }

}
