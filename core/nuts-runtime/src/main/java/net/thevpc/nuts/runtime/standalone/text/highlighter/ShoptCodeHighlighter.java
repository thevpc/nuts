package net.thevpc.nuts.runtime.standalone.text.highlighter;

import net.thevpc.nuts.platform.NShellFamily;
import net.thevpc.nuts.util.NScorable;
import net.thevpc.nuts.util.NScorableContext;
import net.thevpc.nuts.util.NScore;
import net.thevpc.nuts.util.NStringUtils;

public class ShoptCodeHighlighter extends ShCodeHighlighter {


    public ShoptCodeHighlighter() {
        this.opOnly = true;
    }

    private static String resolveKind(NScorableContext context) {
        if (context == null) {
            return "shopt";
        }
        if (!(context.criteria() instanceof String)) {
            return "shopt";
        }
        return resolveKind((String) context.criteria());
    }

    private static String resolveKind(String kind) {
        if (kind == null) {
            return "shopt";
        }
        String k = NStringUtils.trim((String) kind).toLowerCase();
        switch (k) {
            case "shopt":
            case "bashopt":
            case "cshopt":
            case "zshopt":
            case "kshopt":
                return k;
            case "text/x-shellscript-options": {
                return "sh";
            }
            case "systemopt":
            case "sysopt": {
                switch (NShellFamily.current()) {
                    case SH:
                    case BASH:
                    case CSH:
                    case ZSH:
                    case KSH: {
                        return NShellFamily.current().id() + "opt";
                    }
                }
                return "shopt";
            }
        }
        return "shopt";
    }

    @Override
    public String id() {
        return "shopt";
    }

    @NScore
    public static int getScore(NScorableContext context) {
        String s = context.criteria();
        if (s == null) {
            return NScorable.DEFAULT_SCORE;
        }
        switch (s) {
            case "shopt":
            case "bashopt":
            case "cshopt":
            case "zshopt":
            case "kshopt":
            case "text/x-shellscript-options": {
                return NScorable.DEFAULT_SCORE;
            }
            case "systemopt":
            case "systopt": {
                switch (NShellFamily.current()) {
                    case SH:
                    case BASH:
                    case CSH:
                    case ZSH:
                    case KSH: {
                        return NScorable.DEFAULT_SCORE + 10;
                    }
                }
                return NScorable.DEFAULT_SCORE;
            }
        }
        return NScorable.UNSUPPORTED_SCORE;
    }

}
