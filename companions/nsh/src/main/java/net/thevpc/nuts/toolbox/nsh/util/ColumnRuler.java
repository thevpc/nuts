package net.thevpc.nuts.toolbox.nsh.util;

import net.thevpc.nuts.NSession;
import net.thevpc.nuts.text.NText;
import net.thevpc.nuts.text.NTextStyle;
import net.thevpc.nuts.text.NTexts;
import net.thevpc.nuts.toolbox.nsh.util.bundles._StringUtils;

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

    public NText nextNum(long nbr, NSession session){
        return NText.ofStyled(next(String.valueOf(nbr)),
                NTextStyle.number()
        );
    }
}
