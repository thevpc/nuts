package net.thevpc.nuts.elem;


import java.lang.reflect.Type;
import java.util.function.Predicate;

public interface NElementMapperBuilder<T> {
    NElementMapper<T> build();

    public Predicate<String> getParamFieldFieldFilter() ;

    public NElementMapperBuilder<T> setParamFieldFieldFilter(Predicate<String> paramFieldFieldFilter) ;

    public Predicate<String> getBodyFieldNameFilter() ;

    public NElementMapperBuilder<T> setBodyFieldNameFilter(Predicate<String> bodyFieldNameFilter) ;
    NElementMapperBuilder<T> setWrapCollections(boolean wrapCollections);

    NElementMapperBuilder<T> setContainerIsCollection(boolean value);

    NElementMapperBuilder<T> onUnsupportedParam(NElementMapperBuilderFieldConfigurer<T> a);

    NElementMapperBuilder<T> onUnsupportedChild(NElementMapperBuilderFieldConfigurer<T> a);

    NElementMapperBuilder<T> onInitializeInstance(NElementMapperBuilderInitializer<T> a);

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
