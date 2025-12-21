package net.thevpc.nuts.runtime.standalone.extension;

public interface NBeanConstructorContext {
    boolean isSupported(Class<?> paramType);
    Object resolve(Class<?> paramType);
}
