package net.thevpc.nuts.runtime.bundles.iter;

import net.thevpc.nuts.NutsSession;

public interface IterInfoNodeAware {
    IterInfoNode info(NutsSession session);
}
