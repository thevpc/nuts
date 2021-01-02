package net.thevpc.nuts.runtime.core.format.text.stylethemes;

import net.thevpc.nuts.NutsTextNodeStyle;
import net.thevpc.nuts.NutsWorkspace;

public interface NutsTextStyleTheme {
    NutsTextNodeStyle[] toBasicStyles(NutsTextNodeStyle textNodeStyle, NutsWorkspace workspace);
}
