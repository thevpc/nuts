package net.thevpc.nuts.runtime.standalone.extension;

public interface NBeanConstructor<T> {
    T newInstance(Object[] args);
}
