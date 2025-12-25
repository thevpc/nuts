package net.thevpc.nuts.runtime.standalone.text.highlighter;

import net.thevpc.nuts.core.NWorkspace;
import net.thevpc.nuts.io.NIOException;
import net.thevpc.nuts.platform.NShellFamily;
import net.thevpc.nuts.runtime.standalone.text.parser.DefaultNTextPlain;
import net.thevpc.nuts.runtime.standalone.xtra.expr.StringReaderExt;
import net.thevpc.nuts.spi.NCodeHighlighter;
import net.thevpc.nuts.text.*;
import net.thevpc.nuts.util.NScorable;
import net.thevpc.nuts.util.NScorableContext;
import net.thevpc.nuts.util.NScore;
import net.thevpc.nuts.util.NStringUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ShoptCodeHighlighter extends ShCodeHighlighter {


    public ShoptCodeHighlighter() {
        this.opOnly = true;
    }

    private static String resolveKind(NScorableContext context) {
        if (context == null) {
            return "shopt";
        }
        if (!(context.getCriteria() instanceof String)) {
            return "shopt";
        }
        return resolveKind((String) context.getCriteria());
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
                switch (NShellFamily.getCurrent()) {
                    case SH:
                    case BASH:
                    case CSH:
                    case ZSH:
                    case KSH: {
                        return NShellFamily.getCurrent().id() + "opt";
                    }
                }
                return "shopt";
            }
        }
        return "shopt";
    }

    @Override
    public String getId() {
        return "shopt";
    }

    @NScore
    public static int getScore(NScorableContext context) {
        String s = context.getCriteria();
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
                switch (NShellFamily.getCurrent()) {
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
