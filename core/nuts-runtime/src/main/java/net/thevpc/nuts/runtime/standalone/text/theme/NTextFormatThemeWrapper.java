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
    public NTextStyles toBasicStyles(NTextStyles styles) {
        NTextStyles ret = NTextStyles.PLAIN;
        if (styles != null) {
            for (NTextStyle style : styles) {
                ret = ret.append(toBasicStyles(style));
            }
        }
        return ret;
    }

    public NTextStyles toBasicStyles(NTextStyle style) {
        if (style == null) {
            return NTextStyles.PLAIN;
        }
        if (style.getType().basic()) {
            return NTextStyles.of(style);
        }
        NTextStyles t = other.toBasicStyles(NTextStyles.of(style));
        if (t == null) {
            return NTextStyles.PLAIN;
        }
        List<NTextStyle> rr = new ArrayList<>();
        for (NTextStyle s : t) {
            if (s.getType().basic()) {
                rr.add(s);
            } else {
                //ignore...
            }
        }
        return NTextStyles.PLAIN.append(rr.toArray(new NTextStyle[0]));
    }
}
