package net.thevpc.nuts.elem;


import java.lang.reflect.Type;

public interface NElementMapperBuilder<T> {
    NElementMapper<T> build();

    NElementMapperBuilder<T> addAllFields();

    NElementMapperBuilder<T> setWrapCollections(boolean wrapCollections);

    NElementMapperBuilder<T> setContainerIsCollection(boolean value);

    NElementMapperBuilder<T> addFields(String... names);

    NElementMapperBuilder<T> onUnsupportedArg(MissingFieldConfigurer<T> a);

    NElementMapperBuilder<T> onUnsupportedBody(MissingFieldConfigurer<T> a);

    NElementMapperBuilder<T> postProcess(InstanceConfigurer<T> a);

    NElementMapperBuilder.FieldConfig<T> addField(String name);

    NElementMapperBuilder<T> setDefaultValueByType(Type type, Object defaultValue);

    NElementMapperBuilder<T> setInstanceFactory(InstanceFactory<T> instanceFactory);

    NElementMapperBuilder<T> setTrueDefault();

    NElementMapperBuilder<T> configureLenient();

    interface FieldConfig<T> {

        FieldConfig<T> setTrueDefault();

        FieldConfig<T> setDefaultValue(Object valueWhenMissing);

        FieldConfig<T> setWrapCollections(Boolean value);

        FieldConfig<T> setContainerIsCollection(Boolean value);

        FieldConfig<T> setArg(boolean arg);

        FieldConfig<T> setBody(boolean body);

        NElementMapperBuilder<T> end();
    }

    interface InstanceFactory<T2> {
        T2 newInstance(FactoryConfigurerContext<T2> context);
    }

    interface FactoryConfigurerContext<T2> {
        NElement element();

        Class<T2> to();

        <T> NElement elem(T any);

        <T> T obj(NElement element, Class<T> clazz);
    }

    interface InstanceConfigurerContext<T2> {
        T2 instance();

        NElement element();

        Class<T2> to();

        <T> NElement elem(T any);

        <T> T obj(NElement element, Class<T> clazz);
    }

    interface FieldConfigurerContext<T2> {
        T2 instance();

        NElement field();

        NElement element();

        Class<T2> to();

        <T> NElement elem(T any);

        <T> T obj(NElement element, Class<T> clazz);
    }

    interface InstanceConfigurer<T2> {
        boolean prepareInstance(InstanceConfigurerContext<T2> context);
    }

    interface MissingFieldConfigurer<T2> {
        boolean prepareField(FieldConfigurerContext<T2> context);
    }
}
