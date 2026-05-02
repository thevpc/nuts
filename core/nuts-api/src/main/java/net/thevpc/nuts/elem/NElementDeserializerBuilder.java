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

    NElementDeserializerBuilder<T> onUnsupportedParam(NElementDeserializerBuilderFieldConfigurer<T> a);

    NElementDeserializerBuilder<T> onUnsupportedChild(NElementDeserializerBuilderFieldConfigurer<T> a);

    NElementDeserializerBuilder<T> onInitializeInstance(NElementDeserializerBuilderInitializer<T> a);

    NElementDeserializerBuilder<T> setTypeDefaultValue(Type type, Object defaultValue);

    NElementDeserializerBuilder<T> setInstanceFactory(NElementDeserializerBuilderInstanceFactory<T> instanceFactory);

    NElementDeserializerBuilder<T> setBooleanDefaultTrue();

    NElementDeserializerBuilder<T> setBooleanDefaultFalse();

    NElementDeserializerBuilder<T> configureLenient();
    NElementDeserializerBuilder<T> fieldNameNormalizer(Function<String, String> normalizer);
    NElementDeserializerBuilder<T> fieldNameNormalizer(NNameFormat normalizer);

    FieldConfig<T> field(String name);

    interface FieldConfig<T> {
        FieldConfig<T> ignore();

        FieldConfig<T> setAlias(String... aliases);

        FieldConfig<T> setType(Type type);

        FieldConfig<T> setBooleanDefaultTrue();

        FieldConfig<T> setBooleanDefaultFalse();

        FieldConfig<T> setDefaultValue(Object valueWhenMissing);

        FieldConfig<T> setWrapCollections(Boolean value);

        FieldConfig<T> setContainerIsCollection(Boolean value);

        FieldConfig<T> setParam(boolean param);

        FieldConfig<T> setChild(boolean child);

        NElementDeserializerBuilder<T> end();

    }

}
