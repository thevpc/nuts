package net.thevpc.nuts.reflect;

import net.thevpc.nuts.concurrent.NScopedStack;
import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.util.NOptional;

/**
 * NBeanContainer interface.
 *
 * @author thevpc
 * @since 0.8.0
 */
public interface NBeanContainer {
    /**
     * Scoped stack.
     *
     * @return scoped stack result
     */
    static NScopedStack<NBeanContainer> scopedStack() {
        return NReflect.of().scopedBeanContainerStack();
    }
    /**
     * Current.
     *
     * @return current result
     */
    static NBeanContainer current() {
        return NReflect.of().scopedBeanContainer();
    }

    /**
     * Returns the get.
     *
     * @param ref ref
     * @return get result
     */
    <T> NOptional<T> get(NBeanRef ref);

    /**
     * Returns the get.
     *
     * @param ref ref
     * @return get result
     */
    default <T> NOptional<T> get(String ref) {
        /**
         * Returns the get.
         *
         * @param NBeanRef.of(ref) n bean ref.of(ref)
         * @return get result
         */
        return get(NBeanRef.of(ref));
    }

    /**
     * Returns the get.
     *
     * @param ref ref
     * @param variant variant
     * @return get result
     */
    default <T> NOptional<T> get(String ref, NElement variant) {
        /**
         * Returns the get.
         *
         * @param variant) variant)
         * @return get result
         */
        return get(NBeanRef.of(ref, variant));
    }

    /**
     * Creates a new instance of of.
     *
     * @param ref ref
     * @return of result
     */
    default <T> T of(NBeanRef ref) {
        return this.<T>get(ref).get();
    }

    /**
     * Creates a new instance of of.
     *
     * @param ref ref
     * @return of result
     */
    default <T> T of(String ref) {
        return this.<T>of(NBeanRef.of(ref));
    }

    /**
     * Creates a new instance of of.
     *
     * @param ref ref
     * @param variant variant
     * @return of result
     */
    default <T> T of(String ref, NElement variant) {
        return this.<T>of(NBeanRef.of(ref, variant));
    }
}
