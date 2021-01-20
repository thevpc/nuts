package net.thevpc.nuts;

import net.thevpc.nuts.NutsTextNodeStyle;
import net.thevpc.nuts.NutsWorkspace;

public interface NutsTextFormatTheme {
    String getName();
    NutsTextNodeStyle[] toBasicStyles(NutsTextNodeStyle style);
}
