package net.thevpc.nuts.runtime.core.format.text.stylethemes;

import net.thevpc.nuts.NutsTextFormatTheme;
import net.thevpc.nuts.NutsTextNodeStyle;
import net.thevpc.nuts.NutsWorkspace;

import java.util.ArrayList;
import java.util.List;

public class NutsTextFormatThemeWrapper implements NutsTextFormatTheme {
    private NutsTextFormatTheme other;

    public NutsTextFormatThemeWrapper(NutsTextFormatTheme other) {
        this.other = other;
    }

    public String getName() {
        return other.getName();
    }

    @Override
    public NutsTextNodeStyle[] toBasicStyles(NutsTextNodeStyle style) {
        if (style == null) {
            return new NutsTextNodeStyle[0];
        }
        if(style.getType().basic()){
            return new NutsTextNodeStyle[]{style};
        }
        NutsTextNodeStyle[] t = other.toBasicStyles(style);
        if (t == null) {
            return new NutsTextNodeStyle[0];
        }
        List<NutsTextNodeStyle> rr = new ArrayList<>();
        for (NutsTextNodeStyle s : t) {
            if (s.getType().basic()) {
                rr.add(s);
            } else {
                //ignore...
            }
        }
        return rr.toArray(new NutsTextNodeStyle[0]);
    }
}
