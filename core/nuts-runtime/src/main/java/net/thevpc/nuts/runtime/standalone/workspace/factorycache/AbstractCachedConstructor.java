package net.thevpc.nuts.runtime.standalone.workspace.factorycache;

import net.thevpc.nuts.NSession;

import java.lang.reflect.Constructor;

public abstract class AbstractCachedConstructor<T> implements CachedConstructor<T>{
    protected Constructor<T> c;

    public AbstractCachedConstructor(Constructor<T> c) {
        this.c = c;
        this.c.setAccessible(true);
    }

    protected abstract T newInstanceUnsafe(Object[] args, NSession session) throws ReflectiveOperationException;

    @Override
    public T newInstance(Object[] args, NSession session) {
        try {
            return newInstanceUnsafe(args,session);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }
}
