package net.thevpc.nuts.runtime.standalone.xtra.glob;

import net.thevpc.nuts.*;
import net.thevpc.nuts.spi.NutsSupportLevelContext;

import java.io.File;
import java.util.regex.Pattern;

public class DefaultNutsGlob implements NutsGlob {
    private final NutsSession session;
    private String separator;


    public DefaultNutsGlob(NutsSession session) {
        this.session = session;
        separator= File.separator;
    }

    @Override
    public String getSeparator() {
        return separator;
    }

    @Override
    public NutsGlob setSeparator(String separator) {
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

    public String escape(String s){
        StringBuilder sb=new StringBuilder();
        for (char c : s.toCharArray()) {
            switch (c){
                case '\\':
                case '*':
                case '?':{
                    sb.append('\\').append(c);
                    break;
                }
                default:{
                    sb.append(c);
                    break;
                }
            }
        }
        return sb.toString();
    }
}
