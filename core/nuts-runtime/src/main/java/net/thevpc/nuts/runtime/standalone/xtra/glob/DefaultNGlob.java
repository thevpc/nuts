package net.thevpc.nuts.runtime.standalone.xtra.glob;

import net.thevpc.nuts.spi.NScorableContext;
import net.thevpc.nuts.util.NBlankable;
import net.thevpc.nuts.expr.NGlob;

import java.io.File;
import java.util.regex.Pattern;

public class DefaultNGlob implements NGlob {
    private String separator;


    public DefaultNGlob() {
        separator = File.separator;
    }

    @Override
    public String getSeparator() {
        return separator;
    }

    @Override
    public NGlob setSeparator(String separator) {
        this.separator = separator;
        return this;
    }

    @Override
    public boolean isGlob(String pattern) {
        if (pattern == null) {
            return false;
        }
        for (char c : pattern.toCharArray()) {
            switch (c) {
                case '*':
                case '?':
                case '[':
                case ']': {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public Pattern toPattern(String pattern) {
        if (NBlankable.isBlank(pattern)) {
            return GlobUtils.PATTERN_ALL;
        }
        return GlobUtils.glob(pattern, getSeparator());
    }

    @Override
    public String toPatternString(String pattern) {
        if (NBlankable.isBlank(pattern)) {
            return ".*";
        }
        return GlobUtils.globString(pattern, getSeparator());
    }

    @Override
    public int getScore(NScorableContext context) {
        return DEFAULT_SCORE;
    }

    public String escape(String s) {
        StringBuilder sb = new StringBuilder();
        for (char c : s.toCharArray()) {
            switch (c) {
                case '\\':
                case '*':
                case '?': {
                    sb.append('\\').append(c);
                    break;
                }
                default: {
                    sb.append(c);
                    break;
                }
            }
        }
        return sb.toString();
    }
}
