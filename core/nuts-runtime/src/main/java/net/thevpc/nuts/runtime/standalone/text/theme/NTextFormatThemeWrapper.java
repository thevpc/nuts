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

    public String name() {
        return other.name();
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
        if(style.type().isBasic(basicTrueStyles)) {
            return NTextStyles.of(style);
        }
        if(style.type().isBasic(basicTrueStyles)) {
            return NTextStyles.of(style);
        }
        NTextStyles t = other.toBasicStyles(style, basicTrueStyles);
        if (t == null) {
            return NTextStyles.PLAIN;
        }
        List<NTextStyle> rr = new ArrayList<>();
        if(basicTrueStyles){
            for (NTextStyle s : t) {
                if (s.type().trueBasic()) {
                    rr.add(s);
                }
            }
        }else{
            for (NTextStyle s : t) {
                if (s.type().basic()) {
                    rr.add(s);
                }
            }
        }
        return NTextStyles.PLAIN.append(rr.toArray(new NTextStyle[0]));
    }
}
