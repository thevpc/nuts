package net.thevpc.nuts.runtime.standalone.ext;

public interface ImplHelper<T> {
    T createApiTypeInstance(String name,Class[] argTypes,Object[] args);
}
