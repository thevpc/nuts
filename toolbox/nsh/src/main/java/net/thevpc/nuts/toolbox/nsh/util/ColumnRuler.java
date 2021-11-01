package net.thevpc.nuts.toolbox.nsh.util;

import net.thevpc.nuts.NutsSession;
import net.thevpc.nuts.NutsString;
import net.thevpc.nuts.NutsTextStyle;
import net.thevpc.nuts.NutsTexts;
import net.thevpc.nuts.toolbox.nsh.bundles._StringUtils;

public class ColumnRuler {
    private int width;

    public ColumnRuler() {
        this(6);
    }
    public ColumnRuler(int width) {
        this.width = width;
    }

    public String next(String ruleText){
        if (width <= ruleText.length()) {
            width = ruleText.length() + 1;
        }
        return _StringUtils.formatRight(ruleText, width)+" ";
    }

    public NutsString nextNum(long nbr, NutsSession session){
        return NutsTexts.of(session).ofStyled(next(String.valueOf(nbr)),
                NutsTextStyle.number()
        );
    }
}
