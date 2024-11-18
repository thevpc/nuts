package net.thevpc.nuts.runtime.standalone.text.highlighter;

import net.thevpc.nuts.NCodeHighlighter;
import net.thevpc.nuts.NConstants;
import net.thevpc.nuts.NSession;
import net.thevpc.nuts.NWorkspace;
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
        switch (s){
            case "ntf":
            case "nuts-text-format":
            case "text/x-nuts-text-format":
            case "text/x-ntf":
            case "text/nuts-text-format":
            case "text/ntf":
            case "application/x-nuts-text-format":
            case "application/x-ntf":
            case "application/nuts-text-format":
            case "application/ntf":
            {
                return NConstants.Support.DEFAULT_SUPPORT;
            }
        }
        return NConstants.Support.NO_SUPPORT;
    }

    @Override
    public NText stringToText(String text, NTexts txt) {
        return txt.parse(text);
    }

    @Override
    public NText tokenToText(String text, String tokenType, NTexts txt) {
        return txt.parse(text);
    }
}
