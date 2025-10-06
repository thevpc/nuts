package net.thevpc.nuts.runtime.standalone.text.highlighter;

import net.thevpc.nuts.spi.NCodeHighlighter;
import net.thevpc.nuts.core.NConstants;
import net.thevpc.nuts.core.NWorkspace;
import net.thevpc.nuts.text.NText;
import net.thevpc.nuts.text.NTexts;
import net.thevpc.nuts.spi.NSupportLevelContext;

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
    public int getSupportLevel(NSupportLevelContext context) {
        String s = context.getConstraints();
        if(s==null){
            return NConstants.Support.DEFAULT_SUPPORT;
        }
        if(NConstants.Ntf.MIME_TYPES.contains(s)){
            return NConstants.Support.DEFAULT_SUPPORT;
        }
        if(NConstants.Ntf.NAMES.contains(s)){
            return NConstants.Support.DEFAULT_SUPPORT;
        }
        return NConstants.Support.NO_SUPPORT;
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
