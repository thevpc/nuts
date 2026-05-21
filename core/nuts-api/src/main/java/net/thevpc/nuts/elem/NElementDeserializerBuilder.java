package net.thevpc.nuts.elem;


import net.thevpc.nuts.util.NNameFormat;

import java.lang.reflect.Type;
import java.util.function.Function;
import java.util.function.Predicate;

public interface NElementDeserializerBuilder<T> {
    NElementDeserializer<T> build();

    Predicate<String> paramFieldFilter();

    NElementDeserializerBuilder<T> paramFieldFilter(Predicate<String> paramFieldFilter);

    Predicate<String> childFieldNameFilter();

    NElementDeserializerBuilder<T> childFieldNameFilter(Predicate<String> childFieldNameFilter);

    NElementDeserializerBuilder<T> wrapCollections(boolean wrapCollections);

    NElementDeserializerBuilder<T> containerIsCollection(boolean value);

    NElementDeserializerBuilder<T> onUnsupportedParam(NElementDeserializerFieldConfigurer<T> a);

    NElementDeserializerBuilder<T> onUnsupportedChild(NElementDeserializerFieldConfigurer<T> a);

    NElementDeserializerBuilder<T> onInitializeInstance(NElementDeserializerInitializer<T> a);

    NElementDeserializerBuilder<T> typeDefaultValue(Type type, Object defaultValue);

    NElementDeserializerBuilder<T> instanceFactory(NElementDeserializerInstanceFactory<T> instanceFactory);

    NElementDeserializerBuilder<T> booleanDefaultTrue();

    NElementDeserializerBuilder<T> booleanDefaultFalse();

    NElementDeserializerBuilder<T> configureLenient();

    NElementDeserializerBuilder<T> fieldNameNormalizer(Function<String, String> normalizer);

    NElementDeserializerBuilder<T> fieldNameNormalizer(NNameFormat normalizer);

    NElementDeserializerField<T> field(String name);

}
