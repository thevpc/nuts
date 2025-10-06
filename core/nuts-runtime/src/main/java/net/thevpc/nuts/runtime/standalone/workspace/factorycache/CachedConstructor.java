package net.thevpc.nuts.runtime.standalone.workspace.factorycache;

import net.thevpc.nuts.core.NSession;

public interface CachedConstructor<T> {
    T newInstance(Object[] args,NSession session);
}
