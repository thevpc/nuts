package net.thevpc.nuts.reflect;

import net.thevpc.nuts.concurrent.NScopedValue;
import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.util.NOptional;

public interface NBeanContainer {
    static NScopedValue<NBeanContainer> current() {
        return NReflect.of().scopedBeanContainer();
    }

    <T> NOptional<T> get(NBeanRef ref);

    default <T> NOptional<T> get(String ref) {
        return get(NBeanRef.of(ref));
    }

    default <T> NOptional<T> get(String ref, NElement variant) {
        return get(NBeanRef.of(ref, variant));
    }

    default <T> T of(NBeanRef ref) {
        return this.<T>get(ref).get();
    }

    default <T> T of(String ref) {
        return this.<T>of(NBeanRef.of(ref));
    }

    default <T> T of(String ref, NElement variant) {
        return this.<T>of(NBeanRef.of(ref, variant));
    }
}
