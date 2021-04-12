package net.thevpc.nuts.runtime.core.format.text.stylethemes;

import net.thevpc.nuts.NutsTextFormatTheme;
import net.thevpc.nuts.NutsTextNodeStyle;

import java.util.ArrayList;
import java.util.List;
import net.thevpc.nuts.NutsSession;
import net.thevpc.nuts.NutsTextNodeStyles;

public class NutsTextFormatThemeWrapper implements NutsTextFormatTheme {

    private NutsTextFormatTheme other;

    public NutsTextFormatThemeWrapper(NutsTextFormatTheme other) {
        this.other = other;
    }

    public String getName() {
        return other.getName();
    }

    @Override
    public NutsTextNodeStyles toBasicStyles(NutsTextNodeStyles styles, NutsSession session) {
        NutsTextNodeStyles ret = NutsTextNodeStyles.NONE;
        if (styles != null) {
            for (NutsTextNodeStyle style : styles) {
                ret = ret.append(toBasicStyles(style,session));
            }
        }
        return ret;
    }

    public NutsTextNodeStyles toBasicStyles(NutsTextNodeStyle style,NutsSession session) {
        if (style == null) {
            return NutsTextNodeStyles.NONE;
        }
        if (style.getType().basic()) {
            return NutsTextNodeStyles.of(style);
        }
        NutsTextNodeStyles t = other.toBasicStyles(NutsTextNodeStyles.of(style), session);
        if (t == null) {
            return NutsTextNodeStyles.NONE;
        }
        List<NutsTextNodeStyle> rr = new ArrayList<>();
        for (NutsTextNodeStyle s : t) {
            if (s.getType().basic()) {
                rr.add(s);
            } else {
                //ignore...
            }
        }
        return NutsTextNodeStyles.NONE.append(rr.toArray(new NutsTextNodeStyle[0]));
    }
}
