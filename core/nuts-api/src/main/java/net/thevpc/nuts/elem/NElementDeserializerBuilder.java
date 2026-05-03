package net.thevpc.nuts.elem;


import net.thevpc.nuts.util.NNameFormat;

import java.lang.reflect.Type;
import java.util.function.Function;
import java.util.function.Predicate;

public interface NElementDeserializerBuilder<T> {
    NElementDeserializer<T> build();

    Predicate<String> getParamFieldFilter();

    NElementDeserializerBuilder<T> setParamFieldFilter(Predicate<String> paramFieldFilter);

    Predicate<String> getChildFieldNameFilter();

    NElementDeserializerBuilder<T> setChildFieldNameFilter(Predicate<String> childFieldNameFilter);

    NElementDeserializerBuilder<T> setWrapCollections(boolean wrapCollections);

    NElementDeserializerBuilder<T> setContainerIsCollection(boolean value);

    NElementDeserializerBuilder<T> onUnsupportedParam(NElementDeserializerFieldConfigurer<T> a);

    NElementDeserializerBuilder<T> onUnsupportedChild(NElementDeserializerFieldConfigurer<T> a);

    NElementDeserializerBuilder<T> onInitializeInstance(NElementDeserializerInitializer<T> a);

    NElementDeserializerBuilder<T> setTypeDefaultValue(Type type, Object defaultValue);

    NElementDeserializerBuilder<T> setInstanceFactory(NElementDeserializerInstanceFactory<T> instanceFactory);

    NElementDeserializerBuilder<T> setBooleanDefaultTrue();

    NElementDeserializerBuilder<T> setBooleanDefaultFalse();

    NElementDeserializerBuilder<T> configureLenient();
    NElementDeserializerBuilder<T> fieldNameNormalizer(Function<String, String> normalizer);
    NElementDeserializerBuilder<T> fieldNameNormalizer(NNameFormat normalizer);

    NElementDeserializerField<T> field(String name);

}
