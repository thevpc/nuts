package net.thevpc.nuts.elem;

import java.lang.reflect.Type;

public interface NElementDeserializerFieldContext<T> extends NElementDeserializerInstanceContext<T> {
    NElement field();
}
