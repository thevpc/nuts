package net.thevpc.nuts.runtime.standalone.xtra.glob;

import net.thevpc.nuts.*;
import net.thevpc.nuts.spi.NSupportLevelContext;
import net.thevpc.nuts.util.NGlob;

import java.io.File;
import java.util.regex.Pattern;

public class DefaultNGlob implements NGlob {
    private final NSession session;
    private String separator;


    public DefaultNGlob(NSession session) {
        this.session = session;
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
    public int getSupportLevel(NSupportLevelContext context) {
        return NConstants.Support.DEFAULT_SUPPORT;
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
