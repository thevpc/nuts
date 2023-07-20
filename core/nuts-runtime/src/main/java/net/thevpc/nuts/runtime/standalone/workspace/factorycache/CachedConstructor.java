package net.thevpc.nuts.runtime.standalone.workspace.factorycache;

import net.thevpc.nuts.NSession;

import java.lang.reflect.Constructor;

public interface CachedConstructor<T> {
    T newInstance(Object[] args,NSession session);
}
