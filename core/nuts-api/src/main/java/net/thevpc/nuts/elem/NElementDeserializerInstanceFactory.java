package net.thevpc.nuts.elem;

/**
 * NElementDeserializerInstanceFactory interface.
 *
 * @author thevpc
 * @since 0.8.0
 */
public interface NElementDeserializerInstanceFactory<T> {
    /**
     * New instance.
     *
     * @param context context
     * @return new instance result
     */
    T newInstance(NElementDeserializerContext context);
}
