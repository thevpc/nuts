package net.thevpc.nuts.elem;


import java.lang.reflect.Type;
import java.util.function.Predicate;

public interface NElementMapperBuilder<T> {
    NElementMapper<T> build();

    NElementMapperBuilder<T> addAllFields();

    NElementMapperBuilder<T> addFields(Predicate<String> fieldFilter);

    NElementMapperBuilder<T> setWrapCollections(boolean wrapCollections);

    NElementMapperBuilder<T> setContainerIsCollection(boolean value);

    NElementMapperBuilder<T> addFields(String... names);

    NElementMapperBuilder<T> onUnsupportedParam(NElementMapperBuilderFieldConfigurer<T> a);

    NElementMapperBuilder<T> onUnsupportedChild(NElementMapperBuilderFieldConfigurer<T> a);

    NElementMapperBuilder<T> onInitializeInstance(NElementMapperBuilderInitializer<T> a);

    NElementMapperBuilder.FieldConfig<T> addField(String name);

    NElementMapperBuilder<T> removeFields(String... names);

    boolean removeField(String name);

    NElementMapperBuilder<T> setTypeDefaultValue(Type type, Object defaultValue);

    NElementMapperBuilder<T> setInstanceFactory(NElementMapperBuilderInstanceFactory<T> instanceFactory);

    NElementMapperBuilder<T> setBooleanDefaultTrue();

    NElementMapperBuilder<T> setBooleanDefaultFalse();

    NElementMapperBuilder<T> configureLenient();

    interface FieldConfig<T> {

        FieldConfig<T> setBooleanDefaultTrue();

        FieldConfig<T> setBooleanDefaultFalse();

        FieldConfig<T> setDefaultValue(Object valueWhenMissing);

        FieldConfig<T> setWrapCollections(Boolean value);

        FieldConfig<T> setContainerIsCollection(Boolean value);

        FieldConfig<T> setParam(boolean param);

        FieldConfig<T> setChild(boolean child);

        NElementMapperBuilder<T> end();
    }

}
