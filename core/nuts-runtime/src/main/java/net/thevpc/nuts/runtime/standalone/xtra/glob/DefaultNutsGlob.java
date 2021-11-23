package net.thevpc.nuts.runtime.standalone.xtra.glob;

import net.thevpc.nuts.*;
import net.thevpc.nuts.spi.NutsSupportLevelContext;

import java.io.File;
import java.util.regex.Pattern;

public class DefaultNutsGlob implements NutsGlob {
    private final NutsSession session;
    private char separator;


    public DefaultNutsGlob(NutsSession session) {
        this.session = session;
        separator= File.separatorChar;
    }

    @Override
    public char getSeparator() {
        return separator;
    }

    @Override
    public NutsGlob setSeparator(char separator) {
        this.separator = separator;
        return this;
    }

    @Override
    public boolean isGlob(String pattern) {
        if(pattern==null){
            return false;
        }
        for (char c : pattern.toCharArray()) {
            switch (c){
                case '*':
                case '?':
                case '[':
                case ']':{
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public Pattern toPattern(String pattern) {
        if (NutsBlankable.isBlank(pattern)) {
            return GlobUtils.PATTERN_ALL;
        }
        return GlobUtils.glob(pattern, getSeparator());
    }

    @Override
    public int getSupportLevel(NutsSupportLevelContext context) {
        return DEFAULT_SUPPORT;
    }
}
