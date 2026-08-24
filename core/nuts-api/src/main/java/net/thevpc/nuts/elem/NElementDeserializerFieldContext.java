package net.thevpc.nuts.elem;

import java.lang.reflect.Type;

/**
 * NElementDeserializerFieldContext interface.
 *
 * @author thevpc
 * @since 0.8.0
 */
public interface NElementDeserializerFieldContext<T> extends NElementDeserializerInstanceContext<T> {
    /**
     * Field.
     *
     * @return field result
     */
    NElement field();
}
