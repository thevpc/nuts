package net.thevpc.nuts.runtime.standalone.tson;

import java.lang.reflect.Type;

public interface TsonCustomDeserializer<T> extends TsonElementToObject<T> {
    TsonCustomDeserializer<T> addAllFields();

    TsonCustomDeserializer<T> setWrapCollections(boolean wrapCollections);
    TsonCustomDeserializer<T> setContainerIsCollection(boolean value) ;

    TsonCustomDeserializer<T> addFields(String... names);

    TsonCustomDeserializer<T> onUnsupportedArg(MissingFieldConfigurer<T> a);

    TsonCustomDeserializer<T> onUnsupportedBody(MissingFieldConfigurer<T> a);

    TsonCustomDeserializer<T> postProcess(InstanceConfigurer<T> a);

    TsonCustomDeserializer.FieldConfig<T> addField(String name);

    TsonCustomDeserializer<T> setDefaultValueByType(Type type, Object defaultValue);

    TsonCustomDeserializer<T> setInstanceFactory(InstanceFactory<T> instanceFactory);

    TsonCustomDeserializer<T> setTrueDefault();

    TsonCustomDeserializer<T> configureLenient();

    interface FieldConfig<T> {

        FieldConfig<T> setTrueDefault();

        FieldConfig<T> setDefaultValue(Object valueWhenMissing);

        FieldConfig<T> setWrapCollections(Boolean value);

        FieldConfig<T> setContainerIsCollection(Boolean value) ;
        FieldConfig<T> setArg(boolean arg);

        FieldConfig<T> setBody(boolean body);

        TsonCustomDeserializer<T> end();
    }

    interface InstanceFactory<T2> {
        T2 newInstance(FactoryConfigurerContext<T2> context);
    }

    interface FactoryConfigurerContext<T2> {
        TsonElement element();

        Class<T2> to();

        <T> TsonElement elem(T any);

        <T> T obj(TsonElement element, Class<T> clazz);
    }

    interface InstanceConfigurerContext<T2> {
        T2 instance();

        TsonElement element();

        Class<T2> to();

        <T> TsonElement elem(T any);

        <T> T obj(TsonElement element, Class<T> clazz);
    }

    interface FieldConfigurerContext<T2> {
        T2 instance();

        TsonElement field();

        TsonElement element();

        Class<T2> to();

        <T> TsonElement elem(T any);

        <T> T obj(TsonElement element, Class<T> clazz);
    }

    interface InstanceConfigurer<T2> {
        boolean prepareInstance(InstanceConfigurerContext<T2> context);
    }

    interface MissingFieldConfigurer<T2> {
        boolean prepareField(FieldConfigurerContext<T2> context);
    }
}
