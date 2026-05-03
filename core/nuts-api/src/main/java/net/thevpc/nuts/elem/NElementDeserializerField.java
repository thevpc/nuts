package net.thevpc.nuts.elem;

import java.lang.reflect.Type;

public interface NElementDeserializerField<T> {
    NElementDeserializerField<T> ignore();

    NElementDeserializerField<T> setAlias(String... aliases);

    NElementDeserializerField<T> setType(Type type);

    NElementDeserializerField<T> setBooleanDefaultTrue();

    NElementDeserializerField<T> setBooleanDefaultFalse();

    NElementDeserializerField<T> setDefaultValue(Object valueWhenMissing);

    NElementDeserializerField<T> setWrapCollections(Boolean value);

    NElementDeserializerField<T> setContainerIsCollection(Boolean value);

    NElementDeserializerField<T> setParam(boolean param);

    NElementDeserializerField<T> setChild(boolean child);

    NElementDeserializerBuilder<T> end();

}
