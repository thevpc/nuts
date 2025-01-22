package net.thevpc.nuts.runtime.standalone.text.theme;

import net.thevpc.nuts.text.NTextFormatTheme;
import net.thevpc.nuts.text.NTextStyle;

import java.util.ArrayList;
import java.util.List;

import net.thevpc.nuts.text.NTextStyles;

public class NTextFormatThemeWrapper implements NTextFormatTheme {

    private NTextFormatTheme other;

    public NTextFormatThemeWrapper(NTextFormatTheme other) {
        this.other = other;
    }

    public String getName() {
        return other.getName();
    }

    @Override
    public NTextStyles toBasicStyles(NTextStyles styles, boolean basicTrueStyles) {
        NTextStyles ret = NTextStyles.PLAIN;
        if (styles != null) {
            for (NTextStyle style : styles) {
                ret = ret.append(toBasicStyles(style,basicTrueStyles));
            }
        }
        return ret;
    }

    public NTextStyles toBasicStyles(NTextStyle style,boolean basicTrueStyles) {
        if (style == null) {
            return NTextStyles.PLAIN;
        }
        if(style.getType().isBasic(basicTrueStyles)) {
            return NTextStyles.of(style);
        }
        if(style.getType().isBasic(basicTrueStyles)) {
            return NTextStyles.of(style);
        }
        NTextStyles t = other.toBasicStyles(style, basicTrueStyles);
        if (t == null) {
            return NTextStyles.PLAIN;
        }
        List<NTextStyle> rr = new ArrayList<>();
        if(basicTrueStyles){
            for (NTextStyle s : t) {
                if (s.getType().trueBasic()) {
                    rr.add(s);
                } else {
                    System.out.println("Error");
                }
            }
        }else{
            for (NTextStyle s : t) {
                if (s.getType().basic()) {
                    rr.add(s);
                } else {
                    System.out.println("Error");
                }
            }
        }
        return NTextStyles.PLAIN.append(rr.toArray(new NTextStyle[0]));
    }
}
