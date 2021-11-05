package net.thevpc.nuts;

import net.thevpc.nuts.spi.NutsComponent;
import net.thevpc.nuts.spi.NutsComponentScope;
import net.thevpc.nuts.spi.NutsComponentScopeType;

@NutsComponentScope(NutsComponentScopeType.WORKSPACE)
public interface NutsVals extends NutsComponent {
    static NutsVals of(NutsSession session) {
        return session.extensions().createSupported(NutsVals.class, true, session);
    }

    NutsVal of(Object str);
}
